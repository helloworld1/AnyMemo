package org.liberty.android.fantastischmemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.media.MediaPlayer;
import android.util.Log;

public class SpeakWord {
	String mAudioDir;
	MediaPlayer mp;
		
	public SpeakWord(String audioDir){
		mAudioDir = audioDir;
		mp = new MediaPlayer();
	}
	
	public boolean speakWord(String word){
		File audioFile = null;
		String[] fileType = {".ogg", ".wav", ".mp3"};
		String candidateFile =  mAudioDir + "/";
		// Replace break with period
		word = word.replaceAll("\\<br\\>", ". " );
		// Remove HTML
		word = word.replaceAll("\\<.*?>", "");
		// Remove () [] 
		word = word.replaceAll("[\\[\\]\\(\\)]", "");
		// Remove white spaces
        word = word.replaceAll("^\\s+", "");
        word = word.replaceAll("\\s+$", "");
		
		for(String s : fileType){
			audioFile = new File(candidateFile + word + s);
			
			if(audioFile.exists()){
				candidateFile = candidateFile + word + s;
				break;
			}
		}
		if(audioFile == null || !audioFile.exists()){
			for(String s : fileType){
				audioFile = new File(candidateFile + word.substring(0, 1) + "/" + word + s);
				if(audioFile.exists()){
					candidateFile += word.substring(0, 1) + "/" + word + s;
					break;
				}
			}
		}
		if(audioFile == null || !audioFile.exists()){
			return false;
		}
		
		try{
			FileInputStream fis = new FileInputStream(audioFile);
			mp.setDataSource(fis.getFD());
			mp.prepare();
			mp.start();
			while(mp.isPlaying()){
			}
			mp.reset();
		}
		catch(IOException e){
			Log.e("SpeakWord Error", e.toString());
			return false;
		}
		return true;
	}

}
