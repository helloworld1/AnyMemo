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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.res.Configuration;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.text.method.LinkMovementMethod;
import android.text.Html;
import android.content.Context;

public class AnyMemo extends Activity implements OnClickListener{
	private String dbName;
	private String dbPath;
	private int returnValue;
	private Button btnNew;
	private Button btnEdit;
	private Button btnExit;
	private Button btnDownload;
    private final static String TAG = "org.liberty.android.fantastischmemo.AnyMemo";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        /* set if the orientation change is allowed */
        if(!settings.getBoolean("allow_orientation", true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        Locale locale;
        /* Handle Locale */
        String localeSetting = settings.getString("interface_locale", "Auto Detect");
        Log.v(TAG, "Locale " + localeSetting);

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
        else{
            locale = Locale.getDefault();
        }
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.main);

        btnNew = (Button)this.findViewById(R.id.main_open_database_button);
        btnEdit= (Button)this.findViewById(R.id.main_edit_button);
        btnDownload = (Button)this.findViewById(R.id.main_download_button);
        btnExit = (Button)this.findViewById(R.id.main_exit_button);
        btnNew.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
        btnExit.setOnClickListener(this);

        
        this.dbName = settings.getString("dbname", null);
        this.dbPath = settings.getString("dbpath", null);
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
        if(!savedVersion.equals(thisVersion)){
        	SharedPreferences.Editor editor = settings.edit();
            editor.putString("saved_version", thisVersion);
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
                        myIntent.setData(Uri.parse(getString(R.string.website_version)));
                        startActivity(myIntent);
                    }
                })
                .show();
        }

        SetAlarmReceiver.setNotificationAlarm(this);
        //SetAlarmReceiver.setWidgetUpdateAlarm(this);

        /* Go directly to other screen based on user settings */
        String startScreen = "";
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
            startScreen = extras.getString("startup_screen");
        }
        if(startScreen == null || startScreen.equals("")){
            startScreen = settings.getString("startup_screen", "");
        }
        gotoScreen(startScreen);
    }
    
    public void onClick(View v){
    	if(v == btnNew){
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, OpenScreen.class);
    		startActivity(myIntent);
            
    	}
    	if(v == btnExit){
    		finish();
    	}

    	if(v == btnEdit){
            Intent myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".db");
            startActivityForResult(myIntent, 2);
    		
    	}
    	
    	if(v == btnDownload){
    		//Intent myIntent = new Intent(Intent.ACTION_VIEW);
    		//Uri u = Uri.parse("market://search?q=pname:org.liberty.android.fminstaller");
    		//myIntent.setData(u);
            /* Now we have our downloader */
            Intent myIntent = new Intent();
            myIntent.setClass(this, Downloader.class);
    		startActivity(myIntent);
    	}
    }
    
    public void onResume(){
    	super.onResume();
    	if(returnValue == 1){
    		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putString("dbname", this.dbName);
    		editor.putString("dbpath", this.dbPath);
    		editor.commit();
    		
    		
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, MemoScreen.class);
    		myIntent.putExtra("dbname", dbName);
    		myIntent.putExtra("dbpath", dbPath);
    		startActivity(myIntent);
    		returnValue = 0;
    	}
    	
    	
    	
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	switch(requestCode){
    	case 1:
    		if(resultCode == Activity.RESULT_OK){
    			dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
    			dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
    			dbPath += "/";
    			returnValue = 1;
                break;
    			
    		}
    	case 2:
    		if(resultCode == Activity.RESULT_OK){
    			Intent myIntent = new Intent();
    			myIntent.setClass(this, EditScreen.class);
    			//myIntent.setClass(this, ListEditScreen.class);
    			dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
    			dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
    			dbPath += "/";
    			myIntent.putExtra("dbname", dbName);
    			myIntent.putExtra("dbpath", dbPath);
    			myIntent.putExtra("openid", 1);
    			startActivity(myIntent);
                break;
    			
    		}
        case 3:
            /* Going back from the option screen */
            restartActivity();
            break;
    		
    	}
    }

    public void restartActivity(){
        Intent myIntent = new Intent(this, AnyMemo.class);
        finish();
        startActivity(myIntent);
    }
    
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_screen_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
    	Intent myIntent = new Intent();
	    switch (item.getItemId()) {
	    case R.id.mainmenu_clear:
	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
	    	SharedPreferences.Editor editor = settings.edit();
	    	editor.clear();
			editor.commit();
			Intent refresh = new Intent(this, AnyMemo.class);
			startActivity(refresh);
			this.finish();
			return true;
        case R.id.mainmenu_option:
    		myIntent.setClass(this, OptionScreen.class);
    		startActivityForResult(myIntent, 3);
            return true;

        case R.id.mainmenu_help:
            myIntent.setAction(Intent.ACTION_VIEW);
            myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            myIntent.setData(Uri.parse(getString(R.string.website_help_main)));
            startActivity(myIntent);
            return true;

    	case R.id.mainmenu_about: 
    		//myIntent.setClass(this, AboutScreen.class);
    		//startActivity(myIntent);
            /* About screen is now obsolete */
            View alertView = View.inflate(this, R.layout.link_alert, null);
            TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
            textView.setText(Html.fromHtml(getString(R.string.about_text)));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(this)
                .setView(alertView)
                .setTitle(getString(R.string.about_title) + " " + getString(R.string.app_full_name) + " " + getString(R.string.app_version))
                .setPositiveButton(getString(R.string.ok_text), null)
                .setNegativeButton(getString(R.string.about_version), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1){
                        Intent myIntent = new Intent();
                        myIntent.setAction(Intent.ACTION_VIEW);
                        myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                        myIntent.setData(Uri.parse(getString(R.string.website_version)));
                        startActivity(myIntent);
                    }
                })
                .show();
            return true;
	    }
	    return false;
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void gotoScreen(String screenName){
        if(screenName == null){
            return;
        }
        else if(screenName.equals("Open Screen")){
            Intent myIntent = new Intent();
    		myIntent.setClass(this, OpenScreen.class);
    		startActivity(myIntent);
        }
        else if(screenName.equals("Memo Screen")){
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, MemoScreen.class);
    		myIntent.putExtra("dbname", settings.getString("recentdbname0", ""));
    		myIntent.putExtra("dbpath", settings.getString("recentdbpath0", ""));
    		startActivity(myIntent);
    		returnValue = 0;
        }
        else if(screenName.equals("Edit Screen")){
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, EditScreen.class);
    		myIntent.putExtra("dbname", settings.getString("recentdbname0", ""));
    		myIntent.putExtra("dbpath", settings.getString("recentdbpath0", ""));
            myIntent.putExtra("openid", 1);
    		startActivity(myIntent);
    		returnValue = 0;
        }


    }


}
