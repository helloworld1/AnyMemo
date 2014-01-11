package org.liberty.android.fantastischmemo.test.utils;

import org.liberty.android.fantastischmemo.R;
import org.liberty.android.fantastischmemo.utils.AMDateUtil;
import org.liberty.android.fantastischmemo.utils.AMStringUtils;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class AMStringUtilsTest extends AndroidTestCase {

    @SmallTest
    public void testStripHTMLWithNoHtml() {
        String noHtmlText = "I do not have html";
        assertEquals("I do not have html", AMStringUtils.stripHTML(noHtmlText));
    }

    @SmallTest
    public void testStripHTMLWithHtml() {
        String noHtmlText = "Html <b>text</b> is <a href=\"anymemo.org\">anymemo<br />";
        assertEquals("Html text is anymemo", AMStringUtils.stripHTML(noHtmlText));
    }
}
