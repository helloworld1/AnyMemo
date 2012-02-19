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
 * Class that defines the contants that is used in AnyMemo.
 */
public class AMEnv {
    public static String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static String DEFAULT_POSTFIX= "/anymemo/";
    private static String DEFAULT_AUDIO_POSTFIX = "/voice/";
    private static String DEFAULT_IMAGE_POSTFIX = "/images/";
    public static String DEFAULT_ROOT_PATH = EXTERNAL_STORAGE_PATH + DEFAULT_POSTFIX;
    public static String DEFAULT_AUDIO_PATH = DEFAULT_ROOT_PATH + DEFAULT_AUDIO_POSTFIX;
    public static String DEFAULT_IMAGE_PATH = DEFAULT_ROOT_PATH + DEFAULT_IMAGE_POSTFIX;
    public static String DEFAULT_DB_NAME= "french-body-parts.db";
    public static String EMPTY_DB_NAME= "empty.db";
}
