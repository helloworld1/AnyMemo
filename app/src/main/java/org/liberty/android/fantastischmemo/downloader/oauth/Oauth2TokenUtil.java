package org.liberty.android.fantastischmemo.downloader.oauth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.liberty.android.fantastischmemo.common.AMPrefKeys;
import org.liberty.android.fantastischmemo.modules.PerActivity;

import javax.inject.Inject;

@PerActivity
public class Oauth2TokenUtil {

    private final SharedPreferences sharedPreferences;

    private final Activity activity;

    private final String oauthAccessTokenPrefKey ;

    @Inject
    public Oauth2TokenUtil(@NonNull final Activity activity, @NonNull final SharedPreferences sharedPreferences) {
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;

        // The preference key to save / retrieve the access token. The preference name is based
        // on the prefix and the package of the class. So the same package use the same keys.
        this.oauthAccessTokenPrefKey = AMPrefKeys.OAUTH_ACCESS_TOKEN_KEY_PREFIX + activity.getClass().getPackage().getName();
    }

    @Nullable
    public String getSavedToken() {
        return sharedPreferences.getString(oauthAccessTokenPrefKey, null);
    }

    public void invalidateSavedToken() {
        saveToken(null);
    }

    public void saveToken(@Nullable final String token) {
        sharedPreferences.edit()
                .putString(oauthAccessTokenPrefKey, token)
                .apply();
    }
}
