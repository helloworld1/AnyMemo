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
package org.liberty.android.fantastischmemo.ui;

import java.sql.SQLException;

import java.util.HashMap;
import java.io.File;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMUtil;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.DatabaseHelper;
import org.liberty.android.fantastischmemo.FileBrowser;
import org.liberty.android.fantastischmemo.Item;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Option;

import org.liberty.android.fantastischmemo.ui.EditScreen;
import org.liberty.android.fantastischmemo.ui.EditScreen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.util.Log;
import android.os.Environment;
import android.content.res.Configuration;

public class CardEditor extends AMActivity implements View.OnClickListener{
    private final int ACTIVITY_IMAGE_FILE = 1;
    private final int ACTIVITY_AUDIO_FILE = 2;
    Card currentCard = null;
    private Integer currentCardId;
    private EditText questionEdit;
    private EditText answerEdit;
    private Button categoryButton;
    private EditText noteEdit;
    private RadioGroup addRadio;
    private boolean addBack = true;
    private boolean isEditNew = false;
    private Button btnSave;
    private Button btnCancel;
    private String dbName = null;
    String dbPath = null;
    private LearningDataDao learningDataDao;
    CardDao cardDao;
    CategoryDao categoryDao;
    private InitTask initTask;

    private String originalQuestion;
    private String originalAnswer;
    private String originalNote;


    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.edit_dialog);
        initTask = new InitTask();
        initTask.execute((Void)null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbPath);
    }
        
    
    public void onClick(View v){
        if(v == btnSave){
            String qText = questionEdit.getText().toString();
            String aText = answerEdit.getText().toString();
            String nText = noteEdit.getText().toString();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("id", currentCard.getId());
        	setResult(Activity.RESULT_OK, resultIntent);    			
            finish();
        } 
        
        if(v == btnCancel){
            String qText = questionEdit.getText().toString();
            String aText = answerEdit.getText().toString();
            String nText = noteEdit.getText().toString();
            if (!isEditNew && (!qText.equals(originalQuestion) || !aText.equals(originalAnswer) || !nText.equals(originalNote))) {
                new AlertDialog.Builder(this)
                    .setTitle(R.string.warning_text)
                    .setMessage(R.string.edit_dialog_unsave_warning)
                    .setPositiveButton(R.string.yes_text, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface  d, int which){
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_CANCELED, resultIntent);    			
                            finish();

                        }
                    }) 
                    .setNegativeButton(R.string.no_text, null)
                    .create()
                    .show();
                    
            }
            else{
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);    			
                finish();

            }
        }

        if (v == categoryButton) {
            DialogFragment df = new CategoryEditorFragment();
            df.show(getSupportFragmentManager(), "CategoryEditDialog");
        }
    }

        
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.card_editor_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
        View focusView = getCurrentFocus();
	    switch (item.getItemId()) {
            case R.id.editor_menu_br:
                if(focusView == questionEdit || focusView ==answerEdit || focusView == noteEdit){
                    addTextToView((EditText)focusView, "<br />");
                }
                return true;
            case R.id.editor_menu_image:
                if(focusView == questionEdit || focusView ==answerEdit || focusView == noteEdit){
                    Intent myIntent = new Intent(this, FileBrowser.class);
                    myIntent.putExtra("file_extension", ".png,.jpg,.tif,.bmp");
                    startActivityForResult(myIntent, ACTIVITY_IMAGE_FILE);
                }
                return true;

            case R.id.editor_menu_audio:
                if(focusView == questionEdit || focusView ==answerEdit || focusView == noteEdit){
                    Intent myIntent = new Intent(this, FileBrowser.class);
                    myIntent.putExtra("file_extension", ".ogg,.mp3,.wav");
                    startActivityForResult(myIntent, ACTIVITY_AUDIO_FILE);
                }
                return true;

            }
        return false;
    }

    private void addTextToView(EditText v, String text){
        String origText = v.getText().toString();
        /* 
         * keep track of the cursor location and restore it 
         * after pasting because the default location is the 
         * begining of the EditText
         */
        int cursorPos = v.getSelectionStart();
        try{
            String newText = origText.substring(0, cursorPos) + text + origText.substring(cursorPos, origText.length());

            v.setText(newText);
            v.setSelection(cursorPos + text.length());

        }
        catch(Exception e){
            Log.v(TAG, "cursor position is wrong", e);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
        String fontFilename = null;
        String name, path;
    	switch(requestCode){
    	    case ACTIVITY_IMAGE_FILE:
                if(resultCode == Activity.RESULT_OK){
                    View focusView = getCurrentFocus();
                    if(focusView == questionEdit || focusView ==answerEdit || focusView == noteEdit){
                        name = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                        path = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                        addTextToView((EditText)focusView, "<img src=\"" + name + "\" />");
                        /* Copy the image to correct location */
                        String imageRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_image_dir) + "/";
                        String imagePath = imageRoot + dbName + "/";
                        new File(imageRoot).mkdir();
                        new File(imagePath).mkdir();
                        try{
                            String target = imagePath + name;
                            if(!(new File(target)).exists()){
                                FileBrowser.copyFile(path + "/" + name, target);
                            }
                        }
                        catch(Exception e){
                            Log.e(TAG, "Error copying image", e);
                        }
                    }
                }
            break;
    	    case ACTIVITY_AUDIO_FILE:
                if(resultCode == Activity.RESULT_OK){
                    View focusView = getCurrentFocus();
                    if(focusView == questionEdit || focusView ==answerEdit || focusView == noteEdit){
                        name = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                        path = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                        addTextToView((EditText)focusView, "<audio src=\"" + name + "\" />");
                        /* Copy the image to correct location */
                        String audioRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_audio_dir) + "/";
                        String audioPath = audioRoot + dbName + "/";
                        new File(audioRoot).mkdir();
                        new File(audioPath).mkdir();
                        try{
                            String target = audioPath + name;
                            if(!(new File(target)).exists()){
                                FileBrowser.copyFile(path + "/" + name, audioPath + name);
                            }
                        }
                        catch(Exception e){
                            Log.e(TAG, "Error copying audio", e);
                        }
                    }
                }
            break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void setInitRadioButton(){
        if(!isEditNew){
            addRadio.setVisibility(View.GONE);
        }
        else{
            /* 
             * The radio button is only valid when the user is creating 
             * new items. If the user is editng, it has no effect at all
             */
            addRadio.setVisibility(View.VISIBLE);
            final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            addBack = settings.getBoolean("add_back", true);
            if(addBack){
                addRadio.check(R.id.add_back_radio);
            }
            else{
                addRadio.check(R.id.add_here_radio);
            }
            RadioGroup.OnCheckedChangeListener changeListener = new RadioGroup.OnCheckedChangeListener(){
                public void onCheckedChanged(RadioGroup group, int checkedId){
        	        SharedPreferences.Editor editor = settings.edit();
                    if(checkedId == R.id.add_here_radio){
                        addBack = false;
                        editor.putBoolean("add_back", false);
                        editor.commit();
                    }
                    else{
                        addBack = true;
                        editor.putBoolean("add_back", true);
                        editor.commit();
                    }
                }
            };
            addRadio.setOnCheckedChangeListener(changeListener);
        }
    }

    private class InitTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

		@Override
        public void onPreExecute() {
            setTitle(R.string.memo_edit_dialog_title);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                currentCardId = extras.getInt("id");
                dbPath = extras.getString("dbpath");
                isEditNew = extras.getBoolean("is_edit_new");
            }

            progressDialog = new ProgressDialog(CardEditor.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
            public Void doInBackground(Void... params) {
                try {
                    AnyMemoDBOpenHelper helper =
                        AnyMemoDBOpenHelperManager.getHelper(CardEditor.this, dbPath);

                    cardDao = helper.getCardDao();
                    learningDataDao = helper.getLearningDataDao();
                    categoryDao = helper.getCategoryDao();
                    currentCard = cardDao.queryForId(currentCardId);
                    assert currentCard != null : "Try to edit null card!";
                    categoryDao.refresh(currentCard.getCategory());

                } catch (SQLException e) {
                    Log.e(TAG, "Error creating daos", e);
                    throw new RuntimeException("Dao creation error");
                }
                return null;
            }

        @Override
        public void onPostExecute(Void result){
            // It means empty set
            questionEdit = (EditText)findViewById(R.id.edit_dialog_question_entry);
            answerEdit = (EditText)findViewById(R.id.edit_dialog_answer_entry);
            categoryButton = (Button)findViewById(R.id.edit_dialog_category_button);
            noteEdit = (EditText)findViewById(R.id.edit_dialog_note_entry);
            btnSave = (Button)findViewById(R.id.edit_dialog_button_save);
            btnCancel = (Button)findViewById(R.id.edit_dialog_button_cancel);
            addRadio = (RadioGroup)findViewById(R.id.add_radio);
            btnSave.setOnClickListener(CardEditor.this);
            btnCancel.setOnClickListener(CardEditor.this);
            categoryButton.setOnClickListener(CardEditor.this);

            updateViews();

            /* Should be called after the private fields are inited */
            setInitRadioButton();
            progressDialog.dismiss();
        }
    }

    void updateViews() {
        /* Retain the last category when editing new */
        String categoryName = currentCard.getCategory().getName();
        if (categoryName.equals("")) {
            categoryButton.setText(R.string.uncategorized_text);
        } else {
            categoryButton.setText(categoryName);
        }
        /* Prefill the note if it is empty */

        if(isEditNew){
            /* Use this one or the one below ?*/
            noteEdit.setText(currentCard.getNote());
        }
        if(!isEditNew){
            originalQuestion = currentCard.getQuestion();
            originalAnswer = currentCard.getAnswer();
            originalNote = currentCard.getNote();
            questionEdit.setText(originalQuestion);
            answerEdit.setText(originalAnswer);
            noteEdit.setText(originalNote);
        }
    }

}
