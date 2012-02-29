package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.InstrumentationActivity;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import android.test.ActivityInstrumentationTestCase2;

import android.view.KeyEvent;

import android.widget.TabHost;

public class AnyMemoTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected InstrumentationActivity mActivity;

    public AnyMemoTest() {
        super("org.liberty.android.fantastischmemo.ui", AnyMemo.class);
    }

    private TabHost tabHost = (TabHost)mActivity.findViewById(android.R.id.tabhost);

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
