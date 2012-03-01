package org.liberty.android.fantastischmemo.test.ui;

import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import android.test.ActivityInstrumentationTestCase2;

import android.view.KeyEvent;

import android.widget.ListView;
import android.widget.TabHost;

public class AnyMemoTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    public AnyMemoTest() {
        super("org.liberty.android.fantastischmemo.ui", AnyMemo.class);
    }

    private TabHost tabHost;

    private ListView recentList;

    public void setUp() {
        mActivity = this.getActivity();
        tabHost = (TabHost)mActivity.findViewById(android.R.id.tabhost);
        recentList = (ListView)mActivity.findViewById(R.id.recent_open_list);
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

    public void testRecentList() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                recentList.requestFocus();
            }
        });

        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
    }

}
