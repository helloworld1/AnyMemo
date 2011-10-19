/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.liberty.android.fantastischmemo.dao;

import java.util.concurrent.locks.ReentrantLock;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DAOBase {
    private static final String TAG = DAOBase.class.getSimpleName();

    private final String mPath;
    private final String mName;
    /* Use the default cursor factory */
    private final CursorFactory mFactory = null;
    /* Set the version to 2 */
    private final int CURRENT_VERSION = 2;

    private SQLiteDatabase mDatabase = null;
    private boolean mIsInitializing = false;
    /* Lock for DAO */
    private final ReentrantLock mLock = new ReentrantLock(true);

    protected DAOBase(String path, String name) {
        mPath = path;
        mName = name;
    }

    /**
     * See http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html
     * For the detail of this method
     */
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
            return mDatabase;  // The database is already open for business
        }

        if (mIsInitializing) {
            throw new IllegalStateException("getWritableDatabase called recursively");
        }

        // If we have a read-only database open, someone could be using it
        // (though they shouldn't), which would cause a lock to be held on
        // the file, and our attempts to open the database read-write would
        // fail waiting for the file lock.  To prevent that, we acquire the
        // lock on the read-only database, which shuts out other users.

        boolean success = false;
        SQLiteDatabase db = null;
        if (mDatabase != null) mLock.lock();
        try {
            mIsInitializing = true;
            if (mName == null) {
                db = SQLiteDatabase.create(null);
            } else {
                db = SQLiteDatabase.openDatabase(mPath + "/" + mName, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
            }

            int version = db.getVersion();
            if (version != CURRENT_VERSION) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        if (version > CURRENT_VERSION) {
                            Log.wtf(TAG, "Can't downgrade read-only database from version " +
                                    version + " to " + CURRENT_VERSION + ": " + db.getPath());
                        }
                        onUpgrade(db, version, CURRENT_VERSION);
                    }
                    db.setVersion(CURRENT_VERSION);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            onOpen(db);
            success = true;
            return db;
        } finally {
            mIsInitializing = false;
            if (success) {
                if (mDatabase != null) {
                    try { mDatabase.close(); } catch (Exception e) { }
                    mLock.unlock();
                }
                mDatabase = db;
            } else {
                if (mDatabase != null) mLock.unlock();
                if (db != null) db.close();
            }
        }
    }

    /**
     * See http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html
     * For the detail of this method
     */
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (mDatabase != null && mDatabase.isOpen()) {
            return mDatabase;  // The database is already open for business
        }

        if (mIsInitializing) {
            throw new IllegalStateException("getReadableDatabase called recursively");
        }

        try {
            return getWritableDatabase();
        } catch (SQLiteException e) {
            if (mName == null) throw e;  // Can't open a temp database read-only!
            Log.e(TAG, "Couldn't open " + mName + " for writing (will try read-only):", e);
        }

        SQLiteDatabase db = null;
        try {
            mIsInitializing = true;
            db = SQLiteDatabase.openDatabase(mPath + "/" + mName, mFactory, SQLiteDatabase.OPEN_READONLY);
            if (db.getVersion() != CURRENT_VERSION) {
                throw new SQLiteException("Can't upgrade read-only database from version " +
                        db.getVersion() + " to " + CURRENT_VERSION + ": " + mPath);
            }

            onOpen(db);
            Log.w(TAG, "Opened " + mName + " in read-only mode");
            mDatabase = db;
            return mDatabase;
        } finally {
            mIsInitializing = false;
            if (db != null && db != mDatabase) db.close();
        }
    }

    /**
     * Close any open database object.
     */
    public synchronized void close() {
        if (mIsInitializing) throw new IllegalStateException("Closed during initialization");

        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     */
    public void onCreate(SQLiteDatabase db) {
    }

    /*
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /*
     * Called when the database has been opened.  The implementation
     * should check {@link SQLiteDatabase#isReadOnly} before updating the
     * database.
     */
    public void onOpen(SQLiteDatabase db) {
    }
}
