package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

import android.view.KeyEvent;

import android.widget.TabHost;

public class AnyMemoActivityOpenTabTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public AnyMemoActivityOpenTabTest() {
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

        // GO to Open tab
        if (solo.searchText("New version")) {
            solo.clickOnText(solo.getString(R.string.ok_text));
        }
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                tabHost.requestFocus();
            }
        });
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
    }

    public void testOpenTabPrevDir() {
        assertTrue(solo.searchText(".."));
        solo.sendKey(Solo.DOWN);
        solo.sendKey(Solo.ENTER);
        assertTrue(solo.searchText(".android_secure"));
        solo.sendKey(Solo.ENTER);
        solo.sendKey(Solo.ENTER);
        // Should be root now
        assertTrue(solo.searchText("data"));
    }

    public void testActionListStudy() {
        solo.clickOnText("french-body-parts.db");
        solo.clickOnText(solo.getString(R.string.study_text));
        solo.waitForActivity("MemoScreen");
        solo.sleep(3000);
    }

    public void testActionListPrevEdit() {
        solo.clickOnText("french-body-parts.db");
        solo.clickOnText(solo.getString(R.string.edit_button_text));
        solo.waitForActivity("EditScreen");
        solo.sleep(3000);
    }

    public void testActionListCardList() {
        solo.clickOnText("french-body-parts.db");
        solo.clickOnText(solo.getString(R.string.list_mode_text));
        solo.waitForActivity("ListEditScreen");
        solo.sleep(3000);
    }

    public void testActionListCram() {
        solo.clickOnText("french-body-parts.db");
        solo.clickOnText(solo.getString(R.string.learn_ahead));
        solo.waitForActivity("MemoScreen");
        solo.sleep(3000);
    }

    public void testActionListSettings() {
        solo.clickOnText("french-body-parts.db");
        solo.clickOnText(solo.getString(R.string.settings_menu_text));
        solo.waitForActivity("SettingsScreen");
    }

    public void testActionListDelete() {
        solo.clickOnText("french-body-parts.db");
        solo.clickOnText(solo.getString(R.string.delete_text));
        solo.clickOnButton(solo.getString(R.string.cancel_text));
        assertTrue(solo.searchText("french-body-parts.db"));
        solo.clickOnText("french-body-parts.db");
        solo.clickOnText(solo.getString(R.string.delete_text));
        solo.clickOnButton(solo.getString(R.string.delete_text));
        assertFalse(solo.searchText("french-body-parts.db"));
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
