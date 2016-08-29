package org.liberty.android.fantastischmemo.test.utils;

import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.liberty.android.fantastischmemo.test.AbstractPreferencesTest;
import org.liberty.android.fantastischmemo.utils.AMPrefUtil;

import static org.junit.Assert.assertEquals;

public class AMPrefUtilTest extends AbstractPreferencesTest {

    private AMPrefUtil amPrefUtil;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        amPrefUtil = new AMPrefUtil(getContext());
    }

    @SmallTest
    @Test
    public void testSavedIdAndGetId() {
        amPrefUtil.putSavedInt("Prefix", "key", 12345);
        assertEquals(12345, amPrefUtil.getSavedInt("Prefix", "key", 0));
    }

    @SmallTest
    @Test
    public void testGetIdUsingDefaultValue() {
        assertEquals(54321, amPrefUtil.getSavedInt("Prefix", "key", 54321));
    }

    @SmallTest
    @Test
    public void testRemovePrefKeys() {
        amPrefUtil.putSavedInt("Prefix", "key1", 10);
        amPrefUtil.putSavedInt("Prefix", "key2", 20);
        amPrefUtil.putSavedInt("Prefix", "key3", 30);
        amPrefUtil.putSavedInt("Prefix", "eye3", 40);
        amPrefUtil.removePrefKeys("key2");
        assertEquals(10, amPrefUtil.getSavedInt("Prefix", "key1", 0));

        // Default value because it is deleted
        assertEquals(0, amPrefUtil.getSavedInt("Prefix", "key2", 0));

        // This should remove all keys containing "key"
        amPrefUtil.removePrefKeys("key");

        assertEquals(0, amPrefUtil.getSavedInt("Prefix", "key1", 0));
        assertEquals(0, amPrefUtil.getSavedInt("Prefix", "key3", 0));

        // This key should not be deleted
        assertEquals(40, amPrefUtil.getSavedInt("Prefix", "eye3", 0));

    }

}

