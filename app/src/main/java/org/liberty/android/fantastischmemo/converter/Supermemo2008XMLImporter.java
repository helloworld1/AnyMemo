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

import com.google.inject.BindingAnnotation;

public class Supermemo2008XMLImporter extends org.xml.sax.helpers.DefaultHandler implements Converter{

    private static final long serialVersionUID = 8285843731806571485L;

    public Locator mLocator;
    private List<Card> cardList;
    private Card card;
    private int count = 1;

    private StringBuffer characterBuf;

    @Override
    public void convert(String src, String dest) throws Exception{
        URL mXMLUrl = new URL("file:///" + src);
        cardList = new LinkedList<Card>();
        characterBuf = new StringBuffer();

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

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
        if(localName.equals("Question")){
            characterBuf = new StringBuffer();
            card = new Card();
            card.setLearningData(new LearningData());
            card.setCategory(new Category());
        }
        if(localName.equals("Answer")){
            characterBuf = new StringBuffer();
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException{
        if(localName.equals("Question")){
            card.setQuestion(characterBuf.toString());
        }
        if(localName.equals("Answer")){
            card.setAnswer(characterBuf.toString());
            card.setOrdinal(count);
            count++;
            cardList.add(card);
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
