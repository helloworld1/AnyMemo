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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.common.base.Objects;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.BaseIntentService;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.ui.AnyMemo;
import org.liberty.android.fantastischmemo.ui.PreviewEditActivity;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import java.util.Map;

import javax.inject.Inject;

public class ConvertIntentService extends BaseIntentService {

    public static final String ACTION_CONVERT = "convert";

    public static final String EXTRA_INPUT_FILE_PATH = "inputFilePath";

    public static final String EXTRA_OUTPUT_FILE_PATH = "outputFilePath";

    public static final String EXTRA_CONVERTER_CLASS = "converterClass";

    public static final String TAG = ConvertIntentService.class.getSimpleName();

    private static final int CONVERSION_PROGRESS_NOTIFICATION_ID_BASE = 294;

    private NotificationManager notificationManager;

    @Inject RecentListUtil recentListUtil;

    @Inject Map<Class<?>, Converter> converterMap;

    public ConvertIntentService() {
        super(ConvertIntentService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appComponents().inject(this);

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        if (!Objects.equal(intent.getAction(), ACTION_CONVERT)) {
            throw new IllegalArgumentException("The Action is wrong");
        }
        String inputFilePath = intent.getStringExtra(EXTRA_INPUT_FILE_PATH);
        assert inputFilePath != null : "Input file path should not be null";

        String outputFilePath = intent.getStringExtra(EXTRA_OUTPUT_FILE_PATH);
        assert outputFilePath != null : "Output file path should not be null";

        @SuppressWarnings("unchecked")
        Class<Converter> converterClass = (Class<Converter>) intent.getSerializableExtra(EXTRA_CONVERTER_CLASS);

        Converter converter = converterMap.get(converterClass);

        // Replace the extension of the file: file.xml -> file.db

        String conversionFileInfo = "" + FilenameUtils.getName(inputFilePath) + " -> " + FilenameUtils.getName(outputFilePath);

        int notificationId = CONVERSION_PROGRESS_NOTIFICATION_ID_BASE + inputFilePath.hashCode();

        showInProgressNotification(notificationId, conversionFileInfo);
        try {
            converter.convert(inputFilePath, outputFilePath);

            showSuccessNotification(notificationId, outputFilePath);

        } catch (Exception e) {
            Log.e(TAG, "Error while converting", e);
            showFailureNotification(notificationId, conversionFileInfo, e);
        }
    }

    private void showInProgressNotification(int notificationId, String conversionFileInfo) {

        // Dummy intent used for Android 2.3 compatibility
        PendingIntent dummyPendingIntent= PendingIntent.getActivity(this, 0, new Intent(), Intent.FLAG_ACTIVITY_NEW_TASK);

        Notification inProgressNotification = new NotificationCompat.Builder(getApplicationContext())
            .setOngoing(true)
            .setContentTitle(getString(R.string.converting_wait_text))
            .setContentText(conversionFileInfo)
            .setContentIntent(dummyPendingIntent)
            .setProgress(0, 0, true)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setAutoCancel(true)
            .build();

        notificationManager.notify(notificationId, inProgressNotification);
    }

    private void showFailureNotification(int notificationId, String conversionFileInfo, Exception exception) {
        // Dummy intent used for Android 2.3 compatibility
        PendingIntent dummyPendingIntent= PendingIntent.getActivity(this, 0, new Intent(), Intent.FLAG_ACTIVITY_NEW_TASK);

        Notification failureNotification = new NotificationCompat.Builder(getApplicationContext())
            .setOngoing(false)
            .setContentTitle(getString(R.string.converting_failure_text))
            .setContentText(conversionFileInfo)
            .setContentIntent(dummyPendingIntent)
            .setSubText(exception.toString())
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.anymemo_notification_icon)
            .build();
        notificationManager.notify(notificationId, failureNotification);
    }

    private void showSuccessNotification(int notificationId, String outputFilePath) {
        Intent resultIntent = null;

        // For DB file, open it in the Preview/edit.
        if (FilenameUtils.getExtension(outputFilePath).toLowerCase().equals("db")) {
            resultIntent = new Intent(this, PreviewEditActivity.class);
            resultIntent.putExtra(PreviewEditActivity.EXTRA_DBPATH, outputFilePath);

            // Add to recent list util if it is a db
            recentListUtil.addToRecentList(outputFilePath);

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
            .setSmallIcon(R.drawable.anymemo_notification_icon)
            .setAutoCancel(true)
            .build();

        notificationManager.notify(notificationId, successNotification);
    }
}
