package org.liberty.android.fantastischmemo.domain;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

public class SchedulingAlgorithmParameters {

    private SharedPreferences settings;

    private SharedPreferences.Editor editor;

    private static float[] defaultInitialIntervals = {0.0f, 0.0f, 1.0f, 3.0f, 4.0f, 5.0f};

    private static float defaultMinimalInterval = 0.9f;

    private static float[] defaultFailedGradingIntervals = {0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f};

    private static float defaultInitialEasiness = 2.5f;

    private static float defaultMinimalEasiness = 1.3f;

    private static float[] defaultEasinessIncrementals = {0.0f, 0.0f, -0.16f, -0.14f, 0.0f, 0.10f};

    private static boolean defaultEnableNoise = true;

    public SchedulingAlgorithmParameters(Context context) {
    	settings = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settings.edit();
    }

    public float getInitialInterval(int grade) {
        assert grade < defaultInitialIntervals.length;
        return settings.getFloat("initial_grading_interval_" + grade, defaultInitialIntervals[grade]);
    }

    public float getMinimalInterval() {
        return settings.getFloat("minimal_interval", defaultMinimalInterval);
    }

    public float getFailedGradingInterval(int grade) {
        assert grade < defaultFailedGradingIntervals.length;
        return settings.getFloat("failed_grading_interval_" + grade, defaultFailedGradingIntervals[grade]);
    }

    public float getInitialEasiness() {
        return settings.getFloat("initial_easiness", defaultInitialEasiness);
    }

    public float getMinimalEasiness() {
        return settings.getFloat("minimal_easiness", defaultMinimalEasiness);
    }

    public float getEasinessIncremental(int grade) {
        assert grade < defaultFailedGradingIntervals.length;
        return settings.getFloat("easiness_incremental_" + grade, defaultEasinessIncrementals[grade]);
    }

    public boolean getEnableNoise() {
        return settings.getBoolean("enable_noise", defaultEnableNoise);
    }

    /*
     * Reset all the scheduling algorithm parameters to default.
     */
    public void reset() {
        int[] grades = {0, 1, 2, 3, 4, 5};
        for (int grade : grades) {
            editor.putFloat("initial_grading_interval_" + grade, defaultInitialIntervals[grade]);
            editor.putFloat("failed_grading_interval_" + grade, defaultFailedGradingIntervals[grade]);
            editor.putFloat("easiness_incremental_" + grade, defaultEasinessIncrementals[grade]);
        }
        editor.putBoolean("enable_noise", defaultEnableNoise);
        editor.putFloat("minimal_interval", defaultMinimalInterval);
        editor.putFloat("initial_easiness", defaultInitialEasiness);
        editor.putFloat("minimal_easiness", defaultMinimalEasiness);
        editor.putString("learning_queue_size", "10");

        editor.commit();
    }

}
