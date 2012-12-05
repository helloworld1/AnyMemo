package org.liberty.android.fantastischmemo.test.ui;

import org.apache.mycommons.lang3.StringUtils;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.ui.SettingsScreen;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

public class SettingsScreenActivityTest extends ActivityInstrumentationTestCase2<SettingsScreen> {

    protected SettingsScreen mActivity;

    public SettingsScreenActivityTest() {
        super("org.liberty.android.fantastischmemo", SettingsScreen.class);
    }

    private Solo solo;

    public void setUp() throws Exception {
        UITestHelper uiTestHelper = new UITestHelper(getInstrumentation());
        uiTestHelper.clearPreferences();
        uiTestHelper.setUpFBPDatabase();

        Intent intent = new Intent();
        intent.putExtra(SettingsScreen.EXTRA_DBPATH, UITestHelper.SAMPLE_DB_PATH);
        setActivityIntent(intent);

        mActivity = this.getActivity();
        solo = new Solo(getInstrumentation(), mActivity);

        solo.waitForDialogToClose(8000);
        solo.sleep(600);
    }

    public void testSaveFontSize() throws Exception {
        solo.clickOnText("24", 1);
        solo.clickOnText("48");

        solo.clickOnText("24", 1);
        solo.clickOnText("72");
        
        assertTrue(solo.searchText("48"));
        assertTrue(solo.searchText("72"));

        solo.sleep(500);
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.save, 0);
        solo.sleep(2000);

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(48, (int)setting.getQuestionFontSize());
            assertEquals(72, (int)setting.getAnswerFontSize());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    public void testSaveAlignments() throws Exception {
        // 1st spinner
        solo.clickOnText(solo.getString(R.string.center_text), 1);
        solo.clickOnText(solo.getString(R.string.left_text));

        // 2nd spinner
        solo.clickOnText(solo.getString(R.string.center_text), 1);
        solo.clickOnText(solo.getString(R.string.right_text));

        solo.sleep(500);
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.save, 1);
        solo.sleep(2000);

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(Setting.Align.LEFT, setting.getQuestionTextAlign());
            assertEquals(Setting.Align.RIGHT, setting.getAnswerTextAlign());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    public void testSaveCardStyle() throws Exception {
        solo.clickOnText(solo.getString(R.string.card_style_single));
        solo.clickOnText(solo.getString(R.string.card_style_double));

        solo.sleep(500);
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.save, 0);
        solo.sleep(2000);
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(Setting.CardStyle.DOUBLE_SIDED, setting.getCardStyle());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    public void testSaveDisplayRatio() throws Exception {
        solo.clickOnText("50%");
        solo.clickOnText("75%");
        solo.sleep(500);
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.save, 0);
        solo.sleep(2000);
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(75, (int)setting.getQaRatio());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    public void testTTSAudioLocale() throws Exception {
        // Set Question audio
        solo.clickOnView(mActivity.findViewById((R.id.question_locale_spinner)));
        solo.clickOnText(solo.getString(R.string.german_text));

        // Set Answer audio
        solo.clickOnView(mActivity.findViewById((R.id.answer_locale_spinner)));
        solo.clickOnText(solo.getString(R.string.italian_text));
        
        solo.sleep(500);
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.save, 0);

        solo.sleep(2000);
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals("DE", setting.getQuestionAudio());
            assertEquals("IT", setting.getAnswerAudio());
            assertTrue(StringUtils.isEmpty(setting.getQuestionAudioLocation()));
            assertTrue(StringUtils.isEmpty(setting.getAnswerAudioLocation()));

        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

    }
    
    public void testDoubleSidedCard() throws Exception {
        // Set Question audio
        solo.clickOnView(mActivity.findViewById((R.id.card_style_spinner)));

        solo.clickOnText(solo.getString(R.string.card_style_double));

        solo.sleep(500);
        getInstrumentation().invokeMenuActionSync(mActivity, R.id.save, 0);
        solo.sleep(2000);

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertEquals(Setting.CardStyle.DOUBLE_SIDED, setting.getCardStyle());

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
