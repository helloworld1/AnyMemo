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

import org.liberty.android.fantastischmemo.cardscreen.*;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

import android.app.AlertDialog;
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
import android.view.ContextMenu;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;

public class RecentList extends AMActivity implements OnItemClickListener{

    private ListView recentListView;
    private RecentListAdapter recentListAdapter;

    private Handler mHandler;
    private Thread updateRecentListThread;

    private final static String TAG = "org.liberty.android.fantastischmemo.OpenScreen";
    /* The selected item when opening context menu */
    private int contextMenuSelectedId = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.recent_list);
        mHandler = new Handler();
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        recentListView = (ListView)findViewById(R.id.recent_open_list);
        recentListView.setOnItemClickListener(this);
        registerForContextMenu(recentListView);
        /* pre loading stat */
        recentListAdapter = new RecentListAdapter(this, R.layout.open_screen_recent_item);
        recentListView.setAdapter(recentListAdapter);
	}

    @Override
	public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
		
        Intent myIntent = new Intent();
        myIntent.setClass(this, MemoScreen.class);
        String dbPath = recentListAdapter.getItem(position).dbPath;
        String dbName = recentListAdapter.getItem(position).dbName;
        myIntent.putExtra("dbname", dbName);
        myIntent.putExtra("dbpath", dbPath);
        RecentListUtil.addToRecentList(this, dbPath, dbName);
        startActivity(myIntent);
		
	}
	

	
    @Override
    public void onResume(){
    	super.onResume();
        updateRecentListThread = new Thread(){
            public void run(){
                String[] allPath = RecentListUtil.getAllRecentDBPath(RecentList.this);
                String[] allName = RecentListUtil.getAllRecentDBName(RecentList.this);
                final List<RecentItem> ril = new ArrayList<RecentItem>();
                /* Quick list */
                int index = 0;
                try{
                    for(int i = 0; i < allPath.length; i++){
                        if(allPath[i] == null || allName == null){
                            continue;
                        }
                        try{
                            final RecentItem ri = new RecentItem();
                            DatabaseHelper dbHelper = new DatabaseHelper(RecentList.this, allPath[i], allName[i]);
                            ri.dbInfo = getString(R.string.loading_database);
                            dbHelper.close();
                            ri.index = index++;
                            ril.add(ri);
                            ri.dbPath = allPath[i];
                            ri.dbName = allName[i];
                            /* In order to add interrupted exception */
                            Thread.sleep(5);
                        }
                        catch(SQLException e){
                            RecentListUtil.deleteFromRecentList(RecentList.this, allPath[i], allName[i]);
                            Log.e(TAG, "Error db: " + allPath[i] + " " + allName[i], e);
                        }
                    }
                    mHandler.post(new Runnable(){
                        public void run(){
                            recentListAdapter.clear();
                            for(RecentItem ri : ril)
                        recentListAdapter.insert(ri, ri.index);
                        }
                    });
                    /* This will update the detailed statistic info */
                    for(final RecentItem ri : ril){
                        DatabaseHelper dbHelper = new DatabaseHelper(RecentList.this, ri.dbPath, ri.dbName);
                        ri.dbInfo = getString(R.string.stat_total) + dbHelper.getTotalCount() + " " + getString(R.string.stat_new) + dbHelper.getNewCount() + " " + getString(R.string.stat_scheduled)+ dbHelper.getScheduledCount();
                        ril.set(ri.index, ri);
                        dbHelper.close();
                        Thread.sleep(5);
                    }
                    mHandler.post(new Runnable(){
                        public void run(){
                            recentListAdapter.clear();
                            for(RecentItem ri : ril)
                                recentListAdapter.insert(ri, ri.index);
                        }
                    });
                }
                catch(InterruptedException e){
                    Log.e(TAG, "Interrupted", e);
                }
            }
        };
        updateRecentListThread.start();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(updateRecentListThread != null && updateRecentListThread.isAlive()){
            updateRecentListThread.interrupt();
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
            RecentListUtil.clearRecentList(this);
            onResume();
			return true;

	    }

	    return false;
	}

    
    /* Aux class to store data */
    private class RecentItem{
        public String dbName;
        public String dbPath;
        public String dbInfo;
        public int index;
    }

    private class RecentListAdapter extends ArrayAdapter<RecentItem>{

        public RecentListAdapter(Context context, int textViewResourceId){
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View v = convertView;
            if(v == null){
                LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.open_screen_recent_item, null);
            }
            RecentItem recentItem = getItem(position);
            if(recentItem != null){
                TextView filenameView = (TextView)v.findViewById(R.id.recent_item_filename);
                TextView infoView = (TextView)v.findViewById(R.id.recent_item_info);
                filenameView.setText(recentItem.dbName);
                infoView.setText(recentItem.dbInfo);
            }
            return v;
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.open_screen_context_menu, menu);
        menu.setHeaderTitle(R.string.menu_text);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Log.v(TAG, "Menu ID: " + info.id);
        contextMenuSelectedId = (int)info.id;
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuitem) {
        if(contextMenuSelectedId == -1 || contextMenuSelectedId >= recentListAdapter.getCount()){
            return false;
        }
        final String selectedPath = recentListAdapter.getItem(contextMenuSelectedId).dbPath;
        final String selectedName = recentListAdapter.getItem(contextMenuSelectedId).dbName;
        switch(menuitem.getItemId()) {
            case R.id.context_study:
            {
                /* Open database normally*/
                Intent myIntent = new Intent();
                myIntent.setClass(this, MemoScreen.class);
                myIntent.putExtra("dbname", selectedName);
                myIntent.putExtra("dbpath", selectedPath);
                startActivity(myIntent);
                RecentListUtil.addToRecentList(this, selectedPath, selectedName);
                return true;
            }

            case R.id.context_cram:
            {
                /* Cram Review */
                Intent myIntent = new Intent();
                myIntent.setClass(this, CramMemoScreen.class);
                myIntent.putExtra("dbname", selectedName);
                myIntent.putExtra("dbpath", selectedPath);
                startActivity(myIntent);
                RecentListUtil.addToRecentList(this, selectedPath, selectedName);
                return true;
            }

            case R.id.context_edit:
            {
                /* Preview card */
                Intent myIntent = new Intent();
                myIntent.setClass(this, EditScreen.class);
                myIntent.putExtra("dbname", selectedName);
                myIntent.putExtra("dbpath", selectedPath);
                myIntent.putExtra("openid", 1);
                startActivity(myIntent);
                RecentListUtil.addToRecentList(this, selectedPath, selectedName);
                return true;
            }

            case R.id.context_settings:
            {
                /* Edit database settings*/
                Intent myIntent = new Intent();
                myIntent.setClass(this, SettingsScreen.class);
                myIntent.putExtra("dbname", selectedName);
                myIntent.putExtra("dbpath", selectedPath);
                startActivity(myIntent);
                RecentListUtil.addToRecentList(this, selectedPath, selectedName);
                return true;
            }

            case R.id.context_delete:
            {
                /* Delete this database */
                new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.detail_delete))
                    .setMessage(getString(R.string.fb_delete_message))
                    .setPositiveButton(getString(R.string.detail_delete), new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which ){
                            File fileToDelete = new File(selectedPath+ "/" + selectedName);
                            fileToDelete.delete();
                            RecentListUtil.deleteFromRecentList(RecentList.this, selectedPath, selectedName);
                            /* Refresh the list */
                            onResume();
                            
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel_text), null)
                    .create()
                    .show();
                return true;
            }
        }
        return false;
    }
}
