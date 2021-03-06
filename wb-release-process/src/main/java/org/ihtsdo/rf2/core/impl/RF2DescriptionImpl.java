package org.ihtsdo.rf2.core.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

/**
 * Title: RF2DescriptionImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Description File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2DescriptionImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2DescriptionImpl.class);

	public RF2DescriptionImpl(Config config) {
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
		String descriptionid = "";
		String active = "";
		String caseSignificanceId = "";
		String typeId = "";
		String languageCode = "en";
		String updateWbSctId = "false";
		try {
			if(!getConfig().isUpdateWbSctId().equals(null)){
				updateWbSctId = getConfig().isUpdateWbSctId();
			}

			List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allStatuses, 
					allDescTypes, currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());


			for (I_DescriptionTuple description: descriptions) {

				String moduleId=I_Constants.CORE_MODULE_ID;
				String sDescType = getSnomedDescriptionType(description.getTypeNid());
				typeId = getTypeId(sDescType);

				Date descriptionEffectiveDate = new Date(getTermFactory().convertToThickVersion(description.getVersion()));
				effectiveTime = getDateFormat().format(descriptionEffectiveDate);

				if (!sDescType.equals("4") && !description.getLang().equals("es")) { 
					//&& !effectiveTime.contains("1031") && !effectiveTime.contains("0430")) {
					descriptionid = getDescriptionId(description.getDescId(), ExportUtil.getSnomedCorePathNid());

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
						term=StringEscapeUtils.unescapeHtml(term);

					}
					String descriptionstatus = getStatusType(description.getStatusNid());

					String authorName = tf.getConcept(description.getAuthorNid()).getInitialText();


					if (descriptionstatus.equals("0") || descriptionstatus.equals("6") || descriptionstatus.equals("8"))
						active = "1";
					else
						active = "0";

					if (description.isInitialCaseSignificant()) {
						caseSignificanceId = I_Constants.SENSITIVE_CASE;
					} else {
						caseSignificanceId = I_Constants.INITIAL_INSENSITIVE;
					}
					if (active.equals("1")){
						List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
								allStatuses, 
								currenAceConfig.getViewPositionSetReadOnly(), 
								Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

						if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
							I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();

							String conceptStatus = getStatusType(attributes.getStatusNid());
							// Before Jan 31, 2010, then conceptstatus 0 & 6 means current concept (Active)
							// After Jan 31, 2010 , then conceptstatus 0 means current but 6 means retired
							String conceptActive;
							if (conceptStatus.equals("0")) {
								conceptActive = "1";
							} else if (getConfig().getReleaseDate().compareTo(I_Constants.limited_policy_change)<0 && conceptStatus.equals("6")) {
								conceptActive = "1";
							} else {
								conceptActive = "0";
							}
							if(conceptActive.equals("1")){
								moduleId = computeModuleId(concept);	
							}
						}
					}
					if (conceptid==null || conceptid.equals("") || conceptid.equals("0")){
						conceptid=concept.getUids().iterator().next().toString();
					}

					if ((descriptionid==null || descriptionid.equals("") || descriptionid.equals("0")) && active.equals("1")){
						descriptionid=description.getUUIDs().iterator().next().toString();
					}					

					if (descriptionid==null || descriptionid.equals("") || descriptionid.equals("0")){
						logger.info("Unplublished Retired Description: " + description.getUUIDs().iterator().next().toString());
					}else if(getConfig().getRf2Format().equals("false") ){
						writeRF2TypeLine(descriptionid, effectiveTime, active, moduleId, conceptid, languageCode, typeId, term, caseSignificanceId, authorName);
					}else{
						writeRF2TypeLine(descriptionid, effectiveTime, active, moduleId, conceptid, languageCode, typeId, term, caseSignificanceId);
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
			logger.error("Exceptions in exportDescription: " + e.getMessage());
			logger.error("Exceptions in exportDescription: " +conceptid);
		}
	}

	public static void writeRF2TypeLine(String descriptionid, String effectiveTime, String active, String moduleId, String conceptid, String languageCode, String typeId, String term,
			String caseSignificanceId) throws IOException {
		WriteUtil.write(getConfig(), descriptionid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + conceptid + "\t" + languageCode + "\t" + typeId + "\t" + term + "\t"
				+ caseSignificanceId);
		WriteUtil.write(getConfig(), "\r\n");
	}

	public static void writeRF2TypeLine(String descriptionid, String effectiveTime, String active, String moduleId, String conceptid, String languageCode, String typeId, String term,
			String caseSignificanceId, String authorName) throws IOException {
		WriteUtil.write(getConfig(), descriptionid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + conceptid + "\t" + languageCode + "\t" + typeId + "\t" + term + "\t"
				+ caseSignificanceId + "\t"	+ authorName);
		WriteUtil.write(getConfig(), "\r\n");
	}

}
