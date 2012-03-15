package org.liberty.android.fantastischmemo.test.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.mycommons.io.FileUtils;

import org.liberty.android.fantastischmemo.AMEnv;

import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import android.app.Activity;

import android.content.Context;
import android.content.SharedPreferences;

import android.content.SharedPreferences.Editor;

import android.preference.PreferenceManager;

public class UITestHelper {
    public static final String SAMPLE_DB_PATH = "/sdcard/anymemo/french-body-parts.db";
    public static final String SAMPLE_DB_NAME= "french-body-parts.db";

    Context mTestContext;
    Activity mActivity;
    public UITestHelper(Context testContext, Activity activity) {
        mTestContext = testContext;
        mActivity = activity;
    }

    /* Set up the french-body-parts database */
    public void setUpFBPDatabase() {
        try {
            InputStream in = mTestContext.getResources().getAssets().open(AMEnv.DEFAULT_DB_NAME);
            File outFile = new File(SAMPLE_DB_PATH);
            outFile.delete();

            FileUtils.copyInputStreamToFile(in, outFile);
            RecentListUtil.addToRecentList(mActivity, SAMPLE_DB_PATH);
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } 

    /* Clear up the preferences for tests*/
    public void clearPreferences() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
        Editor editor = settings.edit();
        editor.clear();
        editor.commit(); 
        // Don't show first fime;
        editor.putBoolean("first_time", false);
        editor.commit();
    }
}
