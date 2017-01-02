package org.liberty.android.fantastischmemo.utils;

import android.content.Context;

import org.liberty.android.fantastischmemo.common.AMEnv;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.common.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.entity.Card;
import org.liberty.android.fantastischmemo.entity.Setting;
import org.liberty.android.fantastischmemo.modules.ForApplication;
import org.liberty.android.fantastischmemo.modules.PerApplication;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

@PerApplication
public class DatabaseUtil {

    private Context mContext;

    @Inject
    public DatabaseUtil(@ForApplication Context context) {
        mContext = context;
    }

    public Setting readDefaultSetting() {
        String emptyDbPath = mContext.getApplicationContext().getFilesDir().getAbsolutePath() + "/" + AMEnv.EMPTY_DB_NAME;
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mContext, emptyDbPath);

        try {
            SettingDao settingDao = helper.getSettingDao();
            return settingDao.queryForId(1);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }

    }

    public void mergeDatabases(String destPath, String srcPath) throws Exception {
        AnyMemoDBOpenHelper destHelper = AnyMemoDBOpenHelperManager.getHelper(mContext, destPath);
        AnyMemoDBOpenHelper srcHelper = AnyMemoDBOpenHelperManager.getHelper(mContext, srcPath);

        final CardDao cardDaoDest = destHelper.getCardDao();
        final LearningDataDao learningDataDaoSrc= srcHelper.getLearningDataDao();
        final CategoryDao categoryDaoSrc = srcHelper.getCategoryDao();
        final CardDao cardDaoSrc = srcHelper.getCardDao();
        final List<Card> srcCards = cardDaoSrc.queryForAll();

        cardDaoSrc.callBatchTasks(
                new Callable<Void>() {
                    public Void call() throws Exception {
                        for (Card c : srcCards) {
                            // Make sure to create a new ordinal
                            c.setOrdinal(null);
                            learningDataDaoSrc.refresh(c.getLearningData());
                            categoryDaoSrc.refresh(c.getCategory());
                        }
                        return null;
                    }
                });

        cardDaoDest.createCards(srcCards);

        System.out.println("DatabaseUtils release destPath");
        AnyMemoDBOpenHelperManager.releaseHelper(destHelper);
        System.out.println("DatabaseUtils release srcPath");
        AnyMemoDBOpenHelperManager.releaseHelper(srcHelper);
    }

    /*
     * Check if the database is in the correct format
     */
    public boolean checkDatabase(String dbPath) {
        if (!(new File(dbPath)).exists()) {
            return false;
        }
        AnyMemoDBOpenHelper helper = null;
        try {
            helper = AnyMemoDBOpenHelperManager.getHelper(mContext, dbPath);
            helper.getCardDao().getTotalCount(null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (helper != null) {
                AnyMemoDBOpenHelperManager.releaseHelper(helper);
            }
        }
    }
}
