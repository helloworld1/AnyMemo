package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.table.DatabaseTableConfig;

public class CardDaoImpl extends BaseDaoImpl<Card, Integer> implements CardDao {
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
        try {
            QueryBuilder<Card, Integer> qb = queryBuilder();
            PreparedQuery<Card> pq = qb.limit(1L).orderBy("ordinal", true).prepare();
            return queryForFirst(pq);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Get the first card in ordinal.
     */
    public Card queryFirstOrdinal(Category c) {
        try {
            QueryBuilder<Card, Integer> qb = queryBuilder();
            qb.limit(1L).orderBy("ordinal", true);
            PreparedQuery<Card> pq = qb.where().eq("category_id", c.getId()).prepare();
            return queryForFirst(pq);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        Integer currentCardOrdinal = c.getOrdinal();
        Integer nextOridinal = currentCardOrdinal + 1;
        QueryBuilder<Card, Integer> qb = queryBuilder();
        try {
            PreparedQuery<Card> pq = qb.where().eq("ordinal", nextOridinal).prepare();
            Card nc = queryForFirst(pq);
            if (nc == null) {
                nc = queryFirstOrdinal();
            }
            return nc;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Card queryNextCard(final Card c, final Category ct) {
        if (ct == null) {
            return queryNextCard(c);
        }
        try {
            QueryBuilder<Card, Integer> qb = queryBuilder();
            qb.limit(1L).orderBy("ordinal", true);
            PreparedQuery<Card> pq = qb.where()
                .eq("category_id", ct.getId())
                .and().gt("ordinal", c.getOrdinal())
                .prepare();
            Card nc = queryForFirst(pq);
            if (nc == null) {
                nc = queryFirstOrdinal(ct);
            }
            return nc;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Query cylic previous card in ordinal.
     */
    public Card queryPrevCard(final Card c) {
        Integer currentCardOrdinal = c.getOrdinal();
        Integer nextOridinal = currentCardOrdinal - 1;
        QueryBuilder<Card, Integer> qb = queryBuilder();
        try {
            PreparedQuery<Card> pq = qb.where().eq("ordinal", nextOridinal).prepare();
            Card nc = queryForFirst(pq);
            if (nc == null) {
                nc = queryLastOrdinal();
            }
            return nc;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Query cylic previous card in ordinal for a category.
     */
    public Card queryPrevCard(final Card c, final Category ct) {
        if (ct == null) {
            return queryPrevCard(c);
        }
        try {
            QueryBuilder<Card, Integer> qb = queryBuilder();
            qb.limit(1L).orderBy("ordinal", false);
            PreparedQuery<Card> pq = qb.where()
                .eq("category_id", ct.getId())
                .and().lt("ordinal", c.getOrdinal())
                .prepare();
            Card nc = queryForFirst(pq);
            if (nc == null) {
                nc = queryLastOrdinal(ct);
            }
            return nc;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int delete(Card c) {
        try {
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

    public void swapQA(Card c) {
        String answer = c.getAnswer();
        c.setAnswer(c.getQuestion());
        c.setQuestion(answer);
        try {
            update(c);
        } catch (SQLException e) {
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

    private void maintainOrdinal() throws SQLException {
        executeRaw("CREATE TABLE IF NOT EXISTS tmp_count (id INTEGER PRIMARY KEY AUTOINCREMENT, ordinal INTEGER)");
        executeRaw("INSERT INTO tmp_count(ordinal) SELECT ordinal FROM cards;");
        executeRaw("UPDATE cards SET ordinal = (SELECT tmp_count.id FROM tmp_count WHERE tmp_count.ordinal = cards.ordinal)");
        executeRaw("DROP TABLE IF EXISTS tmp_count;");
    }

}

