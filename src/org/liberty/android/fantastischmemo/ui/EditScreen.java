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
package org.liberty.android.fantastischmemo.ui;

import java.sql.SQLException;

import java.util.Locale;
import java.util.Map;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AMUtil;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.DetailScreen;
import org.liberty.android.fantastischmemo.Item;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.ui.SettingsScreen;

import org.liberty.android.fantastischmemo.cardscreen.ListEditScreen;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;

import org.liberty.android.fantastischmemo.domain.Card;
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
import android.os.Environment;
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

public class EditScreen extends AMActivity {
    private AnyMemoTTS questionTTS = null;
    private AnyMemoTTS answerTTS = null;
    private boolean searchInflated = false;
    private final int DIALOG_LOADING_PROGRESS = 100;
    private final int ACTIVITY_FILTER = 10;
    private final int ACTIVITY_EDIT = 11;
    private final int ACTIVITY_CARD_TOOLBOX = 12;
    private final int ACTIVITY_DB_TOOLBOX = 13;
    private final int ACTIVITY_GOTO_PREV = 14;
    private final int ACTIVITY_SETTINGS = 15;
    private final int ACTIVITY_LIST = 16;
    private final int ACTIVITY_MERGE = 17;
    private final int ACTIVITY_DETAIL = 18;
    private final static String WEBSITE_HELP_EDIT = "http://anymemo.org/wiki/index.php?title=Editing_screen";

    public static String EXTRA_DBPATH = "dbpath";
    public static String EXTRA_CARD_ID = "id";
    public static String EXTRA_CATEGORY = "category";

    Card currentCard = null;
    Integer currentCardId = null;
    Integer savedCardId = null;
    String dbPath = "";
    String dbName = "";
    String activeCategory = "";
    SettingDao settingDao;
    CardDao cardDao;
    LearningDataDao learningDataDao;
    CategoryDao categoryDao;
    FlashcardDisplay flashcardDisplay;
    ControlButtons controlButtons;
    Setting setting;
    InitTask initTask;
    Option option;
    
