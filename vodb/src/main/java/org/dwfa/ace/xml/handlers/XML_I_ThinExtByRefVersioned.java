package org.dwfa.ace.xml.handlers;


import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_ThinExtByRefVersioned extends XML_basic implements
		I_Handle_XML {
	
	I_ThinExtByRefVersioned ext;
	public int refSetIDi = -1;
	int compIDi = -1;
	int typeIDi = -1;
	int memberIDi = -1;

	public XML_I_ThinExtByRefVersioned(I_ThinExtByRefVersioned ext, Element parent) {
		super();
		this.ext = ext;
		this.parent = parent;
		process();
	}
	
	public XML_I_ThinExtByRefVersioned(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}
	public void process() {
		Element localE = parent.getOwnerDocument().createElement(CommonXMLStatics.EXT_ENAME);
		
		if (!debug) {
			memberIDi = ext.getMemberId();
			refSetIDi = ext.getRefsetId();
			compIDi = ext.getComponentId();
			typeIDi = ext.getTypeId();
						
		}
		
		BasicXMLStruct.getIntAtt(memberIDi, CommonXMLStatics.MEMBER_ID_ATT, localE);
		//BasicXMLStruct.getIntAtt(refSetIDi, CommonXMLStatics.REFSET_ID_ATT,localE);
		
		BasicXMLStruct.getIdUuidAtts(refSetIDi,CommonXMLStatics.REFSET_ID_ATT,localE);
		
		BasicXMLStruct.getIntAtt(compIDi, CommonXMLStatics.COMPONENT_ID_ATT,localE);
		BasicXMLStruct.getIntAtt(typeIDi, CommonXMLStatics.TYPE_ID_ATT,localE);
		
		Element versionsE = parent.getOwnerDocument().createElement(CommonXMLStatics.VERSIONS_ENAME);
		if (!debug) {
		for (I_ThinExtByRefPart refpart : ext.getMutableParts()) {
			new XML_I_ThinExtByRefPart(refpart,versionsE);
		}
		}
		else{
			new XML_I_ThinExtByRefPart(true,versionsE);
		}
		localE.appendChild(versionsE);
		parent.appendChild(localE);
	}

}
