package org.liberty.android.fantastischmemo.test.converter;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.converter.CSVImporter;
import org.liberty.android.fantastischmemo.converter.Converter;
import org.liberty.android.fantastischmemo.converter.QATxtImporter;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;


/**
 * Test the import ability to merge into the database with the same name.
 */
public class ImportMergingTest extends AndroidTestCase {

    private Context testContext;

    private AMFileUtil amFileUtil;

    private String srcFilePath;

    private String destFilePath;

    private List<Card> newDbCardList;

    @Override
    public void setUp() throws Exception {
        // Reflect out the test context
        Method getTestContext = ServiceTestCase.class.getMethod("getTestContext");
        testContext = (Context) getTestContext.invoke(this);

        amFileUtil = new AMFileUtil(testContext);
        amFileUtil.setAmPrefUtil(new AMPrefUtil(getContext()));

        newDbCardList = new ArrayList<Card>();
        Card c1 = new Card();
        c1.setQuestion("old question 1");
        c1.setAnswer("old answer 1");
        c1.setLearningData(new LearningData());
        c1.setCategory(new Category());
        Card c2 = new Card();
        c2.setQuestion("old question 2");
        c2.setAnswer("old answer 2");
        c2.setLearningData(new LearningData());
        c2.setCategory(new Category());
        newDbCardList.add(c1);
        newDbCardList.add(c2);
    }

    @SmallTest
    public void testMergeCsvIntoDb() throws Exception {
        srcFilePath = AMEnv.DEFAULT_ROOT_PATH + "/" + "csv-test.csv";
        destFilePath = AMEnv.DEFAULT_ROOT_PATH + "/" + "csv-test.db";
        new File(srcFilePath).delete();
        new File(destFilePath).delete();

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(getContext(), destFilePath);
        try {
            // Create an new db with some contents
            helper.getCardDao().createCards(newDbCardList);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

        // Now convert the csv-test.csv into csv-test.db which will be merged
        // into existing csv-test.db
        amFileUtil.copyFileFromAsset("csv-test.csv", new File(srcFilePath));
        Converter converter = new CSVImporter(',');
        converter.convert(srcFilePath, destFilePath);
            
        // verify the content of csv-test has merged cards
        helper = AnyMemoDBOpenHelperManager.getHelper(getContext(), destFilePath);
        try {
            List<Card> cards = helper.getCardDao().getAllCards(null);
            assertEquals(6, cards.size());

            assertEquals(1, (int) cards.get(0).getId());
            assertEquals(1, (int) cards.get(0).getOrdinal());
            assertEquals("old question 1", cards.get(0).getQuestion());
            assertEquals("old answer 1", cards.get(0).getAnswer());

            assertEquals(2, (int) cards.get(1).getId());
            assertEquals(2, (int) cards.get(1).getOrdinal());
            assertEquals("old question 2", cards.get(1).getQuestion());
            assertEquals("old answer 2", cards.get(1).getAnswer());

            assertEquals(3, (int) cards.get(2).getId());
            assertEquals(3, (int) cards.get(2).getOrdinal());
            assertEquals("Question1", cards.get(2).getQuestion());
            assertEquals("Answer1", cards.get(2).getAnswer());
            assertEquals("Category1", cards.get(2).getCategory().getName());
            assertEquals("Note1", cards.get(2).getNote());

            assertEquals(4, (int) cards.get(3).getId());
            assertEquals(4, (int) cards.get(3).getOrdinal());
            assertEquals("Question2", cards.get(3).getQuestion());
            assertEquals("Answer2", cards.get(3).getAnswer());
            assertEquals("Category1", cards.get(3).getCategory().getName());
            assertEquals("Note2", cards.get(3).getNote());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    @SmallTest
    public void testMergeQATxtIntoDb() throws Exception {
        srcFilePath = AMEnv.DEFAULT_ROOT_PATH + "/" + "qa-text-test.txt";
        destFilePath = AMEnv.DEFAULT_ROOT_PATH + "/" + "qa-text-test.db";
        new File(srcFilePath).delete();
        new File(destFilePath).delete();

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(getContext(), destFilePath);
        try {
            // Create an new db with some contents
            helper.getCardDao().createCards(newDbCardList);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

        amFileUtil.copyFileFromAsset("qa-text-test.txt", new File(srcFilePath));
        Converter converter = new QATxtImporter();
        converter.convert(srcFilePath, destFilePath);

        helper = AnyMemoDBOpenHelperManager.getHelper(getContext(), destFilePath);
        try {
            List<Card> cards = helper.getCardDao().getAllCards(null);
            assertEquals(4, cards.size());

            assertEquals(1, (int) cards.get(0).getId());
            assertEquals(1, (int) cards.get(0).getOrdinal());
            assertEquals("old question 1", cards.get(0).getQuestion());
            assertEquals("old answer 1", cards.get(0).getAnswer());

            assertEquals(2, (int) cards.get(1).getId());
            assertEquals(2, (int) cards.get(1).getOrdinal());
            assertEquals("old question 2", cards.get(1).getQuestion());
            assertEquals("old answer 2", cards.get(1).getAnswer());

            assertEquals(3, (int) cards.get(2).getId());
            assertEquals(3, (int) cards.get(2).getOrdinal());
            assertEquals("This is question1", cards.get(2).getQuestion());
            assertEquals("Answer1", cards.get(2).getAnswer());

            assertEquals(4, (int) cards.get(3).getId());
            assertEquals(4, (int) cards.get(3).getOrdinal());
            assertEquals("Question2", cards.get(3).getQuestion());
            assertEquals("Answer2", cards.get(3).getAnswer());
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    @Override
    public void tearDown() {
        if (srcFilePath != null) {
            new File(srcFilePath).delete();
        }
        if (destFilePath != null) {
            new File(destFilePath).delete();
        }
    }
}

