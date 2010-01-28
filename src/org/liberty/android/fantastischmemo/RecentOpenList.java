package org.liberty.android.fantastischmemo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class RecentOpenList {
	final int MAX_LIST_NUMBER = 5;
	final Context mContext;
	List<HashMap<String, String>> recentList;
	Integer i = Integer.valueOf(1);;
	
	
	
	
	
	
	public RecentOpenList(Context context){
		recentList = new LinkedList<HashMap<String, String>>();
		mContext = context; 
		fetchListFromPref();
	}
	
	public List<HashMap<String, String>> getList(){
		ListIterator<HashMap<String, String>> it = recentList.listIterator();
		while(it.hasNext()){
			HashMap<String,  String> hm = (HashMap<String, String>)it.next();
			String dbname = hm.get("recentdbname");
			String dbpath = hm.get("recentdbpath");
			
			DatabaseHelper dbHelper = new DatabaseHelper(mContext, dbpath, dbname, 1);
			if(dbHelper.checkDatabase() == false){
				it.remove();
			}
			else{
				dbHelper.openDatabase();
				hm.put("recentdbtotal", "" + dbHelper.getTotalCount());
				hm.put("recentscheduled", "" + dbHelper.getScheduledCount());
				hm.put("recentnew", "" + dbHelper.getNewCount());
				dbHelper.close();
				it.set(hm);
			}
			
		}
		return recentList;
	}
	
	private void fetchListFromPref(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		for(int i = 0; i < MAX_LIST_NUMBER; i++){
			String dbName = settings.getString("recentdbname" + i, null);
			String dbPath = settings.getString("recentdbpath" + i, null);
			if(dbName == null || dbPath == null){ 
				break;
			}
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put("recentdbname", dbName);
			hm.put("recentdbpath", dbPath);
			recentList.add(hm);
		}
	}
	
	public void writeNewList(String dbpath, String dbname){
		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("recentdbname", dbname);
		hm.put("recentdbpath", dbpath);
		ListIterator<HashMap<String, String>> it = recentList.listIterator();
		while(it.hasNext()){
			HashMap<String, String> h = it.next();
			
			if(h.get("recentdbname").equals(dbname)){
				it.remove();
			}
		}
		if(recentList.size() < MAX_LIST_NUMBER){
			recentList.add(0, hm);
		}
		else{
			recentList.remove(recentList.size() - 1);
			recentList.add(0, hm);
		}
		int i = 0;
		for(HashMap<String, String> h : recentList){
    		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
    		//SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putString("recentdbname" + i, h.get("recentdbname"));
    		editor.putString("recentdbpath" + i, h.get("recentdbpath"));
    		editor.commit();
    		i += 1;
			
		}
		
	}
	


}
