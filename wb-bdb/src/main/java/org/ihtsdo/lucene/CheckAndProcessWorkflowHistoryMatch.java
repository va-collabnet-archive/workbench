package org.ihtsdo.lucene;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.SearchWfHistoryStringWorker.WfHxProgressUpdator;
import org.ihtsdo.ace.api.I_TestWorkflowHistorySearchResults;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

public class CheckAndProcessWorkflowHistoryMatch implements Runnable {

    Collection<WorkflowHistoryJavaBean> matches;

    List<I_TestWorkflowHistorySearchResults> checkList;

    I_ConfigAceFrame config;

    WorkflowHistoryJavaBean bean;

    private final int refsetId = 0;
    private float score;

   // private CountDownLatch beanLatch;

    public CheckAndProcessWorkflowHistoryMatch(//CountDownLatch beanLatch, 
    		WfHxProgressUpdator updater, WorkflowHistoryJavaBean currentBean,
    		Collection<WorkflowHistoryJavaBean> matches, 
            List<I_TestWorkflowHistorySearchResults> checkList, I_ConfigAceFrame config) {
        super();
        this.bean = currentBean;
        this.matches = matches;
        this.checkList = checkList;
        this.config = config;
   //     this.beanLatch = beanLatch;
    }

    public void run() 
    {
	    if (checkList == null || checkList.size() == 0) {
	            matches.add(bean);
	    } else {
	    	 try 
	    	 {
	             boolean failed = false;
	             for (I_TestWorkflowHistorySearchResults test : checkList) {
//	                 if (test.t(bean, config) == false) {
//	                     failed = true;
//	                     break;
//	                 }
	            	 failed = true;
	             }
	
	             if (failed == false) {
	                 matches.add(bean);
	             }
	         } catch (Exception e) {
	             if (ACE.editMode) {
	                 AceLog.getAppLog().alertAndLogException(e);
	             } else {
	                 AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
	             }
	         }
	    }
	    
	 //   beanLatch.countDown();
    }	
}
