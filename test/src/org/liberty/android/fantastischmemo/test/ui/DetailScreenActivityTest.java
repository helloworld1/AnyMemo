package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.domain.Card;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

public class DetailScreenActivityTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public DetailScreenActivityTest() {
        super("org.liberty.android.fantastischmemo", AnyMemo.class);
    }

    private Solo solo;

    public void setUp() throws Exception {
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
        solo.waitForActivity("MemoScreen");
        solo.sleep(4000);
    }

    public void testDisplayDetailInfo() throws Exception {
        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getString(R.string.detail_menu_text));
        solo.waitForActivity("DetailScreen");
        solo.sleep(2000);
        assertTrue(solo.searchText("head"));
        assertTrue(solo.searchText("2.5"));
    }

    public void testSaveChanges() throws Exception {
        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getString(R.string.detail_menu_text));
        solo.waitForActivity("DetailScreen");
        solo.sleep(2000);
        solo.clearEditText(1);
        solo.sleep(400);
        solo.enterText(1, "foot");
        solo.clickOnText(solo.getString(R.string.detail_update));
        solo.clickOnText(solo.getString(R.string.ok_text));
        solo.waitForActivity("MemoScreen");
        solo.sleep(4000);

        // asssert db state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            CardDao cardDao = helper.getCardDao();
            Card c1 = cardDao.queryForId(1);
            // 2 card failed
            assertEquals("foot", c1.getQuestion());
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
