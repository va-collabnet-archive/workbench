package org.ihtsdo.rf2.module.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.module.constant.I_Constants;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;
import org.ihtsdo.rf2.module.util.TestFilters;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

// TODO: Auto-generated Javadoc
/**
 * The Class RF2AbstractImpl.
 */
public abstract class RF2AbstractImpl {

	/** The config. */
	private static Config config;

	// String timeFormat = I_Constants.TimeFormat;
//
//	/** The kbase. */
//	private final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2AbstractImpl.class);

	/** The dup record. */
	private int dupRecord;
	
	/** The metadata count. */
	private int metadataCount=0;
	// CORE CONSTANTS
	/** The root nid. */
	private int rootNid;
	
	/** The isa nid. */
	private int isaNid;

	/** The is ch_ state d_ relationship. */
	private static int isCh_STATED_RELATIONSHIP;
	
	/** The is ch_ definin g_ characteristic. */
	private static int isCh_DEFINING_CHARACTERISTIC;
	
	/** The is ch_ state d_ an d_ inferre d_ relationship. */
	private static int isCh_STATED_AND_INFERRED_RELATIONSHIP;
	
	/** The is ch_ state d_ an d_ subsume d_ relationship. */
	private static int isCh_STATED_AND_SUBSUMED_RELATIONSHIP;
	
	/** The is ch_ inferre d_ relationship. */
	private static int isCh_INFERRED_RELATIONSHIP;

	/** The role type set. */
	private I_IntSet roleTypeSet;
	
	/** The status set. */
	private I_IntSet statusSet;
	
	/** The from path pos. */
	private PositionSetReadOnly fromPathPos;

	// GUI
	/** The gui. */
	I_ShowActivity gui;
	
	/** The precedence. */
	private Precedence precedence;
	
	/** The contradiction mgr. */
	private ContradictionManagerBI contradictionMgr;

	// new fields
	/** The tf. */
	protected I_TermFactory tf;
	
	/** The curren ace config. */
	protected I_ConfigAceFrame currenAceConfig;
	
	/** The snomed int id. */
	protected int snomedIntId;
	
	/** The all statuses. */
	protected NidSetBI allStatuses;
	
	/** The all desc types. */
	protected NidSetBI allDescTypes;
	
	/** The desc types. */
	protected NidSetBI descTypes;
	
	/** The text defin types. */
	protected NidSetBI textDefinTypes;

	/** The snomed root. */
	private I_GetConceptData snomedRoot;
	
	/** The snomed ct model component. */
	private I_GetConceptData snomedCTModelComponent;	
	
	/** The all status set. */
	protected I_IntSet allStatusSet;

	/** The preferred nid. */
	protected int preferredNid;

	/** The acceptable nid. */
	protected int acceptableNid;

	/** The current nid. */
	protected int currentNid;

	/** The retired nid. */
	protected int retiredNid;	
	
	/** The active nid. */
	protected int activeNid ; //Active value	900000000000545005	
	
	/** The inactive nid. */
	protected int inactiveNid; //Inactive value	900000000000546006	
	
	/** The null uuid. */
	protected String nullUuid; // null string to match with UUID.fromString("00000000-0000-0000-C000-000000000046")

	/** The active status. */
	private int activeStatus;

	/** The active status set. */
	private I_IntSet activeStatusSet;

	/**
	 * Gets the config.
	 *
	 * @return the config
	 */
	public static Config getConfig() {
		return config;
	}

	/**
	 * Sets the config.
	 *
	 * @param config the new config
	 */
	public static void setConfig(Config config) {
		RF2AbstractImpl.config = config;
	}

	/**
	 * Gets the time format.
	 *
	 * @return the time format
	 */
	public String getTimeFormat() {
		return ExportUtil.TIMEFORMAT;
	}

	/**
	 * Increment dup record.
	 */
	public void incrementDupRecord() {
		dupRecord++;
	}
	
