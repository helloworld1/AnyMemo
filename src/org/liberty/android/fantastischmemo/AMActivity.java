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

import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.content.Intent;


import android.support.v4.app.FragmentActivity;

import android.view.WindowManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.content.res.Configuration;
import android.view.Window;
import java.util.Locale;

/*
 * This class is the base class for all screen type
 * class in AnyMemo
 * It contains basic configuration of the Activity
 */
public abstract class AMActivity extends FragmentActivity{
    protected String TAG = getClass().getSimpleName();

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if(settings.getBoolean(AMPrefKeys.FULLSCREEN_MODE_KEY, false)) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if(!settings.getBoolean(AMPrefKeys.ALLOW_ORIENTATION_KEY, true)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        updateInterfaceLanguage();
    }


    @Override
    public void onResume() {
        super.onResume();
        updateInterfaceLanguage();
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
        } else {
            locale = Locale.getDefault();
        }
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}

