package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.AMPrefKeys;
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

    /**
     * {@inheritDoc}
     * @see ActivityInstrumentationTestCase2#setUp()
     */
    public void setUp() throws Exception {
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();
        
        Intent intent = new Intent();
        intent.putExtra(StudyActivity.EXTRA_DBPATH, UITestHelper.SAMPLE_DB_PATH);
        setActivityIntent(intent);

        mActivity = this.getActivity();

        solo = new Solo(getInstrumentation(), mActivity);
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }


    public void testSaveCardWithModification() {
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.menu_context_edit, 0);

        solo.waitForDialogToClose(3000);
        solo.sleep(300);

        solo.clearEditText(0);
        solo.enterText(0, "test");
        solo.sleep(300);

        getInstrumentation().invokeMenuActionSync(solo.getCurrentActivity(), R.id.save, 0);
        
        solo.waitForActivity("StudyActivity");
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
        
        // After saving, expect to see the same card
        assertTrue(solo.searchText("test"));
    }
    
    public void testSaveCardWithShuffle() {
        // Turn on shuffle option
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        Editor edit = settings.edit();
        edit.putBoolean(AMPrefKeys.SHUFFLING_CARDS_KEY, true);
        edit.commit();

        // Now do the edit test
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.menu_context_edit, 0);

        
        solo.waitForDialogToClose(3000);
        solo.sleep(300);

        solo.clearEditText(0);
        solo.enterText(0, "test");

        solo.sleep(300);

        getInstrumentation().invokeMenuActionSync(solo.getCurrentActivity(), R.id.save, 0);
        
        solo.waitForActivity("StudyActivity");
        solo.waitForDialogToClose(8000);
        solo.sleep(600);
        
        // After saving, expect to see the same card
        assertTrue(solo.searchText("test"));
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
    

