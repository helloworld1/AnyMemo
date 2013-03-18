package org.liberty.android.fantastischmemo.test.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;

import android.test.suitebuilder.annotation.SmallTest;

public class CardDaoTest extends AbstractExistingDBTest {

    @SmallTest
    public void testDeleteCardMaintainOrdinal() throws Exception {
        CardDao cardDao = helper.getCardDao();
        Card c13 = cardDao.queryForId(13);
        Card c14 = cardDao.queryForId(14);
        Card c15 = cardDao.queryForId(15);
        assertEquals(13, (int)c13.getOrdinal());
        assertEquals(14, (int)c14.getOrdinal());
        assertEquals(15, (int)c15.getOrdinal());
        cardDao.delete(c14);
        c13 = cardDao.queryForId(13);
        c15 = cardDao.queryForId(15);
        assertEquals(13, (int)c13.getOrdinal());
        assertEquals(14, (int)c15.getOrdinal());
    }

    @SmallTest
    public void testCreateCardMaintainOrdinal() throws Exception {
        CardDao cardDao = helper.getCardDao();
        // Create card has null ordinal, append to the end
        Card nc = new Card();
        assertNull(nc.getOrdinal());
        cardDao.create(nc);
        assertEquals(29, (int)nc.getOrdinal());

        // Create card with an ordinal
        nc = new Card();
        nc.setOrdinal(14);
        cardDao.create(nc);

        Card c13 = cardDao.queryForId(13);
        Card c14 = cardDao.queryForId(14);
        Card c15 = cardDao.queryForId(15);
        assertEquals(13, (int)c13.getOrdinal());
        assertEquals(14, (int)nc.getOrdinal());
        assertEquals(15, (int)c14.getOrdinal());
        assertEquals(16, (int)c15.getOrdinal());
    }

    @SmallTest
    public void testSwapQA() throws Exception {
        CardDao cardDao = helper.getCardDao();
        Card c14 = cardDao.queryForId(14);
        String question = c14.getQuestion();
        String answer = c14.getAnswer();
        cardDao.swapQA(c14);
        c14 = cardDao.queryForId(14);
        assertEquals(answer, c14.getQuestion());
        assertEquals(question, c14.getAnswer());
    }

    @SmallTest
    public void testRemoveDuplicates() throws Exception {
        CardDao cardDao = helper.getCardDao();
        long originalSize = cardDao.countOf();
        Card nc = new Card();
        nc.setQuestion("whatever");
        nc.setAnswer("and whatever");
        cardDao.create(nc);
        cardDao.create(nc);
        cardDao.create(nc);
        cardDao.create(nc);
        List<Card> cards = cardDao.queryForEq("question", "whatever");
        assertEquals(4, cards.size());
        assertEquals(originalSize + 4, cardDao.countOf());
        cardDao.removeDuplicates();
        assertEquals(originalSize + 1, cardDao.countOf());
        cards = cardDao.queryForEq("question", "whatever");
        assertEquals(1, cards.size());
        Card cc = cardDao.queryLastOrdinal();
        assertEquals(29, (int)cc.getOrdinal());
    }

    @SmallTest
    public void testSearchFirstOrdinalWithcategoryIfExists() throws Exception {
        setupThreeCategories();
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();
        List<Category> cts = categoryDao.queryForEq("name", "My category");
        Category ct = cts.get(0);
        Card c = cardDao.queryFirstOrdinal(ct);
        assertEquals(2, (int)c.getId());
    }
    
    @SmallTest
    public void testSearchLastOrdinalWithcategoryIfExists() throws Exception {
        setupThreeCategories();
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();
        List<Category> cts = categoryDao.queryForEq("name", "My category");
        Category ct = cts.get(0);
        Card c = cardDao.queryLastOrdinal(ct);
        assertEquals(8, (int)c.getId());
    }


    @SmallTest
    public void testQueryNextCardWithoutCategory() throws Exception {
        setupThreeCategories();
        CardDao cardDao = helper.getCardDao();
        Card c27 = cardDao.queryForId(27);
        Card c28 = cardDao.queryNextCard(c27, null);
        assertEquals(28, (int)c28.getOrdinal());
        Card c1 = cardDao.queryNextCard(c28, null);
        assertEquals(1, (int)c1.getOrdinal());
    }

