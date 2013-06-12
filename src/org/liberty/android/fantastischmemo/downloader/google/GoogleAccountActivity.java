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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccessCodeRetrievalFragment;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccountActivity;

import android.util.Log;

public abstract class GoogleAccountActivity extends OauthAccountActivity {

    @Override
    protected boolean verifyAccessToken(final String[] accessTokens) throws IOException {
        String token = accessTokens[0];
        try {
            URL url1 = new URL("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + token);
            HttpsURLConnection conn = (HttpsURLConnection) url1.openConnection();

            String s = new String(IOUtils.toByteArray(conn.getInputStream()));
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.has("error")) {
                String error = jsonObject.getString("error");
                Log.e(TAG, "Token validation error: " + error);
                return false;
            }

            String audience = jsonObject.getString("audience");
            return AMEnv.GOOGLE_CLIENT_ID.equals(audience);
        } catch (Exception e) {
            Log.i(TAG, "The saved access token is invalid", e);
        }
        return false;
    }

    @Override
    protected String[] getAccessTokens(final String[] requests) throws IOException {
        String code = requests[0];
        URL url1 = new URL("https://accounts.google.com/o/oauth2/token");
        HttpsURLConnection conn = (HttpsURLConnection) url1.openConnection();
        conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        String payload = String.format("code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=%s",
                URLEncoder.encode(code, "UTF-8"),
                URLEncoder.encode(AMEnv.GOOGLE_CLIENT_ID, "UTF-8"),
                URLEncoder.encode(AMEnv.GOOGLE_CLIENT_SECRET, "UTF-8"),
                URLEncoder.encode(AMEnv.GOOGLE_REDIRECT_URI, "UTF-8"),
                URLEncoder.encode("authorization_code", "UTF-8"));
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(payload);
        out.close();

        String s = new String(IOUtils.toByteArray(conn.getInputStream()));
        try {
            JSONObject jsonObject = new JSONObject(s);
            String accessToken = jsonObject.getString("access_token");
            //String refreshToken= jsonObject.getString("refresh_token");
            return new String[] {accessToken};
        } catch (JSONException e) {
            // Throw out JSON exception. it is unlikely to happen
            throw new RuntimeException(e);
        }
    }

    // Get the fragment that request the Oauth through a web page.
    protected OauthAccessCodeRetrievalFragment getOauthRequestFragment() {
        return new GoogleOAuth2AccessCodeRetrievalFragment();
    }
}
