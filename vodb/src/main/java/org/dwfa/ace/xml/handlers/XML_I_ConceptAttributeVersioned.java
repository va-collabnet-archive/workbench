package org.dwfa.ace.xml.handlers;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_ConceptAttributeVersioned extends XML_basic implements
		I_Handle_XML {

	public I_ConceptAttributeVersioned conAttV;
	int con_id = -1;
	int numVer = -1;
	
	
	public XML_I_ConceptAttributeVersioned(I_ConceptAttributeVersioned conAttV, Element parent) {
		super();
		this.conAttV = conAttV;
		this.parent = parent;
		process();
	}
	
	public XML_I_ConceptAttributeVersioned(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}

	public void process() {
		Element localE = parent.getOwnerDocument().createElement(CommonXMLStatics.CAV_ENAME);
		
		if (!debug) {
			con_id = conAttV.getConId();
			numVer = conAttV.versionCount();
			BasicXMLStruct.getNativeIdAtts_i(con_id,localE);
			BasicXMLStruct.getNumVersionAtts(numVer,localE);
			for (I_ConceptAttributePart cap : conAttV.getMutableParts()) {
				XML_I_ConceptAttributePart xcap = new XML_I_ConceptAttributePart(cap,localE);
			}		
		}
		else{
			BasicXMLStruct.getNativeIdAtts_i(con_id,localE);
			BasicXMLStruct.getNumVersionAtts(numVer,localE);
			XML_I_ConceptAttributePart xcap = new XML_I_ConceptAttributePart(debug,localE);
		}
		parent.appendChild(localE);

	}

}
