package org.liberty.android.fantastischmemo.test.integration;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.ui.CardEditor;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;

public class CardEditorActivityFunctionTest extends ActivityInstrumentationTestCase2<CardEditor> {

    protected CardEditor mActivity;

    @SuppressWarnings("deprecation")
    public CardEditorActivityFunctionTest() {
        super("org.liberty.android.fantastischmemo", CardEditor.class);
    }

    private Solo solo;

    public void setUp() throws Exception{
        TestHelper uiTestHelper = new TestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();

        Intent intent = new Intent();
        intent.putExtra(CardEditor.EXTRA_DBPATH, TestHelper.SAMPLE_DB_PATH);
        intent.putExtra(CardEditor.EXTRA_CARD_ID, 2);
        setActivityIntent(intent);

        mActivity = this.getActivity();
        solo = new Solo(getInstrumentation(), mActivity);
        solo.waitForDialogToClose(8000);
        solo.sleep(500);
    }


    @LargeTest
    public void testNewCatetory() throws Exception {
        View categoryButton = mActivity.findViewById(R.id.edit_dialog_category_button);
        solo.clickOnView(categoryButton);
        // First enter and create a category
        solo.waitForText(solo.getString(R.string.category_dialog_title));
        solo.sleep(300);
        solo.clearEditText((EditText) solo.getView(R.id.category_dialog_edit));
        solo.sleep(300);
        solo.enterText((EditText) solo.getView(R.id.category_dialog_edit), "mycategory");
        solo.sleep(300);
        solo.clickOnText(solo.getString(R.string.new_text));
        solo.sleep(300);
        assertTrue(solo.searchText("mycategory"));
        solo.clickOnText(solo.getString(R.string.ok_text));

        solo.sleep(1000);
        // Test the UI changed to mycategory as the category edit button
        assertTrue(solo.searchText("mycategory"));

        getInstrumentation().invokeMenuActionSync(mActivity, R.id.save, 0);
        solo.sleep(3000);

        // Assert database state
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
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
