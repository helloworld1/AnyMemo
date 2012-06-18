package org.liberty.android.fantastischmemo.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.liberty.android.fantastischmemo.AMEnv;

import android.util.Log;

public class AMZipUtils {
	private static final int BUFFER_SIZE = 8192;
	private static final String TAG = "org.liberty.android.fantastischmemo.utils.AMZipUtils";

	public static void unZipFile(File file) throws Exception {
		BufferedOutputStream dest = null;
		BufferedInputStream ins = null;
		ZipEntry entry;

		try {
			ZipFile zipfile = new ZipFile(file);
			Enumeration<?> e = zipfile.entries();
			while(e.hasMoreElements()) {
				entry = (ZipEntry) e.nextElement();
				Log.d(TAG, "Extracting zip: " + entry);
				if(entry.isDirectory()){
					new File(AMEnv.DEFAULT_ROOT_PATH + "/" + entry.getName()).mkdir();
				} else {
					ins = new BufferedInputStream
							(zipfile.getInputStream(entry), BUFFER_SIZE);
					int count;
					byte data[] = new byte[BUFFER_SIZE];
					FileOutputStream fos = new FileOutputStream(AMEnv.DEFAULT_ROOT_PATH + "/" + entry.getName());
					dest = new BufferedOutputStream(fos, BUFFER_SIZE);
					while ((count = ins.read(data, 0, BUFFER_SIZE)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
					ins.close();
				}
			}
		} catch(Exception e) {
			throw new Exception("Exception when extracting zip file: " + file, e);
		}
	}

}
