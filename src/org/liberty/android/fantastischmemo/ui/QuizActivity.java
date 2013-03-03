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

import org.apache.mycommons.lang3.StringUtils;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.queue.QuizQueueManager;
import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.utils.AMStringUtil;
import org.liberty.android.fantastischmemo.utils.DictionaryUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.apis.graphics.FingerPaint;

public class QuizActivity extends QACardActivity {
    public static String EXTRA_START_CARD_ID = "start_card_id";
    public static String EXTRA_CATEGORY_ID = "category_id";
    public static String EXTRA_START_CARD_ORD = "start_card_ord";
    public static String EXTRA_QUIZ_SIZE = "quiz_size";
    public static String EXTRA_SHUFFLE_CARDS = "shuffle_cards";

    private CardDao cardDao;
    private LearningDataDao learningDataDao;
    private CategoryDao categoryDao;
    

    /* UI elements */
    private GradeButtons gradeButtons;

    /* Settings */
    private Setting setting;
    private Option option;
    
    /* Utils */
    private DictionaryUtil dictionaryUtil;
    private AMStringUtil amStringUtil;

    /* Schedulers */
    private Scheduler scheduler = null;

    private QuizQueueManager queueManager;

    private int startCardId = -1;
    private int categoryId = -1;
    private int startCardOrd = -1;
    private int quizSize = -1;

    private Category filterCategory;

    private boolean initialized = false;

    private boolean isNewCardsCompleted = false;

    private boolean shuffleCards = false;

    private int totalQuizSize = -1;

    @Override
    public void onInit() throws Exception {
        cardDao = getDbOpenHelper().getCardDao();
        learningDataDao = getDbOpenHelper().getLearningDataDao();
        categoryDao = getDbOpenHelper().getCategoryDao();
        setting = getSetting();
        option = getOption();
        dictionaryUtil = new DictionaryUtil(this);
        amStringUtil = new AMStringUtil(this);
        if (categoryId!= -1) {
            filterCategory = categoryDao.queryForId(categoryId);
            assert filterCategory != null : "Query filter id: " + categoryId + ". Get null";
        }

        scheduler = new DefaultScheduler(this);

        createQueue();

        // Keep track the initial total quiz size.
        totalQuizSize = queueManager.getNewQueueSize();

        /* Run the learnQueue init in a separate thread */
        if (startCardId != -1) {
            setCurrentCard(queueManager.dequeuePosition(startCardId));
        } else {
            setCurrentCard(queueManager.dequeue());
        }
    }

    @Override
    public void onPostInit() {
        if (getCurrentCard() == null) {
            showNoItemDialog();
            return;
        }
        setupGradeButtons();
        displayCard(false);
        initialized = true;
        setSmallTitle(getActivityTitleString());
        setTitle(getDbName());
    }

