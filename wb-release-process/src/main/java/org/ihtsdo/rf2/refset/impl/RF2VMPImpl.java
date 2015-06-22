package org.ihtsdo.rf2.refset.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
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
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2VMPImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2VMPImpl.class);

	public RF2VMPImpl(Config config) {
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

	public void export(I_GetConceptData concept, String conceptid) throws IOException {
		String effectiveTime = "";
		String active = "";
		I_ExtendByRefPartCid<?> extensionPart;
		UUID refsetuuid = null;
		int extensionStatusId = 0;

		try {
			String refsetId = I_Constants.VMP_REFSET_ID;
			String moduleId = I_Constants.CORE_MODULE_ID;
			int refsetTermAuxId = getNid(I_Constants.VMP_REFSET_UID_TERM_AUX);		
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
									}
								} else if (extensionStatusId == inactiveNid) { 														
									active = "0";
								} else {
									logger.error("unknown extensionStatusId =====>" + extensionStatusId);
								}

								if ((conceptid==null || conceptid.equals("")) && active.equals("1")){
									conceptid=concept.getUids().iterator().next().toString();
								}
								
								if (conceptid==null || conceptid.equals("")){
									logger.error("Unplublished Retired Concept of VMP : " + concept.getUUIDs().iterator().next().toString());
								}else {
									refsetuuid = Type5UuidFactory.get(refsetId + conceptid);
									effectiveTime = getDateFormat().format(new Date(extensionPart.getTime()));
									writeRF2TypeLine(refsetuuid, effectiveTime, active, moduleId, refsetId, conceptid);
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

	private void writeRF2TypeLine(UUID refsetuuid, String effectiveTime, String active, String moduleId, String refsetId, String conceptid) throws IOException {

		WriteUtil.write(getConfig(), refsetuuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + conceptid);
		WriteUtil.write(getConfig(), "\r\n");

	}
}
