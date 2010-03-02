package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.vodb.types.ThinExtByRefPartBoolean;
import org.w3c.dom.Element;

public class XML_ThinExtByRefPartBoolean extends XML_RefSetBasic implements I_Handle_XML {

	ThinExtByRefPartBoolean rpb;	
	boolean rpbb = false;

	public XML_ThinExtByRefPartBoolean(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}

	public XML_ThinExtByRefPartBoolean(ThinExtByRefPartBoolean rpb,
			Element parent) {
		super();
		this.rpb = rpb;
		this.parent = parent;
		process();
	}

	public void process() {
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
