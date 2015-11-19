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

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.queue.QueueManager;
import org.liberty.android.fantastischmemo.queue.QuizQueueManager;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.ui.loader.DBLoader;
import org.liberty.android.fantastischmemo.utils.DictionaryUtil;

import roboguice.util.RoboAsyncTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.apis.graphics.FingerPaint;
import com.google.common.base.Strings;

public class QuizActivity extends QACardActivity {
    public static String EXTRA_CATEGORY_ID = "category_id";
    public static String EXTRA_START_CARD_ORD = "start_card_ord";
    public static String EXTRA_QUIZ_SIZE = "quiz_size";
    public static String EXTRA_SHUFFLE_CARDS = "shuffle_cards";
    public static String EXTRA_START_CARD_ID = "start_card_id";

    /* UI elements */
    private GradeButtonsFragment gradeButtonsFragment;

    /* Settings */
    private Setting setting;
    private Option option;

    /* Utils */
    private DictionaryUtil dictionaryUtil;

    private QuizQueueManager queueManager;

    private int startCardId = -1;
    private int categoryId = -1;
    private int startCardOrd = -1;
    private int quizSize = -1;

    private boolean isNewCardsCompleted = false;

    private boolean shuffleCards = false;

    private int totalQuizSize = -1;

    @Inject
    public void setDictionaryUtil(DictionaryUtil dictionaryUtil) {
        this.dictionaryUtil = dictionaryUtil;
    }

    @Override
    public int getContentView() {
        return R.layout.qa_card_layout_study;
    }

