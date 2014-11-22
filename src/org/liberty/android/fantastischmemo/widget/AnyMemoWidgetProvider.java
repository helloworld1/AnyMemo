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

package org.liberty.android.fantastischmemo.widget;

import org.liberty.android.fantastischmemo.SetAlarmReceiver;
import org.liberty.android.fantastischmemo.service.AnyMemoService;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.widget.RemoteViews;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.ui.AnyMemo;
import org.liberty.android.fantastischmemo.ui.StudyActivity;

import android.net.Uri;
import android.annotation.TargetApi;


public class AnyMemoWidgetProvider extends AppWidgetProvider{
    private static final int WIDGET_REQUEST_ID = 23452435;

    public static void updateWidget(Context context)
    {
        Intent intent = new Intent();
        //int[] widgetId = {WIDGET_REQUEST_ID};
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] widgetId = manager.getAppWidgetIds(new ComponentName(context, AnyMemoWidgetProvider.class));
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetId);
        context.sendBroadcast(intent);

//        AppWidgetManager man = AppWidgetManager.getInstance(this);
//        int[] ids = man.getAppWidgetIds(new ComponentName(this,AnyMemoWidgetProvider.class));
//        Intent myIntent = new Intent();
//        myIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//        myIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
//        this.sendBroadcast(myIntent);

    }

    @Override
    public void onEnabled(Context context){

    }

    @Override
    public void onDisabled(Context context){

    }

    @Override
    @TargetApi(11)
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){

        // update each of the app widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {
            Intent intent = new Intent(context, UpdateWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);

            Intent widgetIntent = new Intent(context, StudyActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,WIDGET_REQUEST_ID,widgetIntent,PendingIntent.FLAG_CANCEL_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_list, pendingIntent);
            rv.setRemoteAdapter(appWidgetIds[i], R.id.widget_list, intent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
