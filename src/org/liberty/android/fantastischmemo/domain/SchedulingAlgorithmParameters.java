package org.liberty.android.fantastischmemo.domain;

import javax.inject.Inject;

import org.liberty.android.fantastischmemo.AMPrefKeys;

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

    @Inject
    public SchedulingAlgorithmParameters(Context context) {
    	settings = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settings.edit();
    }

    public float getInitialInterval(int grade) {
        assert grade < defaultInitialIntervals.length;
        return settings.getFloat(AMPrefKeys.getInitialGradingIntervalKey(grade), defaultInitialIntervals[grade]);
    }

    public float getMinimalInterval() {
        return settings.getFloat(AMPrefKeys.MINIMAL_INTERVAL_KEY, defaultMinimalInterval);
    }

    public float getFailedGradingInterval(int grade) {
        assert grade < defaultFailedGradingIntervals.length;
        return settings.getFloat(AMPrefKeys.getFailedGradingIntervalKey(grade), defaultFailedGradingIntervals[grade]);
    }

    public float getInitialEasiness() {
        return settings.getFloat(AMPrefKeys.INITIAL_EASINESS_KEY, defaultInitialEasiness);
    }

    public float getMinimalEasiness() {
        return settings.getFloat(AMPrefKeys.MINIMAL_EASINESS_KEY, defaultMinimalEasiness);
    }

    public float getEasinessIncremental(int grade) {
        assert grade < defaultFailedGradingIntervals.length;
        return settings.getFloat(AMPrefKeys.getEasinessIncrementalKey(grade), defaultEasinessIncrementals[grade]);
    }

    public boolean getEnableNoise() {
        return settings.getBoolean(AMPrefKeys.ENABLE_NOISE_KEY, defaultEnableNoise);
    }

    /*
     * Reset all the scheduling algorithm parameters to default.
     */
    public void reset() {
        int[] grades = {0, 1, 2, 3, 4, 5};
        for (int grade : grades) {
            editor.putFloat(AMPrefKeys.getInitialGradingIntervalKey(grade), defaultInitialIntervals[grade]);
            editor.putFloat(AMPrefKeys.getFailedGradingIntervalKey(grade), defaultFailedGradingIntervals[grade]);
            editor.putFloat(AMPrefKeys.getEasinessIncrementalKey(grade), defaultEasinessIncrementals[grade]);
        }
        editor.putBoolean(AMPrefKeys.ENABLE_NOISE_KEY, defaultEnableNoise);
        editor.putFloat(AMPrefKeys.MINIMAL_INTERVAL_KEY, defaultMinimalInterval);
        editor.putFloat(AMPrefKeys.INITIAL_EASINESS_KEY, defaultInitialEasiness);
        editor.putFloat(AMPrefKeys.MINIMAL_EASINESS_KEY, defaultMinimalEasiness);
        editor.putString(AMPrefKeys.LEARN_QUEUE_SIZE_KEY, "10");

        editor.commit();
    }

}
