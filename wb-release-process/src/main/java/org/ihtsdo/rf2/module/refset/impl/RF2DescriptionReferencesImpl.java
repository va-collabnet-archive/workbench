package org.ihtsdo.rf2.module.refset.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.ihtsdo.rf2.model.Refset;
import org.ihtsdo.rf2.module.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;
import org.ihtsdo.rf2.module.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2DescriptionImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Description File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2DescriptionReferencesImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2DescriptionReferencesImpl.class);

	/** The refset. */
	private static Refset refset;

	/**
	 * Instantiates a new r f2 description references impl.
	 *
	 * @param config the config
	 */
	public RF2DescriptionReferencesImpl(Config config) {
		super(config);
	}

	/**
	 * Instantiates a new r f2 description references impl.
	 *
	 * @param config the config
	 * @param refset the refset
	 */
	public RF2DescriptionReferencesImpl(Config config, Refset refset) {
		super(config);
		RF2DescriptionReferencesImpl.refset = refset;
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
	public void export(I_GetConceptData concept, String referencedComponentId) throws IOException {
		String descriptionid = "";
		String active = "";
		int extensionStatusId = 0;

		String effectiveTime = "";
		UUID uuid = null;
		String targetComponent = "";

		try {
			String refsetId = getRefset().getId();
			//int refsetTermAuxId = getNid(getRefset().getTermAuxUID());
			int refsetNid = getNid(getRefset().getUID());		
			String moduleId = "";
			List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allStatuses, 
					allDescTypes, currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (logger.isDebugEnabled()) {
				logger.debug("Concept : " + concept);
				logger.debug("I_DescriptionVersioned Descriptions size :" + descriptions.size());
			}

			if (!descriptions.isEmpty() && descriptions.size() > 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("!descs.isEmpty() && descs.size() > 0 :" + (!descriptions.isEmpty() && descriptions.size() > 0));
				}
				for (I_DescriptionTuple description: descriptions) {
					String sDescType = getSnomedDescriptionType(description.getTypeNid());
					String lang = description.getLang();
					if (!sDescType.equals("4") && !lang.equals("es")) {
						List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(
								description.getDescId(), true);
						for (I_ExtendByRef extension : extensions) {
							if (extension.getRefsetId() == refsetNid) {
								long lastVersion = Long.MIN_VALUE;
								I_ExtendByRefPartCid extensionPart=null;
								for (I_ExtendByRefVersion loopTuple : extension.getTuples(allStatusSet,currenAceConfig.getViewPositionSetReadOnly(),
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
										descriptionid = getDescriptionId(description.getDescId(), ExportUtil.getSnomedCorePathNid());

										if (descriptionid==null || descriptionid.equals("") || descriptionid.equals("0")){
											continue;
										}
										if (extensionStatusId == activeNid) { 														
											active = "1";
										} else if (extensionStatusId == inactiveNid) { 														
											active = "0";								
										} else {
											System.out.println("unknown extensionStatusId =====>" + extensionStatusId);
											logger.error("unknown extensionStatusId =====>" + extensionStatusId);
											//System.exit(0);
										}

										if (logger.isDebugEnabled()) {
											logger.debug("extensionStatusId :" + extensionStatusId + "active : " + active);
										}

										int targetComponentNid = extensionPart.getC1id();
										targetComponent = getSctId(targetComponentNid);
										uuid = extensionPart.getPrimUuid();

										if (descriptionid==null || descriptionid.equals("")){
											descriptionid=description.getUUIDs().iterator().next().toString();
										}
										if (targetComponent==null || targetComponent.equals("")){
											targetComponent=Terms.get().getUids(targetComponentNid).iterator().next().toString();
										}
//										int intModuleId=description.getModuleNid();
//										moduleId=getModuleSCTIDForStampNid(intModuleId);
										moduleId = computeModuleId(concept);
										writeRF2TypeLine(uuid, "", active, moduleId, refsetId, descriptionid, targetComponent);
										break;
									}
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
			logger.error(concept);
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Write r f2 type line.
	 *
	 * @param refsetuuid the refsetuuid
	 * @param effectiveTime the effective time
	 * @param active the active
	 * @param moduleId the module id
	 * @param refsetId the refset id
	 * @param referencedComponentId the referenced component id
	 * @param targetComponent the target component
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeRF2TypeLine(UUID refsetuuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String targetComponent) throws IOException {

		WriteUtil.write(getConfig(), refsetuuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + targetComponent);
		WriteUtil.write(getConfig(), "\r\n");

	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public static Logger getLogger() {
		return logger;
	}


	/**
	 * Sets the logger.
	 *
	 * @param logger the new logger
	 */
	public static void setLogger(Logger logger) {
		RF2DescriptionReferencesImpl.logger = logger;
	}

	/**
	 * Gets the refset.
	 *
	 * @return the refset
	 */
	public static Refset getRefset() {
		return refset;
	}

	/**
	 * Sets the refset.
	 *
	 * @param refset the new refset
	 */
	public static void setRefset(Refset refset) {
		RF2DescriptionReferencesImpl.refset = refset;
	}
}