	/**
	 * Increment meta data count.
	 */
	public void incrementMetaDataCount() {
		metadataCount++;
	}

	/**
	 * Gets the dup record.
	 *
	 * @return the dup record
	 */
	public int getDupRecord() {
		return dupRecord;
	}

	/**
	 * Gets the meta data count.
	 *
	 * @return the meta data count
	 */
	public int getMetaDataCount() {
		return metadataCount;
	}
	
//	/**
//	 * Gets the kbase.
//	 *
//	 * @return the kbase
//	 */
//	public KnowledgeBase getKbase() {
//		return kbase;
//	}

	/**
	 * Gets the module sctid for stamp nid.
	 *
	 * @param moduleNid the module nid
	 * @return the module sctid for stamp nid
	 */
	public String getModuleSCTIDForStampNid(int moduleNid){
		
		return ExportUtil.getModuleSCTIDForStampNid(moduleNid);
		
	}
	
	/**
	 * Instantiates a new r f2 abstract impl.
	 *
	 * @param config the config
	 */
	public RF2AbstractImpl(Config config) {

		RF2AbstractImpl.config = config;

		dupRecord = 0;

		try {
			this.tf = Terms.get();
			this.currenAceConfig = tf.getActiveAceFrameConfig();
			snomedIntId = tf.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());
			snomedRoot = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			
			snomedCTModelComponent = tf.getConcept(UUID.fromString("a60bd881-9010-3260-9653-0c85716b4391"));
			
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

	/**
	 * Gets the desc types.
	 *
	 * @return the desc types
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private NidSetBI getDescTypes() throws TerminologyException, IOException {
		NidSetBI descTypes = new NidSet();
		descTypes.add(tf.uuidToNative(UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf")));
		descTypes.add(tf.uuidToNative(UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2")));
		descTypes.add(tf.uuidToNative(UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09")));
		return descTypes;
	}

	/**
	 * Setup core nids.
	 *
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Gets the ace config.
	 *
	 * @return the ace config
	 */
	public I_ConfigAceFrame getAceConfig() {
		return ExportUtil.getAceConfig();
	}

	/**
	 * Gets the isa nid.
	 *
	 * @return the isa nid
	 */
	public int getIsaNid() {
		return isaNid;
	}

	/**
	 * Gets the term factory.
	 *
	 * @return the term factory
	 */
	public I_TermFactory getTermFactory() {
		return tf;
	}
	
	/**
	 * Checks if is component to publish.
	 *
	 * @param part the part
	 * @return true, if is component to publish
	 */
	protected boolean isComponentToPublish(I_AmPart part){
		boolean ret=true;
		TestFilters testFilters=config.getTestFilters();
		if (testFilters!=null){
			return testFilters.testAll(part);
		}
		return ret;
	}
	/*
	 * public static String getModuleID(String snomedIntegerId) { return ExportUtil.getModuleID(snomedIntegerId); }
	 */
	/**
	 * Close file writer.
	 *
	 * @param bw the bw
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void closeFileWriter(BufferedWriter bw) throws IOException {
		if (bw != null)
			bw.close();
	}
	
	/**
	 * Gets the latest active part.
	 *
	 * @param parts the parts
	 * @return the latest active part
	 * @throws Exception the exception
	 */
	public Long getLatestActivePart(List<I_RelPart> parts) throws Exception {
		return ExportUtil.getLatestActivePart(parts);
	}
	
	/**
	 * Gets the refinability status type.
	 *
	 * @param status the status
	 * @return the refinability status type
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String getRefinabilityStatusType(int status) throws TerminologyException, IOException {
		return ExportUtil.getRefinabilityStatusType(status);
	}
	
	/**
	 * Insert sct id.
	 *
	 * @param componentNid the component nid
	 * @param config the config
	 * @param wbSctId the wb sct id
	 * @param pathNid the path nid
	 * @param statusNid the status nid
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean insertSctId(int componentNid  ,Config config, String wbSctId, int pathNid, int statusNid) throws Exception {
		return ExportUtil.insertSctId(componentNid , getConfig(), wbSctId , pathNid , statusNid);
	}	
	
	/**
	 * Gets the concept inactivation status type.
	 *
	 * @param status the status
	 * @return the concept inactivation status type
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String getConceptInactivationStatusType(int status) throws TerminologyException, IOException {
		return ExportUtil.getConceptInactivationStatusType(status);
	}

	/**
	 * Gets the concept inactivation value id.
	 *
	 * @param status the status
	 * @return the concept inactivation value id
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String getConceptInactivationValueId(int status) throws TerminologyException, IOException {
		return ExportUtil.getConceptInactivationValueId(status);
	}

	/**
	 * Gets the desc inactivation value id.
	 *
	 * @param status the status
	 * @return the desc inactivation value id
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String getDescInactivationValueId(int status) throws TerminologyException, IOException {
		return ExportUtil.getDescInactivationValueId(status);
	}

	/**
	 * Gets the desc inactivation status type.
	 *
	 * @param status the status
	 * @return the desc inactivation status type
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String getDescInactivationStatusType(int status) throws TerminologyException, IOException {
		return ExportUtil.getDescInactivationStatusType(status);
	}

	/**
	 * Gets the refinability value id.
	 *
	 * @param charType the char type
	 * @return the refinability value id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public String getRefinabilityValueId(int charType) throws IOException, Exception {
		return ExportUtil.getRefinabilityValueId(charType);
	}

	/**
	 * Gets the refinability type.
	 *
	 * @param type the type
	 * @return the refinability type
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public String getRefinabilityType(int type) throws IOException, Exception {
		return ExportUtil.getRefinabilityType(type);
	}

	/**
	 * Gets the status type.
	 *
	 * @param status the status
	 * @return the status type
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String getStatusType(int status) throws TerminologyException, IOException {
		return ExportUtil.getStatusType(status);
	}

	/**
	 * Gets the snomed id.
	 *
	 * @param concept the concept
	 * @return the snomed id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public String getSnomedId(I_GetConceptData concept) throws IOException, TerminologyException {
		return ExportUtil.getSnomedId(concept);
	}

	/**
	 * Gets the ctv3 id.
	 *
	 * @param concept the concept
	 * @return the ctv3 id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public String getCtv3Id(I_GetConceptData concept) throws IOException, TerminologyException {
		return ExportUtil.getCtv3Id(concept);
	}

	/**
	 * Gets the relationship id version.
	 *
	 * @param denotion the denotion
	 * @param snomedAuthorityNid the snomed authority nid
	 * @return the relationship id version
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public String getRelationshipIdVersion(Object denotion, int snomedAuthorityNid) throws IOException, TerminologyException {
		return ExportUtil.getRelationshipIdVersion(denotion, snomedAuthorityNid);
	}

	/**
	 * Gets the refset id.
	 *
	 * @param typeId the type id
	 * @return the refset id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public String getRefsetId(String typeId) throws IOException, Exception {
		return ExportUtil.getRefsetId(typeId);
	}

	/**
	 * Gets the snomedid.
	 *
	 * @param config the config
	 * @param componentUuid the component uuid
	 * @param parentSnomedId the parent snomed id
	 * @return the snomedid
	 * @throws Exception the exception
	 */
	public String getSNOMEDID(Config config, UUID componentUuid, String parentSnomedId) throws Exception {
		return ExportUtil.getSNOMEDID(config ,componentUuid, parentSnomedId);
	}
	
	/**
	 * Gets the cT v3 id.
	 *
	 * @param config the config
	 * @param componentUuid the component uuid
	 * @return the cT v3 id
	 * @throws Exception the exception
	 */
	public String getCTV3ID(Config config, UUID componentUuid) throws Exception {
		return ExportUtil.getCTV3ID(config ,componentUuid);
	}
	
	/**
	 * Gets the parent local.
	 *
	 * @param parents the parents
	 * @param concept the concept
	 * @return the parent local
	 * @throws Exception the exception
	 */
	public  Set<I_GetConceptData>  getParentLocal(Set<I_GetConceptData> parents, I_GetConceptData concept) throws Exception {
		return ExportUtil.getParentLocal(parents ,concept);
	}

	/**
	 * Gets the nid.
	 *
	 * @param struuid the struuid
	 * @return the nid
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public int getNid(String struuid) throws TerminologyException, IOException {
		return ExportUtil.getNid(struuid);
	}

	/**
	 * Gets the snomed core path nid.
	 *
	 * @return the snomed core path nid
	 */
	public int getSnomedCorePathNid() {
		return ExportUtil.getSnomedCorePathNid();
	}

	/**
	 * Gets the snomed meta path nid.
	 *
	 * @return the snomed meta path nid
	 */
	public int getSnomedMetaPathNid() {
		return ExportUtil.getSnomedMetaPathNid();
	}

	/**
	 * Gets the snorocket author nid.
	 *
	 * @return the snorocket author nid
	 */
	public int getSnorocketAuthorNid() {
		return ExportUtil.getSnorocketAuthorNid();
	}
	
	/**
	 * Gets the user author nid.
	 *
	 * @return the user author nid
	 */
	public int getUserAuthorNid() {
		return ExportUtil.getUserAuthorNid();
	}

	/**
	 * Gets the snomed inferred path nid.
	 *
	 * @return the snomed inferred path nid
	 */
	public int getSnomedInferredPathNid() {
		return ExportUtil.getSnomedInferredPathNid();
	}

	/**
	 * Gets the snomed stated path nid.
	 *
	 * @return the snomed stated path nid
	 */
	public int getSnomedStatedPathNid() {
		return ExportUtil.getSnomedStatedPathNid();
	}
	
	/**
	 * Gets the parent snomed id.
	 *
	 * @param concept the concept
	 * @return the parent snomed id
	 * @throws Exception the exception
	 */
	public String getParentSnomedId(I_GetConceptData concept) throws Exception {
		return ExportUtil.getParentSnomedId(concept);
	}
	
	//Get the sctid for the given UUID
	/**
	 * Gets the sCT id.
	 *
	 * @param config the config
	 * @param componentUuid the component uuid
	 * @param namespaceId the namespace id
	 * @param partitionId the partition id
	 * @param releaseId the release id
	 * @param executionId the execution id
	 * @param moduleId the module id
	 * @return the sCT id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public String getSCTId(Config config, UUID componentUuid, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId) throws IOException, TerminologyException {
		return ExportUtil.getSCTId( config,  componentUuid,  namespaceId,  partitionId,  releaseId,  executionId,  moduleId);
	}


	/**
	 * Gets the sct id.
	 *
	 * @param nid the nid
	 * @return the sct id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public String getSctId(int nid) throws IOException, TerminologyException {
		return ExportUtil.getSctId(nid);
	}

	// get the id for the given UUID
	/**
	 * Gets the sCT id.
	 *
	 * @param config the config
	 * @param uuid the uuid
	 * @return the sCT id
	 * @throws Exception the exception
	 */
	public String getSCTId(Config config, UUID uuid) throws Exception {
		return ExportUtil.getSCTId(config, uuid);
	}
	
	// get the conceptid for the given UUID
	/**
	 * Gets the concept id.
	 *
	 * @param config the config
	 * @param uuid the uuid
	 * @return the concept id
	 * @throws Exception the exception
	 */
	public String getConceptId(Config config, UUID uuid) throws Exception {
		return ExportUtil.getConceptId(config, uuid);
	}
	
	/**
	 * Gets the concept id.
	 *
	 * @param concept the concept
	 * @return the concept id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public String getConceptId(I_GetConceptData concept) throws IOException, TerminologyException {
		return ExportUtil.getConceptId(concept);
	}

	/**
	 * Gets the partition id.
	 *
	 * @param sctId the sct id
	 * @return the partition id
	 */
	public String getPartitionId(String sctId) {
		return ExportUtil.getPartitionId(sctId);
	}

	/**
	 * Gets the snomed description type.
	 *
	 * @param wbDescriptionType the wb description type
	 * @return the snomed description type
	 */
	public String getSnomedDescriptionType(int wbDescriptionType) {
		return ExportUtil.getSnomedDescriptionType(wbDescriptionType);
	}

	/**
	 * Gets the description id.
	 *
	 * @param descriptionNid the description nid
	 * @param snomedCorePathNid the snomed core path nid
	 * @return the description id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public String getDescriptionId(int descriptionNid, int snomedCorePathNid) throws IOException, TerminologyException {
		return ExportUtil.getDescriptionId(descriptionNid);
	}
	
	
	// get the descriptionid for the given UUID
	/**
	 * Gets the description id.
	 *
	 * @param config the config
	 * @param uuid the uuid
	 * @return the description id
	 * @throws Exception the exception
	 */
	public String getDescriptionId(Config config, UUID uuid) throws Exception {
		return ExportUtil.getDescriptionId(config, uuid);
	}
	
	// get the relationshipid for the given UUID
	/**
	 * Gets the relationship id.
	 *
	 * @param config the config
	 * @param uuid the uuid
	 * @return the relationship id
	 * @throws Exception the exception
	 */
	public String getRelationshipId(Config config, UUID uuid) throws Exception {
		return ExportUtil.getRelationshipId(config, uuid);
	}

	
	/**
	 * Gets the characteristic type.
	 *
	 * @param type the type
	 * @return the characteristic type
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public String getCharacteristicType(int type) throws IOException, TerminologyException {
		return ExportUtil.getCharacteristicType(type);
	}

	/**
	 * Gets the date format.
	 *
	 * @return the date format
	 */
	public SimpleDateFormat getDateFormat() {
		return ExportUtil.DATEFORMAT;
	}

	/**
	 * Gets the type id.
	 *
	 * @param descType the desc type
	 * @return the type id
	 */
	public String getTypeId(String descType) {
		return ExportUtil.getTypeId(descType);
	}

	/**
	 * Gets the characteristic type id.
	 *
	 * @param characteristicType the characteristic type
	 * @return the characteristic type id
	 */
	public String getCharacteristicTypeId(String characteristicType) {
		return ExportUtil.getCharacteristicTypeId(characteristicType);
	}

	/**
	 * Gets the root nid.
	 *
	 * @return the root nid
	 */
	public int getRootNid() {
		return rootNid;
	}

	/**
	 * Sets the root nid.
	 *
	 * @param rootNid the new root nid
	 */
	public void setRootNid(int rootNid) {
		this.rootNid = rootNid;
	}

	/**
	 * Sets the isa nid.
	 *
	 * @param isaNid the new isa nid
	 */
	public void setIsaNid(int isaNid) {
		this.isaNid = isaNid;
	}

	/**
	 * Gets the checks if is ch_ state d_ relationship.
	 *
	 * @return the checks if is ch_ state d_ relationship
	 */
	public static int getIsCh_STATED_RELATIONSHIP() {
		return isCh_STATED_RELATIONSHIP;
	}

	/**
	 * Sets the checks if is ch_ state d_ relationship.
	 *
	 * @param isChSTATEDRELATIONSHIP the new checks if is ch_ state d_ relationship
	 */
	public static void setIsCh_STATED_RELATIONSHIP(int isChSTATEDRELATIONSHIP) {
		isCh_STATED_RELATIONSHIP = isChSTATEDRELATIONSHIP;
	}

	/**
	 * Gets the checks if is ch_ definin g_ characteristic.
	 *
	 * @return the checks if is ch_ definin g_ characteristic
	 */
	public static int getIsCh_DEFINING_CHARACTERISTIC() {
		return isCh_DEFINING_CHARACTERISTIC;
	}

	/**
	 * Sets the checks if is ch_ definin g_ characteristic.
	 *
	 * @param isChDEFININGCHARACTERISTIC the new checks if is ch_ definin g_ characteristic
	 */
	public static void setIsCh_DEFINING_CHARACTERISTIC(int isChDEFININGCHARACTERISTIC) {
		isCh_DEFINING_CHARACTERISTIC = isChDEFININGCHARACTERISTIC;
	}

	/**
	 * Gets the checks if is ch_ state d_ an d_ inferre d_ relationship.
	 *
	 * @return the checks if is ch_ state d_ an d_ inferre d_ relationship
	 */
	public static int getIsCh_STATED_AND_INFERRED_RELATIONSHIP() {
		return isCh_STATED_AND_INFERRED_RELATIONSHIP;
	}

	/**
	 * Sets the checks if is ch_ state d_ an d_ inferre d_ relationship.
	 *
	 * @param isChSTATEDANDINFERREDRELATIONSHIP the new checks if is ch_ state d_ an d_ inferre d_ relationship
	 */
	public static void setIsCh_STATED_AND_INFERRED_RELATIONSHIP(int isChSTATEDANDINFERREDRELATIONSHIP) {
		isCh_STATED_AND_INFERRED_RELATIONSHIP = isChSTATEDANDINFERREDRELATIONSHIP;
	}

	/**
	 * Gets the checks if is ch_ state d_ an d_ subsume d_ relationship.
	 *
	 * @return the checks if is ch_ state d_ an d_ subsume d_ relationship
	 */
	public static int getIsCh_STATED_AND_SUBSUMED_RELATIONSHIP() {
		return isCh_STATED_AND_SUBSUMED_RELATIONSHIP;
	}

	/**
	 * Sets the checks if is ch_ state d_ an d_ subsume d_ relationship.
	 *
	 * @param isChSTATEDANDSUBSUMEDRELATIONSHIP the new checks if is ch_ state d_ an d_ subsume d_ relationship
	 */
	public static void setIsCh_STATED_AND_SUBSUMED_RELATIONSHIP(int isChSTATEDANDSUBSUMEDRELATIONSHIP) {
		isCh_STATED_AND_SUBSUMED_RELATIONSHIP = isChSTATEDANDSUBSUMEDRELATIONSHIP;
	}

	/**
	 * Gets the checks if is ch_ inferre d_ relationship.
	 *
	 * @return the checks if is ch_ inferre d_ relationship
	 */
	public static int getIsCh_INFERRED_RELATIONSHIP() {
		return isCh_INFERRED_RELATIONSHIP;
	}

	/**
	 * Sets the checks if is ch_ inferre d_ relationship.
	 *
	 * @param isChINFERREDRELATIONSHIP the new checks if is ch_ inferre d_ relationship
	 */
	public static void setIsCh_INFERRED_RELATIONSHIP(int isChINFERREDRELATIONSHIP) {
		isCh_INFERRED_RELATIONSHIP = isChINFERREDRELATIONSHIP;
	}

	/**
	 * Gets the role type set.
	 *
	 * @return the role type set
	 */
	public I_IntSet getRoleTypeSet() {
		return roleTypeSet;
	}

	/**
	 * Sets the role type set.
	 *
	 * @param roleTypeSet the new role type set
	 */
	public void setRoleTypeSet(I_IntSet roleTypeSet) {
		this.roleTypeSet = roleTypeSet;
	}

	/**
	 * Gets the status set.
	 *
	 * @return the status set
	 */
	public I_IntSet getStatusSet() {
		return statusSet;
	}

	/**
	 * Sets the status set.
	 *
	 * @param statusSet the new status set
	 */
	public void setStatusSet(I_IntSet statusSet) {
		this.statusSet = statusSet;
	}

	/**
	 * Gets the from path pos.
	 *
	 * @return the from path pos
	 */
	public PositionSetReadOnly getFromPathPos() {
		return fromPathPos;
	}

	/**
	 * Sets the from path pos.
	 *
	 * @param fromPathPos the new from path pos
	 */
	public void setFromPathPos(PositionSetReadOnly fromPathPos) {
		this.fromPathPos = fromPathPos;
	}

	/**
	 * Gets the gui.
	 *
	 * @return the gui
	 */
	public I_ShowActivity getGui() {
		return gui;
	}

	/**
	 * Sets the gui.
	 *
	 * @param gui the new gui
	 */
	public void setGui(I_ShowActivity gui) {
		this.gui = gui;
	}

	/**
	 * Gets the precedence.
	 *
	 * @return the precedence
	 */
	public Precedence getPrecedence() {
		return precedence;
	}

	/**
	 * Sets the precedence.
	 *
	 * @param precedence the new precedence
	 */
	public void setPrecedence(Precedence precedence) {
		this.precedence = precedence;
	}

	/**
	 * Gets the contradiction mgr.
	 *
	 * @return the contradiction mgr
	 */
	public ContradictionManagerBI getContradictionMgr() {
		return contradictionMgr;
	}

	/**
	 * Sets the contradiction mgr.
	 *
	 * @param contradictionMgr the new contradiction mgr
	 */
	public void setContradictionMgr(ContradictionManagerBI contradictionMgr) {
		this.contradictionMgr = contradictionMgr;
	}

	/**
	 * Process.
	 *
	 * @param concept the concept
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * @throws ContradictionException the contradiction exception
	 */
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
					export(concept, conceptid);
			}
		}
	}
	
	//all the contents resides under SNOMED CT Model Component (metadata) gets metamoduleid (900000000000012004)
	//This returns of the content which belongs meta-module
	/**
	 * Compute module id.
	 *
	 * @param concept the concept
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * @throws ContradictionException the contradiction exception
	 */
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
		
		return moduleid;
	}
	
	/**
	 * Gets the all statuses.
	 *
	 * @return the all statuses
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Gets the all desc types.
	 *
	 * @return the all desc types
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NidSetBI getAllDescTypes() throws TerminologyException, IOException {
		NidSetBI allDescTypes = new NidSet();
		allDescTypes.add(tf.uuidToNative(UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf")));
		allDescTypes.add(tf.uuidToNative(UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2")));
		allDescTypes.add(tf.uuidToNative(UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09")));
		return allDescTypes;
	}

	
	/**
	 * Gets the descendants local.
	 *
	 * @param descendants the descendants
	 * @param concept the concept
	 * @return the descendants local
	 */
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

	/**
	 * Gets the last current visible id.
	 *
	 * @param parts the parts
	 * @param viewpointSet the viewpoint set
	 * @param relAssertionType the rel assertion type
	 * @return the last current visible id
	 */
	public Object getLastCurrentVisibleId(List<? extends I_IdPart> parts, PositionSet viewpointSet,
			RelAssertionType relAssertionType) {
		Object data = null;
		if (getLastCurrentVisibleIdPart(parts, viewpointSet, relAssertionType) != null) {
			data = getLastCurrentVisibleIdPart(parts, viewpointSet, relAssertionType).getDenotation();
		}
		return data;
	}

	/**
	 * Gets the last current visible id part.
	 *
	 * @param parts the parts
	 * @param viewpointSet the viewpoint set
	 * @param relAssertionType the rel assertion type
	 * @return the last current visible id part
	 */
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

	/**
	 * Export.
	 *
	 * @param concept the concept
	 * @param conceptid the conceptid
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void export(I_GetConceptData concept, String conceptid) throws IOException;

}
