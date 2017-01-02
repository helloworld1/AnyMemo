package org.liberty.android.fantastischmemo.test.converter;

import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.converter.QATxtImporter;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.entity.Card;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class QATxtImporterTest extends AbstractConverterTest {

    @Override
    protected Converter getConverter() {
        return new QATxtImporter(amFileUtil);
    }

    @Override
    protected String getFileNamePrefix() {
        return "qa-text-test";
    }

    @Override
    protected void verify(String destFilePath) throws Exception {
        AnyMemoDBOpenHelper helper =
            AnyMemoDBOpenHelperManager.getHelper(getContext(), destFilePath);
        try {
            CardDao cardDao = helper.getCardDao();
            List<Card> cards = cardDao.queryForAll();
            assertEquals(2, cards.size());
            assertEquals("This is question1", cards.get(0).getQuestion());
            assertEquals("Answer1", cards.get(0).getAnswer());
            assertEquals(1, (int) cards.get(0).getOrdinal());
            assertEquals(1, (int) cards.get(0).getId());

            assertEquals("Question2", cards.get(1).getQuestion());
            assertEquals("Answer2", cards.get(1).getAnswer());
            assertEquals(2, (int) cards.get(1).getOrdinal());
            assertEquals(2, (int) cards.get(1).getId());
        } finally {
            helper.close();
        }
    }

}
