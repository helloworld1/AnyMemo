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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.mycommons.lang3.StringUtils;

import org.liberty.android.fantastischmemo.AMEnv;

import org.liberty.android.fantastischmemo.queue.LearnQueueManager;
import org.liberty.android.fantastischmemo.queue.QuizQueueManager;
import org.liberty.android.fantastischmemo.ui.DetailScreen;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.ui.SettingsScreen;
import org.liberty.android.fantastischmemo.ui.StudyActivity;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;
import org.liberty.android.fantastischmemo.utils.AMStringUtil;
import org.liberty.android.fantastischmemo.utils.AnyMemoExecutor;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;

import org.liberty.android.fantastischmemo.queue.QueueManager;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.scheduler.DefaultScheduler;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;

import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTSImpl;

import org.liberty.android.fantastischmemo.utils.DictionaryUtil;

import com.example.android.apis.graphics.FingerPaint;

import android.os.AsyncTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import android.view.Gravity;
import android.view.Menu;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.util.Log;
import android.net.Uri;

import org.liberty.android.fantastischmemo.ui.CategoryEditorFragment.CategoryEditorResultListener;

import android.widget.Toast;

public class QuizActivity extends QACardActivity {
    public static String EXTRA_START_CARD_ID = "start_card_id";
    public static String EXTRA_CATEGORY_ID = "category_id";
    public static String EXTRA_START_CARD_ORD = "start_card_ord";
    public static String EXTRA_QUIZ_SIZE = "quiz_size";

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
	public void onCreate(Bundle savedInstanceState){
        Bundle extras = getIntent().getExtras();
        categoryId = extras.getInt(EXTRA_CATEGORY_ID, -1);
        startCardOrd = extras.getInt(EXTRA_START_CARD_ORD, -1);
        quizSize = extras.getInt(EXTRA_QUIZ_SIZE, -1);

        super.onCreate(savedInstanceState);
    }

    private void createQueue() {
        QuizQueueManager.Builder builder = new QuizQueueManager.Builder()
            .setDbOpenHelper(getDbOpenHelper())
            .setScheduler(scheduler)
            .setStartCardOrd(startCardOrd)
            .setFilterCategory(filterCategory);

        if (startCardOrd != -1) {
            builder.setStartCardOrd(startCardOrd)
                .setQuizSize(quizSize);
        }

        if (option.getShuffleType() == Option.ShuffleType.LOCAL) {
            builder.setShuffle(true);
        } else {
            builder.setShuffle(false);
        }
        queueManager = (QuizQueueManager) builder.build();
    }

    private void refreshStatInfo() {

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

            if (queueManager.getNewQueueSize() <= 0) {
                showCompleteNewDialog();
                return;
            }

            // Stat data
            setCurrentCard(result);
            displayCard(false);
            setSmallTitle(getActivityTitleString());
        }
    }

    private CharSequence getActivityTitleString() {
        return null;
    }

    /* Called when all quiz is completed */
    private void showCompleteAllDialog() {
    }

    /* Called when all new cards are completed. */
    private void showCompleteNewDialog() {
    }

}
