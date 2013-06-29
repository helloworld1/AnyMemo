package org.liberty.android.fantastischmemo.test.integration;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.ui.QuizActivity;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.jayway.android.robotium.solo.Solo;

public class QuizActivityTest extends ActivityInstrumentationTestCase2<QuizActivity> {

    protected QuizActivity mActivity;

    private static final int TEST_QUIZ_SIZE = 3;

    private static final int TEST_START_ORD = 5;

    private View answerView;

    @SuppressWarnings("deprecation")
    public QuizActivityTest() {
        super("org.liberty.android.fantastischmemo", QuizActivity.class);
    }

    private Solo solo;

    public void setUp() throws Exception{
        TestHelper uiTestHelper = new TestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();

        Intent intent = new Intent();
        intent.putExtra(QuizActivity.EXTRA_DBPATH, TestHelper.SAMPLE_DB_PATH);
        intent.putExtra(QuizActivity.EXTRA_QUIZ_SIZE, TEST_QUIZ_SIZE);
        intent.putExtra(QuizActivity.EXTRA_START_CARD_ORD, TEST_START_ORD);
        setActivityIntent(intent);
        mActivity = this.getActivity();

        answerView = mActivity.findViewById(R.id.answer);

        solo = new Solo(getInstrumentation(), mActivity);
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    public void testLearningWithoutFailures() {
        // Card number 5
        assertTrue(solo.searchText("eyes"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("les"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // Card number 6
        assertTrue(solo.searchText("nose"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("nez"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // Card number 7
        assertTrue(solo.searchText("cheek"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("joue"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // The finishing dialog should be poped up
        solo.waitForDialogToOpen(1000);
        solo.clickOnText(solo.getString(R.string.back_menu_text));
    }

    public void testLearningFailures() {
        // Card number 5
        assertTrue(solo.searchText("eyes"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("les"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // Card number 6
        assertTrue(solo.searchText("nose"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("nez"));
        solo.clickOnText(solo.getString(R.string.forget_text));

        // Card number 7
        assertTrue(solo.searchText("cheek"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("joue"));
        solo.clickOnText(solo.getString(R.string.forget_text));

        // The finishing dialog should be poped up
        solo.waitForDialogToOpen(1000);
        solo.clickOnText(solo.getString(R.string.review_text));

        // Card number 6
        assertTrue(solo.searchText("nose"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("nez"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // Card number 7
        assertTrue(solo.searchText("cheek"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("joue"));
        solo.clickOnText(solo.getString(R.string.forget_text));

        // Repeat Card number 7
        assertTrue(solo.searchText("cheek"));
        solo.clickOnView(answerView);
        assertTrue(solo.searchText("joue"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // The finishing dialog should be poped up
        solo.waitForDialogToOpen(1000);
        solo.clickOnText(solo.getString(R.string.back_menu_text));
    }

    @Override
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
