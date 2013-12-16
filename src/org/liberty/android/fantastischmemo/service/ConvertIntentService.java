/*
Copyright (C) 2013 Haowen Ning

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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.ui.AnyMemo;

import org.liberty.android.fantastischmemo.ui.PreviewEditActivity;

import roboguice.RoboGuice;
import roboguice.service.RoboIntentService;
import roboguice.util.Ln;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class ConvertIntentService extends RoboIntentService {

    public static final String ACTION_CONVERT = "convert";

    public static final String EXTRA_INPUT_FILE_PATH = "inputFilePath";

    public static final String EXTRA_CONVERTER_CLASS = "converterClass";

    private static final int CONVERSION_PROGRESS_NOTIFICATION_ID_BASE = 294;

    private NotificationManager notificationManager;

    public ConvertIntentService() {
        super(ConvertIntentService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        if (!StringUtils.equals(intent.getAction(), ACTION_CONVERT)) {
            throw new IllegalArgumentException("The Action is wrong");
        }
        String inputFilePath = intent.getStringExtra(EXTRA_INPUT_FILE_PATH);
        assert inputFilePath != null : "Input file path should not be null";

        @SuppressWarnings("unchecked")
        Class<Converter> conveterClass = (Class<Converter>) intent.getSerializableExtra(EXTRA_CONVERTER_CLASS);
        Converter converter = RoboGuice.getInjector(getApplication()).getInstance(conveterClass);

        String outputFilePath = inputFilePath + "." + converter.getDestExtension();
        assert outputFilePath != null : "Output file path should not be null";

        String conversionFileInfo = "" + FilenameUtils.getName(inputFilePath) + " -> " + FilenameUtils.getName(outputFilePath);

        int notificationId = CONVERSION_PROGRESS_NOTIFICATION_ID_BASE + inputFilePath.hashCode();

        showInProgressNotification(notificationId, conversionFileInfo);
        try {
            converter.convert(inputFilePath, outputFilePath);

            showSuccessNotification(notificationId, outputFilePath);

        } catch (Exception e) {
            Ln.e(e, "Error while converting");
            showFailureNotification(notificationId, conversionFileInfo, e);
        }
    }

    private void showInProgressNotification(int notificationId, String conversionFileInfo) {
        Notification inProgressNotification = new NotificationCompat.Builder(getApplicationContext())
            .setOngoing(true)
            .setContentTitle(getString(R.string.converting_wait_text))
            .setContentText(conversionFileInfo)
            .setProgress(0, 0, true)
            //.setSmallIcon(R.drawable.icon_notification)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setAutoCancel(true)
            .build();

        notificationManager.notify(notificationId, inProgressNotification);
            
    }

    private void showFailureNotification(int notificationId, String conversionFileInfo, Exception exception) {
        Notification failureNotification = new NotificationCompat.Builder(getApplicationContext())
            .setOngoing(false)
            .setContentTitle(getString(R.string.converting_failure_text))
            .setContentText(conversionFileInfo)
            .setSubText(exception.toString())
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.icon_notification)
            .build();
        notificationManager.notify(notificationId, failureNotification);
    }

    private void showSuccessNotification(int notificationId, String outputFilePath) {
        Intent resultIntent = null;

        // For DB file, open it in the Preview/edit.
        if (FilenameUtils.getExtension(outputFilePath).toLowerCase().equals("db")) {
            resultIntent = new Intent(this, PreviewEditActivity.class);
            resultIntent.putExtra(PreviewEditActivity.EXTRA_DBPATH, outputFilePath);

        } else {
            // For other files, open it in AnyMemo main activity
            resultIntent = new Intent(this, AnyMemo.class);
        }

        PendingIntent pendingIntent = TaskStackBuilder.create(this)
            .addNextIntentWithParentStack(resultIntent)
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);

        Notification successNotification = new NotificationCompat.Builder(getApplicationContext())
            .setOngoing(false)
            .setContentTitle(getString(R.string.converting_success_text))
            .setContentText(FilenameUtils.getName(outputFilePath))
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.icon_notification)
            .setAutoCancel(true)
            .build();

        notificationManager.notify(notificationId, successNotification);
    }
}
