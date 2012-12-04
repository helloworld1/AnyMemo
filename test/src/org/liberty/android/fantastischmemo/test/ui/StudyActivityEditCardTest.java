package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.ui.StudyActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

public class StudyActivityEditCardTest extends ActivityInstrumentationTestCase2<StudyActivity> {

	protected StudyActivity mActivity;

    public StudyActivityEditCardTest() {
        super("org.liberty.android.fantastischmemo", StudyActivity.class);
    }

    private Solo solo;

    public void setUp() throws Exception {
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();
        
        Intent intent = new Intent();
        intent.putExtra(StudyActivity.EXTRA_DBPATH, UITestHelper.SAMPLE_DB_PATH);
        setActivityIntent(intent);

        mActivity = this.getActivity();

        solo = new Solo(getInstrumentation(), mActivity);
        solo.sleep(2000);
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


    public void testSaveCardWithModification() {
        solo.waitForActivity("StudyActivity");

        solo.clickOnMenuItem(solo.getString(R.string.edit_text));
        solo.sleep(1000);
        solo.clearEditText(0);
        solo.enterText(0, "test");

        solo.clickOnText(solo.getString(R.string.edit_text));
        solo.clickOnText(solo.getString(R.string.settings_save));
        
        solo.sleep(2000);
        
    	// After saving, expect to see the same card
        assertTrue(solo.searchText("test"));
    }
	
    public void testSaveCardWithShuffle() {
        // Turn on shuffle option
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        Editor edit = settings.edit();
        edit.putBoolean("shuffling_cards", true);
        edit.commit();

        // Now do the edit test
        solo.clickOnMenuItem(solo.getString(R.string.edit_text));
        solo.sleep(500);
        solo.clearEditText(0);
        solo.enterText(0, "test");

        solo.clickOnText(solo.getString(R.string.edit_text));
        solo.clickOnText(solo.getString(R.string.settings_save));
        
        solo.sleep(2000);
        
    	// After saving, expect to see the same card
        assertTrue(solo.searchText("test"));
    }
}
    

