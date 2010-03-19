package org.ihtsdo.xml.handlers;

import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.ihtsdo.xml.handlers.XML_basic;
import org.w3c.dom.Element;

public class XML_basic_econ extends XML_basic {
	
	public EConcept econcept;
	
	public XML_basic_econ() {
		super();
	}
	
	public void setEconXML(EConcept eConcept, Element parent) {
		this.parent = parent;
		this.econcept = econcept;
		setXML();
	}




}
