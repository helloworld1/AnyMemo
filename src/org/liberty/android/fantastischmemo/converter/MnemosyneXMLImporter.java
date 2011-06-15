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

public class MnemosyneXMLImporter extends org.xml.sax.helpers.DefaultHandler implements AbstractConverter{
    private long timeOfStart = 0L;
	public Locator mLocator;
    private Item.Builder itemBuilder;
    private int count = 1;
    List<Item> itemList;

	private StringBuffer characterBuf;
    private Context mContext;
	private final long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;
    private final String TAG = "org.liberty.android.fantastischmemo.MnemosyneXMLImporter";

	
    public MnemosyneXMLImporter(Context context){
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
        if(localName.equals("mnemosyne")){
            try{
                timeOfStart = Long.parseLong(atts.getValue("time_of_start"));
                /* Convert to local time */
                /*
                Calendar nc = Calendar.getInstance();
                TimeZone tz = nc.getTimeZone();
                int offset = tz.getOffset(timeOfStart);
                timeOfStart -= offset;
                */

                Log.v(TAG, "Time of start: " + timeOfStart);
            }
            catch(Exception e){
                Log.e(TAG, "parse time_of_start error", e);
            }

        }
		if(localName.equals("item")){
            itemBuilder = new Item.Builder();
            itemBuilder.setId(count);
            count += 1;
			String idAttr = atts.getValue("id");
            String grAttr = atts.getValue("gr");
            if(grAttr != null){
                itemBuilder.setGrade(Integer.parseInt(grAttr));
            }
            String eAttr = atts.getValue("e");
            if(eAttr != null){
                itemBuilder.setEasiness(Double.parseDouble(eAttr));
            }
            String acrpAttr = atts.getValue("ac_rp");
            String uAttr = atts.getValue("u");
            String rtrpAttr = atts.getValue("rt_rp");
            if(rtrpAttr != null){
                itemBuilder.setRetReps(Integer.parseInt(rtrpAttr));
            }
            if(acrpAttr != null){
                int acrp = Integer.parseInt(acrpAttr);
                if(uAttr != null){
                    if(Integer.parseInt(uAttr) == 1){
                        /* Commented out for testing */
                        // acrp = 0;
                    }
                }
                if(Integer.valueOf(rtrpAttr) != 0 && acrp == 0){
                    /* This is a workaround for the malformed
                     * XML file.
                     */
                    acrp = Integer.valueOf(rtrpAttr) / 2 + 1;
                }

                itemBuilder.setAcqReps(acrp);
            }
            String lpsAttr = atts.getValue("lps");
            if(lpsAttr != null){
                itemBuilder.setLapses(Integer.parseInt(lpsAttr));
            }
            String acqrplAttr = atts.getValue("ac_rp_l");
            if(acqrplAttr != null){
                itemBuilder.setAcqRepsSinceLapse(Integer.parseInt(acqrplAttr));
            }
            String rtrplAttr = atts.getValue("rt_rp_l");
            if(rtrplAttr != null){
                itemBuilder.setRetRepsSinceLapse(Integer.parseInt(rtrplAttr));
            }
            String lrpAttr = atts.getValue("l_rp");
            if(lrpAttr != null && timeOfStart != 0L){
                long lrp = Math.round(Double.parseDouble(lrpAttr));
                Date date = new Date(timeOfStart * 1000L + lrp * MILLSECS_PER_DAY);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String strDate = formatter.format(date);
                itemBuilder.setDateLearn(strDate);
            }

            String nrpAttr = atts.getValue("n_rp");
            if(nrpAttr != null && lrpAttr != null){
                long lrp = Math.round(Double.parseDouble(lrpAttr));
                long nrp = Math.round(Double.parseDouble(nrpAttr));
                int interval = (int)(nrp - lrp);
                itemBuilder.setInterval(interval);
            }
		}
		characterBuf = new StringBuffer();
	}
	
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException{
		if(localName.equals("item")){
            itemList.add(itemBuilder.build());
            /* The end of life of itemBuilder */
            itemBuilder = null;
		}
		if(localName.equals("cat")){
            itemBuilder.setCategory(characterBuf.toString());
		}
		if(localName.equals("Q")|| localName.equals("Question")){
            itemBuilder.setQuestion(characterBuf.toString());
		}
		if(localName.equals("A")|| localName.equals("Answer")){
            itemBuilder.setAnswer(characterBuf.toString());
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
