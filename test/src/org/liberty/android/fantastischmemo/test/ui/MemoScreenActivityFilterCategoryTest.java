package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

public class MemoScreenActivityFilterCategoryTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public MemoScreenActivityFilterCategoryTest() {
        super("org.liberty.android.fantastischmemo", AnyMemo.class);
    }

    private Solo solo;

    public void setUp() throws Exception{
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        
        
        mActivity = this.getActivity();
        solo = new Solo(getInstrumentation(), mActivity);

        if (solo.searchText("New version")) {
            solo.clickOnText(solo.getString(R.string.ok_text));
        }
        solo.sleep(4000);
        setUpCategories();
        solo.clickLongOnText(UITestHelper.SAMPLE_DB_NAME);
        solo.clickOnText(solo.getString(R.string.study_text));
        solo.waitForActivity("MemoScreen");
        solo.sleep(4000);
    }

    // Move 2, 5, 8 to category: cat1
    private void setUpCategories() throws Exception {
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(getInstrumentation().getTargetContext(), UITestHelper.SAMPLE_DB_PATH);
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

    public void testFilterCategory() {
        solo.sendKey(Solo.MENU);
        solo.clickOnText("More");
        solo.clickOnText(solo.getString(R.string.filter_category_text));
        solo.clickOnText("cat1");
        solo.clickOnText(solo.getString(R.string.ok_text));
        // Wait refersh the activity
        solo.sleep(5000);
        assertTrue(solo.searchText("hair"));
        solo.clickOnText("Show answer");
        solo.clickOnText(solo.getString(R.string.memo_btn4_text));
        assertTrue(solo.searchText("eyes"));
        solo.clickOnText("Show answer");
        solo.clickOnText(solo.getString(R.string.memo_btn4_text));
        assertTrue(solo.searchText("mouth"));
        solo.clickOnText("Show answer");
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
