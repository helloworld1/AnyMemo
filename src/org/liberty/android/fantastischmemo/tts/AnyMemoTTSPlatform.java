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

package org.liberty.android.fantastischmemo.tts;

import java.util.Locale;

import java.util.concurrent.TimeUnit;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.mycommons.lang3.StringUtils;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class AnyMemoTTSPlatform implements AnyMemoTTS, TextToSpeech.OnInitListener{

	private final TextToSpeech myTTS;
	
	private final Locale myLocale;
    
    private ReentrantLock initLock = new ReentrantLock();

    /* TTS Init lock's timeout in seconds. */
    private static long INIT_LOCK_TIMEOUT = 10L;

    public final static String TAG = "org.liberty.android.fantastischmemo.tts.AnyMemoTTSPlatform";

    public void onInit(int status){
        try {
            if (initLock.tryLock() || initLock.tryLock(INIT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                initLock.unlock();
            } else {
                Log.e(TAG, "TTS init timed out");
                return;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "TTS init lock waiting is interrupted.");
        }

        if (status == TextToSpeech.SUCCESS) {
            Log.v(TAG, "init!" + myLocale.toString());
            assert myTTS != null;
            assert myLocale != null;
            int result = myTTS.setLanguage(myLocale);
            if (result == TextToSpeech.LANG_MISSING_DATA) {
                Log.e(TAG, "Missing language data");
            }
            if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language is not supported");
            }
        } else {
            Log.e(TAG, "Can't initialize");
        }
    }
	
	public AnyMemoTTSPlatform(Context context, String locale){
        // We must make sure the constructor happens before
        // the onInit callback. Unfortunately, this is not
        // always true. We have to use lock to ensure the happen before.
        // Or a null pointer for myTTS is waiting
        initLock.lock();
		myLocale = getLocaleForTTS(locale);
		myTTS = new TextToSpeech(context, this);
        initLock.unlock();
	}
	
	public void shutdown(){
        myTTS.shutdown();
	}

    public void stop(){
        myTTS.stop();
        // We wait until the tts is not speaking.
        // This is because top is asynchronized call
        while (myTTS.isSpeaking()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
    }
	
	public int sayText(String s){
        Log.v(TAG, "say it!");
		// Replace break with period
		String processed_str = s.replaceAll("\\<br\\>", ". " );
		// Remove HTML
		processed_str = processed_str.replaceAll("\\<.*?>", "");
		// Remove () [] and their content
		processed_str = processed_str.replaceAll("\\[.*?\\]", "");
        // Remove the XML special character
		processed_str = processed_str.replaceAll("\\[.*?\\]", "");
		processed_str = processed_str.replaceAll("&.*?;", "");

        if(!myTTS.isSpeaking()){
            myTTS.speak(processed_str, 0, null);
        }
        else{
            stop();
        }
		
		return 0;
	}

    private Locale getLocaleForTTS(String loc) {
        if (StringUtils.isEmpty(loc)) {
            return Locale.US;
        }

        if (loc.toLowerCase().equals("us")) {
            return Locale.US;
        }

        if (loc.toLowerCase().equals("uk")) {
            return Locale.UK;
        }

        return new Locale(loc);

    }

}
