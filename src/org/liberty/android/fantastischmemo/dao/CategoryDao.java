package org.liberty.android.fantastischmemo.dao;

import java.util.List;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import com.j256.ormlite.dao.Dao;

public interface CategoryDao extends Dao<Category, Integer> {
    Category createOrReturn(String name);
    void removeCategory(Category c);

    // Populate category for cards.
    void populateCategory(List<Card> cardList);
}
