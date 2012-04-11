package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

public class ListEditScreenActivityTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public ListEditScreenActivityTest() {
        super("org.liberty.android.fantastischmemo", AnyMemo.class);
    }

    private Solo solo;

    public void setUp() throws Exception {
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
        solo.clickOnText(solo.getString(R.string.list_mode_text));
        solo.waitForActivity("ListEditScreen");
        solo.sleep(3000);
    }

    public void testListCards() throws Exception {
        assertTrue(solo.searchText("head"));
        assertTrue(solo.searchText("arm"));
        assertTrue(solo.searchText("toe"));
    }

    public void testGoToCardEditor() throws Exception {
        solo.clickOnText("tooth");
        solo.clickOnText(solo.getString(R.string.edit_text));
        solo.waitForActivity("CardEditor");
        solo.sleep(2000);
        assertTrue(solo.searchText("tooth"));
    }
    
    public void testGoToPrevEditor() throws Exception {
        solo.clickOnText("tooth");
        solo.clickOnText(solo.getString(R.string.edit_button_text));
        solo.waitForActivity("EditScreen");
        solo.sleep(2000);
        assertTrue(solo.searchText("tooth"));
    }

    public void testGoToDetailScreen() throws Exception {
        solo.clickOnText("tooth");
        solo.clickOnText(solo.getString(R.string.detail_menu_text));
        solo.waitForActivity("DetailScreen");
        solo.sleep(2000);
        assertTrue(solo.searchText("tooth"));
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
