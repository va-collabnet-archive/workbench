package org.ihtsdo.xml.handlers.refset;

import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidFloat;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;

import org.w3c.dom.Element;

public class XML_I_ExtendByRefPartCidFloat extends XML_RefSetBasic implements
		I_Handle_XML {

	I_ExtendByRefPartCidFloat rpcm;
	
	float mVal = -1;
	int uom = -1;
	int c1id = -1;
	
	
	public XML_I_ExtendByRefPartCidFloat(I_ExtendByRefPartCidFloat rpcm, Element parent) {
		super();
		this.rpcm = rpcm;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_ExtendByRefPartCidFloat(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	public void getXML() {
		refSetType = CommonXMLStatics.REFSET_TYPE_CIDFLOAT;
		getLocalE();
		
		if (!debug) {
			c1id = rpcm.getC1id();
			mVal = rpcm.getMeasurementValue();
			uom = rpcm.getUnitsOfMeasureId();
			versionid = rpcm.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpcm.getStatusId();
			pathid = rpcm.getPathId();
			
		}
		BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(uom, CommonXMLStatics.UOM_ID_ATT, localE);
		BasicXMLStruct.getStringAtt(Float.toString(mVal), CommonXMLStatics.VAL_ATT,localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);	

	}

}
