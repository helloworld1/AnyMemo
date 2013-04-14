/*
Copyright (C) 2012 Haowen Ning

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

import android.os.Environment;

/*
 * Class that defines the constants that is used in AnyMemo.
 */
public class AMEnv {
    public static final String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final static String DEFAULT_POSTFIX= "/anymemo/";
    private final static String DEFAULT_AUDIO_POSTFIX = "voice/";
    private final static String DEFAULT_IMAGE_POSTFIX = "images/";
    private final static String DEFAULT_TMP_POSTFIX = "tmp/";
    public final static String DEFAULT_ROOT_PATH = EXTERNAL_STORAGE_PATH + DEFAULT_POSTFIX;
    public final static String DEFAULT_AUDIO_PATH = DEFAULT_ROOT_PATH + DEFAULT_AUDIO_POSTFIX;
    public final static String DEFAULT_IMAGE_PATH = DEFAULT_ROOT_PATH + DEFAULT_IMAGE_POSTFIX;
    public final static String DEFAULT_TMP_PATH = DEFAULT_ROOT_PATH + DEFAULT_TMP_POSTFIX;
    public final static String DEFAULT_DB_NAME= "french-body-parts.db";
    public final static String EMPTY_DB_NAME= "empty.db";
    public final static String GOOGLE_CLIENT_ID = "45533559525.apps.googleusercontent.com";
    public final static String GOOGLE_CLIENT_SECRET = "74rz27lrTr9mWNnipgxXwtEd";
    public final static String GOOGLE_REDIRECT_URI = "http://localhost";
    public final static String GDRIVE_SCOPE ="https://docs.google.com/feeds/ https://docs.googleusercontent.com/ https://spreadsheets.google.com/feeds/";
    
    
    // Dropbox oauth constants
    public final static String DROPBOX_REDIRECT_URI = "https://localhost";
    public final static String DROPBOX_CONSUMER_KEY = "q2rclqr44ux8pe7";
    public final static String DROPBOX_CONSUMER_SECRET = "bmgikjefor073dh";
    public final static String DROPBOX_OAUTH_VERSION = "1.0";
    public final static String DROPBOX_OAUTH_SIGNATURE_METHOD="PLAINTEXT";

}
