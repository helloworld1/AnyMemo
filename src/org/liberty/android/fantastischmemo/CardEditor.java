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

import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;

import android.text.Editable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.gesture.GestureOverlayView;
import android.widget.Button;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.util.Log;
import android.os.SystemClock;
import android.os.Environment;
import android.content.res.Configuration;


public class CardEditor extends Activity implements View.OnClickListener{
    private final static String TAG = "org.liberty.android.fantastischmemo.CardEditor";
    private final int ACTIVITY_IMAGE_FILE = 1;
    private final int ACTIVITY_AUDIO_FILE = 2;
    private Item currentItem;
    private EditText questionEdit;
    private EditText answerEdit;
    private EditText categoryEdit;
    private Button btnSave;
    private Button btnCancel;
    private String dbName = null;
    private String dbPath = null;

	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_dialog);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			currentItem = (Item)extras.getSerializable("item");
			dbName = extras.getString("dbname");
			dbPath = extras.getString("dbpath");
		}
        questionEdit = (EditText)findViewById(R.id.edit_dialog_question_entry);
        answerEdit = (EditText)findViewById(R.id.edit_dialog_answer_entry);
        categoryEdit = (EditText)findViewById(R.id.edit_dialog_category_entry);
        btnSave = (Button)findViewById(R.id.edit_dialog_button_save);
        btnCancel = (Button)findViewById(R.id.edit_dialog_button_cancel);
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        questionEdit.setText(currentItem.getQuestion());
        answerEdit.setText(currentItem.getAnswer());
        categoryEdit.setText(currentItem.getCategory());
    }
    
    public void onClick(View v){
        if(v == btnSave){
            String qText = questionEdit.getText().toString();
            String aText = answerEdit.getText().toString();
            String cText = categoryEdit.getText().toString();
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("question", qText);
            hm.put("answer", aText);
            hm.put("category", cText);
            currentItem.setData(hm);
            try{
                DatabaseHelper dbHelper = new DatabaseHelper(this, dbPath, dbName);
                dbHelper.addOrReplaceItem(currentItem);
                dbHelper.close();
            }
            catch(Exception e){
                Log.e(TAG, "Error opending database when editing", e);
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, resultIntent);    			
                finish();
            }
            Intent resultIntent = new Intent();
        	setResult(Activity.RESULT_OK, resultIntent);    			
            finish();

        }
        else if(v == btnCancel){
            String qText = questionEdit.getText().toString();
            String aText = answerEdit.getText().toString();
            String cText = categoryEdit.getText().toString();
            if(!qText.equals(currentItem.getQuestion()) || !aText.equals(currentItem.getAnswer()) || !cText.equals(currentItem.getCategory())){
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
                if(focusView == questionEdit || focusView ==answerEdit || focusView == categoryEdit){
                    addTextToView((EditText)focusView, "<br />");
                }
                return true;
            case R.id.editor_menu_image:
                if(focusView == questionEdit || focusView ==answerEdit || focusView == categoryEdit){
                    Intent myIntent = new Intent(this, FileBrowser.class);
                    myIntent.putExtra("file_extension", ".png,.jpg,.tif");
                    startActivityForResult(myIntent, ACTIVITY_IMAGE_FILE);
                }
                return true;

            case R.id.editor_menu_audio:
                if(focusView == questionEdit || focusView ==answerEdit || focusView == categoryEdit){
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
                    if(focusView == questionEdit || focusView ==answerEdit || focusView == categoryEdit){
                        name = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                        path = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                        addTextToView((EditText)focusView, "<img src=\"" + name + "\" />");
                        /* Copy the image to correct location */
                        String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_image_dir) + "/" + dbName + "/";
                        new File(imagePath).mkdir();
                        try{
                            FileBrowser.copyFile(path + "/" + name, imagePath + name);
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
                    if(focusView == questionEdit || focusView ==answerEdit || focusView == categoryEdit){
                        name = data.getStringExtra("org.liberty.android.fantastischmemo.dbName");
                        path = data.getStringExtra("org.liberty.android.fantastischmemo.dbPath");
                        addTextToView((EditText)focusView, "<audio src=\"" + name + "\" />");
                        /* Copy the image to correct location */
                        String audioPath = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_audio_dir) + "/" + dbName + "/";
                        new File(audioPath).mkdir();
                        try{
                            FileBrowser.copyFile(path + "/" + name, audioPath + name);
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

}
