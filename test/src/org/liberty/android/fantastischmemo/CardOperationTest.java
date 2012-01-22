package org.liberty.android.fantastischmemo;

import java.sql.SQLException;

import java.util.List;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

public class CardOperationTest extends AbstractExistingDBTest {
    public CardOperationTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

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

    public void testSearchFirstOrdinalWithcategoryIfExists() throws Exception {
        setupThreeCategories();
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();
        List<Category> cts = categoryDao.queryForEq("name", "My category");
        Category ct = cts.get(0);
        Card c = cardDao.queryFirstOrdinal(ct);
        assertEquals(2, (int)c.getId());
    }
    
    public void testSearchLastOrdinalWithcategoryIfExists() throws Exception {
        setupThreeCategories();
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();
        List<Category> cts = categoryDao.queryForEq("name", "My category");
        Category ct = cts.get(0);
        Card c = cardDao.queryLastOrdinal(ct);
        assertEquals(8, (int)c.getId());
    }

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

