package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.Stat;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.table.DatabaseTableConfig;

public class StatDaoImpl extends BaseDaoImpl<Stat, Integer> implements StatDao {
    public StatDaoImpl(ConnectionSource connectionSource,  DatabaseTableConfig<Stat> tableConfig)
        throws SQLException {
        super(connectionSource, tableConfig);
    }
    public StatDaoImpl(ConnectionSource connectionSource,  Class<Stat> clazz)
        throws SQLException {
        super(connectionSource, clazz);
    }
}

