package org.ihtsdo.rf2.core.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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
		String moduleId = "";
		String definitionStatusId = "";

		try {

			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allStatuses, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
				I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();
				
				if (attributes.isDefined()) {
					definitionStatusId = I_Constants.FULLY_DEFINED;
				} else {
					definitionStatusId = I_Constants.PRIMITIVE;
				}
				conceptStatus = getStatusType(attributes.getStatusNid());
				et = new Date(attributes.getTime());
				effectiveTime = getDateFormat().format(et);
				// Before Jan 31, 2010, then conceptstatus 0 & 6 means current concept (Active)
				// After Jan 31, 2010 , then conceptstatus 0 means current but 6 means retired
				if (conceptStatus.equals("0")) {
					active = "1";
				} else if (getConfig().getReleaseDate().compareTo(I_Constants.limited_policy_change)<0 && conceptStatus.equals("6")) {
					active = "1";
				} else {
					active = "0";
				}
				moduleId = getConceptMetaModuleID(concept , getConfig().getReleaseDate());
				if(moduleId.equals(I_Constants.META_MOULE_ID)){
					incrementMetaDataCount();
				}
				if (conceptid==null || conceptid.equals("")){
					conceptid=concept.getUids().iterator().next().toString();
				}
				
				writeRF2TypeLine(conceptid, effectiveTime, active, moduleId, definitionStatusId);
			}
		} catch (IOException e) {
			logger.error("conceptid : " + conceptid);
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("conceptid : " + conceptid);
			logger.error("Exceptions in exportConcept: " + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}

	}


	public static void writeRF2TypeLine(String conceptid, String effectiveTime, String active, String moduleId, String definitionStatusId) throws IOException {
		WriteUtil.write(getConfig(), conceptid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + definitionStatusId);
		WriteUtil.write(getConfig(), "\r\n");
	}


}
