package org.ihtsdo.xml.handlers.refset;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartCidCidString extends XML_RefSetBasic implements I_Handle_XML {

	I_ExtendByRefPartCidCidString rpccs;
	int c1id = -1;
	int c2id = -1;
	String text = "test";
	
	public XML_I_ExtendByRefPartCidCidString(
			I_ExtendByRefPartCidCidString rpccs,Element parent) {
		super();
		this.rpccs = rpccs;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_ExtendByRefPartCidCidString(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}
	

	public void getXML() {
		
		refSetType = CommonXMLStatics.REFSET_TYPE_CIDCIDSTR;
		getLocalE();

		if (!debug) {
			c1id = rpccs.getC1id();
			c2id = rpccs.getC2id();
			text = rpccs.getStringValue();
			
			versionid = rpccs.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpccs.getStatusId();
			pathid = rpccs.getPathId();
		}
		BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(c2id, CommonXMLStatics.C2_ID_ATT,localE);
		BasicXMLStruct.getStringAtt(text, CommonXMLStatics.TEXT_ATT,localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);
	}

}
