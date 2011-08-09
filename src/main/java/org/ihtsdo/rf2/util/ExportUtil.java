package org.ihtsdo.rf2.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.core.dao.ModuleIDDAO;
import org.ihtsdo.tk.api.Precedence;

//import org.ihtsdo.tk.api.Precedence;

public class ExportUtil {

	// store the handle for the log files
	private static I_ConfigAceFrame aceConfig;

	// log4j logging
	private static Logger logger = Logger.getLogger(ExportUtil.class.getName());

	private static int snomedCorePathNid;
	private static int snomedMetaPathNid;
	private static int snomedInferredPathNid;
	private static int snomedStatedPathNid;
	private static int snorocketAuthorNid;
	private static int userAuthorNid;

	private static UUID paths[] = null;

	//	private static Set<ConceptDescriptor> editPaths = new HashSet<ConceptDescriptor>();

	private static HashSet<String> metaHier;

	private static Set<I_GetConceptData> metaConceptList;

	public static String TIMEFORMAT = I_Constants.TimeFormat;
	public static SimpleDateFormat DATEFORMAT = new SimpleDateFormat(TIMEFORMAT);

	private static String moduleId = "";

	// protected static Logger logger = Logger.getLogger(ExportUtil.class.getName());


	public static void init() {

		// create ace framework
		//createAceConfig();

		// create the meta hierarchy
		//metaConceptList = initMetaHierarchyIsAList(); // 127;
		inactiveConceptList = getInactiveDecendentList();
		InitializeModuleID();		
	}

	private static HashSet<ModuleIDDAO> metaHierDAO;

	//Temporary solution for module id...
	public static void InitializeModuleID() {
		InputStream is =ExportUtil.class.getResourceAsStream("/fileconfig.properties"); 

		try { 
			// Load properties file for FileNames 
			Properties propsFileName = new Properties();
			propsFileName.load(is); 
			String metaHierarchy = propsFileName.getProperty("metaHierarchy"); 			 
			metaHierDAO = new HashSet<ModuleIDDAO>();
			BufferedReader in = null;
			InputStream isData = ExportUtil.class.getResourceAsStream(metaHierarchy);
			InputStreamReader isR = new InputStreamReader(isData);
			try {
				in = new BufferedReader(isR);
				String str;
				while ((str = in.readLine()) != null) {
					str = str.trim();
					String[] part= str.split("\t");
					//System.out.println(part[0] + " & " + part[1]);
					ModuleIDDAO ModuleIDDAO = new ModuleIDDAO(part[0] , part[1], part[2]);
					metaHierDAO.add(ModuleIDDAO);
				}
			} catch (FileNotFoundException e1) {
				logger.error(e1.getMessage());
			} catch (IOException e1) {
				logger.error(e1.getMessage());
			} catch (Exception e) {
				logger.error(" Exceptions in getHier : " + e.getMessage());
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {		 				
						e.printStackTrace();
					}
			}
			//System.out.println("===Total number of concept with meta module====" + metaHierDAO.size());
		} catch (FileNotFoundException e){ 
			logger.error(e.getMessage()); 
		} catch (IOException e) { 
			logger.error(e.getMessage()); 
		}
	}


	public static String getConceptMetaModuleID(I_GetConceptData snomedConcept , String conEffectiveTime) throws IOException, TerminologyException {
		String snomedIntegerId = getConceptId(snomedConcept, getSnomedCorePathNid());
		moduleId = I_Constants.CORE_MODULE_ID; 
		if (metaHierDAO.isEmpty()) { 
			logger.error("Meta Hierarchy DAO Set is empty"); 
		} else {  
			Iterator iter = metaHierDAO.iterator();
			while (iter.hasNext()) {
				ModuleIDDAO  moduleIdDAO = ( ModuleIDDAO ) iter.next();
				String conceptid = moduleIdDAO.getConceptid();
				String effectivetime = moduleIdDAO.getEffectiveTime();
				String active = moduleIdDAO.getActive();
				if(snomedIntegerId.equals(conceptid) && effectivetime.compareTo(conEffectiveTime)<=0 && active.equals("0")){
					moduleId = I_Constants.CORE_MODULE_ID;
					break;
				}else if(snomedIntegerId.equals(conceptid) && effectivetime.compareTo(conEffectiveTime)<=0 && active.equals("1")){
					moduleId = I_Constants.META_MOULE_ID;					
				}
			} 
		}

		return moduleId; 
	}
	
	private static Set<I_GetConceptData> inactiveConceptList;
	private static boolean inActiveRelationshipState = false;
	public static boolean getInactiveConceptList(I_GetConceptData destinationConcept , String destinationId) throws IOException, TerminologyException {	
		inActiveRelationshipState= false;
		if (inactiveConceptList.isEmpty()) {
			logger.error("No inactive concept in the list"); 
		 } else if (inactiveConceptList.contains(destinationConcept)) {			 
			inActiveRelationshipState = true;
		}
		
		return inActiveRelationshipState;
	}
	
