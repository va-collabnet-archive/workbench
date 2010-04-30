package org.ihtsdo.xml.handlers.refset;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartLong extends XML_RefSetBasic implements
		I_Handle_XML {

	I_ExtendByRefPartLong rpi;
	long ival = -1;
	
	
	public XML_I_ExtendByRefPartLong(I_ExtendByRefPartLong rpi, Element parent) {
		super();
		this.rpi = rpi;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_ExtendByRefPartLong(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}
	public void getXML() {
		refSetType = CommonXMLStatics.REFSET_TYPE_LONG;
		getLocalE();
		
		if (!debug) {
			ival = rpi.getLongValue();
			
			versionid = rpi.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpi.getStatusId();
			pathid = rpi.getPathId();
		}
		BasicXMLStruct.getStringAtt(Long.toString(ival), CommonXMLStatics.VAL_ATT, localE);

		addStdAtt(localE);
		
		parent.appendChild(localE);
	}
}
