
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

import java.util.Date;

import org.apache.mycommons.lang3.time.DateUtils;

public class AMDateUtil {
    /* Difference in days between date1 and date2*/
	public static double diffDate(Date date1, Date date2){
        double date1s = date1.getTime();
        double date2s = date2.getTime();
        return ((double)(date2s - date1s)) / DateUtils.MILLIS_PER_DAY; 
	}
}
