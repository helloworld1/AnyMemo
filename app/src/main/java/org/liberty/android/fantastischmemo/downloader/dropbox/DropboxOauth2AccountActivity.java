package org.liberty.android.fantastischmemo.downloader.dropbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.downloader.oauth.Oauth2AccountActivity;

import javax.inject.Inject;

import io.reactivex.Completable;

public class DropboxOauth2AccountActivity extends Oauth2AccountActivity {

    @Inject DropboxApiHelper dropboxApiHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        activityComponents().inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onAuthenticated(String authTokens) {
        Intent intent = new Intent(this, DropboxListActivity.class);
        intent.putExtra(DropboxListActivity.EXTRA_AUTH_TOKEN, authTokens);
        startActivity(intent);
        finish();
    }

    @Override
    protected Completable verifyAccessToken(@NonNull String accessToken) {
        return Completable.fromSingle(dropboxApiHelper.getUserInfo(accessToken));
    }

    @Override
    protected String getLoginUrl() {
        return String.format("https://www.dropbox.com/oauth2/authorize?client_id=%s&response_type=token&redirect_uri=%s",
                AMEnv.DROPBOX_CONSUMER_KEY,
                AMEnv.DROPBOX_REDIRECT_URI);
    }
}
