package org.liberty.android.fantastischmemo.test.utils;

import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.test.AbstractPreferencesTest;
import org.liberty.android.fantastischmemo.utils.RecentListUtil;

import android.test.suitebuilder.annotation.SmallTest;

public class RecentListUtilTest extends AbstractPreferencesTest {

    @SmallTest
    public void testAddRecentListWithinLimit() {
        RecentListUtil recentListUtil = new RecentListUtil(getContext());
        recentListUtil.addToRecentList("/sdcard/a.db");
        assertEquals("/sdcard/a.db", recentListUtil.getRecentDBPath());

        recentListUtil.addToRecentList("/sdcard/b.db");
        String[] recentList = recentListUtil.getAllRecentDBPath();
        assertEquals("/sdcard/b.db", recentList[0]);
        assertEquals("/sdcard/a.db", recentList[1]);
    }

    @SmallTest
    public void testAddRecentListLargerThanLimit() {
        // Set the limit to 3.
        editor.putInt(AMPrefKeys.RECENT_COUNT_KEY, 3);
        editor.commit();

        // Add 5 dbs
        RecentListUtil recentListUtil = new RecentListUtil(getContext());
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
    public void testDeleteRecentItem() {
        // Add 5 dbs
        RecentListUtil recentListUtil = new RecentListUtil(getContext());
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
    public void testAddEquivalentPath() {
        // Equivalent path are considered the same
        RecentListUtil recentListUtil = new RecentListUtil(getContext());
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
    public void testClearRecentList() {
        RecentListUtil recentListUtil = new RecentListUtil(getContext());
        recentListUtil.addToRecentList("/sdcard/a.db");
        recentListUtil.addToRecentList("/sdcard/b.db");

        recentListUtil.clearRecentList();
        assertNull(recentListUtil.getRecentDBPath());
    }
}
