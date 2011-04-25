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
package org.liberty.android.fantastischmemo;

import org.liberty.android.fantastischmemo.cardscreen.*;
import org.liberty.android.fantastischmemo.downloader.*;
import android.app.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.util.Log;

/* 
 * This class is invoked when the user share the card from other
 * apps like ColorDict 
 */
public class DownloaderTab extends Activity implements View.OnClickListener{
    private static final String TAG = "org.liberty.android.fantastischmemo.DownloaderTab";
    private View amButton;
    private View feButton;
    private View ssButton;
    private final int ACTIVITY_FB = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloader_tab);
        amButton = findViewById(R.id.source_anymemo);
        feButton = findViewById(R.id.source_fe);
        ssButton = findViewById(R.id.source_ss);
        amButton.setOnClickListener(this);
        feButton.setOnClickListener(this);
        ssButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        if(v == amButton){
            startActivity(new Intent(this, DownloaderAnyMemo.class));
        }
        if(v == feButton){
            startActivity(new Intent(this, FELauncher.class));
        }
        if(v == ssButton){
            startActivity(new Intent(this, DownloaderSS.class));

        }
    }

}
