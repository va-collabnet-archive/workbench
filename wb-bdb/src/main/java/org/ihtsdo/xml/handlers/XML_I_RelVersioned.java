package org.ihtsdo.xml.handlers;


import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.w3c.dom.Element;

public class XML_I_RelVersioned extends XML_basic_getCon implements I_Handle_XML {

	I_RelVersioned<?> rel;
	/** If False then is a src relationship **/
	boolean dest = false;
	String relN;
	
	int relid = -1;
	int c1id = -1;
	int c2id = -1;
	//int nid = -1;

	
	
	public XML_I_RelVersioned(I_RelVersioned rel, Element parent, boolean dest) {
		super();
		this.rel = rel;
		this.parent = parent;
		this.dest = dest;
		getXML();
	}
	
	public XML_I_RelVersioned(boolean dest) {
		super();
		this.dest = dest;
		
	}

	public XML_I_RelVersioned(boolean debug, Element parent, boolean dest) {
		super();
		this.debug = debug;
		this.parent = parent;
		this.dest = dest;
		getXML();
	}

	
	public void setXML() {
		
	}
	
	public void getXML() {
		setSrcDest();
		
		Element localE = parent.getOwnerDocument().createElement(relN);
		
		if (!debug) {
			relid = rel.getRelId();
			c1id = rel.getC1Id();
			c2id = rel.getC2Id();
			//nid = rel.getNid();
		
			
			BasicXMLStruct.getNativeIdAtts_i(relid,localE);
			setDestSrcId(c1id, c2id,localE);

			//BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
			//BasicXMLStruct.getIntAtt(c2id, CommonXMLStatics.C2_ID_ATT, localE);
			//BasicXMLStruct.getIntAtt(nid, CommonXMLStatics.N_ID_ATT, localE);
			for (I_RelPart relP : rel.getMutableParts()) {
				XML_I_RelPart x_rp = new XML_I_RelPart(relP,localE);
			}		
		}
		else{
			BasicXMLStruct.getNativeIdAtts_i(relid,localE);
			setDestSrcId(c1id, c2id,localE);
			//BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
			//BasicXMLStruct.getIntAtt(c2id, CommonXMLStatics.C2_ID_ATT, localE);
			//BasicXMLStruct.getIntAtt(nid, CommonXMLStatics.N_ID_ATT, localE);
			XML_I_RelPart x_rp = new XML_I_RelPart(debug,localE);
		}
		parent.appendChild(localE);

	}
	
	public void setDestSrcId(int c1id, int c2id, Element localE){
		if(dest){
			//if dest get the UUID for C1ID
			BasicXMLStruct.getIdUuidAtts(c1id,CommonXMLStatics.C1_ID_ATT,localE);
			BasicXMLStruct.getIntAtt(c2id, CommonXMLStatics.C2_ID_ATT, localE);
		}
		else{
			//if src get the UUID for C2ID
			BasicXMLStruct.getIntAtt(c1id, CommonXMLStatics.C1_ID_ATT, localE);
			BasicXMLStruct.getIdUuidAtts(c2id,CommonXMLStatics.C2_ID_ATT,localE);
		}
	}
	
	public void setSrcDest(){
		if (dest) {
			relN = CommonXMLStatics.DEST_REL_ENAME;
		} else {
			relN = CommonXMLStatics.SRC_REL_ENAME;
		}
		
	}

}
