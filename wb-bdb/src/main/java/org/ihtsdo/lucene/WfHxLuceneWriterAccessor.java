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
import org.ihtsdo.lucene.LuceneManager.LuceneSearchType;
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

	   public static WfHxLuceneWriter getInstance(Set<I_ExtendByRef> uncommittedWfMemberIds) throws InterruptedException {
		   luceneWriterPermit.acquire();
		   
		   return new WfHxLuceneWriter(uncommittedWfMemberIds);
	   }
	   
	   public static class WfHxLuceneWriter implements Runnable {
		      private static Set<I_ExtendByRef> wfExtensionsToUpdate;

		      //~--- fields -----------------------------------------------------------

		      private WorkflowHistoryRefsetReader reader;

		      //~--- constructors -----------------------------------------------------

		      private WfHxLuceneWriter(Set<I_ExtendByRef> uncommittedWfMemberIds) {
		         super();
		         wfExtensionsToUpdate = uncommittedWfMemberIds;

		         try {
		            reader = new WorkflowHistoryRefsetReader();
		         } catch (Exception e) {
		            AceLog.getAppLog().log(Level.WARNING,
		                                   "Unable to access Workflow History Refset with error: " + e.getMessage());
		         }
		      }

		      //~--- methods ----------------------------------------------------------

		      @Override
		      public void run() {
		         try {
		            Set<UUID> workflowsUpdated = new HashSet<UUID>();

		            for (I_ExtendByRef row : wfExtensionsToUpdate) {
		               UUID workflowId = reader.getWorkflowId(((I_ExtendByRefPartStr) row).getStringValue());

		               // If two rows to commit, both will be caught by method below, so do this once per WfId
		               if (!workflowsUpdated.contains(workflowId)) {
		                  workflowsUpdated.add(workflowId);

		                  I_GetConceptData                   con            =
		                     Terms.get().getConcept(row.getComponentNid());
		                  SortedSet<WorkflowHistoryJavaBean> latestWorkflow =
		                     WorkflowHelper.getLatestWfHxForConcept(con, workflowId);

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
		         } catch (Exception e) {
		            AceLog.getAppLog().alertAndLogException(e);
		         }

		         luceneWriterPermit.release();
		      }
		   }

   }