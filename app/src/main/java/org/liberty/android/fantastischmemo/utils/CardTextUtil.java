/*

Copyright (C) 2013 Haowen Ning

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

import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;

import org.liberty.android.fantastischmemo.modules.AppComponents;
import org.liberty.android.fantastischmemo.ui.CardImageGetter;
import org.xml.sax.XMLReader;

/**
 * Utility for displaying the text on a card based on various settings.
 * It also handle the images in the card.
 */
public class CardTextUtil {
    private ImageGetter imageGetter;

    public CardTextUtil(AppComponents appComponents, String[] imageSearchPaths) {
        imageGetter = new CardImageGetter(appComponents, imageSearchPaths);
    }

    /**
     * Return a list of fields to display.
     * @param text the text to display
     * @param displayInHtml true if the text is HTML. Note if the card does not have HTML tags.
     * The card will not be displayed in HTML format.
     * @param htmlLineBreakConversion convert the \n to HTML br tag.
     * @return a CharSequence that represent the result spannable text to be displayed in a TextView.
     * and second one is answer.
     */
    public CharSequence getSpannableText(String text, boolean displayInHtml, boolean htmlLineBreakConversion) {
        /* Automatic check HTML */
        if (AMStringUtils.isHTML(text) && displayInHtml) {
            if (htmlLineBreakConversion == true) {
                String s = text.replace("\n", "<br />");
                return Html.fromHtml(s, imageGetter, tagHandler);
            } else {
                return Html.fromHtml(text, imageGetter, tagHandler);
            }
        } 
            
        return text;
    }

    private TagHandler tagHandler = new TagHandler() {
        @Override
        public void handleTag(boolean opening, String tag, Editable output,
                XMLReader xmlReader) {
            return;
        }
    };

}

