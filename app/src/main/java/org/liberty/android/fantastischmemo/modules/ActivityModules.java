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

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module(subcomponents = FragmentComponents.class)
public abstract class ActivityModules {
    @Binds
    @PerActivity
    abstract Activity providesActivity(BaseActivity activity);

    @Provides
    @PerActivity
    static MultipleLoaderManager providesMultipleLoaderManager(BaseActivity activity) {
        return new MultipleLoaderManager(activity);
    }

    @Provides
    @PerActivity
    static ShareUtil providesShareUtil(Activity activity) {
        return new ShareUtil(activity);
    }

    @Provides
    @PerActivity
    static DictionaryUtil providesDictionaryUtil(Activity activity, Option option) {
        return new DictionaryUtil(activity, option);
    }


    @Provides
    @PerActivity
    static GoogleSignInOptions provideGoogleSignInOptions() {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
    }

    @Provides
    @PerActivity
    static GoogleApiClient provideGoogleApiClient(@NonNull final BaseActivity activity,
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
