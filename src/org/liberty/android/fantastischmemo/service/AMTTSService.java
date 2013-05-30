package org.liberty.android.fantastischmemo.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.mycommons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.aspect.CheckNullArgs;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakContext;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakEventHandler;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakMessage;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTSImpl;
import org.liberty.android.fantastischmemo.tts.NullAnyMemoTTS;
import org.liberty.android.fantastischmemo.ui.PreviewEditActivity;

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

public class AMTTSService extends RoboService {

    public static final String EXTRA_DBPATH = "dbpath";

    // Magic id used for AutoSpeak's notification
    private static final int NOTIFICATION_ID = 9283372;

    // This is the object that receives interactions from clients.
    private final IBinder binder = new LocalBinder();

    private String dbPath;

    private AnyMemoDBOpenHelper dbOpenHelper;

    private CardDao cardDao;

    private SettingDao settingDao;

    private AnyMemoTTS questionTTS;

    private AnyMemoTTS answerTTS;

    private Setting setting;

    private Handler handler;

    private Option option;

    // The context used for autoSpeak state machine.
    private volatile AutoSpeakContext autoSpeakContext = null;

    @Inject
    public void setOption(Option option) {
        this.option = option;
    }

    // Note, it is recommended for service binding in a thread different
    // from UI thread. The initialization like DAO creation is quite heavy
    @Override
    public IBinder onBind(Intent intent) {
        Ln.v("Bind to AMTTSService using intent: " + intent);
        handler = new Handler();
        Bundle extras = intent.getExtras();

        assert extras != null : "dbpath is not passed to AMTTSService.";

        dbPath = extras.getString(EXTRA_DBPATH);

        // It is possible the service is started multiple times and reuse
        // the same instance. So clean up first.
        cleanUp();
        
        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(this, dbPath);

        cardDao = dbOpenHelper.getCardDao();
        settingDao = dbOpenHelper.getSettingDao();

        initTTS();

        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Ln.v("Unbind from AMTTSService using intent: " + intent);
        cleanUp();
        // Always stop service on unbind so the service will not be reused
        // for the next binding.
        stopSelf();
        return false;
    }

    /* 
     * This will speak the question of the card and will not
     * set a callback for speaking completion.
     */
    @CheckNullArgs
    public void speakCardQuestion(Card card) {
        speakCardQuestion(card, null);
    }

    @CheckNullArgs(argIndexToCheck = {0})
    public void speakCardQuestion(Card card, AnyMemoTTS.OnTextToSpeechCompletedListener onTextToSpeechCompletedListener) {
        stopSpeak();
        questionTTS.sayText(card.getQuestion(), onTextToSpeechCompletedListener);
    }

    /* 
     * This will speak the answer of the card and will not
     * set a callback for speaking completion.
     */
    @CheckNullArgs
    public void speakCardAnswer(Card card) {
        speakCardAnswer(card, null);
    }

    @CheckNullArgs(argIndexToCheck = {0})
    public void speakCardAnswer(Card card, AnyMemoTTS.OnTextToSpeechCompletedListener onTextToSpeechCompletedListener) {
        stopSpeak();
        answerTTS.sayText(card.getAnswer(), onTextToSpeechCompletedListener);
    }

    public void stopSpeak() {
        questionTTS.stop();
        answerTTS.stop();
    }

    @CheckNullArgs
    public void startPlaying(Card startCard, AutoSpeakEventHandler eventHandler) {
        // Always to create a new context if we start playing to ensure it is playing
        // from a clean state.
        autoSpeakContext = new AutoSpeakContext(
                eventHandler,
                this,
                handler,
                dbOpenHelper,
                option.getAutoSpeakIntervalBetweenQA(),
                option.getAutoSpeakIntervalBetweenCards());

        autoSpeakContext.setCurrentCard(startCard);
        autoSpeakContext.getState().transition(autoSpeakContext, AutoSpeakMessage.START_PLAYING);
        showNotification();
    }

