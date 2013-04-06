package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.ui.StudyActivity;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.jayway.android.robotium.solo.Solo;

public class StudyActivityStudyTest extends ActivityInstrumentationTestCase2<StudyActivity> {

    protected StudyActivity mActivity;

    private View questionView;

    private View answerView;

    public StudyActivityStudyTest () {
        super("org.liberty.android.fantastischmemo", StudyActivity.class);
    }

    private Solo solo;

    public void setUp() throws Exception {
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

    public void testGrade() throws Exception {
        // Success on card #1
        assertTrue(solo.searchText("head"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("la "));
        solo.clickOnText(solo.getString(R.string.memo_btn4_text));

        // Success on card #2
        assertTrue(solo.searchText("hair"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("les"));
        solo.clickOnText(solo.getString(R.string.memo_btn5_text));

        // Fail on card #3
        assertTrue(solo.searchText("face"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("visage"));
        solo.clickOnText(solo.getString(R.string.memo_btn1_text));

        // Fail on card #4
        assertTrue(solo.searchText("eye"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("oeil"));
        solo.clickOnText(solo.getString(R.string.memo_btn0_text));

        // Success on card #5
        assertTrue(solo.searchText("eyes"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("yeux"));
        solo.clickOnText(solo.getString(R.string.memo_btn3_text));
        solo.goBack();
        solo.waitForDialogToClose(8000);
        solo.sleep(5000);

        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            CardDao cardDao = helper.getCardDao();
            // 2 card failed
            assertEquals(2, cardDao.getScheduledCardCount(null));
            // 28 - 5 = 23
            assertEquals(23, cardDao.getNewCardCount(null));
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    // Test forget 1st card and 3rd and learn 9 new card
    // the 1st and 3rd card should reappear.
    public void testFailedCardRepeat() throws Exception {
        for (int i = 0; i < 10; i++) {
            solo.clickOnView(answerView);
            // Fail 1st and 3rd
            if (i == 0 || i == 2) {
                solo.clickOnText(solo.getString(R.string.memo_btn1_text));
            } else {
                // Success 8 other 
                solo.clickOnText(solo.getString(R.string.memo_btn4_text));
            }
        }
        // 1st
        assertTrue(solo.searchText("head"));
        solo.clickOnView(answerView);
        solo.clickOnText(solo.getString(R.string.memo_btn4_text));
        
        // 11th card, new
        assertTrue(solo.searchText("ear"));
        solo.clickOnView(answerView);
        solo.clickOnText(solo.getString(R.string.memo_btn4_text));

        // 3rd
        assertTrue(solo.searchText("face"));
        solo.clickOnView(answerView);
        solo.clickOnText(solo.getString(R.string.memo_btn0_text));
        solo.goBack();

        solo.waitForDialogToClose(8000);
        solo.sleep(2000);

        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            CardDao cardDao = helper.getCardDao();
            // 2 card failed
            assertEquals(1, cardDao.getScheduledCardCount(null));
            // 28 - 11 = 17
            assertEquals(17, cardDao.getNewCardCount(null));
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
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
