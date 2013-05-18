package org.liberty.android.fantastischmemo.test.integration;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import android.test.ActivityInstrumentationTestCase2;

import android.view.KeyEvent;

import android.widget.TabHost;

public class AnyMemoActivityTabsTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public AnyMemoActivityTabsTest() {
        super("org.liberty.android.fantastischmemo", AnyMemo.class);
    }

    private TabHost tabHost;

    public void setUp() {
        mActivity = this.getActivity();
        tabHost = (TabHost)mActivity.findViewById(android.R.id.tabhost);
    }


    public void testCyclingTabs() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                tabHost.requestFocus();
            }
        });
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);

        sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
        sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
        sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
    }
}
