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
import java.util.Map;

abstract class ControlButtons{
    public abstract Map<String, Button> getButtons();
    public abstract View getView();
    public String[] getButtonNames(){
        Map<String, Button> map = getButtons();
        String[] res = new String[map.size()];
        int i = 0;
        for(String s : map.keySet()){
            res[i++] = s; 
        }
        return res;

    }
    public void setBackgroundColor(int color){
        View v = getView();
        v.setBackgroundColor(color);
    }
}
