package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.vodb.types.ThinExtByRefPartConceptString;
import org.w3c.dom.Element;

public class XML_ThinExtByRefPartConceptString extends XML_RefSetBasic implements
		I_Handle_XML {

	ThinExtByRefPartConceptString rpcs;
	int c1id = -1;
	String text = "test";
	
	public XML_ThinExtByRefPartConceptString(ThinExtByRefPartConceptString rpcs, Element parent) {
		super();
		this.rpcs = rpcs;
		this.parent = parent;
		process();
	}
	
	public XML_ThinExtByRefPartConceptString(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}

	public void process() {
		refSetType = CommonXMLStatics.REFSET_TYPE_CONSTR;
		getLocalE();
		
		if (!debug) {
			c1id = rpcs.getC1id();
			text = rpcs.getStr();
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
