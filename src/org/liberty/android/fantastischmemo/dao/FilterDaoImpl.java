package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import java.util.List;

import org.liberty.android.fantastischmemo.domain.Filter;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.table.DatabaseTableConfig;

public class FilterDaoImpl extends BaseDaoImpl<Filter, Integer> implements FilterDao {
    public FilterDaoImpl(ConnectionSource connectionSource,  DatabaseTableConfig<Filter> tableConfig)
        throws SQLException {
        super(connectionSource, tableConfig);
    }
    public FilterDaoImpl(ConnectionSource connectionSource,  Class<Filter> clazz)
        throws SQLException {
        super(connectionSource, clazz);
    }

    public Filter getActiveFilter() {
        QueryBuilder<Filter, Integer> qb = this.queryBuilder();
        Where<Filter, Integer>  where = qb.where();
        try {
            where.eq("isActive", "1");
            List<Filter> activeFilters = where.query();
            if (activeFilters.size() > 0) {
                return activeFilters.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}

