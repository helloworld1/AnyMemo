package org.liberty.android.fantastischmemo;

import java.util.List;

import java.util.concurrent.Callable;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;

import android.content.Context;

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
                        cardDaoDest.create(c);
                    }
                    return null;
                }
            });
        AnyMemoDBOpenHelperManager.releaseHelper(destPath);
        AnyMemoDBOpenHelperManager.releaseHelper(srcPath);
    }
}
