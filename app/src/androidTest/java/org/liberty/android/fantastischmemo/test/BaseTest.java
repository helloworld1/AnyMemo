package org.liberty.android.fantastischmemo.test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;

public class BaseTest {
    public Context getContext() {
        return InstrumentationRegistry.getContext();
    }

    public Context getTargetContext() {
        return InstrumentationRegistry.getTargetContext();
    }
}
