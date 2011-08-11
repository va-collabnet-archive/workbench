package org.ihtsdo.rf2.refset.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

/**
 * Title: RF2RelationshipImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Relationship File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2HistoricalAssociationImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2HistoricalAssociationImpl.class);

	public RF2HistoricalAssociationImpl(Config config) {
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

	
	@Override
	public void export(I_GetConceptData concept, String referencedComponentId) throws IOException {
		try {
			
			String effectiveTime = "";
			String relTypeId = "";
			String active = "";
			String targetComponent = "";
			String moduleId = I_Constants.CORE_MODULE_ID;
			int relationshipStatusId=0;
			
			List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(allStatuses, null, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			for (I_RelTuple rel : relationships) {
		
				targetComponent = getSctId(rel.getC2Id(), getSnomedCorePathNid());

				relTypeId = getSctId(rel.getTypeNid(), getSnomedCorePathNid());

				if (relTypeId==null || relTypeId.equals("")){
					relTypeId=tf.getUids(rel.getTypeNid()).iterator().next().toString();
				}
				if (relTypeId.equals(I_Constants.MAY_BE) || relTypeId.equals(I_Constants.WAS_A) || relTypeId.equals(I_Constants.SAME_AS) || relTypeId.equals(I_Constants.REPLACED_BY)
						|| relTypeId.equals(I_Constants.MOVED_FROM) || relTypeId.equals(I_Constants.MOVED_TO)) {

					relationshipStatusId = rel.getStatusNid();
					if (relationshipStatusId == activeNid) { 														
						active = "1";
					} else if (relationshipStatusId == inactiveNid) { 														
						active = "0";
					}
					

					effectiveTime = getDateFormat().format(new Date(rel.getTime()));

					String refsetId = getRefsetId(relTypeId);

					if (referencedComponentId==null || referencedComponentId.equals("")){
						referencedComponentId=concept.getUids().iterator().next().toString();
					}
					if (targetComponent==null || targetComponent.equals("")){
						Collection<UUID> Uids=tf.getUids(rel.getC2Id());
						if (Uids==null  ){
							continue;
						}
						targetComponent=Uids.iterator().next().toString();
						if (targetComponent.equals(nullUuid)){
							continue;
						}
					}

					UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId + targetComponent);
										
					writeRF2TypeLine(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, targetComponent);
				}
			}
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void writeRF2TypeLine(UUID uuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String targetComponent) throws IOException {

		WriteUtil.write(getConfig(), uuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + targetComponent);
		WriteUtil.write(getConfig(), "\r\n");

	}
}