    @SmallTest
    public void testQueryPrevCardWithoutCategory() throws Exception {
        setupThreeCategories();
        CardDao cardDao = helper.getCardDao();
        Card c2 = cardDao.queryForId(2);
        Card c1 = cardDao.queryPrevCard(c2, null);
        assertEquals(1, (int)c1.getOrdinal());
        Card c28 = cardDao.queryPrevCard(c1, null);
        assertEquals(28, (int)c28.getOrdinal());
    }

    @SmallTest
    public void testQueryNextCardWithCategory() throws Exception {
        setupThreeCategories();
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();
        List<Category> cts = categoryDao.queryForEq("name", "My category");
        Category ct = cts.get(0);
        Card c2 = cardDao.queryForId(2);
        Card c5 = cardDao.queryNextCard(c2, ct);
        assertEquals(5, (int)c5.getId());
        Card c8 = cardDao.queryForId(8);
        c2 = cardDao.queryNextCard(c8, ct);
        assertEquals(2, (int)c2.getId());
    }

    @SmallTest
    public void testQueryPrevCardWithCategory() throws Exception {
        setupThreeCategories();
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();
        List<Category> cts = categoryDao.queryForEq("name", "My category");
        Category ct = cts.get(0);
        Card c5 = cardDao.queryForId(5);
        Card c2 = cardDao.queryPrevCard(c5, ct);
        assertEquals(2, (int)c2.getId());
        Card c8 = cardDao.queryPrevCard(c2, ct);
        assertEquals(8, (int)c8.getId());
    }

    @SmallTest
    public void testShuffleOrdinals() throws Exception {
        CardDao cardDao = helper.getCardDao();
        cardDao.shuffleOrdinals();
        assertEquals(28, cardDao.countOf());
    }

    @SmallTest
    public void testSwapAllQA() throws Exception {
        CardDao cardDao = helper.getCardDao();
        // Randomly sample 2 cards
        Card c8 = cardDao.queryForId(8);
        Card c18 = cardDao.queryForId(18);
        String question8 = c8.getQuestion();
        String answer8= c8.getAnswer();
        String question18 = c18.getQuestion();
        String answer18= c18.getAnswer();

        cardDao.swapAllQA();
        c8 = cardDao.queryForId(8);
        c18 = cardDao.queryForId(18);
        assertEquals(answer8, c8.getQuestion());
        assertEquals(question8, c8.getAnswer());
        assertEquals(answer18, c18.getQuestion());
        assertEquals(question18, c18.getAnswer());
    }

    @SmallTest
    public void testGetRandomReviewedCards() throws Exception {
        CardDao cardDao = helper.getCardDao();
        List<Card> cards = cardDao.getRandomReviewedCards(null, 10);
        assertEquals(0, cards.size());
    }

    @SmallTest
    public void testCreateCard() throws Exception {
        CardDao cardDao = helper.getCardDao();
        Card c = new Card();
        c.setCategory(new Category());
        c.setLearningData(new LearningData());
        cardDao.createCard(c);
        // Should create a new card
        assertEquals(29, cardDao.countOf());
    }

    @SmallTest
    public void testCreateCards() throws Exception {
        CardDao cardDao = helper.getCardDao();
        Card c = new Card();
        c.setOrdinal(29);
        c.setCategory(new Category());
        c.setLearningData(new LearningData());

        Card c2 = new Card();
        c2.setOrdinal(30);
        c2.setCategory(new Category());
        c2.setLearningData(new LearningData());

        List<Card> cards = new ArrayList<Card>();
        cards.add(c);
        cards.add(c2);

        cardDao.createCards(cards);
        // Should create two new card
        assertEquals(30, cardDao.countOf());
    }

    @SmallTest
    public void testGetNewCardCount() throws Exception {
        CardDao cardDao = helper.getCardDao();
        assertEquals(28L, cardDao.getNewCardCount(null));

        setupThreeCategories();
        CategoryDao categoryDao = helper.getCategoryDao();
        List<Category> cts = categoryDao.queryForEq("name", "My category");
        Category ct = cts.get(0);
        assertEquals(3L, cardDao.getNewCardCount(ct));
    }

    @SmallTest
    public void testGetScheduledCardCount() throws Exception {
        CardDao cardDao = helper.getCardDao();
        assertEquals(0L, cardDao.getScheduledCardCount(null));

        setupThreeCategories();
        CategoryDao categoryDao = helper.getCategoryDao();
        List<Category> cts = categoryDao.queryForEq("name", "My category");
        Category ct = cts.get(0);
        assertEquals(0L, cardDao.getScheduledCardCount(ct));
    }

