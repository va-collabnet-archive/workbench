package org.ihtsdo.rf2.refset.impl;

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
import org.dwfa.ace.api.Terms;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;

/**
 * Title: RF2RelationshipImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Relationship File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2HistoricalAssociationRelationshipImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2HistoricalAssociationRelationshipImpl.class);

	public RF2HistoricalAssociationRelationshipImpl(Config config) {
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
	public void export(I_GetConceptData concept, String referencedComponentId) throws IOException {
		try {
			
			String effectiveTime = "";
			String relTypeId = "";
			String active = "";
			String releaseDate=getConfig().getReleaseDate();
			String targetComponent = "";
			String moduleId = I_Constants.CORE_MODULE_ID;
			int relationshipStatusId=0;
			String modifierId = I_Constants.SOMEMODIFIER; 
			String characteristicTypeId = "-1";
			Date PREVIOUSRELEASEDATE = getDateFormat().parse(I_Constants.inactivation_policy_change);
			String wstargetComponent="wstargetComponent";
			List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(allStatuses, null, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			for (I_RelTuple rel : relationships) {
		
				targetComponent = getSctId(rel.getC2Id(), getSnomedCorePathNid());

				relTypeId = getSctId(rel.getTypeNid(), getSnomedCorePathNid());

				if (relTypeId==null || relTypeId.equals("")){
					relTypeId=tf.getUids(rel.getTypeNid()).iterator().next().toString();
				}
				if (relTypeId.equals(I_Constants.MAY_BE) || relTypeId.equals(I_Constants.WAS_A) || relTypeId.equals(I_Constants.SAME_AS) || relTypeId.equals(I_Constants.REPLACED_BY)
						|| relTypeId.equals(I_Constants.MOVED_FROM) || relTypeId.equals(I_Constants.MOVED_TO)) {
	
					/*I_Identify charId = tf.getId(rel.getCharacteristicId());
					List<? extends I_IdPart> idParts = charId.getVisibleIds(currenAceConfig.getViewPositionSetReadOnly(), 
							snomedIntId);
					if (idParts != null) {
						Object denotation = getLastCurrentVisibleId(idParts, currenAceConfig.getViewPositionSetReadOnly(), 
								RelAssertionType.INFERRED_THEN_STATED);
						if (denotation instanceof Long) {
							Long c = (Long) denotation;
							if (c != null)  characteristicTypeId = c.toString();
						}
					}*/
					
					
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
						Date et = new Date(rel.getTime());
						
						
						//writeRF2TypeLine(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, targetComponent);
						if(et.equals(PREVIOUSRELEASEDATE) || et.after(PREVIOUSRELEASEDATE)) {
						effectiveTime = getDateFormat().format(new Date(rel.getTime()));
						int relationshipGroup = rel.getGroup();
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
						String	wbrelationshipId="wbrelationshipId";
						UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId + targetComponent);
						
						if(active.equals("0"))
							wbrelationshipId= relationshipId;
						else
							wbrelationshipId = getRelationshipId(getConfig(), uuid);
									
						if (targetComponent.contains("-")){
							//get conceptId by calling webservice 
							wstargetComponent = getConceptId(getConfig(), UUID.fromString(targetComponent));
							logger.info("targetComponent " + targetComponent + " & wstargetComponent " +wstargetComponent);
						}else{
							wstargetComponent = targetComponent;
						}
							
						logger.info("==uuid== " +uuid + "  ==wbrelationshipId== " + wbrelationshipId + "  ==characteristicTypeId== " + characteristicTypeId + "   ==relationshipGroup== " + relationshipGroup);
						writeRF2TypeLine(wbrelationshipId ,releaseDate , active, moduleId, referencedComponentId, wstargetComponent, 0, relTypeId, "-1", modifierId);							
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
	
	private static void writeRF2TypeLine(UUID uuid, String relationshipId , String effectiveTime, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId,
			String characteristicTypeId, String modifierId) throws IOException {
		
		WriteUtil.write(getConfig(), uuid + "\t" + relationshipId + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + sourceId + "\t" + destinationId + "\t" + relationshipGroup + "\t" + relTypeId
				+ "\t" + characteristicTypeId + "\t" + modifierId);
		WriteUtil.write(getConfig(), "\r\n");
	}
	
	
	private static void writeRF2TypeLine(String wbrelationshipId,  String effectiveTime, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId,
			String characteristicTypeId, String modifierId) throws IOException {
		
		WriteUtil.write(getConfig(), wbrelationshipId + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + sourceId + "\t" + destinationId + "\t" + relationshipGroup + "\t" + relTypeId
				+ "\t" + characteristicTypeId + "\t" + modifierId);
		WriteUtil.write(getConfig(), "\r\n");
	}
}
