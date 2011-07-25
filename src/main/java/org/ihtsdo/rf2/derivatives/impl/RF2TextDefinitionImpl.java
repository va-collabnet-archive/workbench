package org.ihtsdo.rf2.derivatives.impl;

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
import org.ihtsdo.tk.api.Precedence;

/**
 * Title: RF2TextDefinitionImpl Text Definition: Iterating over all the concept in workbench and fetching all the components required by RF2 TextDefinition File Copyright: Copyright (c) 2010 Company:
 * IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2TextDefinitionImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2TextDefinitionImpl.class);

	public RF2TextDefinitionImpl(Config config) {
		super(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api. I_GetConceptData)
	 */

	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}


	public void export(I_GetConceptData concept, String conceptid) throws IOException {

		String effectiveTime = "";
		String descriptionid = "";
		String active = "";
		String caseSignificanceId = "";
		String typeId = "";
		String moduleId="";
		try {
			
			List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allStatuses, 
					textDefinTypes, currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			for (I_DescriptionTuple description: descriptions) {
				String sDescType = getSnomedDescriptionType(description.getTypeNid());
				typeId = getTypeId(sDescType);

				if (sDescType.equals("4")) {
					descriptionid = getDescriptionId(description.getDescId(), ExportUtil.getSnomedCorePathNid());
					typeId = I_Constants.DEFINITION;
					String languageCode = description.getLang(); // This should be always "en"
					String term = description.getText();
					String textDefstatus = getStatusType(description.getStatusNid());
					if (description.isInitialCaseSignificant()) {
						caseSignificanceId = I_Constants.SENSITIVE_CASE;
					} else {
						caseSignificanceId = I_Constants.INITIAL_INSENSITIVE;
					}

					Date textDefEffectiveDate = new Date(getTermFactory().convertToThickVersion(description.getVersion()));
					effectiveTime = getDateFormat().format(textDefEffectiveDate);
					if (textDefstatus.equals("0") || textDefstatus.equals("6") || textDefstatus.equals("8"))
						active = "1";
					else
						active = "0";
					
					moduleId = getConceptMetaModuleID(concept , effectiveTime);
					
					if (conceptid==null || conceptid.equals("")){
						conceptid=concept.getUids().iterator().next().toString();
					}
					if (descriptionid==null || descriptionid.equals("")){
						descriptionid=description.getUUIDs().iterator().next().toString();
					}
					getConfig().getBw().write(
							descriptionid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + conceptid + "\t" + languageCode + "\t" + typeId + "\t" + term + "\t"
							+ caseSignificanceId);
					getConfig().getBw().write("\r\n");

				}
			}
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exceptions in exporttextDef: " + e.getMessage());
		}
	}

}
