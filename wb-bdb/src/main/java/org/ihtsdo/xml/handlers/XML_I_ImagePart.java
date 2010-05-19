package org.ihtsdo.xml.handlers;

import org.dwfa.ace.api.I_ImagePart;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_ImagePart extends XML_basic implements I_Handle_XML {

	I_ImagePart imgP;
	int typeid = -1;
	String text = "test";
	
	
	public XML_I_ImagePart(I_ImagePart imgP, Element parent) {
		super();
		this.imgP = imgP;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_ImagePart(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	public void getXML() {
		Element localE = parent.getOwnerDocument().createElement(CommonXMLStatics.IMG_PART_ENAME);
		
		if (!debug) {
			typeid = imgP.getTypeId();
			text = imgP.getTextDescription();
			versionid = imgP.getVersion();
			positionid = -2;
			statusid = imgP.getStatusId();
			pathid = imgP.getPathId();
			BasicXMLStruct.getVersionThinDateAtts(versionid,localE);
		}
		
		BasicXMLStruct.getNativeIdAtts_i(typeid,localE);
		BasicXMLStruct.getStringAtt(text, CommonXMLStatics.TEXT_ATT,localE);
		addStdAtt(localE);
		
		parent.appendChild(localE);


	}

}