    @Override
    public void onPostInit() {
        super.onPostInit();
        setting = getSetting();
        option = getOption();

        createQueue();

        // Keep track the initial total quiz size.
        totalQuizSize = queueManager.getNewQueueSize();

        /* Run the learnQueue init in a separate thread */
        if (startCardId != -1) {
            setCurrentCard(queueManager.dequeuePosition(startCardId));
        } else {
            setCurrentCard(queueManager.dequeue());
        }
        if (getCurrentCard() == null) {
            showNoItemDialog();
            return;
        }
        setupGradeButtons();
        displayCard(false);
        setSmallTitle(getActivityTitleString());
        setTitle(getDbName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        categoryId = extras.getInt(EXTRA_CATEGORY_ID, -1);
        startCardOrd = extras.getInt(EXTRA_START_CARD_ORD, -1);
        quizSize = extras.getInt(EXTRA_QUIZ_SIZE, -1);
        shuffleCards = extras.getBoolean(EXTRA_SHUFFLE_CARDS, false);
        if (savedInstanceState != null) {
            startCardId = savedInstanceState.getInt(EXTRA_START_CARD_ID, -1);
        }

        getMultipleLoaderManager().registerLoaderCallbacks(3, new QuizQueueManagerLoaderCallbacks(), false);

        startInit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Card currentCard = getCurrentCard();
        if (currentCard != null) {
            outState.putInt(EXTRA_START_CARD_ID, currentCard.getId());
        }
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
    protected boolean onClickQuestionText() {
        if ((option.getSpeakingType() == Option.SpeakingType.AUTOTAP
                || option.getSpeakingType() == Option.SpeakingType.TAP)) {
            speakQuestion();
        } else {
            onClickQuestionView();
        }
        return true;
    }

    @Override
    protected boolean  onClickAnswerText() {
        if (!isAnswerShown()) {
            onClickAnswerView();
        } else if ((option.getSpeakingType() == Option.SpeakingType.AUTOTAP
                || option.getSpeakingType() == Option.SpeakingType.TAP)) {
            speakAnswer();
        }
        return true;
    }

    @Override
    protected boolean onClickQuestionView() {
        if (!isAnswerShown()) {
            displayCard(true);
        }
        return true;
    }

    @Override
    protected boolean onClickAnswerView() {
        if (!isAnswerShown()) {
            displayCard(true);
        } else if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED && isAnswerShown()) {
            displayCard(false);
        }
        return true;
    }

    @Override
    protected boolean onVolumeUpKeyPressed() {
        if (isAnswerShown()) {
            gradeButtonsFragment.gradeCurrentCard(0);
            Toast.makeText(this, getString(R.string.grade_text) + " 0", Toast.LENGTH_SHORT).show();
        } else {
            displayCard(true);
        }

        return true;
    }

    @Override
    protected boolean onVolumeDownKeyPressed() {
        if (isAnswerShown()) {
            gradeButtonsFragment.gradeCurrentCard(3);
            Toast.makeText(this, getString(R.string.grade_text) + " 3", Toast.LENGTH_SHORT).show();
        } else {
            displayCard(true);
        }
        return true;
    }

    private static class QuizQueueManagerLoader extends
            DBLoader<QueueManager> {

        private Scheduler scheduler;

        private int filterCategoryId = -1;

        private int startCardOrd = -1;

        private int quizSize = 0;

        private boolean shuffleCards = false;

        public QuizQueueManagerLoader(Context context,
                String dbPath, int filterCategoryId,
                int startCardOrd, int quizSize,
                boolean shuffleCards) {
            super(context, dbPath);

            this.filterCategoryId = filterCategoryId;

            this.startCardOrd = startCardOrd;

            this.quizSize = quizSize;

            this.shuffleCards = shuffleCards;

        }

        @Inject
        public void setScheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public QueueManager dbLoadInBackground() {
            Category filterCategory = null;

            if (filterCategoryId != -1) {
                filterCategory = dbOpenHelper.getCategoryDao().queryForId(filterCategoryId);
            }

            QuizQueueManager.Builder builder = new QuizQueueManager.Builder()
                .setDbOpenHelper(dbOpenHelper)
                .setScheduler(scheduler)
                .setStartCardOrd(startCardOrd)
                .setFilterCategory(filterCategory)
                .setShuffle(shuffleCards);

            if (startCardOrd != -1) {
                builder.setStartCardOrd(startCardOrd)
                    .setQuizSize(quizSize);
            }

            return builder.build();
        }

    }

    private class QuizQueueManagerLoaderCallbacks implements
            LoaderManager.LoaderCallbacks<QueueManager> {
        @Override
        public Loader<QueueManager> onCreateLoader(int arg0, Bundle arg1) {
             Loader<QueueManager> loader = new QuizQueueManagerLoader(getApplicationContext(), getDbPath(),
                     categoryId, startCardOrd, quizSize, shuffleCards);
             loader.forceLoad();
             return loader;
        }

        @Override
        public void onLoadFinished(Loader<QueueManager> loader , QueueManager queueManager) {
            QuizActivity.this.queueManager = (QuizQueueManager) queueManager;
            getMultipleLoaderManager().checkAllLoadersCompleted();
        }
        @Override
        public void onLoaderReset(Loader<QueueManager> arg0) {
            // Do nothing now
        }
    }

    private void createQueue() {
    }

    @Override
    public void onPostDisplayCard() {
        // When displaying new card, we should stop the TTS reading.
        getCardTTSUtil().stopSpeak();

        if (isAnswerShown()) {
            gradeButtonsFragment.setVisibility(View.VISIBLE);
        } else {
            // The grade button should be gone for double sided cards.
            if (setting.getCardStyle() ==  Setting.CardStyle.DOUBLE_SIDED) {
                gradeButtonsFragment.setVisibility(View.GONE);
            } else {
                gradeButtonsFragment.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void setupGradeButtons() {
        gradeButtonsFragment = new GradeButtonsFragment();

        Bundle args = new Bundle();
        args.putString(GradeButtonsFragment.EXTRA_DBPATH, getDbPath());
        gradeButtonsFragment.setArguments(args);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.buttons_root, gradeButtonsFragment);
        ft.commit();

        gradeButtonsFragment.setOnCardChangedListener(onCardChangedListener);
    }

    private CharSequence getActivityTitleString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.quiz_text) + ": " + (totalQuizSize - queueManager.getNewQueueSize()) + "/" + totalQuizSize + " ");
        sb.append(getString(R.string.review_short_text) + ": " + queueManager.getReviewQueueSize()+ " ");
        sb.append(getString(R.string.id_text) + ": " + getCurrentCard().getId() + " ");
        if (!Strings.isNullOrEmpty(getCurrentCard().getCategory().getName())) {
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

    private GradeButtonsFragment.OnCardChangedListener onCardChangedListener =
        new GradeButtonsFragment.OnCardChangedListener() {
            public void onCardChanged(Card prevCard, Card updatedCard) {
                gradeButtonsFragment.setVisibility(View.INVISIBLE);

                // Run the task to update the updatedCard in the queue
                // and dequeue the next card 
                ChangeCardTask task = new ChangeCardTask(QuizActivity.this, updatedCard);
                task.execute(); 
            }
        };

    // Task to change the card after a card is graded
    // It needs to update the old card and dequeue the new card
    // and display it.
    private class ChangeCardTask extends RoboAsyncTask<Card> {

        private int newQueueSizeBeforeDequeue;

        private int reviewQueueSizeBeforeDequeue;

        private Card updatedCard;

        public ChangeCardTask(Context context, Card updatedCard) {
            super(context);
            this.updatedCard = updatedCard;
        }

        @Override
        public Card call() throws Exception {
            queueManager.remove(getCurrentCard());
            queueManager.update(updatedCard);

            // Keep track of two values to dermine when to display dialog
            // to promote the quiz completion
            newQueueSizeBeforeDequeue = queueManager.getNewQueueSize();
            reviewQueueSizeBeforeDequeue = queueManager.getReviewQueueSize();

            Card nextCard = queueManager.dequeue();
            return nextCard;
        }

        public void onSuccess(Card result) {
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
