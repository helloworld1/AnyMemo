package org.liberty.android.fantastischmemo.dao;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;

import com.j256.ormlite.dao.Dao;

public interface HelperDao<E, T> extends Dao<E, T> {
    void setHelper(AnyMemoDBOpenHelper helper);
}
