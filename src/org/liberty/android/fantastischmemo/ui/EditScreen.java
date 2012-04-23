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

import java.sql.SQLException;

import java.util.Map;

import org.apache.mycommons.lang3.StringUtils;

import org.apache.mycommons.lang3.math.NumberUtils;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.ui.DetailScreen;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.ui.SettingsScreen;

import org.liberty.android.fantastischmemo.ui.ListEditScreen;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;
import org.liberty.android.fantastischmemo.utils.AMUtil;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;

import org.liberty.android.fantastischmemo.tts.AnyMemoTTS;
import org.liberty.android.fantastischmemo.tts.AnyMemoTTSPlatform;
import org.liberty.android.fantastischmemo.tts.AudioFileTTS;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.EditText;
import android.util.Log;
import android.net.Uri;
import android.view.GestureDetector;

import org.liberty.android.fantastischmemo.ui.CategoryEditorFragment.CategoryEditorResultListener;

public class EditScreen extends AMActivity {
    private AnyMemoTTS questionTTS = null;
    private AnyMemoTTS answerTTS = null;
    private boolean searchInflated = false;
    private final int ACTIVITY_EDIT = 11;
    private final int ACTIVITY_SETTINGS = 15;
    private final int ACTIVITY_LIST = 16;
    private final int ACTIVITY_MERGE = 17;
    private final int ACTIVITY_DETAIL = 18;
    private final static String WEBSITE_HELP_EDIT = "http://anymemo.org/wiki/index.php?title=Editing_screen";
    private long totalCardCount = 0;

    public static String EXTRA_DBPATH = "dbpath";
    public static String EXTRA_CARD_ID = "id";
    public static String EXTRA_CATEGORY = "category";

    private Card currentCard = null;
    private Category currentCategory = null;
    private Integer savedCardId = null;
    private String dbPath = "";
    private String dbName = "";
    private int activeCategoryId = -1;
    private SettingDao settingDao;
    private CardDao cardDao;
    private LearningDataDao learningDataDao;
    private CategoryDao categoryDao;

    private FlashcardDisplay flashcardDisplay;
    private ControlButtons controlButtons;
    private View searchNextButton;
    private View searchPrevButton;


    private Setting setting;
    private InitTask initTask;
    private Option option;
    
    private GestureDetector gestureDetector;

    private AnyMemoDBOpenHelper dbOpenHelper;

    @Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        initTask = new InitTask();
        initTask.execute((Void)null);

