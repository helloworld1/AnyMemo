package org.liberty.android.fantastischmemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FantastischMemo extends Activity implements OnClickListener{
	private String dbName;
	private String dbPath;
	private int returnValue;
	private Button btnNew;
	private Button btnEdit;
	private Button btnOption;
	private Button btnAbout;
	private Button btnExit;
	private Button btnDownload;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btnNew = (Button)this.findViewById(R.id.main_open_database_button);
        //btnRecent = (Button)this.findViewById(R.id.main_open_recent_button);
        btnEdit= (Button)this.findViewById(R.id.main_edit_button);
        btnOption = (Button)this.findViewById(R.id.main_option_button);
        btnDownload = (Button)this.findViewById(R.id.main_download_button);
        btnAbout = (Button)this.findViewById(R.id.main_about_button);
        btnExit = (Button)this.findViewById(R.id.main_exit_button);
        btnNew.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        btnOption.setOnClickListener(this);
        btnAbout.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        //Intent myIntent = new Intent();
        //myIntent.setClass(this, FileBrowser.class);
        //startActivityForResult(myIntent, 1);
        /*
        myIntent.setClass(this, MemoScreen.class);
        myIntent.putExtra("mode", "acq");
        startActivity(myIntent);
        */
       // SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        this.dbName = settings.getString("dbname", null);
        this.dbPath = settings.getString("dbpath", null);
        
        
        boolean firstTime = settings.getBoolean("first_time", true);
        File sdPath = new File(getString(R.string.default_sd_path));
        sdPath.mkdir();
        if(!sdPath.canRead()){
        	DialogInterface.OnClickListener exitButtonListener = new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface arg0, int arg1){
        			finish();
        		}
        	};
        	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        	alertDialog.setTitle("SD card not available");
        	alertDialog.setMessage("Please insert SD card and run again!");
        	alertDialog.setButton("Exit", exitButtonListener);
        	alertDialog.show();
        }
        if(firstTime == true){
        	SharedPreferences.Editor editor = settings.edit();
        	editor.putBoolean("first_time", false);
    		editor.putString("recentdbname0", getString(R.string.default_db_name));
    		editor.putString("recentdbpath0", getString(R.string.default_sd_path));
        	editor.commit();
			try{
				copyFile(getString(R.string.default_db_name));
			}
			catch(IOException e){
				Log.e("Copy file error", e.toString());
				
			}
			
        }
        //Item resItem;
        //resItem = dbHelper.getItemById(4, 0);
        //mDBView.setText(new StringBuilder().append(resItem.getNote()));
        
    }
    
    public void onClick(View v){
    	if(v == btnNew){
    		/*
           Intent myIntent = new Intent();
           myIntent.setClass(this, FileBrowser.class);
           myIntent.putExtra("default_root", dbPath);
           startActivityForResult(myIntent, 1);
           */
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
    	
    	/*
    	if(v == btnRecent){
    		if(dbName != null && dbPath != null){
    			Intent myIntent = new Intent();
    			myIntent.setClass(this, MemoScreen.class);
    			myIntent.putExtra("dbname", dbName);
    			myIntent.putExtra("dbpath", dbPath);
    			startActivity(myIntent);
    		}
    		else{
    			AlertDialog alertDialog = new AlertDialog.Builder(this)
    			.create();
    			alertDialog.setTitle("No database");
    			alertDialog.setMessage("There is recently opened database");
				alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Back",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
							}
						});
    			alertDialog.show();
    			
    		}
    	}*/
    	if(v == btnOption){
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, OptionScreen.class);
    		startActivity(myIntent);
    	}
    	if(v == btnDownload){
    		Intent myIntent = new Intent(Intent.ACTION_VIEW);
    		Uri u = Uri.parse("market://search?q=pname:org.liberty.android.fminstaller");
    		myIntent.setData(u);
    		startActivity(myIntent);
    	}
    	if(v == btnAbout){
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, AboutScreen.class);
    		startActivity(myIntent);
    		
    	}
    }
    
    public void onResume(){
    	super.onResume();
    	if(returnValue == 1){
    		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    		//SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
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
    	case (1):
    		if(resultCode == Activity.RESULT_OK){
    			dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
    			dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
    			dbPath += "/";
    			returnValue = 1;
    			
    		}
    	case 2:
    		if(resultCode == Activity.RESULT_OK){
    			Intent myIntent = new Intent();
    			myIntent.setClass(this, AddItemScreen.class);
    			dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
    			dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
    			dbPath += "/";
    			myIntent.putExtra("dbname", dbName);
    			myIntent.putExtra("dbpath", dbPath);
    			myIntent.putExtra("openid", 1);
    			startActivity(myIntent);
    			
    		}
    		
    		
    	}
    }
    
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_screen_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.mainmenu_clear:
	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
	    	SharedPreferences.Editor editor = settings.edit();
	    	editor.clear();
			editor.commit();
			Intent refresh = new Intent(this, FantastischMemo.class);
			startActivity(refresh);
			this.finish();
			return true;
	    }
	    return false;
	}
	
	private void copyFile(String source) throws IOException{
		String rootPath = getResources().getString(R.string.default_sd_path);
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
}