/*
Copyright (C) 2010 Haowen Ning

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
package org.liberty.android.fantastischmemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Date;
import java.util.EnumSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AMUtil {
    public static boolean isInteger(String s){
        try{
            Integer.parseInt(s);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public static boolean isHTML(String s){
        Pattern htmlPattern1 = Pattern.compile("<[a-zA-Z]+[0-9]*(\\s[a-zA-Z]+[0-9]*=.*)*\\s*/??>");
        Pattern htmlPattern2 = Pattern.compile("&#?[a-z0-9]+;");
        Matcher m1 = htmlPattern1.matcher(s);
        Matcher m2 = htmlPattern2.matcher(s);
        return m1.find() || m2.find();
    }

	public static void copyFile(String source, String dest) throws IOException{
        File sourceFile = new File(source);
        File destFile = new File(dest);
		
        destFile.createNewFile();
		InputStream in = new FileInputStream(sourceFile);
		OutputStream out = new FileOutputStream(destFile);
		
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

    /* Get the file name from the path name */
    public static String getFilenameFromPath(String path) {
        String[] splitted = path.split("/");
        return splitted[splitted.length - 1];
    }

    public static String getDirectoryFromPath(String path) {
        return new File(path).getParent();
    }

    /* Get the EnumSet from a string in format "A,B,C" */
    public static <E extends Enum<E>> EnumSet<E> getEnumSetFromString(Class<E> enumType, String enumString) {
        EnumSet<E> es = EnumSet.noneOf(enumType);

        String[] split = enumString.split(",");
        for (String s : split) {
            es.add(Enum.valueOf(enumType, s));

        }
        return es;
    }

    /* Get the String a string in format "A,B,C" from EnumSet */
    public static <E extends Enum<E>> String getStringFromEnumSet(EnumSet<E> e) {
        String res = "";
        for (E cf : e) {
            res = res + cf.toString() + ",";
        }
        if (res.length() != 0) {
            res = res.substring(0, res.length() - 1);
        }
        return res;
    }

    /* Get the interval in days between two date */
    public float diffDate(Date d1, Date d2) {
        return 0.0f;
    }
}
