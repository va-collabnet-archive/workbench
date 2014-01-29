package org.ihtsdo.rf2.module.core.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.ihtsdo.rf2.module.constant.I_Constants;
import org.ihtsdo.rf2.module.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2ConceptImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Concept File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2ConceptImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2ConceptImpl.class);

	/**
	 * Instantiates a new r f2 concept impl.
	 *
	 * @param config the config
	 */
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

	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.impl.RF2AbstractImpl#export(org.dwfa.ace.api.I_GetConceptData, java.lang.String)
	 */
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
				conceptStatus = getStatusType(attributes.getStatusNid());
				et = new Date(attributes.getTime());
				effectiveTime = getDateFormat().format(et);
				if (!(effectiveTime.compareTo(getConfig().getPreviousReleaseDate())>0) ||
						!(effectiveTime.compareTo(getConfig().getReleaseDate())<=0)){
					return;
				}
				if (isComponentToPublish( attributes.getMutablePart())){
					String authorName = tf.getConcept(attributes.getAuthorNid()).getInitialText();

					if (attributes.isDefined()) {
						definitionStatusId = I_Constants.FULLY_DEFINED;
					} else {
						definitionStatusId = I_Constants.PRIMITIVE;
					}
					conceptStatus = getStatusType(attributes.getStatusNid());
					et = new Date(attributes.getTime());
					effectiveTime = getDateFormat().format(et);
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

					int intModuleId=attributes.getModuleNid();
					moduleId=getModuleSCTIDForStampNid(intModuleId);

					if(getConfig().getRf2Format().equals("false") ){
						writeRF2TypeLine(conceptid, effectiveTime, active, moduleId, definitionStatusId, authorName);
					}else{
						writeRF2TypeLine(conceptid, effectiveTime, active, moduleId, definitionStatusId);
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


	/**
	 * Write r f2 type line.
	 *
	 * @param conceptid the conceptid
	 * @param effectiveTime the effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param definitionStatusId the definition status id
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeRF2TypeLine(String conceptid, String effectiveTime, String active, String moduleId, String definitionStatusId) throws IOException {
		WriteUtil.write(getConfig(), conceptid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + definitionStatusId);
		WriteUtil.write(getConfig(), "\r\n");
	}

	/**
	 * Write r f2 type line.
	 *
	 * @param conceptid the conceptid
	 * @param effectiveTime the effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param definitionStatusId the definition status id
	 * @param authorName the author name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeRF2TypeLine(String conceptid, String effectiveTime, String active, String moduleId, String definitionStatusId , String authorName) throws IOException {
		WriteUtil.write(getConfig(), conceptid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + definitionStatusId + "\t" + authorName);
		WriteUtil.write(getConfig(), "\r\n");
	}


}
