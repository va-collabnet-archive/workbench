package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.vodb.types.ThinExtByRefPartConceptConceptString;
import org.w3c.dom.Element;

public class XML_ThinExtByRefPartConceptConceptString extends XML_RefSetBasic implements I_Handle_XML {

	ThinExtByRefPartConceptConceptString rpccs;
	int c1id = -1;
	int c2id = -1;
	String text = "test";
	
	public XML_ThinExtByRefPartConceptConceptString(
			ThinExtByRefPartConceptConceptString rpccs,Element parent) {
		super();
		this.rpccs = rpccs;
		this.parent = parent;
		process();
	}
	
	public XML_ThinExtByRefPartConceptConceptString(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}
	

	public void process() {
		
		refSetType = CommonXMLStatics.REFSET_TYPE_CONCONSTR;
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
