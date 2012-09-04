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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.text.ClipboardManager;
import android.text.Html;

import android.view.Gravity;
import android.view.Menu;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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

public class StudyActivity extends QACardActivity {
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

    /* State objects */
    private Card prevCard = null;
    private LearningData prevLearningData = null;
    private String dbPath = "";
    private String dbName = "";
    private int filterCategoryId = -1; 
    private Category filterCategory;
    private int startCardId = -1;

    private GradeButtons gradeButtons;
    private QueueManager queueManager;
    private volatile boolean buttonDisabled = false;

    private CardDao cardDao;
    private LearningDataDao learningDataDao;
    private CategoryDao categoryDao;

    private Setting setting;

    private Option option;

    /* Tasks to run */
    private GradeTask gradeTask = null;

    /* Schedulers */
    private Scheduler scheduler = null;


    /* current states */
    private long schedluledCardCount = 0;
    private long newCardCount = 0;

    /* Keep the dbOpenHelper so it will be destroyed in onDestroy */
    private AnyMemoDBOpenHelper dbOpenHelper;

    boolean initialized = false;

    private WaitDbTask waitDbTask;

    @Override
	public void onCreate(Bundle savedInstanceState){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dbPath = extras.getString(EXTRA_DBPATH);
            filterCategoryId = extras.getInt(EXTRA_CATEGORY_ID, -1);
            startCardId = extras.getInt(EXTRA_START_CARD_ID, -1);
        }
        super.onCreate(savedInstanceState);
    }

    private void createQueue() {
        int queueSize = option.getQueueSize();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.study_activity_menu, menu);
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
                if(questionTTS != null && getCurrentCard() != null){
                    questionTTS.sayText(getCurrentCard().getQuestion());
                }
                return true;
            }

            case R.id.menuspeakanswer:
            {
                if(answerTTS != null && getCurrentCard()!= null){
                    answerTTS.sayText(getCurrentCard().getAnswer());
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
                myIntent.putExtra(DetailScreen.EXTRA_CARD_ID, getCurrentCard().getId());
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
            setCurrentCard(prevCard);
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
                ProgressDialog progressDialog = new ProgressDialog(StudyActivity.this);
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
    public void onPause(){
        super.onPause();
        AnyMemoExecutor.submit(flushDatabaseTask);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only if the initTask has been finished and no waitDbTask is waiting.
        if (initialized && (waitDbTask == null || !AsyncTask.Status.RUNNING.equals(waitDbTask.getStatus()))) {
            waitDbTask = new WaitDbTask();
            waitDbTask.execute((Void)null);
        } else {
            Log.i(TAG, "There is another task running. Do not run tasks");
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        /* Short press to scroe the card */

        if(option.getVolumeKeyShortcut()){
            if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
                //TODO is answer shown?
                //if(flashcardDisplay.isAnswerShown() == false){
                    //updateFlashcardView(true);
                    //showButtons();
                //}
                //else{
                    /* Grade 0 for up key */
                    //getGradeButtonListener(0).onClick(null);
                    //Toast.makeText(this, getString(R.string.grade_text) + " 0", Toast.LENGTH_SHORT).show();
                //}
                return true;
            }
            if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
                //if(flashcardDisplay.isAnswerShown() == false){
                //    //updateFlashcardView(true);
                //}
                //else{
                //    /* Grade 3 for down key */
                //    //getGradeButtonListener(3).onClick(null);
                //    Toast.makeText(this, getString(R.string.grade_text) + " 3", Toast.LENGTH_SHORT).show();
                //}
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
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
                    myIntent.setClass(StudyActivity.this, StudyActivity.class);
                    myIntent.putExtra(StudyActivity.EXTRA_DBPATH, dbPath);
                    myIntent.putExtra(StudyActivity.EXTRA_CRAM, true);
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
        if (getCurrentCard() != null) {
            if(option.getSpeakingType() == Option.SpeakingType.AUTOTAP
                    || option.getSpeakingType() == Option.SpeakingType.AUTO){
                if(isAnswerShown()){
                    if(questionTTS != null){
                        // Make sure the TTS is stop, or it will speak nothing.
                        questionTTS.stop();
                        questionTTS.sayText(getCurrentCard().getQuestion());
                    }
                } else if(answerTTS != null){
                    // Make sure the TTS is stop
                    answerTTS.stop();
                    answerTTS.sayText(getCurrentCard().getAnswer());
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
            b.putInt(CategoryEditorFragment.EXTRA_CATEGORY_ID, getCurrentCard().getCategory().getId());
        } else {
            // If we use the category filer, we can just use the currentCategory
            // This will handle the new card situation.
            b.putInt(CategoryEditorFragment.EXTRA_CATEGORY_ID, filterCategory.getId());
        }
        df.setArguments(b);
        df.show(getSupportFragmentManager(), "CategoryEditDialog");
        getSupportFragmentManager().findFragmentByTag("CategoryEditDialog");
    }



    @Override
    public void onInit() throws Exception {
        cardDao = getDbOpenHelper().getCardDao();
        learningDataDao = getDbOpenHelper().getLearningDataDao();
        categoryDao = getDbOpenHelper().getCategoryDao();
        setting = getSetting();
        option = getOption();

        // Initialize the TTS early so it will have time to initialize.
        initTTS();
        scheduler = new DefaultScheduler(this);
        createQueue();

        if (filterCategoryId != -1) {
            filterCategory = categoryDao.queryForId(filterCategoryId);
            assert filterCategory != null : "Query filter id: " + filterCategoryId +". Get null";
        }
        /* Run the learnQueue init in a separate thread */
        if (startCardId != -1) {
            setCurrentCard(queueManager.dequeuePosition(startCardId));
        } else {
            setCurrentCard(queueManager.dequeue());
        }
        refreshStatInfo();
    }

    @Override
    public void onPostInit() {
        setupGradeButtons();
        displayCard(false);
        initialized = true;
        setSmallTitle(getActivityTitleString());
        setTitle(getDbName());
    }

    @Override
    public void onPostDisplayCard() {
        if (isAnswerShown()) {
            setGradeButtonTitle();
            gradeButtons.show();
        } else {
            gradeButtons.hide();
        }
    }


    private void setupGradeButtons() {
        //if (option.getButtonStyle() == Option.ButtonStyle.ANKI) {
        //    controlButtons = new AnkiGradeButtons(this);
        //} else if (option.getButtonStyle() == Option.ButtonStyle.MNEMOSYNE) {
        //    controlButtons = new MnemosyneGradeButtons(this);
        //} else {
        //    controlButtons = new AnyMemoGradeButtons(this);
        //}

        gradeButtons = new GradeButtons(this, R.layout.grade_buttons_anymemo);

        LinearLayout rootView= (LinearLayout)findViewById(R.id.root);

        LinearLayout gradeButtonsView = gradeButtons.getView();

        gradeButtons.setGradeButtonBackground(0, R.drawable.red_button);
        gradeButtons.setGradeButtonBackground(1, R.drawable.red_button);
        gradeButtons.setGradeButtonBackground(4, R.drawable.green_button);
        gradeButtons.setGradeButtonBackground(5, R.drawable.green_button);

        // Make sure touching all areas can reveal the card.
        rootView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                onClickAnswerView();
			}
        });
        rootView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
                onClickAnswerView();
				return true ;
			}
        });

        gradeButtons.setOnGradeButtonClickListener(onGradeButtonClickListener);

        /* This li is make the background of buttons the same as answer */
        LinearLayout li = new LinearLayout(this);
        li.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.FILL_PARENT));
        Integer color = setting.getAnswerBackgroundColor();
        if(color != null) {
            li.setBackgroundColor(color);
        }

        /* 
         * -1: Match parent -2: Wrap content
         * This is necessary or the view will not be 
         * stetched
         */
        li.addView(gradeButtonsView, -1, -2);
        rootView.addView(li, -1, -2);
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
        }

        @Override
        public Card doInBackground(Integer... grades) {
            assert grades.length == 1 : "Grade more than 1 time";
            int grade = grades[0];
            LearningData ld = getCurrentCard().getLearningData();
            if (ld.getAcqReps() == 0) {
                isNewCard = true;
            }

            // This was saved to determine the stat info
            // and the card id for undo
            prevCard = getCurrentCard();

            // Save previous learning for Undo
            // This part is ugly due to muutablity of ORMLite
            prevLearningData = new LearningData();
            prevLearningData.setId(ld.getId());
            prevLearningData.cloneFromLearningData(ld);

            LearningData newLd = scheduler.schedule(ld, grade, true);

            // Need to clone the data due to ORMLite restriction on "update()" method.
            ld.cloneFromLearningData(newLd);
            Card currentCard = getCurrentCard();
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
            if(result == null){
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
            setCurrentCard(result);
            displayCard(false);
            setSmallTitle(getActivityTitleString());
        }
    }


    private class WaitDbTask extends AsyncTask<Void, Void, Void>{
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute(){
            super.onPreExecute();
            progressDialog = new ProgressDialog(StudyActivity.this);
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
        public void onPostExecute(Void result){
            super.onPostExecute(result);
            if (!isCancelled()) {
                progressDialog.dismiss();
            }
        }
    }

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
            Intent myIntent = new Intent(StudyActivity.this, StudyActivity.class);
            myIntent.putExtra(EXTRA_DBPATH, dbPath);
            myIntent.putExtra(EXTRA_CATEGORY_ID, filterCategoryId);

            if (getCurrentCard() != null ) {
                myIntent.putExtra(EXTRA_START_CARD_ID, getCurrentCard().getId());
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
                setCurrentCard(null);
                restartActivity();
            }
        };

    protected void onClickQuestionText() {
        if (!isAnswerShown()) {
            displayCard(true);
        }
    }

    protected void onClickAnswerText() {
        if (!isAnswerShown()) {
            displayCard(true);
        }
    }

    protected void onClickQuestionView() {
        if (!isAnswerShown()) {
            displayCard(true);
        }
    }

    protected void onClickAnswerView() {
        if (!isAnswerShown()) {
            displayCard(true);
        }
    }

    private void setGradeButtonTitle() {
        gradeButtons.setButtonText(0, getString(R.string.memo_btn0_text), ""+ getIntervalToDisplay(scheduler.schedule(getCurrentCard().getLearningData(), 0, false)));
        gradeButtons.setButtonText(1, getString(R.string.memo_btn1_text), ""+ getIntervalToDisplay(scheduler.schedule(getCurrentCard().getLearningData(), 1, false)));
        gradeButtons.setButtonText(2, getString(R.string.memo_btn2_text), ""+ getIntervalToDisplay(scheduler.schedule(getCurrentCard().getLearningData(), 2, false)));
        gradeButtons.setButtonText(3, getString(R.string.memo_btn3_text), ""+ getIntervalToDisplay(scheduler.schedule(getCurrentCard().getLearningData(), 3, false)));
        gradeButtons.setButtonText(4, getString(R.string.memo_btn4_text), ""+ getIntervalToDisplay(scheduler.schedule(getCurrentCard().getLearningData(), 4, false)));
        gradeButtons.setButtonText(5, getString(R.string.memo_btn5_text), ""+ getIntervalToDisplay(scheduler.schedule(getCurrentCard().getLearningData(), 5, false)));
    }


    private GradeButtons.OnGradeButtonClickListener onGradeButtonClickListener
        = new GradeButtons.OnGradeButtonClickListener() {

			@Override
			public void onGradeButtonClick(int grade) {
                GradeTask gradeTask = new GradeTask();
                gradeTask.execute(grade);
			}
        };

    // Interval: 12.3456 day -> "1.7 week", 4.76 -> "4.7 day"
    private String getIntervalToDisplay(LearningData ld) {
        double[] dividers = {365, 30, 7, 1};
        String[] unitName = {getString(R.string.year_text),
            getString(R.string.month_text),
            getString(R.string.week_text),
            getString(R.string.day_text)};
        double interval = ld.getInterval();

        for (int i = 0; i < dividers.length; i++) {
            double divider = dividers[i];
                
            if ((interval / divider) >= 1.0 || i == (dividers.length - 1)) {
                return "" + Double.toString(((double)Math.round(interval / divider * 10)) / 10) + " " + unitName[i];
            }
        }
        return "";
    }

    private String getActivityTitleString(){
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.new_text) + ": " + newCardCount + " ");
        sb.append(getString(R.string.review_short_text) + ": " + schedluledCardCount + " ");
        sb.append(getString(R.string.id_text) + ": " + getCurrentCard().getId() + " ");
        if (StringUtils.isNotEmpty(getCurrentCard().getCategory().getName())) {
            sb.append(getString(R.string.category_short_text) + ": " + getCurrentCard().getCategory().getName());
        }
        return sb.toString();
    }

}
