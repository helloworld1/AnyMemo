package org.liberty.android.fantasisichmemo;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class OptionScreen extends PreferenceActivity{
	public static final String PREFS_NAME = "FantasisichMemoPrefs";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.option_screen);
    }
        

}
