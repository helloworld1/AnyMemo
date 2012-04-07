package org.liberty.android.fantastischmemo.test.ui;

import org.apache.mycommons.lang3.StringUtils;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.SettingDao;

import org.liberty.android.fantastischmemo.domain.Setting;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;

public class SettingsScreenActivityTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public SettingsScreenActivityTest() {
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
        solo.clickOnText(solo.getString(R.string.settings_menu_text));
        solo.waitForActivity("SettingsScreen");
        solo.sleep(2000);
    }

    public void testSaveFontSize() throws Exception {
        solo.clickOnText("24", 1);
        solo.clickOnText("48");

        solo.clickOnText("24", 1);
        solo.clickOnText("72");
        
        assertTrue(solo.searchText("48"));
        assertTrue(solo.searchText("72"));

        solo.clickOnText(solo.getString(R.string.settings_save));
        solo.sleep(3000);

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

        solo.clickOnText(solo.getString(R.string.settings_save));
        solo.sleep(3000);

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
        solo.clickOnText(solo.getString(R.string.settings_save));
        solo.sleep(3000);
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
        solo.clickOnText(solo.getString(R.string.settings_save));
        solo.sleep(3000);
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
        solo.clickOnText("US");
        solo.clickOnText("DE");

        // Set Answer audio
        solo.clickOnText("FR");
        solo.clickOnText("IT");
        
        solo.clickOnText(solo.getString(R.string.settings_save));

        solo.sleep(3000);
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

    public void testUserAudio() throws Exception {
        // Set Question audio

        solo.clickOnText("US");
        // First scroll up
        for (int i = 0; i < 6; i++) {
        	solo.sendKey(Solo.UP);
        }
        
        solo.clickOnText(solo.getString(R.string.user_audio_text));

        // Set answer audio
        solo.clickOnText("FR");
        for (int i = 0; i < 6; i++) {
        	solo.sendKey(Solo.UP);
        }
        
        solo.clickOnText(solo.getString(R.string.user_audio_text));
        solo.clickOnText(solo.getString(R.string.settings_save));
        solo.sleep(3000);
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, UITestHelper.SAMPLE_DB_PATH);
        try {
            SettingDao settingDao = helper.getSettingDao();
            Setting setting = settingDao.queryForId(1);
            assertTrue(StringUtils.isNotEmpty(setting.getQuestionAudioLocation()));
            assertTrue(StringUtils.isNotEmpty(setting.getAnswerAudioLocation()));

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
