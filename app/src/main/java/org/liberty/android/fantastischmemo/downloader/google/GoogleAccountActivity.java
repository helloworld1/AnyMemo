/*
Copyright (C) 2012 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.downloader.google;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.common.base.Strings;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.downloader.oauth.Oauth2AccountActivity;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccessCodeRetrievalFragment;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccountActivity;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GoogleAccountActivity extends Oauth2AccountActivity {

    private static final String TAG = GoogleAccountActivity.class.getSimpleName();

    @Override
    protected void onAuthenticated(String authTokens) {

    }

    @Override
    protected Single<String> fetchAuthTokenFromCallback(Uri callbackUri) {
        final String codePartPrefix = "code=";

        String part = callbackUri.getEncodedQuery();

        final String code = part.startsWith(codePartPrefix)
                ? part.substring(codePartPrefix.length())
                : null;

        if (!Strings.isNullOrEmpty(code)) {
            Log.i(TAG, "Auth code: " + code);

            return Single.create(new SingleOnSubscribe<String>() {
                @Override
                public void subscribe(SingleEmitter<String> e) throws Exception {
                    String token = getAccessTokensFromCode(code);
                    Log.i(TAG, "Auth token: " + token);

                }
            });
        }

        return Single.error(new RuntimeException("Unable to find auth token from callback: " + callbackUri));
    }

    @Override
    protected Completable verifyAccessToken(@NonNull final String accessToken) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                URL url1 = new URL("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + accessToken);
                HttpsURLConnection conn = (HttpsURLConnection) url1.openConnection();

                String s = new String(IOUtils.toByteArray(conn.getInputStream()));
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.has("error")) {
                    String error = jsonObject.getString("error");

                    throw new RuntimeException("Token validation error: " + error);
                }

                String audience = jsonObject.getString("audience");
                if (!AMEnv.GOOGLE_CLIENT_ID.equals(audience)) {
                    throw new RuntimeException("Token validation error: Google Client id does not equal to audience");
                }
            }
        });
    }

    @Override
    protected String getLoginUrl() {
        try {
            return String.format("https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&response_type=%s&redirect_uri=%s&scope=%s",
                    URLEncoder.encode(AMEnv.GOOGLE_CLIENT_ID, "UTF-8"),
                    URLEncoder.encode("code", "UTF-8"),
                    URLEncoder.encode(AMEnv.GOOGLE_REDIRECT_URI, "UTF-8"),
                    URLEncoder.encode(AMEnv.GDRIVE_SCOPE, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Login Url encoding error", e);
            return null;
        }
    }

    @WorkerThread
    private String getAccessTokensFromCode(final String code) throws IOException, JSONException {
        URL url1 = new URL("https://www.googleapis.com/oauth2/v4/token");

        String payload = String.format("code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=%s",
                code,
                AMEnv.GOOGLE_CLIENT_ID,
                AMEnv.GOOGLE_CLIENT_SECRET,
                AMEnv.GOOGLE_REDIRECT_URI,
                "authorization_code");

        Request request = new Request.Builder()
                .url(url1)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), payload))
                .build();

        Response response = appComponents().okHttpClient().newCall(request).execute();
        if (response.isSuccessful()) {
            String stringBody = response.body().string();
            JSONObject jsonObject = new JSONObject(stringBody);
            return jsonObject.getString("access_token");
        } else {
            throw new IOException("Error getting google access token from code: " + response);
        }
    }
}
