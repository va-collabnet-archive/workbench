package org.ihtsdo.rf2.core.impl;

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
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;

/**
 * Title: RF2HistoricalRelationshipImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 HistoricalRelationship File Copyright: Copyright (c) 2010
 * Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */
public class RF2HistoricalRelationshipImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2HistoricalRelationshipImpl.class);
	private String releaseDate;

	public RF2HistoricalRelationshipImpl(Config config) {
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
			List<? extends I_IdPart> idParts;

			List<? extends I_RelTuple> relationships = sourceConcept.getSourceRelTuples(allStatuses, null, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			for (I_RelTuple rel : relationships) {
				characteristicTypeId="-1";
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

				//if (characteristicTypeId.equals(I_Constants.INFERRED) || characteristicTypeId.equals(I_Constants.ADDITIONALRELATION) || characteristicTypeId.equals(I_Constants.STATED) ){
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

				if (!(relTypeId==null || relTypeId.equals(""))){
					if (relTypeId.equals(I_Constants.MAY_BE) || relTypeId.equals(I_Constants.WAS_A) || relTypeId.equals(I_Constants.SAME_AS) || relTypeId.equals(I_Constants.REPLACED_BY)
							|| relTypeId.equals(I_Constants.MOVED_FROM) || relTypeId.equals(I_Constants.MOVED_TO)) {

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

						relationshipStatusId = rel.getStatusNid();
						if (relationshipStatusId == activeNid) { 														
							active = "1";
						} else if (relationshipStatusId == inactiveNid) { 														
							active = "0";
						}

						effectiveTime = getDateFormat().format(new Date(rel.getTime()));

						int relationshipGroup = rel.getGroup();

						if (relationshipId==null || relationshipId.equals("")){
							logger.info("Unplublished Retired Stated Relationship: " + rel.getUUIDs().iterator().next().toString());
						}else if (!((destinationId==null || destinationId.equals("") || destinationId.equals("0"))
								&& (sourceId==null || sourceId.equals("") || sourceId.equals("0"))
								&& (relTypeId==null || relTypeId.equals("") || relTypeId.equals("0")))){
							String reluuidStr =rel.getUUIDs().iterator().next().toString();
							writeRF2TypeLine(reluuidStr , relationshipId, effectiveTime, active, moduleId, sourceId, destinationId, relationshipGroup, relTypeId,
									characteristicTypeId, modifierId);
						}
					}
				}
			}
			//}
		} catch (IOException e) {
			logger.error("======failing for the sourceId=====" + sourceId);
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("======failing for the sourceId=====" + sourceId);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void writeRF2TypeLine(String relationshipId, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId,
			String characteristicTypeId, String modifierId) throws IOException {
		WriteUtil.write(getConfig(), relationshipId + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + sourceId + "\t" + destinationId + "\t" + relationshipGroup + "\t" + relTypeId
				+ "\t" + characteristicTypeId + "\t" + modifierId);
		WriteUtil.write(getConfig(), "\r\n");
	}


	public static void writeRF2TypeLine(String uuid , String relationshipId, String effectiveTime, String active, String moduleId, String sourceId, String destinationId, int relationshipGroup, String relTypeId,
			String characteristicTypeId, String modifierId) throws IOException {
		WriteUtil.write(getConfig(), uuid + "\t" + relationshipId + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + sourceId + "\t" + destinationId + "\t" + relationshipGroup + "\t" + relTypeId
				+ "\t" + characteristicTypeId + "\t" + modifierId);
		WriteUtil.write(getConfig(), "\r\n");
	}

}
