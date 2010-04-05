package org.liberty.android.fantastischmemo;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class TTS implements TextToSpeech.OnInitListener{
	private TextToSpeech myTTS;
	
	private Locale myLocale;
	private int errorCode;
	private	boolean init;
	private int version;
    public final static String TAG = "org.liberty.android.fantastischmemo.TTS";
	
	public TTS(Context context, Locale locale){
		myTTS = new TextToSpeech(context, this);
		myLocale = locale;
		init = false;
		
		
	}
	
	public void shutdown(){
		if(init == true){
			myTTS.shutdown();
		}
	}
	
	public void onInit(int status){
        if(myTTS == null){
            Log.e(TAG, "TTS is NULL");
        }
		if(myTTS != null && status == TextToSpeech.SUCCESS){
			int result = myTTS.setLanguage(myLocale);
			if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
				Log.e("TTS engine", "Language is not available");
				errorCode = 1;
			}
		}
		else{
			Log.e("TTS engine", "Cannot init the TextToSpeech");
			errorCode = 2;
		}
		init = true;
		
	}
	
	public boolean isInit(){
		return init;
	}
	
	public int sayText(String s){
		int status;
		// Replace break with period
		String processed_str = s.replaceAll("\\<br\\>", ". " );
		// Remove HTML
		processed_str = processed_str.replaceAll("\\<.*?>", "");
		// Remove () [] 
		processed_str = processed_str.replaceAll("[\\[\\]\\(\\)]", "");
		
		status = myTTS.speak(processed_str, TextToSpeech.QUEUE_FLUSH, null);
		
		return status;
	}
}
	
