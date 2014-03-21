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
package org.liberty.android.fantastischmemo.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

import android.content.Context;
import android.util.Log;

import com.google.inject.BindingAnnotation;

public class SupermemoXMLImporter extends org.xml.sax.helpers.DefaultHandler implements Converter{

    private static final long serialVersionUID = 8958250001470549241L;

    public Locator mLocator;
    private Context mContext;
    private List<Card> cardList;
    private Card card;
    private LearningData ld;
    private int count = 1;
    private int interval;
    private SimpleDateFormat supermemoFormat = new SimpleDateFormat("dd.MM.yy");


    private StringBuffer characterBuf;
    private final String TAG = "org.liberty.android.fantastischmemo.SupermemoXMLConverter";

    @Override
    public void convert(String src, String dest) throws Exception{
        URL mXMLUrl = new URL("file:///" + src);
        cardList = new LinkedList<Card>();

        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        xr.setContentHandler(this);
        xr.parse(new InputSource(mXMLUrl.openStream()));

        AnyMemoDBOpenHelper helper = AnyMemoDBOpenHelperManager.getHelper(mContext, dest);
        try {
            CardDao cardDao = helper.getCardDao();
            cardDao.createCards(cardList);
        } finally {
            AnyMemoDBOpenHelperManager.releaseHelper(helper);
        }
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
        if(localName.equals("SuperMemoElement")){
            card = new Card();
            card.setCategory(new Category());
            ld = new LearningData();
            card.setLearningData(ld);

            // Set a default interval, in case of malformed the xml file
            interval = 0;

        }
        characterBuf = new StringBuffer();
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException{
        if(localName.equals("SuperMemoElement")){
            card.setOrdinal(count);
            // If this is a new card and the learning data is like this:
            //  <LearningData>
            //    <Repetitions>0</Repetitions>
            //    <Lapses>0</Lapses>
            //    <AFactor>4.620</AFactor>
            //    <UFactor>2.500</UFactor>
            //  </LearningData>
            //  We will use the defualt learning data for missing fields.
            //  Otherwise, we will caluculate it here.
            if (ld.getLastLearnDate() != null) {
                // Calculate the next learning date from interval
                Date nextLearningDate = new Date(ld.getLastLearnDate().getTime() + interval * 24 * 60 * 60 * 1000); // Add days
                ld.setNextLearnDate(nextLearningDate);
            }

            // If an old card get interval 0, then we assume the last grade is 0 as failure card.
            if (interval == 0 && ld.getAcqReps() != 0) {
                ld.setGrade(0);
            }

            cardList.add(card);
            card = null;
            ld = null;
            count += 1;
        }
        if(localName.equals("Question")){
            card.setQuestion(characterBuf.toString());
        }
        if(localName.equals("Answer")){
            card.setAnswer(characterBuf.toString());
        }
        if(localName.equals("Lapses")){
            ld.setLapses(Integer.parseInt(characterBuf.toString()));
        }
        if(localName.equals("Repetitions")){
            ld.setAcqReps(Integer.parseInt(characterBuf.toString()));
        }
        if(localName.equals("Interval")){
            interval = Integer.parseInt(characterBuf.toString());
        }
        if(localName.equals("AFactor")){
            double g = Double.parseDouble(characterBuf.toString());
            if(g <= 5.5) {
                ld.setGrade(2);
            } else {
                ld.setGrade(3);
            }
        }
        if(localName.equals("UFactor")){
            float e = Float.parseFloat(characterBuf.toString());
            ld.setEasiness(e);
        }
        if(localName.equals("LastRepetition")){
            try {
                Date date = supermemoFormat.parse(characterBuf.toString());
                ld.setLastLearnDate(date);
            }
            catch(ParseException e){
                Log.e(TAG, "Parsing date error: " + characterBuf.toString(), e);
            }
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
