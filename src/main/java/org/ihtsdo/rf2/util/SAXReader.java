package org.ihtsdo.rf2.util;

import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.xerces.parsers.SAXParser;
import org.dwfa.ace.log.AceLog;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SAXReader extends DefaultHandler {

	public static ArrayList<Column> columnsList = new ArrayList<Column>();

	public void startElement(String uri, String name, String qName, Attributes atts) {

		if (uri.equals("http://www.cap.org/filters") && name.equals("column")) {
			Column column = new Column();
			column.setName(atts.getValue("", "name"));
			column.setDelimiter(atts.getValue("", "delimiter"));
			columnsList.add(column);
		}

	}

	public ArrayList<Column> getColumns(String fileName) {
		SAXReader saxreader = new SAXReader();
		SAXParser parser = new SAXParser();
		columnsList.clear();
		AceLog.getAppLog().info("===========Initial Coulmn list size using AceLog============" + columnsList.size());

		parser.setContentHandler(saxreader);
		try {
			parser.parse(fileName);
		} catch (Exception e) {
			e.printStackTrace();
			AceLog.getAppLog().alertAndLogException(e);
			AceLog.getAppLog().info("SAXReader getColumns failed");
			AceLog.getAppLog().log(Level.SEVERE, "Exception reading: ", e);
		}

		return columnsList;
	}
}
