/*
Copyright (C) 2012 Haowen Ning

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
package org.liberty.android.fantastischmemo.ui;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.utils.DatabaseUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import roboguice.fragment.RoboFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RecentListFragment extends RoboFragment {

    private ListView recentListView;
    private RecentListAdapter recentListAdapter;

    private Handler mHandler;
    private Thread updateRecentListThread;
    private RecentListUtil recentListUtil;

    private final static String TAG = "org.liberty.android.fantastischmemo.OpenScreen";

    private Activity mActivity;

    private DatabaseUtil databaseUtil;

    @Inject
    public void setRecentListUtil(RecentListUtil recentListUtil) {
        this.recentListUtil = recentListUtil;
    }

    @Inject
    public void setDatabaseUtil(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recent_list, container, false);
        mHandler = new Handler();
        recentListView = (ListView)v.findViewById(R.id.recent_open_list);
        recentListView.setOnItemClickListener(listItemClickListener);
        recentListView.setOnItemLongClickListener(listItemLongClickListener);
        /* pre loading stat */
        recentListAdapter = new RecentListAdapter(mActivity, R.layout.open_screen_recent_item);
        recentListView.setAdapter(recentListAdapter);
        return v;
    }



    @Override
    public void onResume(){
        super.onResume();
        updateRecentListThread = new Thread(){
            public void run(){
                String[] allPath = recentListUtil.getAllRecentDBPath();
                final List<RecentItem> ril = new ArrayList<RecentItem>();
                /* Quick list */
                int index = 0;
                try {
                    for(int i = 0; i < allPath.length; i++){
                        if(allPath[i] == null){
                            continue;
                        }
                        final RecentItem ri = new RecentItem();
                        if (!databaseUtil.checkDatabase(allPath[i])) {
                            recentListUtil.deleteFromRecentList(allPath[i]);
                            continue;
                        }
                        ri.dbInfo = getString(R.string.loading_database);
                        ri.index = index++;
                        ril.add(ri);
                        ri.dbPath = allPath[i];
                        ri.dbName = FilenameUtils.getName(allPath[i]);
                        /* In order to add interrupted exception */
                        Thread.sleep(5);
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
                        try {
                            AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, ri.dbPath);
                            CardDao dao = helper.getCardDao();
                            ri.dbInfo = getString(R.string.stat_total) + dao.getTotalCount(null) + " " + getString(R.string.stat_new) + dao.getNewCardCount(null) + " " + getString(R.string.stat_scheduled)+ dao.getScheduledCardCount(null);
                            ril.set(ri.index, ri);
                            AnyMemoDBOpenHelperManager.releaseHelper(helper);
                        } catch (Exception e) {
                            Log.e(TAG, "Recent list throws exception (Usually can be safely ignored)", e);
                        }
                        Thread.sleep(5);
                    }
                    mHandler.post(new Runnable(){
                        public void run(){
                            recentListAdapter.clear();
                            for(RecentItem ri : ril)
                                recentListAdapter.insert(ri, ri.index);
                        }
                    });
                } catch(InterruptedException e){
                    Log.e(TAG, "Interrupted", e);
                } catch(Exception e) {
                    Log.e(TAG, "Exception Maybe caused by race condition. Ignored.", e);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.open_screen_menu, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.openmenu_clear:
            recentListUtil.clearRecentList();
            onResume();
            return true;

        }

        return false;
    }

    private AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
            Intent myIntent = new Intent();
            myIntent.setClass(mActivity, StudyActivity.class);
            String dbPath = recentListAdapter.getItem(position).dbPath;
            myIntent.putExtra(StudyActivity.EXTRA_DBPATH, dbPath);
            recentListUtil.addToRecentList(dbPath);
            startActivity(myIntent);
        }
    };

    private AdapterView.OnItemLongClickListener listItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parentView, View childView, int position, long id) {
            String dbPath = recentListAdapter.getItem(position).dbPath;
            DialogFragment df = new OpenActionsFragment();
            Bundle b = new Bundle();
            b.putString(OpenActionsFragment.EXTRA_DBPATH, dbPath);
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "OpenActions");
            return true;
        }
    };

    /* Aux class to store data */
    private class RecentItem {
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
                LayoutInflater li = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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


}
