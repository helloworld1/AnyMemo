package org.liberty.android.fantastischmemo.test.integration;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.ui.PreviewEditActivity;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.robotium.solo.Solo;

public class PreviewEditActivityMainButtonTest extends ActivityInstrumentationTestCase2<PreviewEditActivity> {

    protected PreviewEditActivity mActivity;

    @SuppressWarnings("deprecation")
    public PreviewEditActivityMainButtonTest () {
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
    public void testNavigation() throws Exception {
        // 1st card
        assertTrue(solo.searchText("head"));

        // 2nd card
        solo.clickOnText(solo.getString(R.string.add_screen_next));
        solo.sleep(300);
        assertTrue(solo.searchText("hair"));

        // 3rd card
        solo.clickOnText(solo.getString(R.string.add_screen_next));
        solo.sleep(600);
        assertTrue(solo.searchText("face"));

        // 2nd card
        solo.clickOnText(solo.getString(R.string.previous_text_short));
        solo.sleep(600);
        assertTrue(solo.searchText("hair"));

        // 1st card
        solo.clickOnText(solo.getString(R.string.previous_text_short));
        solo.sleep(600);
        assertTrue(solo.searchText("head"));

        // last (28th) card
        solo.clickOnText(solo.getString(R.string.previous_text_short));
        solo.sleep(600);
        assertTrue(solo.searchText("toe"));

        // 1st card
        solo.clickOnText(solo.getString(R.string.add_screen_next));
        solo.sleep(600);
        assertTrue(solo.searchText("head"));
    }

    // Test Edit the second card
    @LargeTest
    public void testEditCard() {
        solo.clickOnText(solo.getString(R.string.add_screen_next));
        solo.clickOnText(solo.getString(R.string.edit_text));
        solo.waitForActivity("CardEditor");
        solo.sleep(4000);

        // Make sure we are editing the 2nd card
        assertTrue(solo.searchText("hair"));
        solo.clearEditText(0);
        solo.enterText(0, "myhair");

        solo.clickOnActionBarItem(R.id.save);
        solo.sleep(4000);
        assertTrue(solo.searchText("myhair"));
    }

    public void tearDown() throws Exception {
        try {
            solo.finishOpenedActivities();
            solo.sleep(2000);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        super.tearDown();
        solo = null;
    }

}
