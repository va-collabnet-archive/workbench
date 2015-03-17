package org.ihtsdo.rf2.workflow.impl;

import java.io.IOException;
import java.util.Date;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * Title: RF2ConceptImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Concept File Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 * 
 * Modified by Alejandro Rodriguez
 * Date 20150311
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

			TreeSet<WorkflowHistoryJavaBean> wfHistory = WorkflowHelper.getAllWorkflowHistory(concept);
			
			if (getConfig().getComponentType().equals("FULL_WF_DATA")){
				for (WorkflowHistoryJavaBean bean:wfHistory){
					exportBean(bean,conceptid);
				}
				
			}else{
				if (wfHistory.size()>0){
					WorkflowHistoryJavaBean bean=wfHistory.last();
					exportBean(bean,conceptid);
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


	private void exportBean(WorkflowHistoryJavaBean bean, String conceptid) throws Exception {
		StringBuffer sb=new StringBuffer();
		String newconceptid="";
		if (conceptid.contains("-")){
			newconceptid=getSCTId(getConfig(),bean.getConcept());
		}else{
			newconceptid=conceptid;
		}
		sb.append(newconceptid);
		sb.append("\t");
		sb.append(bean.getFullySpecifiedName());
		sb.append("\t");
		sb.append(bean.getWorkflowId());
		sb.append("\t");
		sb.append(Terms.get().getConcept(bean.getPath()).getInitialText());
		sb.append("\t");
		sb.append(Terms.get().getConcept(bean.getModeler()).getInitialText());
		sb.append("\t");
		sb.append(Terms.get().getConcept(bean.getAction()).getInitialText());
		sb.append("\t");
		sb.append(Terms.get().getConcept(bean.getState()).getInitialText());
		sb.append("\t");
		sb.append(getDateFormat().format(new Date(bean.getEffectiveTime())));
		sb.append("\t");
		sb.append(getDateFormat().format(new Date(bean.getWorkflowTime())));
		sb.append("\t");
		sb.append(bean.getAutoApproved());
		sb.append("\t");
		sb.append(bean.getOverridden());
		sb.append("\t");
		sb.append(bean.getRefexMemberNid());
		
		writeRF2TypeLine(sb.toString());
	}

	private void writeRF2TypeLine(String workflowMemberInfo) throws IOException {		
		WriteUtil.write(getConfig(), workflowMemberInfo);
		WriteUtil.write(getConfig(), "\r\n");

	}

}
