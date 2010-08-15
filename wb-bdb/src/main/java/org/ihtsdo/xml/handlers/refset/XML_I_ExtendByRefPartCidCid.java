package org.ihtsdo.xml.handlers.refset;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartCidCid extends XML_RefSetBasic implements I_Handle_XML {

	I_ExtendByRefPartCidCid rpcc = null;
	int c1id = -1;
	int c2id = -1;
	
	//int conceptID = -1;

	public XML_I_ExtendByRefPartCidCid(
			I_ExtendByRefPartCidCid rpcc, Element parent) {
		super();
		this.rpcc = rpcc;
		this.parent = parent;
		getXML();
	}

	public XML_I_ExtendByRefPartCidCid(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	public void getXML() {

		refSetType = CommonXMLStatics.REFSET_TYPE_CIDCID;
		getLocalE();

		if (!debug) {
			c1id = rpcc.getC1id();
			c2id = rpcc.getC2id();
			//conceptID = rpcc.getConceptNid();
			
			versionid = rpcc.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpcc.getStatusId();
			pathid = rpcc.getPathId();
		}
		BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(c2id, CommonXMLStatics.C2_ID_ATT, localE);
		
		//BasicXMLStruct.getIntAtt(conceptID, CommonXMLStatics.CONCEPT_ID_ATT,localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);

	}

}
