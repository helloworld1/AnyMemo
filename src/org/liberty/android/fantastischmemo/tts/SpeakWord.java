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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.media.MediaPlayer;
import android.util.Log;

public class SpeakWord {
	String mAudioDir;
	volatile MediaPlayer mp;
    String dbName;
    private final String TAG = "org.liberty.android.fantastischmemo.SpeakWord";
		
	public SpeakWord(String audioDir, String dbname){
		mAudioDir = audioDir;
		mp = new MediaPlayer();
        dbName = dbname;
	}
	
	public boolean speakWord(final String text){
		File audioFile = null;
		String[] fileType = {".ogg", ".wav", ".mp3"};
		String candidateFile =  mAudioDir + "/";
        String word = text;
        /* Find the audio file in tags */
        Pattern p = Pattern.compile("[A-Za-z0-9_-]+\\.(ogg|mp3|wav)");
        Matcher m = p.matcher(text);
        if(m.find()){
           String audioTag = m.group();
           audioFile = new File(mAudioDir + "/" + dbName + "/" + audioTag);
           if(!audioFile.exists()){
               audioFile = new File(mAudioDir + "/" + audioTag);
           }
        }

        if(audioFile == null || ! audioFile.exists()){
            // Replace break with period
            word = word.replaceAll("\\<br\\>", ". " );
            // Remove HTML
            word = word.replaceAll("\\<.*?>", "");
            // Remove () [] 
            word = word.replaceAll("[\\[\\]\\(\\)]", "");
            // Remove white spaces
            word = word.replaceAll("^\\s+", "");
            word = word.replaceAll("\\s+$", "");
            if(word.length() < 1){
                return false;
            }
        }

        

		
        if(audioFile == null || !audioFile.exists()){
            for(String s : fileType){
                audioFile = new File(candidateFile + word + s);
                
                if(audioFile.exists()){
                    candidateFile = candidateFile + word + s;
                    break;
                }
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
			final FileInputStream fis = new FileInputStream(audioFile);
            new Thread(){
                public void run(){
                    try{
                        if(!mp.isPlaying()){
                            mp.reset();
                            mp.setDataSource(fis.getFD());
                            mp.prepare();
                            mp.start();
                        }
                        else{
                            stop();
                        }
                        
                    }

                    catch(Exception e){
                        Log.e(TAG, "Error loading audio. Maybe it is race condition", e);
                    }
                    
                }
            }.start();
		}
		catch(Exception e){
			Log.e(TAG, "Speak error", e);
			return false;
		}
		return true;
	}

    public void stop(){
        if(mp != null){
            try{
                mp.reset();
            }
            catch(Exception e){
                Log.e(TAG, "Error shutting down: ", e);
            }
        }
    }

    public void shutdown(){
        if(mp != null){
            try{
                mp.reset();
                mp.release();
            }
            catch(Exception e){
                Log.e(TAG, "Error shutting down: ", e);
            }
        }
    }

}
