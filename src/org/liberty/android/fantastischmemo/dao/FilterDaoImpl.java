package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.Filter;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.support.ConnectionSource;

public class FilterDaoImpl extends BaseDaoImpl<Filter, Integer> {
    public FilterDaoImpl(ConnectionSource connectionSource)
        throws SQLException {
        super(connectionSource, Filter.class);
    }

}

