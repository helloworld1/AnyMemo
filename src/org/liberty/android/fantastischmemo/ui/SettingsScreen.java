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

import java.sql.SQLException;
import java.util.ArrayList;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.cardscreen.SettingManager;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;

//private Spinner answerFontSizeSpinner;
//private Spinner questionAlignSpinner;
//private Spinner answerAlignSpinner;
//private Spinner questionLocaleSpinner;
//private Spinner answerLocaleSpinner;
//private Spinner ratioSpinner;
//private Spinner styleSpinner;
//
//private CheckBox wipeCheckbox;
//private CheckBox shuffleCheckbox;
//private CheckBox inverseCheckbox;
//private CheckBox qTypefaceCheckbox;
//private CheckBox aTypefaceCheckbox;
//private CheckBox field1Checkbox;
//private CheckBox field2Checkbox;
//private CheckBox htmlCheckbox;
//private CheckBox linebreakCheckbox;
//
//private EditText qTypefaceEdit;
//private EditText aTypefaceEdit;
//private EditText audioLocationEdit;
//private LinearLayout audioLocationLayout;
//private TableRow colorRow;
//private Spinner colorSpinner;
//private CheckBox colorCheckbox;
//private Button colorButton;

//private ArrayList<Integer> colors;
//private long htmlValue = SettingManager.CardField.QUESTION | SettingManager.CardField.ANSWER
//        | SettingManager.CardField.NOTE;
//private long field1Value = SettingManager.CardField.QUESTION;
//private long field2Value = SettingManager.CardField.ANSWER;

public class SettingsScreen extends AMActivity implements OnClickListener {

    public static final String EXTRA_DBPATH = "dbpath";
    private String dbPath;
    private SettingDao settingDao;
    private Setting setting;
    
    private Spinner questionFontSizeSpinner;

    private Button saveButton;
    private Button discardButton;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        InitTask initTask = new InitTask();
        initTask.execute((Void) null);      
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbPath);
    }
    
    @Override    
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    public void onClick(View v) {
        if (v == saveButton) {
            SaveButtonTask task = new SaveButtonTask();
            task.execute((Void)null);
        } 
        
//        if (v == discardButton) {
//            String qText = questionEdit.getText().toString();
//            String aText = answerEdit.getText().toString();
//            String nText = noteEdit.getText().toString();
//            if (!isEditNew && (!qText.equals(originalQuestion) || !aText.equals(originalAnswer) || !nText.equals(originalNote))) {
//                new AlertDialog.Builder(this)
//                    .setTitle(R.string.warning_text)
//                    .setMessage(R.string.edit_dialog_unsave_warning)
//                    .setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener(){
//                        public void onClick(DialogInterface  d, int which){
//                            Intent resultIntent = new Intent();
//                            setResult(Activity.RESULT_CANCELED, resultIntent);              
//                            finish();
//
//                        }
//                    }) 
//                    .setNegativeButton(R.string.no_text, null)
//                    .create()
//                    .show();
//            }
//            else{
//                Intent resultIntent = new Intent();
//                setResult(Activity.RESULT_CANCELED, resultIntent);              
//                finish();
//
//            }
//        }
//
//        if (v == colorButton) {
//            DialogFragment df = new CategoryEditorFragment();
//            Bundle b = new Bundle();
//            b.putString(CategoryEditorFragment.EXTRA_DBPATH, dbPath);
//            b.putInt(CategoryEditorFragment.EXTRA_CARD_ID, currentCardId);
//            df.setArguments(b);
//            df.show(getSupportFragmentManager(), "CategoryEditDialog");
//        }
    }    

    private void updateViews() {
//        /* Retain the last category when editing new */
//        String categoryName = currentCard.getCategory().getName();
//        if (categoryName.equals("")) {
//            categoryButton.setText(R.string.uncategorized_text);
//        } else {
//            categoryButton.setText(categoryName);
//        }
//        /* Prefill the note if it is empty */
//
//        if(isEditNew){
//            /* Use this one or the one below ?*/
//            noteEdit.setText(currentCard.getNote());
//        }
//        if(!isEditNew){
//            originalQuestion = currentCard.getQuestion();
//            originalAnswer = currentCard.getAnswer();
//            originalNote = currentCard.getNote();
//            questionEdit.setText(originalQuestion);
//            answerEdit.setText(originalAnswer);
//            noteEdit.setText(originalNote);
//        }
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) questionFontSizeSpinner.getAdapter();
        
        questionFontSizeSpinner.setSelection(adapter.getPosition(setting.getQuestionFontSize().toString()));
    }
    
    // ============================================================================
    /*
     * Load settings from database
     */
    private class InitTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute() {
            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

            setContentView(R.layout.settings_screen);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                dbPath = extras.getString(EXTRA_DBPATH);
            }
            progressDialog = new ProgressDialog(SettingsScreen.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
            assert dbPath != null : "dbPath shouldn't be null";

//            linebreakCheckbox = (CheckBox) findViewById(R.id.linebreak_conversion);
        }

        @Override
        public Void doInBackground(Void... params) {
            try {
                AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(SettingsScreen.this, dbPath);

                settingDao = helper.getSettingDao();
                setting = settingDao.queryForId(1);                
                /* Run the learnQueue init in a separate thread */
                return null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void onCancelled() {
            return;
        }

        @Override
        public void onPostExecute(Void result) {
//            linebreakCheckbox.setChecked(setting.getHtmlLineBreakConversion());

            questionFontSizeSpinner = (Spinner) findViewById(R.id.question_font_size_spinner);
            ArrayAdapter<CharSequence> fontSizeAdapter 
                = ArrayAdapter.createFromResdource(this, 
                                                  R.array.font_size_list,
                                                  android.R.layout.simple_spinner_item);
            fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);            
            questionFontSizeSpinner.setAdapter(fontSizeAdapter);
                        
            updateViews();
            
            progressDialog.dismiss();            
        }
    }

    // ==============================================================
    private class SaveButtonTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute() {
            progressDialog = new ProgressDialog(SettingsScreen.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public Void doInBackground(Void... params) {
            try {
                settingDao.update(setting);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            progressDialog.dismiss();
            finish();
        }
    }
}


