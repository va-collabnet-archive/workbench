package org.ihtsdo.rf2.module.core.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
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

public class RF2RelationshipImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2RelationshipImpl.class);

	/**
	 * Instantiates a new r f2 relationship impl.
	 *
	 * @param config the config
	 */
	public RF2RelationshipImpl(Config config) {
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
	public void export(I_GetConceptData sourceConcept, String sourceId) throws IOException {
		try {
			String effectiveTime = "";
			String relationshipId = "";
			String destinationId = "";
			String relTypeId = "";
			String active = "";
			String moduleId="";
			String characteristicTypeId = "";
			String modifierId = I_Constants.SOMEMODIFIER; 
			int relationshipStatusId=0;
			String defaultModule=getConfig().getDefaultModule();
			
			List<? extends I_RelTuple> relationships = sourceConcept.getSourceRelTuples(allStatuses, null, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			for (I_RelTuple rel : relationships) {
				effectiveTime = getDateFormat().format(new Date(rel.getTime()));
				if (!(effectiveTime.compareTo(getConfig().getPreviousReleaseDate())>0) ||
						!(effectiveTime.compareTo(getConfig().getReleaseDate())<=0)){
					continue;
				}
				if (isComponentToPublish( rel.getMutablePart())){
					characteristicTypeId="";

					I_Identify charId = tf.getId(rel.getCharacteristicId());
					List<? extends I_IdPart> idParts = charId.getVisibleIds(currenAceConfig.getViewPositionSetReadOnly(), 
							snomedIntId);
					if (idParts != null) {
						Object denotation = getLastCurrentVisibleId(idParts, currenAceConfig.getViewPositionSetReadOnly(), 
								RelAssertionType.INFERRED_THEN_STATED);
						if (denotation instanceof Long) {
							Long c = (Long) denotation;
							if (c != null)  characteristicTypeId = c.toString();
						}
					}

					if (characteristicTypeId.equals(I_Constants.INFERRED) || characteristicTypeId.equals(I_Constants.ADDITIONALRELATION)){
						destinationId = "";
						I_Identify id = tf.getId(rel.getC2Id());
						if (id != null) {
							idParts = id.getVisibleIds(currenAceConfig.getViewPositionSetReadOnly(), 
									snomedIntId);
							if (idParts != null) {
								Object denotation = getLastCurrentVisibleId(idParts, currenAceConfig.getViewPositionSetReadOnly(), 
										RelAssertionType.INFERRED_THEN_STATED);
								if (denotation instanceof Long) {
									Long c = (Long) denotation;
									if (c != null)  destinationId = c.toString();
								}
							}
						}

						relTypeId = "";

						id = tf.getId(rel.getTypeNid());
						if (id != null) {
							idParts = tf.getId(rel.getTypeNid()).getVisibleIds(currenAceConfig.getViewPositionSetReadOnly(), 
									snomedIntId);
							if (idParts != null) {
								Object denotation = getLastCurrentVisibleId(idParts, currenAceConfig.getViewPositionSetReadOnly(), 
										RelAssertionType.INFERRED_THEN_STATED);
								if (denotation instanceof Long) {
									Long c = (Long) denotation;
									if (c != null)  relTypeId = c.toString();
								}
							}
						}

						if (relTypeId.equals(I_Constants.ISA)) {
							if (destinationId.equals(I_Constants.DUPLICATE_CONCEPT) || destinationId.equals(I_Constants.AMBIGUOUS_CONCEPT) ||
									destinationId.equals(I_Constants.OUTDATED_CONCEPT) || destinationId.equals(I_Constants.ERRONEOUS_CONCEPT ) ||
									destinationId.equals(I_Constants.LIMITED_CONCEPT) || destinationId.equals(I_Constants.REASON_NOT_STATED_CONCEPT) ||
									destinationId.equals(I_Constants.MOVED_ELSEWHERE_CONCEPT)){
								continue;
							}
						} 

						relationshipId = "";

						id = tf.getId(rel.getNid());
						if (id != null) {
							idParts = tf.getId(rel.getNid()).getVisibleIds(currenAceConfig.getViewPositionSetReadOnly(), 
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

						relationshipStatusId = rel.getStatusNid();
						if (relationshipStatusId == activeNid) {               
							active = "1";
						} else if (relationshipStatusId == inactiveNid) {               
							active = "0";
						}

						int intModuleId=rel.getModuleNid();
						moduleId=getModuleSCTIDForStampNid(intModuleId);
						
						if (moduleId==null){
							
							List<? extends I_ConceptAttributeTuple> conceptAttributes = sourceConcept.getConceptAttributeTuples(
									allStatuses, 
									currenAceConfig.getViewPositionSetReadOnly(), 
									Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

							if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
								I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();
								
								int intConcModuleId=attributes.getModuleNid();
								moduleId=getModuleSCTIDForStampNid(intConcModuleId);
								
								if (moduleId!=null){
									logger.info("intModuleId=" + intModuleId + " replaced by Concept Module: " + moduleId);
								}
							}
							if (moduleId==null){
								logger.info("intModuleId=" + intModuleId + " replaced by default Module: " + defaultModule);
								moduleId=defaultModule;
							}
						}

						int relationshipGroup = rel.getGroup();

						if (sourceId==null || sourceId.equals("")){
							sourceId=sourceConcept.getUids().iterator().next().toString();
						}

						if (relTypeId==null || relTypeId.equals("")){
							relTypeId=tf.getUids(rel.getTypeNid()).iterator().next().toString();
						}

						if (destinationId==null || destinationId.equals("")){
							Collection<UUID> Uids=tf.getUids(rel.getC2Id());
							if (Uids==null  ){
								continue;
							}
							destinationId=Uids.iterator().next().toString();
							if (destinationId.equals(nullUuid)){
								continue;
							}
						}
						if ((relationshipId==null || relationshipId.equals("")) && active.equals("1")){
							relationshipId=rel.getUUIDs().iterator().next().toString();						
						}

						String authorName = tf.getConcept(rel.getAuthorNid()).getInitialText();
						if (relationshipId==null || relationshipId.equals("")){
							logger.info("Unplublished Retired Relationship: " + rel.getUUIDs().iterator().next().toString());
						}else if(getConfig().getRf2Format().equals("false") ){	
							writeRF2TypeLine(relationshipId, effectiveTime, active, moduleId, sourceId, destinationId, relationshipGroup, relTypeId,
									characteristicTypeId, modifierId, authorName);
						}else{
							writeRF2TypeLine(relationshipId, effectiveTime, active, moduleId, sourceId, destinationId, relationshipGroup, relTypeId,
									characteristicTypeId, modifierId);	
						}
					}
				}
			}

		}catch (NullPointerException ne) {
			logger.error("NullPointerException: " + ne.getMessage());
			logger.error(" NullPointerException " + sourceId);
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			logger.error("IOExceptions: " + sourceId);
		} catch (Exception e) {
			logger.error("Exceptions in exportInfRelationship: " + e.getMessage());
			logger.error("Exceptions in exportInfRelationship: " +sourceId);
		}

	}
	
	/**
	 * Write r f2 type line.
	 *
	 * @param relationshipId the relationship id
	 * @param effectiveTime the effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param sourceId the source id
	 * @param destinationId the destination id
	 * @param relationshipGroup the relationship group
	 * @param relTypeId the rel type id
	 * @param characteristicTypeId the characteristic type id
	 * @param modifierId the modifier id
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeRF2TypeLine(String relationshipId, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId,
			String characteristicTypeId, String modifierId) throws IOException {
		WriteUtil.write(getConfig(), relationshipId + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + sourceId + "\t" + destinationId + "\t" + relationshipGroup + "\t" + relTypeId
				+ "\t" + characteristicTypeId + "\t" + modifierId);
		WriteUtil.write(getConfig(), "\r\n");
	}

	/**
	 * Write r f2 type line.
	 *
	 * @param relationshipId the relationship id
	 * @param effectiveTime the effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param sourceId the source id
	 * @param destinationId the destination id
	 * @param relationshipGroup the relationship group
	 * @param relTypeId the rel type id
	 * @param characteristicTypeId the characteristic type id
	 * @param modifierId the modifier id
	 * @param authorName the author name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeRF2TypeLine(String relationshipId, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId,
			String characteristicTypeId, String modifierId, String authorName) throws IOException {
		WriteUtil.write(getConfig(), relationshipId + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + sourceId + "\t" + destinationId + "\t" + relationshipGroup + "\t" + relTypeId
				+ "\t" + characteristicTypeId + "\t" + modifierId + "\t" + authorName);
		WriteUtil.write(getConfig(), "\r\n");
	}

}
