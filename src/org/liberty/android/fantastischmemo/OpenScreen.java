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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.database.SQLException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.DialogInterface;

public class OpenScreen extends Activity implements OnItemClickListener, OnClickListener, OnItemLongClickListener {

	private String dbName;
	private String dbPath;
	private int returnValue;
    private Context mContext;
    private RecentOpenList mRecentOpenList;
    private ListView recentListView;
    private ArrayList<RecentItem> recentItemList;
    private Button openButton;
    private ProgressDialog mProgressDialog;
    private Handler mHandler;

    /* This part is added for threading data sharing */
    private Intent tmpIntent = null;
    private AlertDialog.Builder mAlert;


    private final int ACTIVITY_DB = 1;
    private final int ACTIVITY_IMPORT_MNEMOSYNE_XML = 2;
    private final int ACTIVITY_EXPORT_MNEMOSYNE_XML = 3;
    private final int ACTIVITY_EXPORT_TABTXT = 4;
    private final int ACTIVITY_EXPORT_QATXT = 5;
    private final int ACTIVITY_EXPORT_CSV= 6;
    private final int ACTIVITY_IMPORT_TABTXT = 7;
    private final int ACTIVITY_IMPORT_QATXT = 8;
    private final int ACTIVITY_IMPORT_CSV= 9;
    private final int ACTIVITY_IMPORT_SUPERMEMO_XML = 10;


