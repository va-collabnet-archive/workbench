package org.ihtsdo.xml.handlers.refset;

import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartCidInt extends XML_RefSetBasic implements I_Handle_XML {

	
	I_ExtendByRefPartCidInt rpc;
	int c1id = -1;
	int ival = -1;

	public XML_I_ExtendByRefPartCidInt(I_ExtendByRefPartCidInt rpc,
			Element parent) {
		super();
		this.rpc = rpc;
		this.parent = parent;
		process();
	}

	public XML_I_ExtendByRefPartCidInt(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}

	public void process() {
		
		refSetType = CommonXMLStatics.REFSET_TYPE_CIDINT;
		getLocalE();

		if (!debug) {
			c1id = rpc.getC1id();
			ival = rpc.getIntValue();
			//conceptID = rpc.getConceptId();
			
			versionid = rpc.getVersion();
			positionid = -2;
			//positionid = rpc.getPositionId();
			
			
			statusid = rpc.getStatusId();
			pathid = rpc.getPathId();
		}
		BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(ival, CommonXMLStatics.VAL_ATT, localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);
	}

}
