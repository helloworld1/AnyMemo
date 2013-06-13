/*
Copyright (C) 2010 Haowen Ning, Xiaoyu Shi

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

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.AMPrefKeys;
import org.liberty.android.fantastischmemo.domain.Option;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/* This class handles the operations on recent list */
public class RecentListUtil {
    private int recentLength = 7;

    private Option option;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    @Inject
    public RecentListUtil(Context context, Option option) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        editor = settings.edit();
        recentLength = option.getRecentCount();
        this.option = option;
    }

    public String getRecentDBPath() {
        return trimPath(settings.getString(AMPrefKeys.getRecentPathKey(0), null));
    }

    public String[] getAllRecentDBPath() {
        // TODO: Reload the recentLength from user option.
        // FIXME: temp hack, need re-write, don't need to get it again.
        recentLength = option.getRecentCount();

        String[] ret = new String[recentLength];

        for(int i = 0; i < recentLength; i++){
            ret[i] = trimPath(settings.getString(AMPrefKeys.getRecentPathKey(i), null));
        }

        return ret;
    }

    public void clearRecentList() {
        for(int i = 0; i < recentLength; i++){
            editor.putString(AMPrefKeys.getRecentPathKey(i), null);
        }
        editor.commit();
    }

    public void deleteFromRecentList(String dbpath){
        dbpath = trimPath(dbpath);
        String[] allPaths = getAllRecentDBPath();
        clearRecentList();
        for(int i = 0, counter = 0; i < recentLength; i++){
            if(allPaths[i] == null || allPaths[i].equals(dbpath)) {
                continue;
            } else {
                editor.putString(AMPrefKeys.getRecentPathKey(counter), allPaths[i]);
                counter++;
            }
        }
        editor.commit();
    }

    public void addToRecentList(String dbpath){
        dbpath = trimPath(dbpath);
        deleteFromRecentList(dbpath);
        String[] allPaths = getAllRecentDBPath();
        for(int i = recentLength - 1; i >= 1; i--){
            editor.putString(AMPrefKeys.getRecentPathKey(i), allPaths[i - 1]);
        }
        editor.putString(AMPrefKeys.getRecentPathKey(0), dbpath);
        editor.commit();
    }

    private static String trimPath(String path){
        return FilenameUtils.normalize(path);
    }
}


