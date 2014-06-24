package org.ihtsdo.xml.handlers.refset;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartCid extends XML_RefSetBasic implements I_Handle_XML {

	
	I_ExtendByRefPartCid rpc;
	int c1id = -1;
	//int conceptID = -1;

	public XML_I_ExtendByRefPartCid(I_ExtendByRefPartCid rpc,
			Element parent) {
		super();
		this.rpc = rpc;
		this.parent = parent;
		getXML();
	}

	public XML_I_ExtendByRefPartCid(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	public void getXML() {
		
		refSetType = CommonXMLStatics.REFSET_TYPE_CID;
		getLocalE();

		if (!debug) {
			c1id = rpc.getC1id();
			//conceptID = rpc.getConceptNid();
			
			versionid = rpc.getVersion();
			positionid = -2;
			//positionid = rpc.getPositionId();
			
			
			statusid = rpc.getStatusId();
			pathid = rpc.getPathId();
		}
		BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
		//BasicXMLStruct.getIntAtt(conceptID, CommonXMLStatics.CONCEPT_ID_ATT,localE);
		addStdAtt(localE);
		
		parent.appendChild(localE);
	}

}
