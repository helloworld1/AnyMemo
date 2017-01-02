package org.liberty.android.fantastischmemo.dao;

import org.liberty.android.fantastischmemo.entity.Card;
import org.liberty.android.fantastischmemo.entity.Category;

import java.util.List;

public interface CategoryDao extends HelperDao<Category, Integer> {
    Category createOrReturn(String name);
    void removeCategory(Category c);

    // Populate category for cards.
    void populateCategory(List<Card> cardList);
}
