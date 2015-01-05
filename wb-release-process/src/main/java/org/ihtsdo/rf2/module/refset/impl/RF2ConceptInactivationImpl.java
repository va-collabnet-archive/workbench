package org.ihtsdo.rf2.module.refset.impl;

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
import org.ihtsdo.rf2.module.constant.I_Constants;
import org.ihtsdo.rf2.module.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2ConceptInactivationImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 ConceptInactivation Refset File Copyright: Copyright (c)
 * 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2ConceptInactivationImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2ConceptInactivationImpl.class);

	/** The record counter. */
	private static int recordCounter = 0;

	private String relEffectiveTime;

	/**
	 * Instantiates a new r f2 concept inactivation impl.
	 *
	 * @param config the config
	 */
	public RF2ConceptInactivationImpl(Config config) {
		super(config);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.I_GetConceptData)
	 */
	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	/**
	 * Gets the concept inactivation relationship value id.
	 *
	 * @param concept the concept
	 * @return the concept inactivation relationship value id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * @throws ParseException the parse exception
	 */
	public String getConceptInactivationRelationshipValueId(I_GetConceptData concept) throws IOException, TerminologyException, ParseException{
		String valueId = null;
		Date PREVIOUSRELEASEDATE = getDateFormat().parse(I_Constants.inactivation_policy_change);

		List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(allStatuses, null, 
				currenAceConfig.getViewPositionSetReadOnly(), 
				Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

		for (I_RelTuple rel : relationships) {
			Date et = new Date(rel.getTime());
			//Active stated relationship pointing to one of the special inactive concept
			if ((rel.getStatusNid() == activeNid) && (et.after(PREVIOUSRELEASEDATE) || et.equals(PREVIOUSRELEASEDATE))){

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
						if (destinationId.equals(I_Constants.DUPLICATE_CONCEPT)){
							valueId = I_Constants.DUPLICATE;
							relEffectiveTime=getDateFormat().format(et);
						}else if (destinationId.equals(I_Constants.AMBIGUOUS_CONCEPT)){
							valueId = I_Constants.AMBIGUOUS;
							relEffectiveTime=getDateFormat().format(et);
						}else if (destinationId.equals(I_Constants.OUTDATED_CONCEPT)){
							valueId = I_Constants.OUTDATED;
							relEffectiveTime=getDateFormat().format(et);
						}else if (destinationId.equals(I_Constants.ERRONEOUS_CONCEPT)){
							valueId = I_Constants.ERRONEOUS;
							relEffectiveTime=getDateFormat().format(et);
						}else if (destinationId.equals(I_Constants.LIMITED_CONCEPT)){
							valueId = I_Constants.LIMITED;
							relEffectiveTime=getDateFormat().format(et);
						}else if (destinationId.equals(I_Constants.MOVED_ELSEWHERE_CONCEPT)){
							valueId = I_Constants.MOVED_ELSE_WHERE;
							relEffectiveTime=getDateFormat().format(et);
						}else if (destinationId.equals(I_Constants.REASON_NOT_STATED_CONCEPT)){
							valueId = "XXX";
							relEffectiveTime=getDateFormat().format(et);
						}
					} 
				}
			}
		}


		return valueId;

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.impl.RF2AbstractImpl#export(org.dwfa.ace.api.I_GetConceptData, java.lang.String)
	 */
	@Override
	public void export(I_GetConceptData concept, String referencedComponentId) {
		try {
			String effectiveTime = "";
			String conceptStatus = "";
			String valueId = "";
			String active = "";

			int conceptInactivationRefsetNid = getNid(I_Constants.CONCEPT_INACTIVATION_REFSET_UID);
			String refsetId = getSctId(conceptInactivationRefsetNid);
			String moduleId = "";
			UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId);
			Date PREVIOUSRELEASEDATE = getDateFormat().parse(I_Constants.inactivation_policy_change);

			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allStatuses, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (conceptAttributes.size() > 0) {
				for (int i = 0; i < conceptAttributes.size(); i++) {

					I_ConceptAttributeTuple<?> i_ConceptAttributeTuple = (I_ConceptAttributeTuple<?>) conceptAttributes.get(i);
					Date et = new Date(getTermFactory().convertToThickVersion(i_ConceptAttributeTuple.getVersion()));
					effectiveTime = getDateFormat().format(et);
					boolean modified=true;
					if (!(effectiveTime.compareTo(getConfig().getPreviousReleaseDate())>0) ||
							!(effectiveTime.compareTo(getConfig().getReleaseDate())<=0)){
						modified=false;
					}
					if (isComponentToPublish( i_ConceptAttributeTuple.getMutablePart())){
						conceptStatus = getConceptInactivationStatusType(i_ConceptAttributeTuple.getStatusNid());

						if (conceptStatus.equals("0")){
							if (!modified){
								continue;
							}
							valueId="XXX";
						} else {
							relEffectiveTime=null;
							valueId= getConceptInactivationRelationshipValueId(concept);
							if (!modified ){
								if (relEffectiveTime!=null){

									if (!(relEffectiveTime.compareTo(getConfig().getPreviousReleaseDate())>0) ||
											!(relEffectiveTime.compareTo(getConfig().getReleaseDate())<=0)){
										continue;
									}
								}else{
									continue;
								}
							}
						}

						if (valueId!=null){

//							int intModuleId=i_ConceptAttributeTuple.getModuleNid();
//							moduleId=getModuleSCTIDForStampNid(intModuleId);

							moduleId = computeModuleId(concept);
							if (!valueId.equals("XXX")) {
								WriteRF2TypeLine(uuid, "", "1", moduleId, refsetId, referencedComponentId, valueId);
							} else {
								WriteRF2TypeLine(uuid, "", "0", moduleId, refsetId, referencedComponentId, "");
								recordCounter++;
							}
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

	/**
	 * Write r f2 type line.
	 *
	 * @param uuid the uuid
	 * @param effectiveTime the effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param refsetId the refset id
	 * @param referencedComponentId the referenced component id
	 * @param valueId the value id
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void WriteRF2TypeLine(UUID uuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String valueId) throws IOException {
		WriteUtil.write(getConfig(), uuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + valueId);
		WriteUtil.write(getConfig(), "\r\n");
	}

}
