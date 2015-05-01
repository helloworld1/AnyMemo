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
package org.liberty.android.fantastischmemo.utils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

public class AMStringUtils {

    // Strip the HTML from the text and return plain text
    public static String stripHTML(String htmlText) {
        // Replace break
        String processed_str = htmlText.replaceAll("\\<br\\>", "" );
        // Remove HTML
        processed_str = processed_str.replaceAll("\\<.*?>", "");
        // Remove () [] and their content
        processed_str = processed_str.replaceAll("\\[.*?\\]", "");
        // Remove the XML special character
        processed_str = processed_str.replaceAll("\\[.*?\\]", "");
        return processed_str.trim();
    }

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
        assert s != null : "Verify Null string";
        Pattern htmlPattern1 = Pattern.compile("<[a-zA-Z]+[0-9]*(\\s[a-zA-Z]+[0-9]*=.*)*\\s*/??>");
        Pattern htmlPattern2 = Pattern.compile("&#?[a-z0-9]+;");
        Matcher m1 = htmlPattern1.matcher(s);
        Matcher m2 = htmlPattern2.matcher(s);
        return m1.find() || m2.find();
    }

    /* Get the EnumSet from a string in format "A,B,C" */
    public static <E extends Enum<E>> EnumSet<E> getEnumSetFromString(Class<E> enumType, String enumString) {
        EnumSet<E> es = EnumSet.noneOf(enumType);

        if (!Strings.isNullOrEmpty(enumString)) {
            String[] split = enumString.split(",");
            for (String s : split) {
                es.add(Enum.valueOf(enumType, s));
            }
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

    /* Escape the text in XML */
    public static String encodeXML(final String text) {
        String outText = text.replaceAll("&", "&amp;");
        outText = outText.replaceAll("<", "&lt;");
        outText = outText.replaceAll(">", "&gt;");
        outText = outText.replaceAll("'", "&apos;");
        outText = outText.replaceAll("\"", "&quot;");
        return outText;
    }

    public static List<String> findFileInCardText(String cardText, String[] fileExtensions){

        List<String> filesFound = new ArrayList<String>();
        if (fileExtensions == null || fileExtensions.length == 0) {
            assert false : "fileExtensions should never be empty or null";
            return filesFound;
        }

        StringBuilder extensionPatternBuilder = new StringBuilder();

        // File name pattern
        extensionPatternBuilder.append("[A-Za-z0-9_-]+");

        // extension pattern
        extensionPatternBuilder.append("\\.(");
        for (int i = 0; i < fileExtensions.length; i++) {
            // The format is ext1|ext2|ext3 so the first occurance
            // does not have a |.
            if (i == 0) {
                extensionPatternBuilder.append(fileExtensions[i]);
            } else {
                extensionPatternBuilder.append("|" + fileExtensions[i]);
            }
        }
        extensionPatternBuilder.append(")");

        // The regex here should match the file types in SUPPORTED_AUDIO_FILE_TYPE
        Pattern p = Pattern.compile(extensionPatternBuilder.toString());
        Matcher m = p.matcher(cardText);
        while (m.find()){
            filesFound.add(m.group());
        }
        return filesFound;
    }

}
