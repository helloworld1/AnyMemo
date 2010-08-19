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
import android.app.PendingIntent;
import android.widget.RemoteViews;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.net.Uri;
import java.util.Date;


public class AnyMemoWidgetProvider extends AppWidgetProvider{

    private final static String TAG = "org.liberty.android.fantastischmemo.AnyMemoWidgetProvider";

    @Override
    public void onEnabled(Context context){
        super.onEnabled(context);
        SetAlarmReceiver.setWidgetUpdateAlarm(context);
        Log.v(TAG, "Widget Enabled!");
    }

    @Override
    public void onDisabled(Context context){
        SetAlarmReceiver.cancelWidgetUpdateAlarm(context);
        super.onDisabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        final int N = appWidgetIds.length;
        Log.v(TAG, "Widget Updated!");

        Intent myIntent = new Intent(context, AnyMemoService.class);
        myIntent.putExtra("request_code", AnyMemoService.UPDATE_WIDGET);
        context.startService(myIntent);
    }

}
