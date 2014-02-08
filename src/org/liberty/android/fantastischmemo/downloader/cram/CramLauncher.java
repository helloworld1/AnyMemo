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
package org.liberty.android.fantastischmemo.downloader.cram;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.downloader.FEDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CramLauncher extends AMActivity implements OnClickListener{
    private Button directoryButton;
    private Button searchTagButton;
    private Button searchUserButton;

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cram_launcher);
        directoryButton = (Button)findViewById(R.id.fe_directory);
        searchTagButton = (Button)findViewById(R.id.fe_search_tag);
        searchUserButton = (Button)findViewById(R.id.fe_search_user);
        directoryButton.setOnClickListener(this);
        searchTagButton.setOnClickListener(this);
        searchUserButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        if(v == directoryButton){
            Intent myIntent = new Intent(this, FEDirectory.class);
            startActivity(myIntent);
        }
        if(v == searchTagButton){
            Intent intent = new Intent(this, CramSearchPublicCardSetActivity.class);
            startActivity(intent);
        }
        if(v == searchUserButton){
            Intent intent = new Intent(this, CramPublicUserCardSetActivity.class);
            startActivity(intent);
        }
    }
}

