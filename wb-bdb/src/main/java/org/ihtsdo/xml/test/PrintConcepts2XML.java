package org.ihtsdo.xml.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_TermFactory;
import org.ihtsdo.xml.controllers.I_TermFactoryCreator;
import org.ihtsdo.xml.controllers.XML_ConceptController;
import org.ihtsdo.xml.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class PrintConcepts2XML {

	private static final Logger log = Logger.getLogger(PrintConcepts2XML.class.getName());
	
	Properties props = null;
	String propsFN = "./test.props";
	String xmlFN = "";
	String dbPath = null;
	
	Document resultDoc = XMLUtil.getEmptyDocument();
	Element consearchE = null;
	
	I_TermFactory tf = null;
	
	ArrayList <UUID>searchUUIDArr = new ArrayList<UUID>();
	ArrayList <String>searchIDArr = new ArrayList<String>();
	ArrayList <Integer>searchIntArr = new ArrayList<Integer>();
	
	public boolean relatedConcepts = false;
	
	
	/**
	 * The first argument can be the path to the properties file. 
	 * If not set then the default is ./test.props
	 */
	public static void main(String[] args) {
		PrintConcepts2XML ct = new PrintConcepts2XML();
		try {
			ct.init(args);
		} catch (Exception e) {
			log.log(Level.SEVERE,"Error thrown in main ", e);
		}
		//ct.printXML();
		System.exit(0);
	}

	public void init(String[] args) throws Exception {

		if (args.length > 0) {
			propsFN = args[0];
			System.out.println("Props found and = " + propsFN);
		}
		
		setupProps();
		getTf();
		processSearch();

			
			// todo check that the file path is OK
			if (xmlFN == null || xmlFN.length() == 0) {
				System.out.println(printXML());
			} else {
				try {
					//System.out.println("About to write out to " + xmlFN);
					XMLUtil.writeXMLToFile(resultDoc, xmlFN);

					//String xml_S = printXML();
					//XMLUtil.writeString(xml_S, xmlFN);
					// XMLUtil.store(xmlFN, xml_S);
					// XMLUtil.writeToFile(xml_S,xmlFN);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		
		if (props == null || props.size() == 0) {
			System.out.println("Props not found at = " + propsFN);
		}

	}

	public void setupProps() throws Exception{
		props = new Properties();		
			props.load(new BufferedReader(new FileReader(propsFN)));
			if (props.getProperty(PrintConceptsStatics.DBPATH) != null) {
				dbPath = props.getProperty(PrintConceptsStatics.DBPATH);
			}
			else{
				throw new Exception("DBPath not set props = "+props);
			}
			if (props.getProperty(PrintConceptsStatics.RELATEDCONCEPTS) != null) {
				String relC = props.getProperty(PrintConceptsStatics.RELATEDCONCEPTS);
				if (relC.equalsIgnoreCase("true")){
					relatedConcepts = true;
				}
			}
			
			setupSearch();
				if (props.getProperty(PrintConceptsStatics.XMLFILE) != null) {
					xmlFN = props.getProperty(PrintConceptsStatics.XMLFILE);
			}		
	}	
	
	public void setupSearch(){
		String val = "";
		if(props != null && props.size() > 0){
			for (Enumeration e = props.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				val = props.getProperty(key);
				if (key.startsWith(PrintConceptsStatics.UUIDPREFIX)) {
					try{
					UUID con_UUID = UUID.fromString(val);
					searchUUIDArr.add(con_UUID);	
					}
					catch(IllegalArgumentException ie){
						log.severe("Concept UUID passed in is not a valid UUID Key = "+key +" val = " +val);
						//System.out.println("Concept UUID passed in is not a valid UUID Key = "+key +" val = " +val);
					}
				}
				if (key.startsWith(PrintConceptsStatics.INTPREFIX)) {
					try{
						Integer conID = new Integer(val);
						searchIntArr.add(conID);
					}
					catch(Exception nfe){
						log.severe("Concept ID number passed in is not a valid Integer Key = "+key +" val = " +val);
					}
				}
				if (key.startsWith(PrintConceptsStatics.IDPREFIX)) {
					searchIDArr.add(val);	
				}
			}
		}
	}
	
	public void processSearch(){
		if(consearchE == null){
			getConsearch();
		}
		if(searchUUIDArr.size() > 0){
			processUUIDs();
		}
		if(searchIDArr.size() > 0){
			processIDs();
		}
		if(searchIntArr.size() > 0){
			processIntIDs();
		}
		
	}
	
	public void processUUIDs() {
			// TODO : Try to init the ConceptCotroller one & then reuse?
		Iterator<UUID> it = searchUUIDArr.iterator();
		//for testing
		long start = System.currentTimeMillis();
		while (it.hasNext()) {
			UUID su = it.next();
			Element uuidSE = resultDoc.createElement(PrintConceptsStatics.CONSEARCH_UUID_ENAME);
			uuidSE.setAttribute(PrintConceptsStatics.CONSEARCH_VAL_ATT, su.toString());
			XML_ConceptController xcc = new XML_ConceptController(tf);
			xcc.setRelatedConcepts(relatedConcepts);
			try {
				Document d = xcc.getXMLConceptUUID(su);
				if(d != null){
				addNodeToElement(uuidSE,d);
				}
				else{
					log.severe("processUUIDs nothing found using " +su);
				}
				
			} catch (Exception e) {
				log.log(Level.SEVERE,"Error thrown in processUUIDs ", e);
			}
			consearchE.appendChild(uuidSE);
		}
		long elapsedTimeMillis = System.currentTimeMillis()-start;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		log.severe("processUUIDs "+searchUUIDArr.size() +" took "+elapsedTimeSec +" seconds" );
	}
	
	
	public void processIDs() {
		Iterator<String> it = searchIDArr.iterator();
		while (it.hasNext()) {
			String id = it.next();
			Element idSE = resultDoc.createElement(PrintConceptsStatics.CONSEARCH_ID_ENAME);
			idSE.setAttribute(PrintConceptsStatics.CONSEARCH_VAL_ATT, id);
			XML_ConceptController xcc = new XML_ConceptController(tf);
			xcc.setRelatedConcepts(relatedConcepts);
			try {
				Document d = xcc.getXMLConceptID(id);
				if(d != null){
				addNodeToElement(idSE,d);
				}
				else{
					log.severe("processIDs nothing found using " +id);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE,"Error thrown in processIDs ", e);
			}
			consearchE.appendChild(idSE);
		}
		
	}
	
	public void processIntIDs() {
		Iterator<Integer> it = searchIntArr.iterator();
		while (it.hasNext()) {
			Integer idI = it.next();
			Element inidSE = resultDoc.createElement(PrintConceptsStatics.CONSEARCH_INT_ENAME);
			inidSE.setAttribute(PrintConceptsStatics.CONSEARCH_VAL_ATT, idI.toString());
			XML_ConceptController xcc = new XML_ConceptController(tf);
			xcc.setRelatedConcepts(relatedConcepts);
			try {
				Document d = xcc.getXMLConceptInt(idI.intValue());
				if(d != null){
				addNodeToElement(inidSE,d);
				}
				else{
					log.severe("processIntIDs nothing found using " +idI);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE,"Error thrown in processIntIDs ", e);
			}
			consearchE.appendChild(inidSE);
		}
	
	}	
	public void addNodeToElement(Element parent, Document newNodeDoc){
		Element newRoot = newNodeDoc.getDocumentElement();
		Node ImpNewNode = resultDoc.importNode(newRoot, true);
		parent.appendChild(ImpNewNode);
		
	}
	
	
	public String printXML(){
		String xml = "";
		//
		try {
			xml = XMLUtil.convertToStringLeaveCDATA(resultDoc);
			log.severe("printXML = \n" + xml);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xml;
	}

	public Element getConsearch() {
		
		if(resultDoc == null){
		resultDoc = XMLUtil.getEmptyDocument();
		}
		
		if(consearchE == null){
			consearchE = resultDoc.createElement(PrintConceptsStatics.CONSEARCH_ENAME);
			resultDoc.appendChild(consearchE);
		}	
		return consearchE;
	}

	public void setConsearch(Element consearch) {
		this.consearchE = consearch;
	}

	public I_TermFactory getTf() throws Exception{
		
		//TODO: Add CacheSize and readonly
		if(tf == null){
			I_TermFactoryCreator itc = new I_TermFactoryCreator(dbPath);
			tf = itc.getTf();
		}
		return tf;
	}

	public void setTf(I_TermFactory tf) {
		this.tf = tf;
	}
	
	
}
