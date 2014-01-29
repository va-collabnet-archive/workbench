package org.ihtsdo.rf2.module.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.idgeneration.IdAssignmentImpl;
import org.ihtsdo.rf2.module.constant.I_Constants;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

// TODO: Auto-generated Javadoc
//import org.ihtsdo.tk.api.Precedence;

/**
 * The Class ExportUtil.
 */
public class ExportUtil {


	/** The Constant META_MODULEID_PARENT. */
	private static final String META_MODULEID_PARENT = "40d1c869-b509-32f8-b735-836eac577a67";

	// store the handle for the log files
	/** The ace config. */
	private static I_ConfigAceFrame aceConfig;

	// log4j logging
	/** The logger. */
	private static Logger logger = Logger.getLogger(ExportUtil.class.getName());
	
	/** The user author nid. */
	private static int userAuthorNid;


	/** The timeformat. */
	public static String TIMEFORMAT = I_Constants.TimeFormat;
	
	/** The dateformat. */
	public static SimpleDateFormat DATEFORMAT = new SimpleDateFormat(TIMEFORMAT);

	/** The all module map nid sct id. */
	private static HashMap<Integer,String> allModuleMapNidSCTId;

	/** The active id. */
	public static int activeId; 
	
	/** The inact id. */
	public static int inactId; 
	
	/** The con ret id. */
	public static int conRetId;
	
	/** The ret id. */
	public static int retId ;
	
	/** The dup id. */
	public static int dupId ;
	
	/** The cur id. */
	public static int curId ;
	
	/** The outdated id. */
	public static int outdatedId ;
	
	/** The ambiguous id. */
	public static int ambiguousId;
	
	/** The err id. */
	public static int errId ;
	
	/** The lim id. */
	public static int limId;
	
	/** The mov id. */
	public static int movId ;
	
	/** The pend id. */
	public static int pendId ;
	
	/** The inappropriate id. */
	public static int inappropriateId ;

	/** The arc aux snomed integer nid. */
	private static int arcAuxSnomedIntegerNid;

	/** The arc aux snomed rt nid. */
	private static int arcAuxSnomedRTNid;

