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

import java.util.Date;

import org.apache.mycommons.lang3.StringUtils;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.AnyMemoService;

import org.liberty.android.fantastischmemo.queue.CramQueueManager;
import org.liberty.android.fantastischmemo.queue.LearnQueueManager;
import org.liberty.android.fantastischmemo.ui.DetailScreen;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.ui.SettingsScreen;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;
import org.liberty.android.fantastischmemo.utils.AMUtil;
import org.liberty.android.fantastischmemo.utils.AnyMemoExecutor;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;

import org.liberty.android.fantastischmemo.queue.QueueManager;
import java.util.Map;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;

import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTSPlatform;
import org.liberty.android.fantastischmemo.tts.AudioFileTTS;

import com.example.android.apis.graphics.FingerPaint;

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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import android.util.Log;
import android.net.Uri;

import org.liberty.android.fantastischmemo.ui.CategoryEditorFragment.CategoryEditorResultListener;

public class MemoScreen extends AMActivity {
    public static String EXTRA_DBPATH = "dbpath";
    public static String EXTRA_CATEGORY_ID = "category_id";
    public static String EXTRA_START_CARD_ID = "start_card_id";
    public static String EXTRA_CRAM = "cram";

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

    private FlashcardDisplay flashcardDisplay;

    /* State objects */
    private Card currentCard = null;
    private Card prevCard = null;
    private LearningData prevLearningData = null;
    private String dbPath = "";
    private String dbName = "";
    private int filterCategoryId = -1; 
    private Category filterCategory;
    private boolean isCram = false;
    private int startCardId = -1;

    /* DAOs */
    private SettingDao settingDao;
    private CardDao cardDao;
    private LearningDataDao learningDataDao;
    private CategoryDao categoryDao;

    /* Global and db settings */
    private Option option;
    private Setting setting;
    
    private ControlButtons controlButtons;
    private QueueManager queueManager;
    private volatile boolean buttonDisabled = false;

    /* Tasks to run */
    private GradeTask gradeTask = null;
    private InitTask initTask = null;
    private WaitDbTask waitDbTask;

    /* Schedulers */
    private Scheduler scheduler = null;


    /* current states */
    private long schedluledCardCount = 0;
    private long newCardCount = 0;

