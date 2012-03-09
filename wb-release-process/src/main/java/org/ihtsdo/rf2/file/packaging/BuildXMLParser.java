package org.ihtsdo.rf2.file.packaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * BuildXMLParser
 * 
 * Generates the directory structure and copies files into the correct directories as defined in an input XML file.
 * 
 * Files needed: properties file called "conf/packaging.properties", which gives the XML file location XML file with directory and file locations Run this program by calling "runbuild-packaging.bat"
 * which uses "build-packaging.xml"
 * 
 */
public class BuildXMLParser {

	private static Logger logger = Logger.getLogger(BuildXMLParser.class.getName());

	private static Properties props = new Properties();

	private static final String propsFile = "conf/packaging.properties";
	private static final String DEFAULT_XML_FILE_LOCATION = "xml/IntlRelease.xml";
	private static final String DEFAULT_TARGET = "target";
	private static final String DEFAULT_SOURCE = "source";

	public static void main(String args[]) {
		try {
			BuildXMLParser buildXMLParser = new BuildXMLParser();
			buildXMLParser.process();

		} catch (NullPointerException ne) {
			System.out.println("NullPointerException:- " + ne.getMessage());
		} catch (Exception e) {
			System.out.println("Exception:- " + e.getMessage());
		}
	}

	public void process() throws Exception {
		props.load(new FileInputStream(propsFile));
		
		String path = getProperty("packaging.xmlFile", DEFAULT_XML_FILE_LOCATION);
		String source = getProperty("packaging.source", DEFAULT_SOURCE);
		String target = getProperty("packaging.target", DEFAULT_TARGET);
		
		Package p = new Package();

		
		Document dom = p.parseXmlFile(path);
				
		p.parseDocument(dom, source, target);
	}

	private static String getProperty(String propertyName, String defaultValue) {
		String value = props.getProperty(propertyName);
		if (value == null || value.equals("")) {
			value = defaultValue;
		}
		return value;
	}
}
