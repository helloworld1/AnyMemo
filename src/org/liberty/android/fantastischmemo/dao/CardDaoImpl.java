package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.aspect.LogInvocation;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;

import com.google.common.base.Strings;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class CardDaoImpl extends AbstractHelperDaoImpl<Card, Integer> implements CardDao {
    public CardDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<Card> config)
        throws SQLException {
        super(connectionSource, config);
    }

    public CardDaoImpl(ConnectionSource connectionSource, Class<Card> clazz)
        throws SQLException {
        super(connectionSource, clazz);
    }

    /*
     * Get the first card in ordinal.
     */
    public Card queryFirstOrdinal() {
        return queryFirstOrdinal(null);
    }

    /*
     * Get the first card in ordinal.
     */
    public Card queryFirstOrdinal(final Category c) {
        return callBatchTasks(new Callable<Card>() {
            public Card call() throws Exception {
                LearningDataDao learningDataDao = getHelper().getLearningDataDao();
                CategoryDao categoryDao = getHelper().getCategoryDao();

                QueryBuilder<Card, Integer> qb = queryBuilder();
                qb.limit(1L).orderBy("ordinal", true);
                if (c != null) {
                    qb.setWhere(qb.where().eq("category_id", c.getId()));
                }
                PreparedQuery<Card> pq = qb.prepare();

                Card card = queryForFirst(pq);
                if (card == null) {
                    return null;
                }
                learningDataDao.refresh(card.getLearningData());
                categoryDao.refresh(card.getCategory());
                return card;
            }
        });

    }

    /*
     * Get the last card in ordinal.
     */
    public Card queryLastOrdinal() {
        try {
            QueryBuilder<Card, Integer> qb = queryBuilder();
            PreparedQuery<Card> pq = qb.limit(1L).orderBy("ordinal", false).prepare();

            return queryForFirst(pq);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Get the first card in ordinal.
     */
    public Card queryLastOrdinal(Category c) {
        if (c == null) {
            return queryLastOrdinal();
        }
        try {
            QueryBuilder<Card, Integer> qb = queryBuilder();
            qb.limit(1L).orderBy("ordinal", false);
            PreparedQuery<Card> pq = qb.where().eq("category_id", c.getId()).prepare();
            return queryForFirst(pq);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Query cylic next card in ordinal.
     */
    public Card queryNextCard(final Card c) {
        return queryNextCard(c, null);
    }

    public Card queryNextCard(final Card c, final Category ct) {
        return callBatchTasks(new Callable<Card>() {
            public Card call() throws Exception {
                LearningDataDao learningDataDao = getHelper().getLearningDataDao();
                CategoryDao categoryDao = getHelper().getCategoryDao();

                QueryBuilder<Card, Integer> qb = queryBuilder();
                qb.limit(1L).orderBy("ordinal", true);
                PreparedQuery<Card> pq;
                if (ct != null ) {
                    pq = qb.where()
                        .eq("category_id", ct.getId())
                        .and().gt("ordinal", c.getOrdinal())
                        .prepare();
                } else {
                    pq = qb.where()
                        .gt("ordinal", c.getOrdinal())
                        .prepare();
                }
                Card nc = queryForFirst(pq);
                if (nc == null) {
                    nc = queryFirstOrdinal(ct);
                }

                if (nc == null) {
                    return null;
                }
                learningDataDao.refresh(nc.getLearningData());
                categoryDao.refresh(nc.getCategory());
                return nc;
            }
        });
    }

    /*
     * Query cylic previous card in ordinal.
     */
    public Card queryPrevCard(final Card c) {
        return queryPrevCard(c, null);
    }

    /*
     * Query cylic previous card in ordinal for a category.
     */
    public Card queryPrevCard(final Card c, final Category ct) {
        return callBatchTasks(new Callable<Card>() {
            public Card call() throws Exception {
                LearningDataDao learningDataDao = getHelper().getLearningDataDao();
                CategoryDao categoryDao = getHelper().getCategoryDao();

                QueryBuilder<Card, Integer> qb = queryBuilder();
                qb.limit(1L).orderBy("ordinal", false);
                PreparedQuery<Card> pq;
                if (ct != null) {
                    pq = qb.where()
                        .eq("category_id", ct.getId())
                        .and().lt("ordinal", c.getOrdinal())
                        .prepare();
                } else {
                    pq = qb.where()
                        .lt("ordinal", c.getOrdinal())
                        .prepare();
                }

                Card nc = queryForFirst(pq);
                if (nc == null) {
                    nc = queryLastOrdinal(ct);
                }
                learningDataDao.refresh(nc.getLearningData());
                categoryDao.refresh(nc.getCategory());
                return nc;
            }
        });
    }

    @Override
    public int delete(Card c) {
        try {
            // First cascade delete the learning data
            LearningDataDao learningDataDao = getHelper().getLearningDataDao();
            learningDataDao.refresh(c.getLearningData());
            learningDataDao.delete(c.getLearningData());

            Integer cardOrdinal = c.getOrdinal();
            int res = super.delete(c);
            // If we delete a card every larger ordinal should -1.
            UpdateBuilder<Card, Integer> updateBuilder = updateBuilder();
            updateBuilder.updateColumnExpression("ordinal", "ordinal - 1");
            updateBuilder.where().gt("ordinal", cardOrdinal).prepare();
            update(updateBuilder.prepare());
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int create(Card c) {
        try {
            Integer cardOrdinal = c.getOrdinal();
            // Null ordinal means we need to put the max oridinal + 1 here
            if (cardOrdinal == null) {
                Card last = queryLastOrdinal();
                // If it is a new db the last oridinal will be null.
                if (last == null) {
                    cardOrdinal = 1;
                } else {
                    cardOrdinal = last.getOrdinal() + 1;
                }
                c.setOrdinal(cardOrdinal);
            } else {
                //  We are adding the card at the middle. Should update other card's ordinal.
                UpdateBuilder<Card, Integer> updateBuilder = updateBuilder();
                updateBuilder.updateColumnExpression("ordinal", "ordinal + 1");
                updateBuilder.where().ge("ordinal", cardOrdinal).prepare();
                update(updateBuilder.prepare());
            }
            int res = super.create(c);
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Override the queryForAll so the return list is ordered by ordinal
     * instead of ID.
     */
    @Override
    public List<Card> queryForAll() {
        try {
            QueryBuilder<Card, Integer> cardQb = this.queryBuilder();
            return cardQb.orderBy("ordinal", true).query();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void swapQA(Card c) {
        String answer = c.getAnswer();
        c.setAnswer(c.getQuestion());
        c.setQuestion(answer);
        update(c);
    }

    @LogInvocation
    public List<Card> getCardsForReview(Category filterCategory, Iterable<Card> exclusion, int limit) {
        try {
            LearningDataDao learningDataDao = getHelper().getLearningDataDao();
            CategoryDao categoryDao = getHelper().getCategoryDao();
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            learnQb.selectColumns("id");
            learnQb.where().le("nextLearnDate", Calendar.getInstance().getTime())
                .and().gt("acqReps", "0");
            QueryBuilder<Card, Integer> cardQb = this.queryBuilder();

            // The "isNotNull" statement is dummy so the "and()" can be cascaded
            // for the following conditions
            Where<Card, Integer> where = cardQb.where().isNotNull("learningData_id");

            if (filterCategory != null) {
                where.and().eq("category_id", filterCategory.getId());
            }

            if (exclusion != null) {
                List<Integer> exclusionList = new ArrayList<Integer>();
                for (Card c : exclusion) {
                    exclusionList.add(c.getId());
                }
                where.and().notIn("id", exclusionList);
            }
            cardQb.setWhere(where);

            // Order by easiness so the hard cards (smaller easiness) will be reviewed first.
            cardQb.join(learnQb)
                .orderByRaw("learning_data.easiness, cards.ordinal")
                .limit((long) limit);

            List<Card> cs = cardQb.query();
            for (Card c : cs) {
                categoryDao.refresh(c.getCategory());
                learningDataDao.refresh(c.getLearningData());
            }
            return cs;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @LogInvocation
    public List<Card> getNewCards(Category filterCategory, Iterable<Card> exclusion, int limit) {
        try {
            LearningDataDao learningDataDao = getHelper().getLearningDataDao();
            CategoryDao categoryDao = getHelper().getCategoryDao();
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            learnQb.selectColumns("id");
            learnQb.where().eq("acqReps", "0");
            QueryBuilder<Card, Integer> cardQb = this.queryBuilder();

            // The "isNotNull" statement is dummy so the "and()" can be cascaded
            // for the following conditions
            Where<Card, Integer> where = cardQb.where().isNotNull("learningData_id");

            if (filterCategory != null) {
                where.and().eq("category_id", filterCategory.getId());
            }

            if (exclusion != null) {
                List<Integer> exclusionList = new ArrayList<Integer>();
                for (Card c : exclusion) {
                    exclusionList.add(c.getId());
                }
                where.and().notIn("id", exclusionList);
            }

            cardQb.setWhere(where);
            cardQb.join(learnQb)
                .orderByRaw("cards.ordinal") 
                .limit((long)limit);

            List<Card> cs = cardQb.query();
            for (Card c : cs) {
                categoryDao.refresh(c.getCategory());
                learningDataDao.refresh(c.getLearningData());
            }
            return cs;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /*
     * Remove the duplicate card with the same question.
     */
    public void removeDuplicates() {
        try {
            executeRaw("DELETE FROM cards WHERE id NOT IN (SELECT MIN(id) FROM cards GROUP BY question)");
            executeRaw("DELETE FROM learning_data WHERE id NOT IN (SELECT learningData_id FROM cards)");
            maintainOrdinal();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long getTotalCount(Category filterCategory) {
        QueryBuilder<Card, Integer> qb = queryBuilder();
        qb.setCountOf(true);
        qb.selectColumns("id");
        try {
            PreparedQuery<Card> pq = qb.prepare();
            Where<Card, Integer> where = qb.where();
            if (filterCategory != null) {
                where.eq("category_id", filterCategory.getId());
                qb.setWhere(where);
            }
            return countOf(pq);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public long getNewCardCount(Category filterCategory) {
        try {
            LearningDataDao learningDataDao = getHelper().getLearningDataDao();
            QueryBuilder<Card, Integer> cardQb = queryBuilder();
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            cardQb.setCountOf(true);
            cardQb.selectColumns("id");
            learnQb.selectColumns("id");

            learnQb.where().eq("acqReps", "0");

            Where<Card, Integer> where = cardQb.where().in("learningData_id", learnQb);
            if (filterCategory != null) {
                where.and().eq("category_id", filterCategory.getId());
            }
            cardQb.setWhere(where);

            return countOf(cardQb.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public long getScheduledCardCount(Category filterCategory) {
        // That is the number of cards that is scheduled before now
        return getScheduledCardCount(filterCategory, new Date(0), new Date());
    }

    public long getScheduledCardCount(Category filterCategory, Date startDate, Date endDate) {
        try {
            LearningDataDao learningDataDao = getHelper().getLearningDataDao();
            QueryBuilder<Card, Integer> cardQb = queryBuilder();
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            cardQb.setCountOf(true);
            cardQb.selectColumns("id");
            learnQb.selectColumns("id");

            learnQb.where().le("nextLearnDate", endDate)
                .and().ge("nextLearnDate", startDate)
                .and().gt("acqReps", "0").prepare();

            Where<Card, Integer> where = cardQb.where().in("learningData_id", learnQb);
            if (filterCategory != null) {
                where.and().eq("category_id", filterCategory.getId());
            }
            cardQb.setWhere(where);

            return countOf(cardQb.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public long getNumberOfCardsWithGrade(int grade) {
        try {
            LearningDataDao learningDataDao = getHelper().getLearningDataDao();
            QueryBuilder<Card, Integer> cardQb = queryBuilder();
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            cardQb.setCountOf(true);
            cardQb.selectColumns("id");
            learnQb.selectColumns("id");

            learnQb.where().eq("grade", grade)
                .and().gt("acqReps", "0").prepare();

            Where<Card, Integer> where = cardQb.where().in("learningData_id", learnQb);
            cardQb.setWhere(where);

            return countOf(cardQb.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /*
     * Note the category and learningData field must be populated
     */
    public void createCards(final List<Card> cardList) {
        try {
            final LearningDataDao learningDataDao = getHelper().getLearningDataDao();
            final CategoryDao categoryDao = getHelper().getCategoryDao();
            callBatchTasks(new Callable<Void>() {
                // Use the map to get rid of duplicate category creation
                final Map<String, Category> categoryMap = new HashMap<String, Category>();

                public Void call() throws Exception {
                    List<Category> existingCategories = categoryDao.queryForAll();

                    for (Category c : existingCategories) {
                        assert c != null : "Null category in db";
                        if (c != null) {
                            categoryMap.put(c.getName(), c);
                        }
                    }

                    // Set the oridinal to be right after the last ordinal
                    // The "createCard" method can also set the correct ordinal
                    // however it query the last ordinal each time it is called.
                    // So we set the ordinal here for performance optimization.
                    Card lastCard = queryLastOrdinal();
                    int startOrdinal = 1;
                    // If it is a new db the last oridinal will be null.
                    if (lastCard != null) {
                        startOrdinal = lastCard.getOrdinal() + 1;
                    }

                    for (int i = 0; i < cardList.size(); i++) {
                        Card card = cardList.get(i);
                        assert card.getCategory() != null : "Card's category must be populated";
                        assert card.getLearningData() != null : "Card's learningData must be populated";
                        String currentCategoryName = card.getCategory().getName();
                        if (categoryMap.containsKey(currentCategoryName)) {
                            card.setCategory(categoryMap.get(currentCategoryName));

                        // Becuase the empty category is created by default
                        // We do not
                        } else if (!Strings.isNullOrEmpty(currentCategoryName)) {
                            categoryDao.create(card.getCategory());
                            categoryMap.put(currentCategoryName, card.getCategory());
                        }
                        learningDataDao.create(card.getLearningData());

                        card.setOrdinal(startOrdinal + i);

                        create(card);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * This method will also create the corresponding learning data and cateogry
     * Note the category and learningData field must be populated
     */
    public void createCard(final Card card) {
        try {
            final LearningDataDao learningDataDao = getHelper().getLearningDataDao();
            final CategoryDao categoryDao = getHelper().getCategoryDao();

            // Use the map to get rid of duplicate category creation

            callBatchTasks(new Callable<Void>() {
                final Map<String, Category> categoryMap = new HashMap<String, Category>();
                public Void call() throws Exception {
                    assert card.getCategory() != null : "Card's category must be populated";
                    assert card.getLearningData() != null : "Card's learningData must be populated";

                    // Populate the existing categories.
                    List<Category> existingCategories = categoryDao.queryForAll();

                    for (Category c : existingCategories) {
                        assert c != null : "Null category in db";
                        if (c != null) {
                            categoryMap.put(c.getName(), c);
                        }
                    }

                    String currentCategoryName = card.getCategory().getName();
                    if (categoryMap.containsKey(currentCategoryName)) {
                        card.setCategory(categoryMap.get(currentCategoryName));
                    } else {
                        categoryDao.create(card.getCategory());
                        categoryMap.put(currentCategoryName, card.getCategory());
                    }
                    learningDataDao.create(card.getLearningData());
                    create(card);
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Card> getRandomReviewedCards(Category filterCategory, int limit) {
        try {
            LearningDataDao learningDataDao = getHelper().getLearningDataDao();
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            learnQb.selectColumns("id");
            learnQb.where().gt("acqReps", "0");
            QueryBuilder<Card, Integer> cardQb = this.queryBuilder();
            Where<Card, Integer> where = cardQb.where().in("learningData_id", learnQb);
            if (filterCategory != null) {
                where.and().eq("category_id", filterCategory.getId());
            }

            cardQb.setWhere(where);
            // Return random ordered cards
            cardQb.orderByRaw("random()");
            cardQb.limit((long)limit);
            List<Card> cs = cardQb.query();
            for (Card c : cs) {
                learningDataDao.refresh(c.getLearningData());
            }
            return cs;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<Card> getCardsByCategory(Category filterCategory, boolean random, int limit) {
        try {
            LearningDataDao learningDataDao = getHelper().getLearningDataDao();
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            learnQb.selectColumns("id");
            QueryBuilder<Card, Integer> cardQb = this.queryBuilder();
            Where<Card, Integer> where = cardQb.where().in("learningData_id", learnQb);
            if (filterCategory != null) {
                where.and().eq("category_id", filterCategory.getId());
            }

            cardQb.setWhere(where);
            // Return random ordered cards
            if (random) {
                cardQb.orderByRaw("random()");
            }
            cardQb.limit((long)limit);
            List<Card> cs = cardQb.query();
            for (Card c : cs) {
                learningDataDao.refresh(c.getLearningData());
            }
            return cs;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void swapAllQA() {
        try {
            callBatchTasks(new Callable<Void>() {
                public Void call() throws Exception {
                    for (Card c : CardDaoImpl.this) {
                        swapQA(c);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error swapping QA of all cards.", e);
        }
    }

    public void swapAllQADup() {
        try {

            callBatchTasks(new Callable<Void>() {
                public Void call() throws Exception {
                    final CategoryDao categoryDao = getHelper().getCategoryDao();
                    final List<Card> cards = queryForAll();

                    int size = cards.size();
                    for (int i = 0; i < size; i++) {
                        Card c = cards.get(i);
                        categoryDao.refresh(c.getCategory());
                        String q = c.getQuestion();
                        c.setQuestion(c.getAnswer());
                        c.setAnswer(q);
                        c.setOrdinal(size + i + 1);
                        c.setLearningData(new LearningData());
                    }
                    createCards(cards);
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error swapping QA of all cards.", e);
        }
    }

    public void shuffleOrdinals() {
        final List<Card> cards = queryForAll();
        Collections.shuffle(cards);
        try {
            callBatchTasks(new Callable<Void>() {
                public Void call() throws Exception {
                    int counter = 0;
                    for (Card c : CardDaoImpl.this) {
                        c.setOrdinal(cards.get(counter).getOrdinal());
                        update(c);
                        counter++;
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error shuffling cards.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Card searchNextCard(String criteria, int ordinal) {
        QueryBuilder<Card, Integer> qb = queryBuilder();
        try {
            Where<Card, Integer> where = qb.where();
            where.and(where.gt("ordinal", ordinal),
                where.or(where.like("question", criteria), where.like("answer", criteria), where.like("note", criteria)));
            qb.setWhere(where);
            qb.orderBy("ordinal", true);
            PreparedQuery<Card> pq = qb.prepare();
            Card nc = queryForFirst(pq);
            return nc;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Card searchPrevCard(String criteria, int ordinal) {
        QueryBuilder<Card, Integer> qb = queryBuilder();
        try {
            Where<Card, Integer> where = qb.where();
            where.and(where.lt("ordinal", ordinal),
                where.or(where.like("question", criteria), where.like("answer", criteria), where.like("note", criteria)));
            qb.setWhere(where);
            qb.orderBy("ordinal", false);
            PreparedQuery<Card> pq = qb.prepare();
            Card nc = queryForFirst(pq);
            return nc;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Card> getCardsByOrdinalAndSize(long startOrd, long size) {
        LearningDataDao learningDataDao = getHelper().getLearningDataDao();
        QueryBuilder<Card, Integer> qb = queryBuilder();
        qb.limit(size);
        try {
            Where<Card, Integer> where = qb.where().ge("ordinal", startOrd);
            qb.setWhere(where);
            qb.orderBy("ordinal", true);
            PreparedQuery<Card> pq = qb.prepare();
            List<Card> result = query(pq);
            for (Card c : result) {
                learningDataDao.refresh(c.getLearningData());
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get card by id.
     * @return card with all foreign fields refreshed.
     */
    public Card getById(final int id) {
        return callBatchTasks(new Callable<Card>() {
            public Card call() {
                LearningDataDao learningDataDao = getHelper().getLearningDataDao();
                CategoryDao categoryDao = getHelper().getCategoryDao();
                Card card = queryForId(id);
                if (card == null) {
                    return null;
                }
                learningDataDao.refresh(card.getLearningData());
                categoryDao.refresh(card.getCategory());
                return card;
            }
        });
    }

    public List<Card> getAllCards(final Category filterCategory) {
        final LearningDataDao learningDataDao = getHelper().getLearningDataDao();
        final CategoryDao categoryDao = getHelper().getCategoryDao();
        return callBatchTasks(new Callable<List<Card>>() {
            public List<Card> call() throws SQLException {
                QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
                learnQb.selectColumns("id");
                QueryBuilder<Card, Integer> cardQb = queryBuilder();
                Where<Card, Integer> where = cardQb.where().in("learningData_id", learnQb);
                if (filterCategory != null) {
                    where.and().eq("category_id", filterCategory.getId());
                }

                cardQb.setWhere(where);
                // Return random ordered cards
                cardQb.orderBy("ordinal", true);
                List<Card> cs = cardQb.query();
                for (Card c : cs) {
                    learningDataDao.refresh(c.getLearningData());
                    categoryDao.refresh(c.getCategory());
                }
                return cs;
            }
        });
    }

    /**
     * Get card by ord.
     * @return card with all foreign fields refreshed.
     */
    public Card getByOrdinal(final int ord) {
        return callBatchTasks(new Callable<Card>() {
            public Card call() throws SQLException {
                LearningDataDao learningDataDao = getHelper().getLearningDataDao();
                CategoryDao categoryDao = getHelper().getCategoryDao();
                List<Card> cards = queryForEq("ordinal", ord);
                if (cards.size() == 0) {
                    return null;
                }

                assert cards.size() < 2 : "Error: Multiple cards with the same ordinal.";

                Card card = cards.get(0);

                learningDataDao.refresh(card.getLearningData());
                categoryDao.refresh(card.getCategory());
                return card;
            }
        });
    }

    private void maintainOrdinal() throws SQLException {
        executeRaw("CREATE TABLE IF NOT EXISTS tmp_count (id INTEGER PRIMARY KEY AUTOINCREMENT, ordinal INTEGER)");
        executeRaw("INSERT INTO tmp_count(ordinal) SELECT ordinal FROM cards;");
        executeRaw("UPDATE cards SET ordinal = (SELECT tmp_count.id FROM tmp_count WHERE tmp_count.ordinal = cards.ordinal)");
        executeRaw("DROP TABLE IF EXISTS tmp_count;");
    }

    public List<Card> getRandomCards(Category filterCategory, int limit) {
        try {
            LearningDataDao learningDataDao = getHelper().getLearningDataDao();
            QueryBuilder<LearningData, Integer> learnQb = learningDataDao.queryBuilder();
            learnQb.selectColumns("id");
            QueryBuilder<Card, Integer> cardQb = this.queryBuilder();
            Where<Card, Integer> where = cardQb.where().in("learningData_id", learnQb);
            if (filterCategory != null) {
                where.and().eq("category_id", filterCategory.getId());
            }

            cardQb.setWhere(where);
            // Return random ordered cards
            cardQb.orderByRaw("random()");
            cardQb.limit((long)limit);
            List<Card> cs = cardQb.query();
            for (Card c : cs) {
                learningDataDao.refresh(c.getLearningData());
            }
            return cs;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

