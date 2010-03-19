package org.ihtsdo.xml.handlers;

import org.dwfa.ace.api.I_DescriptionPart;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_DescriptionPart extends XML_basic implements I_Handle_XML {

	I_DescriptionPart descP;
	int typeid = -1;
	String lang="test";
	String text="test";
	boolean initCaseSig = false;
	
	public XML_I_DescriptionPart(I_DescriptionPart descP, Element parent) {
		super();
		this.descP = descP;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_DescriptionPart(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}
	public void getXML() {
		Element localE = parent.getOwnerDocument().createElement(CommonXMLStatics.DESCP_ENAME);
		
		if (!debug) {
			
			typeid = descP.getTypeId();
			lang = descP.getLang();
			text = descP.getText();
			initCaseSig = descP.isInitialCaseSignificant();
			
			versionid = descP.getVersion();
			positionid = -2;
			statusid = descP.getStatusId();
			pathid = descP.getPathId();
			BasicXMLStruct.getVersionThinDateAtts(versionid,localE);
			
		}
		//BasicXMLStruct.getNativeIdAtts_i(typeid,localE);
		BasicXMLStruct.getIdUuidAtts(typeid,CommonXMLStatics.TYPE_ID_ATT,localE);
		BasicXMLStruct.getStringAtt(lang, CommonXMLStatics.LANG_ATT,localE);
		BasicXMLStruct.getStringAtt(text, CommonXMLStatics.TEXT_ATT,localE);
		BasicXMLStruct.getBoolAtt(initCaseSig, CommonXMLStatics.INITCASESIG_ATT,localE);
		
		addStdAtt(localE);
		
		parent.appendChild(localE);

	}

}
