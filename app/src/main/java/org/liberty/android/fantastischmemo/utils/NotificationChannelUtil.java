package org.liberty.android.fantastischmemo.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.modules.ForApplication;
import org.liberty.android.fantastischmemo.modules.PerApplication;

import javax.inject.Inject;

@PerApplication
public class NotificationChannelUtil {

    public static String CARD_PLAYER_NOTIFICATION_CHANNEL_ID = "CardPlayerNotificationChannel";

    public Context context;

    @Inject
    public NotificationChannelUtil(@ForApplication Context context) {
        this.context = context;
    }

    public void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.card_player_notification_channel_title);
            String description = context.getString(R.string.card_player_notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CARD_PLAYER_NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

    }
}
