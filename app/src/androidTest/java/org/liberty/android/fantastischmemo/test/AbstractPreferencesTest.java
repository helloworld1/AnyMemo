package org.liberty.android.fantastischmemo.test;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;

public class AbstractPreferencesTest extends BaseTest {
    protected SharedPreferences settings;

    protected SharedPreferences.Editor editor;

    @Before
    public void setUp() throws Exception {
    	settings = PreferenceManager.getDefaultSharedPreferences(getContext());
    	editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    @After
    public void tearDown() throws Exception {
        editor.clear();
        editor.commit();
    }

}
