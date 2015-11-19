package org.liberty.android.fantastischmemo.test.integration;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.test.TestHelper;
import org.liberty.android.fantastischmemo.ui.SettingsScreen;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.google.common.base.Strings;
import com.robotium.solo.Solo;

public class SettingsScreenActivityTest extends ActivityInstrumentationTestCase2<SettingsScreen> {

    protected SettingsScreen mActivity;

    @SuppressWarnings("deprecation")
    public SettingsScreenActivityTest() {
        super("org.liberty.android.fantastischmemo", SettingsScreen.class);
    }

    private Solo solo;

    public void setUp() throws Exception {
        TestHelper uiTestHelper = new TestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();

        Intent intent = new Intent();
        intent.putExtra(SettingsScreen.EXTRA_DBPATH, TestHelper.SAMPLE_DB_PATH);
        setActivityIntent(intent);

        mActivity = this.getActivity();
        solo = new Solo(getInstrumentation(), mActivity);

        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    @LargeTest
    public void testSaveFontSize() throws Exception {
        solo.pressSpinnerItem(0 /* Question font size spinner*/ , 4 /* 48 */);
        solo.sleep(300);
        solo.pressSpinnerItem(2 /* Answer font size spinner*/ , 6 /* 72 */);
        solo.sleep(300);

        assertTrue(solo.searchText("48"));
        assertTrue(solo.searchText("72"));

        solo.sleep(500);
        solo.clickOnActionBarItem(R.id.save);
        solo.sleep(2000);

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(48, (int)setting.getQuestionFontSize());
            assertEquals(72, (int)setting.getAnswerFontSize());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    @LargeTest
    public void testSaveAlignments() throws Exception {
        // 1st spinner
        solo.pressSpinnerItem(1 /* Question text alignment */ , -1  /* Left */);
        solo.sleep(300);
        solo.pressSpinnerItem(3 /* Question text alignment */ , 1  /* Right */);
        solo.sleep(300);

        solo.clickOnActionBarItem(R.id.save);
        solo.sleep(2000);

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(Setting.Align.LEFT, setting.getQuestionTextAlign());
            assertEquals(Setting.Align.RIGHT, setting.getAnswerTextAlign());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    @LargeTest
    public void testSaveCardStyle() throws Exception {
        solo.pressSpinnerItem(4 /* Card style */ , 1  /* Double sided */);

        solo.sleep(500);
        solo.clickOnActionBarItem(R.id.save);
        solo.sleep(2000);
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(Setting.CardStyle.DOUBLE_SIDED, setting.getCardStyle());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    @LargeTest
    public void testSaveDisplayRatio() throws Exception {
        solo.pressSpinnerItem(5 /* Q/A ratio */ , 1  /* 60% */);
        solo.sleep(500);

        solo.clickOnActionBarItem(R.id.save);
        solo.sleep(2000);
        
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(60, (int)setting.getQaRatio());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    @LargeTest
    public void testTTSAudioLocale() throws Exception {
        solo.pressSpinnerItem(6 /* Question audio spinner */, 3 /* German */);
        solo.sleep(300);

        solo.pressSpinnerItem(7 /* Answer audio spinner */, 2 /* Italian */);
        solo.sleep(300);

        solo.clickOnActionBarItem(R.id.save);
        solo.sleep(2000);

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals("DE", setting.getQuestionAudio());
            assertEquals("IT", setting.getAnswerAudio());
            assertTrue(Strings.isNullOrEmpty(setting.getQuestionAudioLocation()));
            assertTrue(Strings.isNullOrEmpty(setting.getAnswerAudioLocation()));

        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

    }

    @LargeTest
    public void testDoubleSidedCard() throws Exception {
        solo.pressSpinnerItem(4 /* Card style */ , 1  /* Double sided */);

        solo.sleep(500);
        solo.clickOnActionBarItem(R.id.save);
        solo.sleep(2000);

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(Setting.CardStyle.DOUBLE_SIDED, setting.getCardStyle());

        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    @LargeTest
    public void testGoBackWithSettingChangedPressYesButtonShouldSaveSettings() throws Exception {
        solo.pressSpinnerItem(5 /* Q/A ratio */ , 1  /* 60% */);
        solo.sleep(500);
        solo.goBack();

        solo.sleep(500);
        solo.clickOnButton(solo.getString(R.string.yes_text));
        solo.sleep(2000);
  
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(60, (int)setting.getQaRatio());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    @LargeTest
    public void testGoBackWithSettingChangedPressNoButtonShouldNotSaveSettings() throws Exception {
        solo.pressSpinnerItem(5 /* Q/A ratio */ , 1  /* 60% */);
        solo.sleep(500);

        solo.goBack();

        solo.sleep(500);
        solo.clickOnButton(solo.getString(R.string.no_text));
        solo.sleep(2000);

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(50, (int)setting.getQaRatio());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }        
    }

    @LargeTest
    public void testGoBackWithSettingChangedPressCancelButtonShouldStayInCurrentActivity() throws Exception {
        solo.pressSpinnerItem(5 /* Q/A ratio */ , 1  /* 60% */);
        solo.sleep(500);

        solo.goBack();
        solo.sleep(500);
        solo.clickOnButton(solo.getString(R.string.cancel_text));
        solo.sleep(2000);
 
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(50, (int)setting.getQaRatio());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

        //Is current activity SettingsScreen
        solo.assertCurrentActivity("Stay", SettingsScreen.class);
    }
    
    @LargeTest
    public void testGoBackWithNoSettingChangedShouldQuit() throws Exception {
        solo.goBack(); 
        solo.sleep(2000);

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, TestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(50, (int)setting.getQaRatio());
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
        solo = null;
    }
}
