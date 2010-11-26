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
import android.widget.Toast;
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
    private AnyMemoTTS questionTTS = null;
    private AnyMemoTTS answerTTS = null;
    private final int DIALOG_LOADING_PROGRESS = 100;
    private final int ACTIVITY_FILTER = 10;
    private final int ACTIVITY_EDIT = 11;
    private final int ACTIVITY_CARD_TOOLBOX = 12;
    private final int ACTIVITY_DB_TOOLBOX = 13;
    private final int ACTIVITY_GOTO_PREV = 14;
    private final int ACTIVITY_SETTINGS = 15;

    Handler mHandler;
    Item currentItem = null;
    Item prevItem = null;
    String dbPath = "";
    String dbName = "";
    String activeFilter = "";
    FlashcardDisplay flashcardDisplay;
    SettingManager settingManager;
    ControlButtons controlButtons;
    QueueManager queueManager;


    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		setContentView(R.layout.memo_screen_layout);
        Bundle extras = getIntent().getExtras();
        mHandler = new Handler();
        if (extras != null) {
            dbPath = extras.getString("dbpath");
            dbName = extras.getString("dbname");
            activeFilter = extras.getString("filter");
        }
        try{
            settingManager = new SettingManager(this, dbPath, dbName);
            flashcardDisplay = new FlashcardDisplay(this, settingManager);
            if(settingManager.getButtonStyle() == SettingManager.ButtonStyle.ANKI){
            controlButtons = new AnkiGradeButtons(this);
            }
            else if(settingManager.getButtonStyle() == SettingManager.ButtonStyle.MNEMOSYNE){
                controlButtons = new MnemosyneGradeButtons(this);
            }
            else{
                controlButtons = new AnyMemoGradeButtons(this);
            }

            initTTS();


            composeViews();
            hideButtons();
            registerForContextMenu(flashcardDisplay.getView());
            /* Run the learnQueue init in a separate thread */
            createQueue();
            initQueue();
        }
        catch(Exception e){
            AMGUIUtility.displayError(this, getString(R.string.open_database_error_title), getString(R.string.open_database_error_message), e);
        }
    }

    void createQueue(){
        queueManager =new LearnQueueManager.Builder(this, dbPath, dbName)
            .setFilter(activeFilter)
            .setQueueSize(settingManager.getLearningQueueSize())
            .setShuffle(settingManager.getShufflingCards())
            .build();
    }

    void initQueue(){
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
                            setViewListeners();
                            updateFlashcardView(false);
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
                    answerTTS.sayText(currentItem.getAnswer());
                }
                return true;
            }

            case R.id.menusettings:
            {
                Intent myIntent = new Intent(this, SettingsScreen.class);
                myIntent.putExtra("dbname", dbName);
                myIntent.putExtra("dbpath", dbPath);
                startActivityForResult(myIntent, ACTIVITY_SETTINGS);
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
                undoItem();
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

    /* 
     * When the user select the undo from the menu
     * this is what to do
     */
    protected void undoItem(){
        if(prevItem != null){
            currentItem = prevItem.clone();
            queueManager.insertIntoQueue(currentItem, 0);
            prevItem = null;
            updateFlashcardView(false);
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
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.memoscreen_context_menu, menu);
        menu.setHeaderTitle(R.string.menu_text);
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
                Intent myIntent = new Intent(this, EditScreen.class);
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
                    updateFlashcardView(false);
                    queueManager.updateQueueItem(currentItem);
                }
                break;
            }
            case ACTIVITY_GOTO_PREV:
            {
                restartActivity();
                break;
            }

            case ACTIVITY_SETTINGS:
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

    void updateFlashcardView(boolean showAnswer){
        flashcardDisplay.updateView(currentItem, showAnswer);
        setActivityTitle();
        setGradeButtonTitle();
        setGradeButtonListeners();
    }


    void setViewListeners(){
        View.OnClickListener showAnswerListener = new View.OnClickListener(){
            public void onClick(View v){
                if(currentItem != null){
                    showButtons();
                    updateFlashcardView(true);
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

                    if(!flashcardDisplay.isAnswerShown()){
                        updateFlashcardView(true);
                        showButtons();
                    }
                    else{
                        answerTTS.sayText(currentItem.getAnswer());
                    }
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
            flashcardDisplay.setAnswerTextClickListener(speakAnswerListener);
        }
        else{
            flashcardDisplay.setQuestionTextClickListener(showAnswerListener);
            flashcardDisplay.setAnswerTextClickListener(showAnswerListener);
        }
    }

    void setGradeButtonTitle(){
        Map<String, Button> hm = controlButtons.getButtons();
        if(settingManager.getButtonStyle() == SettingManager.ButtonStyle.MNEMOSYNE){
            hm.get("0").setText(getString(R.string.memo_btn0_brief_text));
            hm.get("1").setText(getString(R.string.memo_btn1_brief_text));
            hm.get("2").setText(getString(R.string.memo_btn2_brief_text));
            hm.get("3").setText(getString(R.string.memo_btn3_brief_text));
            hm.get("4").setText(getString(R.string.memo_btn4_brief_text));
            hm.get("5").setText(getString(R.string.memo_btn5_brief_text));
        }
        else if(settingManager.getButtonStyle() == SettingManager.ButtonStyle.ANKI){
            hm.get("0").setText(getString(R.string.memo_btn0_anki_text) + "\n+" + currentItem.processAnswer(0, true));
            hm.get("1").setText(getString(R.string.memo_btn1_anki_text) + "\n+" + currentItem.processAnswer(1, true));
            hm.get("2").setText(getString(R.string.memo_btn2_anki_text) + "\n+" + currentItem.processAnswer(2, true));
            hm.get("3").setText(getString(R.string.memo_btn3_anki_text) + "\n+" + currentItem.processAnswer(3, true));
            hm.get("4").setText(getString(R.string.memo_btn4_anki_text) + "\n+" + currentItem.processAnswer(4, true));
            hm.get("5").setText(getString(R.string.memo_btn5_anki_text) + "\n+" + currentItem.processAnswer(5, true));
        }
        else{
            hm.get("0").setText(getString(R.string.memo_btn0_text) + "\n+" + currentItem.processAnswer(0, true));
            hm.get("1").setText(getString(R.string.memo_btn1_text) + "\n+" + currentItem.processAnswer(1, true));
            hm.get("2").setText(getString(R.string.memo_btn2_text) + "\n+" + currentItem.processAnswer(2, true));
            hm.get("3").setText(getString(R.string.memo_btn3_text) + "\n+" + currentItem.processAnswer(3, true));
            hm.get("4").setText(getString(R.string.memo_btn4_text) + "\n+" + currentItem.processAnswer(4, true));
            hm.get("5").setText(getString(R.string.memo_btn5_text) + "\n+" + currentItem.processAnswer(5, true));

        }
    }
    

    void setGradeButtonListeners(){
        Map<String, Button> hm = controlButtons.getButtons();
        for(int i = 0; i < 6; i++){
            Button b = hm.get(Integer.valueOf(i).toString());
            b.setOnClickListener(getGradeButtonListener(i));
            b.setOnLongClickListener(getGradeButtonLongClickListener(i));
        }
    }

    void setActivityTitle(){
        int[] stat = queueManager.getStatInfo();
        setTitle(getString(R.string.stat_new) + stat[0] + " " + getString(R.string.stat_scheduled) + stat[1] + " " + getString(R.string.memo_current_id) + currentItem.getId());
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
                    updateFlashcardView(false);
                    hideButtons();
                }
            }
        };
    }

    private View.OnLongClickListener getGradeButtonLongClickListener(final int grade){
        return new View.OnLongClickListener(){
            public boolean onLongClick(View v){
                String[] helpText = {getString(R.string.memo_btn0_help_text),getString(R.string.memo_btn1_help_text),getString(R.string.memo_btn2_help_text),getString(R.string.memo_btn3_help_text),getString(R.string.memo_btn4_help_text),getString(R.string.memo_btn5_help_text)};
                Toast.makeText(MemoScreen.this, helpText[grade], Toast.LENGTH_SHORT).show();
                return true;
            }
        };
    }

    private void hideButtons(){
        controlButtons.getView().setVisibility(View.INVISIBLE);
    }

    private void showButtons(){
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

    @Override
    public void restartActivity(){
        Intent myIntent = new Intent(this, MemoScreen.class);
        myIntent.putExtra("dbname", dbName);
        myIntent.putExtra("dbpath", dbPath);
        myIntent.putExtra("filter", activeFilter);

        finish();
        startActivity(myIntent);
    }

    void showNoItemDialog(){
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
                    myIntent.setClass(MemoScreen.this, CramMemoScreen.class);
                    myIntent.putExtra("dbname", dbName);
                    myIntent.putExtra("dbpath", dbPath);
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


