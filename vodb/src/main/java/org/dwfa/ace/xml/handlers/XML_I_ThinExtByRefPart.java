package org.dwfa.ace.xml.handlers;

import org.dwfa.ace.xml.handlers.refset.*;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.xml.common.CommonXMLStatics;
import org.dwfa.vodb.types.ThinExtByRefPartBoolean;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptConceptString;
import org.dwfa.vodb.types.ThinExtByRefPartConceptString;
import org.dwfa.vodb.types.ThinExtByRefPartCrossmapForRel;
import org.dwfa.vodb.types.ThinExtByRefPartInteger;
import org.dwfa.vodb.types.ThinExtByRefPartLanguage;
import org.dwfa.vodb.types.ThinExtByRefPartMeasurement;
import org.dwfa.vodb.types.ThinExtByRefPartString;
import org.dwfa.vodb.types.ThinExtByRefPartTemplateForRel;
import org.w3c.dom.Element;

public class XML_I_ThinExtByRefPart extends XML_basic implements I_Handle_XML {

	I_ThinExtByRefPart extRefP;
	public String classN = "test";
	
	
	
	
	public XML_I_ThinExtByRefPart(I_ThinExtByRefPart extRefP, Element parent) {
		super();
		this.extRefP = extRefP;
		this.parent = parent;
		process();
	}
	
	public XML_I_ThinExtByRefPart(boolean debug, Element parent) {
		super();
		this.debug = debug;
		this.parent = parent;
		process();
	}

	
	public void process() {
		
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
		if(classN.equals(CommonXMLStatics.CLASS_NAME_TE_BOOL)){
			new XML_ThinExtByRefPartBoolean((ThinExtByRefPartBoolean)extRefP,localE);	
		}
		else if(classN.equals(CommonXMLStatics.CLASS_NAME_TE_CON)){
			new XML_ThinExtByRefPartConcept((ThinExtByRefPartConcept)extRefP,localE);
		}
		else if(classN.equals(CommonXMLStatics.CLASS_NAME_TE_CONCON)){
			new XML_ThinExtByRefPartConceptConcept((ThinExtByRefPartConceptConcept)extRefP,localE);
		}
		else if(classN.equals(CommonXMLStatics.CLASS_NAME_TE_CONCONSTR)){
			new XML_ThinExtByRefPartConceptConceptString((ThinExtByRefPartConceptConceptString)extRefP,localE);	
		}
		else if(classN.equals(CommonXMLStatics.CLASS_NAME_TE_CONSTR)){
			new XML_ThinExtByRefPartConceptString((ThinExtByRefPartConceptString)extRefP,localE);	
		}
		else if(classN.equals(CommonXMLStatics.CLASS_NAME_TE_CROSS)){
			new XML_ThinExtByRefPartCrossmapForRel((ThinExtByRefPartCrossmapForRel)extRefP,localE);	
		}
		else if(classN.equals(CommonXMLStatics.CLASS_NAME_TE_INT)){
			new XML_ThinExtByRefPartInteger((ThinExtByRefPartInteger)extRefP,localE);	
		}
		else if(classN.equals(CommonXMLStatics.CLASS_NAME_TE_LANG)){
			new XML_ThinExtByRefPartLanguage((ThinExtByRefPartLanguage)extRefP,localE);	
		}
		else if(classN.equals(CommonXMLStatics.CLASS_NAME_TE_MEAS)){
			new XML_ThinExtByRefPartMeasurement((ThinExtByRefPartMeasurement)extRefP,localE);	
		}
		else if(classN.equals(CommonXMLStatics.CLASS_NAME_TE_STR)){
			new XML_ThinExtByRefPartString((ThinExtByRefPartString)extRefP,localE);	
		}
		else if(classN.equals(CommonXMLStatics.CLASS_NAME_TE_TEMPL)){
			new XML_ThinExtByRefPartTemplateForRel((ThinExtByRefPartTemplateForRel)extRefP,localE);	
		}		
		}
		else{
			//If Debug print once of each type
			new XML_ThinExtByRefPartBoolean(true,localE);
			new XML_ThinExtByRefPartConcept(true,localE);
			new XML_ThinExtByRefPartConceptConcept(true,localE);
			new XML_ThinExtByRefPartConceptConceptString(true,localE);
			new XML_ThinExtByRefPartConceptString(true,localE);
			new XML_ThinExtByRefPartCrossmapForRel(true,localE);
			new XML_ThinExtByRefPartInteger(true,localE);
			new XML_ThinExtByRefPartLanguage(true,localE);
			new XML_ThinExtByRefPartMeasurement(true,localE);
			new XML_ThinExtByRefPartString(true,localE);
			new XML_ThinExtByRefPartTemplateForRel(true,localE);			
		}
		
		
	}

}
