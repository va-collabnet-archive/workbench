package org.ihtsdo.xml.handlers;

import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.xml.cache.ObjectCache;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
	public String conIdi_S = "";

	public XML_I_GetConceptData(I_GetConceptData concept) {
		super();
		this.concept = concept;
		try {
			process();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Err thrown in XML_I_GetConceptData process",
					e);
		}
	}

	public XML_I_GetConceptData(boolean debug) {
		super();
		this.debug = debug;
		System.out
				.println("XML_I_GetConceptData(boolean debug) called debug = "
						+ debug);
		try {
			process();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Err thrown in XML_I_GetConceptData process",
					e);
		}
	}

	public XML_I_GetConceptData(I_GetConceptData concept, int depth) {
		super();
		this.concept = concept;
		this.depth = depth;
		try {
			process();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Err thrown in XML_I_GetConceptData process",
					e);
		}
	}

	public XML_I_GetConceptData(Document conceptDoc) {
		super();
		this.Doc = conceptDoc;
		// this.concept = concept;
		try {
			processXML();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Err thrown in XML_I_GetConceptData process",
					e);
		}
	}

	public static void main(String[] args) {
		System.out.println("Main called");
		XML_I_GetConceptData xCon = new XML_I_GetConceptData(true);
		System.out.println("Main finished");
	}

	public void processXML() throws Exception {
		EConcept eConcept = new EConcept();
		Vector<Element> elemV = new Vector<Element>();
		Element el;
		// Add ID's
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.IDS_ENAME, Doc);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				new XML_I_Identify(eConcept, el);
			}
		}
		// Add attributes
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.CAV_ENAME, Doc);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				new XML_I_ConceptAttributeVersioned(eConcept, el);
			}
		}
		//Add Descriptions DESCRIPTIONS_ENAME XML_I_DescriptionVersioned
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.DESCRIPTIONS_ENAME, Doc);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				new XML_I_DescriptionVersioned(eConcept, el);
			}
		}
		
		//Add srcRels SRCRELS_ENAME
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.SRCRELS_ENAME, Doc);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				new XML_I_RelVersioned(eConcept, el,false);
			}
		}
		
		//Add destRels DESTRELS_ENAME
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.DESTRELS_ENAME, Doc);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				new XML_I_RelVersioned(eConcept, el,true);
			}
		}
		
		//Add Images IMAGES_ENAME
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.IMAGES_ENAME, Doc);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				new XML_I_ImageVersioned(eConcept, el);
			}
		}
		
		//Add Extensions EXTENSIONS_ENAME
		elemV = XMLUtil.getChildElemsByName(CommonXMLStatics.EXTENSIONS_ENAME, Doc);
		if (elemV.size() > 0) {
			for (Iterator<Element> it1 = elemV.iterator(); it1.hasNext();) {
				el = it1.next();
				new XML_I_ExtendByRef(eConcept, el);
			}
		}

		concept = Concept.get(eConcept);

	}

	public void process() throws Exception {
		// System.out.println("process called debug = "+debug);
		conIdi_S = "-1";
		oc_key = CommonXMLStatics.CONCEPT_PRE + conIdi_S;

		Doc = XMLUtil.getEmptyDocument();
		parent = Doc.createElement(CommonXMLStatics.CONCEPT_ENAME);
		Doc.appendChild(parent);

		try {
			// System.out.println("Printing Doc");
			// System.out.println(XMLUtil.convertToStringLeaveCDATA(Doc));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (!debug) {
			conIdi = concept.getConceptId();
			conIdi_S = Integer.toString(conIdi);
			oc_key = CommonXMLStatics.CONCEPT_PRE + conIdi_S;

			if (ObjectCache.get(conIdi_S) == null) {
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

			}
		} else {
			System.out.println("process in debug method ");
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

		}
		// Doc.getDocumentElement().appendChild(parent);
		// XMLUtil.logNode("Debug Concept Doc",Doc);
		// System.out.println("Adding to OC "+oc_key);

		if (debug) {
			System.out.println(XMLUtil.convertToStringLeaveCDATA(Doc));
		}

		ObjectCache.put(oc_key, Doc);
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

	public String getConIdi_S() {
		return conIdi_S;
	}

	public void setConIdi_S(String conIdiS) {
		conIdi_S = conIdiS;
	}

}
