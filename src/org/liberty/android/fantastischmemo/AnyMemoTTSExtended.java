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

package org.liberty.android.fantastischmemo;

import java.util.Locale;

import android.content.Context;

import com.google.tts.TextToSpeechBeta;

public class AnyMemoTTSExtended implements AnyMemoTTS, TextToSpeechBeta.OnInitListener{
	private TextToSpeechBeta myTTS;
	
	private Locale myLocale;
	private int errorCode;
	private int version;
    public final static String TAG = "org.liberty.android.fantastischmemo.TTS";

    public void onInit(int status, int version){
    }
	
	public AnyMemoTTSExtended(Context context, Locale locale){
		myTTS = new TextToSpeechBeta(context, this);
		myLocale = locale;
	}

	
	public void shutdown(){
			myTTS.shutdown();
	}
	
	public int sayText(String s){
		int status;
		// Replace break with period
		String processed_str = s.replaceAll("\\<br\\>", ". " );
		// Remove HTML
		processed_str = processed_str.replaceAll("\\<.*?>", "");
		// Remove () [] and their content
		processed_str = processed_str.replaceAll("\\[.*?\\]", "");
        // Remove the XML special character
		processed_str = processed_str.replaceAll("\\[.*?\\]", "");
		processed_str = processed_str.replaceAll("&.*?;", "");

        myTTS.setLanguage(myLocale);
        myTTS.speak(s, 0, null);
		
		return 0;
	}

}