    @Override
	public void onCreate(Bundle savedInstanceState){
        Bundle extras = getIntent().getExtras();
        categoryId = extras.getInt(EXTRA_CATEGORY_ID, -1);
        startCardOrd = extras.getInt(EXTRA_START_CARD_ORD, -1);
        quizSize = extras.getInt(EXTRA_QUIZ_SIZE, -1);
        shuffleCards = extras.getBoolean(EXTRA_SHUFFLE_CARDS, false);

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quiz_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_lookup:
            {
                dictionaryUtil.showLookupListDialog("" + getCurrentCard().getQuestion() + " " + getCurrentCard().getAnswer());
                break;
            }
            case R.id.menu_speak_question:
            {
                speakQuestion();
                break;
            }
            case R.id.menu_speak_answer:
            {
                speakAnswer();
                break;
            }
            case R.id.menu_paint:
            {
                Intent myIntent = new Intent(this, FingerPaint.class);
                startActivity(myIntent);
            }
        }
        return false;
    }

    @Override
    protected void onClickQuestionText() {
        if ((option.getSpeakingType() == Option.SpeakingType.AUTOTAP
                || option.getSpeakingType() == Option.SpeakingType.TAP)) {
            speakQuestion();
        } else {
            onClickQuestionView();
        }
    }

    @Override
    protected void onClickAnswerText() {
        if (!isAnswerShown()) {
            onClickAnswerView();
        } else if ((option.getSpeakingType() == Option.SpeakingType.AUTOTAP
                || option.getSpeakingType() == Option.SpeakingType.TAP)) {
            speakAnswer();
        }
    }

    @Override
    protected void onClickQuestionView() {
        if (!isAnswerShown()) {
            displayCard(true);
        }
    }

    @Override
    protected void onClickAnswerView() {
        if (!isAnswerShown()) {
            displayCard(true);
        } else if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED && isAnswerShown()) {
            displayCard(false);
        }
    }

    @Override
    protected boolean onVolumeUpKeyPressed() {
        if (isAnswerShown()) {
            onGradeButtonClickListener.onGradeButtonClick(0);
            Toast.makeText(this, getString(R.string.grade_text) + " 0", Toast.LENGTH_SHORT).show();
        } else {
            displayCard(true);
        }

        return true;
    }

    @Override
    protected boolean onVolumeDownKeyPressed() {
        if (isAnswerShown()) {
            onGradeButtonClickListener.onGradeButtonClick(3);
            Toast.makeText(this, getString(R.string.grade_text) + " 3", Toast.LENGTH_SHORT).show();
        } else {
            displayCard(true);
        }
        return true;
    }

    private void createQueue() {
        QuizQueueManager.Builder builder = new QuizQueueManager.Builder()
            .setDbOpenHelper(getDbOpenHelper())
            .setScheduler(scheduler)
            .setStartCardOrd(startCardOrd)
            .setFilterCategory(filterCategory)
            .setShuffle(shuffleCards);

        if (startCardOrd != -1) {
            builder.setStartCardOrd(startCardOrd)
                .setQuizSize(quizSize);
        }

        queueManager = (QuizQueueManager) builder.build();
    }

    @Override
    public void onPostDisplayCard() {
        // When displaying new card, we should stop the TTS reading.
        stopSpeak();
        if (isAnswerShown()) {
            gradeButtons.show();
        } else {
            gradeButtons.hide();
        }
    }

    private void setupGradeButtons() {
        gradeButtons = new GradeButtons(this, R.layout.grade_buttons_anki);

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
        li.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        Integer color = setting.getAnswerBackgroundColor();
        if (color != null) {
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

    private GradeButtons.OnGradeButtonClickListener onGradeButtonClickListener
        = new GradeButtons.OnGradeButtonClickListener() {

			@Override
			public void onGradeButtonClick(int grade) {
                GradeTask gradeTask = new GradeTask();
                gradeTask.execute(grade);
			}
        };

    /*
     * Use AsyncTask to update the database and update the statistics
     * information
     */
    private class GradeTask extends AsyncTask<Integer, Void, Card>{

        private int newQueueSizeBeforeDequeue;

        private int reviewQueueSizeBeforeDequeue;

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

            LearningData newLd = scheduler.schedule(ld, grade, true);

            // Need to clone the data due to ORMLite restriction on "update()" method.
            ld.cloneFromLearningData(newLd);
            Card currentCard = getCurrentCard();
            currentCard.setLearningData(ld);
            queueManager.update(currentCard);

            // Keep track of two values to dermine when to display dialog
            // to promote the quiz completion
            newQueueSizeBeforeDequeue = queueManager.getNewQueueSize();
            reviewQueueSizeBeforeDequeue = queueManager.getReviewQueueSize();

            Card nextCard = queueManager.dequeue();
            return nextCard;
        }

        @Override
        public void onCancelled() {
            return;
        }

        @Override
        public void onPostExecute(Card result) {
            super.onPostExecute(result);
            setProgressBarIndeterminateVisibility(false);
            if(result == null){
                showCompleteAllDialog();
                return;
            }

            if (newQueueSizeBeforeDequeue <= 0 && !isNewCardsCompleted) {
                showCompleteNewDialog(totalQuizSize - reviewQueueSizeBeforeDequeue);
                isNewCardsCompleted = true;
            }

            // Stat data
            setCurrentCard(result);
            displayCard(false);
            setSmallTitle(getActivityTitleString());
        }
    }

    private CharSequence getActivityTitleString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.quiz_text) + ": " + (totalQuizSize - queueManager.getNewQueueSize()) + "/" + totalQuizSize + " ");
        sb.append(getString(R.string.review_short_text) + ": " + queueManager.getReviewQueueSize()+ " ");
        sb.append(getString(R.string.id_text) + ": " + getCurrentCard().getId() + " ");
        if (StringUtils.isNotEmpty(getCurrentCard().getCategory().getName())) {
            sb.append(getString(R.string.category_short_text) + ": " + getCurrentCard().getCategory().getName());
        }
        return sb.toString();
    }

    /* Called when all quiz is completed */
    private void showCompleteAllDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.quiz_completed_text)
            .setMessage(R.string.quiz_complete_summary)
            .setPositiveButton(R.string.back_menu_text, flushAndQuitListener)
            .setCancelable(false)
            .show();
    }

    /* Called when all new cards are completed. */
    private void showCompleteNewDialog(int correct) {
        LayoutInflater layoutInflater
            = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.quiz_summary_dialog, null);
        TextView scoreView = (TextView) view.findViewById(R.id.score_text);
        int score = correct * 100 / totalQuizSize;

        scoreView.setText("" + score + "% (" + correct + "/" + totalQuizSize + ")");
        new AlertDialog.Builder(this)
            .setTitle(R.string.quiz_completed_text)
            .setView(view)
            .setPositiveButton(R.string.review_text, null)
            .setNegativeButton(R.string.cancel_text, flushAndQuitListener)
            .setCancelable(false)
            .show();
    }
    
    // Current flush is not functional. So this method only quit and does not flush
    // the queue.
    private DialogInterface.OnClickListener flushAndQuitListener =
        new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
			}
        };

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
            .setOnCancelListener(new DialogInterface.OnCancelListener(){
                public void onCancel(DialogInterface dialog){
                    finish();
                }
            })
            .create()
            .show();
    }
}
