package org.ihtsdo.xml.handlers;

import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_Identify extends XML_basic_getCon implements I_Handle_XML {

	I_Identify id;
	int id_id = -1;
	int numver = -1;

	public XML_I_Identify() {
		super();
	}

	public XML_I_Identify(I_Identify id, Element parent) {
		super();
		this.id = id;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_Identify(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
			getXML();

	}
	
	public void setXML() {
		
		//econcept.
		
	}
	
	public void getXML() {
		Element localE = parent.getOwnerDocument().createElement(CommonXMLStatics.IDS_ENAME);
		
		if (!debug) {
			id_id = id.getNid();
			numver = id.getIdVersions().size();
			BasicXMLStruct.getNativeIdAtts_i(id_id,localE);
			BasicXMLStruct.getNumVersionAtts(numver, localE);
			for (I_IdVersion p: id.getIdVersions()) {
				XML_I_IdPart x_idp = new XML_I_IdPart(p,localE);
			}		
		}
		else{
			BasicXMLStruct.getNativeIdAtts_i(id_id,localE);
			BasicXMLStruct.getNumVersionAtts(numver, localE);;
			XML_I_IdPart x_idp = new XML_I_IdPart(debug,localE);
		}
		parent.appendChild(localE);

	}

	
	
	
	
	
	
}
