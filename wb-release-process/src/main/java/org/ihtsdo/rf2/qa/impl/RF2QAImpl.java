package org.ihtsdo.rf2.qa.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;

/**
 * Title: RF2ConceptImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Concept File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2QAImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2QAImpl.class);

	public RF2QAImpl(Config config) {
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
	public void export(I_GetConceptData concept, String conceptid) {
		String effectiveTime = "";
		Date et = null;
		
		
		try {
		
			Date PREVIOUSRELEASEDATE = getDateFormat().parse(I_Constants.inactivation_policy_change);

			//********Concept Component**********//
			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allStatuses, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
				I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();
				et = new Date(attributes.getTime());
				effectiveTime = getDateFormat().format(et);
				
				if(et.after(PREVIOUSRELEASEDATE)){
					String authorName = tf.getConcept(attributes.getAuthorNid()).getInitialText();
					WriteUtil.write(getConfig(), "concept :" + "\t" + concept.getInitialText() + "\t" + conceptid + "\t" + effectiveTime + "\t" + authorName);
					WriteUtil.write(getConfig(), "\r\n");
				}
			}
			
			
			//********Description & text-Definition Component**********//
			List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allStatuses, 
					allDescTypes, currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());
			
			for (I_DescriptionTuple description: descriptions) {
				Date descriptionEffectiveDate = new Date(getTermFactory().convertToThickVersion(description.getVersion()));
				effectiveTime = getDateFormat().format(descriptionEffectiveDate);
				
				if(descriptionEffectiveDate.after(PREVIOUSRELEASEDATE)){
					String sDescType = getSnomedDescriptionType(description.getTypeNid());

					String descAuthorName = tf.getConcept(description.getAuthorNid()).getInitialText();
					String descriptionid = getDescriptionId(description.getDescId(), ExportUtil.getSnomedCorePathNid());
					if ((descriptionid==null || descriptionid.equals("") || descriptionid.equals("0"))){
						descriptionid=description.getUUIDs().iterator().next().toString();
					}
					
					String term = description.getText();
					if (term!=null ){
						if (term.indexOf("\t")>-1){
							term=term.replaceAll("\t", "");
						}
						if (term.indexOf("\r")>-1){
							term=term.replaceAll("\r", "");
						}
						if (term.indexOf("\n")>-1){
							term=term.replaceAll("\n", "");
						}
					}
					if (sDescType.equals("4")) {
						WriteUtil.write(getConfig(), "Text Definition :" + "\t" +  term + "\t" + descriptionid + "\t" + effectiveTime + "\t" + descAuthorName);
						WriteUtil.write(getConfig(), "\r\n");
					}else{
						WriteUtil.write(getConfig(), "Description :" + "\t" +  term + "\t" + descriptionid + "\t" + effectiveTime + "\t" + descAuthorName);
						WriteUtil.write(getConfig(), "\r\n");
					}
				}	
			}
			
					
			
			
			//********Stated Relationship Component**********//
			List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(allStatuses, null, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			for (I_RelTuple rel : relationships) {
				Date relationshipEffectiveDate = new Date(getTermFactory().convertToThickVersion(rel.getVersion()));
				effectiveTime = getDateFormat().format(relationshipEffectiveDate);
				//effectiveTime = getDateFormat().format(new Date(rel.getTime()));
				
				if(relationshipEffectiveDate.after(PREVIOUSRELEASEDATE)){
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
						String statedRelationshipId = "";	
						I_Identify  id = tf.getId(rel.getNid());
						if (id != null) {
							idParts = tf.getId(rel.getNid()).getVisibleIds(currenAceConfig.getViewPositionSetReadOnly(), 
									snomedIntId);
							if (idParts != null) {
								Object denotation = getLastCurrentVisibleId(idParts, currenAceConfig.getViewPositionSetReadOnly(), 
										RelAssertionType.STATED);
								if (denotation instanceof Long) {
									Long c = (Long) denotation;
									if (c != null)  statedRelationshipId = c.toString();
								}
							}
						}	
					
						if ((statedRelationshipId==null || statedRelationshipId.equals(""))){
							statedRelationshipId=rel.getUUIDs().iterator().next().toString();						
						}
					
						String statedAuthorName = tf.getConcept(rel.getAuthorNid()).getInitialText();
						WriteUtil.write(getConfig(), "Stated Relationship : " + "\t" +  concept.getInitialText() + "\t" +  statedRelationshipId + "\t" + effectiveTime + "\t" + statedAuthorName);
						WriteUtil.write(getConfig(), "\r\n");					
					}
					
					
					if (idParts != null) {
						Object denotation = getLastCurrentVisibleId(idParts, currenAceConfig.getViewPositionSetReadOnly(), 
								RelAssertionType.INFERRED_THEN_STATED);
						if (denotation instanceof Long) {
							Long c = (Long) denotation;
							if (c != null)  characteristicTypeId = c.toString();
						}
					}
					
					if (characteristicTypeId.equals(I_Constants.INFERRED) || characteristicTypeId.equals(I_Constants.ADDITIONALRELATION)){
						String inferedRelationshipId = "";
						I_Identify id = tf.getId(rel.getNid());
						if (id != null) {
							idParts = tf.getId(rel.getNid()).getVisibleIds(currenAceConfig.getViewPositionSetReadOnly(), 
									snomedIntId);
							if (idParts != null) {
								Object denotation = getLastCurrentVisibleId(idParts, currenAceConfig.getViewPositionSetReadOnly(), 
										RelAssertionType.INFERRED_THEN_STATED);
								if (denotation instanceof Long) {
									Long c = (Long) denotation;
									if (c != null)  inferedRelationshipId = c.toString();
								}
							}
						}
						
						if ((inferedRelationshipId==null || inferedRelationshipId.equals(""))){
							inferedRelationshipId=rel.getUUIDs().iterator().next().toString();						
						}
					
						String inferredAuthorName = tf.getConcept(rel.getAuthorNid()).getInitialText();
						WriteUtil.write(getConfig(), "Inferred Relationship : " + "\t" +  concept.getInitialText() + "\t" +  inferedRelationshipId + "\t" + effectiveTime + "\t" + inferredAuthorName);
						WriteUtil.write(getConfig(), "\r\n");
					}
				}
			}
			
			
		}catch (NullPointerException ne) {
			logger.error("NullPointerException: " + ne.getMessage());
			logger.error(" NullPointerException " + conceptid);
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			logger.error("IOExceptions: " + conceptid);
		} catch (Exception e) {
			logger.error("Exceptions in exportConcept: " + e.getMessage());
			logger.error("Exceptions in exportConcept: " +conceptid);
		}

	}



	

}
