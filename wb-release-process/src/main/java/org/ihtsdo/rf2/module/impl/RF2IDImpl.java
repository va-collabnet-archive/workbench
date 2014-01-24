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

public abstract class RF2IDImpl {

	private static Config config;

	private static Logger logger = Logger.getLogger(RF2IDImpl.class);

	
	public static Config getConfig() {
		return config;
	}

	public static void setConfig(Config config) {
		RF2IDImpl.config = config;
	}

	public String getTimeFormat() {
		return IdUtil.TIMEFORMAT;
	}

	

	public RF2IDImpl(Config config) {

		RF2IDImpl.config = config;

		
	}

	

	public I_ConfigAceFrame getAceConfig() {
		return IdUtil.getAceConfig();
	}

	

	/*
	 * public static String getModuleID(String snomedIntegerId) { return ExportUtil.getModuleID(snomedIntegerId); }
	 */
	public void closeFileWriter(BufferedWriter bw) throws IOException {
		if (bw != null)
			bw.close();
	}

	

	public String getSctId(int nid, int pathNid) throws IOException, TerminologyException {
		return IdUtil.getSctId(nid, pathNid);
	}

	// get the sctid for the given UUID
	public String getSCTId(Config config, UUID uuid) throws Exception {
		return IdUtil.getSCTId(config, uuid);
	}
	
	
	// get the sctid for the given UUID and namespace , partititonId
	public String getSCTId(Config config, UUID componentUuid, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId) throws Exception {
		return IdUtil.getSCTId(config, componentUuid, namespaceId, partitionId, releaseId, executionId, moduleId);
	}
	public HashMap<UUID, Long> getSCTIdList(Config config, List<UUID> componentUuidlist, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId) throws Exception {
		return IdUtil.getSCTIdList(config, componentUuidlist, namespaceId, partitionId, releaseId, executionId, moduleId);
	}
	public String getConceptId(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		return IdUtil.getConceptId(concept, snomedCorePathNid);
	}

	

	public abstract void generateIdentifier() throws IOException;

	
}
