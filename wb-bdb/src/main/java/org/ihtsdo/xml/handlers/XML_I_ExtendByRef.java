package org.ihtsdo.xml.handlers;


import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_ExtendByRef extends XML_basic_getCon implements
		I_Handle_XML {
	
	I_ExtendByRef ext;
	public int refSetIDi = -1;
	int compIDi = -1;
	int typeIDi = -1;
	int memberIDi = -1;
	
	

	public XML_I_ExtendByRef() {
		super();
	}

	public XML_I_ExtendByRef(I_ExtendByRef ext, Element parent) {
		super();
		this.ext = ext;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_ExtendByRef(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	
	public void setXML(){
		
	}
	
	public void getXML() {
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
		for (I_ExtendByRefPart refpart : ext.getMutableParts()) {
			new XML_I_ExtendByRefPart(refpart,versionsE);
		}
		}
		else{
			new XML_I_ExtendByRefPart(true,versionsE);
		}
		localE.appendChild(versionsE);
		parent.appendChild(localE);
	}

}
