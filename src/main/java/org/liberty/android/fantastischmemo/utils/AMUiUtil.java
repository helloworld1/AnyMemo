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
package org.liberty.android.fantastischmemo.utils;

import javax.inject.Inject;

import android.content.Context;
import android.util.TypedValue;

/*
 * Util that is related to user interface.
 */
public class AMUiUtil {

    private Context mContext;

    @Inject
    public AMUiUtil(Context context) {
        mContext = context;
    }

    /*
     * Convert Pixel unit to DP unit.
     */
    public int convertPxToDp(int px) {
        return (int)(px * mContext.getResources().getDisplayMetrics().density);
    }

    /*
     * Convert DP to Pixel
     */
    public int convertDpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) dp, mContext.getResources().getDisplayMetrics());
    }

}
