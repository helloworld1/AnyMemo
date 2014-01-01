package org.liberty.android.fantastischmemo.test.integration;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.ui.StudyActivity;
import org.liberty.android.fantastischmemo.utils.AnyMemoExecutor;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.jayway.android.robotium.solo.Solo;

public class StudyActivityFilterCategoryTest extends ActivityInstrumentationTestCase2<StudyActivity> {

    protected StudyActivity mActivity;

    @SuppressWarnings("deprecation")
    public StudyActivityFilterCategoryTest() {
        super("org.liberty.android.fantastischmemo", StudyActivity.class);
    }

    private Solo solo;

    public void setUp() throws Exception{
        TestHelper uiTestHelper = new TestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();
        setUpCategories();

        Intent intent = new Intent();
        intent.putExtra(StudyActivity.EXTRA_DBPATH, TestHelper.SAMPLE_DB_PATH);
        setActivityIntent(intent);

        mActivity = this.getActivity();

        solo = new Solo(getInstrumentation(), mActivity);
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    // Move 2, 5, 8 to category: cat1
    @LargeTest
    private void setUpCategories() throws Exception {
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(getInstrumentation().getTargetContext(), TestHelper.SAMPLE_DB_PATH);
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();
        Category cat1 = categoryDao.createOrReturn("cat1");
        Card c2 = cardDao.queryForId(2);
        c2.setCategory(cat1);
        cardDao.update(c2);

        Card c5 = cardDao.queryForId(5);
        c5.setCategory(cat1);
        cardDao.update(c5);

        Card c8 = cardDao.queryForId(8);
        c8.setCategory(cat1);
        cardDao.update(c8);
        AnyMemoDBOpenHelperManager.releaseHelper(helper);
    }

    @LargeTest
    public void testFilterCategory() {
        // Filter category menu item
        solo.clickOnActionBarItem(R.id.menu_memo_category);

        solo.clickOnText("cat1");
        solo.clickOnText(solo.getString(R.string.ok_text));
        // Wait refersh the activity
        AnyMemoExecutor.waitAllTasks();
        solo.sleep(2000);

        assertTrue(solo.searchText("hair"));

        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        solo.clickOnText(solo.getString(R.string.memo_btn4_text));
        assertTrue(solo.searchText("eyes"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        solo.clickOnText(solo.getString(R.string.memo_btn4_text));
        assertTrue(solo.searchText("mouth"));
        solo.clickOnText(solo.getString(R.string.memo_show_answer));
        solo.clickOnText(solo.getString(R.string.memo_btn4_text));
        assertTrue(solo.searchText(solo.getString(R.string.memo_no_item_title)));
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
