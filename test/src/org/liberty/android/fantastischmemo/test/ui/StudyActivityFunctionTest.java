package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

public class StudyActivityFunctionTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public StudyActivityFunctionTest() {
        super("org.liberty.android.fantastischmemo", AnyMemo.class);
    }

    private Solo solo;

    public void setUp() throws Exception{
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        
        mActivity = this.getActivity();
        solo = new Solo(getInstrumentation(), mActivity);
        solo.sleep(1000);

        if (solo.searchText("New version")) {
            solo.clickOnText(solo.getString(R.string.ok_text));
        }
        solo.sleep(4000);
        solo.clickLongOnText(UITestHelper.SAMPLE_DB_NAME);
        solo.clickOnText(solo.getString(R.string.study_text));
        solo.waitForActivity("StudyActivity");
        solo.sleep(4000);
    }


    public void testUndo() throws Exception {


        // Success 1st card
        solo.clickOnText("Show answer");
        solo.clickOnText(solo.getString(R.string.memo_btn2_text));

        // Success 2nd card
        solo.clickOnText("Show answer");
        solo.clickOnText(solo.getString(R.string.memo_btn2_text));

        // Undo
        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getString(R.string.undo_text));
        solo.sleep(5000);



        // 2nd card should be shown
        assertTrue(solo.searchText("hair"));

        // Fail 2nd card 
        solo.clickOnText("Show answer");
        solo.clickOnText(solo.getString(R.string.memo_btn0_text));

        solo.goBack();
        solo.goBack();
        solo.sleep(5000);

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
        solo.clickLongOnText("head");
        solo.clickOnText(solo.getString(R.string.delete_text));
        solo.clickOnText(solo.getString(R.string.ok_text));
        solo.sleep(3000);
        solo.goBack();
        solo.sleep(3000);
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
        solo.clickLongOnText("head");
        // press skip
        solo.clickOnText(solo.getString(R.string.skip_text));
        solo.clickOnText(solo.getString(R.string.ok_text));
        solo.sleep(5000);

        // The card should not be shown
        assertFalse(solo.searchText("head"));
        solo.goBack();
        solo.sleep(3000);
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
        solo.clickLongOnText("head");
        // press skip
        solo.clickOnText(solo.getString(R.string.goto_prev_screen));
        assertTrue(solo.waitForActivity("PreviewEditActivity"));
    }

    public void testGotoDetailScreen() {
        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getString(R.string.detail_menu_text));
        assertTrue(solo.waitForActivity("DetailScreen"));
    }

    public void testGotoSettingsScreen() {
        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getString(R.string.settings_menu_text));
        assertTrue(solo.waitForActivity("SettingsScreen"));
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
