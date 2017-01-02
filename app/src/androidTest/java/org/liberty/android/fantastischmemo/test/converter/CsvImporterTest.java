package org.liberty.android.fantastischmemo.test.converter;

import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.converter.CSVImporter;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.entity.Card;
import org.liberty.android.fantastischmemo.entity.Category;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class CsvImporterTest extends AbstractConverterTest {

    @Override
    protected Converter getConverter() {
        return new CSVImporter(amFileUtil);
    }

    @Override
    protected String getFileNamePrefix() {
        return "csv-test";
    }

    @Override
    protected void verify(String destFilePath) throws Exception {
        AnyMemoDBOpenHelper helper =
            AnyMemoDBOpenHelperManager.getHelper(getContext(), destFilePath);
        try {
            CardDao cardDao = helper.getCardDao();
            CategoryDao categoryDao = helper.getCategoryDao();
            List<Card> cards = cardDao.queryForAll();
            List<Category> categories = categoryDao.queryForAll();
            for (Card c : cards) {
                categoryDao.refresh(c.getCategory());
            }
            assertEquals(4, cards.size());
            assertEquals(3, categories.size());

            assertEquals("Question1", cards.get(0).getQuestion());
            assertEquals("Answer1", cards.get(0).getAnswer());
            assertEquals("Category1", cards.get(0).getCategory().getName());

            assertEquals("Question2", cards.get(1).getQuestion());
            assertEquals("Answer2", cards.get(1).getAnswer());
            assertEquals("Category1", cards.get(1).getCategory().getName());

            assertEquals("Question3", cards.get(2).getQuestion());
            assertEquals("Answer3", cards.get(2).getAnswer());
            assertEquals("Category2", cards.get(2).getCategory().getName());

            assertEquals("Question4", cards.get(3).getQuestion());
            assertEquals("Answer4", cards.get(3).getAnswer());
            assertEquals("", cards.get(3).getCategory().getName());

        } finally {
            helper.close();
        }
    }
}
