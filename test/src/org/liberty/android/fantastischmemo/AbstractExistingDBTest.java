package org.liberty.android.fantastischmemo;

import java.io.File;
import java.io.InputStream;

import org.apache.mycommons.io.FileUtils;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;

import android.content.Context;


import android.test.ActivityInstrumentationTestCase2;

public class AbstractExistingDBTest extends ActivityInstrumentationTestCase2<InstrumentationActivity> {
    protected InstrumentationActivity mActivity;  // the activity under test

    protected AnyMemoDBOpenHelper helper;

    public AbstractExistingDBTest() {
        super("org.liberty.android.fantastischmemo", InstrumentationActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = this.getActivity();
        Context testContext = getInstrumentation().getContext();
        InputStream in = testContext.getResources().getAssets().open(AMEnv.DEFAULT_DB_NAME);
        FileUtils.copyInputStreamToFile(in, new File("/sdcard/french-body-parts.db"));
        in.close();
        helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, "/sdcard/french-body-parts.db");
    }

    @Override
    protected void tearDown() throws Exception {
        AnyMemoDBOpenHelperManager.releaseHelper("/sdcard/french-body-parts.db");
        mActivity.finish();
    }
}
