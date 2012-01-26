package org.liberty.android.fantastischmemo;

import java.io.File;

import java.util.List;

import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;

import android.content.Context;

import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class DatabaseUtils {
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
        AnyMemoDBOpenHelperManager.releaseHelper(destPath);
        System.out.println("DatabaseUtils release srcPath");
        AnyMemoDBOpenHelperManager.releaseHelper(srcPath);
    }

    /*
     * Check if the database is in the correct format
     */
    public static boolean checkDatabase(Context context, String dbPath) {
        if (!(new File(dbPath)).exists()) {
            return false;
        }
        try {
            AnyMemoDBOpenHelperManager.getHelper(context, dbPath);
            AnyMemoDBOpenHelperManager.releaseHelper(dbPath);
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }
}
