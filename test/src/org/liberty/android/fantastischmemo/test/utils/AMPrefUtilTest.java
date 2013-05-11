package org.liberty.android.fantastischmemo.test.utils;

import java.lang.reflect.Method;

import org.liberty.android.fantastischmemo.test.AbstractPreferencesTest;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;

import android.content.Context;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class AMPrefUtilTest extends AbstractPreferencesTest {

    private AMPrefUtil amPrefUtil;

    private Context testContext;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        amPrefUtil = new AMPrefUtil(getContext());
    }

    @SmallTest
    public void testSavedIdAndGetId() {
        amPrefUtil.setSavedId("Prefix", "key", 12345);
        assertEquals(12345, amPrefUtil.getSavedId("Prefix", "key", 0));
    }

    @SmallTest
    public void testGetIdUsingDefaultValue() {
        assertEquals(54321, amPrefUtil.getSavedId("Prefix", "key", 54321));
    }

    @SmallTest
    public void testRemovePrefKeys() {
        amPrefUtil.setSavedId("Prefix", "key1", 10);
        amPrefUtil.setSavedId("Prefix", "key2", 20);
        amPrefUtil.setSavedId("Prefix", "key3", 30);
        amPrefUtil.setSavedId("Prefix", "eye3", 40);
        amPrefUtil.removePrefKeys("key2");
        assertEquals(10, amPrefUtil.getSavedId("Prefix", "key1", 0));

        // Default value because it is deleted
        assertEquals(0, amPrefUtil.getSavedId("Prefix", "key2", 0));

        // This should remove all keys containing "key" 
        amPrefUtil.removePrefKeys("key");

        assertEquals(0, amPrefUtil.getSavedId("Prefix", "key1", 0));
        assertEquals(0, amPrefUtil.getSavedId("Prefix", "key3", 0));

        // This key should not be deleted
        assertEquals(40, amPrefUtil.getSavedId("Prefix", "eye3", 0));

    }

}

