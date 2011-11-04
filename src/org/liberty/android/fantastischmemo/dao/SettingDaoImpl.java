package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.Setting;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.support.ConnectionSource;

public class SettingDaoImpl extends BaseDaoImpl<Setting, Integer> {
    public SettingDaoImpl(ConnectionSource connectionSource)
        throws SQLException {
        super(connectionSource, Setting.class);
    }

}

