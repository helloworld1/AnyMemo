package org.liberty.android.fantastischmemo.test.db;

import java.util.List;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;

import android.test.suitebuilder.annotation.SmallTest;

public class CategoryTest extends AbstractExistingDBTest {

    @SmallTest
    public void testAddCategories() throws Exception {
        CategoryDao categoryDao = helper.getCategoryDao();
        List<Category> categories = categoryDao.queryForAll();
        int initSize = categories.size();
        Category c1 = categoryDao.createOrReturn("c1");
        assertNotNull(c1);
        assertEquals(c1.getName(), "c1");
        categories = categoryDao.queryForAll();
        assertEquals(categories.size(), initSize + 1);
        Category c2 = categoryDao.createOrReturn("c1");
        assertEquals(c2.getName(), "c1");
        assertEquals(categories.size(), initSize + 1);
    }

    @SmallTest
    public void testRemoveCategories() throws Exception {
        CardDao cardDao = helper.getCardDao();
        CategoryDao categoryDao = helper.getCategoryDao();
        Category c1 = categoryDao.createOrReturn("c1");
        categoryDao.create(c1);
        Card nc = new Card();
        nc.setCategory(c1);
        cardDao.create(nc);
        categoryDao.refresh(nc.getCategory());
        assertEquals("c1", nc.getCategory().getName());
        categoryDao.removeCategory(c1);
        nc = cardDao.queryForId(nc.getId());
        categoryDao.refresh(nc.getCategory());
        assertEquals("", nc.getCategory().getName());
    }
}
