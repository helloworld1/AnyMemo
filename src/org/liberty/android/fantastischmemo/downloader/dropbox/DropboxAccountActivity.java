package org.liberty.android.fantastischmemo.downloader.dropbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccessCodeRetrievalFragment;
import org.liberty.android.fantastischmemo.downloader.oauth.OauthAccountActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

public abstract class DropboxAccountActivity extends OauthAccountActivity {
    private String oauthAccessToken;

    private String oauthAccessTokenSecret;

    private static final String ACCESS_TOKEN_URL = "https://api.dropbox.com/1/oauth/access_token";

    // Use the Account info API to verify the token is valid.
    private static final String ACCOUNT_INFO_URL = "https://api.dropbox.com/1/account/info";

    @Override
    protected boolean verifyAccessToken(final String[] accessTokens) throws IOException {
        return verifyToken(accessTokens[0], accessTokens[1]);
    }

    @Override
    protected String[] getAccessTokens(final String[] requests) throws IOException {
        // requestTokenSecret should be a two elements array containing request token and secret
        if (requests.length != 2){
            throw new AssertionError("Error fetching request token and secret");
        }

        BufferedReader reader = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(ACCESS_TOKEN_URL);
        httpPost.setHeader("Authorization", DropboxUtils.buildOAuthAccessHeader(requests[0], requests[1]));

        try {
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200){
                HttpEntity entity = response.getEntity();
                reader = new BufferedReader(new InputStreamReader(entity.getContent()));
                String[] parsedResult = reader.readLine().split("&");
                oauthAccessTokenSecret = parsedResult[0].split("=")[1];
                oauthAccessToken = parsedResult[1].split("=")[1];
                return new String[] {oauthAccessToken, oauthAccessTokenSecret};
            } else {
                throw new IOException("Fetching access token request returns error code: " + statusCode);
            }

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    protected OauthAccessCodeRetrievalFragment getOauthRequestFragment() {
        return new DropboxOAuthTokenRetrievalDialogFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dropbox_list_menu, menu);
        return true;
    }


    // Return true if the token is valid
    // false if the token is not
    private boolean verifyToken(String oauthToken, String oauthTokenSecret) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(ACCOUNT_INFO_URL);
        httpGet.setHeader("Authorization", DropboxUtils.getFileExchangeAuthHeader(oauthToken, oauthTokenSecret));
        HttpResponse response = httpClient.execute(httpGet);

        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200){
            Log.i(TAG, "Token verified");
            return true;
        } else {
            Log.w(TAG, "Call " + ACCOUNT_INFO_URL + " Status code: " + statusCode);
            return false;
        }
    }

}
