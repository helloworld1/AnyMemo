package org.liberty.android.fantastischmemo.common;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.DeckDao;
import org.liberty.android.fantastischmemo.dao.FilterDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.entity.Card;
import org.liberty.android.fantastischmemo.entity.Category;
import org.liberty.android.fantastischmemo.entity.Deck;
import org.liberty.android.fantastischmemo.entity.Filter;
import org.liberty.android.fantastischmemo.entity.LearningData;
import org.liberty.android.fantastischmemo.entity.Setting;

import java.sql.SQLException;

public class AnyMemoDBOpenHelper extends OrmLiteSqliteOpenHelper {

    private final String TAG = getClass().getSimpleName();

    private final String dbPath;

    private static final int CURRENT_VERSION = 5;

    private CardDao cardDao = null;

    private DeckDao deckDao = null;

    private SettingDao settingDao = null;

    private FilterDao filterDao = null;

    private CategoryDao categoryDao = null;

    private LearningDataDao learningDataDao = null;

    private boolean isReleased = false;

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        Log.v(TAG, "Now we are creating a new database!");
        Log.i(TAG, "Newly created db version: " + database.getVersion());

        try {
            TableUtils.createTable(connectionSource, Card.class);
            TableUtils.createTable(connectionSource, Deck.class);
            TableUtils.createTable(connectionSource, Setting.class);
            TableUtils.createTable(connectionSource, Filter.class);
            TableUtils.createTable(connectionSource, Category.class);
            TableUtils.createTable(connectionSource, LearningData.class);

            getSettingDao().create(new Setting());
            getCategoryDao().create(new Category());

            if (database.getVersion() == 0) {
                convertOldDatabase(database);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database creation error: " + e.toString());
        }
    }

