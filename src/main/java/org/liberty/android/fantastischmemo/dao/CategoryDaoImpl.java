package org.liberty.android.fantastischmemo.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class CategoryDaoImpl extends AbstractHelperDaoImpl<Category, Integer> implements CategoryDao {
    public CategoryDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<Category> tableConfig)
        throws SQLException {
        super(connectionSource, Category.class);
    }

    public CategoryDaoImpl(ConnectionSource connectionSource, Class<Category> clazz)
        throws SQLException {
        super(connectionSource, clazz);
    }

    public Category createOrReturn(String name) {
        try {
            QueryBuilder<Category, Integer> qb = queryBuilder();
            PreparedQuery<Category> pq = qb.where().eq("name", name).prepare();
            Category c = queryForFirst(pq);
            // Do not create a new one if exists
            if (c != null) {
                return c;
            }
            Category nc = new Category();
            nc.setName(name);
            create(nc);
            // Create new one and it should exist
            c = queryForFirst(pq);
            assert c != null : "Category creation failed. The query is still null!";
            return c;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeCategory(Category c) {
        try {
            Integer id = c.getId();
            this.delete(c);
            updateRaw("update cards set category_id = 1 where category_id = ?", id.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void populateCategory(final List<Card> cardList) {
        try {
            //fill category info for each card
            callBatchTasks(new Callable<Void>() {
                public Void call() throws Exception {
                    for(Card c: cardList){
                        refresh(c.getCategory());
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Filling category info for card in cache gets exception!", e);
        }
    }
}

