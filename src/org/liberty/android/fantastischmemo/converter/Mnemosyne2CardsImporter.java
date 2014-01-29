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

package org.liberty.android.fantastischmemo.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.utils.AMZipUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.google.inject.BindingAnnotation;

public class Mnemosyne2CardsImporter implements Converter {

    private static final long serialVersionUID = 3472937456L;

    private static final int TAG_FIELD_TYPE = 10;

    private static final int QA_FIELD_TYPE = 16;

    private static final int LEARNING_DATA_FIELD_TYPE = 6;

    @Override
    public void convert(String src, String dest) throws Exception {
        // Make the tmp directory tmp/[src file name]/
        String srcFilename = FilenameUtils.getName(src);
        File tmpDirectory = new File(AMEnv.DEFAULT_TMP_PATH + srcFilename);

        FileUtils.deleteDirectory(tmpDirectory);
        FileUtils.forceMkdir(tmpDirectory);

        AnyMemoDBOpenHelper helper = null;
        try {
            // First unzip the file since cards is just a zip archive
            // Example content of cards
            // $ ls
            // METADATA  cards.xml  musicnotes
            AMZipUtils.unZipFile(new File(src), tmpDirectory);

            // Make sure the XML file exists.
            File xmlFile = new File(tmpDirectory + "/cards.xml");
            if (!xmlFile.exists()) {
                throw new Exception ("Could not find the cards.xml after extracting " + src);
            }

            List<Card> cardList = xmlToCards(xmlFile);

            helper = AnyMemoDBOpenHelperManager.getHelper(dest);
            CardDao cardDao = helper.getCardDao();
            cardDao.createCards(cardList);

            // The last step is to see if there are images to import.
            Collection<File> imageFiles = FileUtils.listFiles(
                tmpDirectory,
                new SuffixFileFilter(new String[] {"jpg", "png", "bmp"}, IOCase.INSENSITIVE),
                DirectoryFileFilter.DIRECTORY);
            if (!imageFiles.isEmpty()) {
                String destDbName = FilenameUtils.getName(dest);
                File imageDir = new File(AMEnv.DEFAULT_IMAGE_PATH + destDbName);
                FileUtils.forceMkdir(imageDir);
                for (File imageFile : imageFiles) {
                    FileUtils.copyFileToDirectory(imageFile, imageDir);
                }
            }
        } finally {
            if (helper != null) {
                AnyMemoDBOpenHelperManager.releaseHelper(helper);
            }
            FileUtils.deleteDirectory(tmpDirectory);
        }


    }

