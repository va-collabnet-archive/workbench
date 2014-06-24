package org.ihtsdo.rf2.core.impl;

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
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;

/**
 * Title: RF2StatedRelationshipImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 StatedRelationship File Copyright: Copyright (c) 2010
 * Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */
public class RF2StatedRelationshipImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2StatedRelationshipImpl.class);
	private String releaseDate;

	public RF2StatedRelationshipImpl(Config config) {
		super(config);
		releaseDate=config.getReleaseDate();
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

	public void export(I_GetConceptData sourceConcept, String sourceId) throws IOException {
		try {
			String effectiveTime = "";
			String relationshipId = "";
			String destinationId = "";
			String relTypeId = "";
			String moduleId =I_Constants.CORE_MODULE_ID;
			String active = "";
			String characteristicTypeId = "";
			String modifierId = I_Constants.SOMEMODIFIER;
			int relationshipStatusId=0;
			
			//Change this to come from config
			Date PREVIOUSRELEASEDATE = getDateFormat().parse(I_Constants.inactivation_policy_change);
		
			List<? extends I_RelTuple> relationships = sourceConcept.getSourceRelTuples(allStatuses, null, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());
			
			for (I_RelTuple rel : relationships) {
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
				if (characteristicTypeId.equals(I_Constants.STATED) ){

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
									RelAssertionType.STATED);
							if (denotation instanceof Long) {
								Long c = (Long) denotation;
								if (c != null)  relationshipId = c.toString();
							}
						}
					}
					
					Date et = new Date(rel.getTime());
					effectiveTime = getDateFormat().format(et);
					
					relationshipStatusId = rel.getStatusNid();
					if (relationshipStatusId == activeNid) {               
						active = "1";
						moduleId = getConceptMetaModuleID(sourceConcept,releaseDate);
				     } else if (relationshipStatusId == inactiveNid) {               
					      active = "0";
					      moduleId=I_Constants.CORE_MODULE_ID;
//					      Long lastActiveDate=getLatestActivePart(rel.getFixedPart().getMutableParts());
//						      
//					      if (lastActiveDate!=null){
//					      moduleId = getConceptMetaModuleID(sourceConcept,
//					        getDateFormat().format(new Date(lastActiveDate)));
//					      }else{
//					       moduleId = getConceptMetaModuleID(sourceConcept,
//					         getDateFormat().format(new Date(rel.getTime())));
//					      }
				     }
				    
					if(moduleId.equals(I_Constants.META_MODULE_ID)){		
						//logger.info("==Meta Concept==" + sourceId + " & Name : " + sourceConcept.getInitialText());
						incrementMetaDataCount();
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
						logger.info("Unplublished Retired Stated Relationship: " + rel.getUUIDs().iterator().next().toString());
					}else if(getConfig().getRf2Format().equals("false") ){
						writeRF2TypeLine(relationshipId, effectiveTime, active, moduleId, sourceId, destinationId, relationshipGroup, relTypeId,
							characteristicTypeId, modifierId, authorName);
					}else{
						writeRF2TypeLine(relationshipId, effectiveTime, active, moduleId, sourceId, destinationId, relationshipGroup, relTypeId,
							characteristicTypeId, modifierId);
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
			logger.error("Exceptions in exportStatedRelationship: " + e.getMessage());
			logger.error("Exceptions in exportStatedRelationship: " +sourceId);
		}

	}
	
	

	public static void writeRF2TypeLine(String relationshipId, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId,
			String characteristicTypeId, String modifierId) throws IOException {
		WriteUtil.write(getConfig(), relationshipId + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + sourceId + "\t" + destinationId + "\t" + relationshipGroup + "\t" + relTypeId
				+ "\t" + characteristicTypeId + "\t" + modifierId);
		WriteUtil.write(getConfig(), "\r\n");
	}
	
	public static void writeRF2TypeLine(String relationshipId, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId,
			String characteristicTypeId, String modifierId, String authorName) throws IOException {
		WriteUtil.write(getConfig(), relationshipId + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + sourceId + "\t" + destinationId + "\t" + relationshipGroup + "\t" + relTypeId
				+ "\t" + characteristicTypeId + "\t" + modifierId + "\t" + authorName);
		WriteUtil.write(getConfig(), "\r\n");
	}
}
