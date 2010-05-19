package org.ihtsdo.xml.handlers.refset;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartInt extends XML_RefSetBasic implements
		I_Handle_XML {

	I_ExtendByRefPartInt rpi;
	int ival = -1;
	
	
	public XML_I_ExtendByRefPartInt(I_ExtendByRefPartInt rpi, Element parent) {
		super();
		this.rpi = rpi;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_ExtendByRefPartInt(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}
	public void getXML() {
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
