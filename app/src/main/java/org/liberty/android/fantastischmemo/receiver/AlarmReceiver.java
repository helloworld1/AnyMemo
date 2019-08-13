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
package org.liberty.android.fantastischmemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.liberty.android.fantastischmemo.common.AMApplication;
import org.liberty.android.fantastischmemo.utils.NotificationUtil;
import org.liberty.android.fantastischmemo.widget.AnyMemoWidgetProvider;

import javax.inject.Inject;


public class AlarmReceiver extends BroadcastReceiver {
    public static final int ALARM_SHOW_NOTIFICATION = 1;
    public static final int ALARM_WIDGET = 2;
    public static final int ALARM_CANCEL_NOTIFICATION = 4;
    private final static String TAG = AlarmReceiver.class.getSimpleName();

    @Inject NotificationUtil notificationUtil;

    @Override
    public void onReceive(Context context, Intent intent){
        ((AMApplication) context.getApplicationContext()).appComponents().inject(this);
        Bundle extras = intent.getExtras();
        if(extras == null){
            Log.e(TAG, "Receive NULL extras");
            return;
        }

        int alarmReq = extras.getInt("request_code", 0);

        Log.v(TAG, "Receive req: " + Integer.toString(alarmReq));
        if ((alarmReq & ALARM_SHOW_NOTIFICATION) != 0) {
            Log.v(TAG, "ALARM NOTIFICATION_ALARM");

            notificationUtil.showNotification();
        }

        if ((alarmReq & ALARM_CANCEL_NOTIFICATION) != 0) {
            Log.v(TAG, "ALARM CANCEL_NOTIFICATION");

            notificationUtil.cancelNotification();
        }

        if ((alarmReq & ALARM_WIDGET) != 0) {
            Log.v(TAG, "ALARM WIDGET");
            AnyMemoWidgetProvider.updateWidget(context);
        }
    }
}



