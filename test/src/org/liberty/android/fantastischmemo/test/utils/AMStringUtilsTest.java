package org.liberty.android.fantastischmemo.test.utils;

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

    @SmallTest
    public void testFindFileInCardTextWithEmptyCard() {
        String card = "";
        assertEquals(0, AMStringUtils.findFileInCardText(card, new String[]{"jpg", "png"}).size());
    }

    @SmallTest
    public void testFindFileInCardTextWithNoFileInCard() {
        String card = "This is a plain old card with no jpg or png";
        assertEquals(0, AMStringUtils.findFileInCardText(card, new String[]{"jpg", "png"}).size());
    }

    @SmallTest
    public void testFindFileInCardTextWithNoExtensionMatch() {
        String card = "This is card has a mozart.mp3 in it";
        assertEquals(0, AMStringUtils.findFileInCardText(card, new String[]{"jpg", "png"}).size());
    }

    @SmallTest
    public void testFindFileInCardTextWithOneMatchedExtension() {
        String card = "This is card has a mozart.mp3 in it";
        assertEquals(1, AMStringUtils.findFileInCardText(card, new String[]{"mp3", "png"}).size());
        assertEquals("mozart.mp3", AMStringUtils.findFileInCardText(card, new String[]{"mp3", "png"}).get(0));
    }

    @SmallTest
    public void testFindFileInCardTextWithMultipleMatchedExtension() {
        String card = "This is card has a mozart.mp3 in it along with a nice.png.";
        assertEquals(2, AMStringUtils.findFileInCardText(card, new String[]{"mp3", "png"}).size());
        assertEquals("mozart.mp3", AMStringUtils.findFileInCardText(card, new String[]{"mp3", "png"}).get(0));
        assertEquals("nice.png", AMStringUtils.findFileInCardText(card, new String[]{"mp3", "png"}).get(1));
    }
}
