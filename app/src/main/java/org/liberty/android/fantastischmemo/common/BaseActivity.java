package org.liberty.android.fantastischmemo.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.liberty.android.fantastischmemo.modules.ActivityComponents;
import org.liberty.android.fantastischmemo.modules.ActivityModules;
import org.liberty.android.fantastischmemo.modules.AppComponents;
import org.liberty.android.fantastischmemo.modules.DaggerActivityComponents;

import java.util.Locale;

/**
 * Created by liberty on 8/28/16.
 */
public class BaseActivity extends AppCompatActivity {
    protected String TAG = getClass().getSimpleName();

    private boolean activityForeground = false;

    private boolean activityCreated = false;

    private ActivityComponents activityComponents;

    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if(settings.getBoolean(AMPrefKeys.FULLSCREEN_MODE_KEY, false)) {
            enableImmersiveMode();
        }
        if(!settings.getBoolean(AMPrefKeys.ALLOW_ORIENTATION_KEY, true)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        updateInterfaceLanguage();
        activityCreated = true;
    }


    @Override
    public void onResume() {
        super.onResume();
        updateInterfaceLanguage();
        activityForeground = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        activityForeground = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        activityCreated = false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateInterfaceLanguage();
    }

    public AppComponents appComponents() {
        return ((AMApplication) getApplication()).appComponents();
    }

    public ActivityComponents activityComponents() {
        if (activityComponents == null) {
            activityComponents = DaggerActivityComponents.builder()
                    .appComponents(appComponents())
                    .activityModules(new ActivityModules(this))
                    .build();
        }
        return activityComponents;
    }

    public void restartActivity(){
        Intent intent = new Intent(this, this.getClass());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }
        startActivity(intent);
        finish();
    }

    public boolean isActivityForeground() {
        return activityForeground;
    }

    public boolean isActivityCreated() {
        return activityCreated;
    }

    private void updateInterfaceLanguage() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String localeSetting = settings.getString(AMPrefKeys.INTERFACE_LOCALE_KEY, "AUTO");
        Locale locale;
        /* Force to use the a language */
        if(localeSetting.equals("EN")){
            locale = Locale.US;
        } else if (localeSetting.equals("SC")){
            locale = Locale.SIMPLIFIED_CHINESE;
        } else if (localeSetting.equals("TC")){
            locale = Locale.TRADITIONAL_CHINESE;
        } else if (localeSetting.equals("CS")){
            locale = new Locale("CS");
        } else if (localeSetting.equals("PL")){
            locale = new Locale("PL");
        } else if (localeSetting.equals("RU")){
            locale = new Locale("RU");
        } else if (localeSetting.equals("DE")){
            locale = new Locale("DE");
        } else if (localeSetting.equals("KO")){
            locale = new Locale("KO");
        } else if (localeSetting.equals("FR")){
            locale = new Locale("FR");
        } else if (localeSetting.equals("PT")){
            locale = new Locale("PT");
        } else if (localeSetting.equals("JA")){
            locale = new Locale("JA");
        } else if (localeSetting.equals("ES")){
            locale = new Locale("ES");
        } else if (localeSetting.equals("IT")) {
            locale = Locale.ITALIAN;
        } else if (localeSetting.equals("FI")) {
            locale = new Locale("FI");
        } else if (localeSetting.equals("EO")) {
            locale = new Locale("EO");
        } else {
            locale = Locale.getDefault();
        }
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    private void enableImmersiveMode() {
        // First enter immersive mode
        // Do not use SYSTEM_UI_FLAG_IMMERSIVE_STICKY since the action bar will not be shown for QACardActivity
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);


        // When the user swipe the bottom of the screen to exit immerisve view, restore the immersive mode after
        // a small delay
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(final int visibility) {
                // Note that system bars will only be "visible" if none of the
                // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getWindow().getDecorView().setSystemUiVisibility(
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
                        }
                    }, 2000);
                }
            }
        });

    }
}
