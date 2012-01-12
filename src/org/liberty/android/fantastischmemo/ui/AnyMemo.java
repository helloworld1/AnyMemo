/*
Copyright (C) 2012 Haowen Ning

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

import org.liberty.android.fantastischmemo.ui.CardEditor;

import android.content.Intent;
import android.os.Bundle;

public class AnyMemo extends AMActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent myIntent = new Intent(this, CardEditor.class);
        //myIntent.putExtra("dbpath", "/sdcard/french-body-parts.db");
        myIntent.putExtra("dbpath", "/sdcard/gre01.db");
        myIntent.putExtra("id", 1);
        startActivity(myIntent);
    }
}
