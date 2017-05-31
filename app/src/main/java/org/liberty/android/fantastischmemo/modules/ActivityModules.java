package org.liberty.android.fantastischmemo.modules;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.entity.Option;
import org.liberty.android.fantastischmemo.ui.loader.MultipleLoaderManager;
import org.liberty.android.fantastischmemo.utils.DictionaryUtil;
import org.liberty.android.fantastischmemo.utils.ErrorUtil;
import org.liberty.android.fantastischmemo.utils.ShareUtil;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModules {
    private final BaseActivity activity;

    public ActivityModules(BaseActivity activity) {
        this.activity = activity;
    }

    @Provides
    @PerActivity
    BaseActivity providesBaseActivity() {
        return activity;
    }

    @Provides
    @PerActivity
    Activity providesActivity() {
        return activity;
    }

    @Provides
    @PerActivity
    MultipleLoaderManager providesMultipleLoaderManager(BaseActivity activity) {
        return new MultipleLoaderManager(activity);
    }

    @Provides
    @PerActivity
    ShareUtil providesShareUtil(Activity activity) {
        return new ShareUtil(activity);
    }

    @Provides
    @PerActivity
    DictionaryUtil providesDictionaryUtil(Activity activity, Option option) {
        return new DictionaryUtil(activity, option);
    }


    @Provides
    @PerActivity
    GoogleSignInOptions provideGoogleSignInOptions() {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
    }

    @Provides
    @PerActivity
    GoogleApiClient provideGoogleApiClient(@NonNull final BaseActivity activity,
                                           @NonNull final ErrorUtil errorUtil,
                                           @NonNull final GoogleSignInOptions gso) {
        return new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        errorUtil.showFatalError("Connection failure: " + connectionResult.getErrorMessage(), null);
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

}
