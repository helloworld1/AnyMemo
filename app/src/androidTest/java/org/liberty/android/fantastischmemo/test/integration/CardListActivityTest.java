package org.liberty.android.fantastischmemo.test.integration;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.scheduler.Scheduler;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.ui.CardEditor;
import org.liberty.android.fantastischmemo.ui.CardListActivity;
import org.liberty.android.fantastischmemo.ui.DetailScreen;
import org.liberty.android.fantastischmemo.ui.PreviewEditActivity;

import roboguice.RoboGuice;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.ListView;

import com.robotium.solo.Solo;

public class CardListActivityTest extends ActivityInstrumentationTestCase2<CardListActivity> {

    protected CardListActivity mActivity;

    @SuppressWarnings("deprecation")
    public CardListActivityTest() {
        super("org.liberty.android.fantastischmemo", CardListActivity.class);
    }

    private Solo solo;

    public void setUp() throws Exception {
        TestHelper uiTestHelper = new TestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();

        Intent intent = new Intent();
        intent.putExtra(CardListActivity.EXTRA_DBPATH, TestHelper.SAMPLE_DB_PATH);
        setActivityIntent(intent);

        mActivity = this.getActivity();

        solo = new Solo(getInstrumentation(), mActivity);
        solo.sleep(2000);
    }

    @LargeTest
    public void testListCards() throws Exception {
        assertTrue(solo.searchText("head"));
        assertTrue(solo.searchText("arm"));
        assertTrue(solo.searchText("toe"));
    }

    @LargeTest
    public void testGoToCardEditor() throws Exception {
        solo.clickLongOnText("tooth");
        solo.clickOnText(solo.getString(R.string.edit_text));
        solo.waitForActivity(CardEditor.class);
        solo.sleep(2000);
        assertTrue(solo.searchText("tooth"));
    }

    @LargeTest
    public void testGoToPrevEditor() throws Exception {
        solo.clickLongOnText("tooth");
        solo.clickOnText(solo.getString(R.string.edit_button_text));
        solo.waitForActivity(PreviewEditActivity.class);
        solo.sleep(2000);
        assertTrue(solo.searchText("tooth"));
    }

    @LargeTest
    public void testGoToDetailScreen() throws Exception {
        solo.clickLongOnText("tooth");
        solo.clickOnText(solo.getString(R.string.detail_menu_text));
        solo.waitForActivity(DetailScreen.class);
        solo.sleep(2000);
        assertTrue(solo.searchText("tooth"));
    }

    @LargeTest
    public void testMarkAsLearned() {
        solo.clickOnText("tooth");
        solo.clickOnText(solo.getString(R.string.mark_as_learned_text));
        solo.sleep(2000);

        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            Scheduler scheduler = RoboGuice.getInjector(mActivity).getInstance(Scheduler.class);
            LearningDataDao learningDataDao = helper.getLearningDataDao();
            LearningData ld = learningDataDao.queryForId(10);

            assertEquals(true, scheduler.isCardLearned(ld));
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
        assertTrue(solo.searchText("tooth"));
    }

    @LargeTest
    public void testMarkAsForgotten() {
        solo.clickOnText("tooth");
        solo.clickOnText(solo.getString(R.string.mark_as_forgotten_text));
        solo.sleep(2000);

        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            Scheduler scheduler = RoboGuice.getInjector(mActivity).getInstance(Scheduler.class);
            LearningDataDao learningDataDao = helper.getLearningDataDao();
            LearningData ld = learningDataDao.queryForId(10);

            assertEquals(true, scheduler.isCardForReview(ld));
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
        assertTrue(solo.searchText("tooth"));
    }

    @LargeTest
    public void testMarkAsNew() {
        solo.clickOnText("tooth");
        solo.clickOnText(solo.getString(R.string.mark_as_forgotten_text));
        solo.sleep(2000);
        solo.clickOnText("tooth");
        solo.clickOnText(solo.getString(R.string.mark_as_new_text));
        solo.sleep(2000);

        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            Scheduler scheduler = RoboGuice.getInjector(mActivity).getInstance(Scheduler.class);
            LearningDataDao learningDataDao = helper.getLearningDataDao();
            LearningData ld = learningDataDao.queryForId(10);

            assertEquals(true, scheduler.isCardNew(ld));
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
        assertTrue(solo.searchText("tooth"));
    }

    @LargeTest
    public void testMarkAsLearnedForever() {
        solo.clickOnText("tooth");
        solo.clickOnText(solo.getString(R.string.mark_as_learned_forever_text));
        solo.sleep(2000);

        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            Scheduler scheduler = RoboGuice.getInjector(mActivity).getInstance(Scheduler.class);
            LearningDataDao learningDataDao = helper.getLearningDataDao();
            LearningData ld = learningDataDao.queryForId(10);

            assertEquals(true, scheduler.isCardLearned(ld));
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
        assertTrue(solo.searchText("tooth"));
    }

    @LargeTest
    public void testShowHideAnswer() throws Exception {
        assertTrue(solo.searchText("la dent", true));

        solo.clickOnActionBarItem(R.id.show_hide_answers);
        solo.sleep(500);
        // Make sure the search come from the top of the list
        solo.scrollListToTop((ListView) solo.getView(R.id.item_list));
        assertFalse(solo.searchText("la dent", true));

        solo.clickOnActionBarItem(R.id.show_hide_answers);
        solo.sleep(500);
        // Make sure the search come from the top of the list
        solo.scrollListToTop((ListView) solo.getView(R.id.item_list));
        assertTrue(solo.searchText("la dent", true));
    }

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
