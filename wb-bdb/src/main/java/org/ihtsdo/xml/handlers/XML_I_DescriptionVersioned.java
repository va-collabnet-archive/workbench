package org.ihtsdo.xml.handlers;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_DescriptionVersioned extends XML_basic_getCon implements
		I_Handle_XML {

	I_DescriptionVersioned desc;
	int desc_id = -1;
	int con_id = -1;
	
	
	
	public XML_I_DescriptionVersioned() {
		super();
	}

	public XML_I_DescriptionVersioned(I_DescriptionVersioned desc, Element parent) {
		super();
		this.desc = desc;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_DescriptionVersioned(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	
	public void setXML(){
		
	}
	
	public void getXML() {
		Element localE = parent.getOwnerDocument().createElement(CommonXMLStatics.DESC_ENAME);
		
		if (!debug) {
			con_id = desc.getConceptId();
			desc_id = desc.getDescId();
			BasicXMLStruct.getNativeIdAtts_i(desc_id,localE);
			BasicXMLStruct.getIntAtt(con_id, CommonXMLStatics.CONCEPT_ID_ATT, localE);
			for (I_DescriptionPart desl : desc.getMutableParts()) {
				XML_I_DescriptionPart x_idp = new XML_I_DescriptionPart(desl,localE);
			}		
		}
		else{
			BasicXMLStruct.getNativeIdAtts_i(desc_id,localE);
			BasicXMLStruct.getIntAtt(con_id, CommonXMLStatics.CONCEPT_ID_ATT, localE);
			XML_I_DescriptionPart x_idp = new XML_I_DescriptionPart(debug,localE);
		}
		parent.appendChild(localE);

	}

}
