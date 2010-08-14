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

import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;


public class NotificationReceiver extends BroadcastReceiver{
    private NotificationManager mNotificationManager;
    private final int NOTIFICATION_ID = 4829352;
    private final static String TAG = "org.liberty.android.fantastischmemo.NotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent){
        Intent myIntent = new Intent(context, AnyMemo.class);
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.icon, "Hello", System.currentTimeMillis());
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, myIntent, 0);
        notification.setLatestEventInfo(context, "Hello hello", "World world", pIntent);

        mNotificationManager.notify(NOTIFICATION_ID, notification);
        Log.v(TAG, "Notification Invoked!");
    }
}



