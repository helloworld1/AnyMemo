package org.liberty.android.fantastischmemo.dao;

import org.liberty.android.fantastischmemo.entity.Setting;

public interface SettingDao extends HelperDao<Setting, Integer> {
    void replaceSetting(Setting settings);
}
