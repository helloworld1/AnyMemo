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
import org.liberty.android.fantastischmemo.cardscreen.*;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Bundle;

import android.preference.PreferenceManager;

import android.text.Html;

import android.text.method.LinkMovementMethod;
import android.view.View;

import android.widget.TextView;

/* 
 * This class is invoked when the user share the card from other
 * apps like ColorDict 
 */
public class MiscTab extends AMActivity implements View.OnClickListener{
    private static final String TAG = "org.liberty.android.fantastischmemo.MiscTab";
    private static final String WEBSITE_VERSION="http://anymemo.org/index.php?page=version";
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
    private View importSupermemo2008Button;
    private View exportMnemosyneButton;
    private View exportCSVButton;
    private View exportTabButton;
    private View exportQAButton;

    private View mergeButton;
    private View resetButton;
    private View donateButton;
    private View helpButton;
    private View aboutButton;

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
        importSupermemo2008Button = findViewById(R.id.import_supermemo_2008);
        importSupermemo2008Button.setOnClickListener(this);
        exportMnemosyneButton = findViewById(R.id.export_mnemosyne);
        exportMnemosyneButton.setOnClickListener(this);
        exportCSVButton = findViewById(R.id.export_csv);
        exportCSVButton.setOnClickListener(this);
        exportTabButton = findViewById(R.id.export_tab);
        exportTabButton.setOnClickListener(this);
        exportQAButton = findViewById(R.id.export_qa);
        exportQAButton.setOnClickListener(this);

        mergeButton = findViewById(R.id.misc_merge);
        mergeButton.setOnClickListener(this);
        resetButton = findViewById(R.id.misc_reset);
        resetButton.setOnClickListener(this);

        donateButton = findViewById(R.id.misc_donate);
        donateButton.setOnClickListener(this);
        helpButton = findViewById(R.id.misc_help);
        helpButton.setOnClickListener(this);
        aboutButton = findViewById(R.id.misc_about);
        aboutButton.setOnClickListener(this);
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
        if(v == importSupermemo2008Button) {
            Intent myIntent = new Intent(this, ConvertScreen.class);
            myIntent.putExtra("file_extension", ".xml");
            myIntent.putExtra("converter", Supermemo2008XMLImporter.class);
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
        if(v == mergeButton){
            Intent myIntent = new Intent(this, DatabaseMerger.class);
            startActivity(myIntent);
        }
        if(v == resetButton){
            new AlertDialog.Builder(this)
                .setTitle(R.string.clear_all_pref)
                .setMessage(R.string.reset_all_pref_warning)
                .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1){
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MiscTab.this);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.clear();
                        editor.commit();
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel_text, null)
                .show();
        }
        if(v == donateButton){
            View alertView = View.inflate(this, R.layout.link_alert, null);
            TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
            textView.setText(Html.fromHtml(getString(R.string.donate_summary)));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(this)
                .setView(alertView)
                .setTitle(R.string.donate_text)
                .setPositiveButton(getString(R.string.buy_pro_text), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1){
                        Intent myIntent = new Intent();
                        myIntent.setAction(Intent.ACTION_VIEW);
                        myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                        myIntent.setData(Uri.parse(getString(R.string.anymemo_pro_link)));
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(getString(R.string.cancel_text), null)
                .show();
        }
        if(v == helpButton){
            Intent myIntent = new Intent();
            myIntent.setAction(Intent.ACTION_VIEW);
            myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            myIntent.setData(Uri.parse(WEBSITE_VERSION));
            startActivity(myIntent);
        }
        if(v == aboutButton){
            View alertView = View.inflate(this, R.layout.link_alert, null);
            TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
            textView.setText(Html.fromHtml(getString(R.string.about_text)));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(this)
                .setView(alertView)
                .setTitle(getString(R.string.about_title) + " " + getString(R.string.app_full_name) + " " + getString(R.string.app_version))
                .setPositiveButton(getString(R.string.ok_text), null)
                .setNegativeButton(getString(R.string.about_version), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1){
                        Intent myIntent = new Intent();
                        myIntent.setAction(Intent.ACTION_VIEW);
                        myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                        myIntent.setData(Uri.parse(WEBSITE_VERSION));
                        startActivity(myIntent);
                    }
                })
                .show();
        }

    }

}
