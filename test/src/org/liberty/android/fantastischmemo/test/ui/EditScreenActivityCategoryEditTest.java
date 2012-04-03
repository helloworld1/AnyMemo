package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

import android.view.KeyEvent;

import android.widget.TabHost;

public class EditScreenActivityCategoryEditTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public EditScreenActivityCategoryEditTest() {
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
        solo.sleep(1000);

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
        // Go to the second card
        solo.clickOnText(solo.getString(R.string.add_screen_next));
        // Goto card editor
        solo.clickOnText(solo.getString(R.string.edit_text));
        // Goto category editor
        solo.clickOnText("French");
    }


    public void testNewCatetory() throws Exception {
        // First enter and create a category
        solo.clearEditText(0);
        solo.enterText(0, "mycategory");
        solo.clickOnText(solo.getString(R.string.new_text));
        assertTrue(solo.searchText("mycategory"));
        solo.clickOnText(solo.getString(R.string.ok_text));
        
        solo.sleep(1000);
        // Test the UI changed to mycategory as the category edit button
        assertTrue(solo.searchText("mycategory"));
        solo.clickOnText(solo.getString(R.string.settings_save));
        solo.sleep(4000);

        // Assert database state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            CardDao cardDao = helper.getCardDao();
            CategoryDao categoryDao = helper.getCategoryDao();
            Card card2 = cardDao.queryForId(2);
            categoryDao.refresh(card2.getCategory());
            
            // One card skipped ...
            assertEquals("mycategory", card2.getCategory().getName());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
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
