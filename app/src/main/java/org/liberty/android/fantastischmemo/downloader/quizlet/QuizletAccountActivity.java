package org.liberty.android.fantastischmemo.downloader.quizlet;

import android.util.Base64;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccessCodeRetrievalFragment;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccountActivity;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public abstract class QuizletAccountActivity extends OauthAccountActivity {
    private String[] authToken;

    @Override
    protected boolean verifyAccessToken(final String[] accessTokens)
            throws IOException {
        String token = accessTokens[0];
        String userId = accessTokens[1];
        try {
            URL url1 = new URL(AMEnv.QUIZLET_API_ENDPOINT + "/users/" + userId);
            HttpsURLConnection conn = (HttpsURLConnection) url1
                    .openConnection();
            conn.addRequestProperty("Authorization",
                    "Bearer " + String.format(token));

            String s = new String(IOUtils.toByteArray(conn.getInputStream()));
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.has("error")) {
                String error = jsonObject.getString("error");
                Log.e(TAG, "Token validation error: " + error);
                return false;
            }

        } catch (Exception e) {
            Log.i(TAG, "The saved access token is invalid", e);
        }
        return true;
    }

    // The string array returns access token and user_id
    @Override
    protected String[] getAccessTokens(final String[] requests)
            throws IOException {
        String code = requests[0];
        String clientIdAndSecret = AMEnv.QUIZLET_CLIENT_ID + ":"
                + AMEnv.QUIZLET_CLIENT_SECRET;
        String encodedClientIdAndSecret = Base64.encodeToString(
                clientIdAndSecret.getBytes(), 0);
        URL url1 = new URL("https://api.quizlet.com/oauth/token");
        HttpsURLConnection conn = (HttpsURLConnection) url1.openConnection();
        conn.addRequestProperty("Content-Type",
                "application/x-www-form-urlencoded; charset=UTF-8");

        // Add the Basic Authorization item
        conn.addRequestProperty("Authorization", "Basic " + encodedClientIdAndSecret);

        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        String payload = String.format("grant_type=%s&code=%s&redirect_uri=%s",
                URLEncoder.encode("authorization_code", "UTF-8"),
                URLEncoder.encode(code, "UTF-8"),
                URLEncoder.encode(AMEnv.QUIZLET_REDIRECT_URI, "UTF-8"));
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(payload);
        out.close();

        if (conn.getResponseCode() / 100 >= 3) {
            Log.e(TAG, "Http response code: " + conn.getResponseCode() + " response message: " + conn.getResponseMessage());
            Log.e(TAG, "Error response for: " + url1 + " is "
                    + new String(IOUtils.toByteArray(conn.getErrorStream())));
            throw new IOException("Response code: " + conn.getResponseCode());
        }

        String s = new String(IOUtils.toByteArray(conn.getInputStream()));
        try {
            JSONObject jsonObject = new JSONObject(s);
            String accessToken = jsonObject.getString("access_token");
            String userId = jsonObject.getString("user_id");
            return new String[] { accessToken, userId };
        } catch (JSONException e) {
            // Throw out JSON exception. it is unlikely to happen
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onAuthenticated(final String[] authTokens) {
        authToken = authTokens;
    }

    // Get the fragment that request the Oauth through a web page.
    protected OauthAccessCodeRetrievalFragment getOauthRequestFragment() {
        return new QuizletOAuth2AccessCodeRetrievalFragment();
    }
}
