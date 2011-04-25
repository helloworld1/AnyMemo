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
import org.liberty.android.fantastischmemo.converter.*;
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
public class MiscTab extends AMActivity implements View.OnClickListener{
    private static final String TAG = "org.liberty.android.fantastischmemo.MiscTab";
    private View optionButton;
    private View importButton;
    private View importItems;
    private View importMnemosyneButton;
    private final int ACTIVITY_FB = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.misc_tab);
        optionButton = findViewById(R.id.misc_options);
        optionButton.setOnClickListener(this);
        importButton = findViewById(R.id.misc_import);
        importButton.setOnClickListener(this);
        importItems = findViewById(R.id.import_items);
        importMnemosyneButton = findViewById(R.id.import_mnemosyne);
        importMnemosyneButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        if(v == optionButton){
            startActivity(new Intent(this, OptionScreen.class));
        }
        /* Toggle visibility for import and export buttons */
        if(v == importButton){
            if(importItems.getVisibility() == View.GONE){
                importItems.setVisibility(View.VISIBLE);
            }
            else{
                importItems.setVisibility(View.GONE);
            }
        }
        if(v == importMnemosyneButton){
            Intent myIntent = new Intent(this, ConvertScreen.class);
            myIntent.putExtra("file_extension", ".xml");
            myIntent.putExtra("converter", MnemosyneXMLImporter.class);
            startActivity(myIntent);
        }
    }

}
