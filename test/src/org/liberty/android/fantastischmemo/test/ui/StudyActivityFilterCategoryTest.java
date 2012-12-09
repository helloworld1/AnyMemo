package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.ui.StudyActivity;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.jayway.android.robotium.solo.Solo;

public class StudyActivityFilterCategoryTest extends ActivityInstrumentationTestCase2<StudyActivity> {

    protected StudyActivity mActivity;

    private View questionView;

    private View answerView;

    public StudyActivityFilterCategoryTest() {
        super("org.liberty.android.fantastischmemo", StudyActivity.class);
    }

    private Solo solo;

    public void setUp() throws Exception{
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();
        setUpCategories();
        
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
        // Filter category menu item
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.menu_memo_category, 0);

        solo.clickOnText("cat1");
        solo.clickOnText(solo.getString(R.string.ok_text));
        // Wait refersh the activity
        solo.waitForDialogToClose(8000);
        solo.sleep(2000);

        answerView = solo.getCurrentActivity().findViewById(R.id.answer);
        
        assertTrue(solo.searchText("hair"));
        
        solo.clickOnView(answerView);
        solo.clickOnText(solo.getString(R.string.memo_btn4_text));
        assertTrue(solo.searchText("eyes"));
        solo.clickOnView(answerView);
        solo.clickOnText(solo.getString(R.string.memo_btn4_text));
        assertTrue(solo.searchText("mouth"));
        solo.clickOnView(answerView);
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
