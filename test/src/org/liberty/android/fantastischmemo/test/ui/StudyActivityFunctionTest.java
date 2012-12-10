package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.ui.StudyActivity;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.jayway.android.robotium.solo.Solo;

public class StudyActivityFunctionTest extends ActivityInstrumentationTestCase2<StudyActivity> {

    protected StudyActivity mActivity;

    private View questionView;

    private View answerView;

    public StudyActivityFunctionTest() {
        super("org.liberty.android.fantastischmemo", StudyActivity.class);
    }

    private Solo solo;

    public void setUp() throws Exception{
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();
        
        Intent intent = new Intent();
        intent.putExtra(StudyActivity.EXTRA_DBPATH, UITestHelper.SAMPLE_DB_PATH);
        setActivityIntent(intent);

        mActivity = this.getActivity();

        questionView = mActivity.findViewById(R.id.question);

        answerView = mActivity.findViewById(R.id.answer);

        solo = new Solo(getInstrumentation(), mActivity);
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }


    public void testUndo() throws Exception {


        // Success 1st card
        solo.clickOnView(answerView);
        solo.clickOnText(solo.getString(R.string.memo_btn2_text));

        // Success 2nd card
        solo.clickOnView(answerView);
        solo.clickOnText(solo.getString(R.string.memo_btn2_text));

        // Undo
        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getString(R.string.undo_text));

        solo.waitForDialogToClose(8000);
        solo.sleep(600);

        // 2nd card should be shown
        assertTrue(solo.searchText("hair"));

        // Fail 2nd card 
        answerView = solo.getCurrentActivity().findViewById(R.id.answer);
        solo.clickOnView(answerView);
        solo.sleep(600);
        solo.clickOnText(solo.getString(R.string.memo_btn0_text));

        solo.goBack();

        solo.waitForDialogToClose(8000);
        solo.sleep(600);

        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            CardDao cardDao = helper.getCardDao();
            // 2nd card failed
            assertEquals(1, cardDao.getScheduledCardCount(null));
            // 28 - 2 = 26
            assertEquals(26, cardDao.getNewCardCount(null));
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    public void testDeleteCard() throws Exception {
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.menu_context_delete, 0);
        solo.clickOnText(solo.getString(R.string.ok_text));
        solo.goBack();

        solo.sleep(2000);
        solo.waitForDialogToClose(8000);

        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            CardDao cardDao = helper.getCardDao();
            LearningDataDao learningDataDao = helper.getLearningDataDao();
            // 28 - 1 = 27
            assertEquals(27L, cardDao.countOf());
            assertEquals(27L, learningDataDao.countOf());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

    }

    public void testSkipCard() throws Exception {
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.menu_context_skip, 0);
        // press skip
        solo.clickOnText(solo.getString(R.string.skip_text));
        solo.clickOnText(solo.getString(R.string.ok_text));

        solo.waitForDialogToClose(8000);
        solo.sleep(600);

        // The card should not be shown
        assertFalse(solo.searchText("head"));
        solo.goBack();

        solo.sleep(2000);
        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            CardDao cardDao = helper.getCardDao();
            // One card skipped ...
            assertEquals(27, cardDao.getNewCardCount(null));
            // ... and shouldn't be a scheduled card
            assertEquals(0, cardDao.getScheduledCardCount(null));
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    public void testGotoPreviewScreen() {
        // press skip
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.menu_context_gotoprev, 0);
        assertTrue(solo.waitForActivity("PreviewEditActivity"));
        solo.sleep(1000);
    }

    public void testGotoDetailScreen() {
        // The way to click menu item in action bar
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.menudetail, 0);
        assertTrue(solo.waitForActivity("DetailScreen"));
        solo.sleep(1000);
    }

    public void testGotoSettingsScreen() {
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.menusettings, 0);
        assertTrue(solo.waitForActivity("SettingsScreen"));
        solo.sleep(1000);
    }

    public void tearDown() throws Exception {
        try {
            solo.finishOpenedActivities();
            solo.sleep(2000);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        super.tearDown();
    }

}
