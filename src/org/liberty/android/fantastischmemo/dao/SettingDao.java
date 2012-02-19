package org.liberty.android.fantastischmemo.dao;

import org.liberty.android.fantastischmemo.domain.Setting;

import com.j256.ormlite.dao.Dao;

public interface SettingDao extends Dao<Setting, Integer> {
    void replaceSetting(Setting settings);
}
