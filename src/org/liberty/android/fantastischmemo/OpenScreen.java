package org.liberty.android.fantastischmemo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class OpenScreen extends Activity implements OnItemClickListener {

	public final static String ITEM_TITLE = "title";
	public final static String ITEM_CAPTION = "caption";
	private final int ACTIVITY_DB = 1;
	private final int ACTIVITY_XML = 2;
	
	private String dbName;
	private String dbPath;
	private int returnValue;
	private RecentOpenList mRecentOpenList;
	private List<HashMap<String, String>> recentList;

	public Map<String,?> createItem(String title, String caption) {
		Map<String,String> item = new HashMap<String,String>();
		item.put(ITEM_TITLE, title);
		item.put(ITEM_CAPTION, caption);
		return item;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		

	}

	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
		if(position == 1){
            Intent myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".db");
            startActivityForResult(myIntent, ACTIVITY_DB);
		}
		
		if(position == 2){
			
            Intent myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".xml");
            startActivityForResult(myIntent, ACTIVITY_XML);
            
			
		}
		
		if(position >= 4){
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, MemoScreen.class);
    		this.dbPath = recentList.get(position - 4).get("recentdbpath");
    		this.dbName = recentList.get(position - 4).get("recentdbname");
    		myIntent.putExtra("dbname", dbName);
    		myIntent.putExtra("dbpath", dbPath);
    		mRecentOpenList.writeNewList(dbPath, dbName);
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
    		
    		mRecentOpenList.writeNewList(this.dbPath, this.dbName);
    		
    		
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, MemoScreen.class);
    		myIntent.putExtra("dbname", dbName);
    		myIntent.putExtra("dbpath", dbPath);
    		startActivity(myIntent);
    		returnValue = 0;
    	}
    	
		List<Map<String,?>> recentItemList = new LinkedList<Map<String,?>>();
    	
		// Fill the recent open list from the pref
		mRecentOpenList = new RecentOpenList(this);
		recentList = mRecentOpenList.getList();
		
		for(HashMap<String, String> hm : recentList){
			recentItemList.add(createItem(hm.get("recentdbname"), this.getString(R.string.stat_total) + hm.get("recentdbtotal") + " " + this.getString(R.string.stat_new) + hm.get("recentnew") + " " + this.getString(R.string.stat_scheduled) + hm.get("recentscheduled")));
			
		}


		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		adapter.addSection("Open New", new ArrayAdapter<String>(this,
			R.layout.open_screen_item, new String[] { this.getString(R.string.open_open_new_db), this.getString(R.string.open_import_xml) }));
		adapter.addSection("Open Recently", new SimpleAdapter(this, recentItemList, R.layout.open_screen_complex,
			new String[] { ITEM_TITLE, ITEM_CAPTION }, new int[]{R.id.list_complex_title, R.id.list_complex_caption} ));

		ListView list = new ListView(this);
		list.setOnItemClickListener(this);
		
		list.setAdapter(adapter);
		this.setContentView(list);
    	
    	
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	switch(requestCode){
    	case ACTIVITY_DB:
    		if(resultCode == Activity.RESULT_OK){
    			dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
    			dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
    			dbPath += "/";
    			returnValue = 1;
    			
    		}
    		break;
    	
    	case ACTIVITY_XML:
    		if(resultCode == Activity.RESULT_OK){
    			XMLConverter conv = null;
    			AlertDialog.Builder ad = new AlertDialog.Builder( this );
    			ad.setPositiveButton( "OK", null );
    			String xmlPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
    			String xmlName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
    			try{
    				conv = new XMLConverter(this, xmlPath, xmlName);
    				conv.outputDB();
    				ad.setTitle( "Success" );
    				ad.setMessage( "The XML is successfully converted and stored as" + xmlPath + "/" + xmlName.replaceAll(".xml", ".db"));
    				
    				//conv.outputTabFile();
    			}
    			catch(Exception e){
    				Log.e("XMLError",e.toString());
    				ad.setTitle("Failed");
    				ad.setMessage("Fail to convert " + xmlPath + "/" + xmlName + " Exception: " + e.toString());
    			}
    			ad.show();
    		}
    		break;
    		
    		
    	}
    }
    
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.open_screen_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.openmenu_clear:
	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
	    	SharedPreferences.Editor editor = settings.edit();
			for(int i = 0; i < RecentOpenList.MAX_LIST_NUMBER; i++){
	    		//SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    		editor.putString("recentdbname" + i, null);
	    		editor.putString("recentdbpath" + i, null);
			}
			editor.commit();
			Intent refresh = new Intent(this, OpenScreen.class);
			startActivity(refresh);
			this.finish();
			return true;
	    }
	    return false;
	}
	

}
