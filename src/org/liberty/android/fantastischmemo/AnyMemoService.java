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

public class AnyMemoService extends Service{
    public static int UPDATE_WIDGET = 1;
    public static int UPDATE_NOTIFICATION = 2;
    private final int NOTIFICATION_ID = 4829352;
    private Intent mIntent;
    private final static String TAG = "org.liberty.android.fantastischmemo.AnyMemoService";

    @Override
    public void onStart(Intent intent, int startId){
		Bundle extras = intent.getExtras();
        if(extras == null){
            Log.e(TAG, "Extras is NULL!");
            return;
        }
        mIntent = intent;
        
		int serviceReq = extras.getInt("service_request_code", 0);
        if((serviceReq & UPDATE_WIDGET) != 0){
            updateWidget();
        }
        if((serviceReq & UPDATE_NOTIFICATION) != 0){
            showNotification();
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    private void updateWidget(){
        RemoteViews updateViews = new RemoteViews(getPackageName(), R.layout.widget);
        updateViews.setTextViewText(R.id.widget_db_name, (new Date()).toString());
        ComponentName thisWidget = new ComponentName(this, AnyMemoWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, updateViews);
    }

    private void showNotification(){
        Intent myIntent = new Intent(this, AnyMemo.class);
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.icon, "Hello", System.currentTimeMillis());
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, myIntent, 0);
        notification.setLatestEventInfo(this, "Hello hello", "World world", pIntent);

        notificationManager.notify(NOTIFICATION_ID, notification);
        Log.v(TAG, "Notification Invoked!");
    }


}
