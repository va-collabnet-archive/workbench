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

/**
 * Title: RF2SimpleMapOpenImpl Refset: Iterating over all the concept in workbench and fetching all the components required by RF2 Map Refset File Copyright: Copyright (c) 2013 Company: IHTSDO
 * 
 * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2SimpleMapOpenImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2SimpleMapOpenImpl.class);

	public RF2SimpleMapOpenImpl(Config config) {
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

	public void export(I_GetConceptData concept, String referencedComponentId) throws IOException {
		String effectiveTime = "";
		String active = "";
		I_ExtendByRefPartStr<?> extensionPart;
		UUID refsetuuid = null;
		String mapTarget = "";

		try {
			String refsetId = getConfig().getRefsetSCTID();
			if (refsetId==null || refsetId.equals("")){
				refsetId=getSCTId(getConfig(), UUID.fromString(getConfig().getRefsetUuid()));
			}
			String moduleId = "";
			int refsetTermAuxId = getNid(getConfig().getRefsetUuid());	
			List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(concept.getNid(), true);

			if (!extensions.isEmpty()) {
				for (I_ExtendByRef extension : extensions) {
					if (extension.getRefsetId() == refsetTermAuxId ) {
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
								if (logger.isDebugEnabled()) {
									logger.debug("Refset extension part not found!:" + extension.getPrimUuid());
								}
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
										System.out.println("unknown extensionStatusId =====>" + extensionStatusId);
										logger.error("unknown extensionStatusId =====>" + extensionStatusId);
										System.exit(0);
									}


									if ((referencedComponentId==null || referencedComponentId.equals("")) && active.equals("1")){
										referencedComponentId=concept.getUids().iterator().next().toString();
									}
									if (referencedComponentId==null || referencedComponentId.equals("")){
										logger.error("Unplublished Retired Concept of Map : " + concept.getUUIDs().iterator().next().toString());
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

	private void writeRF2TypeLine(UUID refsetuuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String targetComponent) throws IOException {

		WriteUtil.write(getConfig(), refsetuuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + targetComponent);
		WriteUtil.write(getConfig(), "\r\n");

	}
}
