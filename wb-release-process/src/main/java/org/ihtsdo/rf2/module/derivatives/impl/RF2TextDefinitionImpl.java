package org.ihtsdo.rf2.module.derivatives.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.ihtsdo.rf2.module.constant.I_Constants;
import org.ihtsdo.rf2.module.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;
import org.ihtsdo.rf2.module.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2TextDefinitionImpl Text Definition: Iterating over all the concept in workbench and fetching all the components required by RF2 TextDefinition File Copyright: Copyright (c) 2010 Company:
 * IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2TextDefinitionImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2TextDefinitionImpl.class);

	/**
	 * Instantiates a new r f2 text definition impl.
	 *
	 * @param config the config
	 */
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


	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.impl.RF2AbstractImpl#export(org.dwfa.ace.api.I_GetConceptData, java.lang.String)
	 */
	public void export(I_GetConceptData concept, String conceptid) throws IOException {

		String effectiveTime = "";
		String descriptionid = "";
		String active = "";
		String caseSignificanceId = "";
		String typeId = "";
		String moduleId="";

		String languageCode = getConfig().getLanguageCode();
		try {
			if (languageCode==null){
				languageCode="en";
			}
			List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allStatuses, 
					textDefinTypes, currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			for (I_DescriptionTuple description: descriptions) {
				
				String sDescType = getSnomedDescriptionType(description.getTypeNid());
				typeId = getTypeId(sDescType);

				if (sDescType.equals("4") 
						&& description.getLang().equals(languageCode) 
						&& isComponentToPublish( description.getMutablePart())){

					Date textDefEffectiveDate = new Date(getTermFactory().convertToThickVersion(description.getVersion()));
					effectiveTime = getDateFormat().format(textDefEffectiveDate);
					if (!(effectiveTime.compareTo(getConfig().getPreviousReleaseDate())>0) ||
							!(effectiveTime.compareTo(getConfig().getReleaseDate())<=0)){
						continue;
					}
					descriptionid = getDescriptionId(description.getDescId(), ExportUtil.getSnomedCorePathNid());
					typeId = I_Constants.DEFINITION;
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
					String textDefstatus = getStatusType(description.getStatusNid());
					if (description.isInitialCaseSignificant()) {
						caseSignificanceId = I_Constants.SENSITIVE_CASE;
					} else {
						caseSignificanceId = I_Constants.INITIAL_INSENSITIVE;
					}
					if (textDefstatus.equals("0") || textDefstatus.equals("6") || textDefstatus.equals("8"))
						active = "1";
					else
						active = "0";
					
					moduleId = computeModuleId(concept);	
					if(moduleId.equals(I_Constants.META_MODULE_ID)){		
						logger.info("==Meta Concept==" + conceptid + " & Name : " + concept.getInitialText());
						incrementMetaDataCount();
					}
					
					if (conceptid==null || conceptid.equals("") || conceptid.equals("0") ){
						conceptid=concept.getUids().iterator().next().toString();
					}
					
					if ((descriptionid==null || descriptionid.equals("") || descriptionid.equals("0")) && active.equals("1")){
						descriptionid=description.getUUIDs().iterator().next().toString();
					}
					
					String authorName = tf.getConcept(description.getAuthorNid()).getInitialText();
					
					if (descriptionid==null || descriptionid.equals("") || descriptionid.equals("0")){
						logger.info("Unplublished Retired Text-definition: " + description.getUUIDs().iterator().next().toString());
					}else if(getConfig().getRf2Format().equals("false") ){
						writeRF2TypeLine(descriptionid, effectiveTime, active, moduleId, conceptid, languageCode, typeId, term, caseSignificanceId, authorName);
					}else{
						writeRF2TypeLine(descriptionid, effectiveTime, active, moduleId, conceptid, languageCode, typeId, term, caseSignificanceId);
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
	
	
	/**
	 * Write r f2 type line.
	 *
	 * @param descriptionid the descriptionid
	 * @param effectiveTime the effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param conceptid the conceptid
	 * @param languageCode the language code
	 * @param typeId the type id
	 * @param term the term
	 * @param caseSignificanceId the case significance id
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeRF2TypeLine(String descriptionid, String effectiveTime, String active, String moduleId, String conceptid, String languageCode, String typeId, String term,
			String caseSignificanceId) throws IOException {
		WriteUtil.write(getConfig(), descriptionid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + conceptid + "\t" + languageCode + "\t" + typeId + "\t" + term + "\t"
				+ caseSignificanceId);
		WriteUtil.write(getConfig(), "\r\n");
	}
	
	/**
	 * Write r f2 type line.
	 *
	 * @param descriptionid the descriptionid
	 * @param effectiveTime the effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param conceptid the conceptid
	 * @param languageCode the language code
	 * @param typeId the type id
	 * @param term the term
	 * @param caseSignificanceId the case significance id
	 * @param authorName the author name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeRF2TypeLine(String descriptionid, String effectiveTime, String active, String moduleId, String conceptid, String languageCode, String typeId, String term,
			String caseSignificanceId, String authorName) throws IOException {
		WriteUtil.write(getConfig(), descriptionid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + conceptid + "\t" + languageCode + "\t" + typeId + "\t" + term + "\t"
				+ caseSignificanceId + "\t"	+ authorName);
		WriteUtil.write(getConfig(), "\r\n");
	}
}
