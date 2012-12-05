package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.ui.DetailScreen;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

public class DetailScreenActivityTest extends ActivityInstrumentationTestCase2<DetailScreen> {

    protected DetailScreen mActivity;

    public DetailScreenActivityTest() {
        super("org.liberty.android.fantastischmemo", DetailScreen.class);
    }

    private Solo solo;

    public void setUp() throws Exception {
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();
        
        Intent intent = new Intent();
        intent.putExtra(DetailScreen.EXTRA_DBPATH, UITestHelper.SAMPLE_DB_PATH);
        intent.putExtra(DetailScreen.EXTRA_CARD_ID, 1);
        setActivityIntent(intent);

        mActivity = this.getActivity();
        solo = new Solo(getInstrumentation(), mActivity);
        solo.waitForDialogToClose(8000);
        solo.sleep(300);
    }

    public void testDisplayDetailInfo() throws Exception {
        assertTrue(solo.searchText("head"));
        assertTrue(solo.searchText("2.5"));
    }

    public void testSaveChanges() throws Exception {
        solo.sleep(400);
        solo.clearEditText(1);
        solo.enterText(1, "foot");

        getInstrumentation().invokeMenuActionSync(mActivity, R.id.save, 0);

        solo.clickOnText(solo.getString(R.string.ok_text));
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
