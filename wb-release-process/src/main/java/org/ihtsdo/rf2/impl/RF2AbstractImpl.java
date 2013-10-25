package org.ihtsdo.rf2.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.PositionSet;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

public abstract class RF2AbstractImpl {

	private static Config config;

	// String timeFormat = I_Constants.TimeFormat;

	private final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

	private static Logger logger = Logger.getLogger(RF2AbstractImpl.class);

	private int dupRecord;
	private int metadataCount=0;
	// CORE CONSTANTS
	private int rootNid;
	private int isaNid;

	private static int isCh_STATED_RELATIONSHIP;
	private static int isCh_DEFINING_CHARACTERISTIC;
	private static int isCh_STATED_AND_INFERRED_RELATIONSHIP;
	private static int isCh_STATED_AND_SUBSUMED_RELATIONSHIP;
	private static int isCh_INFERRED_RELATIONSHIP;

	private I_IntSet roleTypeSet;
	private I_IntSet statusSet;
	private PositionSetReadOnly fromPathPos;

	// GUI
	I_ShowActivity gui;
	private Precedence precedence;
	private ContradictionManagerBI contradictionMgr;

	// new fields
	protected I_TermFactory tf;
	protected I_ConfigAceFrame currenAceConfig;
	protected int snomedIntId;
	protected NidSetBI allStatuses;
	protected NidSetBI allDescTypes;
	protected NidSetBI descTypes;
	protected NidSetBI textDefinTypes;

	private I_GetConceptData snomedRoot;

	private I_GetConceptData snomedCTModelComponent;	
	private I_GetConceptData coreMetaConceptRoot;
	private I_GetConceptData foundationMetaDataConceptRoot;

	protected I_IntSet allStatusSet;

	protected int preferredNid;

	protected int acceptableNid;

	protected int currentNid;

	protected int retiredNid;	

	protected int activeNid ; //Active value	900000000000545005	

	protected int inactiveNid; //Inactive value	900000000000546006	

	protected String nullUuid; // null string to match with UUID.fromString("00000000-0000-0000-C000-000000000046")

	private int activeStatus;

	private I_IntSet activeStatusSet;

	private HashMap<String, String> modMap;

	private HashMap<String, String> map;

	private HashMap<String, String> nspMap;

	private HashMap<String, String> modmodMap;

	private BufferedWriter br;

	public static Config getConfig() {
		return config;
	}

	public static void setConfig(Config config) {
		RF2AbstractImpl.config = config;
	}

	public String getTimeFormat() {
		return ExportUtil.TIMEFORMAT;
	}

	public void incrementDupRecord() {
		dupRecord++;
	}

	public void incrementMetaDataCount() {
		metadataCount++;
	}

	public int getDupRecord() {
		return dupRecord;
	}

	public int getMetaDataCount() {
		return metadataCount;
	}

	public KnowledgeBase getKbase() {
		return kbase;
	}


	public RF2AbstractImpl(Config config) {

		RF2AbstractImpl.config = config;

		dupRecord = 0;



		try {

			this.tf = Terms.get();
			this.currenAceConfig = tf.getActiveAceFrameConfig();
			snomedIntId = tf.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());
			//			snomedRoot = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			snomedRoot = tf.getConcept(UUID.fromString("a60bd881-9010-3260-9653-0c85716b4391"));
			snomedCTModelComponent = tf.getConcept(UUID.fromString("a60bd881-9010-3260-9653-0c85716b4391"));
			coreMetaConceptRoot = tf.getConcept(UUID.fromString("4c6d8b0b-774a-341e-b0e5-1fc2deedb5a5"));
			foundationMetaDataConceptRoot = tf.getConcept(UUID.fromString("f328cdec-6198-36c4-9c55-d7f4f5b30922"));
			File mappingFile = new File("target/SubsetMapping.txt");

			FileInputStream fis = new FileInputStream(mappingFile);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);

			map = new HashMap<String, String>();

