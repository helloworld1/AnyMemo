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
import org.liberty.android.fantastischmemo.domain.SchedulingAlgorithmParameters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class AlgorithmCustomizationScreen extends PreferenceActivity {
    private static final String RESET_CUSTOMIZED_ALGORITHM_KEY
        = "reset_customized_scheduling_algorithm";

    // There is no replacement for addPreferencesFromResource
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.algorithm_customization_screen);

    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        Preference resetPreference = findPreference(RESET_CUSTOMIZED_ALGORITHM_KEY);
        resetPreference.setOnPreferenceClickListener(resetPreferenceOnClickListener);

        /* set if the orientation change is allowed */
        if(!settings.getBoolean(AMPrefKeys.ALLOW_ORIENTATION_KEY, true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private Preference.OnPreferenceClickListener resetPreferenceOnClickListener
        = new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference p) {
                new AlertDialog.Builder(AlgorithmCustomizationScreen.this)
                    .setTitle(R.string.warning_text)
                    .setMessage(R.string.customize_scheduling_algorithm_warning)
                    .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
                            finish();
                            SchedulingAlgorithmParameters parameters = new SchedulingAlgorithmParameters(AlgorithmCustomizationScreen.this);
                            parameters.reset();
						}
                    })
                    .setNegativeButton(R.string.cancel_text, null)
                    .show();
                return true;
			}

        };
}
