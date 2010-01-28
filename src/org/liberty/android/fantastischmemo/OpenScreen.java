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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class OpenScreen extends Activity implements OnItemClickListener {

	public final static String ITEM_TITLE = "title";
	public final static String ITEM_CAPTION = "caption";
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
            startActivityForResult(myIntent, 1);
		}
		
		if(position == 2){
			AlertDialog.Builder ad = new AlertDialog.Builder( this );
			ad.setTitle( "Not implemented" );
			ad.setMessage( "Will be available in next version");
			ad.setPositiveButton( "OK", null );
			ad.show();
			
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
			recentItemList.add(createItem(hm.get("recentdbname"), "Total: " + hm.get("recentdbtotal") + " New: " + hm.get("recentnew") + " Scheduled: " + hm.get("recentscheduled")));
			
		}


		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		adapter.addSection("Open New", new ArrayAdapter<String>(this,
			R.layout.open_screen_item, new String[] { "Open New DB", "Import XML" }));
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
    	case (1):
    		if(resultCode == Activity.RESULT_OK){
    			dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
    			dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
    			dbPath += "/";
    			returnValue = 1;
    			
    		}
    		
    		
    	}
    }
	


}
