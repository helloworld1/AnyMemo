package org.liberty.android.fantastischmemo;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.utils.AMUtil;


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
        AMUtil.copyFile("/sdcard/anymemo/french-body-parts.db", "/sdcard/french-body-parts.db");
        helper = AnyMemoDBOpenHelperManager.getHelper(mActivity, "/sdcard/french-body-parts.db");
    }

    @Override
    protected void tearDown() throws Exception {
        AnyMemoDBOpenHelperManager.releaseHelper("/sdcard/french-body-parts.db");
        mActivity.finish();
    }
}
