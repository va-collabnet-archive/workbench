package org.ihtsdo.xml.handlers;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_ConceptAttributePart extends XML_basic implements
		I_Handle_XML {

	I_ConceptAttributePart cap;
	boolean isDef = false;
	
	
	
	public XML_I_ConceptAttributePart(I_ConceptAttributePart cap, Element parent) {
		super();
		this.cap = cap;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_ConceptAttributePart(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}
	
	public void setXML() {
		
	}

	public void getXML() {
		Element localE = parent.getOwnerDocument().createElement(CommonXMLStatics.CAP_ENAME);
		
		if (!debug) {
			isDef = cap.isDefined();
			versionid = cap.getVersion();
			statusid = cap.getStatusId();
			pathid = cap.getPathId();
			
			positionid = -2;
			
			BasicXMLStruct.getVersionThinDateAtts(versionid,localE);
			
		}
		BasicXMLStruct.getBoolAtt(isDef, CommonXMLStatics.DEFINED_ATT, localE);
		addStdAtt(localE);
		parent.appendChild(localE);

	}

}
