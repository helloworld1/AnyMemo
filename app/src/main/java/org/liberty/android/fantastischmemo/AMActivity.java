/*
Copyright (C) 2010 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

package org.liberty.android.fantastischmemo;

import java.util.Locale;

import android.os.Handler;
import android.view.View;
import roboguice.activity.RoboActionBarActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.WindowManager;

/*
 * This class is the base class for all screen type
 * class in AnyMemo
 * It contains basic configuration of the Activity
 */
public abstract class AMActivity extends RoboActionBarActivity {
    protected String TAG = getClass().getSimpleName();

    boolean activityForeground = false;

    boolean activityCreated = false;

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


    public void restartActivity(){
        startActivity(new Intent(this, this.getClass()));
        finish();
    }

    public boolean isActivityForeground() {
        return activityForeground;
    }

    public boolean isActivityCreated() {
        return activityCreated;
    }

    /**
     * Resolve the attribute of the current theme.
     *
     * @param attr the attribute id
     * @return  the resource id
     */
    public int resolveThemeResource(int attr) {
        TypedValue typedvalueattr = new TypedValue();
        getTheme().resolveAttribute(attr, typedvalueattr, true);
        return typedvalueattr.resourceId;
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
