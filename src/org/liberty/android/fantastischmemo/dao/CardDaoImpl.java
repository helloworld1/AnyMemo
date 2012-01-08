package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.Card;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

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
            PreparedQuery<Card> pq = qb.where().eq("ordinal", "1").prepare();
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
}

