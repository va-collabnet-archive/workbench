package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.vodb.types.ThinExtByRefPartInteger;
import org.w3c.dom.Element;

public class XML_ThinExtByRefPartInteger extends XML_RefSetBasic implements
		I_Handle_XML {

	ThinExtByRefPartInteger rpi;
	int ival = -1;
	
	
	public XML_ThinExtByRefPartInteger(ThinExtByRefPartInteger rpi, Element parent) {
		super();
		this.rpi = rpi;
		this.parent = parent;
		process();
	}
	
	public XML_ThinExtByRefPartInteger(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}
	public void process() {
		refSetType = CommonXMLStatics.REFSET_TYPE_INT;
		getLocalE();
		
		if (!debug) {
			ival = rpi.getIntValue();
			
			versionid = rpi.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpi.getStatusId();
			pathid = rpi.getPathId();
		}
		BasicXMLStruct.getIntAtt(ival, CommonXMLStatics.VAL_ATT, localE);

		addStdAtt(localE);
		
		parent.appendChild(localE);
	}
}
