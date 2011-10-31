package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import org.liberty.android.fantastischmemo.domain.Card;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.support.ConnectionSource;

public class CardDaoImpl extends BaseDaoImpl<Card, Integer> {
    public CardDaoImpl(ConnectionSource connectionSource)
        throws SQLException {
        super(connectionSource, Card.class);
    }

}

