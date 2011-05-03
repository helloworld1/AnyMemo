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

import org.liberty.android.fantastischmemo.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.text.ParseException;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;
import android.content.Context;

public class SupermemoXMLImporter extends org.xml.sax.helpers.DefaultHandler implements AbstractConverter{
	public Locator mLocator;
    private Context mContext;
    private List<Item> itemList;
    private Item.Builder itemBuilder;
    private int count = 1;
    SimpleDateFormat supermemoFormat = new SimpleDateFormat("dd.MM.yy");
    SimpleDateFormat anymemoFormat = new SimpleDateFormat("yyyy-MM-dd");

	
	private StringBuffer characterBuf;
    private final String TAG = "org.liberty.android.fantastischmemo.SupermemoXMLConverter";

	
	
    public SupermemoXMLImporter(Context context){
        mContext = context;
    }

    @Override
    public void convert(String filePath, String fileName) throws Exception{
		URL mXMLUrl = new URL("file:///" + filePath + "/" + fileName);
		itemList = new LinkedList<Item>();

        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver"); 

		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		XMLReader xr = sp.getXMLReader();
		xr.setContentHandler(this);
		xr.parse(new InputSource(mXMLUrl.openStream()));

        DatabaseHelper.createEmptyDatabase(filePath, fileName.replace(".xml", ".db"));
        DatabaseHelper dbHelper =  new DatabaseHelper(mContext, filePath, fileName.replace(".xml", ".db"));
        dbHelper.insertListItems(itemList);
        dbHelper.close();
    }
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
        if(localName.equals("SuperMemoElement")){
            itemBuilder = new Item.Builder();
        }
		characterBuf = new StringBuffer();
	}
	
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException{
        if(localName.equals("SuperMemoElement")){
            itemBuilder.setId(count);
            itemList.add(itemBuilder.build());
            itemBuilder = null;
            count += 1;
        }
		if(localName.equals("Question")){
            itemBuilder.setQuestion(characterBuf.toString());
		}
		if(localName.equals("Answer")){
            itemBuilder.setAnswer(characterBuf.toString());
		}
        if(localName.equals("Lapses")){
            itemBuilder.setLapses(Integer.parseInt(characterBuf.toString()));
        }
        if(localName.equals("Repetitions")){
            itemBuilder.setAcqReps(Integer.parseInt(characterBuf.toString()));
        }
        if(localName.equals("Interval")){
            itemBuilder.setInterval(Integer.parseInt(characterBuf.toString()));
        }
        if(localName.equals("AFactor")){
            double g = Double.parseDouble(characterBuf.toString());
            if(g <= 1.5){
                itemBuilder.setGrade(1);
            }
            else if(g <= 5.5){
                itemBuilder.setGrade(2);
            }
            else{
                itemBuilder.setGrade(3);
            }
        }
        if(localName.equals("UFactor")){
            double e = Double.parseDouble(characterBuf.toString());
            itemBuilder.setEasiness(e);
        }
        if(localName.equals("LastRepetition")){
            try{
                /* Convert date format from SM to AM*/
                Date date = supermemoFormat.parse(characterBuf.toString());
                itemBuilder.setDateLearn(anymemoFormat.format(date));
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
	

}
