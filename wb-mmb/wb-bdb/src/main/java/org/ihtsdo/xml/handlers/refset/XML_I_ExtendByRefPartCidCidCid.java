package org.ihtsdo.xml.handlers.refset;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartCidCidCid extends XML_RefSetBasic implements I_Handle_XML {

	I_ExtendByRefPartCidCidCid rpcc = null;
	int c1id = -1;
	int c2id = -1;
	int c3id = -1;

	public XML_I_ExtendByRefPartCidCidCid(
			I_ExtendByRefPartCidCidCid rpcc, Element parent) {
		super();
		this.rpcc = rpcc;
		this.parent = parent;
		getXML();
	}

	public XML_I_ExtendByRefPartCidCidCid(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	public void getXML() {

		refSetType = CommonXMLStatics.REFSET_TYPE_CIDCIDCID;
		getLocalE();

		if (!debug) {
			c1id = rpcc.getC1id();
			c2id = rpcc.getC2id();
			c3id = rpcc.getC3id();
			//conceptID = rpcc.getConceptId();
			
			versionid = rpcc.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpcc.getStatusId();
			pathid = rpcc.getPathId();
		}
		BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(c2id, CommonXMLStatics.C2_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(c3id, CommonXMLStatics.C3_ID_ATT, localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);

	}

}
