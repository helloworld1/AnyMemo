package org.liberty.android.fantastischmemo.test.integration;

import org.liberty.android.fantastischmemo.ui.AnyMemo;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.KeyEvent;

import com.robotium.solo.Solo;

public class AnyMemoActivityTabsTest extends ActivityInstrumentationTestCase2<AnyMemo> {

    protected AnyMemo mActivity;

    private Solo solo;

    @SuppressWarnings("deprecation")
    public AnyMemoActivityTabsTest() {
        super("org.liberty.android.fantastischmemo", AnyMemo.class);
    }

    public void setUp() {
        mActivity = this.getActivity();
        solo = new Solo(getInstrumentation(), mActivity);
    }


    @LargeTest
    public void testCyclingTabs() {
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);

        sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
        sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
        sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
    }

    public void tearDown() throws Exception {
        try {
            solo.finishOpenedActivities();
            solo.sleep(2000);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        super.tearDown();
        solo = null;
    }
}
