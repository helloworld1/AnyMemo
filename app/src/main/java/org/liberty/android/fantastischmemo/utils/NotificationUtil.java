package org.liberty.android.fantastischmemo.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.common.base.Strings;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMPrefKeys;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.modules.ForApplication;
import org.liberty.android.fantastischmemo.modules.PerApplication;
import org.liberty.android.fantastischmemo.ui.AnyMemo;

import javax.inject.Inject;

@PerApplication
public class NotificationUtil {
    private static final String TAG = NotificationUtil.class.getSimpleName();

    private static final String CHANNEL_ID = "REVIEW_REMINDER";

    private static final int NOTIFICATION_ID = 4829352;
    private static final int NOTIFICATION_REQ = 17239203;

    private Context appContext;

    @Inject
    public NotificationUtil(@ForApplication Context appContext) {
        this.appContext = appContext;
    }

    public void createNotificationChannel() {
        // Create the Notification Channel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = appContext.getString(R.string.review_reminder);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);

            NotificationManager notificationManager = appContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(){
        try{
            DatabaseInfo dbInfo = new DatabaseInfo(appContext);
            if(dbInfo.getRevCount() < 10){
                return;
            }
            Intent myIntent = new Intent(appContext, AnyMemo.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent pIntent = PendingIntent.getActivity(appContext, NOTIFICATION_REQ, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            Notification notification = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                    .setTicker("AnyMemo")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.anymemo_notification_icon)
                    .setContentTitle(dbInfo.getDbName())
                    .setContentText(appContext.getString(R.string.stat_scheduled) + " " + dbInfo.getRevCount())
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(NOTIFICATION_ID, notification);
            Log.v(TAG, "Notification Invoked!");
        }
        catch(Exception e){
            Log.e(TAG, "Error showing notification", e);
        }
    }

    public void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null){
            notificationManager.cancel(NOTIFICATION_ID);
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
                AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(appContext, dbPath);
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
