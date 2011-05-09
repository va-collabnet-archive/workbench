package org.ihtsdo.workflow.refset.history; 

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.ace.api.I_TestWorkflowHistorySearchResults;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WfComparator;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;


/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefsetSearcher extends WorkflowRefsetSearcher {

	private int currentStatusNid = 0;

	public WorkflowHistoryRefsetSearcher()
	{
		try { 
			refset = new WorkflowHistoryRefset();
			setRefsetName(refset.getRefsetName());
			setRefsetId(refset.getRefsetId());
        	
			currentStatusNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid());
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error creating Workflow History Refset Searcher with error: " + e.getMessage());
		}
	}

	public SortedSet<WorkflowHistoryJavaBean> getWfHxForConcept(I_GetConceptData con) throws TerminologyException, IOException, Exception {
		SortedSet <WorkflowHistoryJavaBean> retMap = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxJavaBeanComparer());

		for (I_ExtendByRef historyRow : Terms.get().getRefsetExtensionsForComponent(refsetId, con.getConceptNid()))	{
			I_ExtendByRefPartStr latestVersion = (I_ExtendByRefPartStr)historyRow.getMutableParts().get(historyRow.getMutableParts().size() - 1);
			if (latestVersion.getStatusNid() == currentStatusNid) {
				WorkflowHistoryJavaBean b = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(historyRow.getComponentNid()), latestVersion.getStringValue(), new Long(latestVersion.getTime()));
				retMap.add(b);
			}
		}

		return retMap;
	} 

	public SortedSet<WorkflowHistoryJavaBean>  getLatestWfHxForConcept(I_GetConceptData con) 
	{
		SortedSet <WorkflowHistoryJavaBean> retMap = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxJavaBeanComparer());

		try {
			long currentWorkflowTime = 0;
			UUID currentWorkflowId = UUID.randomUUID();
			String conceptTerm = con.getInitialText();

			for (I_ExtendByRef historyRow : Terms.get().getRefsetExtensionsForComponent(refsetId, con.getConceptNid())) {
				I_ExtendByRefPartStr latestVersion = (I_ExtendByRefPartStr)historyRow.getMutableParts().get(historyRow.getMutableParts().size() - 1);
				if (latestVersion.getStatusNid() == currentStatusNid) {
					WorkflowHistoryJavaBean b = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(historyRow.getComponentNid()), latestVersion.getStringValue(), new Long(latestVersion.getTime()));
				
					if (!b.getWorkflowId().equals(currentWorkflowId) && b.getWorkflowTime() >= currentWorkflowTime)	{	
						retMap.clear();						
						currentWorkflowId = b.getWorkflowId();
						currentWorkflowTime = b.getWorkflowTime();
					}

					retMap.add(b);
				}
			}
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error getting workflow history on Concept: " + con.getPrimUuid());
		}
		
		return retMap;
	}

	public WorkflowHistoryJavaBean getLatestWfHxJavaBeanForConcept(I_GetConceptData con) 
	{
		SortedSet<WorkflowHistoryJavaBean> result = getLatestWfHxForConcept(con);
		if (result != null && result.size() > 0) {
			return result.last();
		} else {
			return null;
		}
	}
	
	//TODO: USED TO TEST OVERWRITING OF RETURN VALUES
	// CHANGE BACK TO COMPARATOR THAT USES FSN
	//class alwaysOne implements Comparator<WorkflowHistoryJavaBean> { public int compare(WorkflowHistoryJavaBean a, WorkflowHistoryJavaBean b) {return 1;}}
	//SortedSet<WorkflowHistoryJavaBean> returnList = new TreeSet<WorkflowHistoryJavaBean>(new alwaysOne());
	public SortedSet<WorkflowHistoryJavaBean> searchForWFHistory( List<I_TestWorkflowHistorySearchResults> checkList, boolean WorkflowInProgress, boolean CompletedWorkflowInProgress, boolean PastReleasesIncluded, String timestampBeforeSearchString, String timestampAfterSearchString) throws TerminologyException, IOException, Exception 
	{
		/* For Debugging: listWorkflowHistory(); */ 

		UUID currentWorkflowID = null;
		HashSet<WorkflowHistoryJavaBean> singleWorkflowBucket = new HashSet<WorkflowHistoryJavaBean>();
		SortedSet<WorkflowHistoryJavaBean> returnList = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxFsnJavaBeanComparer());
		SortedSet<WorkflowHistoryJavaBean> sortedInputList = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createTimestampComparer());

		// Create sorted by WfId/Timestamp list of Active WfHxJavaBeans
		for (I_ExtendByRef historyRow : Terms.get().getRefsetExtensionMembers(refsetId)) {
			I_ExtendByRefPartStr latestVersion = (I_ExtendByRefPartStr)historyRow.getMutableParts().get(historyRow.getMutableParts().size() - 1);
			if (latestVersion.getStatusNid() == currentStatusNid) {
				WorkflowHistoryJavaBean history = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(historyRow.getComponentNid()), latestVersion.getStringValue(), new Long(latestVersion.getTime()));
				sortedInputList.add(history);
			}
		}
		
		// Test each bean
		for (WorkflowHistoryJavaBean bean : sortedInputList) {
			if (currentWorkflowID == null || bean.getWorkflowId().equals(currentWorkflowID)) {
				singleWorkflowBucket.add(bean);
			} else {
				//Core tests. Against checkboxes and against filters
				if (testConceptAgainstCheckboxes(singleWorkflowBucket, WorkflowInProgress, CompletedWorkflowInProgress, PastReleasesIncluded))
					if (testSingleConcept(singleWorkflowBucket, checkList))
						returnList.addAll(singleWorkflowBucket);
				
				singleWorkflowBucket.clear();
				// Add history row from the new workflow
				singleWorkflowBucket.add(bean);
			}	
		
			currentWorkflowID = bean.getWorkflowId();
		}
		
		//Test last bucket
		if (!singleWorkflowBucket.isEmpty())
		{
			if (testConceptAgainstCheckboxes(singleWorkflowBucket, WorkflowInProgress, CompletedWorkflowInProgress, PastReleasesIncluded))
					if (testSingleConcept(singleWorkflowBucket, checkList))
							returnList.addAll(singleWorkflowBucket);
		}		

		return returnList;
	}
	
		
	
	public int getRefsetMembersCount() {
		try {
			return Terms.get().getRefsetExtensionMembers(refsetId).size();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Cant access workflow history refset with error: " + e.getMessage());
		}
		
		return 0;
	}

	public int getRefsetMembersCountByRelease(int relNid) {
		try {
			return Terms.get().getRefsetExtensionsForComponent(refsetId, relNid).size();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Cant access workflow history refset for release with error: " + e.getMessage());
		}

		return 0;
	}

	///// PAST RELEASE INCLUDED
	private boolean testConceptAgainstCheckboxes (Set<WorkflowHistoryJavaBean> singleWorkflowBucket, boolean WorkflowInProgress, boolean CompletedWorkflowInProgress, boolean PastReleasesIncluded) throws IOException, TerminologyException, Exception 
	{
		boolean approvedFound = false;
		boolean currentItemFound = false;
		
		if (!PastReleasesIncluded) {
			for (WorkflowHistoryJavaBean wfHistoryItem : singleWorkflowBucket) {
				if (new Date(wfHistoryItem.getWorkflowTime()).after(WorkflowHelper.getLatestReleaseTimestamp())) {
					currentItemFound = true;
				}
			}
		
			if (!currentItemFound) {
				return false;
			}
		}

		///// END PAST RELEASE INCLUDED
		if (!WorkflowInProgress && !CompletedWorkflowInProgress) { 
				throw new Exception("Error: User must choose either Workflow In Progress or Completed.");
		} else if (!(WorkflowInProgress && CompletedWorkflowInProgress)) {
	    	for (WorkflowHistoryJavaBean wfHistoryItem : singleWorkflowBucket) {
	    		I_GetConceptData state = Terms.get().getConcept(wfHistoryItem.getState());
	    		
	    		List<I_RelVersioned> relList = WorkflowHelper.getWorkflowRelationship(state, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);
	    		
	    		for (I_RelVersioned rel : relList)
	    		{
	    			if (rel != null && 
	    				rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION.getPrimoridalUid()).getConceptNid()) {
	    				approvedFound = true;
	    			}
	    		}
	    		
	    		if (approvedFound) {
	    			break;
	    		}
			}
	    	
	    	if (approvedFound) {
	    		if (WorkflowInProgress)
	    			return false;
	    	} else {
	    		if (CompletedWorkflowInProgress) 
	    			return false;
	    	}
	    	
		}
		
		return true;
	}

	 private boolean testSingleConcept (Set<WorkflowHistoryJavaBean> singleWorkflowBucket, List<I_TestWorkflowHistorySearchResults> checkList) throws TaskFailedException 
	 {
		for (I_TestWorkflowHistorySearchResults results : checkList) {
		
			if (!results.test(singleWorkflowBucket)) {
		   		return false;
			}
		}
		
		return true;
	 }

	public void listWorkflowHistory() throws NumberFormatException, IOException, TerminologyException 
	{
		Writer outputFile = new OutputStreamWriter(new FileOutputStream("C:\\Users\\jefron\\Desktop\\wb-bundle\\lib\\Output.txt"));
		int counter = 0;

		for (I_ExtendByRef row : Terms.get().getRefsetExtensionMembers(refsetId)) 
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)row;
			WorkflowHistoryJavaBean bean = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(row.getComponentNid()), props.getStringValue(), new Long(row.getMutableParts().get(0).getTime()));
			System.out.println("\n\nBean #: " + counter++ + " = " + bean.toString());
			outputFile.write("\n\nBean #: " + counter++ + " = " + bean.toString());
		}
		outputFile.flush();
		outputFile.close();
	}
}

