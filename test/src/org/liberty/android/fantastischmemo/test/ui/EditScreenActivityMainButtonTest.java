package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

import android.view.KeyEvent;

import android.widget.TabHost;

public class EditScreenActivityMainButtonTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public EditScreenActivityMainButtonTest () {
        super("org.liberty.android.fantastischmemo", AnyMemo.class);
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
        solo.sleep(4000);

        // GO to Open tab
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                tabHost.requestFocus();
            }
        });
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        solo.clickOnText(UITestHelper.SAMPLE_DB_NAME);
        solo.clickOnText(solo.getString(R.string.edit_button_text));
        solo.waitForActivity("EditScreen");
        solo.sleep(4000);
    }


    public void testNavigation() throws Exception {
        // 1st card
        assertTrue(solo.searchText("head"));

        // 2nd card
        solo.clickOnText(solo.getString(R.string.add_screen_next));
        assertTrue(solo.searchText("hair"));

        // 3rd card
        solo.clickOnText(solo.getString(R.string.add_screen_next));
        assertTrue(solo.searchText("face"));
        
        // 2nd card
        solo.clickOnText(solo.getString(R.string.add_screen_previous));
        assertTrue(solo.searchText("hair"));

        // 1st card
        solo.clickOnText(solo.getString(R.string.add_screen_previous));
        assertTrue(solo.searchText("head"));

        // last (28th) card
        solo.clickOnText(solo.getString(R.string.add_screen_previous));
        assertTrue(solo.searchText("toe"));

        // 1st card
        solo.clickOnText(solo.getString(R.string.add_screen_next));
        assertTrue(solo.searchText("head"));
    }

    // Test Edit the second card
    public void testEditCard() {
        solo.clickOnText(solo.getString(R.string.add_screen_next));
        solo.clickOnText(solo.getString(R.string.edit_text));
        solo.waitForActivity("CardEditor");
        solo.sleep(4000);

        // Make sure we are editing the 2nd card
        assertTrue(solo.searchText("hair"));
        solo.enterText(0, "myhair");
        solo.clickOnText(solo.getString(R.string.settings_save));
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
    }

}
