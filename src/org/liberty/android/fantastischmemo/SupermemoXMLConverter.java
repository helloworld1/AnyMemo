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
package org.liberty.android.fantastischmemo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;

public class SupermemoXMLConverter extends org.xml.sax.helpers.DefaultHandler{
	private URL mXMLUrl;
	private SAXParserFactory spf;
	private SAXParser sp;
	private XMLReader xr;
	private String fileName;
	private String filePath;
	public Locator mLocator;
    private List<Item> itemList;
    private Item currentItem;
    private int count = 1;

	
	private StringBuffer characterBuf;
    private final String TAG = "org.liberty.android.fantastischmemo.SupermemoXMLConverter";

	
	
	public SupermemoXMLConverter(String filePath, String fileName) throws MalformedURLException, SAXException, ParserConfigurationException, IOException{
		this.filePath = filePath;
		this.fileName = fileName;
		mXMLUrl = new URL("file:///" + filePath + "/" + fileName);
		itemList = new LinkedList<Item>();

		spf = SAXParserFactory.newInstance();
		sp = spf.newSAXParser();
		xr = sp.getXMLReader();
		xr.setContentHandler(this);
		
		xr.parse(new InputSource(mXMLUrl.openStream()));
		
		
	}
	
	public List<Item> outputList() {
        return itemList;
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
        if(localName.equals("SuperMemoElement")){
            currentItem = new Item();
        }
		characterBuf = new StringBuffer();
	}
	
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException{
        if(localName.equals("SuperMemoElement")){
            currentItem.setId(count);
            itemList.add(currentItem);
            count += 1;
        }
		if(localName.equals("Question")){
            currentItem.setQuestion(characterBuf.toString());
		}
		if(localName.equals("Answer")){
            currentItem.setAnswer(characterBuf.toString());
		}
        if(localName.equals("Lapses")){
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("lapses", characterBuf.toString());
            currentItem.setData(hm);
        }
        if(localName.equals("Repetitions")){
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("acq_reps", characterBuf.toString());
            currentItem.setData(hm);
        }
        if(localName.equals("Interval")){
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("interval", characterBuf.toString());
            currentItem.setData(hm);
        }
        if(localName.equals("LastRepetition")){
            try{
                /* Convert date format from SM to AM*/
                SimpleDateFormat sf = new SimpleDateFormat("dd.MM.yy");
                Date date = sf.parse(characterBuf.toString());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("date_learn", df.format(date));
                currentItem.setData(hm);
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
