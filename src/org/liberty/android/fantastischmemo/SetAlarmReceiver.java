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
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;
import java.util.Calendar;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SetAlarmReceiver extends BroadcastReceiver{
    /* This class contains the static methods to set up the alarm
     * also it is a receiver that can be invoked every boot time.
     */
    private final static String TAG = "org.liberty.android.fantastischmemo.SetAlarmReceiver";
    private final static int ALARM_REQUEST_CODE = 1548345;

    @Override
    public void onReceive(Context context, Intent intent){
        setNotificationAlarm(context);
    }

    public static void setNotificationAlarm(Context context){
        /* Set an alarm for the notification */
        Log.v(TAG, "Set ALARM here!");
        /* Set interval from the settings */

    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String s = settings.getString("notification_interval", "24 hr");
        long interval = AlarmManager.INTERVAL_DAY;
        if(s.equals("Disabled")){
            return;
        }
        else if(s.equals("1 hr")){
            interval = AlarmManager.INTERVAL_HOUR * 1;
        }
        else if(s.equals("6 hr")){
            interval = AlarmManager.INTERVAL_HOUR * 6;
        }
        else if(s.equals("12 hr")){
            interval = AlarmManager.INTERVAL_HOUR * 12;
        }
        else if(s.equals("24 hr")){
            interval = AlarmManager.INTERVAL_HOUR * 24 - 10;
        }

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        myIntent.putExtra("request_code", AlarmReceiver.ALARM_NOTIFICATION);
        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        /* Set up the alarm time */
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 2);
        calendar.set(Calendar.SECOND, 3);


        //am.set(AlarmManager.RTC, System.currentTimeMillis() + 15000, sender);
        am.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), interval, sender);
    }

    public static void cancelNotificationAlarm(Context context){
        /* Set an alarm for the notification */
        Log.v(TAG, "Set ALARM here!");
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        myIntent.putExtra("request_code", AlarmReceiver.ALARM_NOTIFICATION);
        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }

    public static void setWidgetUpdateAlarm(Context context){
        /* Set an alarm for the widget to update it*/
        Log.v(TAG, "Set ALARM here Update Widget!");
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        myIntent.putExtra("request_code", AlarmReceiver.ALARM_WIDGET);
        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE + 1, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, sender);
    }

    public static void cancelWidgetUpdateAlarm(Context context){
        Log.v(TAG, "Cancel Widget ALARM here !");
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        myIntent.putExtra("request_code", AlarmReceiver.ALARM_WIDGET);
        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE + 1, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }

}

