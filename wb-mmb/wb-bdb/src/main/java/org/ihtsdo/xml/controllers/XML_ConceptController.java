package org.ihtsdo.xml.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.ihtsdo.objectCache.ObjectCache;
import org.ihtsdo.xml.common.CommonXMLStatics;
import org.ihtsdo.xml.handlers.XML_I_GetConceptData;
import org.ihtsdo.xml.util.AceXMLUtil;
import org.ihtsdo.xml.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XML_ConceptController {
	
	/** THis is the adapter object for interfacing between XML and Concepts 
	 * 
	 */
	
	/**
	 * Get concept by UUID and id int
	 * Expand given a template
	 * Create a document from a root concept (i.e. fully expressed)
	 * Turn a document into a concept 
	 * Throw informative errors if template is not fulfilled (e.g. RV2 is missing a Rv3 rel etc)
	 * 
	 */

	private static final Logger log = Logger.getLogger(XML_ConceptController.class.getName());
	
	I_TermFactory tf = null;
	public Document retDoc = null;
	Element conceptsE = null;
	public boolean relatedConcepts = false;
	
	public XML_ConceptController(String vodbDirectory) {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public XML_ConceptController(I_TermFactory tf) {
		super();
		this.tf = tf;
	}
	
	
	/*public boolean checkProc(int conID){
		//conIdi =concept.getConceptId();
		String oc_key = CommonXMLStatics.CONCEPT_PRE+Integer.toString(conID);
		if(ObjectCache.get(oc_key) != null){
			return true;
		}
		else{
			return false;
		}
	} */


/**
 * Get a concepts Document using a UUID as a string as Key
 * @param uuid
 * @return
 * @throws Exception
 */

	public Document getXMLConceptUUID_S(String uuid) throws Exception{
		UUID uid = UUID.fromString(uuid);
		return getXMLConceptUUID(uid);
	}

	/**
	 * Get a concepts Document using a UUID as Key
	 * @param uuid
	 * @return
	 * @throws Exception
	 */
	
	public Document getXMLConceptUUID(UUID uuid) throws Exception{
		
		if(!AceXMLUtil.getUuidInt().containsKey(uuid.toString())){
		I_GetConceptData editPathConcept = tf.getConcept(uuid);
		AceXMLUtil.addtoUuidInt(uuid.toString(), editPathConcept.getConceptId());
		
			add_I_GetConceptData(editPathConcept);
		}
		return retDoc;
	}
	
	/**
	 * Get a concepts Document using an String id (a matching textual identifier)  as Key
	 * @param id
	 * @return
	 * @throws Exception
	 */
	
	public Document getXMLConceptID(String id) throws Exception{	
		Collection<I_GetConceptData>  conCol = tf.getConcept(id);
		if(conCol.size() > 0){
			Iterator<I_GetConceptData> conIt = conCol.iterator();
			while (conIt.hasNext()) {
				I_GetConceptData editPathConcept = conIt.next();
				if(!AceXMLUtil.checkProc(editPathConcept.getConceptId())){
					add_I_GetConceptData(editPathConcept);
				}
			}
		}
		else{
			// return an empty doc + concepts element
			getConceptsRoot();
		}
		return retDoc;
	}
	
	/**
	 *  Get a concepts Document using a concept using a textual identifier from a known identifier scheme
	 * @param Con_id
	 * @param sourceID
	 * @return
	 * @throws Exception
	 */
	
	public Document getXMLConceptID(String Con_id, int sourceID) throws Exception{	
		I_GetConceptData editPathConcept = tf.getConcept(Con_id,sourceID);
		if(!AceXMLUtil.checkProc(editPathConcept.getConceptId())){
			add_I_GetConceptData(editPathConcept);
		}
		return retDoc;
	}	

	/**
	 * Get a concepts Document using an int id as string as  Key
	 * @param intS
	 * @return
	 * @throws Exception
	 */
	public Document getXMLConceptInt(String intS) throws Exception{
		int conI = Integer.parseInt(intS);
		return getXMLConceptInt(conI);
	}
	
	/**
	 * Get a concepts Document using an int id as  Key
	 * @param conI
	 * @return
	 * @throws Exception
	 */
	
	public Document getXMLConceptInt(int conI) throws Exception{
		
		if(!AceXMLUtil.checkProc(conI)){
			I_GetConceptData editPathConcept = tf.getConcept(conI);
			add_I_GetConceptData(editPathConcept);
		}
		return retDoc;
	}
	
	/**
	 * Adds an I_GetConceptData to the concepts Document
	 * @param icd
	 */
	
	public void add_I_GetConceptData(I_GetConceptData icd) throws Exception{
		XML_I_GetConceptData  icX = new XML_I_GetConceptData(icd);
		String key = icX.getOc_key();	
		 addConceptsToRoot((Document)ObjectCache.get(key));
		 String uuid = icX.getUuid_key();
		 //String intId = icX.getConIdi_S();
		 AceXMLUtil.addtoUuidInt(uuid,icX.getConIdi());
		 if(relatedConcepts){
			 addRelatedConcepts(); 
		 }
	}
	
	
	public Node getRelXMLConceptInt(int conI,Node parent) throws Exception{
		
		if(!AceXMLUtil.checkProc(conI)){
			I_GetConceptData editPathConcept = tf.getConcept(conI);
			add_Related_I_GetConceptData(editPathConcept,parent);
		}
		return parent;
	}
	
	public void add_Related_I_GetConceptData(I_GetConceptData icd, Node parent) throws Exception{
		XML_I_GetConceptData  icX = new XML_I_GetConceptData(icd);
		String key = icX.getOc_key();
		Document conDoc = (Document)ObjectCache.get(key);
		Element conRoot = conDoc.getDocumentElement();
		Node ImpNewNode = parent.getOwnerDocument().importNode(conRoot, true);
		parent.appendChild(ImpNewNode);
	}
	
	/**
	 * Adds the root Concepts element to the root of the document which is returned
	 * @param conceptDoc
	 */
	
	public void addConceptsToRoot(Document conceptDoc){
		if(conceptsE == null){
			getConceptsRoot();
		}
		Element conRoot = conceptDoc.getDocumentElement();
		Node ImpNewNode = retDoc.importNode(conRoot, true);
		retDoc.getDocumentElement().appendChild(ImpNewNode);
	}

	public I_TermFactory getTf() {
		return tf;
	}

	public void setTf(I_TermFactory tf) {
		this.tf = tf;
	}
	
	/**
	 * returns the concepts element, creating it if it does not exist
	 * @return
	 */
	public Element getConceptsRoot(){
		if(retDoc == null){
			getRetDoc();
		}
		if(conceptsE == null){
			conceptsE = retDoc.createElement(CommonXMLStatics.CONCEPTS_ENAME);
			retDoc.appendChild(conceptsE);	
		}
		return conceptsE;
	}

	/**
	 * returns the return document, creating it as an empty document if it does not exist
	 * @return
	 */
	public Document getRetDoc() {
		
		if(retDoc == null){
			retDoc = XMLUtil.getEmptyDocument();
		}
		
		return retDoc;
	}

	public void setRetDoc(Document retDoc) {
		this.retDoc = retDoc;
	}

	public boolean isRelatedConcepts() {
		return relatedConcepts;
	}

	public void setRelatedConcepts(boolean relatedConcepts) {
		this.relatedConcepts = relatedConcepts;
	}
	
	public Element addRCbyXPathS(String XPath,String elemName)throws Exception{
		Element El = null;
		NodeList nl = null;
		log.severe("addRCbyXPathS XPath = " + XPath);
		nl = XMLUtil.getNodesListXpathNode(XPath, retDoc);
		if(nl.getLength() > 0){
			El = retDoc.createElement(elemName);
			log.severe("Number of Atts found = " + nl.getLength());
			for (int y = 0; y < nl.getLength(); y++) {
				Node conIdN = nl.item(y);
				addRelatedConcept(conIdN, El);
			}
		}
		
		return El;
		
	}
	
	public Element addRCbyXPathAttName(String AttName)throws Exception{
		
		String xPath_path = CommonXMLStatics.XPATH_START_ALL_ATT_BY_NAME+AttName+CommonXMLStatics.XPATH_END_SELECT;
		return addRCbyXPathS(xPath_path,AttName);
	}
	
	public void addRelatedConcepts() throws Exception{
		
		NodeList nl = null;
		getConceptsRoot();
		Element relConceptsE = retDoc.createElement(CommonXMLStatics.REL_CONCEPTS_ENAME);
		Element relConceptE = null;
		conceptsE.appendChild(relConceptsE);	
		
		String xpath = null;
		//Structural
		// destRel c1Id
		xpath = AceXMLUtil.getXpathSElemAtt(CommonXMLStatics.DEST_REL_ENAME,CommonXMLStatics.C1_ID_ATT);
		//log.severe("xpath destRel = " + xpath);
		Element destRelE = addRCbyXPathS(xpath,CommonXMLStatics.DESTRELS_ENAME);
		//srcRec c2Id
		xpath = AceXMLUtil.getXpathSElemAtt(CommonXMLStatics.SRC_REL_ENAME,CommonXMLStatics.C2_ID_ATT);
		//log.severe("xpath srcRel = " + xpath);
		Element srcRelE = addRCbyXPathS(xpath,CommonXMLStatics.SRCRELS_ENAME);
		//id
		xpath = AceXMLUtil.getXpathSElemAtt(CommonXMLStatics.ID_ENAME,CommonXMLStatics.NATIVE_ATT);
		//log.severe("xpath srcRel = " + xpath);
		Element idE = addRCbyXPathS(xpath,CommonXMLStatics.IDS_ENAME);
		//relpart id
		xpath = AceXMLUtil.getXpathSElemAtt(CommonXMLStatics.REL_PART_ENAME,CommonXMLStatics.TYPE_ID_ATT);
		//log.severe("xpath srcRel = " + xpath);
		Element reltypeidE = addRCbyXPathS(xpath,CommonXMLStatics.REL_TYPES_ENAME);
		//descpart id
		xpath = AceXMLUtil.getXpathSElemAtt(CommonXMLStatics.DESCP_ENAME,CommonXMLStatics.TYPE_ID_ATT);
		//log.severe("xpath srcRel = " + xpath);
		Element descPtypeidE = addRCbyXPathS(xpath,CommonXMLStatics.DESCRIPTIONS_ENAME);
		
		//RefSets
		Element extensionsE = retDoc.createElement(CommonXMLStatics.EXTENSIONS_ENAME);

		xpath = AceXMLUtil.getXpathSElemAtt(CommonXMLStatics.EXT_ENAME,CommonXMLStatics.REFSET_ID_ATT);
		Element refSetidE = addRCbyXPathS(xpath,CommonXMLStatics.REFSET_ID_ATT);
		
		
		if(refSetidE != null){
			extensionsE.appendChild(refSetidE);
		}
		
		relConceptsE.appendChild(extensionsE);
		
		
		if(destRelE != null){
			relConceptsE.appendChild(destRelE);
		}
		if(srcRelE != null){
			relConceptsE.appendChild(srcRelE);
		}
		if(idE != null){
			relConceptsE.appendChild(idE);
		}
		if(reltypeidE != null){
			relConceptsE.appendChild(reltypeidE);
		}
		if(descPtypeidE != null){
			relConceptsE.appendChild(descPtypeidE);
		}
		
		
		//Basic values
		//path
		Element pathRelE = addRCbyXPathAttName(CommonXMLStatics.PATH_ATT);
		//status
		Element statRelE =addRCbyXPathAttName(CommonXMLStatics.STATUS_ATT);
		//characteristicId
		Element charRelE =addRCbyXPathAttName(CommonXMLStatics.CHAR_ID_ATT);
		//refinabilityId
		Element refRelE =addRCbyXPathAttName(CommonXMLStatics.REFIN_ID_ATT);
		 
		
		
		
		//Add at the end to avoid additonal relationships
		
		

		if(pathRelE != null){
			relConceptsE.appendChild(pathRelE);
		}
		if(statRelE != null){
			relConceptsE.appendChild(statRelE);
		}
		if(charRelE != null){
			relConceptsE.appendChild(charRelE);
		}
		if(refRelE != null){
			relConceptsE.appendChild(refRelE);
		}
		
		
		
		

		
		retDoc.getDocumentElement().appendChild(relConceptsE);
	}
	
/*	public String getXpathSElemAtt(String ename,String attname){

		StringBuilder sb=new StringBuilder();
		sb.append(CommonXMLStatics.XPATH_START_E_BY_NAME);
		sb.append(ename);
		sb.append(CommonXMLStatics.XPATH_END_SELECT);
		sb.append(CommonXMLStatics.XPATH_START_ATT_BY_NAME);
		sb.append(attname);
		sb.append(CommonXMLStatics.XPATH_END_SELECT);
		return sb.toString();
	}
	 */
	
	public void addRelatedConcept(Node idN, Node parent)throws Exception{
		
		int idI = 0;
		String id_S = null;
		if (idN.getNodeType() == Node.ELEMENT_NODE) {
			log.severe("Should be an attribute not an element!!!");
		}
		if (idN.getNodeType() == Node.ATTRIBUTE_NODE) {
			id_S = idN.getNodeValue();
			//log.severe("Related ID = " + id_S);
			idI = Integer.parseInt(id_S);
			getRelXMLConceptInt(idI,parent);	
		}
	}
	


}
