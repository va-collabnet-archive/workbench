package org.ihtsdo.xml.handlers;

import org.w3c.dom.Element;

public class XML_basic implements I_Handle_XML	 {
	
	public boolean debug = false;
	public Element parent;

	
	public int versionid = -1;
	public int positionid = -1;
	public int statusid = -1;
	public int pathid = -1;
	
	
	public XML_basic() {
		super();

	}	
	
	public XML_basic(Element parent) {
		super();
		this.parent = parent;
	}


	public void addStdAtt(Element localE){
		BasicXMLStruct.getVersionIdAtts_i(versionid,localE);
		if(positionid != -2){
		BasicXMLStruct.getPositionIdAtts_i(positionid,localE);
		}
		BasicXMLStruct.getStatusIdAtts_i(statusid,localE);
		BasicXMLStruct.getPathIdAtts_i(pathid,localE);
	}


	public void getXML() {

		
	}

	public void setXML() {

		
	}


	public boolean isDebug() {
		return debug;
	}


	public void setDebug(boolean debug) {
		this.debug = debug;
		getXML();
	}


	public Element getParent() {
		return parent;
	}


	public void setParent(Element parent) {
		this.parent = parent;
	}

}
