package org.ihtsdo.xml.handlers;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dwfa.ace.api.Terms;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.util.XMLUtil;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.concept.Concept;
import org.w3c.dom.Element;

public class BasicXMLStruct {
	
	
	
	public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String testUUID = "testUUID";

	


	
	public static void getText_UUID_S_Atts(int nid,String name,Element parent){
		
		
		 try {
			 Concept cb = Concept.get(nid);	 
		getId_S_Atts(nid,name,parent);
		getUUID_S_Atts(cb.getUids().iterator().next().toString(),name,parent);
		getStringAtt(cb.getInitialText(), name+CommonXMLStatics.UUIDSUFFIX,parent);
		    } catch (IOException e) {
		    	e.printStackTrace();
			}
	}
		
	
	public static void getId_S_Atts(int id, String name,Element parent){
		getIntAtt(id, name+CommonXMLStatics.INTSUFFIX,parent);

	}
	
	public static void getUUID_S_Atts(String uuid, String name,Element parent){
		getStringAtt(uuid, name+CommonXMLStatics.UUIDSUFFIX,parent);

	}
	
	
	public static void getVersionIdAtts(int id,Element parent){		
		getIntAtt(id, CommonXMLStatics.VERSION_ATT+CommonXMLStatics.INTSUFFIX,parent);
		getDateAtt((new Date(ThinVersionHelper.convert(id))),CommonXMLStatics.VERSION_ATT,parent);
	}
	
	public static void getVersionIdAtts_thick(int id,Element parent){
		getIntAtt(id, CommonXMLStatics.VERSION_ATT+CommonXMLStatics.INTSUFFIX,parent);
		getDateAtt((new Date(Terms.get().convertToThickVersion(id))),CommonXMLStatics.VERSION_ATT,parent);
	}
	
	public static void getVersionThinDateAtts(int id,Element parent){	
		getDateAtt((new Date(ThinVersionHelper.convert(id))),CommonXMLStatics.VERSION_ATT+CommonXMLStatics.DATESUFFIX,parent);
	}
	public static void getVersionThickDateAtts(int id,Element parent){	
		getDateAtt((new Date(Terms.get().convertToThickVersion(id))),CommonXMLStatics.VERSION_ATT+CommonXMLStatics.DATESUFFIX,parent);
	}
	
	//nid and UUID
	
	public static String getIdUuidAtts(int id,String name,Element parent){
		//getText_UUID_S_Atts(id,CommonXMLProps.NATIVE_ATT,parent);
		String uuid_s = testUUID;
		if(id != -1){
		 try {
			 Concept cb = Concept.get(id);	 
			 uuid_s = cb.getUids().iterator().next().toString();
		    } catch (IOException e) {
		    	e.printStackTrace();
			}
		}
		getIntAtt(id, name,parent);
		getUUID_S_Atts(uuid_s,name,parent);
		return uuid_s;    
	}
	
	public static String getNativeIdAtts(int id,Element parent){
		//getText_UUID_S_Atts(id,CommonXMLProps.NATIVE_ATT,parent);
		String uuid_s = getIdUuidAtts(id,CommonXMLStatics.NATIVE_ATT,parent);
		return uuid_s;    
	}

	//Thin i.e. int/String only (no resolving)
	
	public static void getVersionIdAtts_i(int id,Element parent){
		getIntAtt(id, CommonXMLStatics.VERSION_ATT,parent);
	}
	
	//Just the nid
	public static void getNativeIdAtts_i(int id,Element parent){
		getIntAtt(id, CommonXMLStatics.NATIVE_ATT,parent);	
	}

	public static void getPathIdAtts_i(int id,Element parent){
		getIntAtt(id, CommonXMLStatics.PATH_ATT,parent);
	}
	
	public static void getPositionIdAtts_i(int id,Element parent){
		getIntAtt(id, CommonXMLStatics.POSITION_ATT,parent);
	}
		
	public static void getStatusIdAtts_i(int id,Element parent){
		getIntAtt(id, CommonXMLStatics.STATUS_ATT,parent);	
	}
	
	public static void getStatusIdAtts(int id,Element parent){
		getId_S_Atts(id,CommonXMLStatics.STATUS_ATT,parent);	
	}
	

		
	public static void getNumVersionAtts(int num,Element parent){
		getIntAtt(num, CommonXMLStatics.NUMVERSION_ATT,parent);
	}
	
	public static void getIntAtt(int i, String name,Element parent){
		parent.setAttribute(name, Integer.toString(i));
	}
	
	public static void getBoolAtt(boolean bl, String name,Element parent){
		parent.setAttribute(name, Boolean.toString(bl));
	}
	
	public static void getStringAtt(String val, String name,Element parent){
		String v1 = XMLUtil.encodePlainText4XML(val);
		parent.setAttribute(name, v1);
	}
	
	public static void getDateAtt(Date date, String name,Element parent){
		parent.setAttribute(name, df.format(date));
	}
	
	//XML convenience methods
	
	public static void getTextElem(String text,Element parent){
	
		String v1 = XMLUtil.encodePlainText4XML(text);
		Element textE = parent.getOwnerDocument().createElement(CommonXMLStatics.TEXT_ENAME);	
		textE.setTextContent(v1);
		parent.appendChild(textE);
		
	}
	
	
	
}
