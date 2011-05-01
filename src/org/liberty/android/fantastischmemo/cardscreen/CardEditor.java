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
package org.liberty.android.fantastischmemo.cardscreen;

import org.liberty.android.fantastischmemo.*;

import java.util.HashMap;
import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.util.Log;
import android.os.Environment;
import android.content.res.Configuration;


public class CardEditor extends Activity implements View.OnClickListener{
    private final static String TAG = "org.liberty.android.fantastischmemo.CardEditor";
    private final int ACTIVITY_IMAGE_FILE = 1;
    private final int ACTIVITY_AUDIO_FILE = 2;
    private Item oldItem = null;
    private EditText questionEdit;
    private EditText answerEdit;
    private EditText categoryEdit;
    private EditText noteEdit;
    private RadioGroup addRadio;
    private boolean addBack = true;
    private Button btnSave;
    private Button btnCancel;
    private String dbName = null;
    private String dbPath = null;
    private boolean isEditNew = true;

	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_dialog);
        setTitle(R.string.memo_edit_dialog_title);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			oldItem = (Item)extras.getParcelable("item");
			dbName = extras.getString("dbname");
			dbPath = extras.getString("dbpath");
            isEditNew = extras.getBoolean("new", false);
		}
        questionEdit = (EditText)findViewById(R.id.edit_dialog_question_entry);
        answerEdit = (EditText)findViewById(R.id.edit_dialog_answer_entry);
        categoryEdit = (EditText)findViewById(R.id.edit_dialog_category_entry);
        noteEdit = (EditText)findViewById(R.id.edit_dialog_note_entry);
        btnSave = (Button)findViewById(R.id.edit_dialog_button_save);
        btnCancel = (Button)findViewById(R.id.edit_dialog_button_cancel);
        addRadio = (RadioGroup)findViewById(R.id.add_radio);
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        if(oldItem == null){
            /* Use default parameters for new item */
            oldItem = new Item.Builder()
                .setId(0)
                .build();
        }

        /* Retain the last category when editing new */
        categoryEdit.setText(oldItem.getCategory());
        /* Prefill the note if it is empty */

        if(isEditNew){
            /* Use this one or the one below ?*/
            noteEdit.setText(oldItem.getNote());
            /*
            String dt = SimpleDateFormat.getDateTimeInstance().format(new Date());
            noteEdit.setText(dt); */
        }
        if(!isEditNew){
            questionEdit.setText(oldItem.getQuestion());
            answerEdit.setText(oldItem.getAnswer());
            noteEdit.setText(oldItem.getNote());
        }
        /* Should be called after the private fields are inited */
        setInitRadioButton();

    }
    
    public void onClick(View v){
        if(v == btnSave){
            String qText = questionEdit.getText().toString();
            String aText = answerEdit.getText().toString();
            String cText = categoryEdit.getText().toString();
            String nText = noteEdit.getText().toString();
            HashMap<String, String> hm = new HashMap<String, String>();
            Item currentItem = null;
            try{
                DatabaseHelper dbHelper = new DatabaseHelper(this, dbPath, dbName);
                /* Here we check if the item is newly created */
                if(isEditNew){
                    int newId;
                    if(addBack){
                        newId = dbHelper.getNewId();
                    }
                    else{
                        newId = oldItem.getId() + 1;
                    }
                    currentItem = new Item.Builder()
                        .setId(newId)
                        .setQuestion(qText)
                        .setAnswer(aText)
                        .setCategory(cText)
                        .setNote(nText)
                        .build();
                    dbHelper.insertItem(currentItem, newId - 1);
                }
                else{
                    currentItem = new Item.Builder(oldItem)
                        .setQuestion(qText)
                        .setAnswer(aText)
                        .setCategory(cText)
                        .setNote(nText)
                        .build();
                    dbHelper.addOrReplaceItem(currentItem);
                }
                dbHelper.close();
            }
            catch(Exception e){
                Log.e(TAG, "Error opending database when editing", e);
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);    			
                finish();
            }
            Intent resultIntent = new Intent();
            resultIntent.putExtra("item", currentItem);
        	setResult(Activity.RESULT_OK, resultIntent);    			
            finish();
        }
        else if(v == btnCancel){
            String qText = questionEdit.getText().toString();
            String aText = answerEdit.getText().toString();
            String cText = categoryEdit.getText().toString();
            if(!isEditNew && (!qText.equals(oldItem.getQuestion()) || !aText.equals(oldItem.getAnswer()) || !cText.equals(oldItem.getCategory()))){
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
                if(focusView == questionEdit || focusView ==answerEdit || focusView == categoryEdit || focusView == noteEdit){
                    addTextToView((EditText)focusView, "<br />");
                }
                return true;
            case R.id.editor_menu_image:
                if(focusView == questionEdit || focusView ==answerEdit || focusView == categoryEdit || focusView == noteEdit){
                    Intent myIntent = new Intent(this, FileBrowser.class);
                    myIntent.putExtra("file_extension", ".png,.jpg,.tif,.bmp");
                    startActivityForResult(myIntent, ACTIVITY_IMAGE_FILE);
                }
                return true;

            case R.id.editor_menu_audio:
                if(focusView == questionEdit || focusView ==answerEdit || focusView == categoryEdit || focusView == noteEdit){
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
                    if(focusView == questionEdit || focusView ==answerEdit || focusView == categoryEdit || focusView == noteEdit){
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
                    if(focusView == questionEdit || focusView ==answerEdit || focusView == categoryEdit || focusView == noteEdit){
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

}