			while (br.ready()) {
				String line = br.readLine();
				line = line.replaceAll("\"", "");
				String[] splited = line.split(",");
				if (splited[1].equals("SNOMED CT UK Clinical extension Refset module (core metadata concept)")) {
					map.put(splited[0], "999000011000000103");
				} else if (splited[1].equals("SNOMED CT UK Edition Refset module (core metadata concept)")) {
					map.put(splited[0], "999000011000000103");
				} else if (splited[1].equals("SNOMED CT UK Drug extension Refset module (core metadata concept)")) {
					map.put(splited[0], "999000011000001104");
				}
			}
			modMap=new HashMap<String,String>();
			modMap.put("f9d7e720-cf9c-43d0-9b6b-17af1ff905c5","999000011000000103");
			modMap.put("6a78f662-d4c8-43a5-9412-9f68e4641f5e","999000011000001104");
			modMap.put("e9195ef9-0e1d-46a2-8268-2281b1b74251","999000021000001108");
			modMap.put("13b9d324-0913-4c7e-a4cd-c92f8d28e8bd","999000021000000109");
			modMap.put("7049fbe6-220d-4580-a369-d1981d0753f8","999000031000000106");
			modMap.put("ab30cbe7-92e5-4445-b489-9226a91d6506","999000041000000102");


			modmodMap=new HashMap<String,String>();
			modmodMap.put("f9d7e720-cf9c-43d0-9b6b-17af1ff905c5","999000011000000103");
			modmodMap.put("6a78f662-d4c8-43a5-9412-9f68e4641f5e","999000011000001104");
			modmodMap.put("e9195ef9-0e1d-46a2-8268-2281b1b74251","999000011000001104");
			modmodMap.put("13b9d324-0913-4c7e-a4cd-c92f8d28e8bd","999000011000000103");
			modmodMap.put("7049fbe6-220d-4580-a369-d1981d0753f8","999000011000000103");
			modmodMap.put("ab30cbe7-92e5-4445-b489-9226a91d6506","999000011000000103");
			modmodMap.put("f81df085-c0a7-4a05-8f55-01bbdd4371d5","999000011000001104");
			modmodMap.put("c4a2e263-5203-4078-8b89-7c12d196fc9e","999000011000001104");
			modmodMap.put("70473ab9-80dc-40a3-8943-0ce703486739","999000011000001104");

			nspMap=new HashMap<String,String>();
			nspMap.put("999000011000000103","1000000");
			nspMap.put("999000011000001104","1000001");
			nspMap.put("999000021000001108","1000001");
			nspMap.put("999000021000000109","1000000");
			nspMap.put("999000031000000106","1000000");
			nspMap.put("999000041000000102","1000000");

			this.preferredNid=tf.uuidToNative(UUID.fromString("266f1bc3-3361-39f3-bffe-69db9daea56e"));
			this.acceptableNid=tf.uuidToNative(UUID.fromString("12b9e103-060e-3256-9982-18c1191af60e"));
			this.currentNid=tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			this.retiredNid=tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
			this.activeNid = getNid("d12702ee-c37f-385f-a070-61d56d4d0f1f"); //Active value	900000000000545005	
			this.inactiveNid = getNid("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"); //Inactive value	900000000000546006	
			this.nullUuid="00000000-0000-0000-c000-000000000046";
			allStatuses = getAllStatuses();
			this.allStatusSet=tf.newIntSet();
			this.allStatusSet.addAll(allStatuses.getSetValues());

