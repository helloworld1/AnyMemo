package org.liberty.android.fantastischmemo.test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

public class BaseTest {
    public Context getContext() {
        return InstrumentationRegistry.getContext();
    }

    public Context getTargetContext() {
        return InstrumentationRegistry.getTargetContext();
    }
}
