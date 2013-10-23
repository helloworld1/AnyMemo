package org.liberty.android.fantastischmemo.dao;

import java.util.Date;
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

    /* Swap the Question and answer */
    void swapQA(Card c);
    void swapAllQA();

    /* Swap QA and append them to the end of the db. */
    void swapAllQADup();

    /* Remove the cards with the same question */
    void removeDuplicates();

    /**
     * Get a list of cards for review.
     * @param filterCategory only the cards in this category will be queried.
     * @param exclusion cards in the list will not be queried
     * @param limit the number of card that can be queried at a time
     * @return the queried cards.
     */
    List<Card> getCardsForReview(Category filterCategory, Iterable<Card> exclusion, int limit);

    /**
     * Get a list of new cards.
     * @param filterCategory only the cards in this category will be queried.
     * @param exclusion cards in the list will not be queried
     * @param limit the number of card that can be queried at a time
     * @return the queried cards.
     */
    List<Card> getNewCards(Category filterCategory, Iterable<Card> exclusion, int limit);

    long getTotalCount(Category filterCategory);
    long getNewCardCount(Category filterCategory);
    /* Get the number of cards that is due for now */
    long getScheduledCardCount(Category filterCategory);

    /* get the number cards that scheduled between startDate and endDate */
    long getScheduledCardCount(Category filterCategory, Date startDate, Date endDate);

    /* Number of cards that was graded with "grade" */
    long getNumberOfCardsWithGrade(int grade);

    /* Create a list of cards. Also create the related LearningData and Category */
    void createCards(final List<Card> cardList);

    /* Create one. Also create the related LearningData and Category */
    void createCard(final Card card);

    /* Randomly get cards that is not new */
    List<Card> getRandomReviewedCards(Category filterCategory, int limit);

    /* Get a list of cards by category */
    List<Card> getCardsByCategory(Category filterCategory, boolean random, int limit);

    /* Randonly get a list of cards */
    List<Card> getRandomCards(Category filterCategory, int limit);

    /* Shuffle the ordinal */
    void shuffleOrdinals();

    /* Searching question/answer/note after ordinal */
    Card searchNextCard(String criteria, int ordinal);

    /* Searching question/answer/note before ordinal */
    Card searchPrevCard(String criteria, int ordinal);

    /* return a list of card from the startOrd with size "size". */
    List<Card> getCardsByOrdinalAndSize(long startOrd, long size);

    /** 
     * @param ord the ordinal
     * @return the card with this ordinal.
     */
    Card getByOrdinal(final int ord);

    /** 
     * @param id the id of the card
     * @return the card with the id.
     */
    Card getById(final int id);

    /**
     * Get all cards with the category.
     * @param filterCategory the category to filter. Null for all categories.
     * @return all cards.
     */
    List<Card> getAllCards(Category filterCategory);
}
