package org.ihtsdo.xml.util;

import java.util.HashMap;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.objectCache.ObjectCache;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.XML_I_GetConceptData;
import org.w3c.dom.Document;


public class AceXMLUtil {
	
	private static final Logger log = Logger.getLogger(AceXMLUtil.class.getName());
	
	/**
	 * Checks to see if the Concept is already in the ObjectCache
	 * @param conID
	 * @return
	 */
	
	public static boolean checkProc(int conID){
		//conIdi =concept.getConceptId();
		String oc_key = getOc_key(conID);
		if(ObjectCache.get(oc_key) != null){
			return true;
		}
		else{
			return false;
		}
	}
/**
 * Returns the key which is used to store the Document representing a concept
 * in the ObjectCache	
 * @param conID
 * @return
 */
	public static String getOc_key(int conID) {
		return CommonXMLStatics.CONCEPT_PRE+Integer.toString(conID);
	}
	
/**
 * Gets a hashmap of UUID as string and int as String	
 * @return
 */
	public static HashMap<String,String> getUuidInt(){
		if(ObjectCache.get(CommonXMLStatics.UUID_INT_HT) == null){
			HashMap<String,String> uuidIntHT = new HashMap<String, String>();
			ObjectCache.put(CommonXMLStatics.UUID_INT_HT, uuidIntHT);
		}
		return (HashMap<String, String>)ObjectCache.get(CommonXMLStatics.UUID_INT_HT);	
	}
	
/**
 * Adds a record to UUIDInt hashmap	
 * @param uuidS
 * @param intS
 */
	
	public static void addtoUuidInt(String uuidS, String intS){
		getUuidInt().put(uuidS, intS);		
	}
	
	/**
	 * Adds a record to UUIDInt hashmap	 uuid as String intI as int
	 * @param uuidS
	 * @param intI
	 */
		
		public static void addtoUuidInt(String uuidS, int intI){
			addtoUuidInt(uuidS, Integer.toString(intI));
			//getUuidInt().put(uuidS, Integer.toString(intI));		
		}
	
	
/**
 * Returns the Xpath string for	all //ename/@attname
 * @param ename
 * @param attname
 * @return
 */
	
	public static String getXpathSElemAtt(String ename,String attname){

		StringBuilder sb=new StringBuilder();
		sb.append(CommonXMLStatics.XPATH_START_E_BY_NAME);
		sb.append(ename);
		sb.append(CommonXMLStatics.XPATH_END_SELECT);
		sb.append(CommonXMLStatics.XPATH_START_ATT_BY_NAME);
		sb.append(attname);
		sb.append(CommonXMLStatics.XPATH_END_SELECT);
		return sb.toString();
	}
	
	public static Document getDocFromConcept(I_GetConceptData igcd) {
		Document doc = null;
		XML_I_GetConceptData x_igcd = new XML_I_GetConceptData(igcd);
		doc = x_igcd.getDoc();
		System.out.println(x_igcd.getConceptXMLAsString());
		return doc;
	}
	
	public static String GetIDValbyUUID(Document doc) {
		String idVal = null;
		
		
		return idVal;
	}
	
	public static Document getDocByID(int id) {
	Document doc = null;	
		
	return doc;	
	}
	

}
