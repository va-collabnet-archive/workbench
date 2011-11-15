package org.ihtsdo.workflow; 

import java.io.IOException;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.workflow.refset.utilities.WfComparator;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;


/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefsetSearcher extends WorkflowRefsetSearcher {

	public WorkflowHistoryRefsetSearcher() throws TerminologyException, IOException
	{
		super(workflowHistoryConcept);
	}

	public boolean isInitialized() {
		return refsetConcept != null;
	}

	public WorkflowHistoryJavaBean getLatestBeanForWorkflowId(int conceptNid, UUID workflowId) {
		long currentTime = 0;
		WorkflowHistoryJavaBean lastBean = null;
		
		try {
			I_GetConceptData con = Terms.get().getConcept(conceptNid);
			TreeSet<WorkflowHistoryJavaBean> beans = WorkflowHelper.getWfHxMembersAsBeans(con);
			for (WorkflowHistoryJavaBean bean : beans ) {
				if (bean.getWorkflowId().equals(workflowId) &&
					currentTime < bean.getWorkflowTime()) {
					currentTime = bean.getWorkflowTime();
					lastBean = bean; 
				}
			}
			
			// Return Latest Bean of Workflow Set
			return lastBean;
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to access Workflow History Refset members with error: " + e.getMessage());
		}
		
		return null;
	}

	public TreeSet<WorkflowHistoryJavaBean> getAllHistoryForWorkflowId(I_GetConceptData concept, UUID wfId) {
		TreeSet<WorkflowHistoryJavaBean> retSet = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxEarliestFirstTimeComparer());

		try {
			TreeSet<WorkflowHistoryJavaBean> beans = WorkflowHelper.getWfHxMembersAsBeans(concept);
			for (WorkflowHistoryJavaBean bean : beans) {
				if (bean.getWorkflowId().equals(wfId)) {
					retSet.add(bean);
				}
				
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to access Workflow History Refset members with error: " + e.getMessage());
		}
		
		return retSet;
	}
}