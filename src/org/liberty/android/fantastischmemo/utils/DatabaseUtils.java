package org.liberty.android.fantastischmemo.utils;

import java.io.File;

import java.sql.SQLException;

import java.util.List;

import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Setting;

import android.content.Context;

public class DatabaseUtils {

    public static Setting readDefaultSetting(Context context) {
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(context, AMEnv.EMPTY_DB_NAME);
        try {
            SettingDao settingDao = helper.getSettingDao(); 
            return settingDao.queryForId(1);
        } catch (SQLException e) {
            throw new RuntimeException("Could not read setting from default db", e);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

    }

    public static void mergeDatabases(Context context, String destPath, String srcPath) throws Exception {
        AnyMemoDBOpenHelper destHelper = AnyMemoDBOpenHelperManager.getHelper(context, destPath);
        AnyMemoDBOpenHelper srcHelper = AnyMemoDBOpenHelperManager.getHelper(context, srcPath);

        final CardDao cardDaoDest = destHelper.getCardDao();
        final LearningDataDao learningDataDaoDest = destHelper.getLearningDataDao();
        final CategoryDao categoryDaoDest = destHelper.getCategoryDao();
        final CardDao cardDaoSrc = srcHelper.getCardDao();
        final List<Card> srcCards = cardDaoSrc.queryForAll();
        final Category uncategorized = categoryDaoDest.queryForId(1);
        cardDaoDest.callBatchTasks(
            new Callable<Void>() {
                public Void call() throws Exception {
                    for (Card c : srcCards) {
                        LearningData emptyLearningData = new LearningData();
                        c.setCategory(uncategorized);
                        learningDataDaoDest.create(emptyLearningData);
                        c.setLearningData(emptyLearningData);
                        c.setOrdinal(null);
                        cardDaoDest.create(c);
                    }
                    return null;
                }
            });
        System.out.println("DatabaseUtils release destPath");
        AnyMemoDBOpenHelperManager.releaseHelper(destHelper);
        System.out.println("DatabaseUtils release srcPath");
        AnyMemoDBOpenHelperManager.releaseHelper(srcHelper);
    }

    /*
     * Check if the database is in the correct format
     */
    public static boolean checkDatabase(Context context, String dbPath) {
        if (!(new File(dbPath)).exists()) {
            return false;
        }
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(context, dbPath);
        try {
            helper.getCardDao(); 
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }
}
