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

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.service.AnyMemoService;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;
import org.liberty.android.fantastischmemo.utils.AnyMemoExecutor;
import org.liberty.android.fantastischmemo.utils.CardTTSUtil;
import org.liberty.android.fantastischmemo.utils.CardTTSUtilFactory;
import org.liberty.android.fantastischmemo.utils.CardTextUtil;
import org.liberty.android.fantastischmemo.utils.CardTextUtilFactory;

import roboguice.RoboGuice;
import roboguice.inject.ContextScope;
import roboguice.util.RoboAsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public abstract class QACardActivity extends AMActivity {
    public static String EXTRA_DBPATH = "dbpath";

    private String dbPath;

    private String dbName;

    private AnyMemoDBOpenHelper dbOpenHelper;

    /* DAOs */
    private SettingDao settingDao;

    private Card currentCard;

    private int animationInResId = 0;
    private int animationOutResId = 0;

    private Option option;

    private Setting setting;

    private InitTask initTask = null;

    private WaitDbTask waitDbTask = null;

    private boolean isAnswerShown = true;

    private TextView smallTitleBar;

    private CardTTSUtilFactory cardTTSUtilFactory;

    private CardTTSUtil cardTTSUtil;

    private CardTextUtilFactory cardTextUtilFactory;

    private CardTextUtil cardTextUtil;

    private GestureLibrary gestureLibrary;

    private volatile boolean initFinished = false;

    @Inject
    public void setOption(Option option) {
         this.option = option;
    }

    @Inject
    public void setCardTTSUtilFactory(CardTTSUtilFactory cardTTSUtilFactory) {
        this.cardTTSUtilFactory = cardTTSUtilFactory;
    }

    @Inject
    public void setCardTextUtilFactory(CardTextUtilFactory cardTextUtilFactory) {
        this.cardTextUtilFactory = cardTextUtilFactory;
    }


    public CardTTSUtil getCardTTSUtil() {
        return cardTTSUtil;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dbPath = extras.getString(EXTRA_DBPATH);
        }

        dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(this, dbPath);
        dbName = FilenameUtils.getName(dbPath);

        dbPath = extras.getString(EXTRA_DBPATH);
        setContentView(getContentView());

        // Set teh default animation
        animationInResId = R.anim.slide_left_in;
        animationOutResId = R.anim.slide_left_out;

        // Load gestures
        loadGestures();

        initTask = new InitTask(this);
        initTask.execute();
    }

    public int getContentView() {
        return R.layout.qa_card_layout;
    }

    protected void setCurrentCard(Card card) {
        currentCard = card;
    }

    protected Card getCurrentCard() {
        return currentCard;
    }

    protected String getDbPath() {
        return dbPath;
    }

    protected String getDbName() {
        return dbName;
    }

    // Important class that display the card using fragment
    // the showAnswer parameter is handled differently on single
    // sided card and double sided card.
    protected void displayCard(boolean showAnswer) {

        // First prepare the text to display

        String questionTypeface = setting.getQuestionFont();
        String answerTypeface = setting.getAnswerFont();

        Setting.Align questionAlign = setting.getQuestionTextAlign();
        Setting.Align answerAlign = setting.getAnswerTextAlign();


        String questionTypefaceValue = null;
        String answerTypefaceValue = null;
        /* Set the typeface of question and answer */
        if (StringUtils.isNotEmpty(questionTypeface)) {
            questionTypefaceValue = questionTypeface;

        }
        if (StringUtils.isNotEmpty(answerTypeface)) {
            answerTypefaceValue = answerTypeface;
        }

        // Handle the QA ratio
        LinearLayout questionLayout = (LinearLayout) findViewById(R.id.question);
        LinearLayout answerLayout = (LinearLayout) findViewById(R.id.answer);
        float qRatio = setting.getQaRatio();
        if (qRatio > 99.0f) {
            answerLayout.setVisibility(View.GONE);
            questionLayout.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
            answerLayout.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        } else if (qRatio < 1.0f) {
            questionLayout.setVisibility(View.GONE);
            questionLayout.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
            answerLayout.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        } else {
            questionLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT, qRatio));
            answerLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT, 100f - qRatio));
        }

        // Buttons view can be null if it is not decleared in the layout XML
        View buttonsView = findViewById(R.id.buttons_root);

        // Make sure the buttons view are also handling the event for the answer view
        // e. g. clicking on the blank area of the buttons layout to reveal the answer
        // or flip the card.
        if (buttonsView != null) {
            buttonsView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onQuestionViewClickListener.onClick(v);
                }
            });
        }
        // Double sided card has no animation and no horizontal line
        if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED) {
            if (showAnswer) {
                findViewById(R.id.question).setVisibility(View.GONE);
                findViewById(R.id.answer).setVisibility(View.VISIBLE);

                // Also the buttons should match the color.
                if (buttonsView != null) {
                    buttonsView.setBackgroundColor(setting.getAnswerBackgroundColor());
                }
            } else {
                findViewById(R.id.question).setVisibility(View.VISIBLE);
                findViewById(R.id.answer).setVisibility(View.GONE);

                // Also the buttons should match the color.
                if (buttonsView != null) {
                    buttonsView.setBackgroundColor(setting.getQuestionBackgroundColor());
                }
            }
            findViewById(R.id.horizontal_line).setVisibility(View.GONE);
        }

        // Set the color of the horizontal line
        View horizontalLine = findViewById(R.id.horizontal_line);
        horizontalLine.setBackgroundColor(setting.getSeparatorColor());

        List<Spannable> spannableFields = cardTextUtil.getFieldsToDisplay(getCurrentCard());

        // Question spannable
        Spannable sq = spannableFields.get(0);

        // Answer spannable
        Spannable sa = spannableFields.get(1);

        // Finally we generate the fragments
        CardFragment questionFragment = new CardFragment.Builder(sq)
                .setTextAlignment(questionAlign)
                .setTypefaceFromFile(questionTypefaceValue)
                .setTextOnClickListener(onQuestionTextClickListener)
                .setCardOnClickListener(onQuestionViewClickListener)
                .setTextFontSize(setting.getQuestionFontSize())
                .setTypefaceFromFile(setting.getQuestionFont())
                .setTextColor(setting.getQuestionTextColor())
                .setBackgroundColor(setting.getQuestionBackgroundColor())
                .build();

        CardFragment answerFragment = null;

        if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED
                || showAnswer) {
            answerFragment = new CardFragment.Builder(sa)
                    .setTextAlignment(answerAlign)
                    .setTypefaceFromFile(answerTypefaceValue)
                    .setTextOnClickListener(onAnswerTextClickListener)
                    .setCardOnClickListener(onAnswerViewClickListener)
                    .setTextFontSize(setting.getAnswerFontSize())
                    .setTextColor(setting.getAnswerTextColor())
                    .setBackgroundColor(setting.getAnswerBackgroundColor())
                    .setTypefaceFromFile(setting.getAnswerFont())
                    .build();
        } else {
            // For "Show answer" text, we do not use the
            // alignment from the settings.
            // It is always center aligned
            answerFragment = new CardFragment.Builder(
                    getString(R.string.memo_show_answer))
                    .setTextAlignment(Setting.Align.CENTER)
                    .setTypefaceFromFile(answerTypefaceValue)
                    .setTextOnClickListener(onAnswerTextClickListener)
                    .setCardOnClickListener(onAnswerViewClickListener)
                    .setTextFontSize(setting.getAnswerFontSize())
                    .setTextColor(setting.getAnswerTextColor())
                    .setBackgroundColor(setting.getAnswerBackgroundColor())
                    .setTypefaceFromFile(setting.getAnswerFont())
                    .build();
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (setting.getCardStyle() != Setting.CardStyle.DOUBLE_SIDED
                && option.getEnableAnimation()) {
            if (isAnswerShown == false && showAnswer == true) {
                // No animation here.
            } else {
                ft.setCustomAnimations(animationInResId, animationOutResId);
            }
        }
        ft.replace(R.id.question, questionFragment);
        ft.commit();

        ft = getSupportFragmentManager().beginTransaction();

        if (option.getEnableAnimation()) {
            if (setting.getCardStyle() != Setting.CardStyle.DOUBLE_SIDED) {
                if (isAnswerShown == false && showAnswer == true) {
                    ft.setCustomAnimations(0, R.anim.slide_down);
                } else {
                    ft.setCustomAnimations(animationInResId, animationOutResId);
                }
            } else {
                // Animation for double sided cards
                // Current no animation
            }
        }

        ft.replace(R.id.answer, answerFragment);
        ft.commit();


        isAnswerShown = showAnswer;

        // Set up the small title bar
        // It is defualt "GONE" so it won't take any space
        // if there is no text
        smallTitleBar = (TextView) findViewById(R.id.small_title_bar);

        // Only copy to clipboard if answer is show
        // as a feature request:
        // http://code.google.com/p/anymemo/issues/detail?id=239
        if (showAnswer == true) {
            copyToClipboard();
        }

        onPostDisplayCard();
    }

    protected boolean isAnswerShown() {
        return isAnswerShown;
    }

    protected AnyMemoDBOpenHelper getDbOpenHelper() {
        return dbOpenHelper;
    }

    protected Setting getSetting() {
        return setting;
    }

    protected Option getOption() {
        return option;
    }

    // Call when initializing thing
    abstract protected void onInit() throws Exception;

    // Called when the initalizing finished.
    protected void onPostInit() {
        // Do nothing

    }

    // Set the card animation, 0 = no animation
    protected void setAnimation(int animationInResId, int animationOutResId) {
        this.animationInResId = animationInResId;
        this.animationOutResId = animationOutResId;
    }

    private class InitTask extends RoboAsyncTask<Void> {

        private ProgressDialog progressDialog;

        private Context context;

        public InitTask(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Get the context scope in UI thread (Who hold the ThreadLocal context scope
        }

        @Override
        public Void call() throws Exception {
            settingDao = dbOpenHelper.getSettingDao();
            setting = settingDao.queryForId(1);

            ContextScope scope = RoboGuice.getInjector(context).getInstance(ContextScope.class);

            // Make sure the method is running under the context
            // The AsyncTask thread does not have the context, so we need
            // to manually enter the scope.
            synchronized(ContextScope.class) {
                scope.enter(context);
                try {
                    cardTTSUtil = cardTTSUtilFactory.create(dbPath);
                    cardTextUtil = cardTextUtilFactory.create(dbPath);
                    // Init of common functions here
                    // Call customized init funciton defined in
                    // the subclass
                    onInit();
                } finally {
                    scope.exit(context);
                }
            }

            return null;
        }

        @Override
        public void onSuccess(Void result) {
            // Make sure the background color of grade buttons matches the answer's backgroud color.
            // buttonsView can be null if the layout does not have buttons_root
            View buttonsView = findViewById(R.id.buttons_root);
            if (buttonsView != null) {
                buttonsView.setBackgroundColor(setting.getAnswerBackgroundColor());
            }
            
            // Call customized method when init completed
            onPostInit();
        }

        @Override
        public void onException(Exception e) throws RuntimeException {
            AMGUIUtility.displayError(QACardActivity.this,
                    getString(R.string.exception_text),
                    getString(R.string.exception_message), e);
        }

        @Override
        public void onFinally() {
            progressDialog.dismiss();
            initFinished = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only if the initTask has been finished and no waitDbTask is waiting.
        if (initFinished && (waitDbTask == null || !AsyncTask.Status.RUNNING
                        .equals(waitDbTask.getStatus()))) {
            waitDbTask = new WaitDbTask();
            waitDbTask.execute((Void) null);
        } else {
            Log.i(TAG, "There is another task running. Do not run tasks");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);

        cardTTSUtil.release();

        /* Update the widget because StudyActivity can be accessed though widget*/
        Intent myIntent = new Intent(this, AnyMemoService.class);
        myIntent.putExtra("request_code", AnyMemoService.CANCEL_NOTIFICATION
                | AnyMemoService.UPDATE_WIDGET);
        startService(myIntent);
    }

    // Set the small title to display additional informaiton
    public void setSmallTitle(CharSequence text) {
        if (StringUtils.isNotEmpty(text)) {
            smallTitleBar.setText(text);
            smallTitleBar.setVisibility(View.VISIBLE);
        } else {
            smallTitleBar.setVisibility(View.GONE);
        }

    }

    /*
     * Use AsyncTask to make sure there is no running task for a db
     */
    private class WaitDbTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(QACardActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_save));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        public Void doInBackground(Void... nothing) {
            AnyMemoExecutor.waitAllTasks();
            return null;
        }

        @Override
        public void onCancelled() {
            return;
        }

        @Override
        public void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
        }
    }

    /* Called when the card is displayed. */
    protected void onPostDisplayCard() {
        // Nothing
    }

    protected boolean speakQuestion() {
        cardTTSUtil.speakCardQuestion(getCurrentCard());
        return true;
    }

    protected boolean speakAnswer() {
        cardTTSUtil.speakCardAnswer(getCurrentCard());
        return true;
    }

    private void loadGestures() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!gestureLibrary.load()) {
            Log.e(TAG, "Gestures can not be load");
        }

        GestureOverlayView gestureOverlay =  (GestureOverlayView) findViewById(R.id.gesture_overlay);
        gestureOverlay.addOnGesturePerformedListener(onGesturePerformedListener);

        // Set if gestures are enabled if set on preference
        gestureOverlay.setEnabled(option.getGestureEnabled());
    }


    // Default implementation is to handle the double sided card correctly.
    // Return true if the event is handled, else return false
    protected boolean onClickQuestionView() {
        if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED) {
            displayCard(true);
            return true;
        }
        return false;
    }

    protected boolean onClickAnswerView() {
        if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED) {
            displayCard(false);
            return true;
        }
        return false;
    }

    protected boolean onClickQuestionText() {
        if (!onClickQuestionView()) {
            speakQuestion();
        }
        return true;
    }

    protected boolean onClickAnswerText() {
        if (!onClickAnswerView()) {
            speakAnswer();
        }
        return true;
    }


    protected void onGestureDetected(GestureName gestureName) {
        // Nothing
    }

    // Return true if handled. Default not handle it.
    // This method will only be called if the volume key shortcut option is enabled.
    protected boolean onVolumeUpKeyPressed() {
        return false;
    }

    // Return true if handled. Default not handle it.
    protected boolean onVolumeDownKeyPressed() {
        return false;
    }

    // Do not handle the key down event. We handle it in onKeyUp
    // This method will only be called if the volume key shortcut option is enabled.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (option.getVolumeKeyShortcut()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    // handle the key event
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(option.getVolumeKeyShortcut()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                return onVolumeUpKeyPressed();
            }

            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                return onVolumeDownKeyPressed();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    // Copy to clipboard
    protected void copyToClipboard() {
        String copiedText = "";
        switch (option.getCopyClipboard()) {
            case QUESTION:
                copiedText = "" + currentCard.getQuestion();
                break;
            case ANSWER:
                copiedText = "" + currentCard.getAnswer();
                break;
            case BOTH:
                copiedText = "" + currentCard.getQuestion() + " " + currentCard.getAnswer();
                break;
            default:
                copiedText = "";
        }
        if (StringUtils.isNotEmpty(copiedText)) {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            // Some Samsung device doesn't have ClipboardManager. So check
            // the null here to prevent crash.
            if (cm != null) {
                cm.setText(copiedText);
            }
        }
    }

    private CardFragment.OnClickListener onQuestionTextClickListener = new CardFragment.OnClickListener() {

        @Override
        public void onClick(View v) {
            onClickQuestionText();
        }
    };

    private CardFragment.OnClickListener onAnswerTextClickListener = new CardFragment.OnClickListener() {

        @Override
        public void onClick(View v) {
            onClickAnswerText();
        }
    };

    private CardFragment.OnClickListener onQuestionViewClickListener = new CardFragment.OnClickListener() {

        @Override
        public void onClick(View v) {
            onClickQuestionView();
        }
    };
    private CardFragment.OnClickListener onAnswerViewClickListener = new CardFragment.OnClickListener() {

        @Override
        public void onClick(View v) {
            onClickAnswerView();
        }
    };

    private GestureOverlayView.OnGesturePerformedListener onGesturePerformedListener = new GestureOverlayView.OnGesturePerformedListener() {

        @Override
        public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
            List<Prediction> predictions = gestureLibrary.recognize(gesture);
            if (predictions.size() > 0 && predictions.get(0).score > 3.0) {

                GestureName name = GestureName.parse(predictions.get(0).name);
                // Run the callback on the Activity.
                onGestureDetected(name);
            }

        }
    };
}
