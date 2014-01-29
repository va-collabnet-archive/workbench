package org.ihtsdo.rf2.module.refset.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.module.constant.I_Constants;
import org.ihtsdo.rf2.module.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2ICDOMapImpl Refset: Iterating over all the concept in workbench and fetching all the components required by RF2 ICDO Refset File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2ICDOMapImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2ICDOMapImpl.class);

	/**
	 * Instantiates a new r f2 icdo map impl.
	 *
	 * @param config the config
	 */
	public RF2ICDOMapImpl(Config config) {
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

	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.impl.RF2AbstractImpl#export(org.dwfa.ace.api.I_GetConceptData, java.lang.String)
	 */
	public void export(I_GetConceptData concept, String referencedComponentId) throws IOException {
		String effectiveTime = "";
		String active = "";
		I_ExtendByRefPartStr<?> extensionPart;
		UUID refsetuuid = null;
		String mapTarget = "";

		try {
			String refsetId = I_Constants.ICDO_REFSET_ID;
			String moduleId = "";
			int refsetTermAuxNid = getNid(I_Constants.ICDO_REFSET_UID_TERM_AUX);
			int refsetNid = getNid("5ef10e09-8f16-398e-99b5-55cff5bd820a");
			List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(concept.getNid(), true);

			if (!extensions.isEmpty()) {
				for (I_ExtendByRef extension : extensions) {
					if (extension.getRefsetId() == refsetTermAuxNid || (extension.getRefsetId() == refsetNid)) {
						if (extension != null) {
							long lastVersion = Long.MIN_VALUE;
							extensionPart=null;
							for (I_ExtendByRefVersion loopTuple : extension.getTuples(allStatusSet,currenAceConfig.getViewPositionSetReadOnly(),
									Precedence.PATH,currenAceConfig.getConflictResolutionStrategy())) {

								if (loopTuple.getTime() >= lastVersion) {
									lastVersion = loopTuple.getTime();
									extensionPart = (I_ExtendByRefPartStr) loopTuple.getMutablePart();
								}
							}
							if (extensionPart == null) {
								throw new Exception("Refset extension part not found!");
							}else{
								effectiveTime = getDateFormat().format(new Date(extensionPart.getTime()));
								if (!(effectiveTime.compareTo(getConfig().getPreviousReleaseDate())>0) ||
										!(effectiveTime.compareTo(getConfig().getReleaseDate())<=0)){
									continue;
								}

								if (isComponentToPublish( extensionPart)){
									int extensionStatusId = extensionPart.getStatusNid();

									if (extensionStatusId == activeNid) { // active													
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
									} else if (extensionStatusId == inactiveNid) { // inactive													
										active = "0";								
									} else {
										logger.error("unknown extensionStatusId =====>" + extensionStatusId);
									}


									if ((referencedComponentId==null || referencedComponentId.equals("")) && active.equals("1")){
										referencedComponentId=concept.getUids().iterator().next().toString();
									}
									if (referencedComponentId==null || referencedComponentId.equals("")){
										logger.error("Unplublished Retired Concept of ICDO Map : " + concept.getUUIDs().iterator().next().toString());
									}else{

										mapTarget = extensionPart.getStringValue();
										if(mapTarget.contains(" ") || mapTarget.contains("[") || mapTarget.contains("]") ){
											String test[] = mapTarget.split(" ");
											mapTarget = test[0];								
										}

										int intModuleId=extensionPart.getModuleNid();
										moduleId=getModuleSCTIDForStampNid(intModuleId);
										refsetuuid = Type5UuidFactory.get(refsetId + referencedComponentId + mapTarget);
										writeRF2TypeLine(refsetuuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, mapTarget);

									}
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("Message : concept : " + referencedComponentId, e);
		} catch (Exception e) {
			logger.error("Message : concept : " + referencedComponentId, e);
		}
	}

	/**
	 * Write r f2 type line.
	 *
	 * @param refsetuuid the refsetuuid
	 * @param effectiveTime the effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param refsetId the refset id
	 * @param referencedComponentId the referenced component id
	 * @param targetComponent the target component
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeRF2TypeLine(UUID refsetuuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String targetComponent) throws IOException {

		WriteUtil.write(getConfig(), refsetuuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + targetComponent);
		WriteUtil.write(getConfig(), "\r\n");

	}
}
