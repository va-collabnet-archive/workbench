package org.ihtsdo.rf2.refset.impl;

import java.io.IOException;
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
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.refset.dao.RefsetConceptDAO;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;

/**
 * Title: RF2ConceptInactivationImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 ConceptInactivation Refset File Copyright: Copyright (c)
 * 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 * 
 */

public class RF2ConceptInactivationImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2ConceptInactivationImpl.class);
	private static int recordCounter = 0;

	public RF2ConceptInactivationImpl(Config config) {
		super(config);
	}

	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	public String getConceptInactivationRelationshipValueId(I_GetConceptData concept) throws IOException, TerminologyException{
		String valueId = "XXX";
		
		List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(allStatuses, null, 
				currenAceConfig.getViewPositionSetReadOnly(), 
				Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());
//logger.info("====relationships====" + relationships.size());

		for (I_RelTuple rel : relationships) {
			String characteristicTypeId="";
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
				String destinationId = "";
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

//logger.info("====destinationId====" + destinationId);
				String relTypeId = "";

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
							valueId = destinationId;
							logger.info("====valueId====" + valueId);
					}
						
					}
				
				} 
		}

				
		return valueId;
		
	}
	
	@Override
	public void export(I_GetConceptData concept, String referencedComponentId) {
		try {
			String effectiveTime = "";
			String conceptStatus = "";
			int firstInactivePassed = 0;
			int firstLimitedPassed = 0;
			String priorValueId = "";
			String valueId = "";
			String priorLimitedActiveFlag = "";
			String active = "";

			int conceptInactivationRefsetNid = getNid(I_Constants.CONCEPT_INACTIVATION_REFSET_UID);
			String refsetId = getSctId(conceptInactivationRefsetNid, getSnomedCorePathNid());
			String moduleId = I_Constants.CORE_MODULE_ID;
			UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId);
			Date LIMITED = getDateFormat().parse(I_Constants.limited_policy_change);

			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allStatuses, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());
			
			if (conceptAttributes.size() > 0) {
				for (int i = 0; i < conceptAttributes.size(); i++) {
					I_ConceptAttributeTuple<?> i_ConceptAttributeTuple = (I_ConceptAttributeTuple<?>) conceptAttributes.get(i);
					conceptStatus = getConceptInactivationStatusType(i_ConceptAttributeTuple.getStatusNid());
					Date et = new Date(getTermFactory().convertToThickVersion(i_ConceptAttributeTuple.getVersion()));
					effectiveTime = getDateFormat().format(et);
					if (conceptStatus.equals("0") || conceptStatus.equals("1")) {
						active = "0";
					} else {
						active = "1";
					}
					logger.info("====conceptStatus====" + conceptStatus + "===referencedComponentId==" + referencedComponentId);
					
					
					valueId = getConceptInactivationValueId(i_ConceptAttributeTuple.getStatusNid());
					if (valueId.equals("XXX")) {
						valueId= getConceptInactivationRelationshipValueId(concept);
					}
					if (!valueId.equals("XXX")) {
						WriteRF2TypeLine(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, valueId);
					} else {
						WriteRF2TypeLine(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, "");
						recordCounter++;
					}
				}
			}
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exceptions in exportConcept: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void WriteRF2TypeLine(RefsetConceptDAO dao) throws IOException {
		WriteUtil.write(getConfig(), dao.getUuid() + "\t" + dao.getEffectiveTime() + "\t" + dao.getActive() + "\t" + dao.getModuleId() + "\t" + dao.getRefsetId() + "\t"
				+ dao.getReferencedComponentId() + "\t" + dao.getValueId());
		WriteUtil.write(getConfig(), "\r\n");
	}

	private void WriteRF2TypeLine(UUID uuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String valueId) throws IOException {
		WriteUtil.write(getConfig(), uuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + valueId);
		WriteUtil.write(getConfig(), "\r\n");
	}

}
