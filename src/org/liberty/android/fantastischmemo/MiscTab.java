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

import org.liberty.android.fantastischmemo.converter.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/* 
 * This class is invoked when the user share the card from other
 * apps like ColorDict 
 */
public class MiscTab extends AMActivity implements View.OnClickListener{
    private static final String TAG = "org.liberty.android.fantastischmemo.MiscTab";
    private View optionButton;
    private View importButton;
    private View exportButton;
    private View importItems;
    private View exportItems;
    private View importMnemosyneButton;
    private View importSupermemoButton;
    private View importCSVButton;
    private View importTabButton;
    private View importQAButton;
    private View exportMnemosyneButton;
    private View exportCSVButton;
    private View exportTabButton;
    private View exportQAButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.misc_tab);
        optionButton = findViewById(R.id.misc_options);
        optionButton.setOnClickListener(this);
        importButton = findViewById(R.id.misc_import);
        importButton.setOnClickListener(this);
        exportButton = findViewById(R.id.misc_export);
        exportButton.setOnClickListener(this);
        importItems = findViewById(R.id.import_items);
        exportItems = findViewById(R.id.export_items);
        importMnemosyneButton = findViewById(R.id.import_mnemosyne);
        importMnemosyneButton.setOnClickListener(this);
        importSupermemoButton = findViewById(R.id.import_supermemo);
        importSupermemoButton.setOnClickListener(this);
        importCSVButton = findViewById(R.id.import_csv);
        importCSVButton.setOnClickListener(this);
        importTabButton = findViewById(R.id.import_tab);
        importTabButton.setOnClickListener(this);
        importQAButton = findViewById(R.id.import_qa);
        importQAButton.setOnClickListener(this);
        exportMnemosyneButton = findViewById(R.id.export_mnemosyne);
        exportMnemosyneButton.setOnClickListener(this);
        exportCSVButton = findViewById(R.id.export_csv);
        exportCSVButton.setOnClickListener(this);
        exportTabButton = findViewById(R.id.export_tab);
        exportTabButton.setOnClickListener(this);
        exportQAButton = findViewById(R.id.export_qa);
        exportQAButton.setOnClickListener(this);
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

        if(v == exportButton){
            if(exportItems.getVisibility() == View.GONE){
                exportItems.setVisibility(View.VISIBLE);
            }
            else{
                exportItems.setVisibility(View.GONE);
            }
        }

        if(v == importMnemosyneButton){
            Intent myIntent = new Intent(this, ConvertScreen.class);
            myIntent.putExtra("file_extension", ".xml");
            myIntent.putExtra("converter", MnemosyneXMLImporter.class);
            startActivity(myIntent);
        }
        if(v == importSupermemoButton){
            Intent myIntent = new Intent(this, ConvertScreen.class);
            myIntent.putExtra("file_extension", ".xml");
            myIntent.putExtra("converter", SupermemoXMLImporter.class);
            startActivity(myIntent);
        }
        if(v == importCSVButton){
            Intent myIntent = new Intent(this, ConvertScreen.class);
            myIntent.putExtra("file_extension", ".csv");
            myIntent.putExtra("converter", CSVImporter.class);
            startActivity(myIntent);
        }
        if(v == importTabButton){
            Intent myIntent = new Intent(this, ConvertScreen.class);
            myIntent.putExtra("file_extension", ".txt");
            myIntent.putExtra("converter", TabTxtImporter.class);
            startActivity(myIntent);
        }
        if(v == importQAButton){
            Intent myIntent = new Intent(this, ConvertScreen.class);
            myIntent.putExtra("file_extension", ".txt");
            myIntent.putExtra("converter", QATxtImporter.class);
            startActivity(myIntent);
        }
        if(v == exportMnemosyneButton){
            Intent myIntent = new Intent(this, ConvertScreen.class);
            myIntent.putExtra("file_extension", ".db");
            myIntent.putExtra("converter", MnemosyneXMLExporter.class);
            startActivity(myIntent);
        }
        if(v == exportCSVButton){
            Intent myIntent = new Intent(this, ConvertScreen.class);
            myIntent.putExtra("file_extension", ".db");
            myIntent.putExtra("converter", CSVExporter.class);
            startActivity(myIntent);
        }
        if(v == exportTabButton){
            Intent myIntent = new Intent(this, ConvertScreen.class);
            myIntent.putExtra("file_extension", ".db");
            myIntent.putExtra("converter", TabTxtExporter.class);
            startActivity(myIntent);
        }
        if(v == exportQAButton){
            Intent myIntent = new Intent(this, ConvertScreen.class);
            myIntent.putExtra("file_extension", ".db");
            myIntent.putExtra("converter", QATxtExporter.class);
            startActivity(myIntent);
        }

    }

}