    public void skipToNext() {
        if (autoSpeakContext != null) {
            autoSpeakContext.getState().transition(autoSpeakContext, AutoSpeakMessage.GO_TO_NEXT);
        } else {
            Ln.i("Call skipToPrev with null autoSpeakContext. Do nothing.");
        }
    }

    public void skipToPrev() {
        if (autoSpeakContext != null) {
            autoSpeakContext.getState().transition(autoSpeakContext, AutoSpeakMessage.GO_TO_PREV);
        } else {
            Ln.i("Call skipToPrev with null autoSpeakContext. Do nothing.");
        }
    }

    public void stopPlaying() {
        Ln.v("Stop playing");
        cancelNotification();
        if (autoSpeakContext != null) {
            autoSpeakContext.getState().transition(autoSpeakContext, AutoSpeakMessage.STOP_PLAYING);
        } else {
            Ln.i("Call stopPlaying with null autoSpeakContext. Do nothing.");
        }
    }

    private void initTTS() {
        try {
            setting = settingDao.queryForId(1);
        } catch (SQLException e) {
            Ln.e(e);
            throw new RuntimeException(e);
        }

        String defaultLocation = AMEnv.DEFAULT_AUDIO_PATH;
        String dbName = FilenameUtils.getName(dbPath);

        if (setting.isQuestionAudioEnabled()) {
            String qa = setting.getQuestionAudio();
            List<String> questionAudioSearchPath = new ArrayList<String>();
            questionAudioSearchPath.add(setting.getQuestionAudioLocation());
            questionAudioSearchPath.add(setting.getQuestionAudioLocation()
                    + "/" + dbName);
            questionAudioSearchPath.add(defaultLocation + "/" + dbName);
            questionAudioSearchPath.add(setting.getQuestionAudioLocation());
            questionTTS = new AnyMemoTTSImpl(this, qa, questionAudioSearchPath);
        } else {
            questionTTS = new NullAnyMemoTTS();
        }

        if (setting.isAnswerAudioEnabled()) {
            String aa = setting.getAnswerAudio();
            List<String> answerAudioSearchPath = new ArrayList<String>();
            answerAudioSearchPath.add(setting.getAnswerAudioLocation());
            answerAudioSearchPath.add(setting.getAnswerAudioLocation() + "/"
                    + dbName);
            answerAudioSearchPath.add(defaultLocation + "/" + dbName);
            answerAudioSearchPath.add(defaultLocation);
            answerTTS = new AnyMemoTTSImpl(this, aa, answerAudioSearchPath);
        }  else {
            answerTTS = new NullAnyMemoTTS();
        }
    }

    private void cleanUp() {
        if (dbOpenHelper != null) {
            AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
        }

        if (questionTTS != null) {
            questionTTS.destory();
        }

        if (answerTTS != null) {
            answerTTS.destory();
        }
        cardDao = null;
        settingDao = null;
    }

    private void showNotification() {

        Intent resultIntent = new Intent(this, PreviewEditActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(PreviewEditActivity.class);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra(PreviewEditActivity.EXTRA_DBPATH, dbPath);
        resultIntent.putExtra(PreviewEditActivity.EXTRA_SHOW_AUTO_SPEAK, true);
        if (autoSpeakContext != null) {
            resultIntent.putExtra(PreviewEditActivity.EXTRA_CARD_ID, autoSpeakContext.getCurrentCard().getId());
        } else {
            Ln.w("The notification for AutoSpeak is shown but the autoSpeakContext is null!");
        }

        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
            stackBuilder.getPendingIntent( 0, PendingIntent.FLAG_UPDATE_CURRENT );

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(getString(R.string.auto_speak_notification_title))
            .setContentText(getString(R.string.auto_speak_notification_text))
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
        public AMTTSService getService() {
            return AMTTSService.this;
        }
    }

}

