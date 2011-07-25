package org.ihtsdo.rf2.core.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
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
 * @author Varsha Parekh
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
		String moduleId = "";
		String active = "";
		String caseSignificanceId = "";
		String typeId = "";
		String languageCode = "en";
		try {

			List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allStatuses, 
					allDescTypes, currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			moduleId = getConceptMetaModuleID(concept , getConfig().getReleaseDate());
			for (I_DescriptionTuple description: descriptions) {
				String sDescType = getSnomedDescriptionType(description.getTypeNid());
				typeId = getTypeId(sDescType);

				Date descriptionEffectiveDate = new Date(getTermFactory().convertToThickVersion(description.getVersion()));
				effectiveTime = getDateFormat().format(descriptionEffectiveDate);
				if (!sDescType.equals("4") && !description.getLang().equals("es") && !effectiveTime.contains("1031") && !effectiveTime.contains("0430")) {
					descriptionid = getDescriptionId(description.getDescId(), ExportUtil.getSnomedCorePathNid());

					String term = description.getText();
					String descriptionstatus = getStatusType(description.getStatusNid());

					if (descriptionstatus.equals("0") || descriptionstatus.equals("6") || descriptionstatus.equals("8"))
						active = "1";
					else
						active = "0";

					if (description.isInitialCaseSignificant()) {
						caseSignificanceId = I_Constants.SENSITIVE_CASE;
					} else {
						caseSignificanceId = I_Constants.INITIAL_INSENSITIVE;
					}
					
					if(moduleId.equals(I_Constants.META_MOULE_ID)){
						//System.out.println(moduleId);
						incrementMetaDataCount();
					}

					if (conceptid==null || conceptid.equals("")){
						conceptid=concept.getUids().iterator().next().toString();
					}
					
					if (descriptionid==null || descriptionid.equals("")){
						descriptionid=description.getUUIDs().iterator().next().toString();
					}

					writeRF2TypeLine(descriptionid, effectiveTime, active, moduleId, conceptid, languageCode, typeId, term, caseSignificanceId);
				}
			}
		} catch (IOException e) {
			logger.error("Exceptions in exportDescription: " + e.getMessage());
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exceptions in exportDescription: " + e.getMessage());
			logger.error(conceptid);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void writeRF2TypeLine(String descriptionid, String effectiveTime, String active, String moduleId, String conceptid, String languageCode, String typeId, String term,
			String caseSignificanceId) throws IOException {
		WriteUtil.write(getConfig(), descriptionid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + conceptid + "\t" + languageCode + "\t" + typeId + "\t" + term + "\t"
				+ caseSignificanceId);
		WriteUtil.write(getConfig(), "\r\n");
	}

}