	@SuppressWarnings("unchecked")
	public static Set<I_GetConceptData> getInactiveDecendentList() {
		Set<I_GetConceptData> inactiveConceptSet = new HashSet<I_GetConceptData>();
		try {
			Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
			
			//I_GetConceptData inactiveConcept = getTermFactory().getConcept(UUID.fromString("f267fc6f-7c4d-3a79-9f17-88b82b42709a")); // Special concept --Inactive concept
			//inactiveConceptSet = getDescendants(descendants, inactiveConcept);
			
			I_GetConceptData ambiguousConcept = getTermFactory().getConcept(UUID.fromString("5adbed85-55d8-3304-a404-4bebab660fff")); // Ambiguous concept
			//inactiveConceptSet = getDescendants(descendants, ambiguousConcept); 
			
			I_GetConceptData duplicateConcept = getTermFactory().getConcept(UUID.fromString("a5db42d4-6d94-33b7-92e7-d4a1d0f0d814")); // Special concept --Inactive concept
			//inactiveConceptSet = getDescendants(descendants, duplicateConcept);
			
			I_GetConceptData erroneousConcept = getTermFactory().getConcept(UUID.fromString("d4227098-db7a-331e-8f00-9d1e27626fc5")); // Special concept --Inactive concept
			//inactiveConceptSet = getDescendants(descendants, erroneousConcept);
			
			I_GetConceptData limitedConcept = getTermFactory().getConcept(UUID.fromString("0c7b717a-3e41-372b-be57-621befb9b3ee")); // Special concept --Inactive concept
			//inactiveConceptSet = getDescendants(descendants, limitedConcept);
			
			I_GetConceptData noncurrentConcept = getTermFactory().getConcept(UUID.fromString("e730d11f-e155-3482-a423-9637db3bc1a2")); // Special concept --Inactive concept
			//inactiveConceptSet = getDescendants(descendants, noncurrentConcept);
			
			I_GetConceptData outdatedConcept = getTermFactory().getConcept(UUID.fromString("d8a42cc5-05dd-3fcf-a1f7-62856e38874a")); // Special concept --Inactive concept
			//inactiveConceptSet = getDescendants(descendants, outdatedConcept);
			
			I_GetConceptData reasonnotstatedConcept = getTermFactory().getConcept(UUID.fromString("a0db7e17-c6b2-3acc-811d-8a523274e869")); // Special concept --Inactive concept
			//inactiveConceptSet = getDescendants(descendants, reasonnotstatedConcept);
		
			//inactiveConceptSet.add(inactiveConcept);
			inactiveConceptSet.add(ambiguousConcept);
			inactiveConceptSet.add(duplicateConcept);
			inactiveConceptSet.add(erroneousConcept);
			inactiveConceptSet.add(limitedConcept);
			inactiveConceptSet.add(noncurrentConcept);
			inactiveConceptSet.add(outdatedConcept);
			inactiveConceptSet.add(reasonnotstatedConcept);
			
			//System.out.println("=====================================" + inactiveConceptSet.size());
			
			Iterator iter = inactiveConceptSet.iterator();		
			
			while (iter.hasNext()) {
		    	I_GetConceptData concept = (I_GetConceptData) iter.next();
		    	String conceptId = getConceptId(concept, getSnomedCorePathNid());		    	
		    }
				
		} catch (StackOverflowError e) {
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			logger.error(e1.getMessage());
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		} catch (Exception e) {
			logger.error(" Exceptions in getMetaHierarchyIsAList : " + e.getMessage());
		}
		return inactiveConceptSet;
	}


	public static  Set<I_GetConceptData> getDescendantsLocal(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
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

	public static void createTermFactory(Database db) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

		File vodbDirectory = new File(db.getLocation());

		DatabaseSetupConfig dbSetupConfig = new DatabaseSetupConfig();
		Terms.createFactory(vodbDirectory, I_Constants.readOnly, I_Constants.cacheSize, dbSetupConfig);
	}

	public static I_TermFactory getTermFactory() {
		// since we are using mojo this handles the return of the opened database
		I_TermFactory termFactory = Terms.get();
		return termFactory;
	}

	public static I_ConfigAceFrame getAceConfig() {
		return aceConfig;
	}

