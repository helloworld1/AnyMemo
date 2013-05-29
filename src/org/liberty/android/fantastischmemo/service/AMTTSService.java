package org.liberty.android.fantastischmemo.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.mycommons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.aspect.CheckNullArgs;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakContext;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakEventHandler;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakMessage;
import org.liberty.android.fantastischmemo.service.autospeak.AutoSpeakState;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTSImpl;

import roboguice.service.RoboService;
import roboguice.util.Ln;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

public class AMTTSService extends RoboService {

    public static final String EXTRA_DBPATH = "dbpath";

    // This is the object that receives interactions from clients.
    private final IBinder binder = new LocalBinder();

    private String dbPath;

    private AnyMemoDBOpenHelper dbOpenHelper;

    private CardDao cardDao;

    private SettingDao settingDao;

    private AnyMemoTTS questionTTS;

    private AnyMemoTTS answerTTS;

    private Setting setting;

    private Card currentPlayingCard;

    private Handler handler;

    @Override
    public IBinder onBind(Intent intent) {
        Ln.v("Bind to AMTTSService using intent: " + intent);
        handler = new Handler();
        Bundle extras = intent.getExtras();

        assert extras != null : "dbpath is not passed to AMTTSService.";

        dbPath = extras.getString(EXTRA_DBPATH);

        // Clean up first in case the service is started multiple times;
        cleanUp();
        
        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(this, dbPath);

        cardDao = dbOpenHelper.getCardDao();
        settingDao = dbOpenHelper.getSettingDao();

        initTTS();

        return binder;
    }

    @Override
    public void onCreate() {
        // mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // // Display a notification about us starting.  We put an icon in the status bar.
        // showNotification();
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
        return false;
    }

    @CheckNullArgs
    public void speakCardQuestion(Card card) {
        speakCardQuestion(card, null);
    }

    @CheckNullArgs
    public void speakCardQuestion(Card card, AnyMemoTTS.OnTextToSpeechCompletedListener onTextToSpeechCompletedListener) {
        stopSpeak();
        questionTTS.sayText(card.getQuestion(), onTextToSpeechCompletedListener);
    }

    @CheckNullArgs
    public void speakCardAnswer(Card card) {
        speakCardAnswer(card, null);
    }

    @CheckNullArgs
    public void speakCardAnswer(Card card, AnyMemoTTS.OnTextToSpeechCompletedListener onTextToSpeechCompletedListener) {
        stopSpeak();
        answerTTS.sayText(card.getAnswer(), onTextToSpeechCompletedListener);
    }

    public void stopSpeak() {
        questionTTS.stop();
        answerTTS.stop();
    }

    private volatile AutoSpeakContext autoSpeakContext = null;

    @CheckNullArgs
    public void startPlaying(Card startCard, AutoSpeakEventHandler eventHandler) {
        if (autoSpeakContext == null) {
            autoSpeakContext = new AutoSpeakContext(
                eventHandler,
                this,
                handler,
                dbOpenHelper);
        }
        autoSpeakContext.setCurrentCard(startCard);
        autoSpeakContext.setState(AutoSpeakState.STOPPED);
        autoSpeakContext.getState().transition(autoSpeakContext, AutoSpeakMessage.START_PLAYING);
    }

    public void skipToNext() {
        autoSpeakContext.getState().transition(autoSpeakContext, AutoSpeakMessage.GO_TO_NEXT);
    }

    public void skipToPrev() {
        autoSpeakContext.getState().transition(autoSpeakContext, AutoSpeakMessage.GO_TO_PREV);
    }

    public void stopPlaying() {
        Ln.v("Stop playing");
        autoSpeakContext.getState().transition(autoSpeakContext, AutoSpeakMessage.STOP_PLAYING);
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


    public class LocalBinder extends Binder {
        public AMTTSService getService() {
            return AMTTSService.this;
        }
    }

}

