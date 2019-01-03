package org.liberty.android.fantastischmemo.test;

import android.content.Context;
import androidx.test.InstrumentationRegistry;

public class BaseTest {
    public Context getContext() {
        return InstrumentationRegistry.getContext();
    }

    public Context getTargetContext() {
        return InstrumentationRegistry.getTargetContext();
    }
}
