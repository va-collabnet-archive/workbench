package org.ihtsdo.xml.handlers.refset;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartBoolean extends XML_RefSetBasic implements I_Handle_XML {

	I_ExtendByRefPartBoolean rpb;	
	boolean rpbb = false;

	public XML_I_ExtendByRefPartBoolean(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	public XML_I_ExtendByRefPartBoolean(I_ExtendByRefPartBoolean rpb,
			Element parent) {
		super();
		this.rpb = rpb;
		this.parent = parent;
		getXML();
	}

	public void getXML() {
		refSetType = CommonXMLStatics.REFSET_TYPE_BOOL;
		getLocalE();

		if (!debug) {
			rpbb = rpb.getBooleanValue();
			
			versionid = rpb.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpb.getStatusId();
			pathid = rpb.getPathId();
		}
		BasicXMLStruct.getBoolAtt(rpbb, CommonXMLStatics.VAL_ATT, localE);
		
		addStdAtt(localE);

		
		parent.appendChild(localE);
	}

}
