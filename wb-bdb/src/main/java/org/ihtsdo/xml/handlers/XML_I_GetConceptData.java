package org.ihtsdo.xml.handlers;

import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.ihtsdo.objectCache.ObjectCache;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.util.AceXMLUtil;
import org.ihtsdo.xml.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XML_I_GetConceptData extends XML_basic implements I_Handle_XML {

	private static final Logger log = Logger.getLogger(I_Handle_XML.class
			.getName());

	/**
	 * A drill down depth for concepts If Set to -1 then infinite depth
	 **/
	public int depth = 0;

	public Document Doc = null;

	public I_GetConceptData concept;

	public int conIdi = -1;
	public String oc_key = "";
	public String uuid_key = "";
	//public String conIdi_S = "";

	public XML_I_GetConceptData(I_GetConceptData concept) {
			
		super();
		this.concept = concept;
		getXML();
	}

	public XML_I_GetConceptData(boolean debug) {
		super();
		System.out.println("XML_I_GetConceptData(boolean debug) called debug = "+ debug);
		setDebug(debug);
	}

	public XML_I_GetConceptData(I_GetConceptData concept, int depth) {
		super();
		this.concept = concept;
		this.depth = depth;
		getXML();
	}

	public XML_I_GetConceptData(Document conceptDoc) {
		super();
		
		
		this.Doc = conceptDoc;
		this.parent = Doc.getDocumentElement();
		// this.concept = concept;
		setXML();
	}

	public static void main(String[] args) {
		System.out.println("Main called");
		XML_I_GetConceptData xCon = new XML_I_GetConceptData(true);
		System.out.println("Main finished");
	}

	public void setXML() {
		
		
		createNewConcept();
		
		//if(parent.getAttribute(name))
		
		
		//EConcept eConcept = new EConcept();
		Vector<Element> elemV = new Vector<Element>();
		Element el;
		// Add ID's
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.IDS_ENAME, parent);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				XML_I_Identify xmlI = new XML_I_Identify();
				xmlI.setIconXML(concept, el);
			}
		}
		// Add attributes
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.CAV_ENAME, parent);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				XML_I_ConceptAttributeVersioned xmlIcav = new XML_I_ConceptAttributeVersioned();
				xmlIcav.setIconXML(concept, el);
			}
		}
		//Add Descriptions DESCRIPTIONS_ENAME XML_I_DescriptionVersioned
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.DESCRIPTIONS_ENAME, parent);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				XML_I_DescriptionVersioned xmlDesc = new XML_I_DescriptionVersioned();
				xmlDesc.setIconXML(concept, el);
			}
		}
		
		//Add srcRels SRCRELS_ENAME
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.SRCRELS_ENAME, parent);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				XML_I_RelVersioned xmlIrelSrc = new XML_I_RelVersioned(false);
				xmlIrelSrc.setIconXML(concept, el);
			}
		}
		
		//Add destRels DESTRELS_ENAME
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.DESTRELS_ENAME, parent);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				XML_I_RelVersioned xmlIrelDest = new XML_I_RelVersioned(true);
				xmlIrelDest.setIconXML(concept, el);
			}
		}
		
		//Add Images IMAGES_ENAME
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.IMAGES_ENAME, parent);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				XML_I_ImageVersioned xmlIm = new XML_I_ImageVersioned();
				xmlIm.setIconXML(concept, el);
			}
		}
		
		//Add Extensions EXTENSIONS_ENAME
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.EXTENSIONS_ENAME, parent);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				XML_I_ExtendByRef xmlIref = new XML_I_ExtendByRef();
				xmlIref.setIconXML(concept, el);
			}
		}



	}

	public void getXML() {		
		
		

		if (!debug) {
			getConXML();	
		} else {
			getDebugConXML();
		}
		if (debug) {
			try {
				System.out.println(XMLUtil.convertToStringLeaveCDATA(Doc));
			} catch (TransformerException e) {
				log.log(Level.SEVERE, "Err thrown in XML_I_GetConceptData setXML \n" +
						"printing out debug XML",
						e);
			}
		}

		
	}
	
	public void getConXML(){
		
		conIdi = concept.getConceptNid();
		//conIdi_S = Integer.toString(conIdi);
		oc_key = AceXMLUtil.getOc_key(conIdi);
		//oc_key = CommonXMLStatics.CONCEPT_PRE + conIdi_S;
		
		if (ObjectCache.get(oc_key) == null) {
			// create the new doc 
			createNewConDoc();
			// Add the ID
			uuid_key = BasicXMLStruct.getNativeIdAtts(conIdi, parent);
			// Add the identifier
			try {
				new XML_I_Identify(concept.getIdentifier(), parent);
				// ConceptAttributeVersioned
				if (concept.getConceptAttributes() != null) {
					XML_I_ConceptAttributeVersioned icav_h = new XML_I_ConceptAttributeVersioned(
							concept.getConceptAttributes(), parent);
				}
				// Descriptions
				Element descsE = Doc
						.createElement(CommonXMLStatics.DESCRIPTIONS_ENAME);

				for (I_DescriptionVersioned desc : concept
						.getDescriptions()) {
					new XML_I_DescriptionVersioned(desc, descsE);
				}
				parent.appendChild(descsE);

				// Src Rels
				Element srcRelsE = Doc
						.createElement(CommonXMLStatics.SRCRELS_ENAME);
				for (I_RelVersioned srcRel : concept.getSourceRels()) {
					new XML_I_RelVersioned(srcRel, srcRelsE, false);
				}
				parent.appendChild(srcRelsE);

				// destRels
				Element destRelsE = Doc
						.createElement(CommonXMLStatics.DESTRELS_ENAME);
				for (I_RelVersioned destRel : concept.getDestRels()) {
					new XML_I_RelVersioned(destRel, destRelsE, true);
				}
				parent.appendChild(destRelsE);
				// Images
				Element ImagesE = Doc
						.createElement(CommonXMLStatics.IMAGES_ENAME);
				for (I_ImageVersioned image : concept.getImages()) {
					new XML_I_ImageVersioned(image, ImagesE);
				}
				parent.appendChild(ImagesE);
				// Extensions
				Element ExtE = Doc
						.createElement(CommonXMLStatics.EXTENSIONS_ENAME);
				for (I_ExtendByRef ext : concept.getExtensions()) {
					new XML_I_ExtendByRef(ext, ExtE);
				}
				parent.appendChild(ExtE);
			} catch (Exception e) {
				System.out
						.println("Err thrown in XML_I_GetConceptData process");
				log.log(Level.SEVERE,
						"Err thrown in XML_I_GetConceptData process", e);
				e.printStackTrace();
			}
			ObjectCache.put(oc_key, Doc);
	}
		else {
			Doc = (Document)ObjectCache.get(oc_key); 
		}
	}
	
	
	public void createNewConDoc() {
		Doc = XMLUtil.getEmptyDocument();
		parent = Doc.createElement(CommonXMLStatics.CONCEPT_ENAME);
		Doc.appendChild(parent);
	}
	
	public void getDebugConXML() {
		System.out.println("process in debug method ");
		conIdi = -1;
		oc_key = AceXMLUtil.getOc_key(conIdi);
		if (ObjectCache.get(oc_key) == null) {
		// create the new doc 
		createNewConDoc();	
		
		new XML_I_Identify(true, parent);
		new XML_I_ConceptAttributeVersioned(debug, parent);
		Element descs = Doc
				.createElement(CommonXMLStatics.DESCRIPTIONS_ENAME);
		new XML_I_DescriptionVersioned(debug, descs);
		parent.appendChild(descs);
		Element srcRelsE = Doc
				.createElement(CommonXMLStatics.SRCRELS_ENAME);
		new XML_I_RelVersioned(debug, srcRelsE, false);
		parent.appendChild(srcRelsE);
		Element destRelsE = Doc
				.createElement(CommonXMLStatics.DESTRELS_ENAME);
		new XML_I_RelVersioned(debug, destRelsE, true);
		parent.appendChild(destRelsE);
		Element ImagesE = Doc.createElement(CommonXMLStatics.IMAGES_ENAME);
		new XML_I_ImageVersioned(debug, ImagesE);
		parent.appendChild(ImagesE);
		Element ExtE = Doc.createElement(CommonXMLStatics.EXTENSIONS_ENAME);
		new XML_I_ExtendByRef(debug, ExtE);
		parent.appendChild(ExtE);
		
		ObjectCache.put(oc_key, Doc);
		}
		else {
			Doc = (Document)ObjectCache.get(oc_key); 
		}	
	}
	
	public String getConceptXMLAsString() {
		String XML_S = "Doc is null";
		
		if(Doc != null) {
			try {
				XML_S = XMLUtil.convertToStringLeaveCDATA(Doc);
			} catch (TransformerException e) {
				log.log(Level.SEVERE, "Err thrown in XML_I_GetConceptData getConceptXMLAsString \n" +
						"printing out XML",
						e);
			}
		}
		
		return XML_S;
		
		
	}
	

	public int getConIdi() {
		return conIdi;
	}

	public void setConIdi(int conIdi) {
		this.conIdi = conIdi;
	}

	public String getOc_key() {
		return oc_key;
	}

	public void setOc_key(String ocKey) {
		oc_key = ocKey;
	}

	public String getUuid_key() {
		return uuid_key;
	}

	public void setUuid_key(String uuidKey) {
		uuid_key = uuidKey;
	}

	/*public String getConIdi_S() {
		return conIdi_S;
	}

	public void setConIdi_S(String conIdiS) {
		conIdi_S = conIdiS;
	} */
	
	public void createNewConcept() {
		UUID uuid_id = UUID.randomUUID();
		createNewConcept(uuid_id);
	}
	
	public void createNewConcept(UUID uuid_id) {
		try {
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			this.concept = Terms.get().newConcept(uuid_id, false, config);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Err thrown in XML_I_GetConceptData createNewConcept UUID = "+uuid_id,e);
		}
	}

	public Document getDoc() {
		return Doc;
	}

	public void setDoc(Document doc) {
		Doc = doc;
	}
}
