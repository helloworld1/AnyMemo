package org.liberty.android.fantastischmemo;

import java.io.File;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.mycommons.io.FilenameUtils;

import android.content.Context;

import android.util.Log;

/*
 * Keep the reference count of each AnyMemoDBOpenHelper.
 */
public class AnyMemoDBOpenHelperManager {
    private static Map<String, AnyMemoDBOpenHelper> helpers = new ConcurrentHashMap<String, AnyMemoDBOpenHelper>();
    private static Map<String, Integer> refCounts = new ConcurrentHashMap<String, Integer>();

    private static String TAG = "AnyMemoDBOpenHelperManager";

    /* Get a db open helper and return a cached one if it was called before for the same db */
    public static synchronized AnyMemoDBOpenHelper getHelper(Context context, String dbpath) {
        assert dbpath != null : "dbpath should not be null";
        dbpath = FilenameUtils.normalize(dbpath);
        if (!helpers.containsKey(dbpath)) {
            Log.i(TAG, "Call get AnyMemoDBOpenHelper for first time."); 
            AnyMemoDBOpenHelper helper = new AnyMemoDBOpenHelper(context, dbpath);
            helpers.put(dbpath, helper);
            refCounts.put(dbpath, 1);
            return helpers.get(dbpath);
        } else {
            Log.i(TAG, "Call get AnyMemoDBOpenHelper again, return existing helper."); 
            refCounts.put(dbpath, refCounts.get(dbpath) + 1);
            return helpers.get(dbpath);
        }
    }

    /* Release a db open helper if there is no open connection to it */
    public static synchronized void releaseHelper(AnyMemoDBOpenHelper helper) {
        String dbpath = FilenameUtils.normalize(helper.getDbPath());
        Log.i(TAG, "Release AnyMemoDBOpenHelper: " + dbpath); 

        if (!helpers.containsKey(dbpath)) {
            throw new RuntimeException("Release a wrong db path or release an already been released helper!");
        }
        refCounts.put(dbpath, refCounts.get(dbpath) - 1);

        if (refCounts.get(dbpath) == 0) {
            helper.close();
            helpers.remove(dbpath);
            Log.i(TAG, "All connection released. Close helper. DB: " + dbpath); 
        }
    }

    public static synchronized void forceRelease(String path) {
        String dbpath = FilenameUtils.normalize(path);
        if (!helpers.containsKey(dbpath)) {
            Log.w(TAG, "Force release a file that is not opened yet. Do nothing");
            return;
        }

        helpers.get(dbpath).close();
        helpers.remove(dbpath);
        Log.i(TAG, "Force release a db file. DB: " + dbpath); 

    }
}
