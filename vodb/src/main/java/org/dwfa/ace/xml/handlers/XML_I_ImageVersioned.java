package org.dwfa.ace.xml.handlers;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_ImageVersioned extends XML_basic implements I_Handle_XML {

	I_ImageVersioned img;
	int imageid = -1;
	int con_id = -1;
	String format = "test";
	String imgBytes="ImageByteArray";
	
	
	public XML_I_ImageVersioned(I_ImageVersioned img, Element parent) {
		super();
		this.img = img;
		this.parent = parent;
		process();
	}
	
	public XML_I_ImageVersioned(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}
	public void process() {
		Element localE = parent.getOwnerDocument().createElement(CommonXMLStatics.IMG_ENAME);
		
		if (!debug) {
			con_id = img.getConceptId();
			imageid = img.getImageId();
			format = img.getFormat();
			BasicXMLStruct.getNativeIdAtts_i(imageid,localE);
			BasicXMLStruct.getIntAtt(con_id, CommonXMLStatics.CONCEPT_ID_ATT, localE);
			BasicXMLStruct.getStringAtt(format, CommonXMLStatics.FORMAT_ATT,localE);
			BasicXMLStruct.getStringAtt(imgBytes, CommonXMLStatics.IMGBYTES_ENAME,localE);
			for (I_ImagePart imgP : img.getMutableParts()) {
				XML_I_ImagePart x_imp = new XML_I_ImagePart(imgP,localE);
			}		
		}
		else{
			BasicXMLStruct.getNativeIdAtts_i(imageid,localE);
			BasicXMLStruct.getIntAtt(con_id, CommonXMLStatics.CONCEPT_ID_ATT, localE);
			BasicXMLStruct.getStringAtt(format, CommonXMLStatics.FORMAT_ATT,localE);
			BasicXMLStruct.getStringAtt(imgBytes, CommonXMLStatics.IMGBYTES_ENAME,localE);
			XML_I_ImagePart x_imp = new XML_I_ImagePart(debug,localE);
		}
		parent.appendChild(localE);
	}

}
