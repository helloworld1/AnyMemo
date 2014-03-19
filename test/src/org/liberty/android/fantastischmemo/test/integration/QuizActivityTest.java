package org.liberty.android.fantastischmemo.test.integration;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.ui.QuizActivity;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.robotium.solo.Solo;

public class QuizActivityTest extends ActivityInstrumentationTestCase2<QuizActivity> {

    protected QuizActivity mActivity;

    private static final int TEST_QUIZ_SIZE = 3;

    private static final int TEST_START_ORD = 5;

    @SuppressWarnings("deprecation")
    public QuizActivityTest() {
        super("org.liberty.android.fantastischmemo", QuizActivity.class);
    }

    private Solo solo;

    public void setUp() throws Exception{
        TestHelper uiTestHelper = new TestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();
    }

    @LargeTest
    public void testLearningWithoutFailures() {
        launchQuizBySize();
        // Card number 5
        assertTrue(solo.searchText("eyes"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("les"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // Card number 6
        assertTrue(solo.searchText("nose"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("nez"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // Card number 7
        assertTrue(solo.searchText("cheek"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("joue"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // The finishing dialog should be poped up
        solo.waitForDialogToOpen(1000);
        solo.clickOnText(solo.getString(R.string.back_menu_text));
    }

    @LargeTest
    public void testLearningFailures() {
        launchQuizBySize();
        // Card number 5
        assertTrue(solo.searchText("eyes"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("les"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // Card number 6
        assertTrue(solo.searchText("nose"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("nez"));
        solo.clickOnText(solo.getString(R.string.forget_text));

        // Card number 7
        assertTrue(solo.searchText("cheek"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("joue"));
        solo.clickOnText(solo.getString(R.string.forget_text));

        // The finishing dialog should be poped up
        solo.waitForDialogToOpen(1000);
        solo.clickOnText(solo.getString(R.string.review_text));

        // Card number 6
        assertTrue(solo.searchText("nose"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("nez"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // Card number 7
        assertTrue(solo.searchText("cheek"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("joue"));
        solo.clickOnText(solo.getString(R.string.forget_text));

        // Repeat Card number 7
        assertTrue(solo.searchText("cheek"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("joue"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // The finishing dialog should be poped up
        solo.waitForDialogToOpen(1000);
        solo.clickOnText(solo.getString(R.string.back_menu_text));
    }

    /**
     * Set up 1 category with 3 cards and the quiz should only contain these 3 cards.
     */ 
    @LargeTest
    public void testQuizByCategory() {
        Category category = setupThreeCategories();
        launchQuizByCategory(category);

        // Card number 2
        assertTrue(solo.searchText("hair"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("les cheveux"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // Card number 5
        assertTrue(solo.searchText("eyes"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("les yeux"));
        solo.clickOnText(solo.getString(R.string.remember_text));

        // Card number 8
        assertTrue(solo.searchText("mouth"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        assertTrue(solo.searchText("la bouche"));
        solo.clickOnText(solo.getString(R.string.remember_text));
    }


    private void launchQuizBySize() {
        Intent intent = new Intent();
        intent.putExtra(QuizActivity.EXTRA_DBPATH, TestHelper.SAMPLE_DB_PATH);
        intent.putExtra(QuizActivity.EXTRA_QUIZ_SIZE, TEST_QUIZ_SIZE);
        intent.putExtra(QuizActivity.EXTRA_START_CARD_ORD, TEST_START_ORD);

        setActivityIntent(intent);
        mActivity = this.getActivity();

        solo = new Solo(getInstrumentation(), mActivity);
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    private void launchQuizByCategory(Category category) {
        Intent intent = new Intent();
        intent.putExtra(QuizActivity.EXTRA_DBPATH, TestHelper.SAMPLE_DB_PATH);
        intent.putExtra(QuizActivity.EXTRA_CATEGORY_ID, category.getId());

        setActivityIntent(intent);
        mActivity = this.getActivity();

        solo = new Solo(getInstrumentation(), mActivity);
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    /*
     * Card with "My Category" in ID 2, 5, 8
     */
    private Category setupThreeCategories() {

        AnyMemoDBOpenHelper dbHelper = AnyMemoDBOpenHelperManager.getHelper(getInstrumentation().getTargetContext(), TestHelper.SAMPLE_DB_PATH);

        try {
            CardDao cardDao = dbHelper.getCardDao();
            CategoryDao categoryDao = dbHelper.getCategoryDao();
            Card c = cardDao.queryForId(2);
            Category ct = new Category();
            ct.setName("My category");
            categoryDao.create(ct);
            c.setCategory(ct);
            cardDao.update(c);
            c = cardDao.queryForId(5);
            c.setCategory(ct);
            cardDao.update(c);
            c = cardDao.queryForId(8);
            c.setCategory(ct);
            cardDao.update(c);
            return ct;
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(dbHelper);
        }
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
        solo = null;
    }

}
