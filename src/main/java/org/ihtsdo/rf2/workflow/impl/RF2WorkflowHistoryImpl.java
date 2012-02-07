package org.ihtsdo.rf2.workflow.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;


import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.workflow.WorkflowHistoryRefsetSearcher;

/**
 * Title: RF2ConceptImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Concept File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2WorkflowHistoryImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2WorkflowHistoryImpl.class);

	public RF2WorkflowHistoryImpl(Config config) {
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

	public void export(I_GetConceptData concept, String conceptid)  {
		try {


			String refsetId  = RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid().toString();
			//int refsetTermAuxId = getNid(I_Constants.WORKFLOW_HISTORY_REFSET_UID);	
			int refsetTermAuxId = getNid(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid().toString());		
			WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();

			List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(concept.getNid(), true);
			I_ExtendByRefPartStr<?> extensionPart;
			int extensionStatusId = 0;
			String active = "";

			if (!extensions.isEmpty()) {
				for (I_ExtendByRef extension : extensions) {
					if (extension.getRefsetId() == refsetTermAuxId) {
						if (extension != null) {
							long lastVersion = Long.MIN_VALUE;
							extensionPart=null;
							for (I_ExtendByRefVersion loopTuple : extension.getTuples(allStatusSet,currenAceConfig.getViewPositionSetReadOnly(),
									Precedence.PATH,currenAceConfig.getConflictResolutionStrategy())) {
								if (loopTuple.getTime() >= lastVersion) {
									lastVersion = loopTuple.getTime();
									extensionPart = (I_ExtendByRefPartStr) loopTuple.getMutablePart();
								}
							}

							if (extensionPart == null) {
								if (logger.isDebugEnabled()) {
									logger.debug("Refset extension part not found!");
								}
							}else{


								//System.out.println(" get Nid " + extension.getNid());
//								String workflowMemberInfo =  searcher.getMemberWfHxForDatabaseImport(extension.getNid());
//
//								extensionStatusId = extensionPart.getStatusNid();
//								if ( (extensionStatusId == activeNid) || (extensionStatusId == currentNid)) { 														
//									active = "1";
//								} else if (extensionStatusId == inactiveNid || (extensionStatusId == retiredNid)) {
//									System.out.println(workflowMemberInfo);
//									active = "0";
//								} else {
//									logger.error("unknown extensionStatusId =====>" + extensionStatusId);
//								}
//
//								if(active.equals("1")){
//									writeRF2TypeLine(workflowMemberInfo);
//								}
							}
						}
					}							
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


	private void writeRF2TypeLine(String workflowMemberInfo) throws IOException {		
		WriteUtil.write(getConfig(), workflowMemberInfo);
		WriteUtil.write(getConfig(), "\r\n");

	}

}
