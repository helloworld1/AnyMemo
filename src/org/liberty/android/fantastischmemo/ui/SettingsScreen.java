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
import java.util.Arrays;
import java.util.List;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Setting;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

//
//private CheckBox wipeCheckbox;
//private CheckBox shuffleCheckbox;
//private CheckBox inverseCheckbox;
//private CheckBox qTypefaceCheckbox;
//private CheckBox aTypefaceCheckbox;
//private CheckBox field1Checkbox;
//private CheckBox field2Checkbox;

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
    private final static int LAYOUT_SPINNER_DROPDOWN_ITEM = android.R.layout.simple_spinner_dropdown_item;
    private final static int LAYOUT_SPINNER_ITEM = android.R.layout.simple_spinner_item;

    public static final String EXTRA_DBPATH = "dbpath";
    private String dbPath;
    private SettingDao settingDao;
    private Setting setting;

    // Widgets
    private AMIntSpinner questionFontSizeSpinner;
    private AMIntSpinner answerFontSizeSpinner;
    private AMEnumSpinner<Setting.Align> questionAlignSpinner;
    private AMEnumSpinner<Setting.Align> answerAlignSpinner;
    // private AMEnumSpinner<Setting.CardStyle> questionStyleSpinner;
    // private AMEnumSpinner<Setting.CardStyle> answerLocaleSpinner;
    private AMPercentageSpinner qaRatioSpinner;
    private AMEnumSpinner<Setting.CardStyle> styleSpinner;


    
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // ============================================================================
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
        //
        // if (v == colorButton) {
        // DialogFragment df = new CategoryEditorFragment();
        // Bundle b = new Bundle();
        // b.putString(CategoryEditorFragment.EXTRA_DBPATH, dbPath);
        // b.putInt(CategoryEditorFragment.EXTRA_CARD_ID, currentCardId);
        // df.setArguments(b);
        // df.show(getSupportFragmentManager(), "CategoryEditDialog");
        // }
    }

    private void updateViews() {
        // /* Retain the last category when editing new */
        // String categoryName = currentCard.getCategory().getName();
        // if (categoryName.equals("")) {
        // categoryButton.setText(R.string.uncategorized_text);
        // } else {
        // categoryButton.setText(categoryName);
        // }
        // /* Prefill the note if it is empty */
        //
        // if(isEditNew){
        // /* Use this one or the one below ?*/
        // noteEdit.setText(currentCard.getNote());
        // }
        // if(!isEditNew){
        // originalQuestion = currentCard.getQuestion();
        // originalAnswer = currentCard.getAnswer();
        // originalNote = currentCard.getNote();
        // questionEdit.setText(originalQuestion);
        // answerEdit.setText(originalAnswer);
        // noteEdit.setText(originalNote);
        // }
        // ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)
        // questionFontSizeSpinner.getAdapter();
        // questionFontSizeSpinner.setSelection(adapter.getPosition(setting.getQuestionFontSize().toString()));

        questionFontSizeSpinner.setSelectedItem(setting.getQuestionFontSize());
        answerFontSizeSpinner.setSelectedItem(setting.getAnswerFontSize());
        questionAlignSpinner.setSelectedItem(setting.getQuestionTextAlign());
        answerAlignSpinner.setSelectedItem(setting.getAnswerTextAlign());
        qaRatioSpinner.setSelectedItem(setting.getQaRatio());
        styleSpinner.setSelectedItem(setting.getCardStyle());
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

            // linebreakCheckbox = (CheckBox)
            // findViewById(R.id.linebreak_conversion);
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
            // linebreakCheckbox.setChecked(setting.getHtmlLineBreakConversion());

            questionFontSizeSpinner = new AMIntSpinner(getSpinner(R.id.question_font_size_spinner,
                    R.array.font_size_list));
            answerFontSizeSpinner = new AMIntSpinner(getSpinner(R.id.answer_font_size_spinner, R.array.font_size_list));
            questionAlignSpinner = new AMEnumSpinner<Setting.Align>(getSpinner(R.id.question_align_spinner,
                    R.array.align_list), Setting.Align.values());
            answerAlignSpinner = new AMEnumSpinner<Setting.Align>(getSpinner(R.id.answer_align_spinner,
                    R.array.align_list), Setting.Align.values());
            // questionLocaleSpinner = getSpinner(R.id.question_locale_spinner,
            // R.array.locale_list);
            // answerLocaleSpinner = getSpinner(R.id.answer_locale_spinner,
            // R.array.locale_list);
            qaRatioSpinner = new AMPercentageSpinner(getSpinner(R.id.ratio_spinner, R.array.ratio_list));
            styleSpinner = new AMEnumSpinner<Setting.CardStyle>(getSpinner(R.id.card_style_spinner,
                    R.array.card_style_list), Setting.CardStyle.values());

            saveButton = (Button) findViewById(R.id.settting_save);
            saveButton.setOnClickListener(SettingsScreen.this);
            discardButton = (Button) findViewById(R.id.setting_discard);
            discardButton.setOnClickListener(SettingsScreen.this);

            updateViews();

            progressDialog.dismiss();
        }
    }

    // ==============================================================
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
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();

            // setting.setQuestionFontSize(Integer.parseInt(getSelectedItem(questionFontSizeSpinner)));
            setting.setQuestionFontSize(questionFontSizeSpinner.getSelectedItem());
            setting.setAnswerFontSize(answerFontSizeSpinner.getSelectedItem());
            setting.setQuestionTextAlign(questionAlignSpinner.getSelectedItem());
            setting.setAnswerTextAlign(answerAlignSpinner.getSelectedItem());
            setting.setQaRatio(qaRatioSpinner.getSelectedItem());
            setting.setCardStyle(styleSpinner.getSelectedItem());
            // setting.setAnswerFontSize(Integer.parseInt(getSelectedItem(answerFontSizeSpinner)));
            // setting.setQuestionTextAlign(Setting.Align.values()[questionAlignSpinner.getSelectedItemPosition()]);
            // setting.setAnswerTextAlign(Setting.Align.values()[questionAlignSpinner.getSelectedItemPosition()]);
            // String ratioString = getSelectedItem(ratioSpinner);
            // setting.setQaRatio(Integer.parseInt(ratioString.substring(0,
            // ratioString.length()-1)));
            // setting.setCardStyle(Setting.CardStyle.values()[styleSpinner.getSelectedItemPosition()]);
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

    // ==============================================================
    // private void setSelectedItem(final Spinner spinner,
    // final CharSequence value) {
    // ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)
    // spinner.getAdapter();
    // spinner.setSelection(adapter.getPosition(value));
    // }
    //
    // private <T> getSelectedItem(final Spinner spinner, final T t) {
    // ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)
    // spinner.getAdapter();
    // CharSequence csValue =
    // adapter.getItem(spinner.getSelectedItemPosition());
    // return csValue.toString();
    // }

    private Spinner getSpinner(final int spinnerId, final int textArrayResId) {
        Spinner spinner = (Spinner) findViewById(spinnerId);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(SettingsScreen.this, textArrayResId,
                LAYOUT_SPINNER_ITEM);
        adapter.setDropDownViewResource(LAYOUT_SPINNER_DROPDOWN_ITEM);
        spinner.setAdapter(adapter);
        return spinner;
    }
}

