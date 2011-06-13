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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import android.app.TabActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Display;
import android.widget.TextView;
import android.content.pm.ActivityInfo;
import android.text.method.LinkMovementMethod;
import android.text.Html;
import android.widget.TabHost;
import android.content.res.Configuration;
import android.view.Window;
import android.view.WindowManager;


public class MainTabs extends TabActivity{
    private final String WEBSITE_VERSION="http://anymemo.org/index.php?page=version";
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
        init();
        setContentView(R.layout.main_tabs);

        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;

        intent = new Intent().setClass(this, RecentList.class);
        spec = tabHost.newTabSpec("recent").setIndicator(
                getString(R.string.recent_tab_text),
                res.getDrawable(R.drawable.recent))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, OpenScreenTab.class);
        spec = tabHost.newTabSpec("open").setIndicator(
                getString(R.string.open_tab_text),
                res.getDrawable(R.drawable.open))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, EditScreenTab.class);
        spec = tabHost.newTabSpec("edit").setIndicator(
                getString(R.string.edit_tab_text),
                res.getDrawable(R.drawable.edit))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, DownloaderTab.class);
        spec = tabHost.newTabSpec("download").setIndicator(
                getString(R.string.download_tab_text),
                res.getDrawable(R.drawable.download))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, MiscTab.class);
        spec = tabHost.newTabSpec("misc").setIndicator(
                getString(R.string.misc_tab_text),
                res.getDrawable(R.drawable.misc))
            .setContent(intent);
        tabHost.addTab(spec);

    }

    private void init(){
        String dbName = settings.getString("dbname", null);
        String dbPath = settings.getString("dbpath", null);
        /* Check the version, if it is updated from an older version
         * , it will show a dialog
         */
        String savedVersion = settings.getString("saved_version", "");
        String thisVersion = getResources().getString(R.string.app_version);
        
        
        boolean firstTime = settings.getBoolean("first_time", true);
        File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_dir));
        sdPath.mkdir();
        if(!sdPath.canRead()){
        	DialogInterface.OnClickListener exitButtonListener = new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface arg0, int arg1){
        			finish();
        		}
        	};
        	new AlertDialog.Builder(this)
        	    .setTitle("SD card not available")
        	    .setMessage("Please insert SD card and run again!")
        	    .setNeutralButton("Exit", exitButtonListener)
                .create()
                .show();
        }
        /* First time installation! It will install the sample db
         * to /sdcard/AnyMemo
         */
        if(firstTime == true){
        	SharedPreferences.Editor editor = settings.edit();
        	editor.putBoolean("first_time", false);
    		editor.putString("recentdbname0", getString(R.string.default_db_name));
    		editor.putString("recentdbpath0", Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_dir));
        	editor.commit();
			try{
				copyFile(getString(R.string.default_db_name));
			}
			catch(IOException e){
				Log.e("Copy file error", e.toString());
				
			}
        }
        /* Detect an update */
        if(!savedVersion.equals(thisVersion)){
        	SharedPreferences.Editor editor = settings.edit();
            /* save new version number */
            editor.putString("saved_version", thisVersion);
            /* Save the screen dimension for further use */
            Display display = getWindowManager().getDefaultDisplay(); 
            editor.putInt("screen_width", display.getWidth());
            editor.putInt("screen_height", display.getHeight());
            editor.commit();

            View alertView = View.inflate(this, R.layout.link_alert, null);
            TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
            textView.setText(Html.fromHtml(getString(R.string.what_is_new_message)));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(this)
                .setView(alertView)
                .setTitle(getString(R.string.what_is_new))
                .setPositiveButton(getString(R.string.ok_text), null)
                .setNegativeButton(getString(R.string.about_version), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1){
                        Intent myIntent = new Intent();
                        myIntent.setAction(Intent.ACTION_VIEW);
                        myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                        myIntent.setData(Uri.parse(WEBSITE_VERSION));
                        startActivity(myIntent);
                    }
                })
                .show();
        }

        SetAlarmReceiver.cancelNotificationAlarm(this);
        SetAlarmReceiver.setNotificationAlarm(this);

        /* Added back from AMActivity */
        if(settings.getBoolean("fullscreen_mode", false)){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if(!settings.getBoolean("allow_orientation", true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        String localeSetting = settings.getString("interface_locale", "AUTO");
        Locale locale;
        /* Force to use the a language */
        if(localeSetting.equals("EN")){
            locale = Locale.US;
        }
        else if(localeSetting.equals("SC")){
            locale = Locale.SIMPLIFIED_CHINESE;
        }
        else if(localeSetting.equals("TC")){
            locale = Locale.TRADITIONAL_CHINESE;
        }
        else if(localeSetting.equals("CS")){
            locale = new Locale("CS");
        }
        else if(localeSetting.equals("PL")){
            locale = new Locale("PL");
        }
        else if(localeSetting.equals("RU")){
            locale = new Locale("RU");
        }
        else if(localeSetting.equals("DE")){
            locale = new Locale("DE");
        }
        else if(localeSetting.equals("KO")){
            locale = new Locale("KO");
        }
        else if(localeSetting.equals("FR")){
            locale = new Locale("FR");
        }
        else if(localeSetting.equals("PT")){
            locale = new Locale("PT");
        }
        else if(localeSetting.equals("JA")){
            locale = new Locale("JA");
        }
        else if(localeSetting.equals("ES")){
            locale = new Locale("ES");
        }
        else{
            locale = Locale.getDefault();
        }
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Intent myIntent = new Intent(this, AnyMemoService.class);
        myIntent.putExtra("request_code", AnyMemoService.CANCEL_NOTIFICATION | AnyMemoService.UPDATE_WIDGET);
        startService(myIntent);
        //SetAlarmReceiver.setWidgetUpdateAlarm(this);
    }

	private void copyFile(String source) throws IOException{
		String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_dir);
		File newDir = new File(rootPath);
		boolean result = newDir.mkdir();
		if(result == false){
			Log.e("Error", "result false");
		}
		
		InputStream in = null;
		OutputStream out = null;
		in = getResources().getAssets().open(source);
		
		File outFile = new File(rootPath + source);
		//getFileStreamPath(rootPath).mkdir();
		outFile.createNewFile();
		
		out = new FileOutputStream(outFile);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

    public void restartActivity(){
        Intent myIntent = new Intent(this, MainTabs.class);
        finish();
        startActivity(myIntent);
    }

}