        /* 
         * Currently always set the result to OK
         * to assume there are always some changes.
         * This may be changed in the future to reflect the
         * real changes
         */
        setResult(Activity.RESULT_OK);

    }

    @Override
    public void onDestroy(){
        AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
        if(questionTTS != null){
            questionTTS.shutdown();
        }
        if(answerTTS != null){
            answerTTS.shutdown();
        }

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_CANCELED){
            return;
        }
        /* Refresh the activity according to activities */
        switch(requestCode){
            case ACTIVITY_EDIT:
            {
                Bundle extras = data.getExtras();
                int cardId = extras.getInt(CardEditor.EXTRA_RESULT_CARD_ID, 1);
                try {
                    Card card = cardDao.queryForId(cardId);
                    if (card != null) {
                        currentCard = card;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                restartActivity();
                break;
            }

            case ACTIVITY_SETTINGS:
            {
                restartActivity();
                break;
            }
            case ACTIVITY_LIST:
            {
                restartActivity();
                break;
            }
            case ACTIVITY_MERGE:
            {
                restartActivity();
                break;
            }
            case ACTIVITY_DETAIL:
            {
                restartActivity();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

            case R.id.editmenu_settings_id:
            {
                Intent myIntent = new Intent(this, SettingsScreen.class);
                myIntent.putExtra(SettingsScreen.EXTRA_DBPATH, dbPath);
                startActivityForResult(myIntent, ACTIVITY_SETTINGS);
                return true;
            }

            case R.id.editmenu_delete_id:
            {
                deleteCurrent();
                return true;
            }


            case R.id.editmenu_detail_id:
            {
                if(currentCard != null){
                    Intent myIntent = new Intent(this, DetailScreen.class);
                    myIntent.putExtra(DetailScreen.EXTRA_DBPATH, this.dbPath);
                    myIntent.putExtra(DetailScreen.EXTRA_CARD_ID, currentCard.getId());
                    startActivityForResult(myIntent, ACTIVITY_DETAIL);
                }
                return true;
            }
            case R.id.editmenu_list_id:
            {
                // List edit mode
                Intent myIntent = new Intent(this, SettingsScreen.class);
                myIntent.setClass(this, ListEditScreen.class);
                myIntent.putExtra(EditScreen.EXTRA_DBPATH, dbPath);
                if(currentCard != null){
                    myIntent.putExtra("openid", currentCard.getId());
                }
                startActivityForResult(myIntent, ACTIVITY_LIST);
                return true;
            }

            case R.id.menu_edit_categories:
            {
                showCategoriesDialog();
                return true;
            }
            case R.id.editmenu_search_id:
            {
                createSearchOverlay();
                return true;
            }

            case R.id.editmenu_help:
            {
                Intent myIntent = new Intent();
                myIntent.setAction(Intent.ACTION_VIEW);
                myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                myIntent.setData(Uri.parse(WEBSITE_HELP_EDIT));
                startActivity(myIntent);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editscreen_context_menu, menu);
        menu.setHeaderTitle(R.string.menu_text);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuitem) {
        switch(menuitem.getItemId()) {
            case R.id.menu_context_copy:
            {

                if (currentCard != null){
                    savedCardId = currentCard.getId();
                }
                return true;
            }
            case R.id.menu_context_paste:
            {
                if (savedCardId != null && currentCard != null) {
                    try {
                        Card savedCard = cardDao.queryForId(savedCardId);
                        LearningData ld = new LearningData();
                        learningDataDao.create(ld);
                        savedCard.setLearningData(ld);
                        savedCard.setOrdinal(currentCard.getOrdinal());
                        cardDao.create(savedCard);
                        restartActivity();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                
                return true;
            }
            case R.id.menu_context_swap_current:
            {
                cardDao.swapQA(currentCard);
                restartActivity();
                return true;
            }

            case R.id.menu_context_reset_current:
            {
                learningDataDao.resetLearningData(currentCard.getLearningData());
                return true;
            }

            case R.id.menu_context_wipe:
            {
                AMGUIUtility.doConfirmProgressTask(this, R.string.settings_wipe, R.string.settings_wipe_warning, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask() {
                    @Override
                    public void doHeavyTask() {
                        learningDataDao.resetAllLearningData();
                    }
                    @Override
                    public void doUITask() {/* Do nothing */}
                });
                return true;
            }

            case R.id.menu_context_swap:
            {
        new AlertDialog.Builder(this)
            .setTitle(R.string.warning_text)
            .setIcon(R.drawable.alert_dialog_icon)
            .setMessage(R.string.settings_inverse_warning)
            .setPositiveButton(R.string.swap_text, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface arg0, int arg1){
                    AMGUIUtility.doProgressTask(EditScreen.this, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
                        public void doHeavyTask(){
                            cardDao.swapAllQA();
                        }
                        public void doUITask(){
                            restartActivity();
                        }
                    });
                }
            })
            .setNeutralButton(R.string.swapdup_text, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface arg0, int arg1){
                            AMGUIUtility.doProgressTask(EditScreen.this, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
                        public void doHeavyTask(){
                            cardDao.swapAllQADup();
                        }
                        public void doUITask(){
                            restartActivity();
                        }
                    });
                }
            })
            .setNegativeButton(R.string.cancel_text, null)
            .create()
            .show();

                return true;
            }

            case R.id.menu_context_remove_dup:
            {
                AMGUIUtility.doConfirmProgressTask(this, R.string.remove_dup_text, R.string.remove_dup_message, R.string.removing_dup_title, R.string.removing_dup_warning, new AMGUIUtility.ProgressTask() {
                    @Override
                    public void doHeavyTask() {
                        cardDao.removeDuplicates();
                    }
                    @Override
                    public void doUITask() {
                        restartActivity();
                    }
                });
                return true;
            }

            case R.id.menu_context_merge_db:
            {
                Intent myIntent = new Intent(this, DatabaseMerger.class);
                myIntent.putExtra(DatabaseMerger.EXTRA_SRC_PATH, dbPath);
                startActivityForResult(myIntent, ACTIVITY_MERGE);
                return true;
            }

            case R.id.menu_context_shuffle:
            {
                AMGUIUtility.doConfirmProgressTask(this, R.string.settings_shuffle, R.string.settings_shuffle_warning, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask() {
                    @Override
                    public void doHeavyTask() {
                        cardDao.shuffleOrdinals();
                    }
                    @Override
                    public void doUITask() {
                        restartActivity();
                    }
                });
                return true;
            }

            default:
            {
                return super.onContextItemSelected(menuitem);
            }
        }
    }

    /*
     * This method should be the same as the one in MemoScreen.
     */
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

    @Override
    public void restartActivity(){
        Intent myIntent = new Intent(this, EditScreen.class);
        assert dbPath != null : "Use null dbPath to restartAcitivity";
        myIntent.putExtra(EXTRA_DBPATH, dbPath);
        if (currentCard != null) {
            myIntent.putExtra(EXTRA_CARD_ID, currentCard.getId());
        }
        myIntent.putExtra(EXTRA_CATEGORY, activeCategoryId);
        finish();
        startActivity(myIntent);
    }


    private void composeViews(){
        LinearLayout memoRoot = (LinearLayout)findViewById(R.id.memo_screen_root);

        LinearLayout flashcardDisplayView = (LinearLayout)flashcardDisplay.getView();
        LinearLayout controlButtonsView = (LinearLayout)controlButtons.getView();
        /* This li is make the background of buttons the same as answer */
        LinearLayout li = new LinearLayout(this);
        li.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.FILL_PARENT));
        li.setBackgroundColor(setting.getAnswerBackgroundColor());

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

    void setViewListeners(){
        Map<String, Button> bm = controlButtons.getButtons();
        Button newButton = bm.get("new");
        Button editButton = bm.get("edit");
        Button prevButton = bm.get("prev");
        Button nextButton = bm.get("next");
        /* Set button listeners */
        newButton.setOnClickListener(newButtonListener);
        editButton.setOnClickListener(editButtonListener);
        prevButton.setOnClickListener(prevButtonListener);
        nextButton.setOnClickListener(nextButtonListener);
        /* For double sided card, the view can be toggled */
        if(setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED){

            flashcardDisplay.setQuestionLayoutClickListener(toggleCardSideListener);
            flashcardDisplay.setAnswerLayoutClickListener(toggleCardSideListener);
            flashcardDisplay.setQuestionTextClickListener(toggleCardSideListener);
            flashcardDisplay.setAnswerTextClickListener(toggleCardSideListener);
            flashcardDisplay.setQuestionLayoutLongClickListener(popupContextMenuListener);
            flashcardDisplay.setAnswerLayoutLongClickListener(popupContextMenuListener);
        }


    }

    private View.OnLongClickListener popupContextMenuListener = new View.OnLongClickListener(){
        public boolean onLongClick(View v){
            closeContextMenu();
            EditScreen.this.openContextMenu(flashcardDisplay.getView());
            return true;
        }
    };

    private View.OnClickListener newButtonListener = new View.OnClickListener(){
        public void onClick(View v){
                Intent myIntent = new Intent(EditScreen.this, CardEditor.class);
                myIntent.putExtra(CardEditor.EXTRA_DBPATH, dbPath);
                if (currentCard != null) {
                    myIntent.putExtra(CardEditor.EXTRA_CARD_ID, currentCard.getId());
                }
                myIntent.putExtra(CardEditor.EXTRA_IS_EDIT_NEW, true);
                //startActivityForResult(myIntent, ACTIVITY_EDIT);
                startActivityForResult(myIntent, ACTIVITY_EDIT);
        }
    };

    private View.OnClickListener editButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            if (currentCard != null) {
                Intent myIntent = new Intent(EditScreen.this, CardEditor.class);
                myIntent.putExtra(CardEditor.EXTRA_DBPATH, dbPath);
                myIntent.putExtra(CardEditor.EXTRA_CARD_ID, currentCard.getId());
                myIntent.putExtra(CardEditor.EXTRA_IS_EDIT_NEW, false);
                startActivityForResult(myIntent, ACTIVITY_EDIT);
            }
        }
    };

    private void updateTitle(){
        if (currentCard != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.total_text) + ": " + totalCardCount + " ");
            sb.append(getString(R.string.id_text) + ": " + currentCard.getId() + " ");
            sb.append(getString(R.string.ordinal_text_short) + ": " + currentCard.getOrdinal() + " ");
            sb.append(currentCard.getCategory().getName());
            setTitle(sb.toString());
        }
    }
    
    private void gotoNext(){
        if (currentCard != null) {
            currentCard = cardDao.queryNextCard(currentCard,currentCategory);
            try {
                categoryDao.refresh(currentCard.getCategory());
                learningDataDao.refresh(currentCard.getLearningData());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            assert currentCard != null : "Next card is null";
            updateCardFrontSide();
            updateTitle();
        }
    }

    private void deleteCurrent(){
        if(currentCard != null){
            new AlertDialog.Builder(EditScreen.this)
                .setTitle(getString(R.string.delete_text))
                .setMessage(getString(R.string.delete_warning))
                .setPositiveButton(getString(R.string.yes_text),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            DeleteCardTask task = new DeleteCardTask();
                            task.execute((Void)null);
                        }
                    })
                .setNegativeButton(getString(R.string.no_text), null)
                .create()
                .show();
        }
    }

    private void gotoPrev(){
        if (currentCard != null) {
            currentCard = cardDao.queryPrevCard(currentCard, currentCategory);
            try {
                categoryDao.refresh(currentCard.getCategory());
                learningDataDao.refresh(currentCard.getLearningData());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            assert currentCard != null : "Prev card is null";
            updateCardFrontSide();
            updateTitle();
        }
    }

    /* 
     * Show the front side of the current card 
     * This method is called instead directly update the flashcard
     * so both single and double sided card will work.
     */
    private void updateCardFrontSide(){
        if(currentCard != null){
            if(setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED){
                /* Double sided card, show front */
                flashcardDisplay.updateView(currentCard, false);
            }
            else{
                /* Single sided, show both answer and questjion. */
                flashcardDisplay.updateView(currentCard, true);
            }
        }
    }

    private void createSearchOverlay(){
        if(searchInflated == false){
            LinearLayout root = (LinearLayout)findViewById(R.id.memo_screen_root);
            LayoutInflater.from(this).inflate(R.layout.search_overlay, root);
            ImageButton close = (ImageButton)findViewById(R.id.search_close_btn);
            close.setOnClickListener(closeSearchButtonListener);
            searchPrevButton = findViewById(R.id.search_previous_btn);
            searchPrevButton.setOnClickListener(searchButtonListener);
            searchNextButton = findViewById(R.id.search_next_btn);
            searchNextButton.setOnClickListener(searchButtonListener);

            EditText editEntry = (EditText)findViewById(R.id.search_entry);
            editEntry.requestFocus();
            searchInflated = true;

        }
        else{
            LinearLayout layout = (LinearLayout)findViewById(R.id.search_root);
            layout.setVisibility(View.VISIBLE);
        }


    }

    private void showCategoriesDialog() {
        CategoryEditorFragment df = new CategoryEditorFragment();
        df.setResultListener(categoryResultListener);
        Bundle b = new Bundle();
        b.putString(CategoryEditorFragment.EXTRA_DBPATH, dbPath);
        if (currentCategory == null) {
            b.putInt(CategoryEditorFragment.EXTRA_CATEGORY_ID, currentCard.getCategory().getId());
        } else {
            // If we use the category filer, we can just use the currentCategory
            // This will handle the new card situation.
            b.putInt(CategoryEditorFragment.EXTRA_CATEGORY_ID, currentCategory.getId());
        }
        df.setArguments(b);
        df.show(getSupportFragmentManager(), "CategoryEditDialog");
        getSupportFragmentManager().findFragmentByTag("CategoryEditDialog");
    }

    private void dismissSearchOverlay(){
        if(searchInflated == true){
            LinearLayout layout = (LinearLayout)findViewById(R.id.search_root);
            layout.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener prevButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            gotoPrev();
            if(questionTTS != null){
                questionTTS.stop();
            }
        }
    };

    private View.OnClickListener nextButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            gotoNext();
            if(answerTTS != null){
                answerTTS.stop();
            }
        }
    };

    private View.OnClickListener closeSearchButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            dismissSearchOverlay();
        }
    };

    private View.OnClickListener searchButtonListener = new View.OnClickListener(){
        public void onClick(View v){

            EditText editEntry = (EditText)findViewById(R.id.search_entry);
            String text = editEntry.getText().toString();
            // Search #123 for id 123
            if (text.startsWith("#")) {
                SearchCardTask task = new SearchCardTask();
                task.execute(SearchMethod.ID.toString(), text.substring(1));
                return;
            }

            // Search normal text
            if (!text.contains("*")) {
                text = "*" + text + "*";
            }
            // Convert to SQL wildcard
            text = text.replace("*", "%");
            text = text.replace("?", "_");
            SearchCardTask task = new SearchCardTask();
            if (v == searchNextButton) {
                task.execute(SearchMethod.TEXT_FORWARD.toString(), text);
            }

            if (v == searchPrevButton) {
                task.execute(SearchMethod.TEXT_BACKWARD.toString(), text);
            }
        }
    };

    private View.OnTouchListener viewTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event){
            return gestureDetector.onTouchEvent(event);
        }
    };

    private View.OnClickListener toggleCardSideListener = new View.OnClickListener(){
        public void onClick(View v){
            if(currentCard != null){
                /* Double sided card, the click will toggle question and answer */
                if(setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED){
                    if(flashcardDisplay.isAnswerShown()){
                        flashcardDisplay.updateView(currentCard, false);
                    } else{
                        flashcardDisplay.updateView(currentCard, true);
                    }
                }
            }
        }
    };


    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener(){
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override 
        public boolean onDown(MotionEvent e){
            /* Trick: Prevent the menu to popup twice */
            return true;
        }
        @Override 
        public void onLongPress(MotionEvent e){
            closeContextMenu();
            EditScreen.this.openContextMenu(flashcardDisplay.getView());
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    /* Swipe Right to Left event */
                    gotoNext();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    /* Swipe Left to Right event */
                    gotoPrev();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling gesture left/right event", e);
            }
            return false;
        }
    };

    private class InitTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private int currentCardId;
        private static final int NULL_ID = -1;

		@Override
        public void onPreExecute() {
            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

            setContentView(R.layout.memo_screen_layout);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                dbPath = extras.getString(EXTRA_DBPATH);
                activeCategoryId = extras.getInt(EXTRA_CATEGORY, NULL_ID);
                currentCardId = extras.getInt(EXTRA_CARD_ID, NULL_ID);
            }

            // Strip leading path!
            dbName = AMUtil.getFilenameFromPath(dbPath);
            progressDialog = new ProgressDialog(EditScreen.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();


        }

        @Override
        public Void doInBackground(Void... params) {
            try {
                dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(EditScreen.this, dbPath);
                
                cardDao = dbOpenHelper.getCardDao();
                learningDataDao = dbOpenHelper.getLearningDataDao();
                categoryDao = dbOpenHelper.getCategoryDao();
                settingDao = dbOpenHelper.getSettingDao();
                setting = settingDao.queryForId(1);
                option = new Option(EditScreen.this);
                
                // If category is set, it will override the card id.
                if (activeCategoryId != NULL_ID) {
                    currentCategory = categoryDao.queryForId(activeCategoryId);
                    currentCard = cardDao.queryFirstOrdinal(currentCategory);
                } else if (currentCardId != NULL_ID) {
                    currentCard = cardDao.queryForId(currentCardId);
                }

                // If None of category and card is is set, first ordinal is queried
                // Note curretnCategory should be null.
                if (currentCard == null) {
                    currentCard = cardDao.queryFirstOrdinal(currentCategory);
                }

                // This means empty deck.
                if (currentCard == null) {
                    return null;
                }

                categoryDao.refresh(currentCard.getCategory());
                learningDataDao.refresh(currentCard.getLearningData());

                totalCardCount = cardDao.countOf();

            } catch (SQLException e) {
                Log.e(TAG, "Error creating daos", e);
                throw new RuntimeException("Dao creation error");
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result){
            if(setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED){
                flashcardDisplay = new DoubleSidedCardDisplay(EditScreen.this, dbPath, setting, option);
            }
            else{
                flashcardDisplay = new SingleSidedCardDisplay(EditScreen.this, dbPath, setting, option);
            }
            controlButtons = new EditScreenButtons(EditScreen.this);

            /* databaseUtility is for global db operations */
            progressDialog.dismiss();
            initTTS();
            composeViews();
            //currentCard = .getItem(currentId);
            setViewListeners();
            if(currentCard != null){
                updateCardFrontSide();
                updateTitle();
            }
            /* Double sided card can't use the flip gesture*/
            if(setting.getCardStyle() != Setting.CardStyle.DOUBLE_SIDED){
                gestureDetector= new GestureDetector(EditScreen.this, gestureListener);
                flashcardDisplay.setScreenOnTouchListener(viewTouchListener);
            }
            registerForContextMenu(flashcardDisplay.getView());
        }
    }

    private class DeleteCardTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
		@Override
        public void onPreExecute() {
            progressDialog = new ProgressDialog(EditScreen.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @Override
        public Void doInBackground(Void... params) {
            try {
                Card delCard = currentCard;
                currentCard = cardDao.queryNextCard(currentCard, currentCategory);
                cardDao.delete(delCard);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        @Override
        public void onPostExecute(Void result){
            restartActivity();
        }
    }

    /*
     * params[2] = {Search Method, Search criteria}
     * Search Method should be in SearchMethod enum.
     */
    private class SearchCardTask extends AsyncTask<String, Void, Card> {

        private ProgressDialog progressDialog;

		@Override
        public void onPreExecute() {
            progressDialog = new ProgressDialog(EditScreen.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public Card doInBackground(String... params) {
            SearchMethod method = SearchMethod.valueOf(params[0]);
            assert method != null : "Pass null params to SearchCardTask";
            String criteria = params[1];
            assert criteria != null : "Pass null criteria to SearchCardTask";

            Card foundCard = null;
            try {
                if (method == SearchMethod.ID && NumberUtils.isDigits(criteria)) {
                    foundCard = cardDao.queryForId(Integer.valueOf(criteria));
                }

                if (method == SearchMethod.TEXT_FORWARD) {
                    foundCard = cardDao.searchNextCard(criteria, currentCard.getOrdinal());
                }

                if (method == SearchMethod.TEXT_BACKWARD) {
                    foundCard = cardDao.searchPrevCard(criteria, currentCard.getOrdinal());
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return foundCard;
        }

        @Override
        public void onPostExecute(Card result){
            progressDialog.dismiss();
            if (result == null) {
                return;
            }
            currentCard = result;
            updateCardFrontSide();
            updateTitle();
        }
    }

    private static enum SearchMethod {
        TEXT_FORWARD,
        TEXT_BACKWARD,
        ID
    }

    // When a category is selected in category fragment.
    private CategoryEditorResultListener categoryResultListener = 
        new CategoryEditorResultListener() {
            public void onReceiveCategory(Category c) {
                activeCategoryId = c.getId();
                restartActivity();
            }
        };
}
