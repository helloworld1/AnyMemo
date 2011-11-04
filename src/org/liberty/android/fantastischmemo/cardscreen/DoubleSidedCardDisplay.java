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

import android.content.Context;
import android.view.View;

public class DoubleSidedCardDisplay extends SingleSidedCardDisplay{
    private final static String TAG = "org.liberty.android.fantastischmemo.cardscreen.SingleSidedCardScreen";

    public DoubleSidedCardDisplay(Context context){
        super(context, new SettingManager(context));
    }

    public DoubleSidedCardDisplay(Context context, SettingManager manager){
        super(context, manager);
    }


	public void updateView(Item item, boolean showAnswer) {
        super.updateView(item, showAnswer);
        /* Only show the visible part */
        if(showAnswer){
            questionLayout.setVisibility(View.GONE);
            answerLayout.setVisibility(View.VISIBLE);
        }
        else{
            questionLayout.setVisibility(View.VISIBLE);
            answerLayout.setVisibility(View.GONE);
        }
    }

    void setQARatio(float qRatio){
        /* Do nothing because we don't need it */
    }
}


