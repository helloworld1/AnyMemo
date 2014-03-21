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

import java.util.HashMap;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.aspect.LogInvocation;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.queue.LearnQueueManager;
import org.liberty.android.fantastischmemo.queue.QueueManager;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.ui.CategoryEditorFragment.CategoryEditorResultListener;
import org.liberty.android.fantastischmemo.ui.loader.DBLoader;
import org.liberty.android.fantastischmemo.utils.DictionaryUtil;
import org.liberty.android.fantastischmemo.utils.ShareUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.apis.graphics.FingerPaint;
import com.google.common.base.Strings;

/**
 * The StudyActivity is used for the classic way of learning cards.
 */
public class StudyActivity extends QACardActivity {
    public static String EXTRA_DBPATH = "dbpath";
    public static String EXTRA_CATEGORY_ID = "category_id";
    public static String EXTRA_START_CARD_ID = "start_card_id";
    private static final int LEARN_QUEUE_MANAGER_LOADER_ID = 10;

    private final int ACTIVITY_FILTER = 10;
    private final int ACTIVITY_EDIT = 11;
    private final int ACTIVITY_GOTO_PREV = 14;
    private final int ACTIVITY_SETTINGS = 15;
    private final int ACTIVITY_DETAIL = 16;

    private final static String WEBSITE_HELP_MEMO="http://anymemo.org/wiki/index.php?title=Learning_screen";

    /* State objects */
    private Card prevCard = null;
    private String dbPath = "";
    private int filterCategoryId = -1;
    private Category filterCategory;
    private int startCardId = -1;

    private QueueManager queueManager;

    /* Schedulers */
    private Scheduler scheduler = null;

    /* current states */
    private long schedluledCardCount = 0;
    private long newCardCount = 0;

    boolean initialized = false;

    private DictionaryUtil dictionaryUtil;

    private ShareUtil shareUtil;

    private GradeButtonsFragment gradeButtonsFragment;

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Inject
    public void setDictionaryUtil(DictionaryUtil dictionaryUtil) {
        this.dictionaryUtil = dictionaryUtil;
    }

    @Inject
    public void setShareUtil(ShareUtil shareUtil) {
        this.shareUtil = shareUtil;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dbPath = extras.getString(EXTRA_DBPATH);
            filterCategoryId = extras.getInt(EXTRA_CATEGORY_ID, -1);
            startCardId = extras.getInt(EXTRA_START_CARD_ID, -1);
        }

        if (savedInstanceState != null) {
            startCardId = savedInstanceState.getInt(EXTRA_START_CARD_ID, -1);
        }

        getMultipleLoaderManager().registerLoaderCallbacks(LEARN_QUEUE_MANAGER_LOADER_ID, new LearnQueueManagerLoaderCallbacks(), false);

