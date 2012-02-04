package org.liberty.android.fantastischmemo;

import java.sql.SQLException;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;

import android.util.Log;

/*
 * Keep the reference count of each AnyMemoDBOpenHelper.
 */
public class AnyMemoDBOpenHelperManager {
    private static Map<String, AnyMemoDBOpenHelper> helpers = new ConcurrentHashMap<String, AnyMemoDBOpenHelper>();
    private static Map<String, Integer> refCounts = new ConcurrentHashMap<String, Integer>();

    private static String TAG = "AnyMemoDBOpenHelperManager";

    public static synchronized AnyMemoDBOpenHelper getHelper(Context context, String dbpath) {
        assert dbpath != null : "dbpath should not be null";
        if (!helpers.containsKey(dbpath)) {
            Log.i(TAG, "Call get AnyMemoDBOpenHelper for first time."); 
            try {
                AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelper.getHelper(context, dbpath);
                helpers.put(dbpath, helper);
                refCounts.put(dbpath, 1);
                return helpers.get(dbpath);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            Log.i(TAG, "Call get AnyMemoDBOpenHelper again, return existing helper."); 
            refCounts.put(dbpath, refCounts.get(dbpath) + 1);
            return helpers.get(dbpath);
        }
    }

    public static synchronized void releaseHelper(String dbpath) {
        Log.i(TAG, "Release AnyMemoDBOpenHelper: " + dbpath); 

        if (!helpers.containsKey(dbpath)) {
            throw new RuntimeException("Release a wrong db path or release an already been released helper!");
        }
        refCounts.put(dbpath, refCounts.get(dbpath) - 1);

        if (refCounts.get(dbpath) == 0) {
            AnyMemoDBOpenHelper helper = helpers.get(dbpath); 
            helper.close();
            helpers.remove(dbpath);
            Log.i(TAG, "All connection released. Close helper. DB: " + dbpath); 
        }
    }
}
