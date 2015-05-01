package org.liberty.android.fantastischmemo.dao;

import org.liberty.android.fantastischmemo.domain.Setting;

public interface SettingDao extends HelperDao<Setting, Integer> {
    void replaceSetting(Setting settings);
}
