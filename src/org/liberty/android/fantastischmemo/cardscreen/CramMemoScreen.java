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
package org.liberty.android.fantastischmemo.cardscreen;

import org.liberty.android.fantastischmemo.*;
import org.liberty.android.fantastischmemo.tts.*;

import java.util.Map;
import android.widget.Button;
import android.util.Log;

public class CramMemoScreen extends MemoScreen{
    private static final String TAG = "org.liberty.android.fantastischmemo.CramMemoScreen";
    @Override
    void createQueue(){
        queueManager =new CramQueueManager.Builder(this, dbPath, dbName)
            .setFilter(activeFilter)
            .setQueueSize(settingManager.getLearningQueueSize())
            .setShuffle(settingManager.getShufflingCards())
            .build();
    }

    @Override
    void setGradeButtonTitle(){
        Map<String, Button> hm = controlButtons.getButtons();
        /* Cram Review has different button titles 
         * because of the incorrect estimated day caluclation */
        for(int i = 0; i < 6; i++){
            Button b = hm.get(Integer.valueOf(i).toString());
            b.setText(Integer.valueOf(i).toString());
        }
    }

    @Override
    String getActivityTitleString(){
        return getString(R.string.learn_ahead);
    }

}


