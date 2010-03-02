package org.dwfa.ace.xml.handlers.refset;

import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.ace.xml.handlers.BasicXMLStruct;
import org.dwfa.ace.xml.handlers.I_Handle_XML;
import org.dwfa.ace.xml.handlers.XML_basic;
import org.w3c.dom.Element;

public class XML_RefSetBasic extends XML_basic implements I_Handle_XML {

	public Element localE = null;
	public String refSetType = "refSetType";
	
	public void process() throws Exception {
		

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
