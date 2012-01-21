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
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;

public class SettingsScreen extends AMActivity {

    public static final String EXTRA_DBPATH = "dbpath";
    private String dbPath;
    private SettingDao settingDao;

    private Spinner questionFontSizeSpinner;
    private Spinner answerFontSizeSpinner;
    private Spinner questionAlignSpinner;
    private Spinner answerAlignSpinner;
    private Spinner questionLocaleSpinner;
    private Spinner answerLocaleSpinner;
    private Spinner ratioSpinner;
    private Spinner styleSpinner;

    private CheckBox wipeCheckbox;
    private CheckBox shuffleCheckbox;
    private CheckBox inverseCheckbox;
    private CheckBox qTypefaceCheckbox;
    private CheckBox aTypefaceCheckbox;
    private CheckBox field1Checkbox;
    private CheckBox field2Checkbox;
    private CheckBox htmlCheckbox;
    private CheckBox linebreakCheckbox;

    private EditText qTypefaceEdit;
    private EditText aTypefaceEdit;
    private EditText audioLocationEdit;
    private LinearLayout audioLocationLayout;
    private TableRow colorRow;
    private Spinner colorSpinner;
    private CheckBox colorCheckbox;
    private Button colorButton;

    private Setting setting;
    private CheckBox linebreakCheckbox;

    private ArrayList<Integer> colors;
    private long htmlValue = SettingManager.CardField.QUESTION | SettingManager.CardField.ANSWER
            | SettingManager.CardField.NOTE;
    private long field1Value = SettingManager.CardField.QUESTION;
    private long field2Value = SettingManager.CardField.ANSWER;

    private Button btnSave;
    private Button btnDiscard;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        InitTask initTask = new InitTask();
        initTask.execute((Void) null);
        // setContentView(R.layout.settings_screen);
        questionFontSizeSpinner = (Spinner) findViewById(R.id.question_font_size_spinner);
        ArrayAdapter<CharSequence> fontSizeAdapter = ArrayAdapter.createFromResource(this, R.array.font_size_list,
                android.R.layout.simple_spinner_item);
        fontSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbPath);
    }

    
    // ============================================================================
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

            linebreakCheckbox = (CheckBox) findViewById(R.id.linebreak_conversion);
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
            linebreakCheckbox.setChecked(setting.getHtmlLineBreakConversion());
            progressDialog.dismiss();
        }
    }

    // ==============================================================
    private class SaveBottomTask extends AsyncTask<Void, Void, Void> {
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
