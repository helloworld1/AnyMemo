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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import org.amr.arabic.ArabicUtilities;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.SettingDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Option;
import org.liberty.android.fantastischmemo.domain.Setting;
import org.liberty.android.fantastischmemo.ui.CardImageGetterFactory;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.google.inject.assistedinject.Assisted;

/*
 * Utility for TTS of a card
 */
public class CardTextUtil {

    private Setting setting;

    private Option option;

    private ImageGetter imageGetter;

    @Inject
    public CardTextUtil(Context context,
            Option option,
            CardImageGetterFactory cardImageGetterFactory,
            @Assisted String dbPath) {
        this.option = option;

        imageGetter = cardImageGetterFactory.create(dbPath);
        
        AnyMemoDBOpenHelper dbOpenHelper = null;
        try {
            dbOpenHelper = AnyMemoDBOpenHelperManager.getHelper(context, dbPath);
            SettingDao settingDao = dbOpenHelper.getSettingDao();
            setting = settingDao.queryForId(1);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(dbOpenHelper);
        }
    }

    /**
     * Return a list of fields to display.
     * @param card the card to display
     * @return a list of spannable fields to display. Usually first one is question
     * and second one is answer.
     */
    public List<Spannable> getFieldsToDisplay(Card card) {
        boolean enableThirdPartyArabic = option.getEnableArabicEngine();
        EnumSet<Setting.CardField> htmlDisplay = setting.getDisplayInHTMLEnum();

        String itemQuestion = card.getQuestion();
        String itemAnswer = card.getAnswer();
        String itemCategory = card.getCategory().getName();
        String itemNote = card.getNote();

        if (enableThirdPartyArabic) {
            itemQuestion = ArabicUtilities.reshape(itemQuestion);
            itemAnswer = ArabicUtilities.reshape(itemAnswer);
            itemCategory = ArabicUtilities.reshape(itemCategory);
            itemNote = ArabicUtilities.reshape(itemNote);
        }

        // For question field (field1)
        SpannableStringBuilder sq = new SpannableStringBuilder();

        // For answer field  (field2)
        SpannableStringBuilder sa = new SpannableStringBuilder();
        /* Show the field that is enabled in settings */
        EnumSet<Setting.CardField> field1 = setting.getQuestionFieldEnum();
        EnumSet<Setting.CardField> field2 = setting.getAnswerFieldEnum();

        /* Iterate all fields */
        for (Setting.CardField cf : Setting.CardField.values()) {
            String str = "";
            if (cf == Setting.CardField.QUESTION) {
                str = itemQuestion;
            } else if (cf == Setting.CardField.ANSWER) {
                str = itemAnswer;
            } else if (cf == Setting.CardField.NOTE) {
                str = itemNote;
            } else {
                throw new AssertionError(
                        "This is a bug! New CardField enum has been added but the display field haven't been nupdated");
            }
            SpannableStringBuilder buffer = new SpannableStringBuilder();

            /* Automatic check HTML */
            if (AMStringUtils.isHTML(str) && (htmlDisplay.contains(cf))) {
                if (setting.getHtmlLineBreakConversion() == true) {
                    String s = str.replace("\n", "<br />");
                    buffer.append(Html.fromHtml(s, imageGetter, tagHandler));
                } else {
                    buffer.append(Html.fromHtml(str, imageGetter, tagHandler));
                }
            } else {
                if (buffer.length() != 0) {
                    buffer.append("\n\n");
                }
                buffer.append(str);
            }
            if (field1.contains(cf)) {
                if (sq.length() != 0) {
                    sq.append(Html.fromHtml("<br /><br />", imageGetter,
                            tagHandler));
                }
                sq.append(buffer);
            }
            if (field2.contains(cf)) {
                if (sa.length() != 0) {
                    sa.append(Html.fromHtml("<br /><br />", imageGetter,
                            tagHandler));
                }
                sa.append(buffer);
            }

        }

        List<Spannable> spannableFields = new ArrayList<Spannable>(2);
        spannableFields.add(sq);
        spannableFields.add(sa);
        return spannableFields;
    }

    private TagHandler tagHandler = new TagHandler() {
        @Override
        public void handleTag(boolean opening, String tag, Editable output,
                XMLReader xmlReader) {
            return;
        }
    };

}

