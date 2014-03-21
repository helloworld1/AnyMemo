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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import org.color.ColorDialog;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.domain.Setting.CardField;
import org.liberty.android.fantastischmemo.ui.loader.MultipleLoaderManager;
import org.liberty.android.fantastischmemo.ui.loader.SettingLoader;
import org.liberty.android.fantastischmemo.ui.widgets.AMSpinner;
import org.liberty.android.fantastischmemo.utils.DatabaseUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableRow;

import com.google.common.base.Strings;

public class SettingsScreen extends AMActivity {

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

    private AnyMemoDBOpenHelper dbOpenHelper;

    private DatabaseUtil databaseUtil;

    private boolean settingsChanged = false;

    private final static String WEBSITE_HELP_SETTINGS="http://anymemo.org/wiki/index.php?title=Card_styles";

    private MultipleLoaderManager multipleLoaderManager;

    @Inject
    public void setDatabaseUtil(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }


    @Inject
    public void setMultipleLoaderManager(
            MultipleLoaderManager multipleLoaderManager) {
        this.multipleLoaderManager = multipleLoaderManager;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dbPath = extras.getString(EXTRA_DBPATH);
        }
        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(SettingsScreen.this, dbPath);

        assert dbPath != null : "dbPath shouldn't be null";
        settingsChanged = false;

        multipleLoaderManager.registerLoaderCallbacks(1, new SettingLoaderCallbacks(), false);
        multipleLoaderManager.setOnAllLoaderCompletedRunnable(onPostInitRunnable);
        multipleLoaderManager.startLoading();
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
        case R.id.save:
            SaveButtonTask task = new SaveButtonTask();
            task.execute((Void) null);
            return true;

        case R.id.load_default:
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

