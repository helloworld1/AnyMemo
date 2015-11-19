package org.liberty.android.fantastischmemo.dao;

import java.util.List;

import org.liberty.android.fantastischmemo.entity.Card;
import org.liberty.android.fantastischmemo.entity.Category;

public interface CategoryDao extends HelperDao<Category, Integer> {
    Category createOrReturn(String name);
    void removeCategory(Category c);

    // Populate category for cards.
    void populateCategory(List<Card> cardList);
}
