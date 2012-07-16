/*
Copyright (C) 2010 Haowen Ning
Modified by Xiaoyu Shi

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

import org.apache.mycommons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.domain.Option;

import android.content.Context;
import android.util.Log;

/* This class handles the operations on recent list */
public class RecentListUtil {
	private static final String TAG = "RecentListUtilTag";
    private int recentLength = 7;
    private Option option;
    
    public RecentListUtil(Context context) {
    	option = new Option(context);
    	recentLength = option.getSettings().getInt("recent_count", 7);
    }
    
    public String getRecentDBPath() {
        return trimPath(option.getSettingString("recentdbpath0", null));
    }

    public String[] getAllRecentDBPath() {
    	// Reload the recentLength from user option.
    	recentLength = option.getSettings().getInt("recent_count", recentLength);
        String[] ret = new String[recentLength];
        
        for(int i = 0; i < recentLength; i++){
            ret[i] = trimPath(option.getSettingString("recentdbpath" + i, null));
        }
        
        return ret;
    }
    
    public void clearRecentList() {
        for(int i = 0; i < recentLength; i++){
            option.setEditorString("recentdbpath" + i, null);
        }
    }

    public void deleteFromRecentList(String dbpath){
        dbpath = trimPath(dbpath);
        String[] allPaths = getAllRecentDBPath();
        clearRecentList();
        for(int i = 0, counter = 0; i < recentLength; i++){
            if(allPaths[i] == null || allPaths[i].equals(dbpath)){
                continue;
            }
            else{
                option.setEditorString("recentdbpath" + counter, allPaths[i]);
                counter++;
            }
        }
    }

    public void addToRecentList(String dbpath){
        dbpath = trimPath(dbpath);
        deleteFromRecentList(dbpath);
        String[] allPaths = getAllRecentDBPath();
        for(int i = recentLength - 1; i >= 1; i--){
            option.setEditorString("recentdbpath" + i, allPaths[i - 1]);
        }
        option.setEditorString("recentdbpath" + 0, dbpath);
    }

    private static String trimPath(String path){
        return FilenameUtils.normalize(path);
    }
}


