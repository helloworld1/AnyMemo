package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.Deck;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.support.ConnectionSource;

public class DeckDaoImpl extends BaseDaoImpl<Deck, Integer> {
    public DeckDaoImpl(ConnectionSource connectionSource)
        throws SQLException {
        super(connectionSource, Deck.class);
    }

}

