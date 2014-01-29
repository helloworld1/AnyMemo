package org.liberty.android.fantastischmemo.test.converter;

import java.util.List;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.converter.Mnemosyne2CardsImporter;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

public class Mnemosyne2CardsImporterTest extends AbstractConverterTest {

    @Override
    protected Converter getConverter() {
        Mnemosyne2CardsImporter importer = new Mnemosyne2CardsImporter();
        return importer;
    }

    @Override
    protected String getFileNamePrefix() {
        return "mnemosyne-2-cards-test";
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
            assertEquals(4, cards.size());
            assertEquals(3, categories.size());

            // LOL, the test cards has a typo
            assertEquals("Qustion1", cards.get(0).getQuestion());
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
            assertEquals("Category2", cards.get(3).getCategory().getName());

        } finally {
            helper.close();
        }
    }

}
