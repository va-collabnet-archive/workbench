package org.ihtsdo.xml.handlers;

import org.dwfa.ace.api.I_GetConceptData;
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
