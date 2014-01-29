package org.ihtsdo.rf2.module.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.IdUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class RF2IDImpl.
 */
public abstract class RF2IDImpl {

	/** The config. */
	private static Config config;

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2IDImpl.class);

	
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
		RF2IDImpl.config = config;
	}

	/**
	 * Gets the time format.
	 *
	 * @return the time format
	 */
	public String getTimeFormat() {
		return IdUtil.TIMEFORMAT;
	}

	

	/**
	 * Instantiates a new r f2 id impl.
	 *
	 * @param config the config
	 */
	public RF2IDImpl(Config config) {

		RF2IDImpl.config = config;

		
	}

	

	/**
	 * Gets the ace config.
	 *
	 * @return the ace config
	 */
	public I_ConfigAceFrame getAceConfig() {
		return IdUtil.getAceConfig();
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
	 * Gets the sct id.
	 *
	 * @param nid the nid
	 * @param pathNid the path nid
	 * @return the sct id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public String getSctId(int nid, int pathNid) throws IOException, TerminologyException {
		return IdUtil.getSctId(nid, pathNid);
	}

	// get the sctid for the given UUID
	/**
	 * Gets the sCT id.
	 *
	 * @param config the config
	 * @param uuid the uuid
	 * @return the sCT id
	 * @throws Exception the exception
	 */
	public String getSCTId(Config config, UUID uuid) throws Exception {
		return IdUtil.getSCTId(config, uuid);
	}
	
	
	// get the sctid for the given UUID and namespace , partititonId
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
	 * @throws Exception the exception
	 */
	public String getSCTId(Config config, UUID componentUuid, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId) throws Exception {
		return IdUtil.getSCTId(config, componentUuid, namespaceId, partitionId, releaseId, executionId, moduleId);
	}
	
	/**
	 * Gets the sCT id list.
	 *
	 * @param config the config
	 * @param componentUuidlist the component uuidlist
	 * @param namespaceId the namespace id
	 * @param partitionId the partition id
	 * @param releaseId the release id
	 * @param executionId the execution id
	 * @param moduleId the module id
	 * @return the sCT id list
	 * @throws Exception the exception
	 */
	public HashMap<UUID, Long> getSCTIdList(Config config, List<UUID> componentUuidlist, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId) throws Exception {
		return IdUtil.getSCTIdList(config, componentUuidlist, namespaceId, partitionId, releaseId, executionId, moduleId);
	}
	
	/**
	 * Gets the concept id.
	 *
	 * @param concept the concept
	 * @param snomedCorePathNid the snomed core path nid
	 * @return the concept id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public String getConceptId(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		return IdUtil.getConceptId(concept, snomedCorePathNid);
	}

	

	/**
	 * Generate identifier.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public abstract void generateIdentifier() throws IOException;

	
}
