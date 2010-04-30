package org.ihtsdo.xml.handlers.refset;

import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartCidLong extends XML_RefSetBasic implements I_Handle_XML {

	
	I_ExtendByRefPartCidLong rpc;
	int c1id = -1;
	long ival = -1L;

	public XML_I_ExtendByRefPartCidLong(I_ExtendByRefPartCidLong rpc,
			Element parent) {
		super();
		this.rpc = rpc;
		this.parent = parent;
		getXML();
	}

	public XML_I_ExtendByRefPartCidLong(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	public void getXML() {
		
		refSetType = CommonXMLStatics.REFSET_TYPE_CIDLONG;
		getLocalE();

		if (!debug) {
			c1id = rpc.getC1id();
			ival = rpc.getLongValue();
			//conceptID = rpc.getConceptId();
			
			versionid = rpc.getVersion();
			positionid = -2;
			//positionid = rpc.getPositionId();
			
			
			statusid = rpc.getStatusId();
			pathid = rpc.getPathId();
		}
		BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
		BasicXMLStruct.getStringAtt(Long.toString(ival), CommonXMLStatics.VAL_ATT, localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);
	}

}
