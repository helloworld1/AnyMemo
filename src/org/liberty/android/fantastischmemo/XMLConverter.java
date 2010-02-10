package org.liberty.android.fantastischmemo;

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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;

public class XMLConverter extends org.xml.sax.helpers.DefaultHandler{
	private Context mContext;
	private URL mXMLUrl;
	private SAXParserFactory spf;
	private SAXParser sp;
	private XMLReader xr;
	private String fileName;
	private String filePath;
	public Locator mLocator;
	private List<String> questionList;
	private List<String> answerList;
	private List<String> categoryList;
	
	private boolean inItem = false;
	private boolean inQuestion = false;
	private boolean inAnser = false;
	private boolean inCategory = false;
	private boolean isInv = false;
	private boolean isReadCharacters = false;
	
	private StringBuffer characterBuf;
	
	
	
	public XMLConverter(Context context, String filePath, String fileName) throws MalformedURLException, SAXException, ParserConfigurationException, IOException{
		mContext = context;
		this.filePath = filePath;
		this.fileName = fileName;
		mXMLUrl = new URL("file:///" + filePath + "/" + fileName);
		questionList = new LinkedList<String>();
		answerList = new LinkedList<String>();
		categoryList = new LinkedList<String>();
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
	
	public void outputDB() throws IOException{
		DatabaseHelper dbHelper = new DatabaseHelper(mContext, filePath, fileName.replaceAll(".xml", ".db"), 1);
		dbHelper.createDatabaseFromList(questionList, answerList, categoryList);
		dbHelper.close();
		
		
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException{
		if(localName.equals("item")){
			this.inItem = true;
			String idAttr = atts.getValue("id");
			if(idAttr.endsWith("inv")){
				this.isInv = true;
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
