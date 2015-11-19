/*
Copyright (C) 2013 Haowen Ning

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
package org.liberty.android.fantastischmemo.utils;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

@Singleton
public class AMPrefUtil {

    private SharedPreferences settings;

    private SharedPreferences.Editor editor;

    @Inject
    public AMPrefUtil(Context context) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settings.edit();
    }

    public int getSavedInt(String prefix, String key, int defaultValue) {
        return settings.getInt(prefix + key, defaultValue);
    }

    public void putSavedInt(String prefix, String key, int value) {
        editor.putInt(prefix + key, value);
        editor.commit();
    }

    public String getSavedString(String prefix, String key, String defaultValue) {
        return settings.getString(prefix + key, defaultValue);
    }

    public void putSavedString(String prefix, String key, String value) {
        editor.putString(prefix + key, value);
        editor.commit();
    }

    public void putSavedBoolean(String prefix, String key, boolean value) {
        editor.putBoolean(prefix + key, value);
        editor.commit();
    }

    public boolean getSavedBoolean(String prefix, String key, boolean defaultValue) {
        return settings.getBoolean(prefix + key, defaultValue);
    }

    /* Remove the pref key contain the string keyContains. */
    public void removePrefKeys(String keyContains) {
        Map<String,?> keys = settings.getAll();

        for (String key : keys.keySet()) {
            if (key.contains(keyContains)) {
                editor.remove(key);
            }
        }
        editor.commit();
    }
}
