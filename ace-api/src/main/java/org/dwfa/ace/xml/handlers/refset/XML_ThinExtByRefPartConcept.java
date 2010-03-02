package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.w3c.dom.Element;

public class XML_ThinExtByRefPartConcept extends XML_RefSetBasic implements I_Handle_XML {

	
	ThinExtByRefPartConcept rpc;
	int c1id = -1;
	int conceptID = -1;

	public XML_ThinExtByRefPartConcept(ThinExtByRefPartConcept rpc,
			Element parent) {
		super();
		this.rpc = rpc;
		this.parent = parent;
		process();
	}

	public XML_ThinExtByRefPartConcept(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}

	public void process() {
		
		refSetType = CommonXMLStatics.REFSET_TYPE_CON;
		getLocalE();

		if (!debug) {
			c1id = rpc.getC1id();
			conceptID = rpc.getConceptId();
			
			versionid = rpc.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpc.getStatusId();
			pathid = rpc.getPathId();
		}
		BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(conceptID, CommonXMLStatics.CONCEPT_ID_ATT,localE);
		addStdAtt(localE);
		
		parent.appendChild(localE);
	}

}
