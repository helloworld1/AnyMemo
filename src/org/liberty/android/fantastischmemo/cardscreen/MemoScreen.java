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
import org.liberty.android.fantastischmemo.tts.*;

import org.amr.arabic.ArabicUtilities;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.ContextMenu;
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
import android.graphics.Typeface;
import android.text.Html.TagHandler;
import android.text.Html.ImageGetter;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;
import android.net.Uri;

public class MemoScreen extends AMActivity{
    private final static String TAG = "org.liberty.android.fantastischmemo.cardscreen.MemoScreen";
    private String dbPath = "";
    private String dbName = "";
    private String activeFilter = "";
    private FlashcardDisplay flashcardDisplay;
    private ControlButtons controlButtons;
    private SettingManager settingManager;
    private AnyMemoTTS questionTTS = null;
    private AnyMemoTTS answerTTS = null;
    private Item currentItem = null;
    private Item prevItem = null;
    private ItemQueueManager queueManager;
    private Handler mHandler;
    private final int DIALOG_LOADING_PROGRESS = 100;
    private final int ACTIVITY_FILTER = 10;
    private final int ACTIVITY_EDIT = 11;
    private final int ACTIVITY_CARD_TOOLBOX = 12;
    private final int ACTIVITY_DB_TOOLBOX = 13;
    private final int ACTIVITY_GOTO_PREV = 14;


    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		setContentView(R.layout.memo_screen_layout);
        Bundle extras = getIntent().getExtras();
        mHandler = new Handler();
        if (extras != null) {
            dbPath = extras.getString("dbpath");
            dbName = extras.getString("dbname");
            activeFilter = extras.getString("active_filter");
        }
        settingManager = new SettingManager(this, dbPath, dbName);
        flashcardDisplay = new FlashcardDisplay(this, settingManager);
        controlButtons = new AnyMemoGradeButtons(this);
        initTTS();
        queueManager = new ItemQueueManager(this, dbPath, dbName);
        queueManager.setFilter(activeFilter);

