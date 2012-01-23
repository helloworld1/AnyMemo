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
import com.example.android.apis.graphics.*;

import java.util.Locale;
import java.util.Map;
import java.util.List;

import android.os.AsyncTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.KeyEvent;
import android.widget.Button;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import android.util.Log;
import android.net.Uri;

public class MemoScreen extends AMActivity{
    private final static String TAG = "org.liberty.android.fantastischmemo.cardscreen.MemoScreen";
    private AnyMemoTTS questionTTS = null;
    private AnyMemoTTS answerTTS = null;
    private final int DIALOG_LOADING_PROGRESS = 100;
    private final int ACTIVITY_FILTER = 10;
    private final int ACTIVITY_EDIT = 11;
    private final int ACTIVITY_GOTO_PREV = 14;
    private final int ACTIVITY_SETTINGS = 15;
    private final int ACTIVITY_DETAIL = 16;
    private final static String WEBSITE_HELP_MEMO="http://anymemo.org/wiki/index.php?title=Learning_screen";

    /* This is useful to determine which view is click or long clicked */
    private View activeView = null;


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
    volatile boolean buttonDisabled = false;
    /* Maintain this task for the exiting sync purpose*/
    BackgroundUpdateTask bgUpdateTask = null;


    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        /* .. so we can display progress on the title bar */
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

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
            if(settingManager.getCardStyle() == SettingManager.CardStyle.DOUBLE_SIDED){
                flashcardDisplay = new DoubleSidedCardDisplay(this, settingManager);
            }
            else{
                flashcardDisplay = new SingleSidedCardDisplay(this, settingManager);
            }

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
                            setTitle(getActivityTitleString());
                        }
                        removeDialog(DIALOG_LOADING_PROGRESS);
                    }
                });
            }
        }.start();
    }


    @Override
    public void onPause(){
        /* Spin the UI thread to wait for the current task done */
        if(bgUpdateTask != null){
            bgUpdateTask.cancel(false);
        }
        super.onPause();
    }

    @Override
    public void onDestroy(){
        Log.v(TAG, "onDestroy now!");
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
        /* Update the widget because MemoScreen can be accessed though widget*/
        Intent myIntent = new Intent(this, AnyMemoService.class);
        myIntent.putExtra("request_code", AnyMemoService.CANCEL_NOTIFICATION | AnyMemoService.UPDATE_WIDGET);
        startService(myIntent);
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
                myIntent.setData(Uri.parse(WEBSITE_HELP_MEMO));
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
                startActivityForResult(myIntent, ACTIVITY_DETAIL);
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
            currentItem = prevItem;
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
            case R.id.menu_context_delete:
            {
                if(currentItem != null){
                    /* DatabaseUtility is only one time used so it is created
                     * locally here */
                    DatabaseUtility du = new DatabaseUtility(this, dbPath, dbName);
                    du.deleteItemFromDb(currentItem);
                }
                return true;

            }
            case R.id.menu_context_skip:
            {
                DatabaseUtility du = new DatabaseUtility(this, dbPath, dbName);
                du.skipItemFromDb(currentItem);
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

            case R.id.menu_context_lookup:
            {
                if(currentItem == null){
                    return false;
                }
                /* default word to lookup is question */
                String lookupWord = currentItem.getQuestion();

                if(flashcardDisplay.getAnswerView() == activeView){
                    lookupWord = currentItem.getAnswer();
                }
                    

                if(settingManager.getDictApp() == SettingManager.DictApp.COLORDICT){
                    System.out.println("Get COLORDICT");
                    Intent intent = new Intent("colordict.intent.action.SEARCH");
                    intent.putExtra("EXTRA_QUERY", lookupWord);
                    intent.putExtra("EXTRA_FULLSCREEN", false);
                    //intent.putExtra(EXTRA_HEIGHT, 400); //400pixel, if you don't specify, fill_parent"
                    intent.putExtra("EXTRA_GRAVITY", Gravity.BOTTOM);
                    //intent.putExtra(EXTRA_MARGIN_LEFT, 100);
                    try{
                        startActivity(intent);
                    }
                    catch(Exception e){
                        Log.e(TAG, "Error opening ColorDict", e);
                        AMGUIUtility.displayException(this, getString(R.string.error_text), getString(R.string.dict_colordict) + " " + getString(R.string.error_no_dict), e);
                    }
                }
                if(settingManager.getDictApp() == SettingManager.DictApp.FORA){
                    System.out.println("Get FORA");
                    Intent intent = new Intent("com.ngc.fora.action.LOOKUP");
                    intent.putExtra("HEADWORD", lookupWord);
                    try{
                        startActivity(intent);
                    }
                    catch(Exception e){
                        Log.e(TAG, "Error opening Fora", e);
                        AMGUIUtility.displayException(this, getString(R.string.error_text), getString(R.string.dict_fora) + " " + getString(R.string.error_no_dict), e);
                    }
                }

                return true;

            }

            case R.id.menu_context_paint:
            {
                Intent myIntent = new Intent(this, FingerPaint.class);
                startActivity(myIntent);
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
                Item item = extras.getParcelable("item");
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

            case ACTIVITY_DETAIL:
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(settingManager.getVolumeKeyShortcut() && buttonDisabled == false){
            if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                return true;
            }
            else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        /* Short press to scroe the card */

        if(settingManager.getVolumeKeyShortcut() && buttonDisabled == false){
            if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                if(flashcardDisplay.isAnswerShown() == false){
                    updateFlashcardView(true);
                    showButtons();
                }
                else{
                    /* Grade 0 for up key */
                    getGradeButtonListener(0).onClick(null);
                    Toast.makeText(this, getString(R.string.grade_text) + " 0", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                if(flashcardDisplay.isAnswerShown() == false){
                    updateFlashcardView(true);
                }
                else{
                    /* Grade 3 for down key */
                    getGradeButtonListener(3).onClick(null);
                    Toast.makeText(this, getString(R.string.grade_text) + " 3", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    void updateFlashcardView(boolean showAnswer){
        if(currentItem == null){
            Log.e(TAG, "currentItem is null in updateFlashcardView", new NullPointerException("currentItem is null"));
            return;
        }
        flashcardDisplay.updateView(currentItem, showAnswer);
        /* Also update the visibility of buttons */
        if(showAnswer){
            showButtons();
        }
        else{
            hideButtons();
        }
        autoSpeak();
        if(!buttonDisabled){
            setGradeButtonTitle();
            setGradeButtonListeners();
        }
        /* Automatic copy the current question to clipboard */
        if(settingManager.getCopyClipboard()){
            ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cm.setText(currentItem.getQuestion());
        }
    }


    void setViewListeners(){
        View.OnClickListener showAnswerListener = new View.OnClickListener(){
            public void onClick(View v){
                if(currentItem != null){
                    /* Double sided card, the click will toggle question and answer */
                    if(settingManager.getCardStyle() == SettingManager.CardStyle.DOUBLE_SIDED){
                        if(flashcardDisplay.isAnswerShown()){
                            updateFlashcardView(false);
                            hideButtons();
                        }
                        else{
                            updateFlashcardView(true);
                            showButtons();
                        }

                    }
                    else{
                        /* For single sided card */
                        updateFlashcardView(true);
                        showButtons();
                    }
                }
            }
        };
        View.OnClickListener speakQuestionListener = new View.OnClickListener(){
            public void onClick(View v){
                if(currentItem != null && questionTTS != null){
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
                        if(answerTTS != null){
                            answerTTS.sayText(currentItem.getAnswer());
                        }
                    }
                }
            }
        };
        View.OnLongClickListener openContextMenuListener = new View.OnLongClickListener(){
            public boolean onLongClick(View v){
                MemoScreen.this.openContextMenu(flashcardDisplay.getView());
                /* To determine which view is long clicked */
                activeView = v;
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
        LinearLayout memoRoot = (LinearLayout)findViewById(R.id.memo_screen_root);
        memoRoot.setOnClickListener(showAnswerListener);
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
            hm.get("0").setText(getString(R.string.memo_btn0_anki_text) + "\n+" + currentItem.processAnswer(0, false).getInterval());
            hm.get("1").setText(getString(R.string.memo_btn1_anki_text) + "\n+" + currentItem.processAnswer(1, false).getInterval());
            hm.get("2").setText(getString(R.string.memo_btn2_anki_text) + "\n+" + currentItem.processAnswer(2, false).getInterval());
            hm.get("3").setText(getString(R.string.memo_btn3_anki_text) + "\n+" + currentItem.processAnswer(3, false).getInterval());
            hm.get("4").setText(getString(R.string.memo_btn4_anki_text) + "\n+" + currentItem.processAnswer(4, false).getInterval());
            hm.get("5").setText(getString(R.string.memo_btn5_anki_text) + "\n+" + currentItem.processAnswer(5, false).getInterval());
        }
        else{
            hm.get("0").setText(getString(R.string.memo_btn0_text) + "\n+" + currentItem.processAnswer(0, false).getInterval());
            hm.get("1").setText(getString(R.string.memo_btn1_text) + "\n+" + currentItem.processAnswer(1, false).getInterval());
            hm.get("2").setText(getString(R.string.memo_btn2_text) + "\n+" + currentItem.processAnswer(2, false).getInterval());
            hm.get("3").setText(getString(R.string.memo_btn3_text) + "\n+" + currentItem.processAnswer(3, false).getInterval());
            hm.get("4").setText(getString(R.string.memo_btn4_text) + "\n+" + currentItem.processAnswer(4, false).getInterval());
            hm.get("5").setText(getString(R.string.memo_btn5_text) + "\n+" + currentItem.processAnswer(5, false).getInterval());

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

    String getActivityTitleString(){
        int[] stat = queueManager.getStatInfo();
        String titleString = getString(R.string.stat_new) + stat[0] + " " + getString(R.string.stat_scheduled) + stat[1] + " " + getString(R.string.memo_current_id) + currentItem.getId();
        if(currentItem != null && currentItem.getCategory() != null){
            titleString += "  " + currentItem.getCategory();
        }
        return titleString;

    }


    private View.OnClickListener getGradeButtonListener(final int grade){
        return new View.OnClickListener(){
            public void onClick(View v){
                prevItem = currentItem;
                currentItem = currentItem.processAnswer(grade, true);
                bgUpdateTask = new BackgroundUpdateTask();
                bgUpdateTask.execute(currentItem);
                if(questionTTS != null){
                    questionTTS.stop();
                }
                if(answerTTS != null){
                    answerTTS.stop();
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
        /* Does it take the place holder space? */
        if(settingManager.getCardStyle() == SettingManager.CardStyle.DOUBLE_SIDED){
            controlButtons.getView().setVisibility(View.GONE);
        }
        else{
            controlButtons.getView().setVisibility(View.INVISIBLE);
        }

    }

    private void showButtons(){
        if(!buttonDisabled){
            controlButtons.getView().setVisibility(View.VISIBLE);
        }
    }


    private void composeViews(){
        LinearLayout memoRoot = (LinearLayout)findViewById(R.id.memo_screen_root);

        LinearLayout flashcardDisplayView = (LinearLayout)flashcardDisplay.getView();
        LinearLayout controlButtonsView = (LinearLayout)controlButtons.getView();
        /* This li is make the background of buttons the same as answer */
        LinearLayout li = new LinearLayout(this);
        li.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.FILL_PARENT));
        List<Integer> colors = settingManager.getColors();
        if(colors != null){
            li.setBackgroundColor(settingManager.getColors().get(3));
        }

        /* 
         * -1: Match parent -2: Wrap content
         * This is necessary or the view will not be 
         * stetched
         */
        memoRoot.addView(flashcardDisplayView, -1, -1);
        li.addView(controlButtonsView, -1, -2);
        memoRoot.addView(li, -1, -2);
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

    void autoSpeak(){
        if(currentItem != null && !buttonDisabled){

            if(settingManager.getSpeechControlMethod() == SettingManager.SpeechControlMethod.AUTOTAP || settingManager.getSpeechControlMethod() == SettingManager.SpeechControlMethod.AUTO){
                if(!flashcardDisplay.isAnswerShown()){
                    if(questionTTS != null){
                        questionTTS.sayText(currentItem.getQuestion());
                    }
                }
                else{
                    if(answerTTS != null){
                        answerTTS.sayText(currentItem.getAnswer());
                    }
                }
            }
        }
    }

    private void initTTS(){
        String audioDir = settingManager.getAudioLocation();
        Locale ql = settingManager.getQuestionAudioLocale();
        Locale al = settingManager.getAnswerAudioLocale();
        if(settingManager.getQuestionUserAudio()){
            questionTTS = new AudioFileTTS(audioDir, dbName);
        }
        else if(ql != null){
            questionTTS = new AnyMemoTTSPlatform(this, ql);
        }
        else{
            questionTTS = null;
        }
        if(settingManager.getAnswerUserAudio()){
            answerTTS = new AudioFileTTS(audioDir, dbName);
        }
        else if(al != null){
            answerTTS = new AnyMemoTTSPlatform(this, al);
        }
        else{
            answerTTS = null;
        }
    }


    /*
     * Use AsyncTask to update the database and update the statistics
     * information
     */
    private class BackgroundUpdateTask extends AsyncTask<Item, Void, Item>{

        @Override
        public void onPreExecute(){
            super.onPreExecute();
            buttonDisabled = true;
            currentItem = queueManager.getNext(currentItem);
            if(currentItem != null){
                updateFlashcardView(false);
            }
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public Item doInBackground(Item... items){
            Item nextItem = queueManager.updateAndNext(items[0]);
            /* The title is only valid if there are still items in the queue */
            return nextItem;
        }
        @Override
        public void onCancelled(){
            return;
        }

        @Override
        public void onPostExecute(Item result){
            super.onPostExecute(result);
            setProgressBarIndeterminateVisibility(false);
            currentItem = result;
            if(currentItem == null){
                showNoItemDialog();
            }
            else{ 
                buttonDisabled = false;
                if(!flashcardDisplay.isAnswerShown()){
                    updateFlashcardView(false);
                }
                else{
                    updateFlashcardView(true);
                    showButtons();
                }
                setTitle(getActivityTitleString());
            }
        }
    }

}


