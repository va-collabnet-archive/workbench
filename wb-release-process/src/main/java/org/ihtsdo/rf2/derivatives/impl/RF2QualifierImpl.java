package org.ihtsdo.rf2.derivatives.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
 * Title: RF2QualifierImpl Iterating over all the concept in workbench and fetching all the components required by RF2 qualifers File Copyright: Copyright (c) 2010 Company:
 * IHTSDO
 * 
 * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2QualifierImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2QualifierImpl.class);

	public RF2QualifierImpl(Config config) {
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

		Date et = null;
		String conceptStatus = "";
		try {
			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allStatuses, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
				I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();
				
				conceptStatus = getStatusType(attributes.getStatusNid());
				et = new Date(attributes.getTime());
				// Before Jan 31, 2010, then conceptstatus 0 & 6 means current concept (Active)
				// After Jan 31, 2010 , then conceptstatus 0 means current but 6 means retired
				if (!conceptStatus.equals("0"))  {
					
					return;
				}
				HashMap<Integer, String> types = ExportUtil.getTypeForQualifiers();

				if (conceptid.contains("-")){
					conceptid=getSCTId(getConfig(), UUID.fromString(conceptid));
				}
				for (Integer order:types.keySet()){
					if (ExportUtil.testDomain(order, concept, currenAceConfig)){
						String line=ExportUtil.getQualifierRF1Row(order,  conceptid, getConfig());
						if (!line.equals("")){
							writeRF1TypeLine(line);
						}
					}
				}
			}
			
		}catch (NullPointerException ne) {
			logger.error("NullPointerException: " + ne.getMessage());
			logger.error("NullPointerException " + conceptid);
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			logger.error("IOExceptions: " + conceptid);
		} catch (Exception e) {
			logger.error("Exceptions in exportTextDefinition: " + e.getMessage());
			logger.error("Exceptions in exportTextDefinition: " +conceptid);
		}
		
	}
	

	public static void writeRF1TypeLine(String line) throws IOException {
		WriteUtil.write(getConfig(), line);
		WriteUtil.write(getConfig(), "\r\n");
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
