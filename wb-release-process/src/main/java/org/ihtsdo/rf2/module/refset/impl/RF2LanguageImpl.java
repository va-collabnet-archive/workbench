package org.ihtsdo.rf2.module.refset.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.ihtsdo.rf2.module.constant.I_Constants;
import org.ihtsdo.rf2.module.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;
import org.ihtsdo.rf2.module.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2LanguageImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Language File Copyright: Copyright (c) 2010 Company: IHTSDO.
 *
 * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2LanguageImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2LanguageImpl.class);

	/** The lang refset id. */
	private int langRefsetId;

	/** The refset sct id. */
	private String refsetSCTId;

	/**
	 * Instantiates a new r f2 language impl.
	 *
	 * @param config the config
	 * @param langRefsetId the lang refset id
	 * @param refsetSCTId the refset sct id
	 */
	public RF2LanguageImpl(Config config, int langRefsetId, String refsetSCTId) {
		super(config);
		this.langRefsetId=langRefsetId;

		if (refsetSCTId==null || refsetSCTId.equals("")){
			try {
				refsetSCTId=getSCTId(getConfig(), UUID.fromString(getConfig().getRefsetUuid()));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		this.refsetSCTId=refsetSCTId;
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
	public void export(I_GetConceptData concept, String conceptid) throws IOException {
		String effectiveTime = "";
		String descriptionid = "";
		String active = "";
		UUID refsetuuid = null;
		int extensionStatusId = 0;
		String acceptabilityId = "";

		try {

			List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allStatuses, 
					descTypes, currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (logger.isDebugEnabled()) {
				logger.info("Concept : " + conceptid);
				logger.info("I_DescriptionVersioned Descriptions size :" + descriptions.size());
			}

			if (!descriptions.isEmpty() && descriptions.size() > 0) {

				if (logger.isDebugEnabled()) {
					logger.debug("!descs.isEmpty() && descs.size() > 0 :" + (!descriptions.isEmpty() && descriptions.size() > 0));
				}
				for (I_DescriptionTuple description: descriptions) {

					List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(
							description.getDescId(), true);
					I_ExtendByRef languageExtension=null;
					for (I_ExtendByRef extension : extensions) {
						if (extension.getRefsetId() == langRefsetId) {
							languageExtension = extension;
							break;
						}
					}
					if (languageExtension != null) {

						long lastVersion = Long.MIN_VALUE;
						I_ExtendByRefPartCid extensionPart=null;
						for (I_ExtendByRefVersion loopTuple : languageExtension.getTuples(allStatusSet,currenAceConfig.getViewPositionSetReadOnly(),
								Precedence.PATH,currenAceConfig.getConflictResolutionStrategy())) {

							if (loopTuple.getTime() >= lastVersion) {
								lastVersion = loopTuple.getTime();
								extensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
							}
						}
						if (extensionPart == null) {
							if (logger.isDebugEnabled()) {
								logger.debug("Language refset extension part not found!");
							}
						}else{
							effectiveTime = getDateFormat().format(new Date(extensionPart.getTime()));
							if (!(effectiveTime.compareTo(getConfig().getPreviousReleaseDate())>0) ||
									!(effectiveTime.compareTo(getConfig().getReleaseDate())<=0)){
								continue;
							}
							if (isComponentToPublish( extensionPart)){
								extensionStatusId = extensionPart.getStatusNid();
								int acceptabilityNid = extensionPart.getC1id();
								descriptionid = getDescriptionId(description.getDescId(), ExportUtil.getSnomedCorePathNid());

								String status = getStatusType(extensionStatusId);
								if (status.equals("0")){ 
									active = "1";
								} else if (status.equals("1")) {
									active = "0";
								} else {
									I_GetConceptData con=tf.getConcept(extensionStatusId);
									logger.error("unknown extensionStatusId =====>" + extensionStatusId + "con : " + con.toString());
									System.exit(0);
								}

								String descriptionstatus = getStatusType(description.getStatusNid());

								if (!(descriptionstatus.equals("0") 
										|| descriptionstatus.equals("6") 
										|| descriptionstatus.equals("8"))){
									if ((descriptionid==null || descriptionid.equals("")) && !active.equals("1")){
										continue;
									}else{
										//									Force member inactivation;
										active="0";
									}
									logger.error("Inactive description with active language refset member: " + description.getUUIDs().iterator().next().toString());

								}

								if (acceptabilityNid == preferredNid) { // preferred
									acceptabilityId = I_Constants.PREFERRED;
								} else if (acceptabilityNid == acceptableNid) { 
									acceptabilityId = I_Constants.ACCEPTABLE;
								} else {
									logger.error("unknown acceptabilityId =====>" + acceptabilityNid + "conceptid  =====>" + conceptid + " descriptionid ===>" + descriptionid);
								}

								if ((descriptionid==null || descriptionid.equals("")) && active.equals("1")){
									descriptionid=description.getUUIDs().iterator().next().toString();
								}

								if (descriptionid==null || descriptionid.equals("")){
									logger.error("Unplublished Retired Description of Lang Refset : " + description.getUUIDs().iterator().next().toString());
								}else {

									refsetuuid = extensionPart.getPrimUuid(); 

									int intModuleId=extensionPart.getModuleNid();
									String moduleId=getModuleSCTIDForStampNid(intModuleId);

									writeRF2TypeLine(refsetuuid, effectiveTime, active, moduleId, refsetSCTId, descriptionid, acceptabilityId);
								}
							}
						}
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


	/**
	 * Write r f2 type line.
	 *
	 * @param refsetuuid the refsetuuid
	 * @param langEffectiveTime the lang effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param refsetId the refset id
	 * @param descriptionid the descriptionid
	 * @param acceptabilityId the acceptability id
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeRF2TypeLine(UUID refsetuuid, String langEffectiveTime, String active, String moduleId, String refsetId, String descriptionid, String acceptabilityId) throws IOException {
		WriteUtil.write(getConfig(), refsetuuid + "\t" + langEffectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + descriptionid + "\t" + acceptabilityId);
		WriteUtil.write(getConfig(), "\r\n");
	}
}
