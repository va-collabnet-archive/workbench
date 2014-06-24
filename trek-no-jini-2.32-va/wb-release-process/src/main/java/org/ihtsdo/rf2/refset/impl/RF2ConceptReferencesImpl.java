package org.ihtsdo.rf2.refset.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.model.Refset;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

/**
 * Title: RF2DescriptionImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Description File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2ConceptReferencesImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2ConceptReferencesImpl.class);

	private static Refset refset;

	public RF2ConceptReferencesImpl(Config config) {
		super(config);
	}

	public RF2ConceptReferencesImpl(Config config, Refset refset) {
		super(config);
		RF2ConceptReferencesImpl.refset = refset;
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

	public void export(I_GetConceptData concept, String referencedComponentId) throws IOException {
		String effectiveTime = "";
		String active = "";
		I_ExtendByRefPartCid<?> extensionPart;
		UUID uuid = null;
		String targetComponent = "";

		try {
			String refsetId = getRefset().getId();			
			//String moduleId = getMetaModuleID(concept);
			String moduleId = I_Constants.CORE_MODULE_ID;
			//int refsetTermAuxId = getNid(getRefset().getTermAuxUID());
			int refsetNid = getNid(getRefset().getUID());
			
			List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(concept.getNid(), true);

			if (!extensions.isEmpty()) {
				for (I_ExtendByRef extension : extensions) {
					if (extension.getRefsetId() == refsetNid) {
						if (extension != null) {
							// List<? extends I_ExtendByRefVersion> t2 =extension.getTuples();
							long lastVersion = Long.MIN_VALUE;
							extensionPart=null;
							for (I_ExtendByRefVersion loopTuple : extension.getTuples(allStatusSet,currenAceConfig.getViewPositionSetReadOnly(),
									Precedence.PATH,currenAceConfig.getConflictResolutionStrategy())) {

								if (loopTuple.getTime() >= lastVersion) {
									lastVersion = loopTuple.getTime();
									extensionPart = (I_ExtendByRefPartCid) loopTuple.getMutablePart();
								}
							}
							if (extensionPart == null) {
								if (logger.isDebugEnabled()) {
									logger.debug("Refset extension part not found!");
								}
							}else{
								
								int extensionStatusId = extensionPart.getStatusNid();
								if (extensionStatusId == activeNid) { 														
									active = "1";
								} else if (extensionStatusId == inactiveNid) { 														
									active = "0";								
								} else {
									System.out.println("unknown extensionStatusId =====>" + extensionStatusId);
									logger.error("unknown extensionStatusId =====>" + extensionStatusId);
									//System.exit(0);
								}
								
								int targetComponentNid = extensionPart.getC1id();
								targetComponent = getSctId(targetComponentNid, getSnomedCorePathNid());
								uuid = extensionPart.getPrimUuid();
								Date effectiveDate = new Date(extensionPart.getTime());
								effectiveTime = getDateFormat().format(effectiveDate);

								if (referencedComponentId==null || referencedComponentId.equals("")){
									referencedComponentId=concept.getUids().iterator().next().toString();
								}
								if (targetComponent==null || targetComponent.equals("")){
									targetComponent=Terms.get().getUids(targetComponentNid).iterator().next().toString();
								}
								writeRF2TypeLine(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, targetComponent);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("IO Exceptions in exportReferences:" + getRefset().getId() + " : " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exceptions in exportReferences:" + getRefset().getId() + " : " + e.getMessage());
			logger.error(referencedComponentId);
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void writeRF2TypeLine(UUID refsetuuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String targetComponent) throws IOException {

		WriteUtil.write(getConfig(), refsetuuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + targetComponent);
		WriteUtil.write(getConfig(), "\r\n");

	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		RF2ConceptReferencesImpl.logger = logger;
	}

	public static Refset getRefset() {
		return refset;
	}

	public static void setRefset(Refset refset) {
		RF2ConceptReferencesImpl.refset = refset;
	}
}
