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

/**
 * Title: RF2SnomedIdImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 SnomedId Refset File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2SnomedIdImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2SnomedIdImpl.class);

	public RF2SnomedIdImpl(Config config) {
		super(config);
	}

	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	@Override
	public void export(I_GetConceptData concept, String referencedComponentId) {
		try{
			String refsetId = I_Constants.SNOMED_REFSET_ID;
			String moduleId = "";
			String mapTarget_Core = getSnomedId(concept);
			String mapTarget="";

			if(mapTarget_Core!=null && !mapTarget_Core.equals("")){
				mapTarget = mapTarget_Core;
			}
			if (referencedComponentId==null || referencedComponentId.equals("")){
				referencedComponentId=concept.getUids().iterator().next().toString();
			}

			if(mapTarget==null || mapTarget.equals("")  ){	
				String parentSnomedId = getParentSnomedId(concept);

				if(parentSnomedId.equals("")){
					logger.info("=====No parentSnomedId===" + concept.getInitialText());
					System.out.println("=====No parentSnomedId===" + concept.getInitialText());
				}else{				


					//get conceptId by calling web service if exist otherwise create
					String wsConceptId="";
					if (referencedComponentId.contains("-")){
						wsConceptId=getSCTId(getConfig(),UUID.fromString(referencedComponentId));

						mapTarget = getSNOMEDID(getConfig(), UUID.fromString(referencedComponentId), parentSnomedId);
					}else{
						mapTarget = getSNOMEDID(getConfig(), concept.getUids().iterator().next(), parentSnomedId);

					}
					if (wsConceptId!=null && !wsConceptId.equals("")){
						referencedComponentId=wsConceptId;
					}
				}
			}

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
					int intModuleId=attributes.getModuleNid();
					moduleId=getModuleSCTIDForStampNid(intModuleId);
					UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId + mapTarget);
					writeRF2TypeLine(uuid, effectiveTime, I_Constants.SIMPLE_MAP_REFSET_ACTIVE, moduleId, refsetId, referencedComponentId, mapTarget);
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
		}
	}


	private void writeRF2TypeLine(UUID uuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String mapTarget) throws IOException {

		WriteUtil.write(getConfig(), uuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + mapTarget);
		WriteUtil.write(getConfig(), "\r\n");

	}
}
