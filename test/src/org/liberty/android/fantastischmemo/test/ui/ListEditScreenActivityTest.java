package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.ui.AnyMemo;
import org.liberty.android.fantastischmemo.ui.ListEditScreen;
import org.liberty.android.fantastischmemo.ui.StudyActivity;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

public class ListEditScreenActivityTest extends ActivityInstrumentationTestCase2<ListEditScreen> {

    protected ListEditScreen mActivity;

    public ListEditScreenActivityTest() {
        super("org.liberty.android.fantastischmemo", ListEditScreen.class);
    }

    private Solo solo;

    public void setUp() throws Exception {
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();
        
        Intent intent = new Intent();
        intent.putExtra(ListEditScreen.EXTRA_DBPATH, UITestHelper.SAMPLE_DB_PATH);
        setActivityIntent(intent);

        mActivity = this.getActivity();

        solo = new Solo(getInstrumentation(), mActivity);
        solo.sleep(2000);
    }
    
    // TODO: Why it doesn't work?
    // public void testListCards() throws Exception {
    //    assertTrue(solo.searchText("head"));
    //    assertTrue(solo.searchText("arm"));
    //    assertTrue(solo.searchText("toe"));
    //}

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
        solo.waitForActivity("PreviewEditActivity");
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
