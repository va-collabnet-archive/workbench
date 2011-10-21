package org.ihtsdo.workflow; 

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrVersionBI;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;


/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefsetSearcher extends WorkflowRefsetSearcher {

	private int currentStatusNid = 0;

	public WorkflowHistoryRefsetSearcher() throws TerminologyException, IOException
	{
		super(workflowHistoryConcept);
		try { 
			currentStatusNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid());
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error creating Workflow History Refset Searcher with error: " + e.getMessage());
		}
	}

	public boolean isInitialized() {
		return refsetConcept != null;
	}

	public WorkflowHistoryJavaBean getLatestBeanForWorkflowId(int conceptNid, UUID workflowId) {
		long currentTime = 0;
		WorkflowHistoryJavaBean lastBean = null;
		
		try {
			I_GetConceptData con = Terms.get().getConcept(conceptNid);
			Collection<? extends RefexVersionBI<?>> members = con.getCurrentAnnotationMembers(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), refsetNid);
			for (RefexVersionBI<?> m : members ) {
				
				RefexStrVersionBI member = (RefexStrVersionBI) m;
				WorkflowHistoryJavaBean currentBean = WorkflowHelper.populateWorkflowHistoryJavaBean(m.getNid(), con.getPrimUuid(), 
																									 member.getStr1(), new Long(m.getTime()));
				
				if (currentBean.getWorkflowId().equals(workflowId) &&
					currentTime < currentBean.getWorkflowTime()) {
					currentTime = currentBean.getWorkflowTime();
					lastBean = currentBean; 
				}
			}
			
			// Return Latest Bean of Workflow Set
			return lastBean;
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to access Workflow History Refset members with error: " + e.getMessage());
		}
		
		return null;
	}
}