	public static void createAceConfig() {
		try {

			aceConfig = getTermFactory().newAceFrameConfig();

			DateFormat df = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss zzz");
			// config.addViewPosition(termFactory.newPosition(termFactory.getPath(new UUID[] { UUID.fromString(test_path_uuid) }),
			// termFactory.convertToThinVersion(df.parse(test_time).getTime())));

			// Added inferred promotion template to catch the context relationships
			aceConfig.addViewPosition(getTermFactory().newPosition(getTermFactory().getPath(new UUID[] { UUID.fromString("cb0f6c0d-ebf3-5d84-9e12-d09a937cbffd") }), Integer.MAX_VALUE));
			aceConfig.addEditingPath(getTermFactory().getPath(new UUID[] { UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2") }));
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(getNid("00791270-77c9-32b6-b34f-d932569bd2bf"));//Fully specified name	
			
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			
			aceConfig.getDescTypes().add(getNid("8bfba944-3965-3946-9bcb-1e80a5da63a2"));
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.TEXT_DEFINITION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(getNid("700546a3-09c7-3fc2-9eb9-53d318659a09"));
			
			aceConfig.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			aceConfig.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.localize().getNid());
			aceConfig.getDestRelTypes().add(Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
			
			aceConfig.getDestRelTypes().add(Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
			//aceConfig.setDefaultStatus(getTermFactory().getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			aceConfig.setDefaultStatus(getTermFactory().getConcept(getNid("d12702ee-c37f-385f-a070-61d56d4d0f1f"))); // Current
		
			getAceConfig().getAllowedStatus().add(getNid("d12702ee-c37f-385f-a070-61d56d4d0f1f")); // Current
			getAceConfig().getAllowedStatus().add(getNid("a5daba09-7feb-37f0-8d6d-c3cadfc7f724")); //Retired
			getAceConfig().getAllowedStatus().add(getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3")); //Concept non-current (foundation metadata concept)
			getAceConfig().getAllowedStatus().add(getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9")); //Pending
			getAceConfig().getAllowedStatus().add(getNid("bcb2ccda-d62a-3fc8-b158-10ad673823b6")); //In-appropriate	900000000000494007
			getAceConfig().getAllowedStatus().add( getNid("0d1278d5-3718-36de-91fd-7c6c8d2d2521")); //Limited	900000000000486000
			getAceConfig().getAllowedStatus().add(getNid("eab9334c-8269-344e-9db6-9189f991566e")); //Outdated	900000000000483008
			getAceConfig().getAllowedStatus().add(getNid("bcb2ccda-d62a-3fc8-b158-10ad673823b6")); //In-appropriate	900000000000494007
			getAceConfig().getAllowedStatus().add(getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c")); //Component Moved elsewhere	900000000000487009
			getAceConfig().getAllowedStatus().add(getNid("dde90dcf-8749-32ff-bdaa-4e5d17e505f2")); //Erroneous component (foundation metadata concept)	900000000000485001
			getAceConfig().getAllowedStatus().add(getNid("8c852b81-6246-34b5-b882-81627aa404e4"));  //Ambiguous component (foundation metadata concept)	900000000000484002
			getAceConfig().getAllowedStatus().add(getNid("16500683-0760-3aa5-8ed7-9cb98562e755"));  //Dups	900000000000482003
			getAceConfig().getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			getAceConfig().getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			getAceConfig().getAllowedStatus().add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
			
			aceConfig.setPrecedence(Precedence.TIME);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}

		// I_ConfigAceDb newDbProfile = tf.newAceDbConfig();
		// newDbProfile.setUsername("username");
		// newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
		// newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
		// newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
		// newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
		// config.setDbConfig(newDbProfile);

	}


	public static String getMetaModuleID(I_GetConceptData snomedConcept) {
		if (metaConceptList.isEmpty()) {
			logger.error("Meta Hierarchy List is empty");
		} else if (metaConceptList.contains(snomedConcept)) {
			moduleId = I_Constants.META_MOULE_ID;
		} else {
			moduleId = I_Constants.CORE_MODULE_ID;
		}
		return moduleId;
	}

	@SuppressWarnings("unchecked")
	public static Set<I_GetConceptData> initMetaHierarchyIsAList() {
		Set<I_GetConceptData> metaConceptSet = null;
		try {
			I_GetConceptData coreMetadataConcept = getTermFactory().getConcept(UUID.fromString("4c6d8b0b-774a-341e-b0e5-1fc2deedb5a5")); // Core metadata concept 27
			I_GetConceptData foundationMetadataConcept = getTermFactory().getConcept(UUID.fromString("f328cdec-6198-36c4-9c55-d7f4f5b30922")); // Foundation metadata concept 99
			I_GetConceptData snomedMetadataConcept = getTermFactory().getConcept(UUID.fromString("a60bd881-9010-3260-9653-0c85716b4391")); // Snomed meta root concept 99
			Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
			descendants = getDescendantsLocal(descendants, coreMetadataConcept); // 27
			descendants = getDescendantsLocal(descendants, foundationMetadataConcept); // 9?
			descendants = getDescendantsLocal(descendants, snomedMetadataConcept); // ?
			metaConceptSet = descendants;
			// metaConceptSet=getDescendants(descendants, snomedMetadataConcept);
			metaConceptSet.add(coreMetadataConcept);
			metaConceptSet.add(foundationMetadataConcept);
			metaConceptSet.add(snomedMetadataConcept);
		} catch (StackOverflowError e) {
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			logger.error(e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			logger.error(e1.getMessage());
			e1.printStackTrace();
		} catch (Exception e) {
			logger.error(" Exceptions in getMetaHierarchyIsAList : " + e.getMessage());
			e.printStackTrace();
		}
		return metaConceptSet;
	}

	public static HashSet<String> getMetaHierarchyList() {
		return metaHier;
	}

	public static HashSet<String> initHierarchyList(String metaHierarchy) {
		metaHier = new HashSet<String>();

		BufferedReader in = null;
		InputStream is = ExportUtil.class.getResourceAsStream(metaHierarchy);
		InputStreamReader isR = new InputStreamReader(is);
		try {
			// InitializeFileName();
			in = new BufferedReader(isR);
			String str;
			while ((str = in.readLine()) != null) {
				str = str.trim();
				metaHier.add(str);
			}
		} catch (FileNotFoundException e1) {
			logger.error(e1.getMessage());
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		} catch (Exception e) {
			logger.error(" Exceptions in getHier : " + e.getMessage());
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return metaHier;
	}

	public static String getRefinabilityStatusType(int status) throws TerminologyException, IOException {
		String statusType = "99";
		int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
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
		int outdatedId = getNid("eab9334c-8269-344e-9db6-9189f991566e"); //Outdated	900000000000483008
		int ambiguousId = getNid("8c852b81-6246-34b5-b882-81627aa404e4"); //Ambiguous component (foundation metadata concept)	900000000000484002
		int errId = getNid("dde90dcf-8749-32ff-bdaa-4e5d17e505f2"); //Erroneous component (foundation metadata concept)	900000000000485001
		int limId = getNid("0d1278d5-3718-36de-91fd-7c6c8d2d2521"); //Limited	900000000000486000
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); //Component Moved elsewhere	900000000000487009
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); //Dups	900000000000492006	
		int inappropriateId = getNid("bcb2ccda-d62a-3fc8-b158-10ad673823b6"); //In-appropriate	900000000000494007
		
	
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
	
	public static String getConceptInactivationStatusType(int status) throws TerminologyException, IOException {
		String statusType = "99";
		int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
		
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
		int curId = getNid("d12702ee-c37f-385f-a070-61d56d4d0f1f"); //Active value	900000000000545005
		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); //Concept non-current (foundation metadata concept)	900000000000495008
		int dupId = getNid("16500683-0760-3aa5-8ed7-9cb98562e755"); //Dups	900000000000482003
		int outdatedId = getNid("eab9334c-8269-344e-9db6-9189f991566e"); //Outdated	900000000000483008
		int ambiguousId = getNid("8c852b81-6246-34b5-b882-81627aa404e4"); //Ambiguous component (foundation metadata concept)	900000000000484002
		int errId = getNid("dde90dcf-8749-32ff-bdaa-4e5d17e505f2"); //Erroneous component (foundation metadata concept)	900000000000485001
		int limId = getNid("0d1278d5-3718-36de-91fd-7c6c8d2d2521"); //Limited	900000000000486000
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); //Component Moved elsewhere	900000000000487009
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); //Pending	900000000000492006	
		int inappropriateId = getNid("bcb2ccda-d62a-3fc8-b158-10ad673823b6"); //In-appropriate	900000000000494007

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
	
	
	

	public static String getConceptInactivationValueId(int status) throws TerminologyException, IOException {
		String valueId = "XXX";
		int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
		int retId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		int curId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();		
		int conRetId_Term_Aux = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();		
		int inappropriateId_Term_Aux = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();
		
		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int ambiguousId_Term_Aux = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();

		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); 			//Concept non-current (foundation metadata concept)	900000000000495008
		int inappropriateId = getNid("bcb2ccda-d62a-3fc8-b158-10ad673823b6"); 	//In-appropriate	900000000000494007
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); 			//Pending move (foundation metadata concept)	900000000000492006	
		int dupId = getNid("16500683-0760-3aa5-8ed7-9cb98562e755"); 			//Dups	900000000000482003
		int outdatedId = getNid("eab9334c-8269-344e-9db6-9189f991566e"); 		//Outdated	900000000000483008
		int ambiguousId = getNid("8c852b81-6246-34b5-b882-81627aa404e4"); 		//Ambiguous component (foundation metadata concept)	900000000000484002
		int errId = getNid("dde90dcf-8749-32ff-bdaa-4e5d17e505f2"); 			//Erroneous component (foundation metadata concept)	900000000000485001
		int limId = getNid("0d1278d5-3718-36de-91fd-7c6c8d2d2521"); 			//Limited	900000000000486000
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); 			//Component Moved elsewhere	900000000000487009
		
		
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
	

	public static String getDescInactivationValueId(int status) throws TerminologyException, IOException {
		String valueId = "XXX";
		int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
		int retId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		int curId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		int inappropriateId_Term_Aux = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();
		int conRetId_Term_Aux = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();

		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();

		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); 			//Concept non-current (foundation metadata concept)	900000000000495008
		int inappropriateId = getNid("bcb2ccda-d62a-3fc8-b158-10ad673823b6"); 	//In-appropriate	900000000000494007
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); 			//Pending move (foundation metadata concept)	900000000000492006	
		int dupId = getNid("16500683-0760-3aa5-8ed7-9cb98562e755"); 			//Dups	900000000000482003
		int outdatedId = getNid("eab9334c-8269-344e-9db6-9189f991566e"); 		//Outdated	900000000000483008
		int errId = getNid("dde90dcf-8749-32ff-bdaa-4e5d17e505f2"); 			//Erroneous component (foundation metadata concept)	900000000000485001
		int limId = getNid("0d1278d5-3718-36de-91fd-7c6c8d2d2521"); 			//Limited	900000000000486000
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

	public static String getDescInactivationStatusType(int status) throws TerminologyException, IOException {
		String statusType = "50";
		int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
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

	public static int activeId; 
	public static int inactId; 
	public static int conRetId;
	public static int retId ;
	public static int dupId ;
	public static int curId ;
	public static int outdatedId ;
	public static int ambiguousId;
	public static int errId ;
	public static int limId;
	public static int movId ;
	public static int pendId ;
	public static int inappropriateId ;
	static{
		try {
			activeId = getTermFactory().uuidToNative(UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));
			inactId =  getTermFactory().uuidToNative(UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"));
			conRetId = getTermFactory().uuidToNative(UUID.fromString("6cc3df26-661e-33cd-a93d-1c9e797c90e3"));
			retId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
			dupId = getTermFactory().uuidToNative(UUID.fromString("16500683-0760-3aa5-8ed7-9cb98562e755"));
			curId =ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
			outdatedId = getTermFactory().uuidToNative(UUID.fromString("eab9334c-8269-344e-9db6-9189f991566e"));
			ambiguousId =getTermFactory().uuidToNative(UUID.fromString("8c852b81-6246-34b5-b882-81627aa404e4"));
			errId = getTermFactory().uuidToNative(UUID.fromString("dde90dcf-8749-32ff-bdaa-4e5d17e505f2"));
			limId = getTermFactory().uuidToNative(UUID.fromString("0d1278d5-3718-36de-91fd-7c6c8d2d2521"));
			movId = getTermFactory().uuidToNative(UUID.fromString("95028943-b11c-3509-b1c0-c4ae16aaad5c"));
			pendId = getTermFactory().uuidToNative(UUID.fromString("9906317a-f50f-30f6-8b59-a751ae1cdeb9"));
			inappropriateId = getTermFactory().uuidToNative(UUID.fromString("bcb2ccda-d62a-3fc8-b158-10ad673823b6"));
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getStatusType(int status) throws TerminologyException, IOException {
		String statusType = "99";		
		int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
			
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
		int curId = getNid("d12702ee-c37f-385f-a070-61d56d4d0f1f"); //Active value	900000000000545005
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); //Pending	900000000000492006	
		int inappropriateId = getNid("bcb2ccda-d62a-3fc8-b158-10ad673823b6"); //In-appropriate	900000000000494007
		int limId = getNid("0d1278d5-3718-36de-91fd-7c6c8d2d2521"); //Limited	900000000000486000
		int outdatedId = getNid("eab9334c-8269-344e-9db6-9189f991566e"); //Outdated	900000000000483008
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); //Component Moved elsewhere	900000000000487009
		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); //Concept non-current (foundation metadata concept)	900000000000495008
		int errId = getNid("dde90dcf-8749-32ff-bdaa-4e5d17e505f2"); //Erroneous component (foundation metadata concept)	900000000000485001
		int ambiguousId = getNid("8c852b81-6246-34b5-b882-81627aa404e4"); //Ambiguous component (foundation metadata concept)	900000000000484002
		int dupId = getNid("16500683-0760-3aa5-8ed7-9cb98562e755"); //Dups	900000000000482003
		
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

	public static String getSnomedId(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		String snomedId = "";
		I_Identify i_Identify = concept.getIdentifier();
		List<? extends I_IdVersion> i_IdentifyList = i_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
				int snomedRTNid = i_IdVersion.getAuthorityNid();
				int arcAuxSnomedRTNid = ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.localize().getNid();
				int pathNid = i_IdVersion.getPathNid();
				if (pathNid == snomedCorePathNid && snomedRTNid == arcAuxSnomedRTNid) {
					snomedId = (String) denotion;
				}
			}
		}

		return snomedId.toString();
	}

	public static String getCtv3Id(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		String ctv3Id = ""; // ConceptId
		I_Identify i_Identify = concept.getIdentifier();
		List<?> i_IdentifyList = i_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				// Actual value for identifier
				int ctv3Nid = i_IdVersion.getAuthorityNid();
				int arcAuxCtv3Nid = ArchitectonicAuxiliary.Concept.CTV3_ID.localize().getNid();
				int pathNid = i_IdVersion.getPathNid();
				if (pathNid == snomedCorePathNid && ctv3Nid == arcAuxCtv3Nid) {
					ctv3Id = (String) denotion;
					// logger.error("=======ctv3id value=======" + ctv3Id);
				}
			}
		}
		return ctv3Id.toString();
	}

