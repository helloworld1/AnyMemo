package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

public class AnyMemoActivityOpenTabTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public AnyMemoActivityOpenTabTest() {
        super("org.liberty.android.fantastischmemo", AnyMemo.class);
    }

    private Solo solo;


    public void setUp() throws Exception{
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        
        mActivity = this.getActivity();
        solo = new Solo(getInstrumentation(), mActivity);
        solo.sleep(1000);

        if (solo.searchText("New version")) {
            solo.clickOnText(solo.getString(R.string.ok_text));
        }
        solo.sleep(4000);
        //solo.clickOnText(solo.getString(R.string.open_tab_text));
        solo.clickOnText("Open");

    }


    public void testActionListStudy() {
        solo.sleep(500);
        solo.clickOnText("bba.db");
        solo.sleep(3000);
    }


    public void tearDown() throws Exception {
        try {
            solo.finishOpenedActivities();
            solo.sleep(1000);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        super.tearDown();
    }

}
