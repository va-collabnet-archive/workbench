package org.ihtsdo.xml.handlers.refset;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartStr extends XML_RefSetBasic implements
		I_Handle_XML {
	
	I_ExtendByRefPartStr rps;
	String text = "test";
	
	

	public XML_I_ExtendByRefPartStr(I_ExtendByRefPartStr rps, Element parent) {
		super();
		this.rps = rps;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_ExtendByRefPartStr(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	public void getXML() {
		refSetType = CommonXMLStatics.REFSET_TYPE_STR;
		getLocalE();
		
		if (!debug) {
			text = rps.getStringValue();
			versionid = rps.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rps.getStatusId();
			pathid = rps.getPathId();
			
		}
		BasicXMLStruct.getStringAtt(text, CommonXMLStatics.VAL_ATT,localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);

	}

}
