package org.ihtsdo.xml.handlers.refset;

import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.BasicXMLStruct;
import org.ihtsdo.xml.handlers.I_Handle_XML;
import org.ihtsdo.xml.handlers.XML_basic;
import org.w3c.dom.Element;

public class XML_RefSetBasic extends XML_basic implements I_Handle_XML {

	public Element localE = null;
	public String refSetType = "refSetType";
	
	public void getXML() {
		

	}
	
	public void setXML() {
		

	}

	public Element getLocalE() {
		if(localE == null){
			localE = parent.getOwnerDocument().createElement(CommonXMLStatics.REFSET_VAL_ENAME);
			BasicXMLStruct.getStringAtt(refSetType,CommonXMLStatics.REFSET_TYPE_ATT,localE);
		}
		return localE;
	}

	public void setLocalE(Element localE) {
		this.localE = localE;
	}
	
	public String getRefSetType() {
		return refSetType;
	}

	public void setRefSetType(String refSetType) {
		this.refSetType = refSetType;
	}

}
