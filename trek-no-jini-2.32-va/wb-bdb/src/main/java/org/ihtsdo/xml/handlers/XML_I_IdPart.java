package org.ihtsdo.xml.handlers;

import org.dwfa.ace.api.I_IdPart;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_IdPart extends XML_basic implements I_Handle_XML {

	I_IdPart id;
	int id_id = -1;
	String val = "test";
	
	
	public XML_I_IdPart(I_IdPart id, Element parent) {
		super();
		this.id = id;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_IdPart(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}
	public void getXML() {
		Element localE = parent.getOwnerDocument().createElement(CommonXMLStatics.ID_ENAME);
		
		if (!debug) {
			id_id = id.getAuthorityNid();
			
			versionid = id.getVersion();
			positionid = -2;
			statusid = id.getStatusId();
			pathid = id.getPathId();
			val = id.getDenotation().toString();
			BasicXMLStruct.getVersionThinDateAtts(versionid,localE);
		}
		
		//System.out.println("IDPart Denotation = "+id.getDenotation());
		
		BasicXMLStruct.getNativeIdAtts(id_id,localE);
		BasicXMLStruct.getStringAtt(val, CommonXMLStatics.VAL_ATT, localE);
		addStdAtt(localE);
		
		parent.appendChild(localE);

	}

}
