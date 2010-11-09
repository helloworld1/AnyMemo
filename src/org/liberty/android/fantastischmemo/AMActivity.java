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
import android.view.WindowManager;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.os.Bundle;
import android.content.res.Configuration;
import android.view.Window;
import android.view.WindowManager;
import java.util.Locale;


/*
 * This class is the base class for all screen type
 * class in AnyMemo
 * It contains basic configuration of the Activity
 */
public abstract class AMActivity extends Activity{
    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if(settings.getBoolean("fullscreen_mode", false)){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if(!settings.getBoolean("allow_orientation", true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        String localeSetting = settings.getString("interface_locale", "Auto Detect");
        Locale locale;
        /* Force to use the a language */
        if(localeSetting.equals("English")){
            locale = Locale.US;
        }
        else if(localeSetting.equals("Simplified Chinese")){
            locale = Locale.SIMPLIFIED_CHINESE;
        }
        else if(localeSetting.equals("Traditional Chinese")){
            locale = Locale.TRADITIONAL_CHINESE;
        }
        else if(localeSetting.equals("Czech")){
            locale = new Locale("CS");
        }
        else if(localeSetting.equals("Polish")){
            locale = new Locale("PL");
        }
        else if(localeSetting.equals("Russian")){
            locale = new Locale("RU");
        }
        else{
            locale = Locale.getDefault();
        }
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

