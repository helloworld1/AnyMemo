package org.liberty.android.fantastischmemo.test.utils;

import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.test.BaseTest;
import org.liberty.android.fantastischmemo.utils.AMDateUtil;

import static org.junit.Assert.assertEquals;

public class AMDateUtilTest extends BaseTest {

    @SmallTest
    @Test
    public void testConvertDayIntervalToDisplayString() {
        AMDateUtil amDateUtil = new AMDateUtil(getTargetContext());
        assertEquals("5.0 " + getTargetContext().getString(R.string.day_text), amDateUtil.convertDayIntervalToDisplayString(5.00));
        assertEquals("3.2 " + getTargetContext().getString(R.string.day_text), amDateUtil.convertDayIntervalToDisplayString(3.22));
        assertEquals("3.3 " + getTargetContext().getString(R.string.day_text), amDateUtil.convertDayIntervalToDisplayString(3.26));
        assertEquals("1.8 " + getTargetContext().getString(R.string.week_text), amDateUtil.convertDayIntervalToDisplayString(12.345));
        assertEquals("1.6 " + getTargetContext().getString(R.string.year_text), amDateUtil.convertDayIntervalToDisplayString(594.322));
    }
}