	private List<HashMap<String, String>> recentList;
    public final static String TAG = "org.liberty.android.fantastischmemo.OpenScreen";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.open_screen);
        mContext = this;
        mRecentOpenList = new RecentOpenList(this);
        openButton = (Button)findViewById(R.id.open_screen_open_exist);
        openButton.setOnClickListener(this);
        mHandler = new Handler();

	}

    @Override
    public void onClick(View v){
        Intent myIntent = new Intent();

        if(v == openButton){
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".db");
            startActivityForResult(myIntent, ACTIVITY_DB);
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
        this.dbPath = recentList.get(position).get("recentdbpath");
        this.dbName = recentList.get(position).get("recentdbname");
        showMenuDialog(dbPath, dbName);
        return true;
		
	}
    @Override
	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
		
        Intent myIntent = new Intent();
        myIntent.setClass(this, MemoScreen.class);
        this.dbPath = recentList.get(position).get("recentdbpath");
        this.dbName = recentList.get(position).get("recentdbname");
        myIntent.putExtra("dbname", dbName);
        myIntent.putExtra("dbpath", dbPath);
        try{
            mRecentOpenList.writeNewList(dbPath, dbName);
        }
        catch(InterruptedException e){
        }
        startActivity(myIntent);
		
	}
	
	
    public void onResume(){
    	super.onResume();
        if(returnValue == ACTIVITY_IMPORT_MNEMOSYNE_XML){
            return;
        }
    	if(returnValue == ACTIVITY_DB){
    		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    		//SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putString("dbname", this.dbName);
    		editor.putString("dbpath", this.dbPath);
    		editor.commit();
    		
            try{
                mRecentOpenList.writeNewList(this.dbPath, this.dbName);
            }
            catch(InterruptedException e){
            }
    		
    		
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, MemoScreen.class);
    		myIntent.putExtra("dbname", dbName);
    		myIntent.putExtra("dbpath", dbPath);
    		startActivity(myIntent);
    		returnValue = 0;
            return;
    	}

            recentListView = (ListView)findViewById(R.id.recent_open_list);
            recentListView.setOnItemClickListener(this);
            recentListView.setOnItemLongClickListener(this);
            /* pre loading stat */
            recentItemList = new ArrayList<RecentItem>();
            
            // Fill the recent open list from the pref
            mRecentOpenList = new RecentOpenList(mContext);
            ArrayList<RecentItem> preRecentItemList = new ArrayList<RecentItem>();
            recentList = mRecentOpenList.getPreList();
            for(HashMap<String, String> hm : recentList){
                preRecentItemList.add(new RecentItem(hm.get("recentdbname"), getString(R.string.loading_recent_list))); 
            }
            recentListView.setAdapter(new RecentListAdapter(mContext, R.layout.open_screen_recent_item, preRecentItemList));

            Thread loadingThread = new Thread(){
                public void run(){
                    /* Get list with stat info */
                    try{
                        recentList = mRecentOpenList.getList();
                        
                        for(HashMap<String, String> hm : recentList){
                            recentItemList.add(new RecentItem(hm.get("recentdbname"), mContext.getString(R.string.stat_total) + hm.get("recentdbtotal") + " " + mContext.getString(R.string.stat_new) + hm.get("recentnew") + " " + mContext.getString(R.string.stat_scheduled) + hm.get("recentscheduled")));
                        }

                        if(mHandler != null){

                            mHandler.post(new Runnable(){
                                public void run(){
                            
                                    recentListView.setAdapter(new RecentListAdapter(mContext, R.layout.open_screen_recent_item, recentItemList));
                            //mProgressDialog.dismiss();

                                }
                            });
                        }
                    }
                    catch(Exception e){
                        Log.e(TAG, "Error refreshing", e);
                    }
                }
            };
            loadingThread.start();
		
    	
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
        final int request = requestCode;
        if(resultCode == Activity.RESULT_OK){
    		if(requestCode == ACTIVITY_DB){
    			dbName = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
    			dbPath = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
    			dbPath += "/";
    			returnValue = ACTIVITY_DB;
    			
    		}
    		else if(requestCode == ACTIVITY_IMPORT_MNEMOSYNE_XML || requestCode == ACTIVITY_IMPORT_TABTXT || requestCode == ACTIVITY_IMPORT_QATXT || requestCode == ACTIVITY_IMPORT_QATXT || requestCode ==ACTIVITY_IMPORT_SUPERMEMO_XML){
                returnValue = ACTIVITY_IMPORT_SUPERMEMO_XML;

                mProgressDialog = ProgressDialog.show(this, getString(R.string.loading_please_wait), getString(R.string.loading_import), true);
                tmpIntent = data;
                Thread convertThread = new Thread(){
                    @Override
                    public void run(){
                        DBImporter importer = null;
                        mAlert = new AlertDialog.Builder(mContext);
                        mAlert.setPositiveButton( "OK", null );
                        String filePath = tmpIntent.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                        String fileName = tmpIntent.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                        try{
                            importer = new DBImporter(mContext, filePath, fileName);
                            if(request == ACTIVITY_IMPORT_MNEMOSYNE_XML){
                                importer.ImportMnemosyneXML();
                                mAlert.setMessage(getString(R.string.success_import)+ " " + filePath + "/" + fileName.replaceAll(".xml", ".db"));
                            }
                            else if(request == ACTIVITY_IMPORT_TABTXT){
                                mAlert.setMessage(getString(R.string.success_import)+ " " + filePath + "/" + fileName.replaceAll(".txt", ".db"));
                                importer.ImportTabTXT();
                            }
                            else if(request == ACTIVITY_IMPORT_QATXT){
                                mAlert.setMessage(getString(R.string.success_import)+ " " + filePath + "/" + fileName.replaceAll(".txt", ".db"));
                                importer.ImportQATXT();
                            }
                            else if(request == ACTIVITY_IMPORT_SUPERMEMO_XML){
                                importer.ImportSupermemoXML();
                                mAlert.setMessage(getString(R.string.success_import)+ " " + filePath + "/" + fileName.replaceAll(".xml", ".db"));
                            }

                            mAlert.setTitle(getString(R.string.success));
                            
                        }
                        catch(Exception e){
                            Log.e(TAG, "Import error", e);
                            mAlert.setTitle(getString(R.string.fail));
                            mAlert.setMessage(getString(R.string.fail_import) + " " + filePath + "/" + fileName + " Exception: " + e.toString());
                        }
                        mHandler.post(new Runnable(){
                            @Override
                            public void run(){
                                mProgressDialog.dismiss();
                                mAlert.show();
                            }
                        });
                    }
                };
                convertThread.start();

    		}
            else if (requestCode == ACTIVITY_EXPORT_MNEMOSYNE_XML || requestCode == ACTIVITY_EXPORT_QATXT || requestCode == ACTIVITY_EXPORT_TABTXT || requestCode == ACTIVITY_EXPORT_CSV){
                returnValue = ACTIVITY_EXPORT_MNEMOSYNE_XML;

                mProgressDialog = ProgressDialog.show(this, getString(R.string.loading_please_wait), getString(R.string.loading_export), true);
                tmpIntent = data;
                Thread convertThread = new Thread(){
                    @Override
                    public void run(){
                        String dbPath = tmpIntent.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                        String dbName = tmpIntent.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                        mAlert = new AlertDialog.Builder(mContext);
                        mAlert.setPositiveButton(getString(R.string.ok_text), null );
                        try{
                            DBExporter exporter = new DBExporter(mContext, dbPath, dbName);
                            if(request == ACTIVITY_EXPORT_MNEMOSYNE_XML){
                                exporter.writeXML();
                                mAlert.setMessage(getString(R.string.success_export)+ " " + dbPath + "/" + dbName.replaceAll(".db", ".xml"));
                            }
                            else if(request == ACTIVITY_EXPORT_QATXT){
                                exporter.writeQATXT();
                                mAlert.setMessage(getString(R.string.success_export)+ " " + dbPath + "/" + dbName.replaceAll(".db", ".txt"));
                            }
                            else if(request == ACTIVITY_EXPORT_TABTXT){
                                exporter.writeTabTXT();
                                mAlert.setMessage(getString(R.string.success_export)+ " " + dbPath + "/" + dbName.replaceAll(".db", ".txt"));
                            }
                            else if(request == ACTIVITY_EXPORT_CSV){
                                exporter.writeCSV();
                                mAlert.setMessage(getString(R.string.success_export)+ " " + dbPath + "/" + dbName.replaceAll(".db", ".csv"));
                            }
                            mAlert.setTitle(getString(R.string.success));
                        }
                        catch(Exception e){
                            Log.e(TAG, "XML export error", e);
                            mAlert.setTitle(getString(R.string.fail));
                            mAlert.setMessage(getString(R.string.fail_export) + " " + dbPath + "/" + dbName + " Exception: " + e.toString());
                        }

                        mHandler.post(new Runnable(){
                            @Override
                            public void run(){
                                mProgressDialog.dismiss();
                                mAlert.show();
                            }
                        });
                    }
                };
                convertThread.start();

            }

    	}
    }
    
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.open_screen_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent;
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
			finish();
			return true;

        case R.id.openmenu_import_mnemosyne_xml:
            myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".xml");
            startActivityForResult(myIntent, ACTIVITY_IMPORT_MNEMOSYNE_XML);
            return true;

        case R.id.openmenu_import_tab_txt:
            myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".txt");
            startActivityForResult(myIntent, ACTIVITY_IMPORT_TABTXT);
            return true;

            
        case R.id.openmenu_export_xml:
            myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".db");
            startActivityForResult(myIntent, ACTIVITY_EXPORT_MNEMOSYNE_XML);
            return true;

        case R.id.openmenu_export_tab_txt:
            myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".db");
            startActivityForResult(myIntent, ACTIVITY_EXPORT_TABTXT);
            return true;

        case R.id.openmenu_export_qa_txt:
            myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".db");
            startActivityForResult(myIntent, ACTIVITY_EXPORT_QATXT);
            return true;

        case R.id.openmenu_export_csv:
            myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".db");
            startActivityForResult(myIntent, ACTIVITY_EXPORT_CSV);
            return true;

        case R.id.openmenu_import_qa_txt:
            myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".txt");
            startActivityForResult(myIntent, ACTIVITY_IMPORT_QATXT);
            return true;

        case R.id.openmenu_import_supermemo_xml:
            myIntent = new Intent();
            myIntent.setClass(this, FileBrowser.class);
            myIntent.putExtra("default_root", dbPath);
            myIntent.putExtra("file_extension", ".xml");
            startActivityForResult(myIntent, ACTIVITY_IMPORT_SUPERMEMO_XML);
            return true;
	    }
	    return false;
	}

    
    private class RecentItem{
        String mFileName;
        String mInfo;

        public RecentItem(){
        }

        public RecentItem(String name, String info){
            mFileName = name;
            mInfo = info;
        }

        public String getFileName(){
            return mFileName;
        }

        public String getInfo(){
            return mInfo;
        }

        public void setFilename(String name){
            mFileName = name;
        }

        public void setInfo(String info){
            mInfo = info;
        }

    }

    private class RecentListAdapter extends ArrayAdapter<RecentItem>{
        private ArrayList<RecentItem> mItems;

        public RecentListAdapter(Context context, int textViewResourceId, ArrayList<RecentItem> items){
            super(context, textViewResourceId, items);
            mItems = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            if(v == null){
                LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.open_screen_recent_item, null);
            }
            RecentItem recentItem = mItems.get(position);
            if(recentItem != null){
                TextView filenameView = (TextView)v.findViewById(R.id.recent_item_filename);
                TextView infoView = (TextView)v.findViewById(R.id.recent_item_info);
                filenameView.setText(recentItem.getFileName());
                infoView.setText(recentItem.getInfo());
            }
            return v;
        }

    }

    private class RecentOpenList {
        public static final int MAX_LIST_NUMBER = 7;
        final Context mContext;
        List<HashMap<String, String>> recentList;
        Integer i = Integer.valueOf(1);;
        /* signal = 0, no lock
         * signal = 1, locked
         * signal = 2, require to unlock
         */
        public AtomicInteger signal;
        
        public RecentOpenList(Context context){
            recentList = new LinkedList<HashMap<String, String>>();
            mContext = context; 
            fetchListFromPref();
            signal = new AtomicInteger(0);
        }

        public List<HashMap<String, String>> getPreList(){
            /* without statistics info */
            return recentList;
        }
        
        public List<HashMap<String, String>> getList() throws SQLException, InterruptedException{
            ListIterator<HashMap<String, String>> it = recentList.listIterator();
            int counter = 0;
            signal.set(1);
            while(it.hasNext() && signal.get() == 1){
                counter += 1;
                HashMap<String,  String> hm = (HashMap<String, String>)it.next();
                DatabaseHelper dbHelper = null;

                String dbname = hm.get("recentdbname");
                String dbpath = hm.get("recentdbpath");

                try{
                
                    dbHelper = new DatabaseHelper(mContext, dbpath, dbname);
                    hm.put("recentdbtotal", "" + dbHelper.getTotalCount());
                    hm.put("recentscheduled", "" + dbHelper.getScheduledCount());
                    hm.put("recentnew", "" + dbHelper.getNewCount());
                    dbHelper.close();
                }
                catch(SQLException e){
                    signal.set(0);
                    throw new SQLException(e.toString());
                }
                it.set(hm);
                
            }

            if(signal.getAndSet(0) == 2){
                throw new InterruptedException("Interrupt updating list");
            }
            else{
                signal.set(0);
            }
            return recentList;
        }
        
        private void fetchListFromPref(){
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean checkResult = false;
            for(int i = 0; i < MAX_LIST_NUMBER; i++){
                String dbName = settings.getString("recentdbname" + i, null);
                String dbPath = settings.getString("recentdbpath" + i, null);
                if(dbName == null || dbPath == null){
                    continue;
                }

                try{
                    DatabaseHelper dbHelper = new DatabaseHelper(mContext, dbPath, dbName);
                    checkResult = true;
                    dbHelper.close();
                }
                catch(Exception e){
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("recentdbname" + i, null);
                    editor.putString("recentdbpath" + i, null);
                    editor.commit();
                    continue;
                }
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("recentdbname", dbName);
                hm.put("recentdbpath", dbPath);
                recentList.add(hm);
            }
        }
        
        public void writeNewList(String dbpath, String dbname) throws InterruptedException{
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("recentdbname", dbname);
            hm.put("recentdbpath", dbpath);
            signal.compareAndSet(1, 2);
            while(signal.get() != 0){
                Thread.sleep(10);
            }
            signal.set(1);
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
            signal.compareAndSet(1, 0);

            int i = 0;
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = settings.edit();
            for(HashMap<String, String> h : recentList){
                //SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                editor.putString("recentdbname" + i, h.get("recentdbname"));
                editor.putString("recentdbpath" + i, h.get("recentdbpath"));
                i += 1;
            }
            /* Fix the duplication issue in some rare cases */
            for(;i < MAX_LIST_NUMBER; i++){
                editor.putString("recentdbname" + i, null);
            }
            editor.commit();

            
        }
        


    }

    protected void showMenuDialog(String dbPath, String dbName){
        /* Display the menu when long clicking the item */
        final String path = dbPath;
        final String name = dbName;
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_text))
            .setItems(R.array.open_screen_menu_list, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    if(which == 0){
                        /* Open database normally*/
                        Intent myIntent = new Intent();
                        myIntent.setClass(OpenScreen.this, MemoScreen.class);
                        myIntent.putExtra("dbname", name);
                        myIntent.putExtra("dbpath", path);
                        try{
                            mRecentOpenList.writeNewList(path, name);
                        }
                        catch(InterruptedException e){
                        }
                        startActivity(myIntent);
                    }
                    if(which == 1){
                        /* Cram Review */
                        Intent myIntent = new Intent();
                        myIntent.setClass(OpenScreen.this, MemoScreen.class);
                        myIntent.putExtra("dbname", name);
                        myIntent.putExtra("dbpath", path);
                        myIntent.putExtra("learn_ahead", true);
                        try{
                            mRecentOpenList.writeNewList(path, name);
                        }
                        catch(InterruptedException e){
                        }
                        startActivity(myIntent);
                    }
                    if(which == 2){
                        /* Preview card */
                        Intent myIntent = new Intent();
                        myIntent.setClass(OpenScreen.this, EditScreen.class);
                        myIntent.putExtra("dbname", name);
                        myIntent.putExtra("dbpath", path);
                        try{
                            mRecentOpenList.writeNewList(path, name);
                        }
                        catch(InterruptedException e){
                        }


                        startActivity(myIntent);
                    }
                    if(which == 3){
                        /* Edit database settings*/
                        Intent myIntent = new Intent();
                        myIntent.setClass(OpenScreen.this, SettingsScreen.class);
                        myIntent.putExtra("dbname", name);
                        myIntent.putExtra("dbpath", path);
                        try{
                            mRecentOpenList.writeNewList(path, name);
                        }
                        catch(InterruptedException e){
                        }
                        startActivity(myIntent);
                    }
                    if(which == 4){
                        /* Delete this database */
                        new AlertDialog.Builder(OpenScreen.this)
                            .setTitle(getString(R.string.detail_delete))
                            .setMessage(getString(R.string.fb_delete_message))
                            .setPositiveButton(getString(R.string.detail_delete), new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which ){
                                    File fileToDelete = new File(path + "/" + name);
                                    fileToDelete.delete();
                                    finish();
                                    Intent myIntent = new Intent();
                                    myIntent.setClass(OpenScreen.this, OpenScreen.class);
                                    startActivity(myIntent);
                                    
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel_text), null)
                            .create()
                            .show();
                    }
                }
            })
            .create()
            .show();
    }

            
}
