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
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.lucene.LuceneManager.LuceneSearchType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
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
	   	private static int wfHxRefsetNid = 0;

	   	public static WfHxLuceneWriter getInstance(Set<I_ExtendByRef> uncommittedWfMemberIds) throws InterruptedException {
            if (wfHxRefsetNid == 0) {
            	try {
            		wfHxRefsetNid = Terms.get().uuidToNative(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid());
				} catch (Exception e) {
            		wfHxRefsetNid = Integer.MAX_VALUE; 
            		AceLog.getAppLog().log(Level.WARNING, "Workflow History Refset Not Available: " + e.getMessage());
            	}
            }
		   
    		if (wfHxRefsetNid < 0) {
    			// Only acquire if can access WfHx refset
    			luceneWriterPermit.acquire();
    			return new WfHxLuceneWriter(uncommittedWfMemberIds);
    		} else {
        		AceLog.getAppLog().log(Level.INFO, "Workflow History History not present.");
        		return null;
    		}
	   	}
	   
	   	
	   	
	   	// WfHx Refset must be present or class will not be instantiated
	   	private static class WfHxLuceneWriter implements Runnable {
	   		private static Set<I_ExtendByRef> wfExtensionsToUpdate;
	   		private WorkflowHistoryRefsetReader reader;

	   		private WfHxLuceneWriter(Set<I_ExtendByRef> uncommittedWfMemberIds) {
	   			try {
	   				reader = new WorkflowHistoryRefsetReader();

		        	// tmp fix hook
	   				Set<I_ExtendByRef> wfExtensionsToIterate = new HashSet<I_ExtendByRef>(uncommittedWfMemberIds);
		        	
	   				for (Object rowObj : wfExtensionsToIterate) {
   						I_ExtendByRef row = (I_ExtendByRef)rowObj;
   						UUID uid = null;
   						try {
   							uid = Terms.get().nidToUuid(row.getComponentNid());
   							Object refComp = Ts.get().getComponent(row.getComponentNid());
   						} catch (Exception e){ 
			        		uncommittedWfMemberIds.remove(rowObj);
			        		if (uid != null) {
	   							AceLog.getAppLog().log(Level.WARNING, ("Unable to add workflow history into Lucene due to bad refCompUid: " + uid));
			        		} if (rowObj != null) {
	   							AceLog.getAppLog().log(Level.WARNING, ("Unable to add workflow history into Lucene due to bad row: " + rowObj.toString()));
			        		} else {
	   							AceLog.getAppLog().log(Level.WARNING, ("Unable to add workflow history into Lucene due to bad row"));
			        		}
   						}
	   				}

					wfExtensionsToUpdate = uncommittedWfMemberIds;	
	   			} catch (Exception e) {
	   				AceLog.getAppLog().log(Level.WARNING, "Unable to access Workflow History Refset with error: " + e.getMessage());
	   			}
	   		}

	   		@Override
	   		public void run() {
	   			try {
	   				Set<UUID> workflowsUpdated = new HashSet<UUID>();

	   				for (I_ExtendByRef row : wfExtensionsToUpdate) {
	   					if (reader != null && row.getRefsetId() == wfHxRefsetNid) {
	   						// Only analyze WfHx Refset Members
	   						UUID workflowId = reader.getWorkflowId(((I_ExtendByRefPartStr) row).getStringValue());

	   						// If two rows to commit, both will be caught by method below, so do this once per WfId
	   						if (!workflowsUpdated.contains(workflowId)) {
	   							workflowsUpdated.add(workflowId);

	   							I_GetConceptData con = Terms.get().getConcept(row.getComponentNid());
	   							SortedSet<WorkflowHistoryJavaBean> latestWorkflow = WorkflowHelper.getLatestWfHxForConcept(con, workflowId);

	   							WfHxLuceneManager.setWorkflowId(workflowId);
		                  
	   							ViewCoordinate vc;
	   							if (Terms.get().getActiveAceFrameConfig() == null) {
	   								vc = null;
	   							} else {
	   								vc = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
	   							}
			                  
		            			LuceneManager.writeToLucene(latestWorkflow, LuceneSearchType.WORKFLOW_HISTORY, vc);
	   						}
	   					}
	   				}
	   			} catch (Exception e) {
	   				AceLog.getAppLog().log(Level.WARNING, "Failed in adding following workflow row:" + wfExtensionsToUpdate.toString());
	   			}

	   			luceneWriterPermit.release();
	   		}
	   	}
   	}