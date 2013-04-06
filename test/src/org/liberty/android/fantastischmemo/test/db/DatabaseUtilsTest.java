package org.liberty.android.fantastischmemo.test.db;

import java.io.File;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.test.AbstractExistingDBTest;
import org.liberty.android.fantastischmemo.utils.DatabaseUtil;

import android.test.suitebuilder.annotation.SmallTest;


public class DatabaseUtilsTest extends AbstractExistingDBTest {

    DatabaseUtil databaseUtil;

    String dbPath = "/sdcard/french-body-parts.db";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        databaseUtil = new DatabaseUtil(getContext());
    }

    @SmallTest
    public void testGetDefaultSetting() throws Exception {
        Setting setting = databaseUtil.readDefaultSetting();
        assertNotNull(setting);
    }

    @SmallTest
    @SuppressWarnings("unused")
    public void testMergeDatabase() throws Exception {
        // Create testing DB to merge
        String path2 = "/sdcard/testdb2.db";
        new File(path2).delete();
        AnyMemoDBOpenHelper helper2 = AnyMemoDBOpenHelperManager.getHelper(getContext(), path2);

        // DAOs to use
        CardDao destCardDao = helper.getCardDao();
        CategoryDao destCategoryDao = helper.getCategoryDao();
        LearningDataDao destLearningDataDao = helper.getLearningDataDao();

        CardDao srcCardDao = helper2.getCardDao();
        CategoryDao srcCategoryDao= helper2.getCategoryDao();

        LearningDataDao srcLearningDataDao = helper2.getLearningDataDao();

        // Create some categories in both db to test category merge
        Category destCat1 = destCategoryDao.createOrReturn("cat1");
        Category destCat2 = destCategoryDao.createOrReturn("cat2");

        Category srcCat2 = srcCategoryDao.createOrReturn("cat2");
        Category srcCat3 = srcCategoryDao.createOrReturn("cat3");

        // Modify the learning data and category for some cards in src
        Card destCard20 = destCardDao.queryForId(20);
        destCategoryDao.refresh(destCard20.getCategory());
        destLearningDataDao.refresh(destCard20.getLearningData());
        destCard20.setCategory(destCat1);

        LearningData destLd20 = destCard20.getLearningData();
        // Use a specific acq_reps for testing
        destLd20.setAcqReps(15);
        destLearningDataDao.update(destLd20);
        destCardDao.update(destCard20);
        
        // Another card for testing duplicated category in src and dest
        Card destCard21 = destCardDao.queryForId(21);
        destCategoryDao.refresh(destCard21.getCategory());
        destLearningDataDao.refresh(destCard21.getLearningData());
        destCard21.setCategory(destCat2);

        LearningData destLd21 = destCard21.getLearningData();
        destLd21.setAcqReps(16);
        destLearningDataDao.update(destLd21);
        destCardDao.update(destCard21);

        // Now set up the cards in the src db

        // Card 1 with a duplicated category as dest db
        Card srcCard1 = new Card();
        srcCard1.setQuestion("card1");
        LearningData srcLd1 = new LearningData();
        srcLd1.setAcqReps(8);
        srcCard1.setLearningData(srcLd1);
        srcCard1.setCategory(srcCat2);
        srcCardDao.createCard(srcCard1);

        // Card 2 with a new category
        Card srcCard2 = new Card();
        srcCard2.setQuestion("card2");
        LearningData srcLd2 = new LearningData();
        srcLd2.setAcqReps(10);
        srcCard2.setLearningData(srcLd2);
        srcCard2.setCategory(srcCat3);
        srcCardDao.createCard(srcCard2);

        // Card 3 with default lenaring data and category
        Card srcCard3 = new Card();
        srcCard3.setQuestion("card3");
        srcCard3.setCategory(new Category());
        srcCard3.setLearningData(new LearningData());
        srcCardDao.createCard(srcCard3);

        // Now merge them!
        databaseUtil.mergeDatabases(dbPath, path2);

        // Original 28 plus 3 merged cards
        assertEquals(31, destCardDao.queryForAll().size());
        assertEquals(31, destLearningDataDao.queryForAll().size());

        // 5 categories: "", "french-body-parts.db", "cat1", "cat2", "cat3"
        assertEquals(5, destCategoryDao.queryForAll().size());

        // Query out the cards we are interested in
        Card mergedCard20 = destCardDao.queryForId(20);
        destLearningDataDao.refresh(mergedCard20.getLearningData());
        destCategoryDao.refresh(mergedCard20.getCategory());

        Card mergedCard21 = destCardDao.queryForId(21);
        destLearningDataDao.refresh(mergedCard21.getLearningData());
        destCategoryDao.refresh(mergedCard21.getCategory());

        Card mergedCard29 = destCardDao.queryForId(29);
        destLearningDataDao.refresh(mergedCard29.getLearningData());
        destCategoryDao.refresh(mergedCard29.getCategory());

        Card mergedCard30 = destCardDao.queryForId(30);
        destLearningDataDao.refresh(mergedCard30.getLearningData());
        destCategoryDao.refresh(mergedCard30.getCategory());

        Card mergedCard31 = destCardDao.queryForId(31);
        destLearningDataDao.refresh(mergedCard31.getLearningData());
        destCategoryDao.refresh(mergedCard31.getCategory());

        // Now verify
        // The original cards in dest should be intact
        assertEquals(destCard20.getOrdinal(), mergedCard20.getOrdinal());
        assertEquals(destCard20.getQuestion(), mergedCard20.getQuestion());
        assertEquals("cat1", mergedCard20.getCategory().getName());
        assertEquals(15, (int)mergedCard20.getLearningData().getAcqReps());

        assertEquals(destCard21.getOrdinal(), mergedCard21.getOrdinal());
        assertEquals(destCard21.getQuestion(), mergedCard21.getQuestion());
        assertEquals("cat2", mergedCard21.getCategory().getName());
        assertEquals(16, (int)mergedCard21.getLearningData().getAcqReps());

        // The newly merged cards
        assertEquals(29, (int)mergedCard29.getOrdinal());
        assertEquals("card1", mergedCard29.getQuestion());
        assertEquals("cat2", mergedCard29.getCategory().getName());
        assertEquals(8, (int)mergedCard29.getLearningData().getAcqReps());

        assertEquals(30, (int)mergedCard30.getOrdinal());
        assertEquals("card2", mergedCard30.getQuestion());
        assertEquals("cat3", mergedCard30.getCategory().getName());
        assertEquals(10, (int)mergedCard30.getLearningData().getAcqReps());

        assertEquals(31, (int)mergedCard31.getOrdinal());
        assertEquals("card3", mergedCard31.getQuestion());
        assertEquals("", mergedCard31.getCategory().getName());
        assertEquals(0, (int)mergedCard31.getLearningData().getAcqReps());

        // Clearn up
        AnyMemoDBOpenHelperManager.releaseHelper(helper2);
        new File(path2).delete();
    }
}
