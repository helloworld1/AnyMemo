package org.liberty.android.fantastischmemo.service;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.aspect.CheckNullArgs;
import org.liberty.android.fantastischmemo.aspect.LogInvocation;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerContext;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerEventHandler;
import org.liberty.android.fantastischmemo.service.cardplayer.CardPlayerMessage;
import org.liberty.android.fantastischmemo.ui.CardPlayerActivity;
import org.liberty.android.fantastischmemo.ui.PreviewEditActivity;
import org.liberty.android.fantastischmemo.utils.CardTTSUtil;
import org.liberty.android.fantastischmemo.utils.CardTTSUtilFactory;

import roboguice.service.RoboService;
import roboguice.util.Ln;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class CardPlayerService extends RoboService {

    public static final String EXTRA_DBPATH = "dbpath";

    // Magic id used for Card player's notification
    private static final int NOTIFICATION_ID = 9283372;

    // This is the object that receives interactions from clients.
    private final IBinder binder = new LocalBinder();

    private String dbPath;

    private AnyMemoDBOpenHelper dbOpenHelper;

    private Handler handler;

    private Option option;

    private CardTTSUtil cardTTSUtil;

    private CardTTSUtilFactory cardTTSUtilFactory;

    // The context used for card player state machine.
    private volatile CardPlayerContext cardPlayerContext = null;

    @Inject
    public void setOption(Option option) {
        this.option = option;
    }

    @Inject
    public void setCardTTSUtilFactory(CardTTSUtilFactory cardTTSUtilFactory) {
        this.cardTTSUtilFactory = cardTTSUtilFactory;
    }

    // Note, it is recommended for service binding in a thread different
    // from UI thread. The initialization like DAO creation is quite heavy
    @Override
    @LogInvocation
    public IBinder onBind(Intent intent) {
        handler = new Handler();
        Bundle extras = intent.getExtras();

        assert extras != null : "dbpath is not passed to AMTTSService.";

        dbPath = extras.getString(EXTRA_DBPATH);
        
        cardTTSUtil = cardTTSUtilFactory.create(dbPath);

        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(this, dbPath);

        return binder;
    }

    @Override
    @LogInvocation
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    @LogInvocation
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    @LogInvocation
    public boolean onUnbind(Intent intent) {
        // Always stop service on unbind so the service will not be reused
        // for the next binding.
        return false;
    }

    @Override
    @LogInvocation
    public void onDestroy() {
        super.onDestroy();
    }

    @CheckNullArgs
    public void startPlaying(Card startCard, CardPlayerEventHandler eventHandler) {
        // Always to create a new context if we start playing to ensure it is playing
        // from a clean state.
        cardPlayerContext = new CardPlayerContext(
                eventHandler,
                cardTTSUtil,
                handler,
                dbOpenHelper,
                option.getCardPlayerIntervalBetweenQA(),
                option.getCardPlayerIntervalBetweenCards());

        cardPlayerContext.setCurrentCard(startCard);
        cardPlayerContext.getState().transition(cardPlayerContext, CardPlayerMessage.START_PLAYING);
        showNotification();
    }

    public void skipToNext() {
        if (cardPlayerContext != null) {
            cardPlayerContext.getState().transition(cardPlayerContext, CardPlayerMessage.GO_TO_NEXT);
        } else {
            Ln.i("Call skipToPrev with null cardPlayerContext. Do nothing.");
        }
    }

    public void skipToPrev() {
        if (cardPlayerContext != null) {
            cardPlayerContext.getState().transition(cardPlayerContext, CardPlayerMessage.GO_TO_PREV);
        } else {
            Ln.i("Call skipToPrev with null cardPlayerContext. Do nothing.");
        }
    }

    public void stopPlaying() {
        Ln.v("Stop playing");
        cancelNotification();
        if (cardPlayerContext != null) {
            cardPlayerContext.getState().transition(cardPlayerContext, CardPlayerMessage.STOP_PLAYING);
        } else {
            Ln.i("Call stopPlaying with null cardPlayerContext. Do nothing.");
        }
    }

    /*
     * A notification is shown if the player is playing.
     * This also put the service in foreground mode to prevent the service
     * being terminated.
     */
    private void showNotification() {

        Intent resultIntent = new Intent(this, CardPlayerActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(CardPlayerActivity.class);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra(PreviewEditActivity.EXTRA_DBPATH, dbPath);
        if (cardPlayerContext != null) {
            resultIntent.putExtra(PreviewEditActivity.EXTRA_CARD_ID, cardPlayerContext.getCurrentCard().getId());
        } else {
            Ln.w("The notification for card player is shown but the cardPlayerContext is null!");
        }

        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
            stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(getString(R.string.card_player_notification_title))
            .setContentText(getString(R.string.card_player_notification_text))
            .setContentIntent(resultPendingIntent)
            .setOngoing(true);

        // Basically make the service foreground so a notification is shown
        // And the service is less susceptible to be kill by Android system.
        startForeground(NOTIFICATION_ID, mBuilder.build());
    }

    private void cancelNotification() {
        stopForeground(true);
    }

    // A local binder that works for local methos call.
    public class LocalBinder extends Binder {
        public CardPlayerService getService() {
            return CardPlayerService.this;
        }

        public Card getCurrentPlayingCard() {
            if (cardPlayerContext != null) {
                return cardPlayerContext.getCurrentCard();
            } else {
                return null;
            }
        }
    }

}

