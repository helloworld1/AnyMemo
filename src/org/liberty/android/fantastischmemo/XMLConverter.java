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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import android.database.SQLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;
import java.text.SimpleDateFormat;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.util.Log;

public class XMLConverter extends org.xml.sax.helpers.DefaultHandler{
	private Context mContext;
	private URL mXMLUrl;
	private SAXParserFactory spf;
	private SAXParser sp;
	private XMLReader xr;
	private String fileName;
	private String filePath;
    private long timeOfStart = 0L;
	public Locator mLocator;
	private List<String> questionList;
	private List<String> answerList;
	private List<String> categoryList;
    private List<String> datelearnList;
    private List<Integer> intervalList;
    private List<Double> easinessList;
    private List<Integer> gradeList;
    private List<Integer> lapsesList;
    private List<Integer> acrpList;
    private List<Integer> rtrpList;
    private List<Integer> arslList;
    private List<Integer> rrslList;

	
	private boolean inItem = false;
	private boolean inQuestion = false;
	private boolean inAnser = false;
	private boolean inCategory = false;
	private boolean isInv = false;
	private boolean isReadCharacters = false;
	
	private StringBuffer characterBuf;
	private final long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;
    private final String TAG = "org.liberty.android.fantastischmemo.XMLConverter";

	
	
	public XMLConverter(Context context, String filePath, String fileName) throws MalformedURLException, SAXException, ParserConfigurationException, IOException{
		mContext = context;
		this.filePath = filePath;
		this.fileName = fileName;
		mXMLUrl = new URL("file:///" + filePath + "/" + fileName);
		questionList = new LinkedList<String>();
		answerList = new LinkedList<String>();
		categoryList = new LinkedList<String>();
        datelearnList = new LinkedList<String>();
        intervalList = new LinkedList<Integer>();
        easinessList = new LinkedList<Double>();
        gradeList = new LinkedList<Integer>();
        lapsesList = new LinkedList<Integer>();
        acrpList = new LinkedList<Integer>();
        rtrpList = new LinkedList<Integer>();
        arslList = new LinkedList<Integer>();
        rrslList = new LinkedList<Integer>();

		spf = SAXParserFactory.newInstance();
		sp = spf.newSAXParser();
		xr = sp.getXMLReader();
		xr.setContentHandler(this);
		
		xr.parse(new InputSource(mXMLUrl.openStream()));
		
		
	}
	
	public void outputTabFile() throws IOException{
		File file = new File(filePath + "/" + fileName);
		file.createNewFile();
		FileOutputStream fileOutStream = new FileOutputStream(file);
		BufferedOutputStream buf = new BufferedOutputStream(fileOutStream, 8192);
		OutputStreamWriter outStream = new OutputStreamWriter(buf);
		
		ListIterator<String> liq = questionList.listIterator();
		ListIterator<String> lia = answerList.listIterator();
		ListIterator<String> lic = categoryList.listIterator();
		
		while(liq.hasNext() && lia.hasNext()){
			outStream.write("Q: " + liq.next() + "\n");
			outStream.write("A: " + lia.next() + "\n");
		}
		outStream.close();
		buf.close();
		fileOutStream.close();
		
		
	}
	
	public void outputDB() throws IOException, SQLException{
        DatabaseHelper dbHelper = new DatabaseHelper(mContext, filePath, fileName.replaceAll(".xml", ".db"));
        Log.v(TAG, "Counts: " + questionList.size() + " " + easinessList.size() + " " + acrpList.size() + " " + arslList.size());
		dbHelper.createDatabaseFromList(questionList, answerList, categoryList, datelearnList, intervalList, easinessList, gradeList, lapsesList, acrpList, rtrpList, arslList, rrslList);
		dbHelper.close();
		
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
        if(localName.equals("mnemosyne")){
            try{
                timeOfStart = Long.parseLong(atts.getValue("time_of_start"));
                /* Convert to local time */
                //Calendar nc = Calendar.getInstance();
                //TimeZone tz = nc.getTimeZone();
                //int offset = tz.getOffset(timeOfStart);
                //timeOfStart -= offset;

                Log.v(TAG, "Time of start: " + timeOfStart);
            }
            catch(Exception e){
                Log.e(TAG, "parse time_of_start error", e);
            }

        }

		if(localName.equals("item")){
			this.inItem = true;
			String idAttr = atts.getValue("id");
			if(idAttr.endsWith("inv")){
				this.isInv = true;
			}
            String grAttr = atts.getValue("gr");
            if(grAttr != null){
                gradeList.add(Integer.parseInt(grAttr));
            }
            String eAttr = atts.getValue("e");
            if(eAttr != null){
                easinessList.add(Double.parseDouble(eAttr));
            }
            String acrpAttr = atts.getValue("ac_rp");
            String uAttr = atts.getValue("u");
            String rtrpAttr = atts.getValue("rt_rp");
            if(rtrpAttr != null){
                rtrpList.add(Integer.valueOf(rtrpAttr));
            }
            if(acrpAttr != null){
                int acrp = Integer.parseInt(acrpAttr);
                if(uAttr != null){
                    if(Integer.parseInt(uAttr) == 1){
                        acrp = 0;
                    }
                }
                if(Integer.valueOf(rtrpAttr) != 0 && acrp == 0){
                    /* This is a workaround for the malformed
                     * XML file.
                     */
                    acrp = Integer.valueOf(rtrpAttr) / 2 + 1;
                }

                acrpList.add(new Integer(acrp));
            }
            String lpsAttr = atts.getValue("lps");
            if(lpsAttr != null){
                lapsesList.add(Integer.valueOf(lpsAttr));
            }
            String acqrplAttr = atts.getValue("ac_rp_l");
            if(acqrplAttr != null){
                arslList.add(Integer.valueOf(acqrplAttr));
            }
            String rtrplAttr = atts.getValue("rt_rp_l");
            if(rtrplAttr != null){
                rrslList.add(Integer.valueOf(rtrplAttr));
            }
            String lrpAttr = atts.getValue("l_rp");
            if(lrpAttr != null && timeOfStart != 0L){
                long lrp = Math.round(Double.parseDouble(lrpAttr));
                Date date = new Date(timeOfStart * 1000L + lrp * MILLSECS_PER_DAY);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String strDate = formatter.format(date);
                datelearnList.add(strDate);
            }

            String nrpAttr = atts.getValue("n_rp");
            if(nrpAttr != null && lrpAttr != null){
                long lrp = Math.round(Double.parseDouble(lrpAttr));
                long nrp = Math.round(Double.parseDouble(nrpAttr));
                intervalList.add(new Integer((int)(nrp - lrp)));
            }
		}
		characterBuf = new StringBuffer();
		if(localName.equals("cat")){
			this.inCategory = true;
		}
		if(localName.equals("Q") || localName.equals("Question")){
			this.inQuestion = true;
		}
		if(localName.equals("A") || localName.equals("Answer")){
			this.inAnser = true;
		}
		
	}
	
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException{
		if(localName.equals("item")){
			this.inItem = false;
			this.isInv = false;
		}
		if(localName.equals("cat")){
			categoryList.add(characterBuf.toString());
			this.inCategory = false;
		}
		if(localName.equals("Q")|| localName.equals("Question")){
			questionList.add(characterBuf.toString());
			this.inQuestion = false;
		}
		if(localName.equals("A")|| localName.equals("Answer")){
			answerList.add(characterBuf.toString());
			this.inAnser = false;
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