	public static boolean IsConceptInActive(I_GetConceptData concept, String effectiveTimeRelStr) throws ParseException, TerminologyException, IOException {
		String conceptStatus = "";
		boolean isInActive = false;
		// set time when Limited concept is no longer current/active
		I_TermFactory tf = Terms.get();
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		List<? extends I_ConceptAttributeTuple> attributes = concept.getConceptAttributeTuples(null, config.getViewPositionSetReadOnly(), 
				Precedence.PATH, config.getConflictResolutionStrategy());
		if (attributes != null && !attributes.isEmpty()) {
			I_ConceptAttributeTuple attribute = attributes.iterator().next();
			conceptStatus = getStatusType(attribute.getStatusNid());
			Date et = new Date(attribute.getTime());
			if (conceptStatus.equals("0")) {
				isInActive = false;
			} else if (effectiveTimeRelStr.compareTo(I_Constants.limited_policy_change)<0 && conceptStatus.equals("6")) {
				isInActive = false;
			} else {
				isInActive = true;
			}
		}
		return isInActive;
	}
	public static boolean IsConceptInActiveNew(I_GetConceptData concept, String effectiveTimeRelStr) throws ParseException, TerminologyException, IOException {
		String conceptStatus = "";
		Date effectiveTimeRelDt = ExportUtil.DATEFORMAT.parse(effectiveTimeRelStr);
		boolean isInActive = false;
		Date LIMITED = ExportUtil.DATEFORMAT.parse(I_Constants.limited_policy_change); // 20100131T000100Z
		// set time when Limited concept is no longer current/active
		I_ConceptAttributeVersioned<?> i_ConceptAttributeVersioned = concept.getConceptAttributes();
		List<? extends I_ConceptAttributeTuple> conceptAttributeTupleList = i_ConceptAttributeVersioned.getTuples();
		String conceptPriorStatus="";
		if (conceptAttributeTupleList.size() > 0 && conceptAttributeTupleList != null) {
			Date effectiveTimeConDt = null;
			for (int i = 0; i < conceptAttributeTupleList.size(); i++) {
				I_ConceptAttributeTuple<?> i_ConceptAttributeTuple = (I_ConceptAttributeTuple<?>) conceptAttributeTupleList.get(i);
				conceptStatus = getStatusType(i_ConceptAttributeTuple.getConceptStatus());
				Date et = new Date(ExportUtil.getTermFactory().convertToThickVersion(i_ConceptAttributeTuple.getVersion()));
				String effectiveTimeConStr = ExportUtil.DATEFORMAT.format(et);
				effectiveTimeConDt = ExportUtil.DATEFORMAT.parse(effectiveTimeConStr);
				//System.out.println(" iteration et  " + et + " & LIMITED " + LIMITED + "effectiveTimeConStr=" + effectiveTimeConStr + "I_Constants.limited_policy_change =" +  I_Constants.limited_policy_change);
				
				if (effectiveTimeConDt.before(effectiveTimeRelDt) || effectiveTimeConDt.equals(effectiveTimeRelDt)) {
					if (conceptStatus.equals("0")) {
						isInActive = false;
					}else if (et.before(LIMITED) && conceptStatus.equals("6")) {
						isInActive = false;
					} else {
						isInActive = true;
					}
					conceptPriorStatus = conceptStatus;
				}
			}			
			
			if (conceptStatus.equals("6")  && effectiveTimeConDt.before(effectiveTimeRelDt) 
				&& (effectiveTimeRelDt.equals(LIMITED) || effectiveTimeRelDt.after(LIMITED))
				&&  effectiveTimeConDt != null) {
				isInActive = true;
			} else if (conceptPriorStatus.equals("6")  
				&& (effectiveTimeRelDt.equals(LIMITED) || effectiveTimeRelDt.after(LIMITED))
				&&  effectiveTimeConDt != null && !conceptPriorStatus.equals("")) {
				isInActive = true;
			}
		}
		return isInActive;
	}
	

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

