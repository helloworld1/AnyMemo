package org.liberty.android.fantastischmemo;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class OptionScreen extends PreferenceActivity{
	public static final String PREFS_NAME = "fantastischhMemoPrefs";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.option_screen);
    }
        

}
