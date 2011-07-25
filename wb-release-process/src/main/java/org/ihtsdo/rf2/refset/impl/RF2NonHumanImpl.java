package org.ihtsdo.rf2.refset.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

/**
 * Title: RF2DescriptionImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Description File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2NonHumanImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2NonHumanImpl.class);

	public RF2NonHumanImpl(Config config) {
		super(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api. I_GetConceptData)
	 */
	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	@Override
	public void export(I_GetConceptData concept, String conceptid) throws IOException {
		String effectiveTime = "";
		String active = "";
		I_ExtendByRefPartCid<?> extensionPart;
		UUID refsetuuid = null;
		int extensionStatusId = 0;
		try {
			String refsetId = I_Constants.NON_HUMAN_REFSET_ID;
			String moduleId = I_Constants.CORE_MODULE_ID;
			int refsetTermAuxId = getNid(I_Constants.NON_HUMAN_REFSET_UID_TERM_AUX);		
			List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(concept.getNid(), true);
			if (!extensions.isEmpty()) {
				for (I_ExtendByRef extension : extensions) {
					if (extension.getRefsetId() == refsetTermAuxId) {
						if (extension != null) {
							long lastVersion = Long.MIN_VALUE;
							extensionPart=null;
							for (I_ExtendByRefVersion loopTuple : extension.getTuples(allStatusSet,currenAceConfig.getViewPositionSetReadOnly(),
									Precedence.PATH,currenAceConfig.getConflictResolutionStrategy())) {

								if (loopTuple.getTime() >= lastVersion) {
									lastVersion = loopTuple.getTime();
									extensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
								}
							}
							if (extensionPart == null) {
								if (logger.isDebugEnabled()) {
									logger.debug("Refset extension part not found!");
								}
							}else{							

								extensionStatusId = extensionPart.getStatusNid();
								if (extensionStatusId == activeNid) { 														
									active = "1";
								} else if (extensionStatusId == inactiveNid) { 														
									active = "0";								
								} else {
									System.out.println("unknown extensionStatusId =====>" + extensionStatusId);
									logger.error("unknown extensionStatusId =====>" + extensionStatusId);
									System.exit(0);
								}
								
								if (conceptid==null || conceptid.equals("")){
									conceptid=concept.getUids().iterator().next().toString();
								}
								refsetuuid = Type5UuidFactory.get(refsetId + conceptid);
								Date effectiveDate = new Date(extensionPart.getTime());
								effectiveTime = getDateFormat().format(effectiveDate);
								writeRF2TypeLine(refsetuuid, effectiveTime, active, moduleId, refsetId, conceptid);
							}							
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exceptions in exportDescription: " + e.getMessage());
			logger.error(conceptid);
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void writeRF2TypeLine(UUID refsetuuid, String effectiveTime, String active, String moduleId, String refsetId, String conceptid) throws IOException {

		WriteUtil.write(getConfig(), refsetuuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + conceptid);
		WriteUtil.write(getConfig(), "\r\n");

	}
}
