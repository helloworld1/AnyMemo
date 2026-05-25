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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.liberty.android.fantastischmemo.BuildConfig;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.common.BaseFragment;
import org.liberty.android.fantastischmemo.converter.CSVExporter;
import org.liberty.android.fantastischmemo.converter.CSVImporter;
import org.liberty.android.fantastischmemo.converter.Mnemosyne2CardsExporter;
import org.liberty.android.fantastischmemo.converter.Mnemosyne2CardsImporter;
import org.liberty.android.fantastischmemo.converter.MnemosyneXMLExporter;
import org.liberty.android.fantastischmemo.converter.MnemosyneXMLImporter;
import org.liberty.android.fantastischmemo.converter.QATxtExporter;
import org.liberty.android.fantastischmemo.converter.QATxtImporter;
import org.liberty.android.fantastischmemo.converter.Supermemo2008XMLImporter;
import org.liberty.android.fantastischmemo.converter.SupermemoXMLImporter;
import org.liberty.android.fantastischmemo.converter.TabTxtExporter;
import org.liberty.android.fantastischmemo.converter.TabTxtImporter;
import org.liberty.android.fantastischmemo.converter.ZipExporter;
import org.liberty.android.fantastischmemo.converter.ZipImporter;
import org.liberty.android.fantastischmemo.utils.AboutUtil;

import javax.inject.Inject;

import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.DatabaseImportUtil;
import org.liberty.android.fantastischmemo.utils.FolderImportExportUtil;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
public class MiscTabFragment extends BaseFragment implements View.OnClickListener {
    private Activity mActivity;
    private View optionButton;
    private View importButton;
    private View exportButton;
    private View importItems;
    private View exportItems;
    private View importAnyMemoButton;
    private View exportAnyMemoButton;
    private View importAnyMemoFolderButton;
    private View exportAnyMemoFolderButton;

    private CompositeDisposable disposables;
    private static final int REQUEST_CODE_IMPORT_DB = 1001;
    private static final int REQUEST_CODE_IMPORT_CONVERT = 1005;
    private static final int REQUEST_CODE_IMPORT_FOLDER = 1006;
    private static final int REQUEST_CODE_EXPORT_FOLDER = 1007;
    private Class<org.liberty.android.fantastischmemo.converter.Converter> pendingImportConverterClass;

    @Inject RecentListUtil recentListUtil;
    @Inject AMFileUtil amFileUtil;
    @Inject DatabaseImportUtil databaseImportUtil;
    @Inject FolderImportExportUtil folderImportExportUtil;
    private View importMnemosyneButton;
    private View importSupermemoButton;
    private View importZipButton;
    private View importCSVButton;
    private View importTabButton;
    private View importQAButton;
    private View importSupermemo2008Button;
    private View importMnemosyne2CardsButton;
    private View exportMnemosyneButton;
    private View exportCSVButton;
    private View exportTabButton;
    private View exportQAButton;
    private View exportZipButton;
    private View exportMnemosyne2CardsButton;

    private View defaultSettingsButton;
    private View mergeButton;
    private View resetButton;
    private View donateButton;
    private View helpButton;
    private View aboutButton;

    @Inject AboutUtil aboutUtil;

