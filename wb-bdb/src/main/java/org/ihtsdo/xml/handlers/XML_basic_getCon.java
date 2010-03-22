package org.ihtsdo.xml.handlers;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.ihtsdo.xml.handlers.XML_basic;
import org.w3c.dom.Element;

public class XML_basic_getCon extends XML_basic {
	
	public I_GetConceptData iconcept;
	
	public XML_basic_getCon() {
		super();
	}
	
	public void setIconXML(I_GetConceptData iconcept, Element parent) {
		this.parent = parent;
		this.iconcept = iconcept;
		setXML();
	}




}
