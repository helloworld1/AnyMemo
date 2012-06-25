package org.liberty.android.fantastischmemo.domain;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

public class SchedulingAlgorithmParameters {

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    public SchedulingAlgorithmParameters(Context context) {
    	settings = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settings.edit();
    }

    public float getInitialInterval(int grade) {
    	// TODO: Shouldn't be 0.0 for default value
        return settings.getFloat("initial_grading_interval_" + grade, 0.0f);
    }
}