    public MiscTabFragment() { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pendingImportConverterClass != null) {
            outState.putSerializable("pendingImportConverterClass", pendingImportConverterClass);
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        fragmentComponents().inject(this);
        disposables = new CompositeDisposable();
        if (bundle != null && bundle.containsKey("pendingImportConverterClass")) {
            pendingImportConverterClass = (Class<org.liberty.android.fantastischmemo.converter.Converter>) bundle.getSerializable("pendingImportConverterClass");
        }
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
        importAnyMemoButton = v.findViewById(R.id.import_anymemo_db);
        importAnyMemoButton.setOnClickListener(this);
        exportAnyMemoButton = v.findViewById(R.id.export_anymemo_db);
        exportAnyMemoButton.setOnClickListener(this);
        importAnyMemoFolderButton = v.findViewById(R.id.import_anymemo_folder);
        importAnyMemoFolderButton.setOnClickListener(this);
        exportAnyMemoFolderButton = v.findViewById(R.id.export_anymemo_folder);
        exportAnyMemoFolderButton.setOnClickListener(this);
        importMnemosyneButton = v.findViewById(R.id.import_mnemosyne);
        importMnemosyneButton.setOnClickListener(this);
        importSupermemoButton = v.findViewById(R.id.import_supermemo);
        importSupermemoButton.setOnClickListener(this);
        importCSVButton = v.findViewById(R.id.import_csv);
        importCSVButton.setOnClickListener(this);
        importZipButton = v.findViewById(R.id.import_zip);
        importZipButton.setOnClickListener(this);
        importTabButton = v.findViewById(R.id.import_tab);
        importTabButton.setOnClickListener(this);
        importQAButton = v.findViewById(R.id.import_qa);
        importQAButton.setOnClickListener(this);
        importSupermemo2008Button = v.findViewById(R.id.import_supermemo_2008);
        importSupermemo2008Button.setOnClickListener(this);
        importMnemosyne2CardsButton= v.findViewById(R.id.import_mnemosyne2_cards);
        importMnemosyne2CardsButton.setOnClickListener(this);
        exportMnemosyneButton = v.findViewById(R.id.export_mnemosyne);
        exportMnemosyneButton.setOnClickListener(this);
        exportCSVButton = v.findViewById(R.id.export_csv);
        exportCSVButton.setOnClickListener(this);
        exportTabButton = v.findViewById(R.id.export_tab);
        exportTabButton.setOnClickListener(this);
        exportQAButton = v.findViewById(R.id.export_qa);
        exportQAButton.setOnClickListener(this);
        exportZipButton = v.findViewById(R.id.export_zip);
        exportZipButton.setOnClickListener(this);
        exportMnemosyne2CardsButton = v.findViewById(R.id.export_mnemosyne2_cards);
        exportMnemosyne2CardsButton.setOnClickListener(this);

        defaultSettingsButton = v.findViewById(R.id.misc_default_settings);
        defaultSettingsButton.setOnClickListener(this);
        mergeButton = v.findViewById(R.id.misc_merge);
        mergeButton.setOnClickListener(this);
        resetButton = v.findViewById(R.id.misc_reset);
        resetButton.setOnClickListener(this);

        donateButton = v.findViewById(R.id.misc_donate);
        donateButton.setOnClickListener(this);

        // Pro version doesn't have donate button
        if (BuildConfig.FLAVOR.equals("pro")) {
            donateButton.setVisibility(View.GONE);
        }

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

        if(v == importAnyMemoButton){
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CODE_IMPORT_DB);
        }
        if(v == exportAnyMemoButton){
            DialogFragment df = new ExportAnyMemoDbFragment();
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ExportAnyMemoDb");
        }
        if(v == importAnyMemoFolderButton){
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_CODE_IMPORT_FOLDER);
        }
        if(v == exportAnyMemoFolderButton){
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_CODE_EXPORT_FOLDER);
        }

        if(v == importMnemosyneButton){
            launchImportConvert(MnemosyneXMLImporter.class);
        }
        if(v == importSupermemoButton){
            launchImportConvert(SupermemoXMLImporter.class);
        }
        if(v == importCSVButton) {
            launchImportConvert(CSVImporter.class);
        }
        if(v == importZipButton) {
            launchImportConvert(ZipImporter.class);
        }
        if(v == importTabButton){
            launchImportConvert(TabTxtImporter.class);
        }
        if(v == importQAButton){
            launchImportConvert(QATxtImporter.class);
        }
        if(v == importSupermemo2008Button) {
            launchImportConvert(Supermemo2008XMLImporter.class);
        }
        if(v == importMnemosyne2CardsButton) {
            launchImportConvert(Mnemosyne2CardsImporter.class);
        }
        if(v == exportMnemosyneButton){
            DialogFragment df = new ConverterFragment();
            Bundle b = new Bundle();
            b.putSerializable(ConverterFragment.EXTRA_CONVERTER_CLASS, MnemosyneXMLExporter.class);
            b.putString(FileBrowserFragment.EXTRA_FILE_EXTENSIONS, ".db");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ExportMnemosyne");
        }
        if(v == exportCSVButton){
            DialogFragment df = new ConverterFragment();
            Bundle b = new Bundle();
            b.putSerializable(ConverterFragment.EXTRA_CONVERTER_CLASS, CSVExporter.class);
            b.putString(FileBrowserFragment.EXTRA_FILE_EXTENSIONS, ".db");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ExportCSV");
        }
        if(v == exportTabButton){
            DialogFragment df = new ConverterFragment();
            Bundle b = new Bundle();
            b.putSerializable(ConverterFragment.EXTRA_CONVERTER_CLASS, TabTxtExporter.class);
            b.putString(FileBrowserFragment.EXTRA_FILE_EXTENSIONS, ".db");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ExportTabTxt");
        }
        if(v == exportQAButton){
            DialogFragment df = new ConverterFragment();
            Bundle b = new Bundle();
            b.putSerializable(ConverterFragment.EXTRA_CONVERTER_CLASS, QATxtExporter.class);
            b.putString(FileBrowserFragment.EXTRA_FILE_EXTENSIONS, ".db");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ExportQA");
        }
        if(v == exportZipButton) {
            DialogFragment df = new ConverterFragment();
            Bundle b = new Bundle();
            b.putSerializable(ConverterFragment.EXTRA_CONVERTER_CLASS, ZipExporter.class);
            b.putString(FileBrowserFragment.EXTRA_FILE_EXTENSIONS, ".db");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ExportZip");
        }
        if(v == exportMnemosyne2CardsButton) {
            DialogFragment df = new ConverterFragment();
            Bundle b = new Bundle();
            b.putSerializable(ConverterFragment.EXTRA_CONVERTER_CLASS, Mnemosyne2CardsExporter.class);
            b.putString(FileBrowserFragment.EXTRA_FILE_EXTENSIONS, ".db");
            df.setArguments(b);
            df.show(((FragmentActivity)mActivity).getSupportFragmentManager(), "ExportMnemosyne2Cards");
        }

        if (v == defaultSettingsButton) {
            Intent intent = new Intent(mActivity, SettingsScreen.class);

            String emptyDbPath = mActivity.getApplicationContext().getFilesDir().getAbsolutePath() + "/" + AMEnv.EMPTY_DB_NAME;
            intent.putExtra(SettingsScreen.EXTRA_DBPATH, emptyDbPath);
            startActivity(intent);
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
            myIntent.setData(Uri.parse(getString(R.string.website_versions_view)));
            startActivity(myIntent);
        }
        if(v == aboutButton){
            aboutUtil.createAboutDialog();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposables != null) {
            disposables.dispose();
        }
    }

    private void launchImportConvert(Class<? extends org.liberty.android.fantastischmemo.converter.Converter> converterClass) {
        pendingImportConverterClass = (Class<org.liberty.android.fantastischmemo.converter.Converter>) converterClass;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_IMPORT_CONVERT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_IMPORT_DB && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            final Uri uri = data.getData();
            databaseImportUtil.handleImportDbResult(mActivity, uri, disposables, null);
        } else if (requestCode == REQUEST_CODE_IMPORT_FOLDER && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            folderImportExportUtil.importFolder(mActivity, data.getData(), disposables);
        } else if (requestCode == REQUEST_CODE_EXPORT_FOLDER && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            folderImportExportUtil.exportFolder(mActivity, data.getData(), disposables);
        } else if (requestCode == REQUEST_CODE_IMPORT_CONVERT && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            final Uri uri = data.getData();
            if (pendingImportConverterClass != null) {
                String newFileName = amFileUtil.getFileNameFromUri(mActivity, uri);
                if (newFileName == null) {
                    newFileName = "imported_db";
                }
                String baseName = org.apache.commons.io.FilenameUtils.removeExtension(newFileName);
                final String outputPath = AMEnv.DEFAULT_ROOT_PATH + baseName + ".db";

                if (new File(outputPath).exists()) {
                    new AlertDialog.Builder(mActivity)
                        .setTitle(R.string.conversion_merge_text)
                        .setMessage(String.format(getString(R.string.conversion_merge_message), outputPath, newFileName, outputPath))
                        .setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                invokeImportConversion(uri, outputPath);
                            }
                        })
                        .setNeutralButton(R.string.no_text, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    amFileUtil.deleteFileWithBackup(outputPath);
                                    invokeImportConversion(uri, outputPath);
                                } catch (Exception e) {
                                    Log.e("MiscTabFragment", "Failed to deleteWithBackup: " + outputPath, e);
                                    Toast.makeText(mActivity, getString(R.string.fail) + ": " + e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel_text, null)
                        .show();
                } else {
                    invokeImportConversion(uri, outputPath);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void invokeImportConversion(Uri inputUri, String outputPath) {
        Intent intent =  new Intent(mActivity, org.liberty.android.fantastischmemo.service.ConvertIntentService.class);
        intent.setAction(org.liberty.android.fantastischmemo.service.ConvertIntentService.ACTION_CONVERT);
        Bundle b = new Bundle();
        b.putSerializable(org.liberty.android.fantastischmemo.service.ConvertIntentService.EXTRA_CONVERTER_CLASS, pendingImportConverterClass);
        b.putString(org.liberty.android.fantastischmemo.service.ConvertIntentService.EXTRA_INPUT_FILE_PATH, amFileUtil.getFileNameFromUri(mActivity, inputUri));
        b.putParcelable(org.liberty.android.fantastischmemo.service.ConvertIntentService.EXTRA_INPUT_URI, inputUri);
        b.putString(org.liberty.android.fantastischmemo.service.ConvertIntentService.EXTRA_OUTPUT_FILE_PATH, outputPath);
        intent.putExtras(b);
        mActivity.startService(intent);
        Toast.makeText(mActivity, R.string.conversion_started_text, Toast.LENGTH_SHORT).show();
    }
}