			this.activeStatus=tf.uuidToNative(UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));
			this.activeStatusSet=tf.newIntSet();
			this.activeStatusSet.add(activeStatus);

			allDescTypes = getAllDescTypes();
			descTypes=tf.newIntSet();
			descTypes=getDescTypes();
			textDefinTypes=tf.newIntSet();
			textDefinTypes.add(tf.uuidToNative(UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09")));
			setupCoreNids();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//		if (config.getInvokeDroolRules().equals("true")) {
		//			// drools knowlegebase
		//			final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		//
		//			// this will parse and compile in one step
		//			kbuilder.add(ResourceFactory.newClassPathResource(getConfig().getDroolsDrlFile(), RF2ConceptFactory.class), ResourceType.DRL);
		//
		//			// Check the builder for errors
		//			if (kbuilder.hasErrors()) {
		//				logger.error(kbuilder.getErrors().toString());
		//				throw new RuntimeException("Unable to compile \"" + getConfig().getDroolsDrlFile() + " \".");
		//			}
		//
		//			// get the compiled packages (which are serializable)
		//			final Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();
		//
		//			// add the packages to a knowledgebase (deploy the knowledge // packages).
		//			kbase.addKnowledgePackages(pkgs);
		//		}
	}

	private NidSetBI getDescTypes() throws TerminologyException, IOException {
		NidSetBI descTypes = new NidSet();
		descTypes.add(tf.uuidToNative(UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf")));
		descTypes.add(tf.uuidToNative(UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2")));
		descTypes.add(tf.uuidToNative(UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09")));
		return descTypes;
	}

	public BufferedWriter getModSubsMapFile(){
		return br;
	}
	public void openModSubsMapFile(){

		FileOutputStream fis;
		OutputStreamWriter isr;
		try {
			fis = new FileOutputStream("subsorigId-refsetId.txt");
			isr = new OutputStreamWriter(fis, "UTF-8");
			br = new BufferedWriter(isr);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void closeModSubsFileMap(){
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void setupCoreNids() throws TerminologyException, IOException {

		// SETUP CORE NATIVES IDs
		setIsaNid(tf.uuidToNative(SNOMED.Concept.IS_A.getUids()));
		setRootNid(tf.uuidToNative(SNOMED.Concept.ROOT.getUids()));

		// Characteristic
		setIsCh_STATED_RELATIONSHIP(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid());
		setIsCh_DEFINING_CHARACTERISTIC(SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getLenient().getNid());
		setIsCh_STATED_AND_INFERRED_RELATIONSHIP(tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP.getUids()));
		setIsCh_STATED_AND_SUBSUMED_RELATIONSHIP(tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_SUBSUMED_RELATIONSHIP.getUids()));
		setIsCh_INFERRED_RELATIONSHIP(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid());
	}



	public String[] getModuleForSubsOrigId(String subsOrigId) {
		String mod=map.get(subsOrigId);
		String nsp;
		if (mod!=null){
			nsp=nspMap.get(mod);
			String[] ret={mod,nsp};
			return ret;
		}
		return null;
	}

	public String[] getModule(I_GetConceptData concept) {
		String uuid=null;
		try {
			uuid = concept.getUids().iterator().next().toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String mod=modmodMap.get(uuid);
		String nsp;
		if (mod!=null){
			nsp=nspMap.get(mod);
			String[] ret={mod,nsp};
			return ret;
		}
		return null;
	}

	public String getSubsetOrigId(I_GetConceptData concept) {

		try {
			List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allStatuses, 
					allDescTypes, currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());


			for (I_DescriptionTuple description: descriptions) {

				String sDescType = getSnomedDescriptionType(description.getTypeNid());
				String typeId = getTypeId(sDescType);


				if (!sDescType.equals("4") ) { 
					//&& !effectiveTime.contains("1031") && !effectiveTime.contains("0430")) {

					String term = description.getText();
					if (term!=null ){
						if (term.indexOf("\t")>-1){
							term=term.replaceAll("\t", "");
						}
						if (term.indexOf("\r")>-1){
							term=term.replaceAll("\r", "");
						}
						if (term.indexOf("\n")>-1){
							term=term.replaceAll("\n", "");
						}
						term=StringEscapeUtils.unescapeHtml(term);

					}
					boolean nan=false;
					try {
						long subsOrigId=Long.parseLong(term);
					}catch(Exception exc){
						nan=true;
					}
					if (!nan){
						return term;
					}

				}
			}
		}catch (NullPointerException ne) {
			logger.error("NullPointerException: " + ne.getMessage());
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Exceptions in exportDescription: " + e.getMessage());
		}
		return null;
	}
	public I_ConfigAceFrame getAceConfig() {
		return ExportUtil.getAceConfig();
	}

	public int getIsaNid() {
		return isaNid;
	}

	public I_TermFactory getTermFactory() {
		return tf;
	}

	/*
	 * public static String getModuleID(String snomedIntegerId) { return ExportUtil.getModuleID(snomedIntegerId); }
	 */
	public void closeFileWriter(BufferedWriter bw) throws IOException {
		if (bw != null)
			bw.close();
	}

	public String getMetaModuleID(I_GetConceptData concept) throws TerminologyException, IOException {
		return ExportUtil.getMetaModuleID(concept);
	}

	public Long getLatestActivePart(List<I_RelPart> parts) throws Exception {
		return ExportUtil.getLatestActivePart(parts);
	}


	public String getRefinabilityStatusType(int status) throws TerminologyException, IOException {
		return ExportUtil.getRefinabilityStatusType(status);
	}

	public boolean insertSctId(int componentNid  ,Config config, String wbSctId, int pathNid, int statusNid, long effectiveDate) throws Exception {
		return ExportUtil.insertSctId(componentNid , getConfig(), wbSctId , pathNid , statusNid , effectiveDate);
	}


	public boolean insertSnomedId(int componentNid  ,Config config, String wbSctId, int pathNid, int statusNid, long effectiveDate) throws Exception {
		return ExportUtil.insertSnomedId(componentNid , getConfig(), wbSctId , pathNid , statusNid , effectiveDate);
	}	

	public boolean insertCtv3Id(int componentNid  ,Config config, String wbSctId, int pathNid, int statusNid, long effectiveDate) throws Exception {
		return ExportUtil.insertCtv3Id(componentNid , getConfig(), wbSctId , pathNid , statusNid , effectiveDate);
	}

	public boolean insertSctId(int componentNid  ,Config config, String wbSctId, int pathNid, int statusNid) throws Exception {
		return ExportUtil.insertSctId(componentNid , getConfig(), wbSctId , pathNid , statusNid);
	}	

	public boolean insertSnomedId(int componentNid  ,Config config, String wbSctId, int pathNid, int statusNid) throws Exception {
		return ExportUtil.insertSnomedId(componentNid , getConfig(), wbSctId , pathNid , statusNid );
	}	

	public boolean insertCtv3Id(int componentNid  ,Config config, String wbSctId, int pathNid, int statusNid) throws Exception {
		return ExportUtil.insertCtv3Id(componentNid , getConfig(), wbSctId , pathNid , statusNid );
	}

	public String getConceptInactivationStatusType(int status) throws TerminologyException, IOException {
		return ExportUtil.getConceptInactivationStatusType(status);
	}

	public String getConceptInactivationValueId(int status) throws TerminologyException, IOException {
		return ExportUtil.getConceptInactivationValueId(status);
	}

	public String getDescInactivationValueId(int status) throws TerminologyException, IOException {
		return ExportUtil.getDescInactivationValueId(status);
	}

	public String getDescInactivationStatusType(int status) throws TerminologyException, IOException {
		return ExportUtil.getDescInactivationStatusType(status);
	}

	public String getRefinabilityValueId(int charType) throws IOException, Exception {
		return ExportUtil.getRefinabilityValueId(charType);
	}

	public String getRefinabilityType(int type) throws IOException, Exception {
		return ExportUtil.getRefinabilityType(type);
	}

	public String getStatusType(int status) throws TerminologyException, IOException {
		return ExportUtil.getStatusType(status);
	}

	public String getSnomedId(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		return ExportUtil.getSnomedId(concept, snomedCorePathNid);
	}

	public String getCtv3Id(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		return ExportUtil.getCtv3Id(concept, snomedCorePathNid);
	}

	public String getRelationshipIdVersion(Object denotion, int snomedAuthorityNid) throws IOException, TerminologyException {
		return ExportUtil.getRelationshipIdVersion(denotion, snomedAuthorityNid);
	}

	public boolean IsConceptInActive(I_GetConceptData concept, String effectiveTimeRelStr) throws ParseException, TerminologyException, IOException {
		return ExportUtil.IsConceptInActive(concept, effectiveTimeRelStr);
	}

	public String getRefsetId(String typeId) throws IOException, Exception {
		return ExportUtil.getRefsetId(typeId);
	}

	public String getSNOMEDID(Config config, UUID componentUuid, String parentSnomedId) throws Exception {
		return ExportUtil.getSNOMEDID(config ,componentUuid, parentSnomedId);
	}

	public String getCTV3ID(Config config, UUID componentUuid) throws Exception {
		return ExportUtil.getCTV3ID(config ,componentUuid);
	}

	public  Set<I_GetConceptData>  getParentLocal(Set<I_GetConceptData> parents, I_GetConceptData concept) throws Exception {
		return ExportUtil.getParentLocal(parents ,concept);
	}



	public int getNid(String struuid) throws TerminologyException, IOException {
		return ExportUtil.getNid(struuid);
	}

	public int getSnomedCorePathNid() {
		return ExportUtil.getSnomedCorePathNid();
	}

	public int getSnomedMetaPathNid() {
		return ExportUtil.getSnomedMetaPathNid();
	}

	public int getSnorocketAuthorNid() {
		return ExportUtil.getSnorocketAuthorNid();
	}

	public int getUserAuthorNid() {
		return ExportUtil.getUserAuthorNid();
	}

	public int getSnomedInferredPathNid() {
		return ExportUtil.getSnomedInferredPathNid();
	}

	public int getSnomedStatedPathNid() {
		return ExportUtil.getSnomedStatedPathNid();
	}

	public boolean isOnPath(int onPath, int nid) throws IOException, TerminologyException {
		return ExportUtil.isOnPath(onPath, nid);
	}


	public String getParentSnomedId(I_GetConceptData concept) throws Exception {
		return ExportUtil.getParentSnomedId(concept);
	}

	//Get the sctid for the given UUID
	public String getSCTId(Config config, UUID componentUuid, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId) throws IOException, TerminologyException {
		return ExportUtil.getSCTId( config,  componentUuid,  namespaceId,  partitionId,  releaseId,  executionId,  moduleId);
	}


	public String getSctId(int nid, int pathNid) throws IOException, TerminologyException {
		return ExportUtil.getSctId(nid, pathNid);
	}

	// get the id for the given UUID
	public String getSCTId(Config config, UUID uuid, String part) throws Exception {
		return ExportUtil.getSCTId(config, uuid, part);
	}

	// get the conceptid for the given UUID
	public String getConceptId(Config config, UUID uuid) throws Exception {
		return ExportUtil.getConceptId(config, uuid);
	}

	public String getConceptId(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		return ExportUtil.getConceptId(concept, snomedCorePathNid);
	}

	public String getPartitionId(String sctId) {
		return ExportUtil.getPartitionId(sctId);
	}

	public String getSnomedDescriptionType(int wbDescriptionType) {
		return ExportUtil.getSnomedDescriptionType(wbDescriptionType);
	}

	public String getDescriptionId(int descriptionNid, int snomedCorePathNid) throws IOException, TerminologyException {
		return ExportUtil.getDescriptionId(descriptionNid, snomedCorePathNid);
	}


	// get the descriptionid for the given UUID
	public String getDescriptionId(Config config, UUID uuid) throws Exception {
		return ExportUtil.getDescriptionId(config, uuid);
	}

	// get the relationshipid for the given UUID
	public String getRelationshipId(Config config, UUID uuid) throws Exception {
		return ExportUtil.getRelationshipId(config, uuid);
	}

	public static void setupProfile(Config config) throws TerminologyException, IOException {
		ExportUtil.setupProfile(config);
	}

	public Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		return ExportUtil.getDescendants(descendants, concept);
	}

	public String getCharacteristicType(int type) throws IOException, TerminologyException {
		return ExportUtil.getCharacteristicType(type);
	}


	public String getSNOMEDrelationshipType(int type) throws TerminologyException, IOException {
		return ExportUtil.getSNOMEDrelationshipType(type);
	}

	public SimpleDateFormat getDateFormat() {
		return ExportUtil.DATEFORMAT;
	}

	public String getTypeId(String descType) {
		return ExportUtil.getTypeId(descType);
	}

	public String getCharacteristicTypeId(String characteristicType) {
		return ExportUtil.getCharacteristicTypeId(characteristicType);
	}

	public boolean isIsaFound(I_GetConceptData concept) throws IOException, TerminologyException {
		boolean isaFound = false;

		List<? extends I_ConceptAttributeTuple> attribs = concept.getConceptAttributeTuples(getAceConfig().getAllowedStatus(), getAceConfig().getViewPositionSetReadOnly(), getAceConfig()
				.getPrecedence(), getAceConfig().getConflictResolutionStrategy());

		if (attribs.size() == 1) {
			List<? extends I_RelTuple> relTupList = concept.getSourceRelTuples(getAceConfig().getAllowedStatus(), getAceConfig().getDestRelTypes(), getAceConfig().getViewPositionSetReadOnly(),
					getAceConfig().getPrecedence(), getAceConfig().getConflictResolutionStrategy());
			for (I_RelTuple rt : relTupList) {
				if (rt.getTypeNid() == getIsaNid() && rt.getAuthorNid() != getSnorocketAuthorNid())
					isaFound = true;
			}
		}

		return isaFound;
	}

	public int getRootNid() {
		return rootNid;
	}

	public void setRootNid(int rootNid) {
		this.rootNid = rootNid;
	}

	public void setIsaNid(int isaNid) {
		this.isaNid = isaNid;
	}

	public static int getIsCh_STATED_RELATIONSHIP() {
		return isCh_STATED_RELATIONSHIP;
	}

	public static void setIsCh_STATED_RELATIONSHIP(int isChSTATEDRELATIONSHIP) {
		isCh_STATED_RELATIONSHIP = isChSTATEDRELATIONSHIP;
	}

	public static int getIsCh_DEFINING_CHARACTERISTIC() {
		return isCh_DEFINING_CHARACTERISTIC;
	}

	public static void setIsCh_DEFINING_CHARACTERISTIC(int isChDEFININGCHARACTERISTIC) {
		isCh_DEFINING_CHARACTERISTIC = isChDEFININGCHARACTERISTIC;
	}

	public static int getIsCh_STATED_AND_INFERRED_RELATIONSHIP() {
		return isCh_STATED_AND_INFERRED_RELATIONSHIP;
	}

	public static void setIsCh_STATED_AND_INFERRED_RELATIONSHIP(int isChSTATEDANDINFERREDRELATIONSHIP) {
		isCh_STATED_AND_INFERRED_RELATIONSHIP = isChSTATEDANDINFERREDRELATIONSHIP;
	}

	public static int getIsCh_STATED_AND_SUBSUMED_RELATIONSHIP() {
		return isCh_STATED_AND_SUBSUMED_RELATIONSHIP;
	}

	public static void setIsCh_STATED_AND_SUBSUMED_RELATIONSHIP(int isChSTATEDANDSUBSUMEDRELATIONSHIP) {
		isCh_STATED_AND_SUBSUMED_RELATIONSHIP = isChSTATEDANDSUBSUMEDRELATIONSHIP;
	}

	public static int getIsCh_INFERRED_RELATIONSHIP() {
		return isCh_INFERRED_RELATIONSHIP;
	}

	public static void setIsCh_INFERRED_RELATIONSHIP(int isChINFERREDRELATIONSHIP) {
		isCh_INFERRED_RELATIONSHIP = isChINFERREDRELATIONSHIP;
	}

	public I_IntSet getRoleTypeSet() {
		return roleTypeSet;
	}

	public void setRoleTypeSet(I_IntSet roleTypeSet) {
		this.roleTypeSet = roleTypeSet;
	}

	public I_IntSet getStatusSet() {
		return statusSet;
	}

	public void setStatusSet(I_IntSet statusSet) {
		this.statusSet = statusSet;
	}

	public PositionSetReadOnly getFromPathPos() {
		return fromPathPos;
	}

	public void setFromPathPos(PositionSetReadOnly fromPathPos) {
		this.fromPathPos = fromPathPos;
	}

	public I_ShowActivity getGui() {
		return gui;
	}

	public void setGui(I_ShowActivity gui) {
		this.gui = gui;
	}

	public Precedence getPrecedence() {
		return precedence;
	}

	public void setPrecedence(Precedence precedence) {
		this.precedence = precedence;
	}

	public ContradictionManagerBI getContradictionMgr() {
		return contradictionMgr;
	}

	public void setContradictionMgr(ContradictionManagerBI contradictionMgr) {
		this.contradictionMgr = contradictionMgr;
	}

	public void process(I_GetConceptData concept) throws IOException, TerminologyException, ContradictionException {

		if (snomedRoot.isParentOf(concept, 
				currenAceConfig.getAllowedStatus(),
				currenAceConfig.getDestRelTypes(), 
				currenAceConfig.getViewPositionSetReadOnly(), 
				currenAceConfig.getPrecedence(), 
				currenAceConfig.getConflictResolutionStrategy())) {
			String conceptid = "";
			List<? extends I_IdPart> idParts = tf.getId(concept.getNid()).getVisibleIds(currenAceConfig.getViewPositionSetReadOnly(), 
					snomedIntId);
			if (idParts != null) {
				Object denotation = getLastCurrentVisibleId(idParts, currenAceConfig.getViewPositionSetReadOnly(), 
						RelAssertionType.INFERRED_THEN_STATED);
				if (denotation instanceof Long) {
					Long c = (Long) denotation;
					if (c != null)  conceptid = c.toString();
				}
			}
			/*int len= conceptid.length();
			CharSequence partition = conceptid.substring(len-3, len).subSequence(0, 2);
			if(partition.equals("00")){		*/	

			String active="0"; //Default value
			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allStatuses, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
				I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();				
				String conceptStatus = getStatusType(attributes.getStatusNid());
				if (conceptStatus.equals("0")) {
					active = "1";
				} else if (getConfig().getReleaseDate().compareTo(I_Constants.limited_policy_change)<0 && conceptStatus.equals("6")) {
					active = "1";
				} else {
					active = "0";
				}

				if ((conceptid==null || conceptid.equals("") || conceptid.equals("0")) && active.equals("1") ){
					conceptid=concept.getUids().iterator().next().toString();
				}
			}

			if (conceptid==null || conceptid.equals("") || conceptid.equals("0")){
				logger.info("Unplublished Retired Concept: " + concept.getUUIDs().iterator().next().toString());
			}else{
				if (conceptid.contains("-")){
					export(concept, conceptid);
				}
			}
		}
	}

	//all the contents resides under SNOMED CT Model Component (metadata) gets metamoduleid (900000000000012004)
	//This returns of the content which belongs meta-module
	public String computeModuleId(I_GetConceptData concept) throws IOException, TerminologyException, ContradictionException {
		String moduleid = I_Constants.CORE_MODULE_ID;	
		if (snomedCTModelComponent.isParentOf(concept, 
				currenAceConfig.getAllowedStatus(),
				currenAceConfig.getDestRelTypes(), 
				currenAceConfig.getViewPositionSetReadOnly(), 
				currenAceConfig.getPrecedence(), 
				currenAceConfig.getConflictResolutionStrategy())) {
			moduleid = I_Constants.META_MODULE_ID;
		}else if(snomedCTModelComponent.equals(concept)){
			moduleid = I_Constants.META_MODULE_ID;
		}

		/*if (foundationMetaDataConceptRoot.isParentOf(concept, 
				currenAceConfig.getAllowedStatus(),
				currenAceConfig.getDestRelTypes(), 
				currenAceConfig.getViewPositionSetReadOnly(), 
				currenAceConfig.getPrecedence(), 
				currenAceConfig.getConflictResolutionStrategy())) {
				isMetaConcept=true;
		}else if(coreMetaConceptRoot.isParentOf(concept, 
				currenAceConfig.getAllowedStatus(),
				currenAceConfig.getDestRelTypes(), 
				currenAceConfig.getViewPositionSetReadOnly(), 
				currenAceConfig.getPrecedence(), 
				currenAceConfig.getConflictResolutionStrategy())) {
				isMetaConcept=true;
		}else if(snomedCTModelComponent.equals(concept)){
				isMetaConcept=true;
		}*/


		return moduleid;
	}

	public NidSetBI getAllStatuses() throws TerminologyException, IOException {
		NidSetBI allStatuses = new NidSet();
		Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
		I_GetConceptData statusRoot = tf.getConcept(UUID.fromString("d944af55-86d9-33f4-bebd-a10bf3f4712c"));
		descendants = getDescendantsLocal(descendants, statusRoot );
		for (I_GetConceptData loopConcept : descendants) {
			allStatuses.add(loopConcept.getNid());
		}
		I_GetConceptData activeValue = tf.getConcept(UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));
		allStatuses.add(activeValue.getNid());
		descendants = getDescendantsLocal(descendants, activeValue );
		for (I_GetConceptData loopConcept : descendants) {
			allStatuses.add(loopConcept.getNid());
		}		
		I_GetConceptData inactiveValue = tf.getConcept(UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"));
		allStatuses.add(inactiveValue.getNid());
		descendants = getDescendantsLocal(descendants, inactiveValue );
		for (I_GetConceptData loopConcept : descendants) {
			allStatuses.add(loopConcept.getNid());
		}
		return allStatuses;
	}

	public NidSetBI getAllDescTypes() throws TerminologyException, IOException {
		NidSetBI allDescTypes = new NidSet();
		allDescTypes.add(tf.uuidToNative(UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf")));
		allDescTypes.add(tf.uuidToNative(UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2")));
		allDescTypes.add(tf.uuidToNative(UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09")));
		allDescTypes.add(tf.uuidToNative(UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66")));
		allDescTypes.add(tf.uuidToNative(UUID.fromString("d6fad981-7df6-3388-94d8-238cc0465a79")));
		allDescTypes.add(tf.uuidToNative(UUID.fromString("d8e3b37d-7c11-33ef-b1d0-8769e2264d44")));
		allDescTypes.add(tf.uuidToNative(UUID.fromString("aa610ed0-fb1c-364f-8db8-551da4c46419")));


		return allDescTypes;
	}

	public String getConceptMetaModuleID(I_GetConceptData concept , String effectiveTime) throws TerminologyException, IOException {
		return ExportUtil.getConceptMetaModuleID(concept , effectiveTime);
	}

	public  Set<I_GetConceptData> getDescendantsLocal(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			allowedDestRelTypes.add(termFactory.uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));

			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));
			descendants.addAll(childrenSet);
			for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendantsLocal(descendants, loopConcept);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return descendants;
	}

	public Object getLastCurrentVisibleId(List<? extends I_IdPart> parts, PositionSet viewpointSet,
			RelAssertionType relAssertionType) {
		Object data = null;
		if (getLastCurrentVisibleIdPart(parts, viewpointSet, relAssertionType) != null) {
			data = getLastCurrentVisibleIdPart(parts, viewpointSet, relAssertionType).getDenotation();
		}
		return data;
	}

	public I_IdPart getLastCurrentVisibleIdPart(List<? extends I_IdPart> parts, PositionSet viewpointSet,
			RelAssertionType relAssertionType) {
		//		System.out.println("Parts Size: " + parts.size());
		I_ConfigAceFrame config = null;
		int currentId = Integer.MIN_VALUE;
		int activeId = Integer.MIN_VALUE;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			currentId = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			activeId = Terms.get().uuidToNative(UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));

		} catch (TerminologyException e) {
			//
		} catch (IOException e) {
			//
		}
		I_IdPart currentVisiblePart = null;

		Long lastTime = Long.MIN_VALUE;
		Long maxTime = Long.MIN_VALUE;

		for (PositionBI viewpoint : viewpointSet) {
			if (viewpoint.getTime() > maxTime) {
				maxTime = viewpoint.getTime();
			}
		}
		RelAssertionType lastAssertionType = null;
		int classifierNid = config.getClassifierConcept().getNid();
		//		System.out.println("Current: " + currentId + " ClassifierNid: " + currentId);
		for (I_IdPart loopPart : parts) {
			I_IdPart loopTempPart = null;
			RelAssertionType loopAssertionType = null;
			//			System.out.println(loopPart.getTime() + "|" + loopPart.getStatusNid() + "|" + String.valueOf(loopPart.getDenotation())
			//					+ "|" + loopPart.getAuthorNid());
			if (loopPart.getTime() > lastTime && loopPart.getTime() <= maxTime) {
				if (relAssertionType == RelAssertionType.INFERRED && 
						loopPart.getAuthorNid() == classifierNid) {
					loopTempPart = loopPart;
					loopAssertionType = RelAssertionType.INFERRED;
				} else if (relAssertionType == RelAssertionType.INFERRED_THEN_STATED) {
					if (loopPart.getAuthorNid() == classifierNid) {
						loopTempPart = loopPart;
						loopAssertionType = RelAssertionType.INFERRED;
					} else if (lastAssertionType == null || lastAssertionType == RelAssertionType.STATED) {
						loopTempPart = loopPart;
						loopAssertionType = RelAssertionType.STATED;
					}
				} else if (relAssertionType == RelAssertionType.STATED && 
						loopPart.getAuthorNid() != classifierNid) {
					loopTempPart = loopPart;
					loopAssertionType = RelAssertionType.STATED;
				}

				if (loopTempPart != null) {
					if (loopTempPart.getStatusNid() == currentId || loopTempPart.getStatusNid()== activeId) {
						currentVisiblePart = loopTempPart;
						lastTime = loopPart.getTime();
						lastAssertionType = loopAssertionType;
					} else if (loopTempPart.getStatusNid() != currentId  
							&& loopTempPart.getStatusNid()!= activeId 
							&& currentVisiblePart != null) {
						if (loopTempPart.getDenotation().equals(currentVisiblePart.getDenotation())) {
							currentVisiblePart = null;
							lastTime = Long.MIN_VALUE;
							lastAssertionType = null;
						}
					}

				}

				// && loopPart.getStatusNid() == currentId
			}
		}
		return currentVisiblePart;
	}

	public abstract void export(I_GetConceptData concept, String conceptid) throws IOException;

	public int getNidFromTermComponent(I_AmTermComponent tc) {
		int nid = Integer.MIN_VALUE;
		// System.out.println(" CLASS " + tc.getClass().getSimpleName());

		if (I_DescriptionVersioned.class.isAssignableFrom(tc.getClass())) {
			I_DescriptionVersioned dv = (I_DescriptionVersioned) tc;
			nid = dv.getDescId();
		} else if (I_GetConceptData.class.isAssignableFrom(tc.getClass())) {
			I_GetConceptData cb = (I_GetConceptData) tc;
			nid = cb.getConceptNid();
		} else if (I_RelVersioned.class.isAssignableFrom(tc.getClass())) {
			// System.out.println(" ELSE IF ");
			I_RelVersioned rel = (I_RelVersioned) tc;
			nid = rel.getRelId();
		}
		return nid;
	}
}
