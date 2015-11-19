package org.liberty.android.fantastischmemo.test;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
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
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());
        
        Context testContext = getContext();
        InputStream in = testContext.getResources().getAssets().open(AMEnv.DEFAULT_DB_NAME);
        File outFile = new File(TestHelper.SAMPLE_DB_PATH);
        outFile.delete();

        FileUtils.copyInputStreamToFile(in, outFile);
        in.close();
        helper = AnyMemoDBOpenHelperManager.getHelper(testContext, TestHelper.SAMPLE_DB_PATH);
    }

    @Override
    protected void tearDown() throws Exception {
        AnyMemoDBOpenHelperManager.releaseHelper(helper);
        helper = null;
        File outFile = new File(TestHelper.SAMPLE_DB_PATH);
        outFile.delete();
    }
}
