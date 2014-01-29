package org.ihtsdo.rf2.module.util;

import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.xerces.parsers.SAXParser;
import org.dwfa.ace.log.AceLog;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

// TODO: Auto-generated Javadoc
/**
 * The Class SAXReader.
 */
public class SAXReader extends DefaultHandler {

	/** The columns list. */
	public static ArrayList<Column> columnsList = new ArrayList<Column>();

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String name, String qName, Attributes atts) {

		if (uri.equals("http://www.cap.org/filters") && name.equals("column")) {
			Column column = new Column();
			column.setName(atts.getValue("", "name"));
			column.setDelimiter(atts.getValue("", "delimiter"));
			columnsList.add(column);
		}

	}

	/**
	 * Gets the columns.
	 *
	 * @param fileName the file name
	 * @return the columns
	 */
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
