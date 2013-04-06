package org.liberty.android.fantastischmemo.test;

import java.io.File;
import java.io.InputStream;

import org.apache.mycommons.io.FileUtils;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;

import android.content.Context;
import android.test.AndroidTestCase;

public class AbstractExistingDBTest extends AndroidTestCase {

    protected AnyMemoDBOpenHelper helper;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Context testContext = getContext();
        InputStream in = testContext.getResources().getAssets().open(AMEnv.DEFAULT_DB_NAME);
        File outFile = new File("/sdcard/french-body-parts.db");
        outFile.delete();

        FileUtils.copyInputStreamToFile(in, outFile);
        in.close();
        helper = AnyMemoDBOpenHelperManager.getHelper(testContext, "/sdcard/french-body-parts.db");
    }

    @Override
    protected void tearDown() throws Exception {
        AnyMemoDBOpenHelperManager.releaseHelper(helper);
        File outFile = new File("/sdcard/french-body-parts.db");
        outFile.delete();
    }
}
