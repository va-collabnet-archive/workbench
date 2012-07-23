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
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.refset.factory.SctidUuid;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;

/**
 * Title: RF2DescriptionImpl Description: Iterating over all the concept in
 * workbench and fetching all the components required by RF2 Description File
 * Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2GenericRefsetImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2GenericRefsetImpl.class);
	private List<SctidUuid> sctidUuidList;
	private String moduleid;

	public RF2GenericRefsetImpl(Config config, List<SctidUuid> sctidUuidList, String moduleid) {
		super(config);
		this.sctidUuidList = sctidUuidList;
		this.moduleid = moduleid;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.
	 * I_GetConceptData)
	 */
	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	public void export(I_GetConceptData concept, String conceptid) throws IOException {
		String effectiveTime = "";
		String active = "";
		I_ExtendByRefPartCid<?> extensionPart;
		UUID refsetuuid = null;
		int extensionStatusId = 0;

		for (SctidUuid sctIdUuid : sctidUuidList) {
			try {

				String refsetId = sctIdUuid.getSctid();

				int refsetTermAuxId = getNid(sctIdUuid.getUuid());

				List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(concept.getNid(), true);
				logger.info("CONCEPT: " + concept.getInitialText());
				logger.info("Extensions Size: " + extensions.size());
				if (!extensions.isEmpty()) {
					for (I_ExtendByRef extension : extensions) {
						if (extension.getRefsetId() == refsetTermAuxId) {
							if (extension != null) {
								long lastVersion = Long.MIN_VALUE;
								extensionPart = null;
								for (I_ExtendByRefVersion loopTuple : extension.getTuples(allStatusSet, currenAceConfig.getViewPositionSetReadOnly(), Precedence.PATH, currenAceConfig.getConflictResolutionStrategy())) {

									if (loopTuple.getTime() >= lastVersion) {
										lastVersion = loopTuple.getTime();
										extensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
									}
								}
								if (extensionPart == null) {
									if (logger.isDebugEnabled()) {
										logger.debug("Refset extension part not found!");
									}
								} else {
									extensionStatusId = extensionPart.getStatusNid();
									if (extensionStatusId == activeNid || extensionStatusId == currentNid) {
										active = "1";
									} else if (extensionStatusId == inactiveNid || extensionStatusId == retiredNid) {
										active = "0";
									} else {
										System.out.println("unknown extensionStatusId =====>" + extensionStatusId);
										logger.error("unknown extensionStatusId =====>" + extensionStatusId);
										continue;
									}

									if ((conceptid == null || conceptid.equals("")) && active.equals("1")) {
										conceptid = concept.getUids().iterator().next().toString();
									}

									if (conceptid == null || conceptid.equals("")) {
										logger.error("Unplublished Retired Concept of Simple refset : " + concept.getUUIDs().iterator().next().toString());
									} else {
										refsetuuid = Type5UuidFactory.get(refsetId + conceptid);
										effectiveTime = getDateFormat().format(new Date(extensionPart.getTime()));
										writeRF2TypeLine(refsetuuid, effectiveTime, active, moduleid, refsetId, conceptid);
									}
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
	}

	private void writeRF2TypeLine(UUID refsetuuid, String effectiveTime, String active, String moduleId, String refsetId, String conceptid) throws IOException {

		WriteUtil.write(getConfig(), refsetuuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + conceptid);
		WriteUtil.write(getConfig(), "\r\n");

	}
}
