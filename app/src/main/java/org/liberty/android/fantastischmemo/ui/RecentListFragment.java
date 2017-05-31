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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.common.BaseFragment;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.ui.helper.SelectableAdapter;
import org.liberty.android.fantastischmemo.utils.DatabaseUtil;
import org.liberty.android.fantastischmemo.utils.RecentListActionModeUtil;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

public class RecentListFragment extends BaseFragment {

    private RecyclerView recentListRecyclerView;
    private RecentListAdapter recentListAdapter;


    // The version of recent list to synchronize multiple loader who writes the adapter
    private final AtomicInteger recentListVersion = new AtomicInteger(0);

    private final static String TAG = RecentListFragment.class.getSimpleName();

    @Inject RecentListUtil recentListUtil;

    @Inject DatabaseUtil databaseUtil;

    @Inject RecentListActionModeUtil recentListActionModeUtil;

    private BroadcastReceiver mRemoveSelectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            recentListAdapter.removeSelected();
        }
    };

    private BroadcastReceiver mClearSelectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            recentListAdapter.deselectAll();
        }
    };

    public RecentListFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentComponents().inject(this);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        setHasOptionsMenu(true);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mClearSelectionReceiver,
                new IntentFilter(RecentListActionModeUtil.ACTION_DESELECT_ALL));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRemoveSelectedReceiver,
                new IntentFilter(RecentListActionModeUtil.ACTION_REMOVE_SELECTED));
    }

    @Override
    public void onDetach() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mClearSelectionReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRemoveSelectedReceiver);
        super.onDetach();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (recentListAdapter != null && !isVisibleToUser) {
            recentListAdapter.stopActionMode();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recent_list, container, false);
        recentListRecyclerView = (RecyclerView) v.findViewById(R.id.recent_open_list);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(v.getContext());
        recentListRecyclerView.addItemDecoration(dividerItemDecoration);

        recentListRecyclerView.setLayoutManager(new LinearLayoutManager(recentListRecyclerView.getContext()));

        /* pre loading stat */
        recentListAdapter = new RecentListAdapter(getContext(), recentListUtil,
                                                  recentListActionModeUtil);

        recentListRecyclerView.setAdapter(recentListAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(1, null, new RecentListLoaderCallbacks());
        getLoaderManager().restartLoader(2, null, new RecentListDetailLoaderCallbacks());
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
     * The loader to load recent list
     */
    private class RecentListLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<RecentItem>> {

        private int loadedVersion;

        @Override
        public Loader<List<RecentItem>> onCreateLoader(int id, Bundle args) {
            Loader<List<RecentItem>> loader = new AsyncTaskLoader<List<RecentItem>>(getContext()) {
                @Override
                public List<RecentItem> loadInBackground() {
                    loadedVersion = recentListVersion.get();
                    return loadRecentItemsWithName();
                }
            };
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<RecentItem>> loader, List<RecentItem> ril) {
            if (recentListVersion.get() == loadedVersion) {
                recentListAdapter.setItems(ril);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<RecentItem>> loader) {
            // Nothing
        }
    }

    private class RecentListDetailLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<RecentItem>> {

        private int loadedVersion;

        @Override
        public Loader<List<RecentItem>> onCreateLoader(int id, Bundle args) {
            Loader<List<RecentItem>> loader = new AsyncTaskLoader<List<RecentItem>>(getContext()) {
                @Override
                public List<RecentItem> loadInBackground() {
                    loadedVersion = recentListVersion.get();
                    return loadRecentItemsWithDetails();
                }
            };
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<RecentItem>> loader, List<RecentItem> ril) {
            // Make sure the recentListVersion is updated so the previous running loadRecentItemsWithNames will fail.
            // Also if multiple loadRecentItemsWithDetails, only one succeeded
            if (recentListVersion.compareAndSet(loadedVersion, loadedVersion + 1)) {
                recentListAdapter.setItems(ril);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<RecentItem>> loader) {
            // Nothing
        }
    }

    private List<RecentItem> loadRecentItemsWithName() {
        String[] allPath = recentListUtil.getAllRecentDBPath();
        final List<RecentItem> ril = new ArrayList<RecentItem>();
        int index = 0;
        for(int i = 0; i < allPath.length; i++){
            if(allPath[i] == null){
                continue;
            }
            final RecentItem ri = new RecentItem();
            if (!databaseUtil.checkDatabase(allPath[i])) {
                recentListUtil.deleteFromRecentList(allPath[i]);
                continue;
            }

            ri.dbInfo = getContext().getString(R.string.loading_database);
            ri.index = index++;
            ril.add(ri);
            ri.dbPath = allPath[i];
            ri.dbName = FilenameUtils.getName(allPath[i]);
        }
        return ril;
    }

    private List<RecentItem> loadRecentItemsWithDetails() {
        final List<RecentItem> ril = loadRecentItemsWithName();
        for (final RecentItem ri : ril){
            try {
                Context context = getContext();
                if (context == null) {
                    break;
                }
                AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(getContext(), ri.dbPath);
                CardDao dao = helper.getCardDao();
                ri.dbInfo = context.getString(R.string.stat_total) + dao.getTotalCount(null) + " " +
                        getContext().getString(R.string.stat_new) + dao.getNewCardCount(null) + " " +
                        getContext().getString(R.string.stat_scheduled)+ dao.getScheduledCardCount(null);
                ril.set(ri.index, ri);
                AnyMemoDBOpenHelperManager.releaseHelper(helper);
            } catch (Exception e) {
                Log.e(TAG, "Recent list throws exception (Usually can be safely ignored)", e);
            }
        }

        return ril;
    }

    /**
     *  The recent list adapter to handle the actual recycler view logic
     */
    private static class RecentListAdapter extends SelectableAdapter<RecentListAdapter.ViewHolder> {

        private final Context context;

        private final List<RecentItem> items = new ArrayList<>();

        private final RecentListUtil recentListUtil;

        private final RecentListActionModeUtil recentListActionModeUtil;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private TextView filenameView;
            private TextView infoView;
            private Button moreButton;
            private View selectedOverlay;

            public ViewHolder(View view) {
                super(view);
                filenameView = (TextView)view.findViewById(R.id.recent_item_filename);
                infoView = (TextView)view.findViewById(R.id.recent_item_info);
                moreButton = (Button)view.findViewById(R.id.recent_item_more_button);
                selectedOverlay = (View) view.findViewById(R.id.selected_overlay);
            }

            public void setItem(RecentItem item) {
                filenameView.setText(item.dbName);
                infoView.setText(item.dbInfo);
            }
        }

        public RecentListAdapter(Context context, RecentListUtil recentListUtil,
                                 RecentListActionModeUtil recentListActionModeUtil) {
            this.context = context;
            this.recentListUtil = recentListUtil;
            this.recentListActionModeUtil = recentListActionModeUtil;
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
                    if (getSelectedItemCount() == 0) {
                        Intent myIntent = new Intent();
                        myIntent.setClass(context, StudyActivity.class);
                        String dbPath = currentItem.dbPath;
                        myIntent.putExtra(StudyActivity.EXTRA_DBPATH, dbPath);
                        recentListUtil.addToRecentList(dbPath);
                        context.startActivity(myIntent);
                    } else {
                        toggleSelection(position);
                    }
                    recentListActionModeUtil.updateActionMode(getSelectedItemCount());
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getSelectedItemCount() == 0) {
                        recentListActionModeUtil.startActionMode();
                    }
                    toggleSelection(position);
                    recentListActionModeUtil.updateActionMode(getSelectedItemCount());
                    return true;
                }
            });

            holder.moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String dbPath = currentItem.dbPath;
                    DialogFragment df = new OpenActionsFragment();
                    Bundle b = new Bundle();
                    b.putString(OpenActionsFragment.EXTRA_DBPATH, dbPath);
                    df.setArguments(b);
                    df.show(((FragmentActivity) context).getSupportFragmentManager(), "OpenActions");
                }
            });

            holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
        }
        @Override public int getItemCount() {
            return items.size();
        }

        public synchronized List<RecentItem> getList() {
            return new ArrayList<RecentItem>(items);
        }

        public synchronized void setItems(List<RecentItem> items) {
            this.items.clear();
            this.items.addAll(items);
            this.notifyDataSetChanged();
        }

        public void stopActionMode() {
            recentListActionModeUtil.stopActionMode();
        }

        public void removeSelected() {
            final List<Integer> indices = getSelectedItems();
            final List<RecentItem> itemsToRemove = new ArrayList<>();
            for (Integer i:indices) {
                final RecentItem currentItem = this.items.get(i);
                recentListUtil.deleteFromRecentList(currentItem.dbPath);
                itemsToRemove.add(currentItem);
            }
            this.items.removeAll(itemsToRemove);

            this.notifyDataSetChanged();
        }

    }

    // simplified and updated DividerItemDecoration.java from android extras
    private static class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private Drawable mDivider;

        public DividerItemDecoration(Context context) {
            mDivider = ContextCompat.getDrawable(context, R.drawable.divider);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            drawVertical(c, parent);
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        }
    }


}
