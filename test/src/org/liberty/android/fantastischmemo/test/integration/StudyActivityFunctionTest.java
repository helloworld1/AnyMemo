package org.liberty.android.fantastischmemo.test.integration;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.ui.StudyActivity;
import org.liberty.android.fantastischmemo.utils.AnyMemoExecutor;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.jayway.android.robotium.solo.Solo;

public class StudyActivityFunctionTest extends ActivityInstrumentationTestCase2<StudyActivity> {

    protected StudyActivity mActivity;

    @SuppressWarnings("deprecation")
    public StudyActivityFunctionTest() {
        super("org.liberty.android.fantastischmemo", StudyActivity.class);
    }

    private Solo solo;

    public void setUp() throws Exception{
        TestHelper uiTestHelper = new TestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();

        Intent intent = new Intent();
        intent.putExtra(StudyActivity.EXTRA_DBPATH, TestHelper.SAMPLE_DB_PATH);
        setActivityIntent(intent);

        mActivity = this.getActivity();

        solo = new Solo(getInstrumentation(), mActivity);
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    @LargeTest
    public void testUndo() throws Exception {


        // Success 1st card
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        solo.clickOnText(solo.getString(R.string.memo_btn2_text));

        // Success 2nd card
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        solo.clickOnText(solo.getString(R.string.memo_btn2_text));

        // Undo
        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getString(R.string.undo_text));

        AnyMemoExecutor.waitAllTasks();
        solo.sleep(600);

        // 2nd card should be shown
        assertTrue(solo.searchText("hair"));

        // Fail 2nd card
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        solo.sleep(600);
        solo.clickOnText(solo.getString(R.string.memo_btn0_text));

        solo.goBack();

        AnyMemoExecutor.waitAllTasks();
        solo.sleep(600);

        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
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

    @LargeTest
    public void testDeleteCard() throws Exception {
        solo.clickOnActionBarItem(R.id.menu_context_delete);
        solo.clickOnText(solo.getString(R.string.ok_text));
        solo.goBack();

        AnyMemoExecutor.waitAllTasks();
        solo.sleep(2000);

        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
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

    @LargeTest
    public void testMarkCardLearnedForever() throws Exception {
        solo.clickOnActionBarItem(R.id.menu_mark_as_learned_forever);

        solo.clickOnText(solo.getString(R.string.ok_text));

        AnyMemoExecutor.waitAllTasks();
        solo.sleep(2000);

        // The card should not be shown
        assertFalse(solo.searchText("head"));
        solo.goBack();

        solo.sleep(2000);
        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
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

    @LargeTest
    public void testGotoPreviewScreen() {
        solo.clickOnActionBarItem(R.id.menu_context_gotoprev);
        assertTrue(solo.waitForActivity("PreviewEditActivity"));
        solo.sleep(1000);
    }

    @LargeTest
    public void testGotoDetailScreen() {
        // The way to click menu item in action bar
        solo.clickOnActionBarItem(R.id.menudetail);
        assertTrue(solo.waitForActivity("DetailScreen"));
        solo.sleep(1000);
    }

    @LargeTest
    public void testGotoSettingsScreen() {
        solo.clickOnActionBarItem(R.id.menusettings);
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
