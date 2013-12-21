package org.liberty.android.fantastischmemo.test.integration;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.ui.PreviewEditActivity;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.jayway.android.robotium.solo.Solo;

public class PreviewEditActivityFunctionTest extends ActivityInstrumentationTestCase2<PreviewEditActivity> {

    protected PreviewEditActivity mActivity;

    @SuppressWarnings("deprecation")
    public PreviewEditActivityFunctionTest() {
        super("org.liberty.android.fantastischmemo", PreviewEditActivity.class);
    }

    private Solo solo;

    public void setUp() throws Exception{
        TestHelper uiTestHelper = new TestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();

        Intent intent = new Intent();
        intent.putExtra(PreviewEditActivity.EXTRA_DBPATH, TestHelper.SAMPLE_DB_PATH);
        setActivityIntent(intent);
        mActivity = this.getActivity();

        solo = new Solo(getInstrumentation(), mActivity);
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }


    @LargeTest
    public void testSearchTextForward() throws Exception {
        solo.clickOnActionBarItem(R.id.action_search);
        solo.enterText(0, "ac");
        solo.sendKey(Solo.ENTER);

        solo.sleep(1000);
        assertTrue(solo.searchText("face"));

        // Search again should return next result
        solo.sendKey(Solo.ENTER);
        solo.sleep(1000);
        assertTrue(solo.searchText("stomach"));

        // Search again should return next result
        solo.sendKey(Solo.ENTER);
        solo.sleep(1000);
        assertTrue(solo.searchText("back"));

        // Search again should return the first result in the beginning
        solo.sendKey(Solo.ENTER);
        solo.sleep(1000);
        assertTrue(solo.searchText("face"));
    }

    @LargeTest
    public void testSearchById() throws Exception {
        solo.clickOnActionBarItem(R.id.action_search);
        solo.enterText(0, "#15");
        solo.sendKey(Solo.ENTER);

        solo.sleep(1000);
        assertTrue(solo.searchText("arm"));
    }

    @LargeTest
    public void testSearchByIdNotExist() throws Exception {
        solo.clickOnActionBarItem(R.id.action_search);
        solo.enterText(0, "#70");
        solo.sendKey(Solo.ENTER);

        solo.sleep(1000);
        // Stay the same card
        assertTrue(solo.searchText("head"));
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
