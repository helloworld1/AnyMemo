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
package org.liberty.android.fantastischmemo.converter;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;

import com.google.inject.BindingAnnotation;

public class MnemosyneXMLImporter extends org.xml.sax.helpers.DefaultHandler implements Converter, Serializable {
    private static final long serialVersionUID = -7871484468353131221L;

    private long timeOfStart = 0L;
    public Locator mLocator;
    private int count = 1;
    private List<Card> cardList;
    private final long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;

    private StringBuffer characterBuf;
    private final String TAG = "org.liberty.android.fantastischmemo.MnemosyneXMLImporter";
    private Card card;

    @Inject
    public MnemosyneXMLImporter(){
    }

    @Override
    public void convert(String src, String dest) throws Exception {
        URL mXMLUrl = new URL("file:///" + src);
        cardList = new LinkedList<Card>();

        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();

        XMLReader xr = sp.getXMLReader();
        xr.setContentHandler(this);
        xr.parse(new InputSource(mXMLUrl.openStream()));
        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(dest);
        try {
            CardDao cardDao = helper.getCardDao();
            cardDao.createCards(cardList);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if(localName.equals("mnemosyne")) {
            try {
                timeOfStart = Long.parseLong(atts.getValue("time_of_start"));
                /* Convert to local time */
                /*
                Calendar nc = Calendar.getInstance();
                TimeZone tz = nc.getTimeZone();
                int offset = tz.getOffset(timeOfStart);
                timeOfStart -= offset;
                */

                Log.v(TAG, "Time of start: " + timeOfStart);
            } catch (Exception e) {
                Log.e(TAG, "parse time_of_start error", e);
            }
        }

        if (localName.equals("item")) {
            card = new Card();
            LearningData learningData = new LearningData();
            card.setLearningData(learningData);
            card.setCategory(new Category());

            card.setId(count);
            card.setOrdinal(count);

            count += 1;

            // Id field is not used here. We use "count" variable instead
            // String idAttr = atts.getValue("id");
            String grAttr = atts.getValue("gr");

            if (grAttr != null) {
                learningData.setGrade(Integer.parseInt(grAttr));
            }
            String eAttr = atts.getValue("e");
            if (eAttr != null) {
                learningData.setEasiness(Float.parseFloat(eAttr));
            }
            String acrpAttr = atts.getValue("ac_rp");
            String uAttr = atts.getValue("u");
            String rtrpAttr = atts.getValue("rt_rp");
            if(rtrpAttr != null) {
                learningData.setRetReps(Integer.parseInt(rtrpAttr));
            }
            if (acrpAttr != null){
                int acrp = Integer.parseInt(acrpAttr);
                if (uAttr != null){
                    if(Integer.parseInt(uAttr) == 1){
                        /* Commented out for testing */
                        // acrp = 0;
                    }
                }
                if (Integer.valueOf(rtrpAttr) != 0 && acrp == 0){
                    /* This is a workaround for the malformed
                     * XML file.
                     */
                    acrp = Integer.valueOf(rtrpAttr) / 2 + 1;
                }
                learningData.setAcqReps(acrp);
            }
            String lpsAttr = atts.getValue("lps");
            if(lpsAttr != null){
                learningData.setLapses(Integer.parseInt(lpsAttr));
            }
            String acqrplAttr = atts.getValue("ac_rp_l");
            if(acqrplAttr != null){
                learningData.setAcqRepsSinceLapse(Integer.parseInt(acqrplAttr));
            }
            String rtrplAttr = atts.getValue("rt_rp_l");
            if(rtrplAttr != null){
                learningData.setRetRepsSinceLapse(Integer.parseInt(rtrplAttr));
            }
            String lrpAttr = atts.getValue("l_rp");

            if(lrpAttr != null && timeOfStart != 0L) {
                // Do nothing now
            }

            String nrpAttr = atts.getValue("n_rp");
            if(nrpAttr != null && lrpAttr != null){
                long lrp = timeOfStart * 1000 + Math.round(Double.parseDouble(lrpAttr)) * MILLSECS_PER_DAY;
                long nrp = timeOfStart * 1000 + Math.round(Double.parseDouble(nrpAttr)) * MILLSECS_PER_DAY;
                learningData.setLastLearnDate(new Date(lrp));
                learningData.setNextLearnDate(new Date(nrp));
            }
        }
        characterBuf = new StringBuffer();
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException{
        if(localName.equals("item")) {
            cardList.add(card);
            /* The end of life of itemBuilder */
            card= null;
        }

        if(localName.equals("cat")) {
            card.getCategory().setName(characterBuf.toString());
        }
        if(localName.equals("Q")|| localName.equals("Question")) {
            card.setQuestion(characterBuf.toString());
        }
        if(localName.equals("A")|| localName.equals("Answer")) {
            card.setAnswer(characterBuf.toString());
        }

    }

    public void setDocumentLocator(Locator locator){
        mLocator = locator;
    }

    public void characters(char ch[], int start, int length){
        characterBuf.append(ch, start, length);
    }

    public void startDocument() throws SAXException{
    }

    public void endDocument() throws SAXException{
    }

    @Override
    public String getSrcExtension() {
        return "xml";
    }

    @Override
    public String getDestExtension() {
        return "db";
    }

    @BindingAnnotation
    @Target({ ElementType. FIELD, ElementType.PARAMETER, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Type {};
}
