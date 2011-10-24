package org.ihtsdo.lucene;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrMember;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetReader;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * Static version of WfHxLuceneWriter to ensure single access at a time
 * 
 * @author Jesse Efron
 */
public  class WfHxLuceneWriterAccessor {
   	private static Semaphore luceneWriterPermit = new Semaphore(1);

   	public static WfHxLuceneWriter prepareWriterWithExtensions(Set<I_ExtendByRef> extensions) throws InterruptedException {
    	Set<UUID> wfIdsSeen = new HashSet<UUID>();
        Set<WorkflowHistoryJavaBean> beansToAddToWf = new HashSet<WorkflowHistoryJavaBean>();

		for (I_ExtendByRef ref : extensions) {
			WorkflowHistoryJavaBean bean = WorkflowHelper.populateWorkflowHistoryJavaBean(ref);

			// Only add to update once per WfId
			if (!wfIdsSeen.contains(bean.getWorkflowId())) {
				beansToAddToWf.add(bean);
				wfIdsSeen.add(bean.getWorkflowId());
			}
		}

		luceneWriterPermit.acquire();

		try {
			return new WfHxLuceneWriter(beansToAddToWf);
		} catch (Exception writerExc) {
    		AceLog.getAppLog().log(Level.WARNING, "Failed to write following beans to Wf Lucene with error: + " + writerExc.getMessage());
    		AceLog.getAppLog().log(Level.WARNING, "Beans: " + beansToAddToWf.toString());
		}
    	
    	return null;
   	}

	public static WfHxLuceneWriter prepareWriterWithEConcept(Set<TkRefsetAbstractMember<?>> eConcepts) throws InterruptedException {
    	try {
        	Set<UUID> wfIdsSeen = new HashSet<UUID>();
            Set<WorkflowHistoryJavaBean> beansToAddToWf = new HashSet<WorkflowHistoryJavaBean>();
            
            for (TkRefsetAbstractMember<?> ref : eConcepts) {
				TkRefsetStrMember member = (TkRefsetStrMember)ref;

				int memberNid = Terms.get().uuidToNative(member.getPrimordialComponentUuid());
				WorkflowHistoryJavaBean bean = WorkflowHelper.populateWorkflowHistoryJavaBean(memberNid, member.getComponentUuid(), 
																							  member.getStrValue(), member.getTime());

				// Only add to update once per WfId
				if (!wfIdsSeen.contains(bean.getWorkflowId())) {
					beansToAddToWf.add(bean);
					wfIdsSeen.add(bean.getWorkflowId());
				}
			}

    		// Only acquire if can access WfHx refset
			luceneWriterPermit.acquire();

			try {
				return new WfHxLuceneWriter(beansToAddToWf);
			} catch (Exception writerExc) {
	    		AceLog.getAppLog().log(Level.WARNING, "Failed to write following beans to Wf Lucene with error: + " + writerExc.getMessage());
	    		AceLog.getAppLog().log(Level.WARNING, "Beans: " + beansToAddToWf.toString());
			}
    	} catch (Exception e) {
    		AceLog.getAppLog().log(Level.WARNING, "Workflow History Refset Not Available: " + e.getMessage());
    	}

    	return null;
	}
	   	
   	// WfHx Refset must be present or class will not be instantiated
   	private static class WfHxLuceneWriter implements Runnable 
   	{
   		private static WorkflowHistoryRefsetReader reader;
   		private static Set<WorkflowHistoryJavaBean> wfExtensionsToUpdate = new HashSet<WorkflowHistoryJavaBean>();
   		
   		private WfHxLuceneWriter(Set<WorkflowHistoryJavaBean> allBeans) {
   			try {
				reader = new WorkflowHistoryRefsetReader();
			} catch (Exception e) {
			}
   			wfExtensionsToUpdate.addAll(allBeans);
   		}

   		@Override
   		public void run() {
   			Set<UUID> wfIdsProcessed= WorkflowHelper.getLuceneChangeWfIdSetStorage();
   			
   			try {
   				for (WorkflowHistoryJavaBean bean : wfExtensionsToUpdate) {
   					if (reader != null) {
   						
   						// Only perform once per wfId
   						if (!wfIdsProcessed.contains(bean.getWorkflowId())) 
   						{
   							wfIdsProcessed.add(bean.getWorkflowId());
   							
   							WfHxLuceneManager.addToLuceneNoWrite(bean);
   						}
   					}
   				}
   				
   				WfHxLuceneManager.writeUnwrittenWorkflows();
   				wfExtensionsToUpdate.clear();
   				WorkflowHelper.getLuceneChangeWfIdSetStorage().clear();
   				WorkflowHelper.getLuceneChangeWfIdSetStorage().addAll(wfIdsProcessed);
   			} catch (Exception e) {
   				AceLog.getAppLog().log(Level.WARNING, "Failed in adding following workflow row:" + wfExtensionsToUpdate.toString());
   			}

   			luceneWriterPermit.release();
   		}
   	}
}