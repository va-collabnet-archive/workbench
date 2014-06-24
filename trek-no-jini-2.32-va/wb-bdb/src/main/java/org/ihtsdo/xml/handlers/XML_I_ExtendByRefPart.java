package org.ihtsdo.xml.handlers;

import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidFloat;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartLong;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartBoolean;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartCid;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartCidCid;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartCidCidCid;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartCidCidString;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartCidFloat;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartCidInt;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartCidLong;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartCidString;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartInt;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartLong;
import org.ihtsdo.xml.handlers.refset.XML_I_ExtendByRefPartStr;
import org.w3c.dom.Element;

public class XML_I_ExtendByRefPart extends XML_basic implements I_Handle_XML {

	I_ExtendByRefPart extRefP;
	public String classN = "test";
	
	
	
	
	public XML_I_ExtendByRefPart(I_ExtendByRefPart extRefP, Element parent) {
		super();
		this.extRefP = extRefP;
		this.parent = parent;
		getXML();
	}
	
	public XML_I_ExtendByRefPart(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		getXML();
	}

	
	public void getXML() {
		
		Element localE = parent.getOwnerDocument().createElement(CommonXMLStatics.REFSET_PART_ENAME);
		
		if (!debug) {
			
			classN = extRefP.getClass().getSimpleName();
			versionid = extRefP.getVersion();
			positionid = -2;
			statusid = extRefP.getStatusId();
			pathid = extRefP.getPathId();		
			BasicXMLStruct.getVersionThickDateAtts(versionid,localE);
		}

		BasicXMLStruct.getStringAtt(classN, CommonXMLStatics.CLASS_NAME_ATT,localE);
		addStdAtt(localE);
		
		addRF(localE);
		
		parent.appendChild(localE);
		
		

	}
	
	public void addRF(Element localE){
		
		if (!debug) {
			if(extRefP instanceof I_ExtendByRefPartBoolean) {
				new XML_I_ExtendByRefPartBoolean((I_ExtendByRefPartBoolean)extRefP,localE);
			}
			else if(extRefP instanceof I_ExtendByRefPartCid) {
				new XML_I_ExtendByRefPartCid((I_ExtendByRefPartCid)extRefP,localE);
			}
			else if(extRefP instanceof I_ExtendByRefPartCidCid) {
				new XML_I_ExtendByRefPartCidCid((I_ExtendByRefPartCidCid)extRefP,localE);
			}
			else if(extRefP instanceof I_ExtendByRefPartCidCidCid) {
				new XML_I_ExtendByRefPartCidCidCid((I_ExtendByRefPartCidCidCid)extRefP,localE);
			}
			else if(extRefP instanceof I_ExtendByRefPartCidCidString) {
				new XML_I_ExtendByRefPartCidCidString((I_ExtendByRefPartCidCidString)extRefP,localE);
			}
			else if(extRefP instanceof I_ExtendByRefPartCidFloat) {
				new XML_I_ExtendByRefPartCidFloat((I_ExtendByRefPartCidFloat)extRefP,localE);
			}
			else if(extRefP instanceof I_ExtendByRefPartCidInt) {
				new XML_I_ExtendByRefPartCidInt((I_ExtendByRefPartCidInt)extRefP,localE);
			}
			else if(extRefP instanceof I_ExtendByRefPartCidLong) {
				new XML_I_ExtendByRefPartCidLong((I_ExtendByRefPartCidLong)extRefP,localE);
			}
			else if(extRefP instanceof I_ExtendByRefPartCidString) {
				new XML_I_ExtendByRefPartCidString((I_ExtendByRefPartCidString)extRefP,localE);
			}
			else if(extRefP instanceof I_ExtendByRefPartInt) {
				new XML_I_ExtendByRefPartInt((I_ExtendByRefPartInt)extRefP,localE);
			}
			else if(extRefP instanceof I_ExtendByRefPartLong) {
				new XML_I_ExtendByRefPartLong((I_ExtendByRefPartLong)extRefP,localE);
			}
			else if(extRefP instanceof I_ExtendByRefPartStr) {
				new XML_I_ExtendByRefPartStr((I_ExtendByRefPartStr)extRefP,localE);
			}
		}

		else{
			//If Debug print once of each type
			new XML_I_ExtendByRefPartBoolean(true,localE);
			new XML_I_ExtendByRefPartCid(true,localE);
			new XML_I_ExtendByRefPartCidCid(true,localE);
			new XML_I_ExtendByRefPartCidCidCid(true,localE);
			new XML_I_ExtendByRefPartCidCidString(true,localE);
			new XML_I_ExtendByRefPartCidFloat(true,localE);
			new XML_I_ExtendByRefPartCidInt(true,localE);
			new XML_I_ExtendByRefPartCidLong(true,localE);
			new XML_I_ExtendByRefPartCidString(true,localE);
			new XML_I_ExtendByRefPartInt(true,localE);
			new XML_I_ExtendByRefPartLong(true,localE);
			new XML_I_ExtendByRefPartStr(true,localE);			
		}
		
		
	}

}
