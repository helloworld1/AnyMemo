package org.liberty.android.fantastischmemo.test.integration;

import java.io.File;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.ui.AnyMemo;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.robotium.solo.Solo;

public class AnyMemoActivityRecentTabTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    @SuppressWarnings("deprecation")
    public AnyMemoActivityRecentTabTest() {
        super("org.liberty.android.fantastischmemo", AnyMemo.class);
    }

    private Solo solo;


    public void setUp() throws Exception{
        TestHelper uiTestHelper = new TestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.markNotFirstTime();

        mActivity = this.getActivity();
        solo = new Solo(getInstrumentation(), mActivity);

        solo.sleep(1000);
        solo.clickLongOnText(TestHelper.SAMPLE_DB_NAME);
    }


    @LargeTest
    public void testActionListStudy() {
        // The study action item
        solo.clickOnView(solo.getView(R.id.study));

        assertTrue(solo.waitForActivity("StudyActivity"));
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    @LargeTest
    public void testActionListPrevEdit() {
        // The edit action item
        solo.clickOnView(solo.getView(R.id.edit));

        assertTrue(solo.waitForActivity("PreviewEditActivity"));
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    @LargeTest
    public void testActionListCardList() {
        // The list action item
        solo.clickOnView(solo.getView(R.id.list));

        assertTrue(solo.waitForActivity("CardListActivity"));
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }


    @LargeTest
    public void testActionListSettings() {
        // The settings action item
        solo.clickOnView(solo.getView(R.id.settings));

        assertTrue(solo.waitForActivity("SettingsScreen"));
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    @LargeTest
    public void testActionListDelete() {
        // The delete action item
        solo.clickOnView(solo.getView(R.id.delete));

        // The cancel button
        solo.clickOnView(solo.getView(android.R.id.button2));
        solo.sleep(600);
        assertTrue(solo.searchText(TestHelper.SAMPLE_DB_NAME));

        solo.clickLongOnText(TestHelper.SAMPLE_DB_NAME);
        solo.sleep(600);
        // The delete action item
        solo.clickOnView(solo.getView(R.id.delete));
        solo.sleep(600);

        // The delete button
        solo.clickOnView(solo.getView(android.R.id.button1));
        solo.sleep(600);
        assertFalse((new File(TestHelper.SAMPLE_DB_PATH)).exists());
    }

    public void tearDown() throws Exception {
        try {
            solo.finishOpenedActivities();
            solo.sleep(1000);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        super.tearDown();
        solo = null;
    }

}
