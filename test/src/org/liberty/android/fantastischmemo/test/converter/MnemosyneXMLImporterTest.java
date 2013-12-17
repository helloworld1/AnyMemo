package org.liberty.android.fantastischmemo.test.converter;

import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.converter.MnemosyneXMLImporter;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

public class MnemosyneXMLImporterTest extends AbstractConverterTest {

    @Override
    protected Converter getConverter() {
        return new MnemosyneXMLImporter();
    }

    @Override
    protected String getFileNamePrefix() {
        return "mnemosyne-xml-1-test";
    }

    @Override
    protected void verify(String destFilePath) throws Exception {
        AnyMemoDBOpenHelper helper =
            AnyMemoDBOpenHelperManager.getHelper(getContext(), destFilePath);
        try {
            CardDao cardDao = helper.getCardDao();
            CategoryDao categoryDao = helper.getCategoryDao();
            LearningDataDao learningDataDao = helper.getLearningDataDao();
            List<Card> cards = cardDao.queryForAll();
            List<Category> categories = categoryDao.queryForAll();
            for (Card c : cards) {
                categoryDao.refresh(c.getCategory());
                learningDataDao.refresh(c.getLearningData());
            }

            assertEquals(11, cards.size());
            assertEquals(2, categories.size());

            assertEquals("q1", cards.get(0).getQuestion());
            assertEquals("a1", cards.get(0).getAnswer());
            assertEquals("<Standard>", cards.get(0).getCategory().getName());

            assertEquals("q2", cards.get(1).getQuestion());
            assertEquals("a2", cards.get(1).getAnswer());
            assertEquals("<Standard>", cards.get(1).getCategory().getName());

            assertEquals("q3", cards.get(2).getQuestion());
            assertEquals("a3", cards.get(2).getAnswer());
            assertEquals("<Standard>", cards.get(3).getCategory().getName());

            assertNotNull(cards.get(3).getQuestion());
            assertNotNull(cards.get(3).getAnswer());
            assertEquals("<Standard>", cards.get(3).getCategory().getName());

        } finally {
            helper.close();
        }
    }

}
