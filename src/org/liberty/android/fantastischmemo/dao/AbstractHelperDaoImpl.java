package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;

import roboguice.util.Ln;

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

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    public int refresh(E domain) {
        try {
            return super.refresh(domain);
        } catch (SQLException e) {
            Ln.e(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    public E queryForId(T id) {
        try {
            return super.queryForId(id);
        } catch (SQLException e) {
            Ln.e(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    public List<E> queryForAll() {
        try {
            return super.queryForAll();
        } catch (SQLException e) {
            Ln.e(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    public int delete(E domain) {
        try {
            return super.delete(domain);
        } catch (SQLException e) {
            Ln.e(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    public int update(E domain) {
        try {
            Ln.i("[ " + new Date() + " ] Updating domain obj: "  + domain);
            return super.update(domain);
        } catch (SQLException e) {
            Ln.e(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    public int create(E domain) {
        try {
            return super.create(domain);
        } catch (SQLException e) {
            Ln.e(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    public long countOf() {
        try {
            return super.countOf();
        } catch (SQLException e) {
            Ln.e(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    public <CT> CT callBatchTasks(Callable<CT> ct) {
        try {
            return super.callBatchTasks(ct);
        } catch (SQLException e) {
            Ln.e(e);
            throw new RuntimeException(e);
        }
    }
}
