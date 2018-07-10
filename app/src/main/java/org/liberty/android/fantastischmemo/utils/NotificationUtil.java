package org.liberty.android.fantastischmemo.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
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
    private final int NOTIFICATION_ID = 4829352;
    private final int NOTIFICATION_REQ = 17239203;

    private Context appContext;

    @Inject
    public NotificationUtil(@ForApplication Context appContext) {
        this.appContext = appContext;
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

            Notification notification = new NotificationCompat.Builder(appContext)
                    .setTicker("AnyMemo")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.anymemo_notification_icon)
                    .setContentTitle(dbInfo.getDbName())
                    .setContentText(appContext.getString(R.string.stat_scheduled) + " " + dbInfo.getRevCount())
                    .setContentIntent(pIntent)
                    .build();

            notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;

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
