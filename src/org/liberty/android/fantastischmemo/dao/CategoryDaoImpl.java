package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.Category;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.support.ConnectionSource;

public class CategoryDaoImpl extends BaseDaoImpl<Category, Integer> {
    public CategoryDaoImpl(ConnectionSource connectionSource)
        throws SQLException {
        super(connectionSource, Category.class);
    }

}

