package org.liberty.android.fantastischmemo.downloader.oauth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.util.Log;

import com.google.common.base.Strings;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.BaseActivity;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public abstract class Oauth2AccountActivity extends BaseActivity {

    private CompositeDisposable disposables = new CompositeDisposable();

    protected abstract void onAuthenticated(final String authTokens);

    protected abstract Completable verifyAccessToken(@NonNull final String accessToken);

    protected abstract String getLoginUrl();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.oauth2_account_activity);

        String savedToken = activityComponents().oauth2TokenUtil().getSavedToken();
        if (savedToken != null) {
            verifyTokenAndOnAuthenticate(savedToken);
        } else {
            requestAuth();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri callbackUri = intent.getData();
        fetchTokenAndOnAuthenticate(callbackUri);
    }

    protected Single<String> fetchAuthTokenFromCallback(Uri callbackUri) {
        String tokenPart = callbackUri.getFragment();
        String[] parts = tokenPart.split("&");
        String token = null;
        final String tokenPartPrefix = "access_token=";
        for (String part : parts) {
            if (part.startsWith(tokenPartPrefix)) {
                token = part.substring(tokenPartPrefix.length());
                break;
            }
        }

        if (!Strings.isNullOrEmpty(token)) {
            Log.i(TAG, "Auth token: " + token);
            return Single.just(token);
        }

        return Single.error(new RuntimeException("Unable to find auth token from callback: " + callbackUri));
    }

    private void fetchTokenAndOnAuthenticate(@NonNull final Uri callbackUri) {
        disposables.add(fetchAuthTokenFromCallback(callbackUri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<String>() {
                    @Override
                    public void onSuccess(String token) {
                        activityComponents().oauth2TokenUtil().saveToken(token);
                        onAuthenticated(token);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error verifying token", e);
                        activityComponents().oauth2TokenUtil().invalidateSavedToken();
                        requestAuth();
                    }
                }));
    }

    private void verifyTokenAndOnAuthenticate(@NonNull final String token) {
        disposables.add(
                verifyAccessToken(token)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        activityComponents().oauth2TokenUtil().saveToken(token);
                        onAuthenticated(token);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error verifying token", e);
                        activityComponents().oauth2TokenUtil().invalidateSavedToken();
                        requestAuth();
                    }
                }));

    }

    private void requestAuth() {
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .build();
        customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        customTabsIntent.launchUrl(this, Uri.parse(getLoginUrl()));
    }
}
