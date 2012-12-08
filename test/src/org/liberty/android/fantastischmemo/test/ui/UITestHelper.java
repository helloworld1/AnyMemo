package org.liberty.android.fantastischmemo.test.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.mycommons.io.FileUtils;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;

import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class UITestHelper {
    public static final String SAMPLE_DB_PATH = "/sdcard/anymemo/french-body-parts.db";
    public static final String SAMPLE_DB_NAME= "french-body-parts.db";

    private Context mTestContext;;
    private Context mTargetContext;

    public UITestHelper(Instrumentation ins) {
        mTestContext = ins.getTargetContext();
        mTargetContext = ins.getTargetContext();
    }

    /* Set up the french-body-parts database */
    public void setUpFBPDatabase() {
        try {
            InputStream in = mTestContext.getResources().getAssets().open(AMEnv.DEFAULT_DB_NAME);
            File outFile = new File(SAMPLE_DB_PATH);
            outFile.delete();

            FileUtils.copyInputStreamToFile(in, outFile);
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } 

    /* Clear up the preferences for tests*/
    public void clearPreferences() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mTargetContext);
        Editor editor = settings.edit();
        editor.clear();
        editor.commit(); 
    }

    // Mark the preference that it is not the first time to use the app.
    public void markNotFirstTime() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mTargetContext);
        Editor editor = settings.edit();
        editor.putBoolean(AMPrefKeys.FIRST_TIME_KEY, true);
        editor.putString(AMPrefKeys.SAVED_VERSION_KEY, mTestContext.getString(R.string.app_version));
        editor.commit();
    }
}
