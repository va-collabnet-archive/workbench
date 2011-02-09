package org.ihtsdo.workflow.refset.history; 

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.ace.task.search.I_TestWorkflowHistorySearchResults;
import org.ihtsdo.tk.example.binding.Taxonomies;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;


/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefsetSearcher extends WorkflowRefsetSearcher {

	private int currentStatusNid = 0;
	private int totalConcepts = 0;
	private SortedSet<I_DescriptionVersioned> releases = null;
	private String releaseSearchString = "version: ";

	// First release with Workflow History
	private final String earliestWorkflowHistoryRelease = "2008-01-31"; 
	
		public WorkflowHistoryRefsetSearcher()
	{
		try {
			refset = new WorkflowHistoryRefset();
			setRefsetName(refset.getRefsetName());
			setRefsetId(refset.getRefsetId());
		
			currentStatusNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid());
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error creating Workflow History Refset Searcher", e);
		}
	}

	public int getTotalMemberCount() {
		try {
			return Terms.get().getRefsetExtensionMembers(refsetId).size();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Cant access workflow history refset", e);
		}
		
		return 0;
	}

	public int getTotalCountByRelease(int relNid) {
		try {
			return Terms.get().getRefsetExtensionsForComponent(refsetId, relNid).size();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Cant access workflow history refset for release", e);
		}

		return 0;
	}


	public int getTotalConceptCount() {
		return totalConcepts;
	}

	public void listWorkflowHistory() throws NumberFormatException, IOException, TerminologyException 
	{
	//	Writer outputFile = new OutputStreamWriter(new FileOutputStream("C:\\Users\\jefron\\Desktop\\wb-bundle\\Output.txt"));

		int counter = 0;
		for (I_ExtendByRef extension : Terms.get().getRefsetExtensionMembers(refsetId)) 
		{
			if (counter++ > 100) break;
			
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)extension;
			WorkflowHistoryJavaBean bean = null;
		
			if (props.getStringValue().length() > 0)
			{
				bean = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(extension.getComponentNid()), props.getStringValue(), new Long(extension.getMutableParts().get(0).getTime()));
			}
			else
			{
				AceLog.getAppLog().log(Level.WARNING, bean.toString(), new Exception("Failure in accessing Workflow History Refset"));
			}

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
		
			if (currentState == currentStatusNid)
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
		if (!singleWorkflowBucket.isEmpty())
		{
			if (testConceptAgainstCheckboxes(singleWorkflowBucket, WorkflowInProgress, CompletedWorkflowInProgress, PastReleasesIncluded))
					if (testSingleConcept(singleWorkflowBucket, checkList))
							returnList.addAll(singleWorkflowBucket);
		}		

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
					if (new Date(wfHistoryItem.getEffectiveTime()).after(new Date("01/01/2010"))) {
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
		    		
		    		List<I_RelVersioned> relList = WorkflowHelper.getWorkflowRelationship(state, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);
		    		
		    		for (I_RelVersioned rel : relList)
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
	 
		

	
	
	
	
	public SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> getAllWorkflowHistory() 
	{
		SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> returnHistory = new TreeMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>>();

		SortedSet<I_DescriptionVersioned> allReleases = getWorkflowAllReleases();
		
		for (I_DescriptionVersioned release : allReleases)
		{
			SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> releaseHistory = getWorkflowHistoryForRelease(release.getNid());
			returnHistory.putAll(releaseHistory);
		}
		
		return returnHistory;
	}

	public SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> getWorkflowHistoryForRelease(int relNid) 
	{
		SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> returnCollection = new TreeMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>>();
		
		try
		{
			// Create sorted by Concept/WfId/Timestamp list of current WfHxJavaBeans
			for (I_ExtendByRef historyRow : Terms.get().getRefsetExtensionsForComponent(refsetId, relNid)) 
			{
				I_ExtendByRefPartStr latestVersion = (I_ExtendByRefPartStr)historyRow.getMutableParts().get(historyRow.getMutableParts().size() - 1);
				
				int currentState = latestVersion.getStatusNid();
			
				if (currentState == currentStatusNid)
				{
					WorkflowHistoryJavaBean history = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(historyRow.getComponentNid()), latestVersion.getStringValue(), new Long(latestVersion.getTime()));
	
					if (!returnCollection.containsKey(history.getConcept()))
					{
						HashMap<UUID, SortedSet<WorkflowHistoryJavaBean>> newConceptSet = new HashMap<UUID, SortedSet<WorkflowHistoryJavaBean>>();
						SortedSet<WorkflowHistoryJavaBean> newWorkflowSet = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.createWfHxJavaBeanComparer());
						
						newConceptSet.put(history.getConcept(), newWorkflowSet);
					}
					
					Map<UUID, SortedSet<WorkflowHistoryJavaBean>> conceptHistory = returnCollection.get(history.getConcept());
					populateWorkflowCollection(conceptHistory, history);
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.SEVERE, "Failure in Getting workflow history", e);
		}
		
		return returnCollection;
	} 
	
	private void populateWorkflowCollection(Map<UUID, SortedSet<WorkflowHistoryJavaBean>> conceptHistory, WorkflowHistoryJavaBean history) 
	{
		if (!conceptHistory.containsKey(history.getWorkflowId()))
		{
			SortedSet<WorkflowHistoryJavaBean> newWorkflowSet = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.createWfHxJavaBeanComparer());			

			conceptHistory.put(history.getWorkflowId(), newWorkflowSet);
		}

		Set<WorkflowHistoryJavaBean> workflowHistory = conceptHistory.get(history.getWorkflowId());
		workflowHistory.add(history);
	}
	
	public Map<UUID, SortedSet<WorkflowHistoryJavaBean>> getAllWorkflowHistoryForConcept(I_GetConceptData con)
	{
		SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> history = getAllWorkflowHistory();

		if (!history.containsKey(con.getPrimUuid()))
		{
			return new TreeMap<UUID, SortedSet<WorkflowHistoryJavaBean>>();
		}
		else
		{
			return history.get(con.getPrimUuid());
		}

	}
	
	public SortedSet<WorkflowHistoryJavaBean> getLatestWorkflowHistoryForConcept(I_GetConceptData con)
	{
		SortedSet<WorkflowHistoryJavaBean> retSet = new TreeSet<WorkflowHistoryJavaBean>();
		Map<UUID, SortedSet<WorkflowHistoryJavaBean>> conHx = getAllWorkflowHistoryForConcept(con);
		
		for (UUID key : conHx.keySet())
		{
			SortedSet<WorkflowHistoryJavaBean> workflowSet = conHx.get(key);
			
			if ((retSet.size() == 0) || 
				(retSet.last().getWorkflowTime() < workflowSet.last().getWorkflowTime()))
			{
				retSet = workflowSet;
			}
				
		}
		
		return retSet;
	}
	public WorkflowHistoryJavaBean getLatestWfHxJavaBeanForConcept(I_GetConceptData con)
	{
		SortedSet<WorkflowHistoryJavaBean> list = getLatestWorkflowHistoryForConcept(con);
		
		if (list.size() > 0)
			return list.last();
		else
			return null;
	}

	public Map<UUID, SortedSet<WorkflowHistoryJavaBean>> getAllWorkflowHistoryForConceptForRelease(I_GetConceptData con, int relNid)
	{
		SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> history = getWorkflowHistoryForRelease(relNid);
		
		if (!history.containsKey(con.getPrimUuid()))
		{
			return new TreeMap<UUID, SortedSet<WorkflowHistoryJavaBean>>();
		}
		else
		{
			return history.get(con.getPrimUuid());
		}
	}
 
	public SortedSet<WorkflowHistoryJavaBean> getLatestWorkflowHistoryForConceptForRelease(I_GetConceptData con, int relNid)
	{
		SortedSet<WorkflowHistoryJavaBean> retSet = new TreeSet<WorkflowHistoryJavaBean>();
		Map<UUID, SortedSet<WorkflowHistoryJavaBean>> conHx = getAllWorkflowHistoryForConceptForRelease(con, relNid);
		
		for (UUID key : conHx.keySet())
		{
			SortedSet<WorkflowHistoryJavaBean> workflowSet = conHx.get(key);
			
			if ((retSet.size() == 0) || 
				(retSet.last().getWorkflowTime() < workflowSet.last().getWorkflowTime()))
			{
				retSet = workflowSet;
			}
				
		}
		
		return retSet;
	}

	public WorkflowHistoryJavaBean getLatestWfHxJavaBeanForConceptForRelease(I_GetConceptData con, int relNid)
	{
		SortedSet<WorkflowHistoryJavaBean> list = getLatestWorkflowHistoryForConceptForRelease(con, relNid);
		
		if (list.size() > 0)
			return list.last();
		else
			return null;
	}
	
	private SortedSet<I_DescriptionVersioned> getWorkflowAllReleases() {
		if (releases == null)
		{
			releases = new TreeSet<I_DescriptionVersioned>(WorkflowHistoryRefset.createDescriptionTimestampComparer());

			try {
				
				I_GetConceptData snomedConcept = Terms.get().getConcept(Taxonomies.SNOMED.getUuids());
				for ( I_DescriptionVersioned desc : snomedConcept.getDescriptions())
				{
					I_DescriptionTuple tuple = desc.getLastTuple();
					int currentState = tuple.getStatusNid();
					
					if ((currentState == currentStatusNid) &&
						(tuple.getText().contains(releaseSearchString)))
					{
						int releaseStringLocation = tuple.getText().indexOf(releaseSearchString);
						int releaseStringLength = tuple.getText().length();
						String dateString = tuple.getText().substring(releaseStringLocation, releaseStringLength);
						String normalizedDateString = dateString.substring(0, 3) + "-" + 
													  dateString.substring(4, 2) + "-" +
													  dateString.substring(6,2);
						
						if (earliestWorkflowHistoryRelease.compareTo(normalizedDateString) >= 0)
						{
							releases.add(desc);
						}
					}
				}
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.SEVERE, "Failure in identifying the workflow history releases", e);
			}
		}
		
		return releases;
	}


	// @ TODO
	/*
	
	
	search backwards pre release till found


	
	getLatestWfForConcept
		search backwards pre release till WF FOund, then till begin Action
		
	getLatestWfRowForConcept
	*/

}

// @TODO 
//Add if (currentState == currentStatusNid)
