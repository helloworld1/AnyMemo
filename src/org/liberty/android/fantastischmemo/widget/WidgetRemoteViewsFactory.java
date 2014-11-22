package org.liberty.android.fantastischmemo.widget;

import android.appwidget.AppWidgetManager;
import android.widget.RemoteViewsService;
import android.widget.RemoteViews;
import android.content.Context;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.ui.StudyActivity;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import android.content.Intent;
import android.annotation.TargetApi;

import javax.inject.Inject;

import roboguice.RoboGuice;
import roboguice.util.Ln;


@TargetApi(11)
class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private RecentListUtil recentListUtil;

    private Context mContext;

    private String[] allPath;

    private int mAppWidgetId;

    @Inject
    public void setRecentListUtil(RecentListUtil recentListUtil) {
        this.recentListUtil = recentListUtil;
    }

    public WidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        RoboGuice.getInjector(context.getApplicationContext()).injectMembers(this);
    }

    public void onCreate() {
    }

    public void onDestroy() {
    }

    public void onDataSetChanged() {
        allPath = recentListUtil.getAllRecentDBPath();
    }

    public int getViewTypeCount() {
        return 1;
    }

    public boolean hasStableIds() {
        return true;
    }

    public long getItemId(int position) {
        return position;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getCount() {
        int count = 0;
        for (int i = 0; i < allPath.length; i++) {
            if (allPath[i] != null) count++;
        }
        return count;
    }

    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
        if (position < allPath.length) {
            AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mContext, allPath[position]);
            CardDao dao = helper.getCardDao();
            String dbName = FilenameUtils.getName(allPath[position]);
            long totalNum = dao.getTotalCount(null);
            long revNum = dao.getScheduledCardCount(null);
            String viewNumber = mContext.getString(R.string.stat_total) + totalNum + " " + mContext.getString(R.string.stat_scheduled) + revNum;
            rv.setTextViewText(R.id.widget_item_name, dbName);
            rv.setTextViewText(R.id.widget_item_number, viewNumber);
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(StudyActivity.EXTRA_DBPATH,allPath[position]);
            rv.setOnClickFillInIntent(R.id.widget_item,fillInIntent);
        }
        return rv;
    }
}