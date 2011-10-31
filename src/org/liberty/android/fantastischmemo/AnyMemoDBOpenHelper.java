package org.liberty.android.fantastischmemo;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

import com.j256.ormlite.support.ConnectionSource;

import android.content.Context;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;

import android.util.Log;

public class AnyMemoDBOpenHelper extends OrmLiteSqliteOpenHelper {

    private final String TAG = getClass().getSimpleName();

    private static String dbPath = null;

	private static AnyMemoDBOpenHelper helper = null;

    private static final int CURRENT_VERSION = 2;

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
    }
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
    }

}


