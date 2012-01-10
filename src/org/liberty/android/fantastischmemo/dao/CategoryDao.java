package org.liberty.android.fantastischmemo.dao;

import org.liberty.android.fantastischmemo.domain.Category;

import com.j256.ormlite.dao.Dao;

public interface CategoryDao extends Dao<Category, Integer> {
    Category createOrReturn(String name);
    void removeCategory(Category c);
}
