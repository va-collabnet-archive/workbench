package org.ihtsdo.rf2.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.idgeneration.IdAssignmentImpl;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.core.dao.ModuleIDDAO;

public class IdUtil {

	// store the handle for the log files
	private static I_ConfigAceFrame aceConfig;

	// log4j logging
	private static Logger logger = Logger.getLogger(IdUtil.class.getName());

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


	}

	private static HashSet<ModuleIDDAO> metaHierDAO;




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
		I_ConceptAttributeVersioned<?> i_ConceptAttributeVersioned = con.getConAttrs();
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
		if (identify==null) return null;
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
			return null;
		return sctId.toString();
	}


	//Get the sctid for the given UUID
	public static String getSCTId(Config config, UUID componentUuid, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId) {
		final IdAssignmentImpl idGen = new IdAssignmentImpl(config.getEndPoint(), config.getUsername(), config.getPassword());
		long sctId = 0L;

		try {
			sctId = idGen.getSCTID(componentUuid);
		} catch (NullPointerException e) {
			// there is no SCTID so we are getting NULL
			if (logger.isDebugEnabled())
				logger.debug("getSCTID for UUID : " + componentUuid + " returned NULL calling create to generate a new SCTID");
			try {

				if (releaseId!=null && releaseId.length()>8){
					releaseId=releaseId.substring(0,8);
				}
				sctId = idGen.createSCTID(componentUuid, namespaceId, partitionId, releaseId, executionId, moduleId);
			} catch (Exception cE) {
				logger.error("Message : SCTID creation error for UUID :" + componentUuid, cE);
			}
		} catch (Exception e) {
			logger.error("Message : " + componentUuid, e);
		}
		return String.valueOf(sctId);
	}


	// get the conceptid for the given UUID (Hardcoded values)
	public static String getSCTId(Config config, UUID uuid) {
		final IdAssignmentImpl idGen = new IdAssignmentImpl(config.getEndPoint(), config.getUsername(), config.getPassword());
		long sctId = 0L;

		try {
			sctId = idGen.getSCTID(uuid);
		} catch (NullPointerException e) {
			// there is no SCTID so we are getting NULL
			if (logger.isDebugEnabled())
				logger.debug("getSCTID for UUID : " + uuid + " returned NULL calling create to generate a new SCTID");
			try {
				String releaseId=config.getReleaseDate();
				if (releaseId!=null && releaseId.length()>8){
					releaseId=releaseId.substring(0,8);
				}
				sctId = idGen.createSCTID(uuid, 0, "01", releaseId, "TEST EXECUTION", "12345");
			} catch (Exception cE) {
				logger.error("Message : SCTID creation error for UUID :" + uuid, cE);
			}
		} catch (Exception e) {
			logger.error("Message : " + uuid, e);
		}
		return String.valueOf(sctId);
	}

	public static String getConceptId(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		Long conceptId = null; // ConceptId
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

		if (conceptId==null) return null;

		return conceptId.toString();
	}

	public static String getPartitionId(String sctId) {
		int slength = sctId.length();
		String partId = sctId.substring(slength - 3, slength - 1);
		return partId;
	}


	public static String getDescriptionId(int descriptionNid, int snomedCorePathNid) throws IOException, TerminologyException {

		Long descriptionId = null; //If description is new then descriptionid doesn't exist in workbench so use dummy value.
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
		if (descriptionId==null)return null;

		return descriptionId.toString();
	}

	public static HashMap<UUID, Long> getSCTIdList(Config config,
			List<UUID> componentUuidlist, Integer namespaceId,
			String partitionId, String releaseId, String executionId,
			String moduleId2) {

		final IdAssignmentImpl idGen = new IdAssignmentImpl(config.getEndPoint(), config.getUsername(), config.getPassword());
		HashMap<UUID, Long> sctId = new HashMap<UUID,Long>();

		try {

			if (releaseId!=null && releaseId.length()>8){
				releaseId=releaseId.substring(0,8);
			}
			sctId = idGen.createSCTIDList(componentUuidlist, namespaceId, partitionId, releaseId, executionId, moduleId);
		} catch (Exception cE) {
			logger.error("Message : SCTID creation error for list " , cE);
		}
		return sctId;
	}



}