        startInit();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getCurrentCard() != null) {
            outState.putInt(EXTRA_START_CARD_ID, getCurrentCard().getId());
        }
    }

    @Override
    public int getContentView() {
        return R.layout.qa_card_layout_study;
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
                gotoHelp();
                return true;
            }
            case R.id.menuspeakquestion:
            {
                return speakQuestion();
            }

            case R.id.menuspeakanswer:
            {
                return speakAnswer();
            }

            case R.id.menusettings:
            {
                gotoSettings();
                return true;
            }

            case R.id.menudetail:
            {
                gotoDetail();
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

            case R.id.menu_context_edit:
            {
                showEditDialog();
                return true;
            }
            case R.id.menu_context_delete:
            {
                showDeleteDialog();
                return true;

            }
            case R.id.menu_mark_as_learned_forever:
            {
                showMarkAsLearnedForeverDialog();
                return true;
            }
            case R.id.menu_context_gotoprev:
            {
                gotoPreviewEdit();
                return true;
            }

            case R.id.menu_context_lookup:
            {
                if(getCurrentCard() == null){
                    return false;
                }
                // Look up words in both question and answer
                lookupDictionary();

                return true;

            }

            case R.id.menu_gestures:
            {
                showGesturesDialog();
                return true;
            }

            case R.id.menu_context_paint:
            {
                gotoPaint();
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

        if (resultCode == Activity.RESULT_CANCELED) {
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
    public void onDestroy() {
        super.onDestroy();
        if (queueManager != null) {
            queueManager.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void restartActivity(){
        finish();
        Intent myIntent = new Intent(StudyActivity.this, StudyActivity.class);
        myIntent.putExtra(EXTRA_DBPATH, dbPath);
        myIntent.putExtra(EXTRA_CATEGORY_ID, filterCategoryId);

        if (getCurrentCard() != null ) {
            myIntent.putExtra(EXTRA_START_CARD_ID, getCurrentCard().getId());
        }

        startActivity(myIntent);
    }

    @Override
    public void onPostInit() {
        super.onPostInit();
        if (filterCategoryId != -1) {
            filterCategory = getDbOpenHelper().getCategoryDao().queryForId(filterCategoryId);
        }

        /* Run the learnQueue init in a separate thread */
        if (startCardId != -1) {
            Card card = queueManager.dequeuePosition(startCardId);
            queueManager.remove(card);
            setCurrentCard(card);
        } else {
            Card card = queueManager.dequeue();
            queueManager.remove(card);
            setCurrentCard(card);
        }
        refreshStatInfo();
        // If the db does not contain any cards. Show no item dialog.
        if (getCurrentCard() == null) {
            showNoItemDialog();
            return;
        }
        setupGradeButtons();
        displayCard(false);
        initialized = true;
        setTitle(getDbName());
    }

    @Override
    public void onPostDisplayCard() {
        // When displaying new card, we should stop the TTS reading.
        getCardTTSUtil().stopSpeak();
        if (isAnswerShown()) {
            gradeButtonsFragment.setVisibility(View.VISIBLE);
        } else {
            // The grade button should be gone for double sided cards.
            if (getSetting().getCardStyle() ==  Setting.CardStyle.DOUBLE_SIDED) {
                gradeButtonsFragment.setVisibility(View.GONE);
            } else {
                gradeButtonsFragment.setVisibility(View.INVISIBLE);
            }
        }

        // Auto speak after displaying a card.
        if (getOption().getSpeakingType() == Option.SpeakingType.AUTO
            || getOption().getSpeakingType() ==Option.SpeakingType.AUTOTAP) {
            autoSpeak();
        }
        setSmallTitle(getActivityTitleString());
    }

    @Override
    protected void onGestureDetected(GestureName gestureName) {
        switch (gestureName) {
            case O_SHAPE:
                lookupDictionary();
                break;

            case S_SHAPE:
                gotoPaint();
                break;
            default:
                break;

        }
    }

    @Override
    protected boolean onClickQuestionText() {
        if ((getOption().getSpeakingType() == Option.SpeakingType.AUTOTAP
                || getOption().getSpeakingType() == Option.SpeakingType.TAP)) {
            speakQuestion();
        } else {
            onClickQuestionView();
        }
        return true;
    }

    @Override
    protected boolean onClickAnswerText() {
        if (!isAnswerShown()) {
            onClickAnswerView();
        } else {
            if ((getOption().getSpeakingType() == Option.SpeakingType.AUTOTAP
                        || getOption().getSpeakingType() == Option.SpeakingType.TAP)) {
                speakAnswer();
            } else {
                onClickAnswerView();
            }
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
        } else if (getSetting().getCardStyle() == Setting.CardStyle.DOUBLE_SIDED && isAnswerShown()) {
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

    private void showNoItemDialog(){
        new AlertDialog.Builder(this)
            .setTitle(this.getString(R.string.memo_no_item_title))
            .setMessage(this.getString(R.string.memo_no_item_message))
            .setNeutralButton(getString(R.string.back_menu_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    /* Finish the current activity and go back to the last activity.
                     * It should be the open screen. */
                        onBackPressed();
                    }
                })
            .setOnCancelListener(new DialogInterface.OnCancelListener(){
                public void onCancel(DialogInterface dialog){
                        onBackPressed();
                    }
                })
            .create()
            .show();
    }

    private class LearnQueueManagerLoaderCallbacks implements
            LoaderManager.LoaderCallbacks<QueueManager> {
        @Override
        public Loader<QueueManager> onCreateLoader(int arg0, Bundle arg1) {
             Loader<QueueManager> loader = new LearnQueueManagerLoader(getApplicationContext(), dbPath, filterCategoryId);
             loader.forceLoad();
             return loader;
        }

        @Override
        public void onLoadFinished(Loader<QueueManager> loader , QueueManager queueManager) {
            StudyActivity.this.queueManager = queueManager;
            getMultipleLoaderManager().checkAllLoadersCompleted();
        }
        @Override
        public void onLoaderReset(Loader<QueueManager> arg0) {
            // Do nothing now
        }
    }

    private static class LearnQueueManagerLoader extends
            DBLoader<QueueManager> {

        private Option option;

        private Scheduler scheduler;

        private final int filterCategoryId;


        public LearnQueueManagerLoader(Context context, String dbPath, int filterCategoryId) {
            super(context, dbPath);
            this.filterCategoryId = filterCategoryId;
        }

        @Inject
        public void setOption(Option option) {
            this.option = option;
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
            int queueSize = option.getQueueSize();
            LearnQueueManager.Builder builder = new LearnQueueManager.Builder(getContext(), dbPath)
                .setScheduler(scheduler)
                .setLearnQueueSize(queueSize)
                .setCacheSize(50)
                .setFilterCategory(filterCategory);
            if (option.getShuffleType() == Option.ShuffleType.LOCAL) {
                builder.setShuffle(true);
            } else {
                builder.setShuffle(false);
            }
            return builder.build();
        }

    }

    private void autoSpeak() {
        if (getCurrentCard() != null) {
            if(!isAnswerShown()){
                // Make sure the TTS is stop, or it will speak nothing.
                speakQuestion();
            } else {
                // Make sure the TTS is stop
                speakAnswer();
            }
        }
    }

    private void refreshStatInfo() {
       newCardCount = getDbOpenHelper().getCardDao().getNewCardCount(filterCategory);
       schedluledCardCount = getDbOpenHelper().getCardDao().getScheduledCardCount(filterCategory);
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



    @LogInvocation
    private void setupGradeButtons() {
        gradeButtonsFragment = new GradeButtonsFragment();
        Bundle args = new Bundle();
        args.putString(GradeButtonsFragment.EXTRA_DBPATH, dbPath);
        gradeButtonsFragment.setArguments(args);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.buttons_root, gradeButtonsFragment);
        ft.commit();

        gradeButtonsFragment.setOnCardChangedListener(onCardChangedListener);
    }

    /*
     * When the user select the undo from the menu
     * this is what to do
     */
    private void undoCard(){
        if (prevCard != null) {

            // This is a very hacky solution
            // It will first release the queue manager so we can manually manipulate the card
            queueManager.remove(prevCard);
            queueManager.release();
            queueManager = null;

            // Then copy the correct learning data.
            LearningData prevCardLearningDataToUpdate = getDbOpenHelper().getLearningDataDao().queryForId(prevCard.getLearningData().getId());
            prevCardLearningDataToUpdate.cloneFromLearningData(prevCard.getLearningData());
            getDbOpenHelper().getLearningDataDao().update(prevCardLearningDataToUpdate);

            setCurrentCard(prevCard);
            restartActivity();
        } else {
            new AlertDialog.Builder(this)
                .setTitle(getString(R.string.undo_fail_text))
                .setMessage(getString(R.string.undo_fail_message))
                .setNeutralButton(R.string.ok_text, null)
                .create()
                .show();
        }
    }

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

    private GradeButtonsFragment.OnCardChangedListener onCardChangedListener =
        new GradeButtonsFragment.OnCardChangedListener() {
            public void onCardChanged(Card prevCard, Card updatedCard) {
                StudyActivity.this.prevCard = prevCard;
                gradeButtonsFragment.setVisibility(View.INVISIBLE);

                // Set the stat info in the title
                if (scheduler.isCardNew(prevCard.getLearningData())) {
                    newCardCount -= 1;
                    if (!scheduler.isCardLearned(updatedCard.getLearningData())) {
                        schedluledCardCount += 1;
                    }
                } else {
                    if (scheduler.isCardLearned(updatedCard.getLearningData())) {
                        schedluledCardCount -= 1;
                    }
                }

                // Dequeue card and update the queue
                queueManager.update(updatedCard);

                Card nextCard = queueManager.dequeue();
                queueManager.remove(nextCard);

                if (nextCard == null) {
                    showNoItemDialog();
                } else {
                    setCurrentCard(nextCard);
                    displayCard(false);
                }
            }
        };

    private String getActivityTitleString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.new_text) + ": " + newCardCount + " ");
        sb.append(getString(R.string.review_short_text) + ": " + schedluledCardCount + " ");
        sb.append(getString(R.string.id_text) + ": " + getCurrentCard().getId() + " ");
        if (!Strings.isNullOrEmpty(getCurrentCard().getCategory().getName())) {
            sb.append(getString(R.string.category_short_text) + ": " + getCurrentCard().getCategory().getName());
        }
        return sb.toString();
    }

    private void markCurrentCardAsLearnedForever() {
        if(getCurrentCard() != null) {
            getDbOpenHelper().getLearningDataDao()
                .markAsLearnedForever(getCurrentCard().getLearningData());

            // Do not restart on this card
            setCurrentCard(null);
            restartActivity();
        }
    }

    private void showGesturesDialog() {
        final HashMap<String, String> gestureNameDescriptionMap
            = new HashMap<String, String>();
        gestureNameDescriptionMap.put(GestureName.O_SHAPE.getName(), getString(R.string.look_up_text));
        gestureNameDescriptionMap.put(GestureName.S_SHAPE.getName(), getString(R.string.paint_text));


        GestureSelectionDialogFragment df = new GestureSelectionDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(GestureSelectionDialogFragment.EXTRA_GESTURE_NAME_DESCRIPTION_MAP, gestureNameDescriptionMap);
        df.setArguments(args);
        df.show(getSupportFragmentManager(), "GestureSelectionDialog");
    }

    private void lookupDictionary() {
        dictionaryUtil.showLookupListDialog(getCurrentCard().getQuestion(), getCurrentCard().getAnswer());
    }

    private void showEditDialog() {
        Intent myIntent = new Intent(this, CardEditor.class);
        myIntent.putExtra(CardEditor.EXTRA_DBPATH, this.dbPath);
        myIntent.putExtra(CardEditor.EXTRA_CARD_ID, getCurrentCard().getId());
        myIntent.putExtra(CardEditor.EXTRA_IS_EDIT_NEW, false);
        startActivityForResult(myIntent, ACTIVITY_EDIT);
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete_text)
            .setMessage(R.string.delete_warning)
            .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    if(getCurrentCard() != null){
                        getDbOpenHelper().getCardDao().delete(getCurrentCard());
                        // Do not restart with this card
                        setCurrentCard(null);
                        restartActivity();
                    }
                }
            })
        .setNegativeButton(R.string.cancel_text, null)
            .show();
    }

    private void showMarkAsLearnedForeverDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.mark_as_learned_forever_text)
            .setMessage(R.string.mark_as_learned_forever_warning_text)
            .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface arg0, int arg1) {
                    markCurrentCardAsLearnedForever();
                }
            })
        .setNegativeButton(R.string.cancel_text, null)
            .show();
    }

    private void gotoPreviewEdit() {
        Intent myIntent = new Intent();
        myIntent.setClass(this, PreviewEditActivity.class);
        myIntent.putExtra(PreviewEditActivity.EXTRA_DBPATH, dbPath);
        if (getCurrentCard() != null) {
            myIntent.putExtra(PreviewEditActivity.EXTRA_CARD_ID, getCurrentCard().getId());
        }

        startActivity(myIntent);
    }

    private void gotoPaint() {
        Intent myIntent = new Intent(this, FingerPaint.class);
        startActivity(myIntent);
    }

    private void gotoDetail() {
        Intent myIntent = new Intent(this, DetailScreen.class);
        myIntent.putExtra(DetailScreen.EXTRA_DBPATH, this.dbPath);
        myIntent.putExtra(DetailScreen.EXTRA_CARD_ID, getCurrentCard().getId());
        startActivityForResult(myIntent, ACTIVITY_DETAIL);
    }

    private void gotoSettings() {
        Intent myIntent = new Intent(this, SettingsScreen.class);
        myIntent.putExtra(SettingsScreen.EXTRA_DBPATH, dbPath);
        startActivityForResult(myIntent, ACTIVITY_SETTINGS);
    }

    private void gotoHelp() {
        Intent myIntent = new Intent();
        myIntent.setAction(Intent.ACTION_VIEW);
        myIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        myIntent.setData(Uri.parse(WEBSITE_HELP_MEMO));
        startActivity(myIntent);
    }
}
