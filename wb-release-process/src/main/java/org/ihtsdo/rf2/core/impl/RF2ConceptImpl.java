package org.ihtsdo.rf2.core.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

/**
 * Title: RF2ConceptImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Concept File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2ConceptImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2ConceptImpl.class);

	public RF2ConceptImpl(Config config) {
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
		String conceptStatus = "";
		String active = "";
		String moduleId = I_Constants.CORE_MODULE_ID;
		String definitionStatusId = "";
		String updateWbSctId = "false";

		try {
			if(!getConfig().isUpdateWbSctId().equals(null)){
				updateWbSctId = getConfig().isUpdateWbSctId();
			}

			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allStatuses, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
				I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();

				String authorName = tf.getConcept(attributes.getAuthorNid()).getInitialText();

				if (attributes.isDefined()) {
					definitionStatusId = I_Constants.FULLY_DEFINED;
				} else {
					definitionStatusId = I_Constants.PRIMITIVE;
				}
				conceptStatus = getStatusType(attributes.getStatusNid());
				et = new Date(attributes.getTime());
				effectiveTime = getConfig().getReleaseDate();
				// Before Jan 31, 2010, then conceptstatus 0 & 6 means current concept (Active)
				// After Jan 31, 2010 , then conceptstatus 0 means current but 6 means retired
				if (conceptStatus.equals("0")) {
					active = "1";
				} else if (getConfig().getReleaseDate().compareTo(I_Constants.limited_policy_change)<0 && conceptStatus.equals("6")) {
					active = "1";
				} else {
					active = "0";
				}

				if ((conceptid==null || conceptid.equals("") || conceptid.equals("0"))){
					conceptid=concept.getUUIDs().iterator().next().toString();
				}

				if(active.equals("1")){
					//moduleId = getConceptMetaModuleID(concept , getConfig().getReleaseDate());
					//	moduleId = computeModuleId(concept);					
					//					if(moduleId.equals(I_Constants.META_MODULE_ID)){
					//						//logger.info("==Meta Concept==" + conceptid + " & Name : " + concept.getInitialText());
					//						incrementMetaDataCount();
					//					}
					String[]moduleNspId;
					moduleNspId=getModule(concept);
					String subsOrigId=null;
					if (moduleNspId==null){
						subsOrigId=getSubsetOrigId(concept);
						if (subsOrigId==null){
							moduleNspId=new String[]{"999000011000000103","1000000"};
						}else{
							moduleNspId=getModuleForSubsOrigId(subsOrigId);
						}
					}
					if (moduleNspId!=null){
						moduleId=moduleNspId[0];
						String namespaceId=moduleNspId[1];
						UUID componentUuid=concept.getUids().iterator().next();
						conceptid= getSCTId(getConfig(), componentUuid, Integer.parseInt(namespaceId), getConfig().getPartitionId(), getConfig().getReleaseDate(), getConfig().getExecutionId(), moduleId);
					}
					if (subsOrigId!=null){
						getModSubsMapFile().append(subsOrigId);
						getModSubsMapFile().append("\t");
						getModSubsMapFile().append(conceptid);
						getModSubsMapFile().append("\r\n");
					}
				}

				if(getConfig().getRf2Format().equals("false") ){
					writeRF2TypeLine(conceptid, effectiveTime, active, moduleId, definitionStatusId, authorName);
				}else{
					writeRF2TypeLine(conceptid, effectiveTime, active, moduleId, definitionStatusId);
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


	public static void writeRF2TypeLine(String conceptid, String effectiveTime, String active, String moduleId, String definitionStatusId) throws IOException {
		WriteUtil.write(getConfig(), conceptid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + definitionStatusId);
		WriteUtil.write(getConfig(), "\r\n");
	}

	public static void writeRF2TypeLine(String conceptid, String effectiveTime, String active, String moduleId, String definitionStatusId , String authorName) throws IOException {
		WriteUtil.write(getConfig(), conceptid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + definitionStatusId + "\t" + authorName);
		WriteUtil.write(getConfig(), "\r\n");
	}


}
