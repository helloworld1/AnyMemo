package org.liberty.android.fantastischmemo.test;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

public class AbstractPreferencesTest extends AndroidTestCase {
    protected SharedPreferences settings;

    protected SharedPreferences.Editor editor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    	settings = PreferenceManager.getDefaultSharedPreferences(getContext());
    	editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        editor.clear();
        editor.commit();
    }

}