    /* Keep the dbOpenHelper so it will be destroyed in onDestroy */
    private AnyMemoDBOpenHelper dbOpenHelper;

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dbPath = extras.getString(EXTRA_DBPATH);
            filterCategoryId = extras.getInt(EXTRA_CATEGORY_ID, -1);
            isCram = extras.getBoolean(EXTRA_CRAM, false);
            startCardId = extras.getInt(EXTRA_START_CARD_ID, -1);
        }
        initTask = new InitTask();
        initTask.execute((Void)null);
    }

    private void createQueue() {
        int queueSize = option.getQueueSize();
        if (!isCram) {
             LearnQueueManager.Builder builder = new LearnQueueManager.Builder()
                .setCardDao(cardDao)
                .setCategoryDao(categoryDao)
                .setLearningDataDao(learningDataDao)
                .setScheduler(scheduler)
                .setLearnQueueSize(queueSize)
                .setCacheSize(50)
                .setFilterCategory(filterCategory);
             if (option.getShuffleType() == Option.ShuffleType.LOCAL) {
                 builder.setShuffle(true);
             } else {
                 builder.setShuffle(false);
             }
             queueManager = builder.build();
        } else {
            queueManager = new CramQueueManager.Builder()
                .setCardDao(cardDao)
                .setLearnQueueSize(queueSize)
                .build();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only if the initTask has been finished and no waitDbTask is waiting.
        if ((initTask != null && AsyncTask.Status.FINISHED.equals(initTask.getStatus()))
                && (waitDbTask == null || !AsyncTask.Status.RUNNING.equals(waitDbTask.getStatus()))) {
            waitDbTask = new WaitDbTask();
            waitDbTask.execute((Void)null);
        } else {
            Log.i(TAG, "There is another task running. Do not run tasks");
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        AnyMemoExecutor.submit(flushDatabaseTask);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
        if(questionTTS != null){
            questionTTS.shutdown();
        }
        if(answerTTS != null){
            answerTTS.shutdown();
        }

        /* Update the widget because MemoScreen can be accessed though widget*/
        Intent myIntent = new Intent(this, AnyMemoService.class);
        myIntent.putExtra("request_code", AnyMemoService.CANCEL_NOTIFICATION | AnyMemoService.UPDATE_WIDGET);
        startService(myIntent);
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
                if(questionTTS != null && currentCard != null){
                    questionTTS.sayText(currentCard.getQuestion());
                }
                return true;
            }

            case R.id.menuspeakanswer:
            {
                if(answerTTS != null && currentCard != null){
                    answerTTS.sayText(currentCard.getAnswer());
                }
                return true;
            }

            case R.id.menusettings:
            {
                Intent myIntent = new Intent(this, SettingsScreen.class);
                myIntent.putExtra(SettingsScreen.EXTRA_DBPATH, dbPath);
                startActivityForResult(myIntent, ACTIVITY_SETTINGS);
                return true;
            }

            case R.id.menudetail:
            {
                Intent myIntent = new Intent(this, DetailScreen.class);
                myIntent.putExtra(DetailScreen.EXTRA_DBPATH, this.dbPath);
                myIntent.putExtra(DetailScreen.EXTRA_CARD_ID, currentCard.getId());
                startActivityForResult(myIntent, ACTIVITY_DETAIL);
                return true;
            }

            case R.id.menuundo:
            {
                undoCard();
                return true;
            }

            case R.id.menu_memo_category:
            {
                showCategoriesDialog();
                return true;
            }
        }

        return false;
    }

    /* 
     * When the user select the undo from the menu
     * this is what to do
     */
    private void undoCard(){
        if(prevLearningData != null){
            currentCard = prevCard;
            learningDataDao.updateLearningData(prevLearningData);
            restartActivity();
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
                myIntent.putExtra(CardEditor.EXTRA_DBPATH, this.dbPath);
                myIntent.putExtra(CardEditor.EXTRA_CARD_ID, currentCard.getId());
                myIntent.putExtra(CardEditor.EXTRA_IS_EDIT_NEW, false);
                startActivityForResult(myIntent, ACTIVITY_EDIT);
                return true;
            }
            case R.id.menu_context_delete:
            {
                new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_text)
                    .setMessage(R.string.delete_warning)
                    .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface arg0, int arg1) {
                            if(currentCard != null){
                                try {
                                    cardDao.delete(currentCard);
                                    // Do not restart this card
                                    currentCard = null;
                                    restartActivity();
                                } catch (SQLException e) {
                                    Log.e(TAG, "Delete card error", e);
                                }
                            }
                        }
                    })
                .setNegativeButton(R.string.cancel_text, null)
                .show();

                return true;

            }
            case R.id.menu_context_skip:
            {
                new AlertDialog.Builder(this)
                    .setTitle(R.string.skip_text)
                    .setMessage(R.string.skip_warning)
                    .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface arg0, int arg1) {
                            if(currentCard != null) {
                                try {
                                    LearningData ld = currentCard.getLearningData();
                                    ld.setNextLearnDate(new Date(Long.MAX_VALUE));
                                    ld.setAcqReps(1);
                                    learningDataDao.update(ld);
                                    // Do not restart this card
                                    currentCard = null;
                                    restartActivity();
                                } catch (SQLException e) {
                                    Log.e(TAG, "Delete card error", e);
                                }
                            }
                        }
                    })
                .setNegativeButton(R.string.cancel_text, null)
                .show();
                return true;
            }
            case R.id.menu_context_gotoprev:
            {
                Intent myIntent = new Intent();
                myIntent.setClass(this, EditScreen.class);
                myIntent.putExtra(EditScreen.EXTRA_DBPATH, dbPath);
                if (currentCard != null) {
                    myIntent.putExtra(EditScreen.EXTRA_CARD_ID, currentCard.getId());
                }
                
                startActivity(myIntent);
                return true;
            }

            case R.id.menu_context_lookup:
            {
                if(currentCard == null){
                    return false;
                }
                /* default word to lookup is question */
                String lookupWord = currentCard.getQuestion();

                if(flashcardDisplay.getAnswerView() == activeView){
                    lookupWord = currentCard.getAnswer();
                }

                if(option.getDictApp() == Option.DictApp.COLORDICT){
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
                if(option.getDictApp() == Option.DictApp.FORA){
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
                filterCategoryId = extras.getInt(EXTRA_CATEGORY_ID);
                restartActivity();
                break;
            }
            case ACTIVITY_EDIT:
            {
                restartActivity();
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
        if(option.getVolumeKeyShortcut()){
            if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                return true;
            }
            else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                return true;
            }
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.v(TAG, "back button pressed");
            FinishTask task = new FinishTask();
            task.execute((Void)null);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        /* Short press to scroe the card */

        if(option.getVolumeKeyShortcut()){
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

    private void updateFlashcardView(boolean showAnswer) {
        if(currentCard == null) {
            Log.e(TAG, "current card is null in updateFlashcardView");
            return;
        }
        flashcardDisplay.updateView(currentCard, showAnswer);
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
        if(option.getCopyClipboard()){
            ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cm.setText(currentCard.getQuestion());
        }
    }


    private void setViewListeners(){
        View.OnClickListener showAnswerListener = new View.OnClickListener(){
            public void onClick(View v){
                if(currentCard!= null){
                    /* Double sided card, the click will toggle question and answer */
                    if(setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED){
                        if(flashcardDisplay.isAnswerShown()){
                            updateFlashcardView(false);
                            hideButtons();
                        } else{
                        	updateFlashcardView(true);
                            showButtons();
                        }

                    } else {
                        /* For single sided card */
                        updateFlashcardView(true);
                        showButtons();
                    }
                }
            }
        };
        View.OnClickListener speakQuestionListener = new View.OnClickListener(){
            public void onClick(View v){
                if(currentCard != null && questionTTS != null){
                    questionTTS.sayText(currentCard.getQuestion());
                }
            }
        };
        View.OnClickListener speakAnswerListener = new View.OnClickListener(){
            public void onClick(View v){
                if (currentCard!= null) {
                    if(!flashcardDisplay.isAnswerShown()){
                        updateFlashcardView(true);
                        showButtons();
                    }
                    else{
                        if(answerTTS != null){
                            answerTTS.sayText(currentCard.getAnswer());
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
                return true;
            }
        };
        flashcardDisplay.setQuestionLayoutLongClickListener(openContextMenuListener);
        flashcardDisplay.setAnswerLayoutLongClickListener(openContextMenuListener);

        flashcardDisplay.setQuestionLayoutClickListener(showAnswerListener);
        flashcardDisplay.setAnswerLayoutClickListener(showAnswerListener);
        if(option.getSpeakingType() == Option.SpeakingType.TAP || option.getSpeakingType() == Option.SpeakingType.AUTOTAP){
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

    private void setGradeButtonTitle() {
        Map<String, Button> hm = controlButtons.getButtons();
        if(option.getButtonStyle() == Option.ButtonStyle.MNEMOSYNE) {
            hm.get("0").setText(getString(R.string.memo_btn0_brief_text));
            hm.get("1").setText(getString(R.string.memo_btn1_brief_text));
            hm.get("2").setText(getString(R.string.memo_btn2_brief_text));
            hm.get("3").setText(getString(R.string.memo_btn3_brief_text));
            hm.get("4").setText(getString(R.string.memo_btn4_brief_text));
            hm.get("5").setText(getString(R.string.memo_btn5_brief_text));
        } else if(option.getButtonStyle() == Option.ButtonStyle.ANKI) {
            hm.get("0").setText(getString(R.string.memo_btn0_anki_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 0, false)));
            hm.get("1").setText(getString(R.string.memo_btn1_anki_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 1, false)));
            hm.get("2").setText(getString(R.string.memo_btn2_anki_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 2, false)));
            hm.get("3").setText(getString(R.string.memo_btn3_anki_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 3, false)));
            hm.get("4").setText(getString(R.string.memo_btn4_anki_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 4, false)));
            hm.get("5").setText(getString(R.string.memo_btn5_anki_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 5, false)));
        } else {
            hm.get("0").setText(getString(R.string.memo_btn0_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 0, false)));
            hm.get("1").setText(getString(R.string.memo_btn1_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 1, false)));
            hm.get("2").setText(getString(R.string.memo_btn2_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 2, false)));
            hm.get("3").setText(getString(R.string.memo_btn3_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 3, false)));
            hm.get("4").setText(getString(R.string.memo_btn4_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 4, false)));
            hm.get("5").setText(getString(R.string.memo_btn5_text) + "\n+" + getIntervalToDisplay(scheduler.schedule(currentCard.getLearningData(), 5, false)));
        }
    }

    private void setGradeButtonListeners(){
        Map<String, Button> hm = controlButtons.getButtons();
        for(int i = 0; i < 6; i++){
            Button b = hm.get(Integer.valueOf(i).toString());
            b.setOnClickListener(getGradeButtonListener(i));
            b.setOnLongClickListener(getGradeButtonLongClickListener(i));
        }
    }

    private String getActivityTitleString(){
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.new_text) + ": " + newCardCount + " ");
        sb.append(getString(R.string.review_short_text) + ": " + schedluledCardCount + " ");
        sb.append(getString(R.string.id_text) + ": " + currentCard.getId() + " ");
        if (StringUtils.isNotEmpty(currentCard.getCategory().getName())) {
            sb.append(getString(R.string.category_short_text) + ": " + currentCard.getCategory().getName());
        }
        return sb.toString();
    }


    private View.OnClickListener getGradeButtonListener(final int grade){
        return new View.OnClickListener(){
            public void onClick(View v){
                gradeTask = new GradeTask();
                gradeTask.execute(grade);
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
        if(setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED){
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
        Integer color = setting.getAnswerBackgroundColor();
        if(color != null){
            li.setBackgroundColor(color);
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

        RestartTask task = new RestartTask();
        task.execute((Void)null);
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
                    myIntent.putExtra(MemoScreen.EXTRA_DBPATH, dbPath);
                    myIntent.putExtra(MemoScreen.EXTRA_CRAM, true);
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

    private void autoSpeak(){
        if (currentCard != null) {
            if(option.getSpeakingType() == Option.SpeakingType.AUTOTAP || option.getSpeakingType() == Option.SpeakingType.AUTO){
                if(!flashcardDisplay.isAnswerShown()){
                    if(questionTTS != null){
                        // Make sure the TTS is stop, or it will speak nothing.
                        questionTTS.stop();
                        questionTTS.sayText(currentCard.getQuestion());
                    }
                }
                else{
                    if(answerTTS != null){
                        // Make sure the TTS is stop
                        answerTTS.stop();
                        answerTTS.sayText(currentCard.getAnswer());
                    }
                }
            }
        }
    }

    private void refreshStatInfo() {
       newCardCount = cardDao.getNewCardCount(filterCategory);
       schedluledCardCount = cardDao.getScheduledCardCount(filterCategory);
    }

    private void initTTS(){
        String defaultLocation = AMEnv.DEFAULT_AUDIO_PATH;
        String qa = setting.getQuestionAudio();
        String aa = setting.getAnswerAudio();

        if (StringUtils.isNotEmpty(setting.getQuestionAudioLocation())) {
            questionTTS = new AudioFileTTS(defaultLocation, dbName);
        } else if (StringUtils.isNotEmpty(qa)){
            questionTTS = new AnyMemoTTSPlatform(this, qa);
        } else{
            questionTTS = null;
        }

        if (StringUtils.isNotEmpty(setting.getAnswerAudioLocation())) {
            answerTTS = new AudioFileTTS(defaultLocation, dbName);
        } else if (StringUtils.isNotEmpty(aa)){
            answerTTS = new AnyMemoTTSPlatform(this, aa);
        } else{
            answerTTS = null;
        }
    }

    private void showCategoriesDialog() {
        CategoryEditorFragment df = new CategoryEditorFragment();
        df.setResultListener(categoryResultListener);
        Bundle b = new Bundle();
        b.putString(CategoryEditorFragment.EXTRA_DBPATH, dbPath);
        if (filterCategory == null) {
            b.putInt(CategoryEditorFragment.EXTRA_CATEGORY_ID, currentCard.getCategory().getId());
        } else {
            // If we use the category filer, we can just use the currentCategory
            // This will handle the new card situation.
            b.putInt(CategoryEditorFragment.EXTRA_CATEGORY_ID, filterCategory.getId());
        }
        df.setArguments(b);
        df.show(getSupportFragmentManager(), "CategoryEditDialog");
        getSupportFragmentManager().findFragmentByTag("CategoryEditDialog");
    }


    // Interval: 12.3456 -> "12.3", 12.0 -> "12.0"
    private String getIntervalToDisplay(LearningData ld) {
        return "" + ((double)Math.round(ld.getInterval() * 10)) / 10;
    }

    private class InitTask extends AsyncTask<Void, Void, Card> {

		@Override
        public void onPreExecute() {
            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

            setContentView(R.layout.memo_screen_layout);

            option = new Option(MemoScreen.this);

            scheduler = new DefaultScheduler(MemoScreen.this);

            // Strip leading path!
            dbName = AMUtil.getFilenameFromPath(dbPath);
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public Card doInBackground(Void... params) {
            try {
                
                dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(MemoScreen.this, dbPath);
                cardDao = dbOpenHelper.getCardDao();
                learningDataDao = dbOpenHelper.getLearningDataDao();
                settingDao = dbOpenHelper.getSettingDao();
                categoryDao = dbOpenHelper.getCategoryDao();
                setting = settingDao.queryForId(1);
                // Initialize the TTS early so it will have time to initialize.
                initTTS();
                if (filterCategoryId != -1) {
                    filterCategory = categoryDao.queryForId(filterCategoryId);
                    assert filterCategory != null : "Query filter id: " + filterCategoryId +". Get null";
                }
                /* Run the learnQueue init in a separate thread */
                createQueue();
                if (startCardId != -1) {
                    return queueManager.dequeuePosition(startCardId);
                }
                return queueManager.dequeue();
            } catch (Exception e) {
                Log.e(TAG, "Excepting doing in bacground", e);
                return null;
            }
        }
        
        @Override
        public void onCancelled(){
            return;
        }

        @Override
        public void onPostExecute(Card result){
            setProgressBarIndeterminateVisibility(false);
            if (result == null) {
                showNoItemDialog();
                return;
            }
            assert result != null : "Init get null card";
            if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED) {
                flashcardDisplay = new DoubleSidedCardDisplay(MemoScreen.this, dbName, setting, option);
            } else {
                flashcardDisplay = new SingleSidedCardDisplay(MemoScreen.this, dbName, setting, option);
            }
            if (option.getButtonStyle() == Option.ButtonStyle.ANKI) {
                controlButtons = new AnkiGradeButtons(MemoScreen.this);
            }
            else if (option.getButtonStyle() == Option.ButtonStyle.MNEMOSYNE){
                controlButtons = new MnemosyneGradeButtons(MemoScreen.this);
            }
            else{
                controlButtons = new AnyMemoGradeButtons(MemoScreen.this);
            }

            currentCard = result;
            
            composeViews();
            setViewListeners();
            hideButtons();
            registerForContextMenu(flashcardDisplay.getView());

            refreshStatInfo();
            setTitle(getActivityTitleString());
            updateFlashcardView(false);
        }
    }

    /*
     * Use AsyncTask to update the database and update the statistics
     * information
     */
    private class GradeTask extends AsyncTask<Integer, Void, Card>{
        private boolean isNewCard = false;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            setProgressBarIndeterminateVisibility(true);
            hideButtons();
        }

        @Override
        public Card doInBackground(Integer... grades) {
            assert grades.length == 1 : "Grade more than 1 time";
            int grade = grades[0];
            LearningData ld = currentCard.getLearningData();
            if (ld.getAcqReps() == 0) {
                isNewCard = true;
            }

            // This was saved to determine the stat info
            // and the card id for undo
            prevCard = currentCard;

            // Save previous learning for Undo
            // This part is ugly due to muutablity of ORMLite
            prevLearningData = new LearningData();
            prevLearningData.setId(ld.getId());
            prevLearningData.cloneFromLearningData(ld);

            LearningData newLd = scheduler.schedule(ld, grade, true);

            // Need to clone the data due to ORMLite restriction on "update()" method.
            ld.cloneFromLearningData(newLd);
            currentCard.setLearningData(ld);
            queueManager.update(currentCard);
            Card nextCard = queueManager.dequeue();
            return nextCard;
        }

        @Override
        public void onCancelled() {
            return;
        }

        @Override
        public void onPostExecute(Card result){
            super.onPostExecute(result);
            setProgressBarIndeterminateVisibility(false);
            currentCard = result;
            if(currentCard == null){
                showNoItemDialog();
                return;
            }

            if (isNewCard) {
                newCardCount -= 1;
                if (!scheduler.isCardLearned(prevCard.getLearningData())) {
                    schedluledCardCount += 1;
                }
            } else {
                if (scheduler.isCardLearned(prevCard.getLearningData())) {
                    schedluledCardCount -= 1;
                }
            }
            updateFlashcardView(false);

            setTitle(getActivityTitleString());
        }
    }

    /*
     * Use AsyncTask to make sure there is no running task for a db 
     */
    private class WaitDbTask extends AsyncTask<Void, Void, Void>{
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute(){
            super.onPreExecute();
            progressDialog = new ProgressDialog(MemoScreen.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_save));
            progressDialog.show();
        }

        @Override
        public Void doInBackground(Void... nothing){
            AnyMemoExecutor.waitAllTasks();
            return null;
        }

        @Override
        public void onCancelled(){
            return;
        }

        @Override
        public void onPostExecute(Void result){
            super.onPostExecute(result);
            if (!isCancelled()) {
                progressDialog.dismiss();
            }
        }
    }

    /*
     * Like the wait db task but finish the current activity
     */
    private class FinishTask extends WaitDbTask {
        @Override
        public void onPostExecute(Void result){
            super.onPostExecute(result);
            
            finish();
        }
    }

    /* When restarting an activity, we have to flush db first. */
    private class RestartTask extends WaitDbTask {
        @Override
        public void onPostExecute(Void result){
            super.onPostExecute(result);
            
            finish();
            Intent myIntent = new Intent(MemoScreen.this, MemoScreen.class);
            myIntent.putExtra(EXTRA_DBPATH, dbPath);
            myIntent.putExtra(EXTRA_CATEGORY_ID, filterCategoryId);
            if (currentCard != null ) {
                myIntent.putExtra(EXTRA_START_CARD_ID, currentCard.getId());
            }
            startActivity(myIntent);
        }
    }


    Runnable flushDatabaseTask = new Runnable() {
        public void run() {
            queueManager.flush();
        }
    };

    // When a category is selected in category fragment.
    private CategoryEditorResultListener categoryResultListener = 
        new CategoryEditorResultListener() {
            public void onReceiveCategory(Category c) {
                assert c != null : "Receive null category";
                filterCategoryId = c.getId();
                // Do not restart with the current card
                currentCard = null;
                restartActivity();
            }
        };

}
