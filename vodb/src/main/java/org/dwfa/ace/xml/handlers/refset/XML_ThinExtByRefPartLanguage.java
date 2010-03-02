package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.vodb.types.ThinExtByRefPartLanguage;
import org.w3c.dom.Element;

public class XML_ThinExtByRefPartLanguage extends XML_RefSetBasic implements
		I_Handle_XML {
	
	ThinExtByRefPartLanguage rpl;
	int acceptid = -1;
	int correctid = -1;
	int dosid = -1;
	
	

	public XML_ThinExtByRefPartLanguage(ThinExtByRefPartLanguage rpl, Element parent) {
		super();
		this.rpl = rpl;
		this.parent = parent;
		process();
	}
	
	public XML_ThinExtByRefPartLanguage(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}
	
	public void process() {
		refSetType = CommonXMLStatics.REFSET_TYPE_LANG;
		getLocalE();
		
		if (!debug) {
			acceptid = rpl.getAcceptabilityId();
			correctid = rpl.getCorrectnessId();
			dosid = rpl.getDegreeOfSynonymyId();
			
			versionid = rpl.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpl.getStatusId();
			pathid = rpl.getPathId();
		}
		BasicXMLStruct.getIntAtt(acceptid, CommonXMLStatics.ACCEPT_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(correctid, CommonXMLStatics.CORRECT_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(dosid, CommonXMLStatics.DEG_SYN_ID_ATT, localE);

		addStdAtt(localE);
		
		parent.appendChild(localE);
		
		
	}

}
