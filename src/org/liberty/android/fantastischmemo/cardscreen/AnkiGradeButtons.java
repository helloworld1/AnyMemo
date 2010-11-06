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

import android.widget.Button;
import android.view.View;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.view.LayoutInflater;

public class AnkiGradeButtons implements ControlButtons{
    private Context mContext;
    private View buttonView;
    private Button grade0, grade1, grade2, grade3, grade4, grade5;
    private HashMap<String, Button> hm;
    public AnkiGradeButtons(Context context){
        mContext = context;
        LayoutInflater factory = LayoutInflater.from(mContext);
        buttonView = factory.inflate(R.layout.grade_buttons_anki, null);
        grade0 = (Button)buttonView.findViewById(R.id.grade_btn_anki_0);
        grade1 = (Button)buttonView.findViewById(R.id.grade_btn_anki_1);
        grade2 = (Button)buttonView.findViewById(R.id.grade_btn_anki_2);
        grade3 = (Button)buttonView.findViewById(R.id.grade_btn_anki_3);
        grade4 = (Button)buttonView.findViewById(R.id.grade_btn_anki_4);
        grade5 = (Button)buttonView.findViewById(R.id.grade_btn_anki_5);
        HashMap<String, Button> hm = new HashMap<String, Button>();
        hm.put("0", grade0);
        hm.put("1", grade1);
        hm.put("2", grade2);
        hm.put("3", grade3);
        hm.put("4", grade4);
        hm.put("5", grade5);
    }
    public Map<String, Button> getButtons(){
        return hm;
    }
    public View getView(){
        return buttonView;
    }
    public String[] getButtonNames(){
        return new String[]{"0", "1", "2", "3", "4", "5"};
    }
}

