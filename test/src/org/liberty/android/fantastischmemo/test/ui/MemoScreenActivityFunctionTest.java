package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

import android.view.KeyEvent;

import android.widget.TabHost;

public class MemoScreenActivityFunctionTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public MemoScreenActivityFunctionTest () {
        super("org.liberty.android.fantastischmemo.ui", AnyMemo.class);
    }

    private Solo solo;

    private TabHost tabHost;

    public void setUp() throws Exception{
        mActivity = this.getActivity();
        tabHost = (TabHost)mActivity.findViewById(android.R.id.tabhost);
        solo = new Solo(getInstrumentation(), mActivity);
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation().getContext(), mActivity);
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();

        if (solo.searchText("New version")) {
            solo.clickOnText(solo.getString(R.string.ok_text));
        }

        // GO to Open tab
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                tabHost.requestFocus();
            }
        });
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        solo.clickOnText(UITestHelper.SAMPLE_DB_NAME);
        solo.clickOnText(solo.getString(R.string.study_text));
        solo.waitForActivity("MemoScreen");
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
