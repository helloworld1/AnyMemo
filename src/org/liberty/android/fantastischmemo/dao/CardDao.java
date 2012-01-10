package org.liberty.android.fantastischmemo.dao;

import org.liberty.android.fantastischmemo.domain.Card;

import com.j256.ormlite.dao.Dao;

public interface CardDao extends Dao<Card, Integer> {
    Card queryFirstOrdinal();
    Card queryLastOrdinal();
    Card queryNextCard(final Card c);
    Card queryPrevCard(final Card c);
}