    /*
       Sample format to parse:
       <openSM2sync number_of_entries="10"><log type="10" o_id="epbt47DXNulRU8DsEtwtRp"><name>Category1</name></log>
       <log type="10" o_id="5SfWDFGwqrlnGLDQxHHyG0"><name>Category2</name></log>
       <log type="16" o_id="4pblLFgzF7vONXhjIQv5Rm"><b>Answer1</b><f>Qustion1</f></log>
       <log type="16" o_id="QiQz84nC2tvMF5OKdPepaf"><b>Answer2</b><f>Question2</f></log>
       <log type="16" o_id="hfoNwlcf0quuxpcWntyCZl"><b>Answer3</b><f>Question3</f></log>
       <log type="16" o_id="7xmRCBH0WP0DZaxeFn5NLw"><b>Answer4</b><f>Question4</f></log>
       <log card_t="1" fact_v="1.1" e="2.5" gr="-1" tags="epbt47DXNulRU8DsEtwtRp" rt_rp_l="0" lps="0" l_rp="-1" n_rp="-1" ac_rp_l="0" rt_rp="0" ac_rp="0" type="6" o_id="MDcIMVQJUGEjqR6DJoAqVm" fact="4pblLFgzF7vONXhjIQv5Rm"></log>
       <log card_t="1" fact_v="1.1" e="2.5" gr="-1" tags="epbt47DXNulRU8DsEtwtRp" rt_rp_l="0" lps="0" l_rp="-1" n_rp="-1" ac_rp_l="0" rt_rp="0" ac_rp="0" type="6" o_id="iVwbE7q2QLPY4SJRS1AtqK" fact="QiQz84nC2tvMF5OKdPepaf"></log>
       <log card_t="1" fact_v="1.1" e="2.5" gr="-1" tags="5SfWDFGwqrlnGLDQxHHyG0" rt_rp_l="0" lps="0" l_rp="-1" n_rp="-1" ac_rp_l="0" rt_rp="0" ac_rp="0" type="6" o_id="hW3JxWbfBOWoKDVQZ3jVQ1" fact="hfoNwlcf0quuxpcWntyCZl"></log>
       <log card_t="1" fact_v="1.1" e="2.5" gr="-1" tags="5SfWDFGwqrlnGLDQxHHyG0" rt_rp_l="0" lps="0" l_rp="-1" n_rp="-1" ac_rp_l="0" rt_rp="0" ac_rp="0" type="6" o_id="7IXjCysHuCDtXo8hlFrK55" fact="7xmRCBH0WP0DZaxeFn5NLw"></log>
       </openSM2sync>
   */
    private List<Card> xmlToCards(File xmlFile) throws IOException, XmlPullParserException {
        FileInputStream inputStream = new FileInputStream(xmlFile);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new BufferedReader(new InputStreamReader(inputStream)));


        List<Card> cardList = null;
        // Map between xml oid to card
        Map<String, Card> cardOidMap = null;
        // Map between xml oid to category
        Map<String, Category> categoryOidMAP = null;

