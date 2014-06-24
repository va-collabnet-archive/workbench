package org.ihtsdo.xml.handlers;

import org.dwfa.ace.api.I_RelPart;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_RelPart extends XML_basic implements I_Handle_XML {

	I_RelPart relP;
	int typeid = -1;
	int charid = -1;
	int refid = -1;
	int groupid = -1;
	
	
	public XML_I_RelPart(I_RelPart relP, Element parent) {
		super();
		this.relP = relP;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_RelPart(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	public void getXML() {
		Element localE = parent.getOwnerDocument().createElement(CommonXMLStatics.REL_PART_ENAME);
		
		if (!debug) {
			
			typeid = relP.getTypeId();
			charid = relP.getCharacteristicId();
			refid = relP.getRefinabilityId();
			groupid = relP.getGroup();

			versionid = relP.getVersion();
			positionid = -2;
			statusid = relP.getStatusId();
			pathid = relP.getPathId();	
			
			BasicXMLStruct.getVersionThinDateAtts(versionid,localE);
		}
		//BasicXMLStruct.getNativeIdAtts_i(typeid,localE);
		BasicXMLStruct.getIdUuidAtts(typeid,CommonXMLStatics.TYPE_ID_ATT,localE);
		BasicXMLStruct.getIntAtt(charid, CommonXMLStatics.CHAR_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(refid, CommonXMLStatics.REFIN_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(groupid, CommonXMLStatics.GROUP_ATT, localE);
		
		
		addStdAtt(localE);
		
		parent.appendChild(localE);

	}

}
