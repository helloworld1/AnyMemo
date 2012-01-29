package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import java.util.Calendar;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;

import org.liberty.android.fantastischmemo.domain.LearningData;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.table.DatabaseTableConfig;

public class LearningDataDaoImpl extends BaseDaoImpl<LearningData, Integer>
    implements LearningDataDao {

    private AnyMemoDBOpenHelper helper;
    public LearningDataDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<LearningData> tableConfig)
        throws SQLException {
        super(connectionSource, LearningData.class);
    }

    public LearningDataDaoImpl(ConnectionSource connectionSource, Class<LearningData> clazz)
        throws SQLException {
        super(connectionSource, clazz);
    }

    public void setHelper(AnyMemoDBOpenHelper helper) {
        if (this.helper == null) {
            this.helper = helper;
        } else {
            throw new RuntimeException("Set the helper for DAO twice!");
        }
    }

    public long getTotalCount() {
        QueryBuilder<LearningData, Integer> qb = queryBuilder();
        qb.setCountOf(true);
        qb.selectColumns("id");
        try {
            PreparedQuery<LearningData> pq = qb.prepare();
            return countOf(pq);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public long getNewCardCount() {
        QueryBuilder<LearningData, Integer> qb = queryBuilder();
        qb.setCountOf(true);
        qb.selectColumns("id");
        try {
            PreparedQuery<LearningData> pq = qb.where().eq("acqReps", "0").prepare();
            return countOf(pq);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public long getScheduledCardCount() {
        QueryBuilder<LearningData, Integer> qb = queryBuilder();
        qb.setCountOf(true);
        qb.selectColumns("id");
        try {
            PreparedQuery<LearningData> pq =
                qb.where().le("nextLearnDate", Calendar.getInstance().getTime())
                .and().gt("acqReps", "0").prepare();
            return countOf(pq);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}

