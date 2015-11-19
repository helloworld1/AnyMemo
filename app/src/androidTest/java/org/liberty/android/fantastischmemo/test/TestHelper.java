package org.liberty.android.fantastischmemo.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AMPrefKeys;

import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

public class TestHelper {
    public static final String SAMPLE_DB_PATH = "/sdcard/anymemo/french-body-parts.db";
    public static final String SAMPLE_DB_NAME= "french-body-parts.db";

    private Context mTargetContext;

    public TestHelper(Instrumentation ins) {
        mTargetContext = ins.getTargetContext();
    }

    /* Set up the french-body-parts database */
    public void setUpFBPDatabase() {
        try {
            InputStream in = mTargetContext.getResources().getAssets().open(AMEnv.DEFAULT_DB_NAME);
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
        try {
            int currentVersionCode = mTargetContext.getPackageManager()
                .getPackageInfo(mTargetContext.getPackageName(), 0).versionCode;
            Editor editor = settings.edit();
            editor.putBoolean(AMPrefKeys.FIRST_TIME_KEY, true);
            editor.putInt(AMPrefKeys.SAVED_VERSION_CODE_KEY, currentVersionCode);
            editor.commit();
        } catch (PackageManager.NameNotFoundException e) {
            // This is a coding error
            throw new AssertionError(e);
        }
    }
}
