package org.liberty.android.fantastischmemo;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import android.util.Log;

/*
 * Keep the reference count of each AnyMemoDBOpenHelper.
 */
public class AnyMemoDBOpenHelperManager {
    private static Map<String, AnyMemoDBOpenHelper> helpers
        = new HashMap<String, AnyMemoDBOpenHelper>();
    private static Map<String, Integer> refCounts
        = new HashMap<String, Integer>();

    private static String TAG = "AnyMemoDBOpenHelperManager";

    public static AnyMemoDBOpenHelper getHelper(Context context, String dbpath) {
        if (helpers.containsKey(dbpath)) {
            // Increase the reference count
            Log.i(TAG, "Call get AnyMemoDBOpenHelper with ref" + refCounts.get(dbpath)); 
            refCounts.put(dbpath, refCounts.get(dbpath) + 1);
            return helpers.get(dbpath);
        } else {
            try {
                Log.i(TAG, "Call get AnyMemoDBOpenHelper for first time."); 
                AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelper.getHelper(context, dbpath);
                helpers.put(dbpath, helper);
                refCounts.put(dbpath, 1);
                return helper;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void releaseHelper(String dbpath) {
        Integer count = refCounts.get(dbpath);
        Log.i(TAG, "Release AnyMemoDBOpenHelper: " + dbpath); 
        if (count > 1) {
            refCounts.put(dbpath, count - 1);
        } else {
            Log.i(TAG, "Close AnyMemoDBOpenHelper since there are 0 connections: " + dbpath); 
            AnyMemoDBOpenHelper helper = helpers.get(dbpath); 
            helper.close();
            helpers.remove(dbpath);
            refCounts.remove(dbpath);
        }
    }

}

