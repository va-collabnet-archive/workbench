package org.ihtsdo.rf2.module.refset.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.module.constant.I_Constants;
import org.ihtsdo.rf2.module.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2RelationshipImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Relationship File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2HistoricalAssociationImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2HistoricalAssociationImpl.class);

	/**
	 * Instantiates a new r f2 historical association impl.
	 *
	 * @param config the config
	 */
	public RF2HistoricalAssociationImpl(Config config) {
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
	@Override
	public void export(I_GetConceptData concept, String referencedComponentId) throws IOException {
		try {

			String effectiveTime = "";
			String relTypeId = "";
			String active = "";
			String targetComponent = "";
			String moduleId = "";
			int relationshipStatusId=0;

			List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(allStatuses, null, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			for (I_RelTuple rel : relationships) {
				effectiveTime = getDateFormat().format(new Date(rel.getTime()));
				if (!(effectiveTime.compareTo(getConfig().getPreviousReleaseDate())>0) ||
						!(effectiveTime.compareTo(getConfig().getReleaseDate())<=0)){
					continue;
				}
				if (isComponentToPublish( rel.getMutablePart())){
					targetComponent = getSctId(rel.getC2Id());

					relTypeId = getSctId(rel.getTypeNid());

					if (relTypeId==null || relTypeId.equals("")){
						relTypeId=tf.getUids(rel.getTypeNid()).iterator().next().toString();
					}
					if (relTypeId.equals(I_Constants.MAY_BE) || relTypeId.equals(I_Constants.WAS_A) || relTypeId.equals(I_Constants.SAME_AS) || relTypeId.equals(I_Constants.REPLACED_BY)
							|| relTypeId.equals(I_Constants.MOVED_FROM) || relTypeId.equals(I_Constants.MOVED_TO)) {

						relationshipStatusId = rel.getStatusNid();
						if (relationshipStatusId == activeNid) { 														
							active = "1";
						} else if (relationshipStatusId == inactiveNid) { 														
							active = "0";
						}

						String relationshipId = "";

						I_Identify id = tf.getId(rel.getNid());
						if (id != null) {
							List<? extends I_IdPart> idParts = tf.getId(rel.getNid()).getVisibleIds(currenAceConfig.getViewPositionSetReadOnly(), 
									snomedIntId);
							if (idParts != null) {
								Object denotation = getLastCurrentVisibleId(idParts, currenAceConfig.getViewPositionSetReadOnly(), 
										RelAssertionType.INFERRED_THEN_STATED);
								if (denotation instanceof Long) {
									Long c = (Long) denotation;
									if (c != null)  relationshipId = c.toString();
								}
							}
						}

						if ((relationshipId==null || relationshipId.equals("")) && active.equals("1")){
							relationshipId=rel.getUUIDs().iterator().next().toString();
						}

						if (relationshipId==null || relationshipId.equals("")){
							logger.error("Unplublished Retired Historical Relationship: " + rel.getUUIDs().iterator().next().toString());
						}else{


							String refsetId = getRefsetId(relTypeId);

							if (referencedComponentId==null || referencedComponentId.equals("")){
								referencedComponentId=concept.getUids().iterator().next().toString();
							}
							if (targetComponent==null || targetComponent.equals("")){
								Collection<UUID> Uids=tf.getUids(rel.getC2Id());
								if (Uids==null  ){
									continue;
								}
								targetComponent=Uids.iterator().next().toString();
								if (targetComponent.equals(nullUuid)){
									continue;
								}
							}

							UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId + targetComponent);

//							int intModuleId=rel.getModuleNid();
//							moduleId=getModuleSCTIDForStampNid(intModuleId);
							moduleId = computeModuleId(concept);
							writeRF2TypeLine(uuid, "", active, moduleId, refsetId, referencedComponentId, targetComponent);
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Write r f2 type line.
	 *
	 * @param uuid the uuid
	 * @param effectiveTime the effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param refsetId the refset id
	 * @param referencedComponentId the referenced component id
	 * @param targetComponent the target component
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeRF2TypeLine(UUID uuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String targetComponent) throws IOException {

		WriteUtil.write(getConfig(), uuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + targetComponent);
		WriteUtil.write(getConfig(), "\r\n");

	}
}
