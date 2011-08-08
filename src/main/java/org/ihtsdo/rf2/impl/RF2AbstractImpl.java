package org.ihtsdo.rf2.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.core.factory.RF2ConceptFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.PositionSet;

public abstract class RF2AbstractImpl {

	private static Config config;

	// String timeFormat = I_Constants.TimeFormat;

	private final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

	private static Logger logger = Logger.getLogger(RF2AbstractImpl.class);

	private int dupRecord;
	private int metadataCount;
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
	private I_ManageContradiction contradictionMgr;

	// new fields
	protected I_TermFactory tf;
	protected I_ConfigAceFrame currenAceConfig;
	protected int snomedIntId;
	protected NidSetBI allStatuses;
	protected NidSetBI allDescTypes;
	protected NidSetBI descTypes;
	protected NidSetBI textDefinTypes;

	private I_GetConceptData snomedRoot;

	protected I_IntSet allStatusSet;

	protected int preferredNid;

	protected int acceptableNid;

	protected int currentNid;

	protected int retiredNid;	
	
	protected int activeNid ; //Active value	900000000000545005	
	
	protected int inactiveNid; //Inactive value	900000000000546006	

	private int activeStatus;

	private I_IntSet activeStatusSet;

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
			snomedRoot = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			this.preferredNid=tf.uuidToNative(UUID.fromString("266f1bc3-3361-39f3-bffe-69db9daea56e"));
			this.acceptableNid=tf.uuidToNative(UUID.fromString("12b9e103-060e-3256-9982-18c1191af60e"));
			this.currentNid=tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			this.retiredNid=tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
			this.activeNid = getNid("d12702ee-c37f-385f-a070-61d56d4d0f1f"); //Active value	900000000000545005	
			this.inactiveNid = getNid("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"); //Inactive value	900000000000546006	
		
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
//		descTypes.add(tf.uuidToNative(UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09")));
		return descTypes;
	}

	private void setupCoreNids() throws TerminologyException, IOException {

		// SETUP CORE NATIVES IDs
		setIsaNid(tf.uuidToNative(SNOMED.Concept.IS_A.getUids()));
		setRootNid(tf.uuidToNative(SNOMED.Concept.ROOT.getUids()));

		// Characteristic
		setIsCh_STATED_RELATIONSHIP(tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()));
		setIsCh_DEFINING_CHARACTERISTIC(tf.uuidToNative(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()));
		setIsCh_STATED_AND_INFERRED_RELATIONSHIP(tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP.getUids()));
		setIsCh_STATED_AND_SUBSUMED_RELATIONSHIP(tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_SUBSUMED_RELATIONSHIP.getUids()));
		setIsCh_INFERRED_RELATIONSHIP(tf.uuidToNative(ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids()));
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

	public String getRefinabilityStatusType(int status) throws TerminologyException, IOException {
		return ExportUtil.getRefinabilityStatusType(status);
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

	public String getSctId(int nid, int pathNid) throws IOException, TerminologyException {
		return ExportUtil.getSctId(nid, pathNid);
	}

	// get the description id for the given UUID
	public String getSCTId(Config config, UUID uuid) throws Exception {
		return ExportUtil.getSCTId(config, uuid);
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

	public I_ManageContradiction getContradictionMgr() {
		return contradictionMgr;
	}

	public void setContradictionMgr(I_ManageContradiction contradictionMgr) {
		this.contradictionMgr = contradictionMgr;
	}

	public void process(I_GetConceptData concept) throws IOException, TerminologyException {
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
				export(concept, conceptid);				
			//}	
		}
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
