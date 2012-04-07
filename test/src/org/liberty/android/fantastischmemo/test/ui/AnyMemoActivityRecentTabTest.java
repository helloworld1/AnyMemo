package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

public class AnyMemoActivityRecentTabTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public AnyMemoActivityRecentTabTest() {
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
        solo.clickLongOnText(UITestHelper.SAMPLE_DB_NAME);
    }


    public void testActionListStudy() {
        solo.clickOnText(solo.getString(R.string.study_text));
        solo.waitForActivity("MemoScreen");
        solo.sleep(3000);
    }

    public void testActionListPrevEdit() {
        solo.clickOnText(solo.getString(R.string.edit_button_text));
        solo.waitForActivity("EditScreen");
        solo.sleep(3000);
    }

    public void testActionListCardList() {
        solo.clickOnText(solo.getString(R.string.list_mode_text));
        solo.waitForActivity("ListEditScreen");
        solo.sleep(3000);
    }

    public void testActionListCram() {
        solo.clickOnText(solo.getString(R.string.learn_ahead));
        solo.waitForActivity("MemoScreen");
        solo.sleep(3000);
    }

    public void testActionListSettings() {
        solo.clickOnText(solo.getString(R.string.settings_menu_text));
        solo.waitForActivity("SettingsScreen");
        solo.sleep(3000);
    }

    public void testActionListDelete() {
        solo.clickOnText(solo.getString(R.string.delete_text));
        solo.clickOnButton(solo.getString(R.string.cancel_text));
        assertTrue(solo.searchText(UITestHelper.SAMPLE_DB_NAME));
        
        solo.clickLongOnText(UITestHelper.SAMPLE_DB_NAME);
        solo.clickOnText(solo.getString(R.string.delete_text));
        solo.clickOnButton(solo.getString(R.string.delete_text));
        assertFalse(solo.searchText(UITestHelper.SAMPLE_DB_NAME));
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
