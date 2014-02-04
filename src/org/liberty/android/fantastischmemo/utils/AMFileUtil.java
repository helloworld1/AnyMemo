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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;

import android.content.Context;

@Singleton
public class AMFileUtil {

    private Context mContext;

    private AMPrefUtil amPrefUtil;

    @Inject
    public AMFileUtil(Context context) {
        mContext = context;
    }

    @Inject
    public void setAmPrefUtil(AMPrefUtil amPrefUtil) {
        this.amPrefUtil = amPrefUtil;
    }

    public void deleteDbSafe(String filepath) {
        if (!new File(filepath).exists()) {
            return;
        }
        AnyMemoDBOpenHelperManager.forceRelease(filepath);


        // Also delete all the preference related to the db file.
        amPrefUtil.removePrefKeys(filepath);

        new File(filepath).delete();
    }

    public void deleteFileWithBackup(String filepath) throws IOException {
        if (!new File(filepath).exists()) {
            return;
        }

        String ext = FilenameUtils.getExtension(filepath);
        String nameWtihoutExt = FilenameUtils.removeExtension(filepath);
        String backFileName = nameWtihoutExt + ".backup." + ext;
        FileUtils.copyFile(new File(filepath), new File(backFileName));
        deleteDbSafe(filepath);
    }

    // Copy a file from asset to the dest file.
    public void copyFileFromAsset(String fileName, File dest) throws IOException {
        InputStream in = null;
        try {
            in = mContext.getResources().getAssets().open(fileName);
            FileUtils.copyInputStreamToFile(in, dest);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Find file in candidateDirs
     * @param candidateDirs, an array of dirs that could contain the file.
     * @return files found
     */
    public List<File> findFileInPaths(String fileName, String[] candidateDirs) {
        List<File> filesFound = new ArrayList<File>();
        for (String path : candidateDirs) {
            File file1 = new File(path + "/" + fileName);
            if (file1.exists()) {
                filesFound.add(file1);
            }
            File file2 = new File(path + "/" + FilenameUtils.getName(fileName));
            if (file2.exists()) {
                filesFound.add(file2);
            }
        }
        return filesFound;
    }
}
