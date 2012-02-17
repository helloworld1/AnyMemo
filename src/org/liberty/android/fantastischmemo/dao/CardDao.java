package org.liberty.android.fantastischmemo.dao;

import java.util.List;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

public interface CardDao extends HelperDao<Card, Integer> {
    Card queryFirstOrdinal();
    /* c is the filter category */
    Card queryFirstOrdinal(Category c);
    Card queryLastOrdinal();
    Card queryLastOrdinal(Category c);
    Card queryNextCard(final Card c);
    /* ct is the filter category */
    Card queryNextCard(final Card c, final Category ct);
    Card queryPrevCard(final Card c);
    Card queryPrevCard(final Card c, final Category ct);
    void swapQA(Card c);

    /* Remove the cards with the same question */
    void removeDuplicates();

    List<Card> getCardForReview(Category filterCategory, int maxReviewCacheOrdinal, int limit);
    List<Card> getNewCards(Category filterCategory, int maxNewCacheOrdinal, int limit);

    long getTotalCount(Category filterCategory);
    long getNewCardCount(Category filterCategory);
    long getScheduledCardCount(Category filterCategory);

    /* Create a list of cards. Also create the related LearningData and Category */
    void createCards(final List<Card> cardList);

    /* Create one. Also create the related LearningData and Category */
    void createCard(final Card card);
}
