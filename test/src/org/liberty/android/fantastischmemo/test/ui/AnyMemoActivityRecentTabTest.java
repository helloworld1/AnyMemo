package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

import java.io.File;

public class AnyMemoActivityRecentTabTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public AnyMemoActivityRecentTabTest() {
        super("org.liberty.android.fantastischmemo", AnyMemo.class);
    }

    private Solo solo;


    public void setUp() throws Exception{
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.markNotFirstTime();
        
        mActivity = this.getActivity();
        solo = new Solo(getInstrumentation(), mActivity);

        solo.sleep(1000);
        solo.clickLongOnText(UITestHelper.SAMPLE_DB_NAME);
    }


    public void testActionListStudy() {
        solo.clickOnText(solo.getString(R.string.study_text));
        solo.waitForActivity("StudyActivity");
        solo.sleep(3000);
    }

    public void testActionListPrevEdit() {
        solo.clickOnText(solo.getString(R.string.edit_button_text));
        solo.waitForActivity("PreviewEditActivity");
        solo.sleep(3000);
    }

    public void testActionListCardList() {
        solo.clickOnText(solo.getString(R.string.list_mode_text));
        solo.waitForActivity("ListPreviewEditActivity");
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
        solo.sleep(1000);
        assertFalse((new File(UITestHelper.SAMPLE_DB_PATH)).exists());
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
