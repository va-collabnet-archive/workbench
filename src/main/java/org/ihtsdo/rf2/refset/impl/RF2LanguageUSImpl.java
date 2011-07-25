package org.ihtsdo.rf2.refset.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.refset.dao.RefsetConceptDAO;
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

public class RF2LanguageUSImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2LanguageUSImpl.class);
	private int langRefsetId;
	
	public RF2LanguageUSImpl(Config config) {
		super(config);
	}
	
	public RF2LanguageUSImpl(Config config, int langRefsetId) {
		super(config);
		this.langRefsetId=langRefsetId;
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
	public void export(I_GetConceptData concept, String conceptid) throws IOException {
		// String effectiveTime = "";
		String langEffectiveTime = "";
		String descriptionid = "";
		String active = "";
		UUID refsetuuid = null;
		int extensionStatusId = 0;
		String acceptabilityId = "";

		try {
			String refsetId = I_Constants.US_LANG_REFSET_ID;		
			String moduleId = I_Constants.CORE_MODULE_ID;
			int languageRefsetNid_Term_Aux = getNid(I_Constants.USLANG_REFSET_UID_TERM_AUX);
			int languageRefsetNid = getNid("bca0a686-3516-3daf-8fcf-fe396d13cfad");
		
			List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allStatuses, 
					descTypes, currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (logger.isDebugEnabled()) {
				logger.debug("Concept : " + conceptid);
				logger.debug("I_DescriptionVersioned Descriptions size :" + descriptions.size());
			}

			if (!descriptions.isEmpty() && descriptions.size() > 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("!descs.isEmpty() && descs.size() > 0 :" + (!descriptions.isEmpty() && descriptions.size() > 0));
				}
				for (I_DescriptionTuple description: descriptions) {
					String sDescType = getSnomedDescriptionType(description.getTypeNid());
					String lang = description.getLang();
					if (!sDescType.equals("4") && !lang.equals("es") && !lang.equals("en-GB")) {
						List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(
								description.getDescId(), true);
						I_ExtendByRef languageExtension=null;
						for (I_ExtendByRef extension : extensions) {
							if ((extension.getRefsetId() == languageRefsetNid) || (extension.getRefsetId() == languageRefsetNid_Term_Aux)) {								
							//if (extension.getRefsetId() == langRefsetId) {
								languageExtension = extension;
								break;
							}
						}
						if (languageExtension != null) {

							long lastVersion = Long.MIN_VALUE;
							I_ExtendByRefPartCid languageExtensionPart=null;
							for (I_ExtendByRefVersion loopTuple : languageExtension.getTuples(allStatusSet,currenAceConfig.getViewPositionSetReadOnly(),
									Precedence.PATH,currenAceConfig.getConflictResolutionStrategy())) {

								if (loopTuple.getTime() >= lastVersion) {
									lastVersion = loopTuple.getTime();
									languageExtensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
								}
							}
							if (languageExtensionPart == null) {
								if (logger.isDebugEnabled()) {
									logger.debug("Language refset extension part not found!");
								}
							}else{

								extensionStatusId = languageExtensionPart.getStatusNid();
								int acceptabilityNid = languageExtensionPart.getC1id();
								descriptionid = getDescriptionId(description.getDescId(), ExportUtil.getSnomedCorePathNid());

								if (extensionStatusId == retiredNid) { // retired
									active = "0";
								} else if (extensionStatusId == currentNid) { // current
									active = "1";
								} else if (extensionStatusId == activeNid) { // active													
									active = "1";
								} else if (extensionStatusId == inactiveNid) { // inactive													
									active = "0";								
								} else {
									System.out.println("unknown extensionStatusId =====>" + extensionStatusId);
									logger.error("unknown extensionStatusId =====>" + extensionStatusId);
									//System.exit(0);
								}
								
								if (logger.isDebugEnabled()) {
									logger.debug("extensionStatusId :" + extensionStatusId + "active : " + active);
								}

								if (acceptabilityNid == preferredNid) { // preferred
									acceptabilityId = I_Constants.PREFERRED;
								} else if (acceptabilityNid == acceptableNid) { // fully
									acceptabilityId = I_Constants.ACCEPTABLE;
								} else {
									logger.error("unknown acceptabilityId =====>" + acceptabilityNid + "conceptis  =====>" + conceptid + " descriptionid ===>" + descriptionid);
									// System.exit(0);
								}

								if (descriptionid==null || descriptionid.equals("")){
									descriptionid=description.getUUIDs().iterator().next().toString();
								}
								refsetuuid = Type5UuidFactory.get(refsetId + descriptionid);
								Date langEffectiveDate = new Date(languageExtensionPart.getTime());
								langEffectiveTime = getDateFormat().format(langEffectiveDate);

								writeRF2TypeLine(refsetuuid, langEffectiveTime, active, moduleId, refsetId, descriptionid, acceptabilityId);
							}

						} // end desctype != lanf != es lang != un-GB

					}
				}
			}
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exceptions in exportDescription: " + e.getMessage());
			logger.error(conceptid);
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void writeRF2TypeLine(UUID refsetuuid, String langEffectiveTime, String active, String moduleId, String refsetId, String descriptionid, String acceptabilityId) throws IOException {
		WriteUtil.write(getConfig(), refsetuuid + "\t" + langEffectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + descriptionid + "\t" + acceptabilityId);
		WriteUtil.write(getConfig(), "\r\n");
	}
}