    private GestureDetector gestureDetector;

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
        AnyMemoDBOpenHelperManager.releaseHelper(dbPath);
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
        System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_CANCELED){
            return;
        }
        /* Refresh the activity according to activities */
        switch(requestCode){
            case ACTIVITY_EDIT:
            {
                // TODO: Jump to edit card.
                restartActivity();
                break;
            }

            case ACTIVITY_FILTER:
            {
                // TODO: filter
                Bundle extras = data.getExtras();
                activeCategory = extras.getString("category");
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
                // TODO should use card id
                Bundle extras = data.getExtras();
                currentCard = extras.getParcelable("item");
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
                // TODO: Edit this
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
                    myIntent.putExtra("dbpath", this.dbPath);
                    myIntent.putExtra("itemid", currentCard.getId());
                    startActivityForResult(myIntent, ACTIVITY_DETAIL);
                }
                return true;
            }
            case R.id.editmenu_list_id:
            {
                // List edit mode
                Intent myIntent = new Intent(this, SettingsScreen.class);
                myIntent.setClass(this, ListEditScreen.class);
                myIntent.putExtra("dbpath", dbPath);
                if(currentCard != null){
                    myIntent.putExtra("openid", currentCard.getId());
                }
                startActivityForResult(myIntent, ACTIVITY_LIST);
                return true;
            }

            case R.id.menu_edit_filter:
            {
                // TODO: Need rework for category
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

                if(currentCard != null){
                    savedCardId = currentCard.getId();
                }
                return true;
            }
            case R.id.menu_context_paste:
            {
                // TODO: Need paste
                //if(savedItem != null){
                //    itemManager.insert(savedItem, currentItem.getId());
                //    /* Set the Id to the current one */
                //    currentItem = new Item.Builder(savedItem)
                //        .setId(currentItem.getId() + 1)
                //        .build();
                //    updateCardFrontSide();
                //    updateTitle();
                //}

                return true;
            }
            case R.id.menu_context_swap_current:
            {
                // TODO: swap
                //if(currentItem != null){
                //    databaseUtility.swapSingelItem(currentItem);
                //}
                return true;
            }

            case R.id.menu_context_reset_current:
            {
                // TODO: Reset
                //if(currentItem != null){
                //    databaseUtility.resetCurrentLearningData(currentItem);
                //}
                return true;
            }

            case R.id.menu_context_wipe:
            {
                // TODO: wipe 
                //databaseUtility.wipeLearningData();
                return true;
            }

            case R.id.menu_context_swap:
            {
                // TODO: swap
                //databaseUtility.swapAllQA();
                return true;
            }

            case R.id.menu_context_remove_dup:
            {
                // TODO: dedup
                //databaseUtility.removeDuplicates();
                return true;
            }

            case R.id.menu_context_merge_db:
            {
                // TODO: Merge
                Intent myIntent = new Intent(this, DatabaseMerger.class);
                myIntent.putExtra("dbpath", dbPath);
                startActivityForResult(myIntent, ACTIVITY_MERGE);
                return true;
            }

            case R.id.menu_context_shuffle:
            {
                // TODO: Shuffle
                //databaseUtility.shuffleDatabase();
                return true;
            }

            default:
            {
                return super.onContextItemSelected(menuitem);
            }
        }
    }

    private void initTTS(){
        String defaultLocation =
            Environment.getExternalStorageDirectory().getAbsolutePath()
            + getString(R.string.default_audio_dir);
        // TODO: This couldn't be null but be wary
        String qa = setting.getQuestionAudio();
        String aa = setting.getAnswerAudio();

        // TODO: Pay attention to null pointer
        if(!setting.getQuestionAudioLocation().equals("")){
            questionTTS = new AudioFileTTS(defaultLocation, dbName);
        }
        else if(qa != null && !qa.equals("")){
            questionTTS = new AnyMemoTTSPlatform(this, new Locale(qa));
        }
        else{
            questionTTS = null;
        }

        // TODO: Pay attention to null pointer
        if(!setting.getAnswerAudioLocation().equals("")){
            answerTTS = new AudioFileTTS(defaultLocation, dbName);
        }
        else if(qa != null && !qa.equals("")){
            answerTTS = new AnyMemoTTSPlatform(this, new Locale(aa));
        }
        else{
            answerTTS = null;
        }
    }

    @Override
    public void restartActivity(){
        Intent myIntent = new Intent(this, EditScreen.class);
        assert currentCard != null : "Null card is used when restarting activity";
        myIntent.putExtra(EXTRA_CARD_ID, currentCard.getId());
        myIntent.putExtra(EXTRA_DBPATH, dbPath);
        myIntent.putExtra(EXTRA_CATEGORY, activeCategory);
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
            myIntent.putExtra(CardEditor.EXTRA_CARD_ID, currentCardId);
            myIntent.putExtra(CardEditor.EXTRA_IS_EDIT_NEW, true);
            //startActivityForResult(myIntent, ACTIVITY_EDIT);
            startActivityForResult(myIntent, ACTIVITY_EDIT);
        }
    };

    private View.OnClickListener editButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            Intent myIntent = new Intent(EditScreen.this, CardEditor.class);
            myIntent.putExtra(CardEditor.EXTRA_DBPATH, dbPath);
            myIntent.putExtra(CardEditor.EXTRA_CARD_ID, currentCardId);
            myIntent.putExtra(CardEditor.EXTRA_IS_EDIT_NEW, false);
            startActivityForResult(myIntent, ACTIVITY_EDIT);
        }
    };

    private void updateTitle(){
        // TODO: How to update
        /*
        if(currentItem != null){
            int total = itemManager.getStatInfo()[0];
            String titleString = getString(R.string.stat_total) + total + " " + getString(R.string.memo_current_id) + " " + currentItem.getId();
            if(currentItem != null && currentItem.getCategory() != null){
                titleString += "  " + currentItem.getCategory();
            }
            setTitle(titleString);
        }
        */
    }
    
    private void gotoNext(){
        currentCard = cardDao.queryNextCard(currentCard);
        assert currentCard != null : "Next card is null";
        updateCardFrontSide();
        updateTitle();
    }

    private void deleteCurrent(){
        if(currentCard != null){
            new AlertDialog.Builder(EditScreen.this)
                .setTitle(getString(R.string.detail_delete))
                .setMessage(getString(R.string.delete_warning))
                .setPositiveButton(getString(R.string.yes_text),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            //TODO: How to delete
                            //currentItem = itemManager.deleteItem(currentItem);
                            restartActivity();
                        }
                    })
                .setNegativeButton(getString(R.string.no_text), null)
                .create()
                .show();
        }
    }

    private void gotoPrev(){
        // TODO: How to go to prev card
        //currentItem = itemManager.getPreviousItem(currentItem);
        //updateCardFrontSide();
        currentCard = cardDao.queryPrevCard(currentCard);
        assert currentCard != null : "Prev card is null";
        updateCardFrontSide();
        updateTitle();
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
            ImageButton prev = (ImageButton)findViewById(R.id.search_previous_btn);
            prev.setOnClickListener(searchPrevButtonListener);
            ImageButton next = (ImageButton)findViewById(R.id.search_next_btn);
            next.setOnClickListener(searchNextButtonListener);

            EditText editEntry = (EditText)findViewById(R.id.search_entry);
            editEntry.requestFocus();
            searchInflated = true;

        }
        else{
            LinearLayout layout = (LinearLayout)findViewById(R.id.search_root);
            layout.setVisibility(View.VISIBLE);
        }


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

    private View.OnClickListener searchNextButtonListener = new View.OnClickListener(){
        public void onClick(View v){

            // TODO: How to search next?
            EditText editEntry = (EditText)findViewById(R.id.search_entry);
            String text = editEntry.getText().toString();
            //Item item = itemManager.search(text, true, currentItem);
            //if(item != null){
            //    currentItem = item;
            //    updateCardFrontSide();
            //    updateTitle();
            //}
        }
    };

    private View.OnClickListener searchPrevButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            EditText editEntry = (EditText)findViewById(R.id.search_entry);
            String text = editEntry.getText().toString();
            // TODO: How to search
            //Item item = itemManager.search(text, false, currentItem);
            //if(item != null){
            //    currentItem = item;
            //    updateCardFrontSide();
            //    updateTitle();
            //}
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
                    }
                    else{
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

		@Override
        public void onPreExecute() {
            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

            setContentView(R.layout.memo_screen_layout);

            Bundle extras = getIntent().getExtras();
            int currentId = 1;
            if (extras != null) {
                dbPath = extras.getString("dbpath");
                activeCategory = extras.getString("category");
                currentCardId = extras.getInt("id", 1);
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
                AnyMemoDBOpenHelper helper =
                    AnyMemoDBOpenHelperManager.getHelper(EditScreen.this, dbPath);
                
                cardDao = helper.getCardDao();
                learningDataDao = helper.getLearningDataDao();
                settingDao = helper.getSettingDao();
                setting = settingDao.queryForId(1);
                option = new Option(EditScreen.this);
                /* Run the learnQueue init in a separate thread */
                currentCard = cardDao.queryForId(currentCardId);
                // TODO: Query for first ordinal
                if (currentCard == null) {
                    currentCard = cardDao.queryFirstOrdinal();
                }

            } catch (SQLException e) {
                Log.e(TAG, "Error creating daos", e);
                throw new RuntimeException("Dao creation error");
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result){
            // It means empty set
            if (currentCard == null) {
                // TODO: should create a new card
            } else {
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
                if(currentCard == null){
                    // TODO: how to get current card?
                }
                updateCardFrontSide();
                updateTitle();
                setViewListeners();
                /* Double sided card can't use the flip gesture*/
                if(setting.getCardStyle() != Setting.CardStyle.DOUBLE_SIDED){
                    gestureDetector= new GestureDetector(EditScreen.this, gestureListener);
                    flashcardDisplay.setScreenOnTouchListener(viewTouchListener);
                }
                registerForContextMenu(flashcardDisplay.getView());
            }
        }
    }
}