        int lastType = 0;
        int currentOrd = 1;
        String lastOid = "";
        ValueType lastValueType = null;

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            if (eventType == XmlPullParser.START_DOCUMENT) {
                // Nothing
            } else if (eventType == XmlPullParser.START_TAG) {
                // Get the count
                // <openSM2sync number_of_entries="10">
                if (xpp.getName().equals("openSM2sync")) {
                    int cardCount = Integer.parseInt(xpp.getAttributeValue(null, "number_of_entries"));
                    cardList = new ArrayList<Card>(cardCount);
                    // The hash map is 0.75 full for performance and space.
                    // Use LinkedHashMap to maintain insertion order.
                    cardOidMap = new LinkedHashMap<String, Card> (cardCount * 4 / 3);
                    categoryOidMAP = new LinkedHashMap<String, Category>();
                }

                if (xpp.getName().equals("log")) {
                    lastType  = Integer.parseInt(xpp.getAttributeValue(null, "type"));
                    lastOid = xpp.getAttributeValue(null, "o_id");
                }

                // <log type="10" o_id="5SfWDFGwqrlnGLDQxHHyG0"><name>Category2</name></log>
                if (lastType == TAG_FIELD_TYPE && xpp.getName().equals("name")) {
                    lastValueType = ValueType.CARD_TAG;
                }

                // <log type="16" o_id="7xmRCBH0WP0DZaxeFn5NLw"><b>Answer4</b><f>Question4</f></log>
                if (lastType == QA_FIELD_TYPE && xpp.getName().equals("f")) {
                    lastValueType = ValueType.CARD_FRONT;
                }

                if (lastType == QA_FIELD_TYPE && xpp.getName().equals("b")) {
                    lastValueType = ValueType.CARD_BACK;
                }

                //  <log card_t="1" fact_v="1.1" e="2.5" gr="-1" tags="epbt47DXNulRU8DsEtwtRp" rt_rp_l="0" lps="0" l_rp="-1" n_rp="-1" ac_rp_l="0" rt_rp="0" ac_rp="0" type="6" o_id="MDcIMVQJUGEjqR6DJoAqVm" fact=  "4pblLFgzF7vONXhjIQv5Rm"></log>
                if (lastType == LEARNING_DATA_FIELD_TYPE) {
                    String tagOid = xpp.getAttributeValue(null, "tags");
                    String factOid = xpp.getAttributeValue(null, "fact");
                    LearningData ld = new LearningData();

                    ld.setEasiness(Float.parseFloat(xpp.getAttributeValue(null, "e")));
                    ld.setGrade(Integer.parseInt(xpp.getAttributeValue(null, "gr")));
                    ld.setRetReps(Integer.parseInt(xpp.getAttributeValue(null, "rt_rp_l")));
                    ld.setLapses(Integer.parseInt(xpp.getAttributeValue(null, "lps")));
                    ld.setLapses(Integer.parseInt(xpp.getAttributeValue(null, "lps")));

                    long nrp = Integer.parseInt(xpp.getAttributeValue(null, "n_rp"));

                    long lrp = Integer.parseInt(xpp.getAttributeValue(null, "l_rp"));

                    if (lrp != -1 && nrp != -1) {
                        ld.setLastLearnDate(new Date(lrp * 1000L));
                        ld.setNextLearnDate(new Date(nrp * 1000L));
                    }

                    ld.setAcqRepsSinceLapse(Integer.parseInt(xpp.getAttributeValue(null, "ac_rp_l")));
                    ld.setRetReps(Integer.parseInt(xpp.getAttributeValue(null, "rt_rp")));
                    ld.setAcqReps(Integer.parseInt(xpp.getAttributeValue(null, "ac_rp")));
                    // Now find the card that need this learning data
                    Card card = cardOidMap.get(factOid);

                    // The learning data is using the same id as card
                    ld.setId(card.getId());

                    // and also find out the corresponding tag for card's category
                    Category category = categoryOidMAP.get(tagOid);
                    if (tagOid.contains(",")) {
                        // If the card has multiple category, select only the first category.
                        String firstOid = tagOid.split(",")[0];
                        category = categoryOidMAP.get(firstOid);
                    } else {
                        category = categoryOidMAP.get(tagOid);
                    }
                    if (category == null) {
                        category = new Category();
                    }

                    card.setLearningData(ld);
                    card.setCategory(category);
                }

            } else if (eventType == XmlPullParser.TEXT) {
                if (lastValueType == ValueType.CARD_TAG) {
                    Category category = new Category();
                    category.setName(xpp.getText());
                    categoryOidMAP.put(lastOid, category);
                } else if (lastValueType == ValueType.CARD_FRONT) {
                    Card card = cardOidMap.get(lastOid);
                    if (card == null) {
                        card = new Card();
                        card.setId(currentOrd);
                        card.setOrdinal(currentOrd);
                        cardOidMap.put(lastOid, card);
                        cardList.add(card);
                        currentOrd++;
                    }
                    card.setQuestion(xpp.getText());
                } else if (lastValueType == ValueType.CARD_BACK) {
                    Card card = cardOidMap.get(lastOid);
                    if (card == null) {
                        card = new Card();
                        card.setId(currentOrd);
                        card.setOrdinal(currentOrd);
                        cardOidMap.put(lastOid, card);
                        cardList.add(card);
                        currentOrd++;
                    }
                    card.setAnswer(xpp.getText());
                }
            } else if (eventType ==XmlPullParser.END_TAG) {
                if (xpp.getName().equals("log")) {
                    lastType = 0;
                    lastOid = "";
                    lastValueType = null;
                }
            }
            eventType = xpp.next();
        }

        return cardList;
    }

    @Override
    public String getSrcExtension() {
        return "cards";
    }

    @Override
    public String getDestExtension() {
        return "db";
    }

    private static enum ValueType {
        CARD_TAG,
        CARD_FRONT,
        CARD_BACK;
    }

    @BindingAnnotation
    @Target({ ElementType. FIELD, ElementType.PARAMETER, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Type {};

}


