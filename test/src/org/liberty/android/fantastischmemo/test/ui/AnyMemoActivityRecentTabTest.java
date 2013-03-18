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
        // The study action item
        solo.clickOnView(solo.getView(R.id.study));

        assertTrue(solo.waitForActivity("StudyActivity"));
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    public void testActionListPrevEdit() {
        // The edit action item
        solo.clickOnView(solo.getView(R.id.edit));

        assertTrue(solo.waitForActivity("PreviewEditActivity"));
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    public void testActionListCardList() {
        // The list action item
        solo.clickOnView(solo.getView(R.id.list));

        assertTrue(solo.waitForActivity("ListEditScreen"));
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }


    public void testActionListSettings() {
        // The settings action item
        solo.clickOnView(solo.getView(R.id.settings));

        assertTrue(solo.waitForActivity("SettingsScreen"));
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    public void testActionListDelete() {
        // The delete action item
        solo.clickOnView(solo.getView(R.id.delete));

        // The cancel button
        solo.clickOnView(solo.getView(android.R.id.button2));
        assertTrue(solo.searchText(UITestHelper.SAMPLE_DB_NAME));
        
        solo.clickLongOnText(UITestHelper.SAMPLE_DB_NAME);
        // The delete action item
        solo.clickOnView(solo.getView(R.id.delete));

        // The delete button
        solo.clickOnView(solo.getView(android.R.id.button1));
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
