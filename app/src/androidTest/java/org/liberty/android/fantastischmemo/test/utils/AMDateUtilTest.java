package org.liberty.android.fantastischmemo.test.utils;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.utils.AMDateUtil;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class AMDateUtilTest extends AndroidTestCase {

    @SmallTest
    public void testConvertDayIntervalToDisplayString() {
        AMDateUtil amDateUtil = new AMDateUtil(getContext());
        assertEquals("5.0 " + getContext().getString(R.string.day_text), amDateUtil.convertDayIntervalToDisplayString(5.00));
        assertEquals("3.2 " + getContext().getString(R.string.day_text), amDateUtil.convertDayIntervalToDisplayString(3.22));
        assertEquals("3.3 " + getContext().getString(R.string.day_text), amDateUtil.convertDayIntervalToDisplayString(3.26));
        assertEquals("1.8 " + getContext().getString(R.string.week_text), amDateUtil.convertDayIntervalToDisplayString(12.345));
        assertEquals("1.6 " + getContext().getString(R.string.year_text), amDateUtil.convertDayIntervalToDisplayString(594.322));
    }
}
