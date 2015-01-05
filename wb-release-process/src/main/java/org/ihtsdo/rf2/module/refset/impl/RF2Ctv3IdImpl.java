package org.ihtsdo.rf2.module.refset.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.module.constant.I_Constants;
import org.ihtsdo.rf2.module.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2Ctv3IdImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Ctv3Id Refset File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2Ctv3IdImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2Ctv3IdImpl.class);

	/**
	 * Instantiates a new r f2 ctv3 id impl.
	 *
	 * @param config the config
	 */
	public RF2Ctv3IdImpl(Config config) {
		super(config);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.I_GetConceptData)
	 */
	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.impl.RF2AbstractImpl#export(org.dwfa.ace.api.I_GetConceptData, java.lang.String)
	 */
	@Override
	public void export(I_GetConceptData concept, String referencedComponentId) {

		try{
			String refsetId = I_Constants.CTV3_REFSET_ID;
			String moduleId = "";

			String mapTarget_Core = getCtv3Id(concept);
			String mapTarget="";

			if(mapTarget_Core!=null && !mapTarget_Core.equals("")){
				mapTarget = mapTarget_Core;
			}

			if (referencedComponentId==null || referencedComponentId.equals("")){
				referencedComponentId=concept.getUids().iterator().next().toString();
			}

//			id generator must not be used
			
//			if(mapTarget == null || mapTarget.equals("") ){
//				//get conceptId by calling web service if exist otherwise create
//				String wsConceptId="";
//				if (referencedComponentId.contains("-")){
//					wsConceptId=getSCTId(getConfig(),UUID.fromString(referencedComponentId));
//					mapTarget = getCTV3ID(getConfig(), UUID.fromString(referencedComponentId));
//				}else{
//					mapTarget = getCTV3ID(getConfig(), concept.getUids().iterator().next());
//
//				}
//				if (wsConceptId!=null && !wsConceptId.equals("")){
//					referencedComponentId=wsConceptId;
//				}
//			}

			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allStatuses, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
				I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();
				Date et = new Date(getTermFactory().convertToThickVersion(attributes.getVersion()));
				String effectiveTime = getDateFormat().format(et);
				if (!(effectiveTime.compareTo(getConfig().getPreviousReleaseDate())>0) ||
						!(effectiveTime.compareTo(getConfig().getReleaseDate())<=0)){
					return;
				}
				if (isComponentToPublish( attributes.getMutablePart())){
//					int intModuleId=attributes.getModuleNid();
//					moduleId=getModuleSCTIDForStampNid(intModuleId);

					moduleId = computeModuleId(concept);
					UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId + mapTarget);
					writeRF2TypeLine(uuid, "", I_Constants.SIMPLE_MAP_REFSET_ACTIVE, moduleId, refsetId, referencedComponentId, mapTarget);
				}
			}
		} catch (TerminologyException e) {
			logger.error("TerminologyException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exceptions in exportIncremental SnomedIdRefset: " + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Write r f2 type line.
	 *
	 * @param uuid the uuid
	 * @param effectiveTime the effective time
	 * @param simpleMapRefsetActive the simple map refset active
	 * @param moduleId the module id
	 * @param refsetId the refset id
	 * @param referencedComponentId the referenced component id
	 * @param mapTarget the map target
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeRF2TypeLine(UUID uuid, String effectiveTime, String simpleMapRefsetActive, String moduleId, String refsetId, String referencedComponentId, String mapTarget) throws IOException {
		WriteUtil.write(getConfig(), uuid + "\t" + effectiveTime + "\t" + I_Constants.SIMPLE_MAP_REFSET_ACTIVE + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + mapTarget);
		WriteUtil.write(getConfig(), "\r\n");

	}
}
