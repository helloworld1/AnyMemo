package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.LearningData;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.table.DatabaseTableConfig;

public class LearningDataDaoImpl extends BaseDaoImpl<LearningData, Integer>
    implements LearningDataDao {

    public LearningDataDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<LearningData> tableConfig)
        throws SQLException {
        super(connectionSource, LearningData.class);
    }

    public LearningDataDaoImpl(ConnectionSource connectionSource, Class<LearningData> clazz)
        throws SQLException {
        super(connectionSource, clazz);
    }
}

