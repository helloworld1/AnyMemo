package org.liberty.android.fantastischmemo.utils;

import android.app.Activity;
import android.content.DialogInterface;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.liberty.android.fantastischmemo.common.BaseActivity;
import org.liberty.android.fantastischmemo.modules.PerActivity;

import javax.inject.Inject;

@PerActivity
public class GooglePlayUtil {

    private final Activity activity;

    @Inject
    public GooglePlayUtil(BaseActivity activity) {
        this.activity = activity;
    }

    public boolean checkPlayServices(int requestCode) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (result != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(result)) {
                googleApiAvailability.getErrorDialog(activity, result,
                        requestCode, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                activity.finish();
                            }
                        }).show();
            }

            return false;
        }

        return true;
    }
}
