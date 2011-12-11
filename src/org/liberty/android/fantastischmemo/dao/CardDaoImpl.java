package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;

import java.util.List;

import org.liberty.android.fantastischmemo.domain.Card;

import com.j256.ormlite.dao.BaseDaoImpl;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.table.DatabaseTableConfig;

public class CardDaoImpl extends BaseDaoImpl<Card, Integer> implements CardDao {
    public CardDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<Card> config)
        throws SQLException {
        super(connectionSource, config);
    }
}

