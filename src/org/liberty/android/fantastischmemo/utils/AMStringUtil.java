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

import org.liberty.android.fantastischmemo.R;

import android.content.Context;

public class AMStringUtil {

    private Context context;

    public AMStringUtil(Context context) {
        this.context = context;
    }

    // Interval: 12.3456 day -> "1.7 week", 4.76 -> "4.7 day"
    public String convertDayIntervalToDisplayString(double intervalInDay) {
        double[] dividers = {365, 30, 7, 1};
        String[] unitName = {context.getString(R.string.year_text),
            context.getString(R.string.month_text),
            context.getString(R.string.week_text),
            context.getString(R.string.day_text)};

        for (int i = 0; i < dividers.length; i++) {
            double divider = dividers[i];
                
            if ((intervalInDay / divider) >= 1.0 || i == (dividers.length - 1)) {
                return "" + Double.toString(((double)Math.round(intervalInDay / divider * 10)) / 10) + " " + unitName[i];
            }
        }
        return "";
    }

}