    /* Convert database from AnyMemo < 9.0 */
    private void convertOldDatabase(SQLiteDatabase database) {
        Cursor res = database.rawQuery("select name from sqlite_master where type = 'table' and name = 'dict_tbl'", null);
        boolean isOldDatabase = res.getCount() > 0;
        res.close();
        // This is old database
        if (isOldDatabase) {
            // copy all cards
            database.execSQL("insert into cards (ordinal, question, answer, note)" +
                    " select _id as ordinal, question, answer, note from dict_tbl");


            // Make sure the count matches in old database;
            int count_dict = 0, count_learn = 0;
            Cursor result = database.rawQuery("SELECT _id FROM dict_tbl", null);
            count_dict = result.getCount();
            result.close();
            result = database.rawQuery("SELECT _id FROM learn_tbl", null);
            count_learn = result.getCount();
            result.close();
            if(count_learn != count_dict){
                database.execSQL("DELETE FROM learn_tbl");
                database.execSQL("INSERT INTO learn_tbl(_id) SELECT _id FROM dict_tbl");
                database.execSQL("UPDATE learn_tbl SET date_learn = '2010-01-01', interval = 0, grade = 0, easiness = 2.5, acq_reps = 0, ret_reps  = 0, lapses = 0, acq_reps_since_lapse = 0, ret_reps_since_lapse = 0");
            }

            // copy learning data
            database.execSQL("update cards set learningData_id = ("
                + " select _id as learningData_id"
                + " from learn_tbl where learn_tbl._id = cards.id)");
            database.execSQL("insert into learning_data (acqReps, acqRepsSinceLapse, easiness,"
                + " grade, lapses, lastLearnDate, nextLearnDate, retReps, "
                + " retRepsSinceLapse)"
                + " select acq_reps as acqReps , acq_reps_since_lapse as acqRepsSinceLapse,"
                + " easiness, grade, lapses,"
                + " date_learn || ' 00:00:00.000000' as lastLearnDate,"
                + " datetime(julianday(date_learn) + interval) || '.000000' as nextLearnDate,"
                + " ret_reps as retReps, ret_reps_since_lapse as retRepsSinceLapse"
                + " from learn_tbl");


            // copy categories
            database.execSQL("insert into categories (name)"
                + " select category as name from dict_tbl where category != ''"
                + " and category is not null"
                + " group by category");
            database.execSQL("update cards set category_id = ("
                + " select id as category_id from categories as cat"
                + " join dict_tbl as dic on dic.category = cat.name"
                + " where cards.id = dic._id)");

            // Update category if the category is null
            database.execSQL("update cards "
                    + " set category_id = 1"
                    + " where category_id is null");

            database.execSQL("update cards set updateDate='2010-01-01 00:00:00.000000'," +
                    "creationDate='2010-01-01 00:00:00.000000'");
            database.execSQL("update categories set updateDate='2010-01-01 00:00:00.000000'");
            database.execSQL("update learning_data set updateDate='2010-01-01 00:00:00.000000'");

            // Set unused fields
            database.execSQL("update cards"
                + " set cardType = 0");


        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.v(TAG, "Old version" + oldVersion + " new version: " + newVersion);
        // Update possible card with null category field
        if (oldVersion <= 2) {
            database.execSQL("update cards "
                    + " set category_id = 1"
                    + " where category_id is null");
        }
        if (oldVersion <= 3) {
            database.execSQL("update settings set questionTextColor = ? where questionTextColor = ?", new Object[] {null, 0xFFBEBEBE});
            database.execSQL("update settings set answerTextColor = ? where answerTextColor = ?", new Object[] {null, 0xFFBEBEBE} );
            database.execSQL("update settings set questionBackgroundColor = ? where questionBackgroundColor = ?", new Object[] {null, 0xFF000000});
            database.execSQL("update settings set answerBackgroundColor = ? where answerBackgroundColor = ?", new Object[] {null, 0xFF000000});
        }
        if (oldVersion <= 4) {
            try {
                database.execSQL("alter table learning_data add column firstLearnDate VARCHAR");
                database.execSQL("update learning_data set firstLearnDate='2010-01-01 00:00:00.000000'");
            } catch (android.database.SQLException e) {
                Log.e(TAG, "Upgrading failed, the column firstLearnData might already exists.", e);
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, String.format("Downgrading database from version %1$d to %2$d", oldVersion, newVersion));
    }

    /**
     * Do not call this method directly, use AnyMemoDBOpenHelperManager instead.
     */
    @Override
    public void close() {
        isReleased = true;
        try {
            DatabaseConnection connection = getConnectionSource().getReadWriteConnection();
            getConnectionSource().releaseConnection(connection);
        } catch (SQLException e) {
            Log.e(TAG, "Error releasing the connection.", e);
        }
        super.close();
    }

    public synchronized CardDao getCardDao() {
        try {
            if (cardDao == null) {
                cardDao = getDao(Card.class);
                cardDao.setHelper(this);
            }
            return cardDao;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized DeckDao getDeckDao() {
        try {
            if (deckDao == null) {
                deckDao = getDao(Deck.class);
            }
            return deckDao;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized SettingDao getSettingDao() {
        try {
            if (settingDao == null) {
                settingDao = getDao(Setting.class);
            }
            return settingDao;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized FilterDao getFilterDao() {
        try {
            if (filterDao == null) {
                filterDao = getDao(Filter.class);
            }
            return filterDao;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized CategoryDao getCategoryDao() {
        try {
            if (categoryDao == null) {
                categoryDao = getDao(Category.class);
            }
            return categoryDao;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized LearningDataDao getLearningDataDao() {
        try {
            if (learningDataDao == null) {
                learningDataDao = getDao(LearningData.class);
            }
            return learningDataDao;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Override the finalize in case the helper is not release.
     */
    @Override
    public void finalize() throws Throwable {
        super.finalize();
        // If the finalize kicked in before the db is released.
        // force release the helper!
        // This is usually a bug in program.
        if (!isReleased) {
            Log.w(TAG, "AnyMemoDBOpenHelper for db " + dbPath + " is not released before being GCed. This class must be explicitly released! Force releasing now.");
            AnyMemoDBOpenHelperManager.forceRelease(dbPath);
        }
    }

    /* Package private constructor used in Manager. */
    AnyMemoDBOpenHelper(Context context, String dbpath) {
        // R.raw.ormlite_config is used to accelerate the DAO creation.
        super(context, dbpath, null, CURRENT_VERSION);
        this.dbPath = dbpath;
    }

    /* Package private getDbPath used in Manager. */
    String getDbPath() {
        return dbPath;
    }
}
