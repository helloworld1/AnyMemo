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

import org.liberty.android.fantastischmemo.*;

import java.util.Locale;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;

public class AudioFileTTS implements AnyMemoTTS{
    private String audioDir;
    private String dbName;
    private SpeakWord speakWord;
    

    public AudioFileTTS(String audiodir, String dbname){
        audioDir = audiodir;
        dbName = dbname;
        speakWord = new SpeakWord(audiodir, dbname);
    }

    public int sayText(String text){
        speakWord.speakWord(text);
        return 0;
    }
    public void stop(){
        speakWord.stop();
    }

    public void shutdown(){
        speakWord.shutdown();
    }
}


