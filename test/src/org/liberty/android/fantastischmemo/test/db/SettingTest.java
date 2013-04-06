package org.liberty.android.fantastischmemo.test.db;

import java.sql.SQLException;
import java.util.EnumSet;

import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;

import android.graphics.Color;
import android.test.suitebuilder.annotation.SmallTest;

public class SettingTest extends AbstractExistingDBTest {

    @SmallTest
    public void testQuestionFontSize() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setQuestionFontSize(50);
        Setting savedSetting = setSetting(setting);
        assertEquals(50, (int)savedSetting.getQuestionFontSize());
    }

    @SmallTest
    public void testAnswerFontSize() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setAnswerFontSize(60);
        Setting savedSetting = setSetting(setting);
        assertEquals(60, (int)savedSetting.getAnswerFontSize());
    }

    @SmallTest
    public void testQuestionTextAlign() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setQuestionTextAlign(Setting.Align.RIGHT);
        Setting savedSetting = setSetting(setting);
        assertEquals(Setting.Align.RIGHT, savedSetting.getQuestionTextAlign());
    }

    @SmallTest
    public void testAnswerTextAlign() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setAnswerTextAlign(Setting.Align.CENTER_LEFT);
        Setting savedSetting = setSetting(setting);
        assertEquals(Setting.Align.CENTER_LEFT, savedSetting.getAnswerTextAlign());
    }
    
    @SmallTest
    public void testCardStyle() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setCardStyle(Setting.CardStyle.DOUBLE_SIDED);
        Setting savedSetting = setSetting(setting);
        assertEquals(Setting.CardStyle.DOUBLE_SIDED, savedSetting.getCardStyle());
    }

    @SmallTest
    public void testQARatio() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setQaRatio(70);
        Setting savedSetting = setSetting(setting);
        assertEquals(70, (int)savedSetting.getQaRatio());
    }

    @SmallTest
    public void testQuestionAudio() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setQuestionAudio("DE");
        Setting savedSetting = setSetting(setting);
        assertEquals("DE", savedSetting.getQuestionAudio());
    }

    @SmallTest
    public void testAnswerAudio() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setAnswerAudio("DE");
        Setting savedSetting = setSetting(setting);
        assertEquals("DE", savedSetting.getAnswerAudio());
    }

    @SmallTest
    public void testQuestionTextColor() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setQuestionTextColor(Color.RED);
        Setting savedSetting = setSetting(setting);
        assertEquals(Color.RED, (int)savedSetting.getQuestionTextColor());
    }

    @SmallTest
    public void testAnswerTextColor() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setAnswerTextColor(Color.RED);
        Setting savedSetting = setSetting(setting);
        assertEquals(Color.RED, (int)savedSetting.getAnswerTextColor());
    }

    @SmallTest
    public void testQuestionBackgroundColor() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setQuestionBackgroundColor(Color.RED);
        Setting savedSetting = setSetting(setting);
        assertEquals(Color.RED, (int)savedSetting.getQuestionBackgroundColor());
    }

    @SmallTest
    public void testAnswerBackgroundColor() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setAnswerBackgroundColor(Color.RED);
        Setting savedSetting = setSetting(setting);
        assertEquals(Color.RED, (int)savedSetting.getAnswerBackgroundColor());
    }

    @SmallTest
    public void testSeparatorColor() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setSeparatorColor(Color.RED);
        Setting savedSetting = setSetting(setting);
        assertEquals(Color.RED, (int)savedSetting.getSeparatorColor());
    }

    @SmallTest
    public void testDisplayInHTML() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setDisplayInHTMLEnum(EnumSet.of(Setting.CardField.NOTE, Setting.CardField.ANSWER));
        Setting savedSetting = setSetting(setting);
        assertEquals(EnumSet.of(Setting.CardField.NOTE, Setting.CardField.ANSWER), savedSetting.getDisplayInHTMLEnum());
    }

    @SmallTest
    public void testQuestionField() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setQuestionFieldEnum(EnumSet.of(Setting.CardField.NOTE, Setting.CardField.ANSWER));
        Setting savedSetting = setSetting(setting);
        assertEquals(EnumSet.of(Setting.CardField.NOTE, Setting.CardField.ANSWER), savedSetting.getQuestionFieldEnum());
    }

    @SmallTest
    public void testAnswerField() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setAnswerFieldEnum(EnumSet.of(Setting.CardField.NOTE, Setting.CardField.ANSWER));
        Setting savedSetting = setSetting(setting);
        assertEquals(EnumSet.of(Setting.CardField.NOTE, Setting.CardField.ANSWER), savedSetting.getAnswerFieldEnum());
    }

    @SmallTest
    public void testQuestionFont() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setQuestionFont("/sdcard/font1.ttf");
        Setting savedSetting = setSetting(setting);
        assertEquals("/sdcard/font1.ttf", savedSetting.getQuestionFont());
    }

    @SmallTest
    public void testAnswerFont() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setAnswerFont("/sdcard/font1.ttf");
        Setting savedSetting = setSetting(setting);
        assertEquals("/sdcard/font1.ttf", savedSetting.getAnswerFont());
    }

    @SmallTest
    public void testQuestionAudioLocation() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setQuestionAudioLocation("/sdcard/");
        Setting savedSetting = setSetting(setting);
        assertEquals("/sdcard/", savedSetting.getQuestionAudioLocation());
    }

    @SmallTest
    public void testAnswerAudioLocation() throws Exception {
        Setting setting = getCurrentSetting();
        setting.setAnswerAudioLocation("/sdcard/");
        Setting savedSetting = setSetting(setting);
        assertEquals("/sdcard/", savedSetting.getAnswerAudioLocation());
    }

    private Setting getCurrentSetting() throws SQLException {
        SettingDao settingDao = helper.getSettingDao();
        return settingDao.queryForId(1);
    }

    // Return the saved setting.
    private Setting setSetting(Setting setting) throws SQLException {
        SettingDao settingDao = helper.getSettingDao();
        settingDao.update(setting);
        return settingDao.queryForId(1);
    }

}
