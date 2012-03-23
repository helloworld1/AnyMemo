/*
Copyright (C) 2012 Haowen Ning, Xinyu Zhang

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

import java.io.File;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.mycommons.lang3.StringUtils;

import org.color.ColorDialog;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.domain.Setting.CardField;
import org.liberty.android.fantastischmemo.ui.widgets.AMSpinner;

import org.liberty.android.fantastischmemo.utils.DatabaseUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.Toast;

public class SettingsScreen extends AMActivity implements OnClickListener , ColorDialog.OnClickListener {

    public static final String EXTRA_DBPATH = "dbpath";
    private String dbPath;
    private SettingDao settingDao;
    private Setting setting;

    // Widgets
    private AMSpinner questionFontSizeSpinner;
    private AMSpinner answerFontSizeSpinner;
    private AMSpinner questionAlignSpinner;
    private AMSpinner answerAlignSpinner;
    private AMSpinner styleSpinner;
    private AMSpinner qaRatioSpinner;
    private AMSpinner questionLocaleSpinner;
    private AMSpinner answerLocaleSpinner;
    private LinearLayout audioLocationLayout;
    private EditText audioLocationEdit;

    private CheckBox colorCheckbox;
    private TableRow colorRow;
    private AMSpinner colorSpinner;
    private Button colorButton;
    private List<Integer> colors;

    private CheckBox qTypefaceCheckbox;
    private CheckBox aTypefaceCheckbox;
    private EditText qTypefaceEdit;
    private EditText aTypefaceEdit;

    private CheckBox displayInHTMLCheckbox;
    private EnumSet<CardField> fieldsDisplayedInHTML;    
    private CheckBox linebreakCheckbox;
    private CheckBox field1Checkbox;
    private CheckBox field2Checkbox;
    private EnumSet<CardField> questionFields;
    private EnumSet<CardField> answerFields;

    private Button saveButton;
    private Button discardButton;
    private Button loadDefaultButton;

    private AnyMemoDBOpenHelper dbOpenHelper;

    private final static String WEBSITE_HELP_SETTINGS="http://anymemo.org/wiki/index.php?title=Card_styles";
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        InitTask initTask = new InitTask();
        initTask.execute((Void) null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.settingsmenu_save:
            SaveButtonTask task = new SaveButtonTask();
            task.execute((Void) null);
            return true;

        case R.id.settingsmenu_discard:
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, resultIntent);
            finish();
            return true;

        case R.id.settingsmenu_help:
            Intent myIntent = new Intent();
            myIntent.setAction(Intent.ACTION_VIEW);
            myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            myIntent.setData(Uri.parse(WEBSITE_HELP_SETTINGS));
            startActivity(myIntent);
            return true;

        }
        return false;
    }

    // Override method of onClickListener
    public void onClick(View v) {
        if (v == saveButton) {
            SaveButtonTask task = new SaveButtonTask();
            task.execute((Void) null);
        }

        if (v == discardButton) {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, resultIntent);
            finish();
        }

        if (v == loadDefaultButton) {
            new AlertDialog.Builder(this)
                .setTitle(R.string.load_default_text)
                .setMessage(R.string.load_default_warning_text)
                .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LoadDefaultTask task = new LoadDefaultTask();
                        task.execute((Void)null);
                        // Need to refresh the activity that invoke this activity.
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                    }
                })
                .setNegativeButton(R.string.cancel_text, null)
                .show();
        }
        
        if (v == colorCheckbox) {
            if (colorCheckbox.isChecked()) {
                colorRow.setVisibility(View.VISIBLE);
            } else {
                colorRow.setVisibility(View.GONE);
                resetToDefaultColors();
            }
        }
        
        if (v == colorButton) {
            int pos = colorSpinner.getSelectedItemPosition();
            ColorDialog dialog = new ColorDialog(this, colorButton, colors.get(pos), this);
            dialog.show();
        }
        
        if (v == qTypefaceCheckbox) {
            if (qTypefaceCheckbox.isChecked()) {
                qTypefaceEdit.setVisibility(View.VISIBLE);
            } else {
                qTypefaceEdit.setVisibility(View.GONE);
                setting.setQuestionFont("");
            }
        }
        
        if (v == aTypefaceCheckbox) {
            if (aTypefaceCheckbox.isChecked()) {
                aTypefaceEdit.setVisibility(View.VISIBLE);
            } else {
                aTypefaceEdit.setVisibility(View.GONE);
                setting.setAnswerFont("");
            }
        }
        
        if (v == displayInHTMLCheckbox) {
            if (displayInHTMLCheckbox.isChecked()) {
                // Create a AlertDialog for user to select fields display in HTML
                fieldsDisplayedInHTML = setting.getDisplayInHTMLEnum();
                showCardFieldMultipleChoiceAlertDialog(fieldsDisplayedInHTML, R.string.settings_html_display);
            } else {
                fieldsDisplayedInHTML = EnumSet.noneOf(CardField.class);
            }
            if (fieldsDisplayedInHTML.size() == 0) {
                field1Checkbox.setChecked(false);
            }            
        }

        if (v == field1Checkbox) {
            if (field1Checkbox.isChecked()) {
                questionFields = setting.getQuestionFieldEnum();
                showCardFieldMultipleChoiceAlertDialog(questionFields, R.string.settings_field1);
            } else {
                questionFields = EnumSet.of(CardField.QUESTION);
            }
            if (questionFields.size() == 0) {
                field1Checkbox.setChecked(false);
            }            
        }
        
        if (v == field2Checkbox) {
            if (field2Checkbox.isChecked()) {
                answerFields = setting.getAnswerFieldEnum();
                showCardFieldMultipleChoiceAlertDialog(answerFields, R.string.settings_field2);
            } else {
                answerFields = EnumSet.of(CardField.ANSWER);
            }
            if (answerFields.size() == 0) {
                field2Checkbox.setChecked(false);
            }            
        }
        if (v == qTypefaceEdit) {
            FileBrowserFragment df = new FileBrowserFragment();
            Bundle b = new Bundle();
            b.putString(FileBrowserFragment.EXTRA_FILE_EXTENSIONS, ".ttf");
            b.putString(FileBrowserFragment.EXTRA_DEFAULT_ROOT, AMEnv.DEFAULT_ROOT_PATH);
            b.putBoolean(FileBrowserFragment.EXTRA_DISMISS_ON_SELECT, true);
            df.setArguments(b);
            df.setOnFileClickListener(qTypefaceEditFbListener);
            df.show(getSupportFragmentManager(), "qTypefaceEditFB");
        }

        if (v == aTypefaceEdit) {
            FileBrowserFragment df = new FileBrowserFragment();
            Bundle b = new Bundle();
            b.putString(FileBrowserFragment.EXTRA_FILE_EXTENSIONS, ".ttf");
            b.putString(FileBrowserFragment.EXTRA_DEFAULT_ROOT, AMEnv.DEFAULT_ROOT_PATH);
            b.putBoolean(FileBrowserFragment.EXTRA_DISMISS_ON_SELECT, true);
            df.setArguments(b);
            df.setOnFileClickListener(aTypefaceEditFbListener);
            df.show(getSupportFragmentManager(), "aTypefaceEditFB");
        }
    }


    @Override
    public void onClick(View view, int color){
        int pos = colorSpinner.getSelectedItemPosition();
        colorButton.setTextColor(color);
        colors.set(pos, color);
    }
    
    private void resetToDefaultColors() {
        int[] defaultColors = getResources().getIntArray(R.array.default_color_list);
        for (int i=0; i < colors.size() && i < defaultColors.length; i++) {
            colors.set(i, defaultColors[i]);
        }
    }
    
    private void showCardFieldMultipleChoiceAlertDialog(final EnumSet<CardField> selectedFields, 
                                                        final int titleStringId) {
        // Create a AlertDialog for user to select fields in field 1 (the question part).        
        boolean[] fieldSelection = new boolean[CardField.values().length];
        int i = 0;
        for (CardField field: CardField.values()) {
            if (selectedFields.contains(field)) {
                fieldSelection[i] = true;
            } else {
                fieldSelection[i] = false;
            }
            i++;
        }
        final String[] fieldText = getResources().getStringArray(R.array.card_field_list);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleStringId);
        builder.setMultiChoiceItems(fieldText, fieldSelection,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedFields.add(CardField.values()[which]);
                        } else {
                            selectedFields.remove(CardField.values()[which]);
                        }
                    }
                });
        builder.setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

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
        }

        @Override
        public Void doInBackground(Void... params) {
            try {
                dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(SettingsScreen.this, dbPath);

                settingDao = dbOpenHelper.getSettingDao();
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
            questionFontSizeSpinner = (AMSpinner)findViewById(R.id.question_font_size_spinner);
            answerFontSizeSpinner =  (AMSpinner)findViewById(R.id.answer_font_size_spinner);
            questionAlignSpinner = (AMSpinner)findViewById(R.id.question_align_spinner);
            answerAlignSpinner = (AMSpinner)findViewById(R.id.answer_align_spinner);
            styleSpinner =  (AMSpinner)findViewById(R.id.card_style_spinner);
            qaRatioSpinner =  (AMSpinner)findViewById(R.id.ratio_spinner);

            questionLocaleSpinner =  (AMSpinner)findViewById(R.id.question_locale_spinner);
            answerLocaleSpinner =  (AMSpinner)findViewById(R.id.answer_locale_spinner);
            
            AdapterView.OnItemSelectedListener localeListener = new AdapterView.OnItemSelectedListener(){
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id){
                    /* This is the position os "User Audio" */
                    if(position == 1){
                        audioLocationLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(SettingsScreen.this, getString(R.string.tts_tip_user_audio), Toast.LENGTH_SHORT).show();
                    }
                    else if(position > 7){
                        Toast.makeText(SettingsScreen.this, getString(R.string.tts_tip_extender), Toast.LENGTH_SHORT).show();
                    }

                    if(answerLocaleSpinner.getSelectedItemPosition() != 1 && questionLocaleSpinner.getSelectedItemPosition() != 1) {
                        audioLocationLayout.setVisibility(View.GONE);
                    }
                }
                public void onNothingSelected(AdapterView<?> adapterView){
                    audioLocationLayout.setVisibility(View.GONE);
                }
            };
            questionLocaleSpinner.setOnItemSelectedListener(localeListener);
            answerLocaleSpinner.setOnItemSelectedListener(localeListener);
            audioLocationLayout = (LinearLayout) findViewById(R.id.settings_audio_location_view);
            audioLocationEdit = (EditText) findViewById(R.id.settings_audio_location);
            audioLocationEdit.setText(AMEnv.DEFAULT_AUDIO_PATH);
            colorCheckbox = (CheckBox) findViewById(R.id.checkbox_customize_color);
            colorCheckbox.setOnClickListener(SettingsScreen.this);
            colorRow = (TableRow) findViewById(R.id.color_row);

            colorSpinner =  (AMSpinner)findViewById(R.id.color_item_spinner);
            colorButton = (Button) findViewById(R.id.settings_color_button);
            colorButton.setOnClickListener(SettingsScreen.this);
            colors = new ArrayList<Integer>(5);
            colors.add(setting.getQuestionTextColor());
            colors.add(setting.getAnswerTextColor());
            colors.add(setting.getQuestionBackgroundColor());
            colors.add(setting.getAnswerBackgroundColor());
            colors.add(setting.getSeparatorColor());
            colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id){
                    colorButton.setTextColor(colors.get(position));

                }
                public void onNothingSelected(AdapterView<?> adapterView){
                }
            });
            
            qTypefaceCheckbox = (CheckBox) findViewById(R.id.checkbox_typeface_question);
            qTypefaceCheckbox.setOnClickListener(SettingsScreen.this);
            aTypefaceCheckbox = (CheckBox) findViewById(R.id.checkbox_typeface_answer);
            aTypefaceCheckbox.setOnClickListener(SettingsScreen.this);

            qTypefaceEdit = (EditText) findViewById(R.id.edit_typeface_question);
            qTypefaceEdit.setOnClickListener(SettingsScreen.this);

            aTypefaceEdit = (EditText) findViewById(R.id.edit_typeface_answer);
            aTypefaceEdit.setOnClickListener(SettingsScreen.this);

            displayInHTMLCheckbox = (CheckBox) findViewById(R.id.display_html);
            displayInHTMLCheckbox.setOnClickListener(SettingsScreen.this);
            fieldsDisplayedInHTML = setting.getDisplayInHTMLEnum();

            linebreakCheckbox = (CheckBox) findViewById(R.id.linebreak_conversion);
            linebreakCheckbox.setOnClickListener(SettingsScreen.this);

            field1Checkbox = (CheckBox) findViewById(R.id.checkbox_field1);
            field1Checkbox.setOnClickListener(SettingsScreen.this);
            questionFields = setting.getQuestionFieldEnum();
            
            field2Checkbox = (CheckBox) findViewById(R.id.checkbox_field2);
            field2Checkbox.setOnClickListener(SettingsScreen.this);
            answerFields = setting.getAnswerFieldEnum();

            saveButton = (Button) findViewById(R.id.settting_save);
            saveButton.setOnClickListener(SettingsScreen.this);

            discardButton = (Button) findViewById(R.id.setting_discard);
            discardButton.setOnClickListener(SettingsScreen.this);

            loadDefaultButton = (Button) findViewById(R.id.load_default);
            loadDefaultButton.setOnClickListener(SettingsScreen.this);
                    
            updateViews();

            progressDialog.dismiss();
        }
    }

    private void updateViews() {
        // Default 24 px font
        questionFontSizeSpinner.selectItemFromValue(Integer.toString(setting.getQuestionFontSize()), 6);
        answerFontSizeSpinner.selectItemFromValue(Integer.toString(setting.getAnswerFontSize()), 6);

        // Default center
        questionAlignSpinner.selectItemFromValue(setting.getQuestionTextAlign().toString(), 1);
        answerAlignSpinner.selectItemFromValue(setting.getAnswerTextAlign().toString(), 1);

        // Default US but need special care
        questionLocaleSpinner.selectItemFromValue(setting.getQuestionAudio(), 2);
        answerLocaleSpinner.selectItemFromValue(setting.getAnswerAudio(), 2);

        if (StringUtils.isNotEmpty(setting.getQuestionAudioLocation())) {
            // User audio
            questionLocaleSpinner.setSelection(1);
        } else if (StringUtils.isEmpty(setting.getQuestionAudio())) {
            // Disabled
            questionLocaleSpinner.setSelection(0);
        }

        if (StringUtils.isNotEmpty(setting.getAnswerAudioLocation())) {
            // User audio
            answerLocaleSpinner.setSelection(1);
        } else if (StringUtils.isEmpty(setting.getAnswerAudio())) {
            // Disabled
            answerLocaleSpinner.setSelection(0);
        }


        // Default to single sided
        styleSpinner.selectItemFromValue(setting.getCardStyle().toString(), 0);

        // Default to 50
        qaRatioSpinner.selectItemFromValue(setting.getQaRatio().toString(), 0);
        
        if (StringUtils.isNotEmpty(setting.getQuestionAudioLocation()) || 
            StringUtils.isNotEmpty(setting.getAnswerAudio())){
            audioLocationLayout.setVisibility(View.VISIBLE);
        } else {
            audioLocationLayout.setVisibility(View.GONE);
        }

        colorCheckbox.setChecked(!setting.isDefaultColor());
        if (colorCheckbox.isChecked()) {
            colorRow.setVisibility(View.VISIBLE);
        } else {
            colorRow.setVisibility(View.GONE);
        }
        colorButton.setTextColor(colors.get(colorSpinner.getSelectedItemPosition()));

        qTypefaceCheckbox.setChecked(StringUtils.isNotEmpty(setting.getQuestionFont()));
        if (qTypefaceCheckbox.isChecked()) {
            qTypefaceEdit.setVisibility(View.VISIBLE);
            qTypefaceEdit.setText(setting.getQuestionFont());
        } else {
            qTypefaceEdit.setVisibility(View.GONE);
        }
        
        aTypefaceCheckbox.setChecked(StringUtils.isNotEmpty(setting.getAnswerFont()));
        if (aTypefaceCheckbox.isChecked()) {
            aTypefaceEdit.setVisibility(View.VISIBLE);
            aTypefaceEdit.setText(setting.getAnswerFont());
        } else {
            aTypefaceEdit.setVisibility(View.GONE);
        }

        displayInHTMLCheckbox.setChecked(!(fieldsDisplayedInHTML.isEmpty()));
        linebreakCheckbox.setChecked(setting.getHtmlLineBreakConversion());
        field1Checkbox.setChecked(!(questionFields.size() == 1 && questionFields.contains(CardField.QUESTION)));
        field2Checkbox.setChecked(!(answerFields.size() == 1 && answerFields.contains(CardField.ANSWER)));
    }

    /*
     * Save settings back to database
     */
    private class SaveButtonTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute() {
            progressDialog = new ProgressDialog(SettingsScreen.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_save));
            progressDialog.setCancelable(false);
            progressDialog.show();

            setting.setQuestionFontSize(Integer.valueOf(questionFontSizeSpinner.getSelectedItemValue()));
            setting.setAnswerFontSize(Integer.valueOf(answerFontSizeSpinner.getSelectedItemValue()));
            setting.setQuestionTextAlign(Setting.Align.valueOf(questionAlignSpinner.getSelectedItemValue()));
            setting.setAnswerTextAlign(Setting.Align.valueOf(answerAlignSpinner.getSelectedItemValue()));
            setting.setCardStyle(Setting.CardStyle.valueOf(styleSpinner.getSelectedItemValue()));
            setting.setQaRatio(Integer.valueOf(qaRatioSpinner.getSelectedItemValue()));


            // User audio need specicial care.
            // The posision = 0 means disabled. 1 means user audio
            if (questionLocaleSpinner.getSelectedItemPosition() == 0) {
                // audio and location are both empty to indicate disable
                setting.setQuestionAudio("");
                setting.setQuestionAudioLocation("");
            } else if (questionLocaleSpinner.getSelectedItemPosition() == 1) {
                setting.setQuestionAudioLocation(audioLocationEdit.getText().toString());
                setting.setQuestionAudio(questionLocaleSpinner.getSelectedItemValue());
            } else {
                setting.setQuestionAudio(questionLocaleSpinner.getSelectedItemValue());
                // Location is empty to indicate it is not user audio
                setting.setQuestionAudioLocation("");
            }

            if (answerLocaleSpinner.getSelectedItemPosition() == 0) {
                setting.setAnswerAudio("");
                setting.setAnswerAudioLocation("");
            } else if (answerLocaleSpinner.getSelectedItemPosition() == 1) {
                setting.setAnswerAudioLocation(audioLocationEdit.getText().toString());
                setting.setAnswerAudio(answerLocaleSpinner.getSelectedItemValue());
            } else {
                setting.setAnswerAudio(answerLocaleSpinner.getSelectedItemValue());
                setting.setAnswerAudioLocation("");
            }

            setting.setQuestionTextColor(colors.get(0));
            setting.setAnswerTextColor(colors.get(1));
            setting.setQuestionBackgroundColor(colors.get(2));
            setting.setAnswerBackgroundColor(colors.get(3));
            setting.setSeparatorColor(colors.get(4));

            setting.setQuestionFont(qTypefaceEdit.getText().toString());
            setting.setAnswerFont(aTypefaceEdit.getText().toString());

            setting.setDisplayInHTMLEnum(fieldsDisplayedInHTML);
            setting.setHtmlLineBreakConversion(linebreakCheckbox.isChecked());
            setting.setQuestionFieldEnum(questionFields);
            setting.setAnswerEnum(answerFields);

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
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }

    private class LoadDefaultTask extends AsyncTask<Void, Void, Void> {
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
                Setting defaultSetting = DatabaseUtils.readDefaultSetting(SettingsScreen.this);
                settingDao.replaceSetting(defaultSetting);
                setting = settingDao.queryForId(1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            progressDialog.dismiss();
            updateViews();
        }
    }

    private FileBrowserFragment.OnFileClickListener qTypefaceEditFbListener
        = new FileBrowserFragment.OnFileClickListener() {
            public void onClick(File file) {
                qTypefaceEdit.setText(file.getAbsolutePath());
            }
        };

    private FileBrowserFragment.OnFileClickListener aTypefaceEditFbListener
        = new FileBrowserFragment.OnFileClickListener() {
            public void onClick(File file) {
                aTypefaceEdit.setText(file.getAbsolutePath());
            }
        };
    
}

