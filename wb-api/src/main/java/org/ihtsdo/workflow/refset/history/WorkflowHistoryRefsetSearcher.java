package org.ihtsdo.workflow.refset.history; 

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.ace.task.search.I_TestWorkflowHistorySearchResults;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;


/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefsetSearcher extends WorkflowRefsetSearcher {
	
	private int activeStatusNid = 0;
	private int currentStatusNid = 0;


	private int totalConcepts = 0;
		
	public WorkflowHistoryRefsetSearcher()
		throws TerminologyException, IOException 
	{
		refset = new WorkflowHistoryRefset();
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
		
		activeStatusNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.ACTIVE.getPrimoridalUid());
		currentStatusNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid());
	}

	public int getTotalCount() throws IOException {
		return Terms.get().getRefsetExtensionMembers(refsetId).size();
	}

	public int getTotalCountByConcept(I_GetConceptData con) throws IOException {
		return Terms.get().getRefsetExtensionsForComponent(refsetId, con.getConceptNid()).size();
	}


	public int getTotalConceptCount() {
		return totalConcepts;
	}

	
	
	public SortedSet<WorkflowHistoryJavaBean> getWfHxByConcept(I_GetConceptData con) throws TerminologyException, IOException, Exception {
		SortedSet <WorkflowHistoryJavaBean> retMap = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.createWfHxJavaBeanComparer());

		for (I_ExtendByRef historyRow : Terms.get().getRefsetExtensionsForComponent(refsetId, con.getConceptNid())) 
		{
			I_ExtendByRefPartStr latestVersion = (I_ExtendByRefPartStr)historyRow.getMutableParts().get(historyRow.getMutableParts().size() - 1);
			
			int currentState = latestVersion.getStatusNid();
		
			// TODO: Must make ExportWorkflowHistory use proper active/current status
			if ((currentState == activeStatusNid) ||
			    (currentState == currentStatusNid))
			{
				WorkflowHistoryJavaBean b = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(historyRow.getComponentNid()), latestVersion.getStringValue(), new Long(latestVersion.getTime()));
System.out.println(b);
				retMap.add(b);
			}
		}

		return retMap;
	} 
	
	public UUID getLatestWfIdForConcept(I_GetConceptData con) 
	{
		UUID currentWorkflowId = UUID.randomUUID();
		long currentTimeStamp = 0;

		try {
			for (I_ExtendByRef historyRow : Terms.get().getRefsetExtensionsForComponent(refsetId, con.getConceptNid())) 
			{
				I_ExtendByRefPartStr latestVersion = (I_ExtendByRefPartStr)historyRow.getMutableParts().get(historyRow.getMutableParts().size() - 1);
				
				int currentState = latestVersion.getStatusNid();
			
				// TODO: Must make ExportWorkflowHistory use proper active/current status
				if ((currentState == activeStatusNid) ||
				    (currentState == currentStatusNid))
				{
		
					WorkflowHistoryJavaBean b = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(historyRow.getComponentNid()), latestVersion.getStringValue(), new Long(latestVersion.getTime()));
				
					if (!b.getWorkflowId().equals(currentWorkflowId) &&
						b.getTimeStamp() >= currentTimeStamp)
					{
						currentWorkflowId = b.getWorkflowId();
						currentTimeStamp = b.getTimeStamp();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return currentWorkflowId;
	}
	
	public WorkflowHistoryJavaBean getLatestWfHxJavaBeanForConcept(I_GetConceptData con) 
	{
		WorkflowHistoryJavaBean mostCurrent = null;
		
		try {
			for (I_ExtendByRef extension : Terms.get().getRefsetExtensionsForComponent(refsetId, con.getConceptNid()))
			{
				I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)extension.getMutableParts().get(extension.getMutableParts().size() - 1);

				int currentState = props.getStatusNid();

				if ((currentState == activeStatusNid) ||
					(currentState == currentStatusNid))
				{

					WorkflowHistoryJavaBean b = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(extension.getComponentNid()), props.getStringValue(), new Long(extension.getMutableParts().get(0).getTime()));
					
					if (mostCurrent == null || b.getTimeStamp() >= mostCurrent.getTimeStamp())
					{
						mostCurrent = b;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return mostCurrent;
	}

	
	
	public void listWorkflowHistory() throws NumberFormatException, IOException, TerminologyException 
	{
	//	Writer outputFile = new OutputStreamWriter(new FileOutputStream("C:\\Users\\jefron\\Desktop\\wb-bundle\\Output.txt"));

		int counter = 0;
		for (I_ExtendByRef extension : Terms.get().getRefsetExtensionMembers(refsetId)) 
		{
			if (counter++ > 100) break;
			
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)extension;
		
			if (props.getStringValue().length() > 0)
			{
				WorkflowHistoryJavaBean bean = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(extension.getComponentNid()), props.getStringValue(), new Long(extension.getMutableParts().get(0).getTime()));
				System.out.println("\n\nBean #" + counter + " with status: " + props.getStatusNid() + " has properties: " + bean);
			}
			else
				System.out.println("\n\nBean #" + counter + " is badly formed");

			//outputFile.write("\n\nBean #" + counter + " with status UID: " + Terms.get().nativeToUuid(props.getStatusNid()).get(0) + " has properties: " + bean);
		}
	//	outputFile.flush();
	//	outputFile.close();
	}
	
	//TODO: USED TO TEST OVERWRITING OF RETURN VALUES
	// CHANGE BACK TO COMPARATOR THAT USES FSN
	//class alwaysOne implements Comparator<WorkflowHistoryJavaBean> { public int compare(WorkflowHistoryJavaBean a, WorkflowHistoryJavaBean b) {return 1;}}
	//SortedSet<WorkflowHistoryJavaBean> returnList = new TreeSet<WorkflowHistoryJavaBean>(new alwaysOne());
	public SortedSet<WorkflowHistoryJavaBean> searchForWFHistory( List<I_TestWorkflowHistorySearchResults> checkList, boolean WorkflowInProgress, boolean CompletedWorkflowInProgress, boolean PastReleasesIncluded) throws TerminologyException, IOException, Exception 
	{
		UUID currentWorkflowID = null;
		HashSet<WorkflowHistoryJavaBean> singleWorkflowBucket = new HashSet<WorkflowHistoryJavaBean>();
		SortedSet<WorkflowHistoryJavaBean> returnList = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.createWfHxFsnJavaBeanComparer());
		SortedSet<WorkflowHistoryJavaBean> sortedInputList = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.createWfIdTimeStampComparer());

		// For Debug
		listWorkflowHistory(); 

		// Create sorted by WfId/Timestamp list of Active WfHxJavaBeans
		for (I_ExtendByRef historyRow : Terms.get().getRefsetExtensionMembers(refsetId)) 
		{
			
			I_ExtendByRefPartStr latestVersion = (I_ExtendByRefPartStr)historyRow.getMutableParts().get(historyRow.getMutableParts().size() - 1);
			
			int currentState = latestVersion.getStatusNid();
		
			// TODO: Must make ExportWorkflowHistory use proper active/current status
			if ((currentState == activeStatusNid) ||
			    (currentState == currentStatusNid))
			{
	
				WorkflowHistoryJavaBean history = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(historyRow.getComponentNid()), latestVersion.getStringValue(), new Long(latestVersion.getTime()));
				sortedInputList.add(history);
			}
		}
		

		// Test each bean
		for (WorkflowHistoryJavaBean bean : sortedInputList)
		{
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
		if (testConceptAgainstCheckboxes(singleWorkflowBucket, WorkflowInProgress, CompletedWorkflowInProgress, PastReleasesIncluded))
				if (testSingleConcept(singleWorkflowBucket, checkList))
						returnList.addAll(singleWorkflowBucket);
		

		return returnList;
	}





 private boolean testSingleConcept (Set<WorkflowHistoryJavaBean> singleWorkflowBucket, List<I_TestWorkflowHistorySearchResults> checkList) throws TaskFailedException {

	 totalConcepts++;
	 
	for (I_TestWorkflowHistorySearchResults results : checkList) {
	
		if (!results.test(singleWorkflowBucket)) {
	   		return false;
		}
	}
	
	return true;
 }
 
 private boolean testConceptAgainstCheckboxes (Set<WorkflowHistoryJavaBean> singleWorkflowBucket, boolean WorkflowInProgress, boolean CompletedWorkflowInProgress, boolean PastReleasesIncluded) throws IOException, TerminologyException, Exception {
	 
		boolean approvedFound = false;

		///// PAST RELEASE INCLUDED
		
		boolean currentItemFound = false;
		
		if (!PastReleasesIncluded) {
			for (WorkflowHistoryJavaBean wfHistoryItem : singleWorkflowBucket) {
				if (new Date(wfHistoryItem.getTimeStamp()).after(new Date("01/01/2010"))) {
					currentItemFound = true;
				}
			}
		
			if (!currentItemFound)
				return false;
		}

		///// END PAST RELEASE INCLUDED
		
		if (!WorkflowInProgress && !CompletedWorkflowInProgress) { 
				throw new Exception("Error: User must choose either Workflow In Progress or Completed.");
		} else if (!(WorkflowInProgress && CompletedWorkflowInProgress)) {
			
	    	for (WorkflowHistoryJavaBean wfHistoryItem : singleWorkflowBucket) {
	    		I_GetConceptData state = Terms.get().getConcept(wfHistoryItem.getState());
	    		
	    		List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(state, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);
	    		
	    		for (I_RelTuple rel : relList)
	    		{
	    			if (rel != null && 
	    				rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION.getPrimoridalUid()).getConceptNid())
	    				approvedFound = true;
	    		}
	    		
	    		if (approvedFound)
	    			break;
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
 

}

