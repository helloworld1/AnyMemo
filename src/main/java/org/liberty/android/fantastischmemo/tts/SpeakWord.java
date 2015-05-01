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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

public class SpeakWord {
    private volatile MediaPlayer mp;
    private final List<String> searchPath;
    private final String[] SUPPORTED_AUDIO_FILE_TYPE = {".3gp", ".ogg", ".wav", ".mp3", ".amr"};
    private final String TAG = "SpeakWord";
    private OnCompletedListener mOnCompletedListener = new OnCompletedListener() {
        @Override
        public void onCompleted() {
            Log.i(TAG, "SpeakWord on completed");
        }
    };

    /* Search in all given search path and try to find exact match audio file with name specified in the text*/
    private File searchGivenPath(String cardText){
        // The regex here should match the file types in SUPPORTED_AUDIO_FILE_TYPE
        Pattern p = Pattern.compile("[A-Za-z0-9_-]+\\.(3gp|ogg|mp3|wav|amr)");
        Matcher m = p.matcher(cardText);
        File audioFile = null;
        if(m.find()){
            String audioTag = m.group();
            for(String sp : searchPath){
                audioFile = new File(sp + "/" + audioTag);
                if(audioFile.exists()){
                    break;
                }
            }
        }
        return audioFile;
    }

    /* Search alternative filenames and subpaths */
    private File searchAlternatives(String cardText){
        File audioFile = null;
        String alternativeFilename = stripNonCardContent(cardText);
        if(alternativeFilename.length() < 1){
            return audioFile;
        }

        audioFile = searchAltFilename(alternativeFilename);
        if(isAudioFileValid(audioFile)){
            return audioFile;
        }

        audioFile = searchAltSubPath(alternativeFilename);

        return audioFile;
    }

    private File searchAltFilename(String alternativeFilename){
        File audioFile = null;
        for(String sp : searchPath){
            for(String ft : SUPPORTED_AUDIO_FILE_TYPE){
                audioFile = new File(sp + "/" + alternativeFilename + ft);
                if(audioFile.exists()){
                    break;
                }
            }
        }
        return audioFile;
    }

    private File searchAltSubPath(String filename){
        File audioFile = null;
        for(String sp :searchPath){
            for(String ft : SUPPORTED_AUDIO_FILE_TYPE){
                audioFile = new File(sp + "/" +filename.substring(0, 1) + "/" + filename + ft);
                if(audioFile.exists()){
                    break;
                }
            }
        }
        return audioFile;

    }

    private String stripNonCardContent(String text){
        // Replace break with period
        text = text.replaceAll("\\<br\\>", ". " );
        // Remove HTML
        text = text.replaceAll("\\<.*?>", "");
        // Remove () []
        text = text.replaceAll("[\\[\\]\\(\\)]", "");
        // Remove white spaces
        text = text.replaceAll("^\\s+", "");
        text = text.replaceAll("\\s+$", "");
        return text;
    }

    private boolean isAudioFileValid(File audioFile){
        return audioFile!=null && audioFile.exists();
    }

    public SpeakWord(List<String> audioSearchPath){
        mp = new MediaPlayer();
        searchPath = audioSearchPath;
    }


    public boolean speakWord(String text){
        File audioFile = searchGivenPath(text);

        if(!isAudioFileValid(audioFile)){
            audioFile = searchAlternatives(text);
        }

        if(!isAudioFileValid(audioFile)){
            return false;
        }

        mp.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mOnCompletedListener.onCompleted();
            }
        });

        final File finalAudioFile = audioFile;

        try{
            new Thread(){
                public void run(){
                    try{
                        if(!mp.isPlaying()){
                            FileInputStream fis = null;
                            try {
                                fis = new FileInputStream(finalAudioFile);
                                mp.reset();
                                mp.setDataSource(fis.getFD());
                                mp.prepare();
                                mp.start();
                            } finally {
                                if (fis != null) {
                                    fis.close();
                                }
                            }
                        }
                    } catch(Exception e){
                        Log.e(TAG, "Error loading audio. Maybe it is race condition", e);
                    }
                }
            }.start();
        } catch(Exception e){
            Log.e(TAG, "Speak error", e);
            return false;
        }        return true;
    }

    public void stop(){
        if(mp != null){
            try{
                if (mp.isPlaying()) {
                    mOnCompletedListener.onCompleted();
                }
                mp.reset();
            }
            catch(Exception e){
                Log.e(TAG, "Error shutting down: ", e);
            }
        }
    }

    public void destory(){
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

    public void setOnCompletedListener(OnCompletedListener ocl) {
        this.mOnCompletedListener = ocl;
    }

    public interface OnCompletedListener {
        public void onCompleted();
    }


}
