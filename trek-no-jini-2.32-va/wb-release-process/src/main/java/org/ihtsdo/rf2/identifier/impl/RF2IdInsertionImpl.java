package org.ihtsdo.rf2.identifier.impl;

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
 * Title: RF2InsertIdentifierImpl Description: Inserting Identifier in the workbench by fetching newly created all the components Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2IdInsertionImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2IdInsertionImpl.class);

	public RF2IdInsertionImpl(Config config) {
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
		try {	
			
			//********Inserting Concept Identifier**********//
			String updateWbSctId = "false"; //default value
			boolean insertConceptId=false;
			boolean insertCtv3Id=false;
			boolean insertSnomedId=false;
			boolean insertDescriptionId = false;
			boolean insertStatedRelationshipId = false;
			boolean insertInferedRelationshipId = false;
			
			if(!getConfig().isUpdateWbSctId().equals(null)){
				updateWbSctId = getConfig().isUpdateWbSctId();
			}
				
			if(updateWbSctId.equals("true")){
				List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
						allStatuses, 
						currenAceConfig.getViewPositionSetReadOnly(), 
						Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

				if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
					I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();
					if ((conceptid==null || conceptid.equals("") || conceptid.equals("0"))){
						conceptid=concept.getUUIDs().iterator().next().toString();
					}
					//Sctid doesn't exist
					if (conceptid.contains("-")){
						
						//DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
						//DateFormat df = new SimpleDateFormat("yyyyMMdd");
						//long effectiveDate=df.parse(getConfig().getReleaseDate()).getTime();
						
						//get conceptId by calling webservice 
						String wsConceptId = getConceptId(getConfig(), UUID.fromString(conceptid));
						//insert conceptId in the workbench database 
						insertConceptId = insertSctId(concept.getNid() , getConfig(), wsConceptId , attributes.getPathNid() , attributes.getStatusNid());
						//get ctv3Id by calling webservice 
						String wsCtv3Id = getCTV3ID(getConfig(), UUID.fromString(conceptid));
						//insert ctv3id if conceptId inserted Successfully
						insertCtv3Id = insertCtv3Id(concept.getNid() , getConfig(), wsCtv3Id , attributes.getPathNid() , attributes.getStatusNid());
							
						//get snomedId by calling webservice 
						String parentSnomedId = getParentSnomedId(concept);
						String wsSnomedId = getSNOMEDID(getConfig(), UUID.fromString(conceptid), parentSnomedId);
							
						//insert snomedid if conceptId & Ctv3Id inserted Successfully
						insertSnomedId = insertSnomedId(concept.getNid() , getConfig(), wsSnomedId , attributes.getPathNid() , attributes.getStatusNid());
							
					}
				}
				
				
				
				
				
				
				
				
				
				//********Inserting Description and Text-definition Identifier**********//
				List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allStatuses, 
						allDescTypes, currenAceConfig.getViewPositionSetReadOnly(), 
						Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());
				
				String active = "";
				
				for (I_DescriptionTuple description: descriptions) {
					
					String sDescType = getSnomedDescriptionType(description.getTypeNid());
					Date descriptionEffectiveDate = new Date(getTermFactory().convertToThickVersion(description.getVersion()));
			
					if (!description.getLang().equals("es")) {
						String descriptionstatus = getStatusType(description.getStatusNid());
						
						if (descriptionstatus.equals("0") || descriptionstatus.equals("6") || descriptionstatus.equals("8"))
							active = "1";
						else
							active = "0";
						
						String descriptionid = getDescriptionId(description.getDescId(), ExportUtil.getSnomedCorePathNid());
						
						//Newly created active description
						if ((descriptionid==null || descriptionid.equals("") || descriptionid.equals("0")) && active.equals("1")){
							
							descriptionid=description.getUUIDs().iterator().next().toString();
							if (descriptionid.contains("-") && updateWbSctId.equals("true")){
								//pass proper values from getConfig()
								
								//get descriptionId by calling web service 
								String wsDescriptionId = getDescriptionId(getConfig(), UUID.fromString(descriptionid));
														
								//insert descriptionId in the workbench database 
								insertDescriptionId = insertSctId(description.getDescId() , getConfig(), wsDescriptionId , description.getPathNid() , description.getStatusNid());
							}
						}
					}
				}
				
				
				
				
				//********Inserting Inferred Relationship and Stated-Relationship Identifier**********//
				
				List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(allStatuses, null, 
						currenAceConfig.getViewPositionSetReadOnly(), 
						Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());
		
				for (I_RelTuple rel : relationships) {
					
						String relationshipId = "";
						if (rel.getStatusNid() == activeNid) { 														
							active = "1";
						} else if (rel.getStatusNid() == inactiveNid) { 														
							active = "0";
						}
						
						I_Identify id = tf.getId(rel.getNid());
						//Newly Created Inferred Relationship needs identifier
						if (id != null) {
							List<? extends I_IdPart> idParts = tf.getId(rel.getNid()).getVisibleIds(currenAceConfig.getViewPositionSetReadOnly(), 
									snomedIntId);
							if (idParts != null) {
								Object denotation = getLastCurrentVisibleId(idParts, currenAceConfig.getViewPositionSetReadOnly(), 
										RelAssertionType.INFERRED);
										//RelAssertionType.INFERRED_THEN_STATED);
								if (denotation instanceof Long) {
									Long c = (Long) denotation;
									if (c != null)  relationshipId = c.toString();
								}
							}
						}
						
						
						if ((relationshipId==null || relationshipId.equals("")) && active.equals("1")){
							relationshipId=rel.getUUIDs().iterator().next().toString();
							if (relationshipId.contains("-")){
								//get relationshipId by calling web-service 
								String wbSctId = getRelationshipId(getConfig(), UUID.fromString(relationshipId));
							
								//insert relationshipId in the workbench database 
								insertInferedRelationshipId = insertSctId(rel.getNid() , getConfig(), wbSctId , rel.getPathNid() , rel.getStatusNid());
							}
						}
						
						//Newly Created Stated Relationship needs identifier
						if (id != null) {
							List<? extends I_IdPart> idParts = tf.getId(rel.getNid()).getVisibleIds(currenAceConfig.getViewPositionSetReadOnly(), 
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
						
						
						if ((relationshipId==null || relationshipId.equals("")) && active.equals("1")){
							
							relationshipId=rel.getUUIDs().iterator().next().toString();
							if (relationshipId.contains("-")){								
								//get relationshipId by calling web-service 
								String wbSctId = getRelationshipId(getConfig(), UUID.fromString(relationshipId));
							
								//insert relationshipId in the workbench database 
								insertStatedRelationshipId = insertSctId(rel.getNid() , getConfig(), wbSctId , rel.getPathNid() , rel.getStatusNid());
							}
						}
					}
				
				
				//Relationship id insertion needs to handle by different mojo
				if((insertCtv3Id && insertConceptId && insertSnomedId) ||
					insertDescriptionId ||
					insertInferedRelationshipId ||
					insertStatedRelationshipId){
					logger.info("===============ChangeSet Creation Started============");
					setupProfile(getConfig());
					getTermFactory().commit();
			        //Ts.get().removeChangeSetGenerator(tempKey);
			        logger.info("===============ChangeSet Creation Finished============");
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
