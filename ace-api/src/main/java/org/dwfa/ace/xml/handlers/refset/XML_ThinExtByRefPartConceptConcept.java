package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.vodb.types.ThinExtByRefPartConceptConcept;
import org.w3c.dom.Element;

public class XML_ThinExtByRefPartConceptConcept extends XML_RefSetBasic implements I_Handle_XML {

	ThinExtByRefPartConceptConcept rpcc = null;
	int c1id = -1;
	int c2id = -1;
	int conceptID = -1;

	public XML_ThinExtByRefPartConceptConcept(
			ThinExtByRefPartConceptConcept rpcc, Element parent) {
		super();
		this.rpcc = rpcc;
		this.parent = parent;
		process();
	}

	public XML_ThinExtByRefPartConceptConcept(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}

	public void process() {

		refSetType = CommonXMLStatics.REFSET_TYPE_CONCON;
		getLocalE();

		if (!debug) {
			c1id = rpcc.getC1id();
			c2id = rpcc.getC2id();
			conceptID = rpcc.getConceptId();
			
			versionid = rpcc.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpcc.getStatusId();
			pathid = rpcc.getPathId();
		}
		BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(c2id, CommonXMLStatics.C2_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(conceptID, CommonXMLStatics.CONCEPT_ID_ATT,localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);

	}

}
