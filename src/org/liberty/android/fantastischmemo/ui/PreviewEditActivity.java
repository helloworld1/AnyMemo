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
import java.util.HashMap;

import org.apache.mycommons.lang3.math.NumberUtils;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.ui.CategoryEditorFragment.CategoryEditorResultListener;
import org.liberty.android.fantastischmemo.utils.AMGUIUtility;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;
import org.liberty.android.fantastischmemo.utils.ShareUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class PreviewEditActivity extends QACardActivity {
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
    
    private static final String TAG = "PreviewEditActivity";
    private static final int MAGIC_FRAME_LAYOUT_ID = 675198655; // A magic id that we used to set frame layout id. 

    private Category currentCategory = null;
    private Integer savedCardId = null;
    private String dbPath = "";
    private int activeCategoryId = -1;
    private SettingDao settingDao;
    private CardDao cardDao;
    private LearningDataDao learningDataDao;
    private CategoryDao categoryDao;
    LinearLayout buttonsLayout;

    Button newButton;
    Button editButton;
    Button prevButton;
    Button nextButton;
    //private ControlButtons controlButtons;
    private View searchNextButton;
    private View searchPrevButton;

    private Setting setting;

    private ShareUtil shareUtil;

    private AMPrefUtil amPrefUtil;
    
    private GestureDetector gestureDetector;

    // The first card to read and display.
    private int startCardId = 1;

      
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dbPath = extras.getString(EXTRA_DBPATH);
            activeCategoryId = extras.getInt(EXTRA_CATEGORY, -1);
            startCardId = extras.getInt(EXTRA_CARD_ID, -1);
            
        }

        /* 
         * Currently always set the result to OK
         * to assume there are always some changes.
         * This may be changed in the future to reflect the
         * real changes
         */
        setResult(Activity.RESULT_OK);

    }

    @Override
    public void onInit() throws Exception {
        Card currentCard = null;
        cardDao = getDbOpenHelper().getCardDao();
        learningDataDao = getDbOpenHelper().getLearningDataDao();
        categoryDao = getDbOpenHelper().getCategoryDao();
        settingDao = getDbOpenHelper().getSettingDao();
        setting = settingDao.queryForId(1);

        shareUtil = new ShareUtil(this);
        amPrefUtil = new AMPrefUtil(this);

        // If category is set, it will override the card id.
        if (activeCategoryId != -1) {
            currentCategory = categoryDao.queryForId(activeCategoryId);
            currentCard = cardDao.queryFirstOrdinal(currentCategory);
        } else if (startCardId != -1) {
            currentCard = cardDao.queryForId(startCardId);
        }

        // If None of category and card is is set, first ordinal is queried
        // Note curretnCategory should be null.
        if (currentCard == null) {
            currentCard = cardDao.queryFirstOrdinal(currentCategory);
        }

        // If all attemp failed. we will return null and let user to create a new card.
        if (currentCard == null) {
            return;
        }

        categoryDao.refresh(currentCard.getCategory());
        learningDataDao.refresh(currentCard.getLearningData());

        totalCardCount = cardDao.countOf();
        setCurrentCard(currentCard);

    }

    @Override
    public void onPostInit() {
        composeViews();
        //currentCard = .getItem(currentId);
        setViewListeners();
        if(getCurrentCard() != null){
            updateCardFrontSide();
            updateTitle();
        }
        /* Double sided card can't use the flip gesture*/
        if(setting.getCardStyle() != Setting.CardStyle.DOUBLE_SIDED){
            gestureDetector= new GestureDetector(PreviewEditActivity.this, gestureListener);
        }
    }

    // Save the card id in onPause
    @Override
    public void onPause() {
        super.onPause();
        if (getCurrentCard() != null) {
            amPrefUtil.setSavedId(AMPrefKeys.PREVIEW_EDIT_START_ID_PREFIX, dbPath, getCurrentCard().getId());
        }

    }

    @Override
    public void onDestroy() {
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
                        setCurrentCard(card);
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
        inflater.inflate(R.menu.preview_edit_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuspeakquestion:
            {
                if (getCurrentCard() != null) {
                    return speakQuestion();
                }
                return true;
            }

            case R.id.menuspeakanswer:
            {
                if (getCurrentCard() != null) {
                    return speakAnswer();
                }
                return true;
            }

            case R.id.editmenu_settings_id:
            {
                Intent myIntent = new Intent(this, SettingsScreen.class);
                myIntent.putExtra(SettingsScreen.EXTRA_DBPATH, dbPath);
                startActivityForResult(myIntent, ACTIVITY_SETTINGS);
            }
                return true;

            case R.id.editmenu_delete_id:
            {
                if (getCurrentCard() != null) {
                    deleteCurrent();
                }
                return true;
            }


            case R.id.editmenu_detail_id:
            {
                if (getCurrentCard() != null) {
                    Intent myIntent = new Intent(this, DetailScreen.class);
                    myIntent.putExtra(DetailScreen.EXTRA_DBPATH, this.dbPath);
                    myIntent.putExtra(DetailScreen.EXTRA_CARD_ID, getCurrentCard().getId());
                    startActivityForResult(myIntent, ACTIVITY_DETAIL);
                }
                return true;
            }
            case R.id.editmenu_list_id:
            {
                // List edit mode
                Intent myIntent = new Intent(this, SettingsScreen.class);
                myIntent.setClass(this, ListEditScreen.class);
                myIntent.putExtra(ListEditScreen.EXTRA_DBPATH, dbPath);
                if(getCurrentCard() != null){
                    myIntent.putExtra("openid", getCurrentCard().getId());
                }
                startActivityForResult(myIntent, ACTIVITY_LIST);
                return true;
            }

            case R.id.menu_edit_categories:
            {
                if (getCurrentCard() != null) {
                    showCategoriesDialog();
                }
                return true;
            }
            case R.id.editmenu_search_id:
            {
                if (getCurrentCard() != null) {
                    createSearchOverlay();
                }
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

            case R.id.menu_auto_speak:
            {
                if (getCurrentCard() != null) {
                	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                	LinearLayout root = (LinearLayout)findViewById(R.id.root);
                	FrameLayout fl = new FrameLayout(this);
                	
                	AutoSpeakFragment f = new AutoSpeakFragment();
                	
                	fl.setId(MAGIC_FRAME_LAYOUT_ID);
                	f.setAutoSpeakEventHander(f.getAutoSpeakEventHandler());
                	root.addView(fl);
                //	Log.e(TAG, String.format("fl id is %d", fl.getId()));
                	ft.add(fl.getId(), f);
                	
                	ft.commit();
                }
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

                if (getCurrentCard()!= null){
                    Toast.makeText(this, R.string.copy_text, Toast.LENGTH_LONG).show();
                    savedCardId = getCurrentCard().getId();
                }
                return true;
            }
            case R.id.menu_context_paste:
            {
                if (savedCardId != null && getCurrentCard() != null) {
                    try {
                        Card savedCard = cardDao.queryForId(savedCardId);
                        LearningData ld = new LearningData();
                        learningDataDao.create(ld);
                        savedCard.setLearningData(ld);
                        savedCard.setOrdinal(getCurrentCard().getOrdinal());
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
                cardDao.swapQA(getCurrentCard());
                restartActivity();
                return true;
            }

            case R.id.menu_context_reset_current:
            {
                learningDataDao.resetLearningData(getCurrentCard().getLearningData());
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
                            AMGUIUtility.doProgressTask(PreviewEditActivity.this, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
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
                        AMGUIUtility.doProgressTask(PreviewEditActivity.this, R.string.loading_please_wait, R.string.loading_save, new AMGUIUtility.ProgressTask(){
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

            case R.id.menu_gestures:
            {
                showGesturesDialog();
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

            case R.id.menu_share:
            {
                shareUtil.shareCard(getCurrentCard());
                return true;
            }
        }

        return false;
    }
    
    // Handle click event for double sided card.
    protected void onClickAnswerView() {
        if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED) {
            displayCard(false);
            // Also the buttons should match the color.
            buttonsLayout.setBackgroundColor(setting.getQuestionBackgroundColor());
        }
    }

    protected void onClickAnswerText() {
        onClickAnswerView();
    }

    protected void onClickQuestionView() {
        if (setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED) {
            displayCard(true);
            buttonsLayout.setBackgroundColor(setting.getAnswerBackgroundColor());
        }
    }

    protected void onClickQuestionText() {
        onClickQuestionView();
    }

    @Override
    protected void onGestureDetected(GestureName gestureName) {
        switch (gestureName) {
            case O_SHAPE:
                break;

            case S_SHAPE:
                break;
            case LEFT_SWIPE:
                gotoNext();
                break;

            case RIGHT_SWIPE:
                gotoPrev();
                break;
            default:
                break;

        }
    }


    @Override
    public void restartActivity(){
        Intent myIntent = new Intent(this, PreviewEditActivity.class);
        assert dbPath != null : "Use null dbPath to restartAcitivity";
        myIntent.putExtra(EXTRA_DBPATH, dbPath);
        if (getCurrentCard() != null) {
            myIntent.putExtra(EXTRA_CARD_ID, getCurrentCard().getId());
        }
        myIntent.putExtra(EXTRA_CATEGORY, activeCategoryId);
        finish();
        startActivity(myIntent);
    }


    private void composeViews(){
        LinearLayout rootView= (LinearLayout) findViewById(R.id.root);

        LayoutInflater factory = LayoutInflater.from(this);
        LinearLayout controlButtonsView = (LinearLayout) factory.inflate(R.layout.preview_edit_activity_buttons, null);

        //LinearLayout controlButtonsView = (LinearLayout)controlButtons.getView();
        /* This li is make the background of buttons the same as answer */
        buttonsLayout = new LinearLayout(this);
        buttonsLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        /* 
         * -1: Match parent -2: Wrap content
         * This is necessary or the view will not be 
         * stetched
         */
        buttonsLayout.addView(controlButtonsView, -1, -2);
        
        rootView.addView(buttonsLayout, -1, -2);
        //rootView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));

        newButton = (Button) findViewById(R.id.new_button);
        editButton = (Button) findViewById(R.id.edit_button);
        prevButton = (Button) findViewById(R.id.prev_button);
        nextButton = (Button) findViewById(R.id.next_button);
        
    }

    void setViewListeners(){
        ///* Set button listeners */
        newButton.setOnClickListener(newButtonListener);
        editButton.setOnClickListener(editButtonListener);
        prevButton.setOnClickListener(prevButtonListener);
        nextButton.setOnClickListener(nextButtonListener);
        /* For double sided card, the view can be toggled */
        if(setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED){
            // TODO: Set listeners

        }


    }

    private View.OnClickListener newButtonListener = new View.OnClickListener(){
        public void onClick(View v){
                Intent myIntent = new Intent(PreviewEditActivity.this, CardEditor.class);
                myIntent.putExtra(CardEditor.EXTRA_DBPATH, dbPath);
                if (getCurrentCard() != null) {
                    myIntent.putExtra(CardEditor.EXTRA_CARD_ID, getCurrentCard().getId());
                }
                myIntent.putExtra(CardEditor.EXTRA_IS_EDIT_NEW, true);
                startActivityForResult(myIntent, ACTIVITY_EDIT);
        }
    };

    private View.OnClickListener editButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            if (getCurrentCard() != null) {
                Intent myIntent = new Intent(PreviewEditActivity.this, CardEditor.class);
                myIntent.putExtra(CardEditor.EXTRA_DBPATH, dbPath);
                myIntent.putExtra(CardEditor.EXTRA_CARD_ID, getCurrentCard().getId());
                myIntent.putExtra(CardEditor.EXTRA_IS_EDIT_NEW, false);
                startActivityForResult(myIntent, ACTIVITY_EDIT);
            }
        }
    };

    private void updateTitle(){
        if (getCurrentCard() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.total_text) + ": " + totalCardCount + " ");
            sb.append(getString(R.string.id_text) + ": " + getCurrentCard().getId() + " ");
            sb.append(getString(R.string.ordinal_text_short) + ": " + getCurrentCard().getOrdinal() + " ");
            sb.append(getCurrentCard().getCategory().getName());
            setSmallTitle(sb.toString());
        }
    }
    
    public void gotoNext(){
        if (getCurrentCard() != null) {
            setCurrentCard(cardDao.queryNextCard(getCurrentCard(), currentCategory));
            try {
                categoryDao.refresh(getCurrentCard().getCategory());
                learningDataDao.refresh(getCurrentCard().getLearningData());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            assert getCurrentCard() != null : "Next card is null";
            setAnimation(R.anim.slide_left_in, R.anim.slide_left_out);
            updateCardFrontSide();
            updateTitle();
        }
    }

    private void deleteCurrent(){
        if(getCurrentCard() != null){
            new AlertDialog.Builder(this)
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

    public void gotoPrev(){
        if (getCurrentCard() != null) {
            setCurrentCard(cardDao.queryPrevCard(getCurrentCard(), currentCategory));
            try {
                categoryDao.refresh(getCurrentCard().getCategory());
                learningDataDao.refresh(getCurrentCard().getLearningData());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            assert getCurrentCard() != null : "Prev card is null";
            setAnimation(R.anim.slide_right_in, R.anim.slide_right_out);
            updateCardFrontSide();
            // Set it back
            setAnimation(R.anim.slide_left_in, R.anim.slide_left_out);
            updateTitle();
        }
    }

    /* 
     * Show the front side of the current card 
     * This method is called instead directly update the flashcard
     * so both single and double sided card will work.
     */
    private void updateCardFrontSide(){
        if(getCurrentCard() != null){
            if(setting.getCardStyle() == Setting.CardStyle.DOUBLE_SIDED){
                /* Double sided card, show front */
                displayCard(false);
                buttonsLayout.setBackgroundColor(setting.getQuestionBackgroundColor());
            } else {
                /* Single sided, show both answer and questjion. */
                displayCard(true);
                buttonsLayout.setBackgroundColor(setting.getAnswerBackgroundColor());
            }
        }
    }

    private void createSearchOverlay(){
        // Disable the function if there are no cards.
        if (getCurrentCard() == null) {
            return;
        }
        if(searchInflated == false){
            LinearLayout root = (LinearLayout)findViewById(R.id.root);
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
            b.putInt(CategoryEditorFragment.EXTRA_CATEGORY_ID, getCurrentCard().getCategory().getId());
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
            stopSpeak();
        }
    };

    private View.OnClickListener nextButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            gotoNext();
            stopSpeak();
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
            // TODO: Long press what???
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

    private class DeleteCardTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        @Override
        public void onPreExecute() {
            progressDialog = new ProgressDialog(PreviewEditActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(getString(R.string.loading_please_wait));
            progressDialog.setMessage(getString(R.string.loading_database));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        @Override
        public Void doInBackground(Void... params) {
            try {
                Card delCard = getCurrentCard();
                setCurrentCard(cardDao.queryNextCard(getCurrentCard(), currentCategory));
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
            progressDialog = new ProgressDialog(PreviewEditActivity.this);
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
                    foundCard = cardDao.searchNextCard(criteria, getCurrentCard().getOrdinal());
                }

                if (method == SearchMethod.TEXT_BACKWARD) {
                    foundCard = cardDao.searchPrevCard(criteria, getCurrentCard().getOrdinal());
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
            setCurrentCard(result);
            updateCardFrontSide();
            updateTitle();
        }
    }

    private void showGesturesDialog() {
        final HashMap<String, String> gestureNameDescriptionMap
            = new HashMap<String, String>();
        gestureNameDescriptionMap.put(GestureName.LEFT_SWIPE.getName(), getString(R.string.add_screen_next));
        gestureNameDescriptionMap.put(GestureName.RIGHT_SWIPE.getName(), getString(R.string.previous_text_short));


        GestureSelectionDialogFragment df = new GestureSelectionDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(GestureSelectionDialogFragment.EXTRA_GESTURE_NAME_DESCRIPTION_MAP, gestureNameDescriptionMap);
        df.setArguments(args);
        df.show(getSupportFragmentManager(), "GestureSelectionDialog");
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
