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
package org.liberty.android.fantastischmemo.ui;

import org.liberty.android.fantastischmemo.ui.OptionScreen;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.converter.CSVExporter;
import org.liberty.android.fantastischmemo.converter.CSVImporter;
import org.liberty.android.fantastischmemo.converter.MnemosyneXMLExporter;
import org.liberty.android.fantastischmemo.converter.MnemosyneXMLImporter;
import org.liberty.android.fantastischmemo.converter.QATxtExporter;
import org.liberty.android.fantastischmemo.converter.QATxtImporter;
import org.liberty.android.fantastischmemo.converter.Supermemo2008XMLImporter;
import org.liberty.android.fantastischmemo.converter.SupermemoXMLImporter;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Bundle;

import android.preference.PreferenceManager;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.text.Html;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

public class MiscTabFragment extends Fragment implements View.OnClickListener {
    private static final String WEBSITE_VERSION="http://anymemo.org/index.php?page=version";
    private Activity mActivity;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.misc_tab, container, false);
        optionButton = v.findViewById(R.id.misc_options);
        optionButton.setOnClickListener(this);
        importButton = v.findViewById(R.id.misc_import);
        importButton.setOnClickListener(this);
        exportButton = v.findViewById(R.id.misc_export);
        exportButton.setOnClickListener(this);
        importItems = v.findViewById(R.id.import_items);
        exportItems = v.findViewById(R.id.export_items);
        importMnemosyneButton = v.findViewById(R.id.import_mnemosyne);
        importMnemosyneButton.setOnClickListener(this);
        importSupermemoButton = v.findViewById(R.id.import_supermemo);
        importSupermemoButton.setOnClickListener(this);
        importCSVButton = v.findViewById(R.id.import_csv);
        importCSVButton.setOnClickListener(this);
        importTabButton = v.findViewById(R.id.import_tab);
        importTabButton.setOnClickListener(this);
        importQAButton = v.findViewById(R.id.import_qa);
        importQAButton.setOnClickListener(this);
        importSupermemo2008Button = v.findViewById(R.id.import_supermemo_2008);
        importSupermemo2008Button.setOnClickListener(this);
        exportMnemosyneButton = v.findViewById(R.id.export_mnemosyne);
        exportMnemosyneButton.setOnClickListener(this);
        exportCSVButton = v.findViewById(R.id.export_csv);
        exportCSVButton.setOnClickListener(this);
        exportTabButton = v.findViewById(R.id.export_tab);
        exportTabButton.setOnClickListener(this);
        exportQAButton = v.findViewById(R.id.export_qa);
        exportQAButton.setOnClickListener(this);

        mergeButton = v.findViewById(R.id.misc_merge);
        mergeButton.setOnClickListener(this);
        resetButton = v.findViewById(R.id.misc_reset);
        resetButton.setOnClickListener(this);

        donateButton = v.findViewById(R.id.misc_donate);
        donateButton.setOnClickListener(this);
        helpButton = v.findViewById(R.id.misc_help);
        helpButton.setOnClickListener(this);
        aboutButton = v.findViewById(R.id.misc_about);
        aboutButton.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v){
        if(v == optionButton){
            startActivity(new Intent(mActivity, OptionScreen.class));
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

        if(v == exportButton) {
            if (exportItems.getVisibility() == View.GONE) {
                exportItems.setVisibility(View.VISIBLE);
            } else {
                exportItems.setVisibility(View.GONE);
            }
        }

        if(v == importMnemosyneButton){
            DialogFragment df = new ConverterFragment(new MnemosyneXMLImporter(mActivity), ".db");
            Bundle b = new Bundle();
            b.putString("file_extension", ".xml");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ImportMnemosyne");
        }
        if(v == importSupermemoButton){
            DialogFragment df = new ConverterFragment(new SupermemoXMLImporter(mActivity), ".db");
            Bundle b = new Bundle();
            b.putString("file_extension", ".xml");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ImportSuperMemo2008");
        }
        if(v == importCSVButton) {
            DialogFragment df = new ConverterFragment(new CSVImporter(mActivity), ".db");
            Bundle b = new Bundle();
            b.putString("file_extension", ".csv");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ImportCSV");
        }
        if(v == importTabButton){
            DialogFragment df = new ConverterFragment(new CSVImporter(mActivity, '\t'), ".db");
            Bundle b = new Bundle();
            b.putString("file_extension", ".txt");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ImportTabTxt");
        }
        if(v == importQAButton){
            DialogFragment df = new ConverterFragment(new QATxtImporter(mActivity), ".db");
            Bundle b = new Bundle();
            b.putString("file_extension", ".txt");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ImportCSV");
        }
        if(v == importSupermemo2008Button) {
            DialogFragment df = new ConverterFragment(new Supermemo2008XMLImporter(mActivity), ".db");
            Bundle b = new Bundle();
            b.putString("file_extension", ".xml");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ImportSuperMemo2008");
        }
        if(v == exportMnemosyneButton){
            DialogFragment df = new ConverterFragment(new MnemosyneXMLExporter(mActivity), ".xml");
            Bundle b = new Bundle();
            b.putString("file_extension", ".db");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ExportMnemosyne");
        }
        if(v == exportCSVButton){
            DialogFragment df = new ConverterFragment(new CSVExporter(mActivity), ".csv");
            Bundle b = new Bundle();
            b.putString("file_extension", ".db");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ExportCSV");
        }
        if(v == exportTabButton){
            DialogFragment df = new ConverterFragment(new CSVExporter(mActivity, '\t'), ".txt");
            Bundle b = new Bundle();
            b.putString("file_extension", ".db");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ExportTabTxt");
        }
        if(v == exportQAButton){
            DialogFragment df = new ConverterFragment(new QATxtExporter(mActivity), ".txt");
            Bundle b = new Bundle();
            b.putString("file_extension", ".db");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ExportQA");
        }
        if(v == mergeButton){
            Intent myIntent = new Intent(mActivity, DatabaseMerger.class);
            startActivity(myIntent);
        }
        if(v == resetButton){
            new AlertDialog.Builder(mActivity)
                .setTitle(R.string.clear_all_pref)
                .setMessage(R.string.reset_all_pref_warning)
                .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1){
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.clear();
                        editor.commit();
                        mActivity.finish();
                    }
                })
                .setNegativeButton(R.string.cancel_text, null)
                .show();
        }
        if(v == donateButton){
            View alertView = View.inflate(mActivity, R.layout.link_alert, null);
            TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
            textView.setText(Html.fromHtml(getString(R.string.donate_summary)));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(mActivity)
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
            View alertView = View.inflate(mActivity, R.layout.link_alert, null);
            TextView textView = (TextView)alertView.findViewById(R.id.link_alert_message);
            textView.setText(Html.fromHtml(getString(R.string.about_text)));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            new AlertDialog.Builder(mActivity)
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
