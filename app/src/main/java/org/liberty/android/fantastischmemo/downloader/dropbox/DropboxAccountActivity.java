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
        // TODO: Stub
        return false;
    }

    @Override
    protected String[] getAccessTokens(final String[] requests) throws IOException {
        // todo
        return null;
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
}
