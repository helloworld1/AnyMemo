package org.liberty.android.fantastischmemo;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.DeckDao;
import org.liberty.android.fantastischmemo.dao.FilterDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.dao.SettingDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.Deck;
import org.liberty.android.fantastischmemo.domain.Filter;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.domain.Setting;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

import com.j256.ormlite.support.ConnectionSource;

import com.j256.ormlite.table.TableUtils;

import android.content.Context;

import java.sql.SQLException;

import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;

import android.util.Log;

public class AnyMemoDBOpenHelper extends OrmLiteSqliteOpenHelper {

    private final String TAG = getClass().getSimpleName();

    private static String dbPath = null;

	private static AnyMemoDBOpenHelper helper = null;

    private static final int CURRENT_VERSION = 2;

    private CardDao cardDao = null;
    
    private DeckDao deckDao = null;

    private SettingDao settingDao = null;

    private FilterDao filterDao = null;

    private CategoryDao categoryDao = null;

    private LearningDataDao learningDataDao = null;

    public AnyMemoDBOpenHelper(Context context, String dbpath) {
        super(context, dbpath, null, CURRENT_VERSION);
    }

	public static synchronized AnyMemoDBOpenHelper getHelper(Context context, String dbpath)
        throws SQLException {
		if (helper == null) {
			helper = new AnyMemoDBOpenHelper(context, dbpath);
		} else if (!dbpath.equals(dbPath)) {
            Log.e("AnyMemoDBOpenHelper.getHelper",
                    "Open two database at the same. Please close the previous connection");
            throw new SQLException("Open two database at the same. Please close the previous connection");
        } else {
            Log.i("AnyMemoDBOpenHelper.getHelper", "Reuse database helper");
        }
		return helper;
	}

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

            if (database.getVersion() == 0) {
                convertOldDatabase(database);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            database.execSQL("insert into cards (ordinal, question, answer)" +
                    " select _id as ordinal, question, answer from dict_tbl");


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
                    + " select category as name from dict_tbl"
                    + " group by category");
            database.execSQL("update cards set category_id = ("
                + " select id as category_id from categories as cat"
                + " join dict_tbl as dic on dic.category = cat.name"
                + " where cards.id = dic._id)");
        }
    }

    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.v(TAG, "Old version" + oldVersion + " new version: " + newVersion);
    }

    public CardDao getCardDao() throws SQLException {
        if (cardDao == null) {
            cardDao = getDao(Card.class);
        }
        return cardDao;
    }

    public DeckDao getDeckDao() throws SQLException {
        if (deckDao == null) {
            deckDao = getDao(Deck.class);
        }
        return deckDao;
    }

    public SettingDao getSettingDao() throws SQLException {
        if (settingDao == null) {
            settingDao = getDao(Setting.class);
        }
        return settingDao;
    }

    public FilterDao getFilterDao() throws SQLException {
        if (filterDao == null) {
            filterDao = getDao(Filter.class);
        }
        return filterDao;
    }

    public CategoryDao getCategoryDao() throws SQLException {
        if (categoryDao == null) {
            categoryDao = getDao(Category.class);
        }
        return categoryDao;
    }

    public LearningDataDao getLearningDataDao() throws SQLException {
        if (learningDataDao == null) {
            learningDataDao = getDao(LearningData.class);
        }
        return learningDataDao;
    }
}


