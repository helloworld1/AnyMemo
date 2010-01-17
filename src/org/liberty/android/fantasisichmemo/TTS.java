package org.liberty.android.fantasisichmemo;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class TTS implements TextToSpeech.OnInitListener{
	private TextToSpeech myTTS;
	
	private Locale myLocale;
	private int errorCode;
	
	public TTS(Context context, Locale locale){
		myTTS = new TextToSpeech(context, this);
		myLocale = locale;
		errorCode = 0;
		
	}
	
	public void shutdown(){
		myTTS.shutdown();
	}
	
	public void onInit(int status){
		if(status == TextToSpeech.SUCCESS){
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
		
	}
	
	public int sayText(String s){
		if(this.errorCode == 0){
			
			int status = myTTS.speak(s, TextToSpeech.QUEUE_FLUSH, null);
			return status;
		}
		else{
			return this.errorCode;
		}
		
	}
}
	
