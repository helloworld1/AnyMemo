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


import android.app.PendingIntent;
import android.app.Service;
import android.os.Bundle;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import java.util.Date;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class AnyMemoService extends Service{
    public static int UPDATE_WIDGET = 1;
    public static int UPDATE_NOTIFICATION = 2;
    public static int CANCEL_NOTIFICATION = 4;
    private final int NOTIFICATION_ID = 4829352;
    private final static String TAG = "org.liberty.android.fantastischmemo.AnyMemoService";

    @Override
    public void onStart(Intent intent, int startId){
		Bundle extras = intent.getExtras();
        if(extras == null){
            Log.e(TAG, "Extras is NULL!");
            return;
        }
        
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
            updateViews.setTextViewText(R.id.widget_review_count, getString(R.string.stat_scheduled) + " " + dbInfo.getRevCount());
            updateViews.setTextViewText(R.id.widget_new_count, getString(R.string.stat_new) + " " + dbInfo.getNewCount());
            manager.updateAppWidget(thisWidget, updateViews);
        }
        catch(Exception e){
            updateViews.setTextViewText(R.id.widget_db_name, getString(R.string.widget_fail_fetch));
            manager.updateAppWidget(thisWidget, updateViews);
        }
    }

    private void showNotification(){
        try{
            DatabaseInfo dbInfo = new DatabaseInfo(this);
            Intent myIntent = new Intent(this, AnyMemo.class);
            myIntent.putExtra("startup_screen", "Open Screen");
            NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.icon, getString(R.string.app_name), System.currentTimeMillis());
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, myIntent, 0);
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
        private int revCount;
        private int newCount;

        public DatabaseInfo(Context context) throws Exception{
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            /* Feed the data from the most recent database */
            dbName = settings.getString("recentdbname0", "");
            dbPath = settings.getString("recentdbpath0", "");

            DatabaseHelper dbHelper = new DatabaseHelper(context, dbPath, dbName);
            revCount = dbHelper.getScheduledCount();
            newCount = dbHelper.getNewCount();
            dbHelper.close();
        }

        public DatabaseInfo(String dbname, String dbpath){
        }

        public DatabaseInfo(String dbname, String dbpath, int revcount, int newcount){
        }

        public String getDbName(){
            return dbName;
        }

        public String getDbPath(){
            return dbPath;
        }

        public int getNewCount(){
            return newCount;
        }

        public int getRevCount(){
            return revCount;
        }
    }
}
