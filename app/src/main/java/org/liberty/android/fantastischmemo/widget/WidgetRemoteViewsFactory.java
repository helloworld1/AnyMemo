package org.liberty.android.fantastischmemo.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMApplication;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.ui.StudyActivity;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import javax.inject.Inject;

@TargetApi(11)
public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {


    private Context mContext;

    private String[] allPath;

    private int mAppWidgetId;

    @Inject RecentListUtil recentListUtil;

    public WidgetRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        ((AMApplication) context.getApplicationContext()).appComponents().inject(this);
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
            long totalCount = dao.getTotalCount(null);
            long revCount = dao.getScheduledCardCount(null);
            long newCount = dao.getNewCardCount(null);

            String detail = mContext.getString(R.string.stat_total) + totalCount + " "
                + mContext.getString(R.string.stat_new) + newCount + " "
                + mContext.getString(R.string.stat_scheduled) + revCount;
            rv.setTextViewText(R.id.widget_db_name, dbName);
            rv.setTextViewText(R.id.widget_db_detail, detail);
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(StudyActivity.EXTRA_DBPATH,allPath[position]);
            rv.setOnClickFillInIntent(R.id.widget_item,fillInIntent);
        }
        return rv;
    }
}