        composeViews();
        hideButtons();
        registerForContextMenu(flashcardDisplay.getView());
        /* Run the learnQueue init in a separate thread */
        showDialog(DIALOG_LOADING_PROGRESS);
        new Thread(){
            public void run(){
                queueManager.initQueue();
                currentItem = queueManager.updateAndNext(null);
                mHandler.post(new Runnable(){
                    public void run(){
                        if(currentItem == null){
                            showNoItemDialog();
                        }
                        else{
                            flashcardDisplay.updateView(currentItem, false);
                            setListeners();
                        }
                        removeDialog(DIALOG_LOADING_PROGRESS);
                    }
                });
            }
        }.start();
    }

    @Override
    public void onDestroy(){
        if(settingManager != null){
            settingManager.close();
            settingManager = null;
        }
        if(questionTTS != null){
            questionTTS.shutdown();
        }
        if(answerTTS != null){
            answerTTS.shutdown();
        }
        if(queueManager != null){
            queueManager.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.memo_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_memo_help:
            {
                Intent myIntent = new Intent();
                myIntent.setAction(Intent.ACTION_VIEW);
                myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                myIntent.setData(Uri.parse(getString(R.string.website_help_memo)));
                startActivity(myIntent);
                return true;
            }
            case R.id.menuspeakquestion:
            {
                if(questionTTS != null && currentItem != null){
                    questionTTS.sayText(currentItem.getQuestion());
                }
                return true;
            }

            case R.id.menuspeakanswer:
            {
                if(answerTTS != null && currentItem != null){
                    answerTTS.sayText(currentItem.getQuestion());
                }
                return true;
            }

            case R.id.menusettings:
            {
                Intent myIntent = new Intent(this, SettingsScreen.class);
                myIntent.putExtra("dbname", dbName);
                myIntent.putExtra("dbpath", dbPath);
                startActivityForResult(myIntent, 1);
                return true;
            }

            case R.id.menudetail:
            {
                Intent myIntent = new Intent(this, DetailScreen.class);
                myIntent.putExtra("dbname", this.dbName);
                myIntent.putExtra("dbpath", this.dbPath);
                myIntent.putExtra("itemid", currentItem.getId());
                startActivityForResult(myIntent, 2);
                return true;
            }

            case R.id.menuundo:
            {
                if(prevItem != null){
                    currentItem = prevItem.clone();
                    queueManager.insertIntoQueue(currentItem, 0);
                    prevItem = null;
                    flashcardDisplay.updateView(currentItem, false);
                    hideButtons();
                }
                else{
                    new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.undo_fail_text))
                        .setMessage(getString(R.string.undo_fail_message))
                        .setNeutralButton(R.string.ok_text, null)
                        .create()
                        .show();
                }
                return true;
            }

            case R.id.menu_memo_filter:
            {
                Intent myIntent = new Intent(this, Filter.class);
                myIntent.putExtra("dbname", dbName);
                myIntent.putExtra("dbpath", dbPath);
                startActivityForResult(myIntent, ACTIVITY_FILTER);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.memoscreen_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuitem) {
        switch(menuitem.getItemId()) {
            case R.id.menu_context_edit:
            {
                Intent myIntent = new Intent(this, CardEditor.class);
                myIntent.putExtra("dbname", this.dbName);
                myIntent.putExtra("dbpath", this.dbPath);
                myIntent.putExtra("item", currentItem);
                startActivityForResult(myIntent, ACTIVITY_EDIT);
                return true;
            }
            case R.id.menu_context_gotoprev:
            {
                Intent myIntent = new Intent(this, CardEditor.class);
                myIntent.putExtra("dbname", this.dbName);
                myIntent.putExtra("dbpath", this.dbPath);
                myIntent.putExtra("id", currentItem.getId());
                startActivityForResult(myIntent, ACTIVITY_GOTO_PREV);
                return true;
            }

            default:
            {
                return super.onContextItemSelected(menuitem);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode ==Activity.RESULT_CANCELED){
            return;
        }
        switch(requestCode){
            case ACTIVITY_FILTER:
            {
                Bundle extras = data.getExtras();
                activeFilter = extras.getString("filter");
                restartActivity();
                break;
            }
            case ACTIVITY_EDIT:
            {
                Bundle extras = data.getExtras();
                Item item = (Item)extras.getSerializable("item");
                if(item != null){
                    currentItem = item;
                    flashcardDisplay.updateView(currentItem, false);
                    queueManager.updateQueueItem(currentItem);
                }
                break;
            }
            case ACTIVITY_GOTO_PREV:
            {
                restartActivity();
                break;
            }
        }
    }



    @Override
    public Dialog onCreateDialog(int id){
        switch(id){
            case DIALOG_LOADING_PROGRESS:{
                ProgressDialog progressDialog = new ProgressDialog(MemoScreen.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle(getString(R.string.loading_please_wait));
                progressDialog.setMessage(getString(R.string.loading_database));
                progressDialog.setCancelable(false);

                return progressDialog;
            }
            default:
                return super.onCreateDialog(id);

        }
    }

    private void setListeners(){
        View.OnClickListener showAnswerListener = new View.OnClickListener(){
            public void onClick(View v){
                if(currentItem != null){
                    showButtons();
                    flashcardDisplay.updateView(currentItem, true);
                    controlButtons.getView().setVisibility(View.VISIBLE);
                }
            }
        };
        View.OnClickListener speakQuestionListener = new View.OnClickListener(){
            public void onClick(View v){
                if(currentItem != null){
                    questionTTS.sayText(currentItem.getQuestion());
                }
            }
        };
        View.OnClickListener speakAnswerListener = new View.OnClickListener(){
            public void onClick(View v){
                if(currentItem != null){
                    answerTTS.sayText(currentItem.getAnswer());
                }
            }
        };
        View.OnLongClickListener openContextMenuListener = new View.OnLongClickListener(){
            public boolean onLongClick(View v){
                MemoScreen.this.openContextMenu(flashcardDisplay.getView());
                Log.v(TAG, "Open Menu!");
                return true;
            }
        };
        flashcardDisplay.setQuestionLayoutLongClickListener(openContextMenuListener);
        flashcardDisplay.setAnswerLayoutLongClickListener(openContextMenuListener);

        flashcardDisplay.setQuestionLayoutClickListener(showAnswerListener);
        flashcardDisplay.setAnswerLayoutClickListener(showAnswerListener);
        if(settingManager.getSpeechControlMethod() == SettingManager.SpeechControlMethod.TAP || settingManager.getSpeechControlMethod() == SettingManager.SpeechControlMethod.AUTOTAP){
            flashcardDisplay.setQuestionTextClickListener(speakQuestionListener);
            flashcardDisplay.setAnswerLayoutClickListener(speakAnswerListener);
        }
        else{
            flashcardDisplay.setQuestionTextClickListener(showAnswerListener);
            flashcardDisplay.setAnswerTextClickListener(showAnswerListener);
        }
        Map<String, Button> hm = controlButtons.getButtons();
        for(int i = 0; i < 6; i++){
            Button b = hm.get(Integer.valueOf(i).toString());
            b.setOnClickListener(getGradeButtonListener(i));
            b.setText(Integer.valueOf(i).toString() + " +" + currentItem.processAnswer(i, true));
        }
    }

    private View.OnClickListener getGradeButtonListener(final int grade){
        return new View.OnClickListener(){
            public void onClick(View v){
                prevItem = currentItem.clone();
                currentItem.processAnswer(grade, false);
                currentItem = queueManager.updateAndNext(currentItem);
                if(currentItem == null){
                    showNoItemDialog();
                }
                else{
                    flashcardDisplay.updateView(currentItem, false);
                    hideButtons();
                    setTitle("" + currentItem.getId());
                }
            }
        };
    }

    private void hideButtons(){
        flashcardDisplay.updateView(currentItem, false);
        controlButtons.getView().setVisibility(View.INVISIBLE);
    }

    private void showButtons(){
        flashcardDisplay.updateView(currentItem, false);
        controlButtons.getView().setVisibility(View.VISIBLE);
    }


    private void composeViews(){
        LinearLayout memoRoot = (LinearLayout)findViewById(R.id.memo_screen_root);

        LinearLayout flashcardDisplayView = (LinearLayout)flashcardDisplay.getView();
        LinearLayout controlButtonsView = (LinearLayout)controlButtons.getView();

        /* 
         * -1: Match parent -2: Wrap content
         * This is necessary or the view will not be 
         * stetched
         */
        memoRoot.addView(flashcardDisplayView, -1, -1);
        memoRoot.addView(controlButtonsView, -1, -2);
        flashcardDisplayView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
    }

    private void restartActivity(){
        Intent myIntent = new Intent(this, MemoScreen.class);
        myIntent.putExtra("dbname", dbName);
        myIntent.putExtra("dbpath", dbPath);
        myIntent.putExtra("active_filter", activeFilter);

        finish();
        startActivity(myIntent);
    }

    private void showNoItemDialog(){
        new AlertDialog.Builder(this)
            .setTitle(this.getString(R.string.memo_no_item_title))
            .setMessage(this.getString(R.string.memo_no_item_message))
            .setNeutralButton(getString(R.string.back_menu_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    /* Finish the current activity and go back to the last activity.
                     * It should be the open screen. */
                    finish();
                    }
                })
            .setNegativeButton(getString(R.string.learn_ahead), new OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    finish();
                    Intent myIntent = new Intent();
                    myIntent.setClass(MemoScreen.this, MemoScreen.class);
                    myIntent.putExtra("dbname", dbName);
                    myIntent.putExtra("dbpath", dbPath);
                    myIntent.putExtra("learn_ahead", true);
                    startActivity(myIntent);
                }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener(){
                public void onCancel(DialogInterface dialog){
                    finish();
                    }
                })
            .create()
            .show();
    }

    private void initTTS(){
        String audioDir = Environment.getExternalStorageDirectory().getAbsolutePath() + getString(R.string.default_audio_dir);
        Locale ql = settingManager.getQuestionAudioLocale();
        Locale al = settingManager.getAnswerAudioLocale();
        if(settingManager.getQuestionUserAudio()){
            questionTTS = new AudioFileTTS(audioDir, dbName);
        }
        else if(ql != null){
            if(settingManager.getEnableTTSExtended()){
                questionTTS = new AnyMemoTTSExtended(this, ql);
            }
            else{
                questionTTS = new AnyMemoTTSPlatform(this, ql);
            }
        }
        else{
            questionTTS = null;
        }
        if(settingManager.getAnswerUserAudio()){
            answerTTS = new AudioFileTTS(audioDir, dbName);
        }
        else if(al != null){
            if(settingManager.getEnableTTSExtended()){
                answerTTS = new AnyMemoTTSExtended(this, al);
            }
            else{
                answerTTS = new AnyMemoTTSPlatform(this, al);
            }
        }
        else{
            answerTTS = null;
        }
    }

}


