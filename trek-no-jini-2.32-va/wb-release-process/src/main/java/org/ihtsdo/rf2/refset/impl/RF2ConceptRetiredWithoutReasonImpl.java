package org.ihtsdo.rf2.refset.impl;

import java.io.IOException;
import java.text.ParseException;
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

public class RF2ConceptRetiredWithoutReasonImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2ConceptRetiredWithoutReasonImpl.class);
	private static int recordCounter = 0;

	public RF2ConceptRetiredWithoutReasonImpl(Config config) {
		super(config);
	}

	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	public String getConceptInactivationRelationshipValueId(I_GetConceptData concept) throws IOException, TerminologyException, ParseException{
		String valueId = "XXX";
		Date PREVIOUSRELEASEDATE = getDateFormat().parse(I_Constants.inactivation_policy_change);

		List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(allStatuses, null, 
				currenAceConfig.getViewPositionSetReadOnly(), 
				Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

		for (I_RelTuple rel : relationships) {
			
			Date et = new Date(rel.getTime());
			if (et.after(PREVIOUSRELEASEDATE) || et.equals(PREVIOUSRELEASEDATE)){
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
						if (destinationId.equals(I_Constants.REASON_NOT_STATED_CONCEPT)){
							valueId = destinationId;
							System.out.println("====Concept====" + valueId + " & " + concept.getInitialText());
							recordCounter++;
						}	
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
			String valueId = "";
			String active = "";

			int conceptInactivationRefsetNid = getNid(I_Constants.CONCEPT_INACTIVATION_REFSET_UID);
			String refsetId = getSctId(conceptInactivationRefsetNid, getSnomedCorePathNid());
			String moduleId = I_Constants.CORE_MODULE_ID;
			UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId);
			Date PREVIOUSRELEASEDATE = getDateFormat().parse(I_Constants.inactivation_policy_change);

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
					
					//if(conceptStatus.equals("1") && (et.after(PREVIOUSRELEASEDATE) || et.equals(PREVIOUSRELEASEDATE))) {
					
					if(conceptStatus.equals("1")){
						active = "1";
						valueId = getConceptInactivationValueId(i_ConceptAttributeTuple.getStatusNid());
					
						if (valueId.equals("XXX")) {
							//logger.info("====if====" + conceptStatus + "===referencedComponentId==" + referencedComponentId + "==et==" + et);
							valueId= getConceptInactivationRelationshipValueId(concept);
						}
					
						if (!valueId.equals("XXX")) {
							WriteRF2TypeLine(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, valueId);
						} 
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

	private void WriteRF2TypeLine(UUID uuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String valueId) throws IOException {
		WriteUtil.write(getConfig(), uuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + valueId);
		WriteUtil.write(getConfig(), "\r\n");
	}

}
