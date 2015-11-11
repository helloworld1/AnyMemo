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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.utils.DatabaseUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;
import roboguice.fragment.RoboFragment;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class RecentListFragment extends RoboFragment {

    private RecyclerView recentListRecyclerView;
    private RecentListAdapter recentListAdapter;

    private Handler mHandler;
    private Thread updateRecentListThread;
    private RecentListUtil recentListUtil;

    private final static String TAG = RecentListFragment.class.getSimpleName();

    private Activity mActivity;

    private DatabaseUtil databaseUtil;

    public RecentListFragment() { }

    @Inject
    public void setRecentListUtil(RecentListUtil recentListUtil) {
        this.recentListUtil = recentListUtil;
    }

    @Inject
    public void setDatabaseUtil(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mActivity = (Activity) activity;
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recent_list, container, false);
        mHandler = new Handler();
        recentListRecyclerView = (RecyclerView) v.findViewById(R.id.recent_open_list);

        recentListRecyclerView.setLayoutManager(new LinearLayoutManager(recentListRecyclerView.getContext()));

        /* pre loading stat */
        recentListAdapter = new RecentListAdapter(mActivity, recentListUtil);

        recentListRecyclerView.setAdapter(recentListAdapter);

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
                    }
                    mHandler.post(new Runnable(){
                        public void run(){
                            recentListAdapter.clear();
                            for(RecentItem ri : ril) {
                                recentListAdapter.insert(ri.index, ri);
                            }
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
                    }
                    mHandler.post(new Runnable(){
                        public void run(){
                            recentListAdapter.clear();
                            for(RecentItem ri : ril)
                                recentListAdapter.insert(ri.index, ri);
                        }
                    });
                } catch(Exception e) {
                    Log.e(TAG, "Exception Maybe caused by race condition. Ignored.", e);
                }
            }
        };
        updateRecentListThread.start();
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

    /** Aux class to store data */
    private static class RecentItem {
        public String dbName;
        public String dbPath;
        public String dbInfo;
        public int index;
    }

    /**
     *  The recent list adapter to handle the actual recycler view logic
     */
    private static class RecentListAdapter extends RecyclerView.Adapter<RecentListAdapter.ViewHolder> {

        private final Context context;

        private final List<RecentItem> items = new ArrayList<>();

        private final RecentListUtil recentListUtil;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private TextView filenameView;
            private TextView infoView;

            public ViewHolder(View view) {
                super(view);
                filenameView = (TextView)view.findViewById(R.id.recent_item_filename);
                infoView = (TextView)view.findViewById(R.id.recent_item_info);
            }

            public void setItem(RecentItem item) {
                filenameView.setText(item.dbName);
                infoView.setText(item.dbInfo);
            }
        }

        public RecentListAdapter(Context context, RecentListUtil recentListUtil) {
            this.context = context;
            this.recentListUtil = recentListUtil;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater li = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = li.inflate(R.layout.open_screen_recent_item, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final RecentItem currentItem = items.get(position);
            holder.setItem(currentItem);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent();
                    myIntent.setClass(context, StudyActivity.class);
                    String dbPath = currentItem.dbPath;
                    myIntent.putExtra(StudyActivity.EXTRA_DBPATH, dbPath);
                    recentListUtil.addToRecentList(dbPath);
                    context.startActivity(myIntent);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String dbPath = currentItem.dbPath;
                    DialogFragment df = new OpenActionsFragment();
                    Bundle b = new Bundle();
                    b.putString(OpenActionsFragment.EXTRA_DBPATH, dbPath);
                    df.setArguments(b);
                    df.show(((FragmentActivity)context).getSupportFragmentManager(), "OpenActions");
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void insert(int index, RecentItem item) {
            items.add(index, item);
            this.notifyDataSetChanged();
        }

        public void clear() {
            items.clear();
            this.notifyDataSetChanged();
        }
    }
}