	public static int getNid(String struuid) throws TerminologyException, IOException {
		int nid = 0;
		ArrayList<UUID> uuidList = new ArrayList<UUID>();
		UUID uuid = UUID.fromString(struuid); // SNOMED Core Inferred 5e51196f-903e-5dd4-8b3e-658f7e0a4fe6
		uuidList.add(uuid);
		I_GetConceptData findPathCon = getTermFactory().getConcept(uuidList);
		nid = findPathCon.getConceptNid();
		return nid;
	}

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

	public static boolean isOnPath(int onPath, int nid) throws IOException, TerminologyException {
		boolean snomedPathFlag = false;
		int pathId = 0;
		I_GetConceptData con = getTermFactory().getConcept(nid);
		I_ConceptAttributeVersioned<?> i_ConceptAttributeVersioned = con.getConceptAttributes();
		List<? extends I_ConceptAttributeTuple> conceptAttributeTupleList = i_ConceptAttributeVersioned.getTuples();
		for (int i = 0; i < conceptAttributeTupleList.size(); i++) {
			I_ConceptAttributeTuple<?> i_ConceptAttributeTuple = (I_ConceptAttributeTuple<?>) conceptAttributeTupleList.get(i);
			pathId = i_ConceptAttributeTuple.getPathNid();
			if (pathId == onPath) {
				snomedPathFlag = true;
			}
		}
		return snomedPathFlag;
	}

