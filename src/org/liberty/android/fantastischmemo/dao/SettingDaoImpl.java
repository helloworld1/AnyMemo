package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.Setting;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.table.DatabaseTableConfig;

public class SettingDaoImpl extends BaseDaoImpl<Setting, Integer> implements SettingDao {
    public SettingDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<Setting> config)
        throws SQLException {
        super(connectionSource, config);
    }
    public SettingDaoImpl(ConnectionSource connectionSource, Class<Setting> clazz)
        throws SQLException {
        super(connectionSource, clazz);
    }

}

