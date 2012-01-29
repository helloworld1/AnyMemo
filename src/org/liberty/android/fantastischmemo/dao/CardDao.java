package org.liberty.android.fantastischmemo.dao;

import java.util.List;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import com.j256.ormlite.dao.Dao;

public interface CardDao extends HelperDao<Card, Integer> {
    Card queryFirstOrdinal();
    Card queryFirstOrdinal(Category c);
    Card queryLastOrdinal();
    Card queryLastOrdinal(Category c);
    Card queryNextCard(final Card c);
    Card queryNextCard(final Card c, final Category ct);
    Card queryPrevCard(final Card c);
    Card queryPrevCard(final Card c, final Category ct);
    void swapQA(Card c);
    void removeDuplicates();
    List<Card> getCardForReview(Category filterCategory, int maxReviewCacheOrdinal, int limit);
    List<Card> getNewCards(Category filterCategory, int maxNewCacheOrdinal, int limit);
}
