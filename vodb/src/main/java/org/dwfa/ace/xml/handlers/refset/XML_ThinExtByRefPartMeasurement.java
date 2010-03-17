package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.vodb.types.ThinExtByRefPartMeasurement;
import org.w3c.dom.Element;

public class XML_ThinExtByRefPartMeasurement extends XML_RefSetBasic implements
		I_Handle_XML {

	ThinExtByRefPartMeasurement rpcm;
	
	Double mVal = new Double("-1");
	int uom = -1;
	
	
	
	
	public XML_ThinExtByRefPartMeasurement(ThinExtByRefPartMeasurement rpcm, Element parent) {
		super();
		this.rpcm = rpcm;
		this.parent = parent;
		process();
	}
	
	public XML_ThinExtByRefPartMeasurement(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}

	public void process() {
		refSetType = CommonXMLStatics.REFSET_TYPE_MEAS;
		getLocalE();
		
		if (!debug) {
			mVal = rpcm.getMeasurementValue();
			uom = rpcm.getUnitsOfMeasureId();
			versionid = rpcm.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpcm.getStatusId();
			pathid = rpcm.getPathId();
			
		}
		BasicXMLStruct.getIntAtt(uom, CommonXMLStatics.UOM_ID_ATT, localE);
		BasicXMLStruct.getStringAtt(Double.toString(mVal), CommonXMLStatics.VAL_ATT,localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);	

	}

}
