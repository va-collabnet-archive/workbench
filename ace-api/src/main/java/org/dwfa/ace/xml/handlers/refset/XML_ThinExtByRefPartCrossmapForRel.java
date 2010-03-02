package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.vodb.types.ThinExtByRefPartCrossmapForRel;
import org.w3c.dom.Element;

public class XML_ThinExtByRefPartCrossmapForRel extends XML_RefSetBasic implements
		I_Handle_XML {

	ThinExtByRefPartCrossmapForRel rpcm;
	
	int rFlagid = -1;
	int addCodeid = -1;
	int elemNum = -1;
	int blockNum = -1;
	
	
	
	public XML_ThinExtByRefPartCrossmapForRel(
			ThinExtByRefPartCrossmapForRel rpcm, Element parent) {
		super();
		this.rpcm = rpcm;
		this.parent = parent;
		process();
	}
	
	public XML_ThinExtByRefPartCrossmapForRel(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}

	public void process() {
		refSetType = CommonXMLStatics.REFSET_TYPE_CROSS;
		getLocalE();
		
		if (!debug) {
			rFlagid = rpcm.getRefineFlagId();
			addCodeid = rpcm.getAdditionalCodeId();
			elemNum = rpcm.getElementNo();
			blockNum = rpcm.getBlockNo();
			
			
			versionid = rpcm.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpcm.getStatusId();
			pathid = rpcm.getPathId();
		}
		BasicXMLStruct.getIntAtt(rFlagid, CommonXMLStatics.REFIN_FLAG_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(addCodeid, CommonXMLStatics.ADD_CODE_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(elemNum, CommonXMLStatics.ELEM_NO_ATT, localE);
		BasicXMLStruct.getIntAtt(blockNum, CommonXMLStatics.BLOCK_NO_ATT, localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);
	}


}
