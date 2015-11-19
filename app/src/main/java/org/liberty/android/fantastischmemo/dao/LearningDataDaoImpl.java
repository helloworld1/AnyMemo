package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.domain.LearningData;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class LearningDataDaoImpl extends AbstractHelperDaoImpl<LearningData, Integer>
    implements LearningDataDao {

    public LearningDataDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<LearningData> tableConfig)
        throws SQLException {
        super(connectionSource, LearningData.class);
    }

    public LearningDataDaoImpl(ConnectionSource connectionSource, Class<LearningData> clazz)
        throws SQLException {
        super(connectionSource, clazz);
    }

    public void updateLearningData(LearningData ld) {
        try {
            int id = ld.getId();
            deleteById(id);
            create(ld);
            updateId(ld, id);
        } catch (SQLException e) {
            throw new RuntimeException("Error replacing settings", e);
        }
    }

    public void resetLearningData(LearningData ld) {
        ld.cloneFromLearningData(new LearningData());
        update(ld);
    }

    public void resetAllLearningData() {
        try {
            callBatchTasks(new Callable<Void>() {
                public Void call() throws Exception {
                    for (LearningData ld : LearningDataDaoImpl.this) {
                        resetLearningData(ld);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error resetting all learning data", e);
        }

    }

    public void markAsLearnedForever(LearningData ld) {
        // 2099-12-31
        ld.setNextLearnDate(new Date(4102358400000L));
        ld.setAcqReps(1);
        ld.setGrade(5);
        update(ld);
    }
}
