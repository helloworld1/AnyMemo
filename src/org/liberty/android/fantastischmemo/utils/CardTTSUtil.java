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
package org.liberty.android.fantastischmemo.utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.aspect.CheckNullArgs;
import org.liberty.android.fantastischmemo.aspect.LogInvocation;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTSImpl;
import org.liberty.android.fantastischmemo.tts.NullAnyMemoTTS;

import roboguice.util.Ln;

import android.content.Context;

import com.google.inject.assistedinject.Assisted;

/*
 * Utility for TTS of a card
 */
public class CardTTSUtil {

    private String dbPath;

    private AnyMemoDBOpenHelper dbOpenHelper;

    private SettingDao settingDao;

    private AnyMemoTTS questionTTS;

    private AnyMemoTTS answerTTS;

    private Setting setting;

    private Context context;

    @Inject
    public CardTTSUtil(Context context, @Assisted String dbPath) {
        this.context = context;

        this.dbPath = dbPath;
        
        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(context, dbPath);

        settingDao = dbOpenHelper.getSettingDao();
        initTTS();
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

    /*
     * Release the TTSUtil. This must be called explicitly.
     */
    @LogInvocation
    public void release() {
        if (dbOpenHelper != null) {
            AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
        }

        if (questionTTS != null) {
            questionTTS.destory();
            questionTTS = null;
        }

        if (answerTTS != null) {
            answerTTS.destory();
            answerTTS = null;
        }
        settingDao = null;
    }

    /*
     * Override the finalize() function to check if the release() is called
     * and release it if not.
     */
    @Override
    public void finalize() throws Throwable {
        try {
            if (questionTTS != null || answerTTS != null) {
                Ln.w("release() must be called explicitly to clean up CardTTSUtil.");
                release();
            }
        } finally {
            super.finalize();
        }
    }

    private void initTTS() {
        setting = settingDao.queryForId(1);

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
            questionTTS = new AnyMemoTTSImpl(context, qa, questionAudioSearchPath);
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
            answerTTS = new AnyMemoTTSImpl(context, aa, answerAudioSearchPath);
        }  else {
            answerTTS = new NullAnyMemoTTS();
        }
    }

}
