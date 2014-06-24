package org.ihtsdo.lucene;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * Static version of WfHxLuceneWriter to ensure single access at a time
 * 
 * @author Jesse Efron
 */
public  class WfHxLuceneWriterAccessor {
   	private static Semaphore luceneWriterPermit = new Semaphore(1);
        public static int importCount = 0;

   	public static WfHxLuceneWriter addWfHxLuceneMembersFromExtensions(Set<I_ExtendByRef> extensions) throws InterruptedException {
    	Set<UUID> wfIdsSeen = new HashSet<UUID>();
        Set<WorkflowHistoryJavaBean> beansToAddToWf = new HashSet<WorkflowHistoryJavaBean>();

		try {
			for (I_ExtendByRef ref : extensions) {
	        	if (ref.getRefsetId() == WorkflowHelper.getWorkflowRefsetNid()) {
					WorkflowHistoryJavaBean bean = WorkflowHelper.populateWorkflowHistoryJavaBean(ref);
		
					// Only add to update once per WfId
					if (bean != null) {
						if (!wfIdsSeen.contains(bean.getWorkflowId())) {
							beansToAddToWf.add(bean);
							wfIdsSeen.add(bean.getWorkflowId());
						}
					} else {
			    		AceLog.getAppLog().log(Level.WARNING, "Failed to add WfHx from extension to Lucene for: " + ref.toString());
					}
				}
			}
                        importCount = 0;
			return new WfHxLuceneWriter(beansToAddToWf);
		} catch (Exception writerExc) {
    		AceLog.getAppLog().log(Level.WARNING, "Failed to write following beans to Wf Lucene with error: + " + writerExc.getMessage());
    		AceLog.getAppLog().log(Level.WARNING, "Beans: " + beansToAddToWf.toString());
		}
    	
    	return null;
   	}

	public static WfHxLuceneWriter addWfHxLuceneMembersFromEConcept(Set<TkRefexAbstractMember<?>> eConceptMembers) throws InterruptedException {
    	try {
        	Set<UUID> wfIdsSeen = new HashSet<UUID>();
            Set<WorkflowHistoryJavaBean> beansToAddToWf = new HashSet<WorkflowHistoryJavaBean>();

            for (TkRefexAbstractMember<?> mem : eConceptMembers) {
	        	if (mem.getRefexUuid().equals(WorkflowHelper.getWorkflowRefsetUid())) {
            		WorkflowHistoryJavaBean bean = WorkflowHelper.populateWorkflowHistoryJavaBean(mem);

					// Only add to update once per WfId
					if (bean != null) {
						if (!wfIdsSeen.contains(bean.getWorkflowId())) {
							beansToAddToWf.add(bean);
							wfIdsSeen.add(bean.getWorkflowId());
						}
					} else {
			    		AceLog.getAppLog().log(Level.WARNING, "Failed to add WfHx from eConcept to Lucene for: " + mem.toString());
					} 
	    		}
            }
                        importCount = 0;
			return new WfHxLuceneWriter(beansToAddToWf);
		} catch (Exception e) {
    		AceLog.getAppLog().log(Level.WARNING, "Workflow History Refset Not Available: " + e.getMessage());
    		AceLog.getAppLog().log(Level.WARNING, "EConcept: " + eConceptMembers.toString());
    	}

    	return null;
	}
	   	
   	// WfHx Refset must be present or class will not be instantiated
   	private static class WfHxLuceneWriter implements Runnable 
   	{
   		private static Set<WorkflowHistoryJavaBean> wfExtensionsToUpdate = new HashSet<WorkflowHistoryJavaBean>();
                
   		private WfHxLuceneWriter(Set<WorkflowHistoryJavaBean> allBeans) {
   			wfExtensionsToUpdate.addAll(allBeans);
   		}
                

   		@Override
   		public void run() {
   			try {
				luceneWriterPermit.acquire();
            
   				for (WorkflowHistoryJavaBean bean : wfExtensionsToUpdate) {
   					WfHxLuceneManager.addToLuceneNoWrite(bean);
   				}
   				
                                importCount = WfHxLuceneManager.writeUnwrittenWorkflows();
   			} catch (Exception e) {
   				AceLog.getAppLog().log(Level.WARNING, "Failed in adding following workflow row:" + wfExtensionsToUpdate.toString());
   			}

   			wfExtensionsToUpdate.clear();
   			luceneWriterPermit.release();
   		}
   	}
}