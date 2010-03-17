package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.vodb.types.ThinExtByRefPartString;
import org.w3c.dom.Element;

public class XML_ThinExtByRefPartString extends XML_RefSetBasic implements
		I_Handle_XML {
	
	ThinExtByRefPartString rps;
	String text = "test";
	
	

	public XML_ThinExtByRefPartString(ThinExtByRefPartString rps, Element parent) {
		super();
		this.rps = rps;
		this.parent = parent;
		process();
	}
	
	public XML_ThinExtByRefPartString(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}

	public void process() {
		refSetType = CommonXMLStatics.REFSET_TYPE_STR;
		getLocalE();
		
		if (!debug) {
			text = rps.getStringValue();
			versionid = rps.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rps.getStatusId();
			pathid = rps.getPathId();
			
		}
		BasicXMLStruct.getStringAtt(text, CommonXMLStatics.VAL_ATT,localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);

	}

}
