package org.ihtsdo.xml.handlers.refset;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartCidString extends XML_RefSetBasic implements
		I_Handle_XML {

	I_ExtendByRefPartCidString rpcs;
	int c1id = -1;
	String text = "test";
	
	public XML_I_ExtendByRefPartCidString(I_ExtendByRefPartCidString rpcs, Element parent) {
		super();
		this.rpcs = rpcs;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_ExtendByRefPartCidString(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	public void getXML() {
		refSetType = CommonXMLStatics.REFSET_TYPE_CIDSTR;
		getLocalE();
		
		if (!debug) {
			c1id = rpcs.getC1id();
			text = rpcs.getStringValue();
			versionid = rpcs.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpcs.getStatusId();
			pathid = rpcs.getPathId();
			
		}
		BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
		BasicXMLStruct.getStringAtt(text, CommonXMLStatics.TEXT_ATT,localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);
	}

}
