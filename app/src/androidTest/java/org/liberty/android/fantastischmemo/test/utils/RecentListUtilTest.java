package org.liberty.android.fantastischmemo.test.utils;

import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.liberty.android.fantastischmemo.entity.Option;
import org.liberty.android.fantastischmemo.test.AbstractPreferencesTest;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecentListUtilTest extends AbstractPreferencesTest {

    @SmallTest
    @Test
    public void testAddRecentListWithinLimit() {
        Option mockOption = mock(Option.class);
        when(mockOption.getRecentCount())
            .thenReturn(7);
        RecentListUtil recentListUtil = new RecentListUtil(getContext(), mockOption);
        recentListUtil.addToRecentList("/sdcard/a.db");
        assertEquals("/sdcard/a.db", recentListUtil.getRecentDBPath());

        recentListUtil.addToRecentList("/sdcard/b.db");
        String[] recentList = recentListUtil.getAllRecentDBPath();
        assertEquals("/sdcard/b.db", recentList[0]);
        assertEquals("/sdcard/a.db", recentList[1]);
    }

    @SmallTest
    @Test
    public void testAddRecentListLargerThanLimit() {
        // Mock recent count limit to 3.
        Option mockOption = mock(Option.class);
        when(mockOption.getRecentCount())
            .thenReturn(3);

        // Add 5 dbs
        RecentListUtil recentListUtil = new RecentListUtil(getContext(), mockOption);
        recentListUtil.addToRecentList("/sdcard/1.db");
        recentListUtil.addToRecentList("/sdcard/2.db");
        recentListUtil.addToRecentList("/sdcard/3.db");
        recentListUtil.addToRecentList("/sdcard/4.db");
        recentListUtil.addToRecentList("/sdcard/5.db");

        // Only 3 should be in recent list
        String[] recentList = recentListUtil.getAllRecentDBPath();
        assertEquals(3, recentList.length);
        assertEquals("/sdcard/5.db", recentList[0]);
        assertEquals("/sdcard/4.db", recentList[1]);
        assertEquals("/sdcard/3.db", recentList[2]);
    }

    @SmallTest
    @Test
    public void testDeleteRecentItem() {
        Option mockOption = mock(Option.class);
        when(mockOption.getRecentCount())
            .thenReturn(7);
        // Add 5 dbs
        RecentListUtil recentListUtil = new RecentListUtil(getContext(), mockOption);
        recentListUtil.addToRecentList("/sdcard/1.db");
        recentListUtil.addToRecentList("/sdcard/2.db");
        recentListUtil.addToRecentList("/sdcard/3.db");
        recentListUtil.addToRecentList("/sdcard/4.db");
        recentListUtil.addToRecentList("/sdcard/5.db");

        recentListUtil.deleteFromRecentList("/sdcard/5.db");
        assertEquals("/sdcard/4.db", recentListUtil.getRecentDBPath());

        recentListUtil.deleteFromRecentList("/sdcard/2.db");
        assertEquals("/sdcard/4.db", recentListUtil.getRecentDBPath());

        String[] recentList = recentListUtil.getAllRecentDBPath();
        assertEquals("/sdcard/4.db", recentList[0]);
        assertEquals("/sdcard/3.db", recentList[1]);
        assertEquals("/sdcard/1.db", recentList[2]);
    }

    @SmallTest
    @Test
    public void testAddEquivalentPath() {
        Option mockOption = mock(Option.class);
        when(mockOption.getRecentCount())
            .thenReturn(7);
        // Equivalent path are considered the same
        RecentListUtil recentListUtil = new RecentListUtil(getContext(), mockOption);
        recentListUtil.addToRecentList("/sdcard/1.db");
        recentListUtil.addToRecentList("/sdcard//1.db");
        recentListUtil.addToRecentList("//sdcard/1.db");
        recentListUtil.addToRecentList("/sdcard/1.db");
        recentListUtil.addToRecentList("/sdcard/./2.db");
        recentListUtil.addToRecentList("/sdcard//2.db");

        String[] recentList = recentListUtil.getAllRecentDBPath();
        assertEquals("/sdcard/2.db", recentList[0]);
        assertEquals("/sdcard/1.db", recentList[1]);
    }

    @SmallTest
    @Test
    public void testClearRecentList() {
        Option mockOption = mock(Option.class);
        when(mockOption.getRecentCount())
            .thenReturn(7);
        RecentListUtil recentListUtil = new RecentListUtil(getContext(), mockOption);
        recentListUtil.addToRecentList("/sdcard/a.db");
        recentListUtil.addToRecentList("/sdcard/b.db");

        recentListUtil.clearRecentList();
        assertNull(recentListUtil.getRecentDBPath());
    }
}
