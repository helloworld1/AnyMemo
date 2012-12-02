package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.ui.AnyMemo;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;

public class StudyActivityEditCardTest extends ActivityInstrumentationTestCase2<AnyMemo> {

	protected AnyMemo mActivity;

    public StudyActivityEditCardTest() {
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


    public void testSaveCardWithoutShuffle() {
    	// Save a card with shuffle turned off
    	solo.clickLongOnText(UITestHelper.SAMPLE_DB_NAME);
    	
        solo.clickOnText(solo.getString(R.string.study_text));
        solo.waitForActivity("StudyActivity");
    	
        solo.sleep(4000);
        
        solo.clickLongOnText("head");
        solo.clickOnText(solo.getString(R.string.edit_text));
        solo.clickOnText(solo.getString(R.string.settings_save));
        
        solo.sleep(4000);
        
    	// After saving, expect to see the same card
        assertTrue(solo.searchText("head"));
    }
	
    public void testSaveCardWithShuffle() {
	    	// Turn on shuffle option
	    	solo.clickOnText(solo.getString(R.string.misc_category));
	    	solo.clickOnText(solo.getString(R.string.option_button_text));
	    	solo.clickOnText(solo.getString(R.string.shuffling_cards_title));
	    	solo.goBack();
	    	solo.clickOnText(solo.getString(R.string.recent_tab_text));
	    	
	    	// Save a card with shuffle turned on
	    	solo.clickLongOnText(UITestHelper.SAMPLE_DB_NAME);
	    	
	        solo.clickOnText(solo.getString(R.string.study_text));
	        solo.waitForActivity("StudyActivity");
	    	
	        solo.sleep(4000);
	        
	        // Since the card is shuffled, we need to get the textview so that we know what is the current card string
	        TextView v = (TextView)solo.getCurrentActivity().findViewById(R.id.question);
	        String oldQuestion = v.getText().toString();
	        
	        solo.clickLongOnText(oldQuestion);
	        solo.clickOnText(solo.getString(R.string.edit_text));
	        solo.clickOnText(solo.getString(R.string.settings_save));
	        
	        solo.sleep(4000);
	    	
	        assertTrue(solo.searchText(oldQuestion));
    	}
}
    

