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

package org.liberty.android.fantastischmemo.common;

import android.os.Environment;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/*
 * Class that defines the constants that is used in AnyMemo.
 */
public class AMEnv {
    private static final String TAG = AMEnv.class.getSimpleName();

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
    public final static String ANYMEMO_SERVICE_ENDPOINT = "https://anymemo.org";

    public final static String GDRIVE_SCOPE ="oauth2:https://www.googleapis.com/auth/plus.login https://docs.google.com/feeds/ https://docs.googleusercontent.com/ https://spreadsheets.google.com/feeds/ https://www.googleapis.com/auth/drive.file";

    // Dropbox oauth constants
    public final static String DROPBOX_REDIRECT_URI = "anymemo-dropbox://oauth";

    // Defined in AMSecrets.java
    public final static String DROPBOX_CONSUMER_KEY;
    public final static String DROPBOX_CONSUMER_SECRET;

    public final static String DROPBOX_OAUTH_VERSION = "1.0";
    public final static String DROPBOX_OAUTH_SIGNATURE_METHOD="PLAINTEXT";
    
    // Quizlet oauth constants
    public final static String QUIZLET_CLIENT_ID;
    public final static String QUIZLET_CLIENT_SECRET;
    public final static String QUIZLET_REDIRECT_URI = "anymemo-quizlet://oauth";
    public final static String QUIZLET_API_ENDPOINT = "https://api.quizlet.com/2.0";

    // Cram (Formally FlashcardExchange)
    public final static String CRAM_API_ENDPOINT = "https://api.cram.com/v2";
    public final static String CRAM_CLIENT_ID;
    public final static String CRAM_CLIENT_SECRET;


    // Load values from AMSecrets.
    static {
        Map<String, String> secretValuesMap = new HashMap<String, String>(5);

        try {
            Class<?> secretClass = Class.forName("org.liberty.android.fantastischmemo.common.AMSecrets");
            for (Field f : secretClass.getFields()) {
                if (f.getType().isAssignableFrom(String.class)) {
                    secretValuesMap.put(f.getName(), (String) f.get(null));
                }
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "AMSecrets class is not found. Please provide your own credentials and create AMSecrets file.", e);
            assert false;
        } catch (IllegalAccessException e) {
            Log.e(TAG, "AMSecrets can not be accessed.", e);
            assert false;
        }

        DROPBOX_CONSUMER_KEY = secretValuesMap.get("DROPBOX_CONSUMER_KEY");
        DROPBOX_CONSUMER_SECRET = secretValuesMap.get("DROPBOX_CONSUMER_SECRET");

        QUIZLET_CLIENT_ID = secretValuesMap.get("QUIZLET_CLIENT_ID");
        QUIZLET_CLIENT_SECRET = secretValuesMap.get("QUIZLET_CLIENT_SECRET");
        

        CRAM_CLIENT_ID = secretValuesMap.get("CRAM_CLIENT_ID");
        CRAM_CLIENT_SECRET = secretValuesMap.get("CRAM_CLIENT_SECRET");


        assert DROPBOX_CONSUMER_KEY != null;
        assert DROPBOX_CONSUMER_SECRET != null;

        assert QUIZLET_CLIENT_ID != null;
        assert QUIZLET_CLIENT_SECRET != null;

        assert CRAM_CLIENT_ID != null;
        assert CRAM_CLIENT_SECRET != null;

    }
}
