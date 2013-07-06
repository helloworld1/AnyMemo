package org.liberty.android.fantastischmemo.dao;

import java.util.List;
import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;

import com.j256.ormlite.dao.Dao;

public interface HelperDao<E, T> extends Dao<E, T> {

    /**
     * Set the AnyMemoDBOpenHelper so the inherrited DAO
     * can use any other DAO.
     */
    void setHelper(AnyMemoDBOpenHelper helper);

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    E queryForId(T id);

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    int refresh(E domain);

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    List<E> queryForAll();

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    int delete(E e);

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    int update(E e);

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    int create(E e);

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    long countOf();

    /**
     * Override so it throws RuntimeException instead of SQLException
     */
    @Override
    <CT> CT callBatchTasks(Callable<CT> ct);
}
