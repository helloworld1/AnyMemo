/*
Copyright (C) 2012 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.ui;

import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.R;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.res.Configuration;

public class OptionScreen extends PreferenceActivity {
	public static final String PREFS_NAME = "fantastischhMemoPrefs";

    private static final String CUSTOMIZE_SCHEDULING_ALGORITHM_KEY
        = "customize_scheduling_algorithm_key";

    // addPreferencesFromResource is used for compatibility
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.option_screen);

        // Lauch algorithm customization screen if click on the specific
        // preference item.
        Preference algorithmCustomizationPreference = findPreference(CUSTOMIZE_SCHEDULING_ALGORITHM_KEY);
        algorithmCustomizationPreference.setOnPreferenceClickListener(algorithmCustomizationPreferenceOnClickListener);

    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        /* set if the orientation change is allowed */
        if(!settings.getBoolean(AMPrefKeys.ALLOW_ORIENTATION_KEY, true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private Preference.OnPreferenceClickListener algorithmCustomizationPreferenceOnClickListener
        = new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference p) {
                new AlertDialog.Builder(OptionScreen.this)
                    .setTitle(R.string.warning_text)
                    .setMessage(R.string.customize_scheduling_algorithm_warning)
                    .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(OptionScreen.this, AlgorithmCustomizationScreen.class);
                            startActivity(intent);
						}
                    })
                    .setNegativeButton(R.string.cancel_text, null)
                    .show();
                return true;
			}

        };
}