    @SmallTest
    public void testSearchNextCard() throws Exception {
        CardDao cardDao = helper.getCardDao();
        Card c = cardDao.searchNextCard("mouth", 1);
        assertEquals(8, (int)c.getId());

        c = cardDao.searchNextCard("%oreille%", 10);
        assertEquals(11, (int)c.getId());

        c = cardDao.searchNextCard("whatever", 3);
        assertNull(c);

        c = cardDao.searchNextCard("mouth", 8);
        assertNull(c);
    }

    @SmallTest
    public void testSearchPrevCard() throws Exception {
        CardDao cardDao = helper.getCardDao();
        Card c = cardDao.searchPrevCard("mouth", 10);
        assertEquals(8, (int)c.getId());

        c = cardDao.searchPrevCard("%oreille%", 28);
        assertEquals(11, (int)c.getId());

        c = cardDao.searchPrevCard("whatever", 27);
        assertNull(c);

        c = cardDao.searchPrevCard("mouth", 8);
        assertNull(c);
    }

    @SmallTest
    public void testGetRandomCardsWithoutCategory() throws Exception {
        CardDao cardDao = helper.getCardDao();

        // limit higher than total number of cards
        List<Card> cards = cardDao.getRandomCards(null, 50);
        assertEquals(28, cards.size());

        // limit lower than total number of cards
        List<Card> cards2 = cardDao.getRandomCards(null, 10);
        assertEquals(10, cards2.size());
    }

    @SmallTest
    public void testGetRandomCardsWithCategory() throws Exception {
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();
        setupThreeCategories();
        Category filterCategory1 = categoryDao.createOrReturn("My category");

        // larger than limit
        List<Card> cards1 = cardDao.getRandomCards(filterCategory1, 50);
        assertEquals(3, cards1.size());

        // smaller than limit
        List<Card> cards2 = cardDao.getRandomCards(filterCategory1, 1);
        assertEquals(1, cards2.size());
    }

    @SmallTest
    public void testGetCardsByOrdinalAndSize() throws Exception {
        CardDao cardDao = helper.getCardDao();

        // Card ordianl 1 to 10
        List<Card> cards1 = cardDao.getCardsByOrdinalAndSize(1, 10);
        assertEquals(10, (int) cards1.size());
        assertEquals(1, (int) cards1.get(0).getOrdinal());
        assertEquals(10, (int) cards1.get(9).getOrdinal());

        // Card orgdinal 20 to 28
        List<Card> cards2 = cardDao.getCardsByOrdinalAndSize(20, 10);
        assertEquals(9, (int) cards2.size());
        assertEquals(20,(int) cards2.get(0).getOrdinal());
        assertEquals(28, (int) cards2.get(8).getOrdinal());

        // Get nothing
        List<Card> cards3 = cardDao.getCardsByOrdinalAndSize(31, 10);
        assertEquals(9, (int) cards2.size());
        assertEquals(0, (int) cards3.size());

    }

    @SmallTest
    public void testGetCardsByCategory() throws Exception {
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();

        setupThreeCategories();
        Category filterCategory1 = categoryDao.createOrReturn("My category");

        // If category specified is null, return all cards up to limit
        List<Card> cards1 = cardDao.getCardsByCategory(null, false, 50);
        assertEquals(28, cards1.size());

        // No category specified but with limit
        List<Card> cards2 = cardDao.getCardsByCategory(null, false, 10);
        assertEquals(10, cards2.size());

        // Get by category
        List<Card> cards3 = cardDao.getCardsByCategory(filterCategory1, false, 50);
        assertEquals(3, cards3.size());

        // Get by category with limit
        List<Card> cards4 = cardDao.getCardsByCategory(filterCategory1, false, 2);
        assertEquals(2, cards4.size());

        // Random cards shouldn't affect number of cards to get
        List<Card> cards5 = cardDao.getCardsByCategory(filterCategory1, true, 50);
        assertEquals(3, cards5.size());
    }

    /*
     * Card with "My Category" in ID 2, 5, 8
     */
    private void setupThreeCategories() throws SQLException {
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();
        Card c =cardDao.queryForId(2);
        Category ct = new Category();
        ct.setName("My category");
        categoryDao.create(ct);
        c.setCategory(ct);
        cardDao.update(c);
        c = cardDao.queryForId(5);
        c.setCategory(ct);
        cardDao.update(c);
        c = cardDao.queryForId(8);
        c.setCategory(ct);
        cardDao.update(c);
    }
}