	public static String getSctId(int nid, int pathNid) throws IOException, TerminologyException {
		Long sctId = null;
		I_Identify identify = getTermFactory().getId(nid);
		List<? extends I_IdVersion> i_IdentifyList = identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				int snomedIntegerNid = i_IdVersion.getAuthorityNid();
				int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
				int sctNid = i_IdVersion.getPathNid();
				if (sctNid == pathNid && snomedIntegerNid == arcAuxSnomedIntegerNid) {
					sctId = (Long) denotion;
				} else if (sctNid == getSnomedInferredPathNid() && snomedIntegerNid == arcAuxSnomedIntegerNid) { // -2147480867
					sctId = (Long) denotion; // Inferred Path
				} else if (sctNid == getSnomedStatedPathNid() && snomedIntegerNid == arcAuxSnomedIntegerNid) { // -2147480865
					sctId = (Long) denotion; // Stated Path
				}
			}
		}
		if (sctId == null)
			sctId = 0L; // This is temporary fix so needs to remove later on
		return sctId.toString();
	}

	public static String getRelationshipIdVersion(Object denotion, int snomedAuthorityNid) throws IOException, TerminologyException {
		Long sctId = null;

		int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();

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

	// get the description id for the given UUID
	public static String getSCTId(Config config, UUID uuid) {

//		final IdAssignmentImpl idGen = null; // new IdAssignmentImpl(config.getEndPoint(), config.getUsername(), config.getPassword());
//		long descId = 0L;
//
//		try {
//			descId = idGen.getSCTID(uuid);
//		} catch (NullPointerException e) {
//			// there is no SCTID so we are getting NULL
//			if (logger.isDebugEnabled())
//				logger.debug("getSCTID for UUID : " + uuid + " returned NULL calling create to generate a new SCTID");
//
//			try {
//				descId = idGen.createSCTID(uuid, 0, "01", "20110131", "TEST EXECUTION", "12345");
//			} catch (Exception cE) {
//				logger.error("Message : SCTID creation error for UUID :" + uuid, cE);
//			}
//		} catch (Exception e) {
//			logger.error("Message : " + uuid, e);
//		}
//		return String.valueOf(descId);
		return "0";
	}

	public static String getConceptId(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		Long conceptId = 0L; // ConceptId
		I_Identify i_Identify = concept.getIdentifier();
		List<? extends I_IdVersion> i_IdentifyList = i_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				int snomedIntegerNid = i_IdVersion.getAuthorityNid();
				int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
				int pathNid = i_IdVersion.getPathNid();
				if (pathNid == snomedCorePathNid && snomedIntegerNid == arcAuxSnomedIntegerNid) {
					try {
						conceptId = (Long) denotion;
					} catch (java.lang.ClassCastException e) {
						// e.printStackTrace();
						// This is all the subset which gets imported and subsetoriginalid becomes conceptid (not need to extracted)
						// System.out.println("ClassCastException ===>" + concept.getInitialText());
					}
				}
			}
		}
		
	   /*	
	   if(conceptId.toString().equals("0")){
			System.out.println("==conceptId==" + conceptId.toString());
		}*/
		
		return conceptId.toString();
	}

	public static String getPartitionId(String sctId) {
		int slength = sctId.length();
		String partId = sctId.substring(slength - 3, slength - 1);
		return partId;
	}

	public static String getSnomedDescriptionType(int wbDescriptionType) {
		String snomedDescType = "99";
		try {
			int fsnId_Term_Aux = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid();
			int symId_Term_Aux = ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid();
			int textDefId_Term_Aux = ArchitectonicAuxiliary.Concept.TEXT_DEFINITION_TYPE.localize().getNid();
			int ptId = ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid();
			int fsnId =getNid("00791270-77c9-32b6-b34f-d932569bd2bf"); //Fully specified name	
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

	public static String getDescriptionId(int descriptionNid, int snomedCorePathNid) throws IOException, TerminologyException {

		Long descriptionId = 0L; //If description is new then descriptionid doesn't exist in workbench so use dummy value.
		I_Identify desc_Identify = getTermFactory().getId(descriptionNid);
		List<? extends I_IdVersion> i_IdentifyList = desc_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				int snomedIntegerNid = i_IdVersion.getAuthorityNid();
				int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
				int pathNid = i_IdVersion.getPathNid();
				if (pathNid == snomedCorePathNid && snomedIntegerNid == arcAuxSnomedIntegerNid) {
					descriptionId = (Long) denotion;
				}
			}
		}
		/*if(descriptionId.toString().equals("0")){
			System.out.println("==descriptionId==" + descriptionId.toString());
		}*/
		return descriptionId.toString();
	}

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
	

	public static String getSNOMEDrelationshipType(int type) throws TerminologyException, IOException {
		// Real RF2 data will no longer have PT just Synonym (needs modification)
		String relType = "99";
		int isA_Term_Aux = ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid();
		int movedElsewhere_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();
		int isSameAs_Term_Aux = ArchitectonicAuxiliary.Concept.IS_SAME_AS_REL.localize().getNid();
		int dup_Term_Aux = ArchitectonicAuxiliary.Concept.DUP_REL_TYPE.localize().getNid();
		int isADup_Term_Aux = ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.localize().getNid();
		
		List<? extends I_DescriptionTuple> descVersions = getTermFactory().getConcept(type).getDescriptionTuples();
		for (I_DescriptionTuple<?> descTuple : descVersions) {
			I_DescriptionPart<?> descPart = descTuple.getMutablePart();
			UUID uuidDesc = getTermFactory().getUids(descTuple.getDescVersioned().getDescId()).iterator().next();
			String descriptionText = descTuple.getText();
			int descriptionNid = descPart.getTypeNid();
			int prefferedNid = ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid();
			if (descriptionNid == prefferedNid) {
				relType = descriptionText;
			}
		}
		return relType;
	}

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

	/*public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			
			 * Set sPositions = getAceConfig().getViewPositions(); Loop and Print sPositions.get(i); if SNOMED_INFERRED path is included in sPositions verify timestamp of that position. Make sure its
			 * before Change in Admin Procedure if SNOMED_INFERRED not in ANY of sPosition sPositions.add(SNOMED_INFERRED, BEGINNING_OF_TIME) getAceConfig().setViewPositions(sPosition) Loop and Print
			 * sPositions.get(i);
			 

			// getAceConfig() = getTermFactory().newAceFrameConfig();
			// PositionSetReadOnly o = getAceConfig().getViewPositionSetReadOnly();
			
			 * Set<I_Position> fullSet = getAceConfig().getViewPositionSet(); if (fullSet.size() > 0){ Iterator<I_Position> itr = fullSet.iterator(); while (itr.hasNext()) { //logger.error("111"); //
			 * logger.error(((I_Position)itr.next()).toString()); } }
			 

			// Set<I_Position> toAdd = new HashSet<I_Position>();
			// I_Position p = new Position(Integer.MAX_VALUE, )

			// getAceConfig().addViewPosition(getTermFactory().newPosition(getTermFactory().getPath(new
			// UUID[] {UUID.fromString(I_Constants.SNOMED_CORE_PATH_UID)}),Integer.MAX_VALUE));
			getAceConfig().addViewPosition(getTermFactory().newPosition(getTermFactory().getPath(new UUID[] { UUID.fromString(I_Constants.SNOMED_INFERRED_PATH_UID) }), Integer.MAX_VALUE));
			PositionSetReadOnly o = getAceConfig().getViewPositionSetReadOnly();

			
			 * getAceConfig().addViewPosition(getTermFactory().newPosition( getTermFactory().getPath(new UUID[] {UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}), Integer.MAX_VALUE));
			 * getAceConfig().addViewPosition(getTermFactory().newPosition( getTermFactory().getPath(new UUID[] {UUID.fromString(I_Constants.SNOMED_CORE_PATH_UID)}), Integer.MAX_VALUE));
			 * SNOMED_INFERRED_PATH_UID getAceConfig().addViewPosition(getTermFactory().newPosition( getTermFactory().getPath(new UUID[] {UUID.fromString(I_Constants.SNOMED_STATED_PATH_UID)}),
			 * Integer.MAX_VALUE));
			 
			// getAceConfig().addEditingPath(getTermFactory().getPath(new UUID[] {UUID.fromString(I_Constants.SNOMED_CORE_PATH_UID)}));
			getAceConfig().getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			getAceConfig().getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			getAceConfig().getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			getAceConfig().setDefaultStatus(getTermFactory().getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			getAceConfig().getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			getAceConfig().getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			getAceConfig().getAllowedStatus().add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());

			// getAceConfig().setPrecedence(PRECEDENCE.TIME);

			// Precedence precedence = null;
			// getAceConfig().setPrecedence(precedence);
			
			 * Assign Configuration ConceptDescriptor editPathCore =new ConceptDescriptor(I_Constants.SNOMED_CORE_PATH_UID, "SNOMED Core"); ConceptDescriptor editPathInferred =new
			 * ConceptDescriptor(I_Constants.SNOMED_INFERRED_PATH_UID, "SNOMED Core Inferred"); ConceptDescriptor editPathStated =new ConceptDescriptor(I_Constants.SNOMED_STATED_PATH_UID,
			 * "SNOMED Core Stated"); editPaths.add(editPathCore); editPaths.add(editPathInferred); editPaths.add(editPathStated); for (ConceptDescriptor pathConcept : editPaths) { if
			 * (pathConcept.getUuid() == null) { pathConcept.setUuid(Type5UuidFactory .get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathConcept.getDescription()).toString()); }
			 * //logger.error(editPaths.size()); //logger.error(pathConcept .getVerifiedConcept().getInitialText()); getAceConfig().addEditingPath(getTermFactory()
			 * .getPath(pathConcept.getVerifiedConcept().getUids())); }
			 * 
			 * paths = new UUID[2]; paths[0] = (UUID)ArchitectonicAuxiliary.Concept .SNOMED_CORE.getUids().iterator().next(); paths[1] = (UUID)ArchitectonicAuxiliary
			 * .Concept.ARCHITECTONIC_BRANCH.getUids().iterator().next(); //paths[1] = UUID.fromString(I_Constants.SNOMED_INFERRED_PATH_UID);
			 * 
			 * for (int i = 0; i < paths.length; i++) { getAceConfig().addViewPosition(getTermFactory() .newPosition(getTermFactory().getPath(paths[i]), Integer.MAX_VALUE)); }
			 * 
			 * Edit Path is FIRST UUID in paths[] getAceConfig().addEditingPath (getTermFactory().getPath(paths[1]));
			 * 
			 * allowedDestRelTypes.add(getTermFactory().uuidToNative( ArchitectonicAuxiliary.Concept.IS_A_REL.getUids())); allowedDestRelTypes .add(getTermFactory().uuidToNative(ArchitectonicAuxiliary
			 * .Concept.IS_A_DUP_REL.getUids()));
			 

			I_IntSet allowedDestRelTypes = getTermFactory().newIntSet();
			Collection<UUID> uids = new ArrayList<UUID>();
			UUID uuid = UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25");
			uids.add(uuid);
			allowedDestRelTypes.add(getTermFactory().uuidToNative(uids));

			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			Set<? extends I_GetConceptData> testObj = concept.getDestRelOrigins(getAceConfig().getAllowedStatus(), allowedDestRelTypes, getAceConfig().getViewPositionSetReadOnly(), getAceConfig()
					.getPrecedence(), getAceConfig().getConflictResolutionStrategy());

			childrenSet.addAll(testObj);
			descendants.addAll(childrenSet);
			for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendants(descendants, loopConcept);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return descendants;

	}*/
	
	public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			//getAceConfig().addViewPosition(getTermFactory().newPosition(getTermFactory().getPath(new UUID[] { UUID.fromString(I_Constants.SNOMED_CORE_PATH_UID) }), Integer.MAX_VALUE));
			//getAceConfig().addViewPosition(getTermFactory().newPosition(getTermFactory().getPath(new UUID[] { UUID.fromString(I_Constants.SNOMED_INFERRED_PATH_UID) }), Integer.MAX_VALUE));
			//PositionSetReadOnly o = aceConfig.getViewPositionSetReadOnly();
			aceConfig.addViewPosition(getTermFactory().newPosition(getTermFactory().getPath(new UUID[] { UUID.fromString("2b2035dd-9419-56e2-a472-636e8545742c") }), Integer.MAX_VALUE));
			aceConfig.addEditingPath(getTermFactory().getPath(new UUID[] {UUID.fromString("2b2035dd-9419-56e2-a472-636e8545742c")}));
		
			//getAceConfig().addEditingPath(getTermFactory().getPath(new UUID[] {UUID.fromString(I_Constants.SNOMED_CORE_PATH_UID)}));
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(getNid("00791270-77c9-32b6-b34f-d932569bd2bf"));//Fully specified name	
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(getNid("8bfba944-3965-3946-9bcb-1e80a5da63a2"));
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.TEXT_DEFINITION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(getNid("700546a3-09c7-3fc2-9eb9-53d318659a09"));
			
			aceConfig.setDefaultStatus(getTermFactory().getConcept(getNid("d12702ee-c37f-385f-a070-61d56d4d0f1f"))); // Current
			aceConfig.getAllowedStatus().add(getNid("d12702ee-c37f-385f-a070-61d56d4d0f1f")); // Current
			aceConfig.getAllowedStatus().add(getNid("a5daba09-7feb-37f0-8d6d-c3cadfc7f724")); //Retired
			aceConfig.getAllowedStatus().add(getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3")); //Concept non-current (foundation metadata concept)
			aceConfig.getAllowedStatus().add(getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9")); //Pending
			aceConfig.getAllowedStatus().add(getNid("bcb2ccda-d62a-3fc8-b158-10ad673823b6")); //In-appropriate	900000000000494007
			aceConfig.getAllowedStatus().add( getNid("0d1278d5-3718-36de-91fd-7c6c8d2d2521")); //Limited	900000000000486000
			aceConfig.getAllowedStatus().add(getNid("eab9334c-8269-344e-9db6-9189f991566e")); //Outdated	900000000000483008
			aceConfig.getAllowedStatus().add(getNid("bcb2ccda-d62a-3fc8-b158-10ad673823b6")); //In-appropriate	900000000000494007
			aceConfig.getAllowedStatus().add(getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c")); //Component Moved elsewhere	900000000000487009
			aceConfig.getAllowedStatus().add(getNid("dde90dcf-8749-32ff-bdaa-4e5d17e505f2")); //Erroneous component (foundation metadata concept)	900000000000485001
			aceConfig.getAllowedStatus().add(getNid("8c852b81-6246-34b5-b882-81627aa404e4"));  //Ambiguous component (foundation metadata concept)	900000000000484002
			aceConfig.getAllowedStatus().add(getNid("16500683-0760-3aa5-8ed7-9cb98562e755"));  //Dups	900000000000482003
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());

			aceConfig.setPrecedence(Precedence.TIME);
		
			I_IntSet allowedDestRelTypes = getTermFactory().newIntSet();
			Collection<UUID> uids = new ArrayList<UUID>();
			UUID uuid = UUID.fromString(I_Constants.IS_A_UID);
			uids.add(uuid);
			//uids.add(UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));
			allowedDestRelTypes.add(getTermFactory().uuidToNative(uids));	
		
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			
			Set<? extends I_GetConceptData> testObj = concept.getDestRelOrigins(aceConfig.getAllowedStatus(), aceConfig.getDestRelTypes(), aceConfig.getViewPositionSetReadOnly(), aceConfig
				.getPrecedence(), aceConfig.getConflictResolutionStrategy());
			childrenSet.addAll(testObj);
			descendants.addAll(childrenSet);
			
			/*for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendants(descendants, loopConcept);
			}*/
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return descendants;

	}


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

	public static String getInferredCharacteristicTypeId(String characteristicType) {
		String characteristicTypeId = "99";

		if (characteristicType.equals("0")) {
			characteristicTypeId = I_Constants.INFERRED;
		} else if (characteristicType.equals("3")) {
			characteristicTypeId = I_Constants.ADDITIONALRELATION;
		} 

		return characteristicTypeId;
	}
		
	
	public static String getStatedCharacteristicTypeId(String characteristicType) {

		String characteristicTypeId = "99";
			
		if (characteristicType.equals("4")) {
			characteristicTypeId = I_Constants.STATED;
		}

		return characteristicTypeId;
	}

}
