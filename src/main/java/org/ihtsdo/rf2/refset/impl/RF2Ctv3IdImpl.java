package org.ihtsdo.rf2.refset.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;

/**
 * Title: RF2Ctv3IdImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Ctv3Id Refset File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2Ctv3IdImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2Ctv3IdImpl.class);

	public RF2Ctv3IdImpl(Config config) {
		super(config);
	}

	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	@Override
	public void export(I_GetConceptData concept, String referencedComponentId) {

		try{
			String refsetId = I_Constants.CTV3_REFSET_ID;
			//String moduleId = getMetaModuleID(concept);
			String moduleId = I_Constants.CORE_MODULE_ID;
			
			String mapTarget_Core = getCtv3Id(concept, getSnomedCorePathNid());
// TODO confirm requeriment 			
			String mapTarget_Aux = getCtv3Id(concept, getNid("2faa9260-8fb2-11db-b606-0800200c9a66")); //Workbench Auxillary path
			String mapTarget="";
			
			if(!(mapTarget_Core.equals("") && mapTarget_Core.equals(null))){
				mapTarget = mapTarget_Core;
			}else if(!(mapTarget_Aux.equals("") && mapTarget_Aux.equals(null))){
				mapTarget = mapTarget_Aux;
			}
		
			UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId + mapTarget);
			if (referencedComponentId==null || referencedComponentId.equals("")){
				referencedComponentId=concept.getUids().iterator().next().toString();
			}
			writeRF2TypeLine(uuid, getConfig().getReleaseDate(), I_Constants.SIMPLE_MAP_REFSET_ACTIVE, moduleId, refsetId, referencedComponentId, mapTarget);
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

	private void writeRF2TypeLine(UUID uuid, String effectiveTime, String simpleMapRefsetActive, String moduleId, String refsetId, String referencedComponentId, String mapTarget) throws IOException {

		WriteUtil.write(getConfig(), uuid + "\t" + effectiveTime + "\t" + I_Constants.SIMPLE_MAP_REFSET_ACTIVE + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + mapTarget);
		WriteUtil.write(getConfig(), "\r\n");

	}
}