    @Override
    public void onBackPressed() {
        if (settingsChanged) {
            new AlertDialog.Builder(this)
                .setTitle(R.string.warning_text)
                .setMessage(R.string.edit_dialog_unsave_warning)
                .setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface  d, int which){
                        SaveButtonTask task = new SaveButtonTask();
                        task.execute((Void) null);
                    }
                })
            .setNeutralButton(R.string.no_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface  d, int which){
                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    finish();
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();
        } else {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, resultIntent);
            finish();

        }
    }

    private ColorDialog.OnClickListener colorDialogOnClickListener = new ColorDialog.OnClickListener() {
        public void onClick(View view, int color){
            int pos = colorSpinner.getSelectedItemPosition();
            colorButton.setTextColor(color);
            colors.set(pos, color);
        }
    };

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
    private Runnable onPostInitRunnable = new Runnable() {
        @Override
        public void run() {
            // Dismiss the progress dialog

            settingDao = dbOpenHelper.getSettingDao();

            setContentView(R.layout.settings_screen);

            questionFontSizeSpinner = (AMSpinner)findViewById(R.id.question_font_size_spinner);

            answerFontSizeSpinner =  (AMSpinner)findViewById(R.id.answer_font_size_spinner);

            questionAlignSpinner = (AMSpinner)findViewById(R.id.question_align_spinner);

            answerAlignSpinner = (AMSpinner)findViewById(R.id.answer_align_spinner);

            styleSpinner =  (AMSpinner)findViewById(R.id.card_style_spinner);

            qaRatioSpinner =  (AMSpinner)findViewById(R.id.ratio_spinner);

            questionLocaleSpinner =  (AMSpinner)findViewById(R.id.question_locale_spinner);

            answerLocaleSpinner =  (AMSpinner)findViewById(R.id.answer_locale_spinner);

            audioLocationEdit = (EditText) findViewById(R.id.settings_audio_location);

            // If we got no text, we will use the default location.
            if (!Strings.isNullOrEmpty(setting.getQuestionAudio())) {
                audioLocationEdit.setText(setting.getQuestionAudioLocation());
            }

            if (!Strings.isNullOrEmpty(setting.getAnswerAudio())) {
                audioLocationEdit.setText(setting.getAnswerAudioLocation());
            }

            if (Strings.isNullOrEmpty(audioLocationEdit.getText().toString())) {
                audioLocationEdit.setText(AMEnv.DEFAULT_AUDIO_PATH);
            }

            colorCheckbox = (CheckBox) findViewById(R.id.checkbox_customize_color);
            colorCheckbox.setOnClickListener(settingFieldOnClickListener);
            colorRow = (TableRow) findViewById(R.id.color_row);

            colorSpinner =  (AMSpinner)findViewById(R.id.color_item_spinner);
            colorButton = (Button) findViewById(R.id.settings_color_button);
            colorButton.setOnClickListener(settingFieldOnClickListener);
            colors = new ArrayList<Integer>(5);
            colors.add(setting.getQuestionTextColor());
            colors.add(setting.getAnswerTextColor());
            colors.add(setting.getQuestionBackgroundColor());
            colors.add(setting.getAnswerBackgroundColor());
            colors.add(setting.getSeparatorColor());

            qTypefaceCheckbox = (CheckBox) findViewById(R.id.checkbox_typeface_question);
            qTypefaceCheckbox.setOnClickListener(settingFieldOnClickListener);
            aTypefaceCheckbox = (CheckBox) findViewById(R.id.checkbox_typeface_answer);
            aTypefaceCheckbox.setOnClickListener(settingFieldOnClickListener);

            qTypefaceEdit = (EditText) findViewById(R.id.edit_typeface_question);
            qTypefaceEdit.setOnClickListener(settingFieldOnClickListener);

            aTypefaceEdit = (EditText) findViewById(R.id.edit_typeface_answer);
            aTypefaceEdit.setOnClickListener(settingFieldOnClickListener);

            displayInHTMLCheckbox = (CheckBox) findViewById(R.id.display_html);
            displayInHTMLCheckbox.setOnClickListener(settingFieldOnClickListener);
            fieldsDisplayedInHTML = setting.getDisplayInHTMLEnum();

            linebreakCheckbox = (CheckBox) findViewById(R.id.linebreak_conversion);
            linebreakCheckbox.setOnClickListener(settingFieldOnClickListener);

            field1Checkbox = (CheckBox) findViewById(R.id.checkbox_field1);
            field1Checkbox.setOnClickListener(settingFieldOnClickListener);
            questionFields = setting.getQuestionFieldEnum();

            field2Checkbox = (CheckBox) findViewById(R.id.checkbox_field2);
            field2Checkbox.setOnClickListener(settingFieldOnClickListener);
            answerFields = setting.getAnswerFieldEnum();

            updateViews();

            setSpinnerListeners();

        }
    };

    private void setSpinnerListeners() {
        // This listener is set to detect if the spinner has been touched.
        // If it is touched then we think the settings are changed.
        View.OnTouchListener spinnerOnTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    settingsChanged = true;
                }

                // This must return false, we the spinner can still work.
                return false;
            }
        };

        questionFontSizeSpinner.setOnTouchListener(spinnerOnTouchListener);
        answerFontSizeSpinner.setOnTouchListener(spinnerOnTouchListener);
        questionAlignSpinner.setOnTouchListener(spinnerOnTouchListener);
        answerAlignSpinner.setOnTouchListener(spinnerOnTouchListener);
        styleSpinner.setOnTouchListener(spinnerOnTouchListener);
        qaRatioSpinner.setOnTouchListener(spinnerOnTouchListener);
        questionLocaleSpinner.setOnTouchListener(spinnerOnTouchListener);
        answerLocaleSpinner.setOnTouchListener(spinnerOnTouchListener);
        colorSpinner.setOnTouchListener(spinnerOnTouchListener);

        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id){
                colorButton.setTextColor(colors.get(position));

            }
            public void onNothingSelected(AdapterView<?> adapterView){
            }
        });
    }

    private class SettingLoaderCallbacks implements
            LoaderManager.LoaderCallbacks<Setting> {

        @Override
        public Loader<Setting> onCreateLoader(int arg0, Bundle arg1) {
            Loader<Setting> loader = new SettingLoader(SettingsScreen.this, dbPath);
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Setting> loader , Setting setting) {
            SettingsScreen.this.setting = setting;
            multipleLoaderManager.checkAllLoadersCompleted();
        }

        @Override
        public void onLoaderReset(Loader<Setting> arg0) {
            // Do nothing now
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

        if (!Strings.isNullOrEmpty(setting.getQuestionAudioLocation())) {
            // User audio
            questionLocaleSpinner.setSelection(1);
        } else if (Strings.isNullOrEmpty(setting.getQuestionAudio())) {
            // Disabled
            questionLocaleSpinner.setSelection(0);
        }

        if (!Strings.isNullOrEmpty(setting.getAnswerAudioLocation())) {
            // User audio
            answerLocaleSpinner.setSelection(1);
        } else if (Strings.isNullOrEmpty(setting.getAnswerAudio())) {
            // Disabled
            answerLocaleSpinner.setSelection(0);
        }


        // Default to single sided
        styleSpinner.selectItemFromValue(setting.getCardStyle().toString(), 0);

        // Default to 50, the index in the array for value 50 is 5.
        qaRatioSpinner.selectItemFromValue(setting.getQaRatio().toString(), 5);

        colorCheckbox.setChecked(!setting.isDefaultColor());
        if (colorCheckbox.isChecked()) {
            colorRow.setVisibility(View.VISIBLE);
        } else {
            colorRow.setVisibility(View.GONE);
        }
        colorButton.setTextColor(colors.get(colorSpinner.getSelectedItemPosition()));

        qTypefaceCheckbox.setChecked(!Strings.isNullOrEmpty(setting.getQuestionFont()));
        if (qTypefaceCheckbox.isChecked()) {
            qTypefaceEdit.setVisibility(View.VISIBLE);
            qTypefaceEdit.setText(setting.getQuestionFont());
        } else {
            qTypefaceEdit.setVisibility(View.GONE);
        }

        aTypefaceCheckbox.setChecked(!Strings.isNullOrEmpty(setting.getAnswerFont()));
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

            if (!qTypefaceCheckbox.isChecked()) {
                setting.setQuestionFont("");
            } else {
                setting.setQuestionFont(qTypefaceEdit.getText().toString());
            }
            if (!aTypefaceCheckbox.isChecked()) {
                setting.setAnswerFont("");
            } else {
                setting.setAnswerFont(aTypefaceEdit.getText().toString());
            }

            setting.setDisplayInHTMLEnum(fieldsDisplayedInHTML);
            setting.setHtmlLineBreakConversion(linebreakCheckbox.isChecked());
            setting.setQuestionFieldEnum(questionFields);
            setting.setAnswerEnum(answerFields);

        }

        @Override
        public Void doInBackground(Void... params) {
            settingDao.update(setting);
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
            Setting defaultSetting = databaseUtil.readDefaultSetting();
            settingDao.replaceSetting(defaultSetting);
            setting = settingDao.queryForId(1);
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            progressDialog.dismiss();

            // Force reloading
            multipleLoaderManager.startLoading(true);
        }
    }

    private View.OnClickListener settingFieldOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            // If the user click on these fields, then we assume user has changed some settings.
            settingsChanged = true;

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
                ColorDialog dialog = new ColorDialog(SettingsScreen.this, colorButton, colors.get(pos), colorDialogOnClickListener);
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
    };


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

