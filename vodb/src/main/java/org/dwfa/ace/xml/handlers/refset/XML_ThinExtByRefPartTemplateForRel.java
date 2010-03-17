package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.vodb.types.ThinExtByRefPartTemplateForRel;
import org.w3c.dom.Element;

public class XML_ThinExtByRefPartTemplateForRel extends XML_RefSetBasic implements
		I_Handle_XML {

	ThinExtByRefPartTemplateForRel rpt;
	
	int valTypeid = -1;
	int card = -1;
	int semStatid = -1;
	int browValOrd = -1;
	int browAttOrd = -1;
	int noteScreenOrd = -1;
	int attDispStat = -1;
	int charStatid = -1;
	
	
	public XML_ThinExtByRefPartTemplateForRel(ThinExtByRefPartTemplateForRel rpt, Element parent) {
		super();
		this.rpt = rpt;
		this.parent = parent;
		process();
	}
	
	public XML_ThinExtByRefPartTemplateForRel(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}

	public void process() {
		refSetType = CommonXMLStatics.REFSET_TYPE_TEMPL;
		getLocalE();
		
		if (!debug) {
			valTypeid = rpt.getValueTypeId();
			card = rpt.getCardinality();
			semStatid = rpt.getSemanticStatusId();
			browValOrd = rpt.getBrowseValueOrder();
			browAttOrd = rpt.getBrowseAttributeOrder();
			noteScreenOrd = rpt.getNotesScreenOrder();
			attDispStat = rpt.getAttributeDisplayStatusId();
			charStatid = rpt.getCharacteristicStatusId();
		
			versionid = rpt.getVersion();
			positionid = -2;
			//positionid = rpi.getPositionId();
			statusid = rpt.getStatusId();
			pathid = rpt.getPathId();
			
		}
		BasicXMLStruct.getIntAtt(valTypeid, CommonXMLStatics.VAL_TYPE_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(card, CommonXMLStatics.CARD_ATT, localE);
		BasicXMLStruct.getIntAtt(semStatid, CommonXMLStatics.SEM_STATUS_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(browValOrd, CommonXMLStatics.BROWSE_VAL_ORDER_ATT, localE);
		BasicXMLStruct.getIntAtt(browAttOrd, CommonXMLStatics.BROWSE_ORDER_ATT, localE);
		BasicXMLStruct.getIntAtt(noteScreenOrd, CommonXMLStatics.NOTE_SCREEN_ORDER_ATT, localE);
		BasicXMLStruct.getIntAtt(attDispStat, CommonXMLStatics.ATT_DISP_ID_ATT, localE);
		BasicXMLStruct.getIntAtt(charStatid, CommonXMLStatics.CHAR_STATUS_ID_ATT, localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);

	}

}
