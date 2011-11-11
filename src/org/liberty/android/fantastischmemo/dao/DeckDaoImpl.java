package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.Deck;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.table.DatabaseTableConfig;

public class DeckDaoImpl extends BaseDaoImpl<Deck, Integer> {
    public DeckDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<Deck> tableConfig)
        throws SQLException {
        super(connectionSource, Deck.class);
    }

}

