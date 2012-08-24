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
package org.liberty.android.fantastischmemo.ui;

import org.liberty.android.fantastischmemo.*;

import android.widget.Button;
import android.view.View;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.view.LayoutInflater;

class EditScreenButtons extends ControlButtons{
    private Context mContext;
    private View buttonView;
    private Button btnNew, btnEdit, btnPrev, btnNext;
    private Map<String, Button> buttonMap;
    public EditScreenButtons(Context context){
        mContext = context;
        LayoutInflater factory = LayoutInflater.from(mContext);
        buttonView = factory.inflate(R.layout.edit_screen_buttons, null);
        btnNew = (Button)buttonView.findViewById(R.id.edit_screen_btn_new);
        btnEdit = (Button)buttonView.findViewById(R.id.edit_screen_btn_edit);
        btnPrev = (Button)buttonView.findViewById(R.id.edit_screen_btn_prev);
        btnNext = (Button)buttonView.findViewById(R.id.edit_screen_btn_next);
        buttonMap = new HashMap<String, Button>();
        buttonMap.put("new", btnNew);
        buttonMap.put("edit", btnEdit);
        buttonMap.put("prev", btnPrev);
        buttonMap.put("next", btnNext);
    }

    public Map<String, Button> getButtons(){
        return buttonMap;
    }
    public View getView(){
        return buttonView;
    }
}
