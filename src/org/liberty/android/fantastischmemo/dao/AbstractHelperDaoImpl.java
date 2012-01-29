package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.table.DatabaseTableConfig;

public abstract class AbstractHelperDaoImpl<E, T> extends BaseDaoImpl<E, T> {
    private AnyMemoDBOpenHelper helper = null;

    protected AbstractHelperDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<E> config)
        throws SQLException {
        super(connectionSource, config);
    }

    protected AbstractHelperDaoImpl(ConnectionSource connectionSource, Class<E> clazz)
        throws SQLException {
        super(connectionSource, clazz);
    }

    protected AnyMemoDBOpenHelper getHelper() {
        if (helper != null) {
            return helper;
        } else {
            throw new IllegalStateException("Must set the helper in order to use");
        }
    }
    public void setHelper(AnyMemoDBOpenHelper helper) {
        if (this.helper == null) {
            this.helper = helper;
        } else {
            throw new RuntimeException("Set the helper for DAO twice!");
        }
    }
}
