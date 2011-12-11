package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.Setting;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.table.DatabaseTableConfig;

public class SettingDaoImpl extends BaseDaoImpl<Setting, Integer> implements SettingDao {
    public SettingDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<Setting> tableConfig)
        throws SQLException {
        super(connectionSource, Setting.class);
    }

}