	/** The arc aux ctv3 nid. */
	private static int arcAuxCtv3Nid;
	static{
		try {

			activeId = SnomedMetadataRfx.getSTATUS_CURRENT_NID();
			inactId =  getTermFactory().uuidToNative(UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"));
			conRetId = getTermFactory().uuidToNative(UUID.fromString("6cc3df26-661e-33cd-a93d-1c9e797c90e3"));
			retId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
			dupId = getTermFactory().uuidToNative(SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getUUIDs().get(0));
			curId =ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
			arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
			arcAuxSnomedRTNid = ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.localize().getNid();
			arcAuxCtv3Nid = ArchitectonicAuxiliary.Concept.CTV3_ID.localize().getNid();
			outdatedId = getTermFactory().uuidToNative(SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getUUIDs().get(0));
			ambiguousId =getTermFactory().uuidToNative(SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getUUIDs().get(0));
			errId = getTermFactory().uuidToNative(SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getUUIDs().get(0));
			limId = getTermFactory().uuidToNative(SnomedMetadataRfx.getSTATUS_LIMITED().getLenient().getUUIDs().get(0));
			movId = getTermFactory().uuidToNative(UUID.fromString("95028943-b11c-3509-b1c0-c4ae16aaad5c"));
			pendId = getTermFactory().uuidToNative(UUID.fromString("9906317a-f50f-30f6-8b59-a751ae1cdeb9"));
			inappropriateId = getTermFactory().uuidToNative(UUID.fromString("bcb2ccda-d62a-3fc8-b158-10ad673823b6"));
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inits the.
	 *
	 * @param config the config
	 */
	public static void init(Config config) {

		InitializeModuleID(config);		
	}

	/**
	 * Initialize module id.
	 *
	 * @param config the config
	 */
	public static void InitializeModuleID(Config config) {
		try{
			if (allModuleMapNidSCTId!=null && allModuleMapNidSCTId.size()>0){
				return;
			}
			allModuleMapNidSCTId=new HashMap<Integer, String>();
			HashMap<UUID, String> allModuleIds=new HashMap<UUID, String>();
			I_GetConceptData metaModuleIdParent = getTermFactory().getConcept(UUID.fromString(META_MODULEID_PARENT));
			Set <I_GetConceptData>moduleIdDescendants=new HashSet<I_GetConceptData>();
			moduleIdDescendants = getDescendantsLocal(moduleIdDescendants, metaModuleIdParent );

			for (I_GetConceptData loopConcept : moduleIdDescendants) {
				allModuleIds.put(loopConcept.getPrimUuid(),"");
			}	
			CompleteMapSCTIDtoModule(config, allModuleIds);
		} catch (FileNotFoundException e){ 
			logger.error(e.getMessage()); 
		} catch (IOException e) { 
			logger.error(e.getMessage()); 
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Complete map scti dto module.
	 *
	 * @param config the config
	 * @param allModuleIds the all module ids
	 */
	private static void CompleteMapSCTIDtoModule(Config config,HashMap<UUID, String> allModuleIds) {

		String part="00";
		if (config.getNamespaceId()!="0"){
			part="10";
		}
		for (UUID uuid:allModuleIds.keySet()){

			String moduleSCTId=getSCTId(config,uuid, Integer.parseInt(config.getNamespaceId()) , part, config.getReleaseId(),config.getExecutionId(), "");
			try {
				allModuleMapNidSCTId.put(getTermFactory().uuidToNative(uuid), moduleSCTId);
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Gets the parent snomed id.
	 *
	 * @param concept the concept
	 * @return the parent snomed id
	 * @throws Exception the exception
	 */
	public static String getParentSnomedId(I_GetConceptData concept) throws Exception{		
		Set<I_GetConceptData> parents = new HashSet<I_GetConceptData>();
		parents = getParentLocal(parents, concept); 
		String parentSnomedId="";

		for (I_GetConceptData loopConcept : parents) {
			parentSnomedId = getSnomedId(loopConcept);
			if(parentSnomedId!=null){
				return parentSnomedId;
			}
		}

		return "";
	}


	/**
	 * Gets the latest active part.
	 *
	 * @param parts the parts
	 * @return the latest active part
	 * @throws Exception the exception
	 */
	public static Long getLatestActivePart(List<I_RelPart> parts)
			throws Exception {
		long latestVersion = Integer.MIN_VALUE;
		for (I_RelPart rel : parts) {
			if (rel.getTime() > latestVersion && rel.getStatusNid()==getNid("d12702ee-c37f-385f-a070-61d56d4d0f1f")) {
				latestVersion = rel.getTime();
			}
		}
		if (latestVersion>Integer.MIN_VALUE)
			return latestVersion;

		return null;	  
	}


	/**
	 * Gets the descendants local.
	 *
	 * @param descendants the descendants
	 * @param concept the concept
	 * @return the descendants local
	 */
	public static  Set<I_GetConceptData> getDescendantsLocal(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
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


	//Add specific parent who has closest snomedid
	/**
	 * Gets the parent local.
	 *
	 * @param parent the parent
	 * @param concept the concept
	 * @return the parent local
	 */
	public static  Set<I_GetConceptData> getParentLocal(Set<I_GetConceptData> parent, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			Collection<UUID> uids = new ArrayList<UUID>();
			UUID uuid = UUID.fromString(I_Constants.IS_A_UID);
			uids.add(uuid);
			allowedDestRelTypes.add(getTermFactory().uuidToNative(uids));			
			//will have to check if this is sufficient
			//allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));

			Set<I_GetConceptData> parentSet = new HashSet<I_GetConceptData>();
			parentSet.addAll(concept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));

			boolean findParentSnomedId = true;
			String parentSnomedId="";

			for (I_GetConceptData loopConcept : parentSet) {
				if(findParentSnomedId){
					parentSnomedId = getSnomedId(loopConcept);	
					if(parentSnomedId!=null){
						parent.addAll(parentSet);
						findParentSnomedId = false;
					}
				}

				if(findParentSnomedId){
					parent = getParentLocal(parent, loopConcept);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return parent;
	}

	/**
	 * Creates the term factory.
	 *
	 * @param db the db
	 * @throws InstantiationException the instantiation exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws ClassNotFoundException the class not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void createTermFactory(Database db) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

		File vodbDirectory = new File(db.getLocation());

		DatabaseSetupConfig dbSetupConfig = new DatabaseSetupConfig();
		Terms.createFactory(vodbDirectory, I_Constants.readOnly, I_Constants.cacheSize, dbSetupConfig);
	}

	/**
	 * Gets the term factory.
	 *
	 * @return the term factory
	 */
	public static I_TermFactory getTermFactory() {
		// since we are using mojo this handles the return of the opened database
		I_TermFactory termFactory = Terms.get();
		return termFactory;
	}

	/**
	 * Gets the ace config.
	 *
	 * @return the ace config
	 */
	public static I_ConfigAceFrame getAceConfig() {
		return aceConfig;
	}

	/**
	 * Gets the refinability status type.
	 *
	 * @param status the status
	 * @return the refinability status type
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getRefinabilityStatusType(int status) throws TerminologyException, IOException {
		String statusType = "99";
		int curId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		int retId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();

		int conRetId_Term_Aux = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();		
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();		
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int ambiguousId_Term_Aux = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();
		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int inappropriateId_Term_Aux = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();

		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); //Concept non-current (foundation metadata concept)	900000000000495008
		int dupId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); //Dups	900000000000482003
		int outdatedId = SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid(); //Outdated	900000000000483008
		int ambiguousId = SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid(); //Ambiguous component (foundation metadata concept)	900000000000484002
		int errId = SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid(); //Erroneous component (foundation metadata concept)	900000000000485001
		int limId = SnomedMetadataRfx.getSTATUS_LIMITED_NID(); //Limited	900000000000486000
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); //Component Moved elsewhere	900000000000487009
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); //Dups	900000000000492006	
		int inappropriateId = SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID(); //In-appropriate	900000000000494007

		if (status == curId)
			statusType = "0";
		else if (status == retId)
			statusType = "1";
		else if (status == dupId)
			statusType = "2";
		else if (status == outdatedId)
			statusType = "3";
		else if (status == ambiguousId)
			statusType = "4";
		else if (status == errId)
			statusType = "5";
		else if (status == limId)
			statusType = "6";
		else if (status == inappropriateId)
			statusType = "7";
		else if (status == conRetId)
			statusType = "8";
		else if (status == movId)
			statusType = "10";
		else if (status == pendId)
			statusType = "11";		
		else if (status == dupId_Term_Aux)
			statusType = "2";
		else if (status == outdatedId_Term_Aux)
			statusType = "3";
		else if (status == ambiguousId_Term_Aux)
			statusType = "4";
		else if (status == errId_Term_Aux)
			statusType = "5";
		else if (status == limId_Term_Aux)
			statusType = "6";
		else if (status == inappropriateId_Term_Aux)
			statusType = "7";
		else if (status == conRetId_Term_Aux)
			statusType = "8";
		else if (status == movId_Term_Aux)
			statusType = "10";
		else if (status == pendId_Term_Aux)
			statusType = "11";
		return statusType;
	}

	/**
	 * Gets the concept inactivation status type.
	 *
	 * @param status the status
	 * @return the concept inactivation status type
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getConceptInactivationStatusType(int status) throws TerminologyException, IOException {
		String statusType = "99";

		int curId_Term_Aux = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		int retId_Term_Aux = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		int conRetId_Term_Aux = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();		
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();		
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int ambiguousId_Term_Aux = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();
		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int inappropriateId_Term_Aux = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();

		int retId = getNid("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"); //Inactive value	900000000000546006
		int curId = SnomedMetadataRfx.getSTATUS_CURRENT_NID(); //Active value	900000000000545005
		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); //Concept non-current (foundation metadata concept)	900000000000495008
		int dupId =SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid(); //Dups	900000000000482003
		int outdatedId =SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid(); //Outdated	900000000000483008
		int ambiguousId = SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid(); //Ambiguous component (foundation metadata concept)	900000000000484002
		int errId = SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid(); //Erroneous component (foundation metadata concept)	900000000000485001
		int limId = SnomedMetadataRfx.getSTATUS_LIMITED_NID(); //Limited	900000000000486000
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); //Component Moved elsewhere	900000000000487009
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); //Pending	900000000000492006	
		int inappropriateId = SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID(); //In-appropriate	900000000000494007

		if (status == retId_Term_Aux) 
			statusType = "1";
		else if (status == curId_Term_Aux) 
			statusType = "0";
		else if (status == curId)
			statusType = "0";
		else if (status == retId)
			statusType = "1";		
		else if (status == dupId)
			statusType = "2";
		else if (status == outdatedId)
			statusType = "3";
		else if (status == ambiguousId)
			statusType = "4";
		else if (status == errId)
			statusType = "5";
		else if (status == limId)
			statusType = "6";
		else if (status == inappropriateId)
			statusType = "7";
		else if (status == conRetId)
			statusType = "8";
		else if (status == movId)
			statusType = "10";
		else if (status == pendId)
			statusType = "11";		
		else if (status == dupId_Term_Aux)
			statusType = "2";
		else if (status == outdatedId_Term_Aux)
			statusType = "3";
		else if (status == ambiguousId_Term_Aux)
			statusType = "4";
		else if (status == errId_Term_Aux)
			statusType = "5";
		else if (status == limId_Term_Aux)
			statusType = "6";
		else if (status == inappropriateId_Term_Aux)
			statusType = "7";
		else if (status == conRetId_Term_Aux)
			statusType = "8";
		else if (status == movId_Term_Aux)
			statusType = "10";
		else if (status == pendId_Term_Aux)
			statusType = "11";
		return statusType;
	}

	/**
	 * Gets the concept inactivation value id.
	 *
	 * @param status the status
	 * @return the concept inactivation value id
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getConceptInactivationValueId(int status) throws TerminologyException, IOException {
		String valueId = "XXX";

		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int ambiguousId_Term_Aux = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();

		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); 			//Pending move (foundation metadata concept)	900000000000492006	
		int dupId = SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid(); 			//Dups	900000000000482003
		int outdatedId = SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid(); 		//Outdated	900000000000483008
		int ambiguousId = SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid(); 		//Ambiguous component (foundation metadata concept)	900000000000484002
		int errId = SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid(); 			//Erroneous component (foundation metadata concept)	900000000000485001
		int limId = SnomedMetadataRfx.getSTATUS_LIMITED_NID(); 			//Limited	900000000000486000
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); 	//Component Moved elsewhere	900000000000487009

		// if (status==retId) valueId="Don'tknow value"; //1 Retired without reason (foundation metadata concept)
		if (status == dupId)
			valueId = I_Constants.DUPLICATE; 		// 2 Duplicate component
		else if (status == outdatedId)
			valueId = I_Constants.OUTDATED; 		// 3 Outdated component
		else if (status == ambiguousId)
			valueId = I_Constants.AMBIGUOUS; 		// 4 Ambiguous component
		else if (status == errId)
			valueId = I_Constants.ERRONEOUS; 		// 5 Erroneous component
		else if (status == limId)
			valueId = I_Constants.LIMITED; 			// 6 Limited component
		else if (status == movId)
			valueId = I_Constants.MOVED_ELSE_WHERE; // 10 Component moved elsewhere
		else if (status == pendId)
			valueId = I_Constants.PENDING_MOVE; 	// 11 Pending move
		if (status == dupId_Term_Aux)
			valueId = I_Constants.DUPLICATE; 		// 2 Duplicate component
		else if (status == outdatedId_Term_Aux)
			valueId = I_Constants.OUTDATED; 		// 3 Outdated component
		else if (status == ambiguousId_Term_Aux)
			valueId = I_Constants.AMBIGUOUS; 		// 4 Ambiguous component
		else if (status == errId_Term_Aux)
			valueId = I_Constants.ERRONEOUS; 		// 5 Erroneous component
		else if (status == limId_Term_Aux)
			valueId = I_Constants.LIMITED; 			// 6 Limited component
		else if (status == movId_Term_Aux)
			valueId = I_Constants.MOVED_ELSE_WHERE; // 10 Component moved elsewhere
		else if (status == pendId_Term_Aux)
			valueId = I_Constants.PENDING_MOVE; 	// 11 Pending move
		return valueId;
	}


	/**
	 * Gets the desc inactivation value id.
	 *
	 * @param status the status
	 * @return the desc inactivation value id
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getDescInactivationValueId(int status) throws TerminologyException, IOException {
		String valueId = "XXX";
		int inappropriateId_Term_Aux = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();
		int conRetId_Term_Aux = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();

		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();

		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); 			//Concept non-current (foundation metadata concept)	900000000000495008
		int inappropriateId = SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID(); 	//In-appropriate	900000000000494007
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); 			//Pending move (foundation metadata concept)	900000000000492006	
		int dupId = SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid(); 			//Dups	900000000000482003
		int outdatedId = SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid(); 		//Outdated	900000000000483008
		int errId = SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid(); 			//Erroneous component (foundation metadata concept)	900000000000485001
		int limId = SnomedMetadataRfx.getSTATUS_LIMITED_NID(); 			//Limited	900000000000486000
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); 			//Component Moved elsewhere	900000000000487009

		// if (status==retId) valueId="Don'tknow value"; //1 Retired without reason (foundation metadata concept)
		if (status == dupId)
			valueId = I_Constants.DUPLICATE; // 2 Duplicate component
		else if (status == outdatedId)
			valueId = I_Constants.OUTDATED; // 3 Outdated component
		else if (status == errId)
			valueId = I_Constants.ERRONEOUS; // 5 Erroneous component
		else if (status == limId)
			valueId = I_Constants.LIMITED; // 6 Limited component
		else if (status == inappropriateId)
			valueId = I_Constants.INAPPROPRIATE; // 7 Inappropriate component
		else if (status == conRetId)
			valueId = I_Constants.CONCEPT_NON_CURRENT; // 8 Concept non-current
		else if (status == movId)
			valueId = I_Constants.MOVED_ELSE_WHERE; // 10 Component moved elsewhere
		else if (status == pendId)
			valueId = I_Constants.PENDING_MOVE; // 11 Pending move
		else if (status == dupId_Term_Aux)
			valueId = I_Constants.DUPLICATE; // 2 Duplicate component
		else if (status == outdatedId_Term_Aux)
			valueId = I_Constants.OUTDATED; // 3 Outdated component
		else if (status == errId_Term_Aux)
			valueId = I_Constants.ERRONEOUS; // 5 Erroneous component
		else if (status == limId_Term_Aux)
			valueId = I_Constants.LIMITED; // 6 Limited component
		else if (status == inappropriateId_Term_Aux)
			valueId = I_Constants.INAPPROPRIATE; // 7 Inappropriate component
		else if (status == conRetId_Term_Aux)
			valueId = I_Constants.CONCEPT_NON_CURRENT; // 8 Concept non-current
		else if (status == movId_Term_Aux)
			valueId = I_Constants.MOVED_ELSE_WHERE; // 10 Component moved elsewhere
		else if (status == pendId_Term_Aux)
			valueId = I_Constants.PENDING_MOVE; // 11 Pending move
		return valueId;
	}

	/**
	 * Gets the desc inactivation status type.
	 *
	 * @param status the status
	 * @return the desc inactivation status type
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getDescInactivationStatusType(int status) throws TerminologyException, IOException {
		String statusType = "50";
		int conRetId = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();
		int retId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		int dupId = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();
		int curId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		int outdatedId = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int ambiguousId = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();
		int errId = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();
		int pendId = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int inappropriateId = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();
		if (status == curId)
			statusType = "0";
		else if (status == retId)
			statusType = "1";
		else if (status == dupId)
			statusType = "2";
		else if (status == outdatedId)
			statusType = "3";
		else if (status == ambiguousId)
			statusType = "4";
		else if (status == errId)
			statusType = "5";
		else if (status == limId)
			statusType = "6";
		else if (status == inappropriateId)
			statusType = "7";
		else if (status == conRetId)
			statusType = "8";
		else if (status == movId)
			statusType = "10";
		else if (status == pendId)
			statusType = "11";
		return statusType;
	}

	/**
	 * Gets the refinability value id.
	 *
	 * @param charType the char type
	 * @return the refinability value id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public static String getRefinabilityValueId(int charType) throws IOException, Exception {
		String refinabilityType = "99";
		if (charType == 0)
			refinabilityType = I_Constants.NON_REFINABLE;
		else if (charType == 1)
			refinabilityType = I_Constants.OPTIONAL_REFINABLE;
		else if (charType == 2)
			refinabilityType = I_Constants.MANTOTARY;
		return refinabilityType;
	}

	/**
	 * Gets the refinability type.
	 *
	 * @param type the type
	 * @return the refinability type
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public static String getRefinabilityType(int type) throws IOException, Exception {
		String charType = "99";
		int notRefiniableNid_Term_Aux = ArchitectonicAuxiliary.Concept.NOT_REFINABLE.localize().getNid();
		int optionalNid_Term_Aux = ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.localize().getNid();
		int mandatoryNid_Term_Aux = ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.localize().getNid();
		int notRefiniableNid =getNid("ce30636d-bfc9-3a70-9678-abc6b542ab4c"); //Not refinable	900000000000007000	
		int optionalNid= getNid("7d2d6cd0-c727-397e-9bc8-da65562e9350"); //Optional refinability	900000000000216007
		int mandatoryNid =getNid("67a79b5a-d56e-37f6-ad66-da712e39c453"); //Mandatory refinability	900000000000218008	

		if (type == notRefiniableNid_Term_Aux)
			charType = "0";
		else if (type == optionalNid_Term_Aux)
			charType = "1";
		else if (type == mandatoryNid_Term_Aux)
			charType = "2";
		else if (type == notRefiniableNid)
			charType = "0";
		else if (type == optionalNid)
			charType = "1";
		else if (type == mandatoryNid)
			charType = "2";
		return charType;
	}



	/**
	 * Gets the status type.
	 *
	 * @param status the status
	 * @return the status type
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getStatusType(int status) throws TerminologyException, IOException {
		String statusType = "99";		

		int curId_Term_Aux = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		int retId_Term_Aux = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();
		int inappropriateId_Term_Aux = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();
		int conRetId_Term_Aux = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int ambiguousId_Term_Aux = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();

		int retId = getNid("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"); //Inactive value	900000000000546006
		int curId = SnomedMetadataRfx.getSTATUS_CURRENT_NID(); //Active value	900000000000545005
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); //Pending	900000000000492006	
		int inappropriateId = SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID(); //In-appropriate	900000000000494007
		int limId =SnomedMetadataRfx.getSTATUS_LIMITED_NID(); //Limited	900000000000486000
		int outdatedId = SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid(); //Outdated	900000000000483008
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); //Component Moved elsewhere	900000000000487009
		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); //Concept non-current (foundation metadata concept)	900000000000495008
		int errId = SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid(); //Erroneous component (foundation metadata concept)	900000000000485001
		int ambiguousId = SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid(); //Ambiguous component (foundation metadata concept)	900000000000484002
		int dupId = SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid(); //Dups	900000000000482003

		if (status == retId) {
			statusType = "1";
		} else if (status == retId_Term_Aux) {
			statusType = "1";		
		} else if (status == curId) {
			statusType = "0";
		} else if (status == curId_Term_Aux) {
			statusType = "0";
		}  else if (status == dupId) {
			statusType = "2";
		} else if (status == dupId_Term_Aux) {
			statusType = "2";
		} else if (status == outdatedId) {
			statusType = "3";
		} else if (status == outdatedId_Term_Aux) {
			statusType = "3";
		} else if (status == ambiguousId) {
			statusType = "4";
		} else if (status == ambiguousId_Term_Aux) {
			statusType = "4";
		} else if (status == errId) {
			statusType = "5";
		} else if (status == errId_Term_Aux) {
			statusType = "5";
		} else if (status == limId) {
			statusType = "6";
		} else if (status == limId_Term_Aux) {
			statusType = "6";
		} else if (status == inappropriateId) {
			statusType = "7";
		} else if (status == inappropriateId_Term_Aux) {
			statusType = "7";
		} else if (status == conRetId) {
			statusType = "8";
		} else if (status == conRetId_Term_Aux) {
			statusType = "8";
		} else if (status == movId) {
			statusType = "10";
		} else if (status == movId_Term_Aux) {
			statusType = "10";
		} else if (status == pendId) {
			statusType = "11";
		} else if (status == pendId_Term_Aux) {
			statusType = "11";
		}
		return statusType;
	}
	
	/** The st id gen. */
	private static IdAssignmentImpl stIdGen =null;
	
	/**
	 * Gets the id generator client.
	 *
	 * @param config the config
	 * @return the id generator client
	 */
	private static IdAssignmentImpl getIdGeneratorClient(Config config){
		if (stIdGen==null ){
			stIdGen = new IdAssignmentImpl(config.getEndPoint(), config.getUsername(), config.getPassword());
		}
		return stIdGen;
	}
	// get the snomedID for the given UUID 
	/**
	 * Gets the snomedid.
	 *
	 * @param config the config
	 * @param uuid the uuid
	 * @param parentSnomedId the parent snomed id
	 * @return the snomedid
	 */
	public static String getSNOMEDID(Config config, UUID uuid, String parentSnomedId) {
		String snomedId = null;
		try {
			IdAssignmentImpl idGen = getIdGeneratorClient( config);
			snomedId = idGen.getSNOMEDID(uuid);

			if(snomedId!=null && !snomedId.equals("") ){
				return snomedId;
			}

			snomedId = idGen.createSNOMEDID(uuid, parentSnomedId);

			logger.info("===SnomedId Created: " + snomedId);
		} catch (Exception cE) {
			logger.error("Message : SnomedId creation error for UUID :" + uuid, cE);
		}
		return snomedId;
	}





	/**
	 * Gets the snomed id.
	 *
	 * @param concept the concept
	 * @return the snomed id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public static String getSnomedId(I_GetConceptData concept) throws IOException, TerminologyException {
		String snomedId = "";
		I_Identify i_Identify = concept.getIdentifier();
		List<? extends I_IdVersion> i_IdentifyList = i_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
				int snomedRTNid = i_IdVersion.getAuthorityNid();

				if ( snomedRTNid == arcAuxSnomedRTNid) {
					snomedId = (String) denotion;
					return snomedId.toString();
				}
			}
		}

		return null;
	}



	/**
	 * Gets the ctv3 id.
	 *
	 * @param concept the concept
	 * @return the ctv3 id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public static String getCtv3Id(I_GetConceptData concept) throws IOException, TerminologyException {
		String ctv3Id = ""; // ConceptId
		I_Identify i_Identify = concept.getIdentifier();
		List<?> i_IdentifyList = i_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				// Actual value for identifier
				int ctv3Nid = i_IdVersion.getAuthorityNid();

				if (ctv3Nid == arcAuxCtv3Nid){
					ctv3Id = (String) denotion;
					return ctv3Id.toString();
				}
			}
		}
		return null;
	}


	// get the Ctv3Id for the given UUID
	/**
	 * Gets the cT v3 id.
	 *
	 * @param config the config
	 * @param uuid the uuid
	 * @return the cT v3 id
	 */
	public static String getCTV3ID(Config config, UUID uuid) {
		String ctv3Id = null;

		try {
			IdAssignmentImpl idGen = getIdGeneratorClient( config);
			ctv3Id = idGen.getCTV3ID(uuid);
			if( ctv3Id !=null && !ctv3Id.equals("")  ){ 
				return ctv3Id;
			}
			ctv3Id = idGen.createCTV3ID(uuid);
			logger.info("===Ctv3Id Created Successfully " + ctv3Id);

		}
		catch (Exception cE) {
			logger.error("Message : Ctv3Id creation error for UUID :" + uuid, cE);
		}
		return ctv3Id;
	}


	/**
	 * Gets the refset id.
	 *
	 * @param typeId the type id
	 * @return the refset id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public static String getRefsetId(String typeId) throws IOException, Exception {
		String refsetId = "99";
		if (typeId.equals("149016008"))
			refsetId = I_Constants.MAY_BE_REFSET_ID;
		else if (typeId.equals("384598002"))
			refsetId = I_Constants.MOVED_FROM_REFSET_ID;
		else if (typeId.equals("370125004"))
			refsetId = I_Constants.MOVED_TO_REFSET_ID;
		else if (typeId.equals("370124000"))
			refsetId = I_Constants.REPLACED_BY_REFSET_ID;
		else if (typeId.equals("168666000"))
			refsetId = I_Constants.SAME_AS_REFSET_ID;
		else if (typeId.equals("159083000"))
			refsetId = I_Constants.WAS_A_REFSET_ID;
		return refsetId;
	}

	/**
	 * Gets the nid.
	 *
	 * @param struuid the struuid
	 * @return the nid
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static int getNid(String struuid) throws TerminologyException, IOException {
		int nid = 0;
		ArrayList<UUID> uuidList = new ArrayList<UUID>();
		UUID uuid = UUID.fromString(struuid); // SNOMED Core Inferred 5e51196f-903e-5dd4-8b3e-658f7e0a4fe6
		uuidList.add(uuid);
		I_GetConceptData findPathCon = getTermFactory().getConcept(uuidList);
		nid = findPathCon.getConceptNid();
		return nid;
	}

	/**
	 * Gets the snomed core path nid.
	 *
	 * @return the snomed core path nid
	 */
	public static int getSnomedCorePathNid() {
		int snomedCorePathNid = 0;
		try {
			snomedCorePathNid = getNid(I_Constants.SNOMED_CORE_PATH_UID);
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return snomedCorePathNid;
	}

	/**
	 * Gets the snomed meta path nid.
	 *
	 * @return the snomed meta path nid
	 */
	public static int getSnomedMetaPathNid() {
		int snomedMetaPathNid = 0;
		try {
			snomedMetaPathNid = getNid(I_Constants.SNOMED_META_PATH_UID);
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return snomedMetaPathNid;
	}

	/**
	 * Gets the snorocket author nid.
	 *
	 * @return the snorocket author nid
	 */
	public static int getSnorocketAuthorNid() {
		int snorocketAuthorNid = 0;
		try {
			snorocketAuthorNid = getNid(I_Constants.SNOROCKET_AUTHOR_UID); // Snorocket Author Nid (Classifier) Inferred Rels
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return snorocketAuthorNid;
	}

	/**
	 * Gets the user author nid.
	 *
	 * @return the user author nid
	 */
	public static int getUserAuthorNid() {
		try {
			userAuthorNid = getNid(I_Constants.USER_AUTHOR_UID); // User Author Nid (Stated Rels)
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return userAuthorNid;
	}

	/**
	 * Gets the snomed inferred path nid.
	 *
	 * @return the snomed inferred path nid
	 */
	public static int getSnomedInferredPathNid() {
		int snomedInferredPathNid = 0;
		try {
			snomedInferredPathNid = getNid(I_Constants.SNOMED_INFERRED_PATH_UID); // SNOMED Core Inferred
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return snomedInferredPathNid;
	}

	/**
	 * Gets the snomed stated path nid.
	 *
	 * @return the snomed stated path nid
	 */
	public static int getSnomedStatedPathNid() {
		int snomedStatedPathNid = 0;
		try {
			snomedStatedPathNid = getNid(I_Constants.SNOMED_STATED_PATH_UID); // SNOMED Stated
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return snomedStatedPathNid;
	}

	/**
	 * Gets the sct id.
	 *
	 * @param nid the nid
	 * @return the sct id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public static String getSctId(int nid) throws IOException, TerminologyException {
		Long sctId = null;
		I_Identify identify = getTermFactory().getId(nid);
		if (identify==null) return null;
		List<? extends I_IdVersion> i_IdentifyList = identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				int snomedIntegerNid = i_IdVersion.getAuthorityNid();
				if (  snomedIntegerNid == arcAuxSnomedIntegerNid) {
					sctId = (Long) denotion;
					return sctId.toString();
				}
			}
		}
		return null;
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
	public static String getRelationshipIdVersion(Object denotion, int snomedAuthorityNid) throws IOException, TerminologyException {
		Long sctId = null;

		if (snomedAuthorityNid == arcAuxSnomedIntegerNid) {
			sctId = (Long) denotion;
		} else {
			// System.out.println("===this is an uuid not relationship id"); //Special handling for uuid
			sctId = 1L;
		}

		if (sctId == null)
			sctId = 0L; // This is temporary fix to avoid null pointer exception...

		return sctId.toString();

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
	 */
	public static String getSCTId(Config config, UUID componentUuid, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId) {

		IdAssignmentImpl idGen = getIdGeneratorClient( config);
		Long sctId = 0L;

		try {
			sctId = idGen.getSCTID(componentUuid);
			if (sctId!=null && !sctId.equals("") ){
				return String.valueOf(sctId);
			}
			sctId = idGen.createSCTID(componentUuid, namespaceId, partitionId, releaseId, executionId, moduleId);
		} catch (Exception cE) {
			logger.error("Message : SCTID creation error for UUID :" + componentUuid, cE);
		}
		return String.valueOf(sctId);
	}


	// get the conceptid for the given UUID (Hardcoded values)
	/**
	 * Gets the sCT id.
	 *
	 * @param config the config
	 * @param uuid the uuid
	 * @return the sCT id
	 */
	public static String getSCTId(Config config, UUID uuid) {
		try{
			String namespaceId = null;
			String partitionId = null;
			String releaseId = null;
			String executionId = null;
			String sctModuleId = null;
			if(!config.getNamespaceId().equals(null))
			{
				namespaceId = config.getNamespaceId();
				partitionId = config.getPartitionId();
				releaseId	= config.getReleaseId();
				executionId = config.getExecutionId();
				sctModuleId = "12345";
				return getSCTId(config,uuid, Integer.parseInt(namespaceId) , partitionId, releaseId, executionId, sctModuleId);
			}	
		} catch (Exception cE) {
			logger.error("Message : SCTID creation error for UUID :" + uuid, cE);
		}
		return null;
	}

	/**
	 * Gets the concept id.
	 *
	 * @param concept the concept
	 * @return the concept id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public static String getConceptId(I_GetConceptData concept) throws IOException, TerminologyException {
		Long conceptId = null; // ConceptId
		I_Identify i_Identify = concept.getIdentifier();
		List<? extends I_IdVersion> i_IdentifyList = i_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				int snomedIntegerNid = i_IdVersion.getAuthorityNid();


				if ( snomedIntegerNid == arcAuxSnomedIntegerNid) {
					try {
						conceptId = (Long) denotion;
						return conceptId.toString();
					} catch (java.lang.ClassCastException e) {
						e.printStackTrace();
						// This is all the subset which gets imported and subsetoriginalid becomes conceptid (not need to extracted)
						System.out.println("ClassCastException ===>" + concept.getInitialText());
					}
				}
			}
		}

		return null;

	}


	// get the conceptid for the given UUID using Specific namespace and partition values
	/**
	 * Gets the concept id.
	 *
	 * @param config the config
	 * @param uuid the uuid
	 * @return the concept id
	 */
	public static String getConceptId(Config config, UUID uuid) {
		IdAssignmentImpl idGen = getIdGeneratorClient( config);
		long conceptId = 0L;

		String partitionId = "";
		String sctModuleId = "Concept Component";

		String namespaceId = "";
		String releaseId = "";
		String executionId = "";

		try {

			conceptId = idGen.getSCTID(uuid);
		} catch (NullPointerException Ne) {
			// There is no conceptid in the repository so we are getting NULL
			if (logger.isDebugEnabled())
				logger.debug("getSCTID for UUID : " + uuid + " returned NULL calling create to generate a new SCTID");

			try{
				if(!config.getReleaseId().equals(null)) {
					namespaceId = config.getNamespaceId();
					releaseId	= config.getReleaseId();
					executionId = config.getExecutionId();
				}
				conceptId = idGen.createSCTID(uuid, Integer.parseInt(namespaceId) , partitionId, releaseId, executionId, sctModuleId);

			} catch (Exception Ex) {
				logger.error("Message : SCTID creation error for UUID :"  + uuid, Ex);
			}

		} catch (Exception e) {
			logger.error("Exception Message : "  + uuid, e);
		}
		return String.valueOf(conceptId);
	}



	// get the descriptionid for the given UUID using Specific namespace and partition values
	/**
	 * Gets the description id.
	 *
	 * @param config the config
	 * @param uuid the uuid
	 * @return the description id
	 */
	public static String getDescriptionId(Config config, UUID uuid) {
		IdAssignmentImpl idGen = getIdGeneratorClient( config);
		long descriptionId = 0L;

		String partitionId = "";
		String sctModuleId = "Description Component";		
		String namespaceId = "";
		String releaseId = "";
		String executionId = "";

		try {
			if(!config.getReleaseId().equals(null)) {
				namespaceId = config.getNamespaceId();
				releaseId	= config.getReleaseId();
				executionId = config.getExecutionId();
			}		
			descriptionId = idGen.getSCTID(uuid);
		} catch (NullPointerException Ne) {
			// There is no descriptionid in the repository so we are getting NULL
			try{
				descriptionId = idGen.createSCTID(uuid, Integer.parseInt(namespaceId) , partitionId, releaseId, executionId, sctModuleId);

			} catch (Exception Ex) {
				logger.error("Message : SCTID creation error for UUID :"  + uuid, Ex);
			}

		} catch (Exception e) {
			logger.error("Exception Message : "  + uuid, e);
		}

		return String.valueOf(descriptionId);
	}





	// get the relationshipId for the given UUID using Specific namespace and partition values
	/**
	 * Gets the relationship id.
	 *
	 * @param config the config
	 * @param uuid the uuid
	 * @return the relationship id
	 */
	public static String getRelationshipId(Config config, UUID uuid) {
		IdAssignmentImpl idGen = getIdGeneratorClient( config);

		long relationshipId = 0L;
		String partitionId = "";
		String sctModuleId = "Relationship Component";
		String namespaceId = "";
		String releaseId = "";
		String executionId = "";

		try {
			if(!config.getReleaseId().equals(null)) {
				namespaceId = config.getNamespaceId();
				releaseId	= config.getReleaseId();
				executionId = config.getExecutionId();
			}		
			relationshipId = idGen.getSCTID(uuid);
		} catch (NullPointerException Ne) {
			// There is no relationshipId in the repository so we are getting NULL
			try{
				relationshipId = idGen.createSCTID(uuid, Integer.parseInt(namespaceId) , partitionId, releaseId, executionId, sctModuleId);
			} catch (Exception Ex) {
				logger.error("Message : SCTID creation error for UUID :"  + uuid, Ex);
			}

		} catch (Exception e) {
			logger.error("Exception Message : "  + uuid, e);
		}

		return String.valueOf(relationshipId);
	}

	/**
	 * Insert sct id.
	 *
	 * @param componentNid the component nid
	 * @param config the config
	 * @param wsSctId the ws sct id
	 * @param pathNid the path nid
	 * @param statusNid the status nid
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static boolean insertSctId(int componentNid , Config config, String wsSctId , int pathNid , int statusNid) throws IOException {
		boolean flag = false;
		try {	
			I_Identify i_Identify = getTermFactory().getId(componentNid);	
			I_GetConceptData commitedConcept = getTermFactory().getConceptForNid(componentNid);
			flag = i_Identify.addLongId(Long.parseLong(wsSctId), ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(),
					statusNid,
					Long.MAX_VALUE,
					aceConfig.getEditCoordinate().getAuthorNid(),
					aceConfig.getEditCoordinate().getModuleNid(),
					pathNid);
			getTermFactory().addUncommitted(commitedConcept);

		} catch (Exception ex) {
			logger.error(ex);
		}
		return flag;
	}

	/**
	 * Gets the partition id.
	 *
	 * @param sctId the sct id
	 * @return the partition id
	 */
	public static String getPartitionId(String sctId) {
		int slength = sctId.length();
		String partId = sctId.substring(slength - 3, slength - 1);
		return partId;
	}

	/**
	 * Gets the snomed description type.
	 *
	 * @param wbDescriptionType the wb description type
	 * @return the snomed description type
	 */
	public static String getSnomedDescriptionType(int wbDescriptionType) {
		String snomedDescType = "99";
		try {
			int fsnId_Term_Aux = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid();
			int symId_Term_Aux = ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid();
			int textDefId_Term_Aux = ArchitectonicAuxiliary.Concept.TEXT_DEFINITION_TYPE.localize().getNid();
			int ptId = ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid();
			int fsnId =SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID(); //Fully specified name	
			int symId = getNid("8bfba944-3965-3946-9bcb-1e80a5da63a2"); //Synonym
			int textDefId = getNid("700546a3-09c7-3fc2-9eb9-53d318659a09"); //Definition

			// Note: for RF2, There will not be any PT only Sym or FSN (text definition will be exported separately)
			if (wbDescriptionType == ptId) // Preferred Term
				snomedDescType = "2";
			else if (wbDescriptionType == symId) // Synonym
				snomedDescType = "2";
			else if (wbDescriptionType == symId_Term_Aux) // Synonym
				snomedDescType = "2";
			else if (wbDescriptionType == fsnId) // FSN
				snomedDescType = "3";
			else if (wbDescriptionType == fsnId_Term_Aux) // FSN
				snomedDescType = "3";
			else if (wbDescriptionType == textDefId) // Text Definition
				snomedDescType = "4";
			else if (wbDescriptionType == textDefId_Term_Aux) // Text Definition
				snomedDescType = "4";

		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Exceptions: " + e.getMessage());
		}
		return snomedDescType;
	}

	/**
	 * Gets the description id.
	 *
	 * @param descriptionNid the description nid
	 * @return the description id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public static String getDescriptionId(int descriptionNid) throws IOException, TerminologyException {

		Long descriptionId = null; 
		I_Identify desc_Identify = getTermFactory().getId(descriptionNid);
		List<? extends I_IdVersion> i_IdentifyList = desc_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				int snomedIntegerNid = i_IdVersion.getAuthorityNid();
				if (snomedIntegerNid == arcAuxSnomedIntegerNid) {
					descriptionId = (Long) denotion;
					return descriptionId.toString();
				}
			}
		}
		return null;

	}

	/**
	 * Gets the characteristic type.
	 *
	 * @param type the type
	 * @return the characteristic type
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public static String getCharacteristicType(int type) throws IOException, TerminologyException {
		String charType = "99";

		int defId =getNid("e607218d-7027-3058-ae5f-0d4ccd148fd0"); //	
		int qualId = getNid("569dac14-a8a5-3cf0-b608-5ae2f1c89461"); //
		int hisId = getNid("700546a3-09c7-3fc2-9eb9-53d318659a09"); //
		int addId = getNid("85aba419-17fe-3033-a7c2-21df0af84176"); //
		int statedId = getNid("3b0dbd3b-2e53-3a30-8576-6c7fa7773060"); //statedId
		int inferredId = getNid("1290e6ba-48d0-31d2-8d62-e133373c63f5"); //inferredId

		int defId_Term_Aux  = ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize().getNid();
		int qualId_Term_Aux  = ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC.localize().getNid();
		int hisId_Term_Aux  = ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC.localize().getNid();
		int addId_Term_Aux = ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC.localize().getNid();
		int statedId_Term_Aux = ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.localize().getNid();

		if (type == inferredId) {
			charType = "0";
		} else if (type == defId) {
			charType = "0";
		} else if (type == defId_Term_Aux) {
			charType = "0";
		} else if (type == qualId) {
			charType = "1";
		} else if (type == qualId_Term_Aux) {
			charType = "1";
		} else if (type == hisId) {
			charType = "2";
		} else if (type == hisId_Term_Aux) {
			charType = "2";
		} else if (type == addId) {
			charType = "3";
		} else if (type == addId_Term_Aux) {
			charType = "3";
		} else if (type == statedId) {
			charType = "4";
		} else if (type == statedId_Term_Aux) {
			charType = "4";
		}
		return charType;
	}


	/**
	 * Gets the type id.
	 *
	 * @param descType the desc type
	 * @return the type id
	 */
	public static String getTypeId(String descType) {
		String typeId = "";

		if (descType.equals("4")) {
			typeId = I_Constants.DEFINITION;
		} else if (descType.equals("3")) {
			typeId = I_Constants.FSN;
		} else {
			typeId = I_Constants.SYN;
		}

		return typeId;
	}


	/**
	 * Gets the characteristic type id.
	 *
	 * @param characteristicType the characteristic type
	 * @return the characteristic type id
	 */
	public static String getCharacteristicTypeId(String characteristicType) {
		String characteristicTypeId = "";

		if (characteristicType.equals("0")) {
			characteristicTypeId = I_Constants.DEFININGRELATION;
		} else if (characteristicType.equals("1")) {
			characteristicTypeId = I_Constants.QUALIFYRELATION;
		} else if (characteristicType.equals("2")) {
			characteristicTypeId = I_Constants.HISTORICAL;
		} else if (characteristicType.equals("3")) {
			characteristicTypeId = I_Constants.ADDITIONALRELATION;
		} else if (characteristicType.equals("4")) {
			characteristicTypeId = I_Constants.STATED;
		}

		return characteristicTypeId;
	}

	/**
	 * Gets the inferred characteristic type id.
	 *
	 * @param characteristicType the characteristic type
	 * @return the inferred characteristic type id
	 */
	public static String getInferredCharacteristicTypeId(String characteristicType) {
		String characteristicTypeId = "99";

		if (characteristicType.equals("0")) {
			characteristicTypeId = I_Constants.INFERRED;
		} else if (characteristicType.equals("3")) {
			characteristicTypeId = I_Constants.ADDITIONALRELATION;
		} 

		return characteristicTypeId;
	}


	/**
	 * Gets the stated characteristic type id.
	 *
	 * @param characteristicType the characteristic type
	 * @return the stated characteristic type id
	 */
	public static String getStatedCharacteristicTypeId(String characteristicType) {
		String characteristicTypeId = "99";

		if (characteristicType.equals("4")) {
			characteristicTypeId = I_Constants.STATED;
		}

		return characteristicTypeId;
	}

	/**
	 * Gets the module sctid for stamp nid.
	 *
	 * @param moduleNid the module nid
	 * @return the module sctid for stamp nid
	 */
	public static String getModuleSCTIDForStampNid(int moduleNid) {
		return allModuleMapNidSCTId.get(moduleNid);
	}

}