abstract class AMSpinner<T> {
    protected Spinner mSpinner;

    public AMSpinner(Spinner spinner) {
        mSpinner = spinner;
    }

    abstract public void setSelectedItem(final T value);

    abstract public T getSelectedItem();
}

class AMIntSpinner extends AMSpinner<Integer> {
    public AMIntSpinner(Spinner spinner) {
        super(spinner);
    }

    @Override
    public void setSelectedItem(final Integer value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) mSpinner.getAdapter();
        mSpinner.setSelection(adapter.getPosition(String.valueOf(value)));
    }

    @Override
    public Integer getSelectedItem() {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) mSpinner.getAdapter();
        String strValue = adapter.getItem(mSpinner.getSelectedItemPosition()).toString();
        return Integer.valueOf(strValue);
    }
}

class AMPercentageSpinner extends AMSpinner<Integer> {
    public AMPercentageSpinner(Spinner spinner) {
        super(spinner);
    }

    @Override
    public void setSelectedItem(final Integer value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) mSpinner.getAdapter();
        mSpinner.setSelection(adapter.getPosition(String.valueOf(value) + '%'));
    }

    @Override
    public Integer getSelectedItem() {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) mSpinner.getAdapter();
        String strValue = adapter.getItem(mSpinner.getSelectedItemPosition()).toString();
        return Integer.valueOf(strValue.substring(0, strValue.length() - 1));
    }
}

/**
 * The valid values are given in the Enum T
 * 
 * @author sean
 * 
 * @param <T>
 */
class AMEnumSpinner<T extends Enum<T>> extends AMSpinner<T> {
    List<T> validValues;

    public AMEnumSpinner(Spinner spinner, T[] validValues) {
        super(spinner);
        this.validValues = Arrays.asList(validValues);
    }

    public void setSelectedItem(final T value) {
        for (int position = 0; position < validValues.size(); position++) {
            if (value == validValues.get(position)) {
                mSpinner.setSelection(position);
                return;
            }
        }
        assert (false);
    }

    public T getSelectedItem() {
        return validValues.get(mSpinner.getSelectedItemPosition());
    }
}
