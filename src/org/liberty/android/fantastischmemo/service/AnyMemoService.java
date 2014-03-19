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

package org.liberty.android.fantastischmemo.service;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.AnyMemoWidgetProvider;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.ui.AnyMemo;
import org.liberty.android.fantastischmemo.ui.StudyActivity;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import roboguice.service.RoboService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.common.base.Strings;

public class AnyMemoService extends RoboService{
    public static int UPDATE_WIDGET = 1;
    public static int UPDATE_NOTIFICATION = 2;
    public static int CANCEL_NOTIFICATION = 4;
    private final int NOTIFICATION_ID = 4829352;
    private final int NOTIFICATION_REQ = 17239203;
    private final int WIDGET_REQ = 23579234;
    private final static String TAG = "org.liberty.android.fantastischmemo.AnyMemoService";

    private RecentListUtil recentListUtil;

    @Inject
    public void setRecentListUtil(RecentListUtil recentListUtil) {
        this.recentListUtil = recentListUtil;
    }

    @Override
    public void onStart(Intent intent, int startId){
        Bundle extras = intent.getExtras();
        if(extras == null){
            Log.e(TAG, "Extras is NULL!");
            return;
        }
            Log.v(TAG, "Service now!");

        int serviceReq = extras.getInt("request_code", 0);
        if((serviceReq & UPDATE_WIDGET) != 0){
            updateWidget();
        }
        if((serviceReq & UPDATE_NOTIFICATION) != 0){
            showNotification();
        }

        if((serviceReq & CANCEL_NOTIFICATION) != 0){
            cancelNotification();
        }
        stopSelf();

    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    private void updateWidget(){
        RemoteViews updateViews = new RemoteViews(getPackageName(), R.layout.widget);
        ComponentName thisWidget = new ComponentName(this, AnyMemoWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        try{
            DatabaseInfo dbInfo = new DatabaseInfo(this);
            updateViews.setTextViewText(R.id.widget_db_name, dbInfo.getDbName());
            int revCount = dbInfo.getRevCount();
            /* Display different colors for different review number*/
            updateViews.setTextViewText(R.id.widget_new_count, getString(R.string.stat_new) + " " + dbInfo.getNewCount());
            if(revCount == 0){
                updateViews.setTextViewText(R.id.widget_review_count, getString(R.string.widget_no_review));
                /* Dark green color */
                updateViews.setTextColor(R.id.widget_review_count, 0xFF008100);
            }
            else{
                updateViews.setTextViewText(R.id.widget_review_count, getString(R.string.stat_scheduled) + " " + dbInfo.getRevCount());
                if(revCount <= 10){
                    updateViews.setTextColor(R.id.widget_review_count, 0xFF008100);
                }
                else if(revCount <= 50){
                    updateViews.setTextColor(R.id.widget_review_count, Color.BLACK);
                }
                else if(revCount <= 100){
                    updateViews.setTextColor(R.id.widget_review_count, Color.MAGENTA);
                }
                else{
                    updateViews.setTextColor(R.id.widget_review_count, Color.RED);
                }
            }

        }
        catch(Exception e){
            Log.e(TAG, "Update widget error", e);
            updateViews.setTextViewText(R.id.widget_db_name, getString(R.string.widget_fail_fetch));
            updateViews.setTextViewText(R.id.widget_review_count, "");
            updateViews.setTextViewText(R.id.widget_new_count, "");
        }
        finally{
            /* Set on click event */
            Intent intent = new Intent(this, StudyActivity.class);
            intent.putExtra("dbpath", recentListUtil.getRecentDBPath());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, WIDGET_REQ, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            updateViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
            manager.updateAppWidget(thisWidget, updateViews);
        }
    }

    @SuppressWarnings("deprecation")
    private void showNotification(){
        try{
            DatabaseInfo dbInfo = new DatabaseInfo(this);
            if(dbInfo.getRevCount() < 10){
                return;
            }
            Intent myIntent = new Intent(this, AnyMemo.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notification = new Notification(R.drawable.anymemo_notification_icon, getString(R.string.app_name), System.currentTimeMillis());
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            PendingIntent pIntent = PendingIntent.getActivity(this, NOTIFICATION_REQ, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            notification.setLatestEventInfo(this, dbInfo.getDbName(), getString(R.string.stat_scheduled) + " " + dbInfo.getRevCount(), pIntent);

            notificationManager.notify(NOTIFICATION_ID, notification);
            Log.v(TAG, "Notification Invoked!");
        }
        catch(Exception e){
            /* Do not show notification when AnyMemo can not
             * fetch the into
             */
        }
    }

    private void cancelNotification(){
        try{
            NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
        }
        catch(Exception e){
        }
    }

    private class DatabaseInfo{
        private String dbName;
        private String dbPath;
        private int revCount = 0;
        private int newCount = 0;

        public DatabaseInfo(Context context) throws Exception{
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            /* Feed the data from the most recent database */
            dbPath = settings.getString(AMPrefKeys.getRecentPathKey(0), "");
            dbName = FilenameUtils.getName(dbPath);

            if (!Strings.isNullOrEmpty(dbPath)) {
                AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(AnyMemoService.this, dbPath);
                try {
                    final CardDao cardDao = helper.getCardDao();
                    revCount = (int)cardDao.getScheduledCardCount(null);
                    newCount = (int)cardDao.getNewCardCount(null);
                } finally {
                    AnyMemoDBOpenHelperManager.releaseHelper(helper);
                }
            }
        }

        public String getDbName(){
            return dbName;
        }

        public int getNewCount(){
            return newCount;
        }

        public int getRevCount(){
            return revCount;
        }
    }
}
