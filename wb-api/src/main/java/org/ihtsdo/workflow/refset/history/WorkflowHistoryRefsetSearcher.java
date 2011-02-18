package org.ihtsdo.workflow.refset.history; 

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.ace.task.search.I_TestWorkflowHistorySearchResults;
import org.ihtsdo.tk.api.ComponentVersionBI;
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
	private static SortedMap<String, ComponentVersionBI> releasesMap = null;
	private String releaseSearchString = "version: ";
	private I_GetConceptData snomedConcept = null;
	private long earliestWorkflowHistoryReleaseTimestamp = 0;

	// First release with Workflow History
	private final String earliestWorkflowHistoryRelease = "01/31/2008"; 
	private final SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
	private final int dateStringLength = 8;

	public WorkflowHistoryRefsetSearcher()
	{
		try {
			refset = new WorkflowHistoryRefset();
			setRefsetName(refset.getRefsetName());
			setRefsetId(refset.getRefsetId());
        	snomedConcept = Terms.get().getConcept(Taxonomies.SNOMED.getUuids());
        	earliestWorkflowHistoryReleaseTimestamp = format.parse(earliestWorkflowHistoryRelease).getTime();
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
	public SortedSet<WorkflowHistoryJavaBean> searchForWFHistory( List<I_TestWorkflowHistorySearchResults> checkList, boolean wfInProgress, boolean completedWorkflow, boolean PastReleasesIncluded, String timestampBeforeSearchString, String timestampAfterSearchString) throws TerminologyException, IOException, Exception 
	{
		String normalizedBeforeTimestamp = null;
		String normalizedAfterTimestamp = null;
		UUID currentWorkflowID = null;
		SortedSet<WorkflowHistoryJavaBean> returnList = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.createWfHxFsnJavaBeanComparer());
		SortedSet<WorkflowHistoryJavaBean> sortedInputList = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.ConceptWorkflowTimestampComparer());
		long beforeTimestamp = 0;
		long afterTimestamp = 0;
		
		if (!wfInProgress && !completedWorkflow) { 
			Exception e = new Exception("Error: User must choose either Workflow In Progress or Completed.");
			AceLog.getAppLog().alertAndLog(Level.WARNING,  e.getMessage(), e);
		}

		// For Debug
		//listWorkflowHistory(); 

		// Only search specified date ranges
			
		if (PastReleasesIncluded)
		{
			if (timestampBeforeSearchString != null && timestampBeforeSearchString.length() > 0)
			{
				normalizedBeforeTimestamp = normalizeSearchTimestamp(timestampBeforeSearchString);
			}
			
			if (timestampAfterSearchString != null && timestampAfterSearchString.length() > 0)
			{
				normalizedAfterTimestamp = normalizeSearchTimestamp(timestampAfterSearchString);
			}

			sortedInputList.addAll(processAllReleases(normalizedBeforeTimestamp, normalizedAfterTimestamp));
		}	
		
		// Always Add current release
		sortedInputList.addAll(processCurrentRelease());
		
		Map<UUID, TreeSet<WorkflowHistoryJavaBean>> trimmedInputList = trimBasedOnFilters(sortedInputList, wfInProgress, completedWorkflow);
		Map<UUID, TreeSet<WorkflowHistoryJavaBean>> trimOutputList = new HashMap<UUID, TreeSet<WorkflowHistoryJavaBean>>();
		
		// Test each workflow removing from collection those that don't pass criteria tests
		for (UUID wfId : trimmedInputList.keySet())
		{
			boolean matchFound = false;
			TreeSet<WorkflowHistoryJavaBean> currentBucket = trimmedInputList.get(wfId);
			if (testSingleWorkflow(currentBucket, checkList))
			{
				trimOutputList.put(wfId, currentBucket);
			}
		}
		
		// Remove from most recent back to ensure displayed item within dat range
		if (timestampBeforeSearchString != null && timestampBeforeSearchString.length() > 0) 
			beforeTimestamp = format.parse(timestampBeforeSearchString).getTime();
		if (timestampAfterSearchString != null && timestampAfterSearchString.length() > 0)  
			afterTimestamp = format.parse(timestampAfterSearchString).getTime();


		for (UUID wfId : trimOutputList.keySet())
		{
			Set<WorkflowHistoryJavaBean> removeList = new HashSet<WorkflowHistoryJavaBean>();
			
			TreeSet<WorkflowHistoryJavaBean> currentBucket = trimOutputList.get(wfId);
			
			if (timestampBeforeSearchString != null && timestampBeforeSearchString.length() > 0) {
				for (WorkflowHistoryJavaBean bean : currentBucket) 
				{
					if (bean.getWorkflowTime().longValue() > beforeTimestamp) 
					{
						removeList.add(bean);
					} else
						break;
					
				}
			} 
		
			if (timestampAfterSearchString != null && timestampAfterSearchString.length() > 0)  
			{
				for (WorkflowHistoryJavaBean bean : currentBucket) 
				{
					if (bean.getWorkflowTime().longValue() < afterTimestamp)
					{
						removeList.add(bean);
					} else
						break;
				}
			}
			
			for (WorkflowHistoryJavaBean bean : removeList)
				currentBucket.remove(bean);
		} 
		
		for (UUID wfId : trimOutputList.keySet())
			returnList.addAll(trimOutputList.get(wfId));
		
		return returnList;
	}

	 private Map<UUID, TreeSet<WorkflowHistoryJavaBean>> trimBasedOnFilters(SortedSet<WorkflowHistoryJavaBean> sortedInputList,
			 													   boolean wfInProgress, 
			 													   boolean completedWorkflow) 
	{
		UUID currentWorkflow = null;
		Set<UUID> processedConcepts = new HashSet<UUID>();
		Map<UUID, TreeSet<WorkflowHistoryJavaBean>> retSet = new HashMap<UUID, TreeSet<WorkflowHistoryJavaBean>>();
		TreeSet<WorkflowHistoryJavaBean> currentWorkflowBucket = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.createTimestampComparer());
		
		for (WorkflowHistoryJavaBean bean : sortedInputList)
		{
			// Don't reprocess if concept already handled if completedWorkflow un-checked
			if (!completedWorkflow && processedConcepts.contains(bean.getConcept()))
				continue;
			 
			if (currentWorkflow != null && !currentWorkflow.equals(bean.getWorkflowId()))
			{
				// workflow set completed, identify latest bean in set
				WorkflowHistoryJavaBean latestWfBean = currentWorkflowBucket.first();
				boolean isCompletedWorkflow = isFinalState(currentWorkflowBucket);

				if (wfInProgress && !completedWorkflow)
				{
					// Only add/process if incomplete workflow
					// Also, don't search for this concept any more
					if (!isCompletedWorkflow)
					{
						retSet.put(currentWorkflow, currentWorkflowBucket);
						processedConcepts.add(latestWfBean.getConcept());
					}
				}
				else if (!wfInProgress && completedWorkflow)
				{
					if (isCompletedWorkflow)
					{
						// Only add/process if completed workflow
						retSet.put(currentWorkflow, currentWorkflowBucket);
					}
				} else {
					// Must be search for all
					retSet.put(currentWorkflow, currentWorkflowBucket);
				} 
					
				currentWorkflowBucket = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.createTimestampComparer());
				currentWorkflow = bean.getWorkflowId();
			}	
			
			if (currentWorkflow == null)
				currentWorkflow = bean.getWorkflowId();
			
			currentWorkflowBucket.add(bean);
		}

		// Handle Last row
		if (currentWorkflowBucket.size() > 0)
		{	
			WorkflowHistoryJavaBean latestWfBean = currentWorkflowBucket.last();
			boolean isCompletedWorkflow = isFinalState(currentWorkflowBucket);
	
			if (wfInProgress && !completedWorkflow)
			{
				// Only add/process if incomplete workflow
				// Also, don't search for this concept any more
				if (!isCompletedWorkflow)
				{
					retSet.put(currentWorkflow, currentWorkflowBucket);
					processedConcepts.add(latestWfBean.getConcept());
				}
			}
			else if (!wfInProgress && completedWorkflow)
			{
				if (isCompletedWorkflow)
				{
					// Only add/process if completed workflow
					retSet.put(currentWorkflow, currentWorkflowBucket);
				}
			} else {
				// Must be search for all
				retSet.put(currentWorkflow, currentWorkflowBucket);
			} 
		}

		return retSet;
	}

	private boolean isFinalState(TreeSet<WorkflowHistoryJavaBean> currentWorkflowBucket)  
	{
		try {
			// TODO: Make faster if only loop once and store results, but then updates ignored?
			for (WorkflowHistoryJavaBean bean : currentWorkflowBucket)
			{
				I_GetConceptData action = Terms.get().getConcept(bean.getAction());
		
				if (WorkflowHelper.isEndWorkflowAction(action))
				{
					return true;
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.SEVERE, "Failure in getting current release's workflow", e);
		}
		
		return false;
	}

	private String normalizeSearchTimestamp(String timestamp) {
		return timestamp.substring(0, 2) + timestamp.substring(3, 5) + timestamp.substring(6);
	}

	private SortedSet<WorkflowHistoryJavaBean> processCurrentRelease() throws IOException, NumberFormatException, TerminologyException {
		 SortedSet<WorkflowHistoryJavaBean> sortedBeans = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.ConceptWorkflowTimestampComparer());

		 for (I_ExtendByRef row : Terms.get().getRefsetExtensionsForComponent(refsetId, snomedConcept.getConceptNid())) 
		 {
			 WorkflowHistoryJavaBean bean = processWorkflowHistoryRow(row);
			
			 if (bean != null)
			 {
				 sortedBeans.add(bean);
			 }
		 }
		
		 return sortedBeans;
	}

	private SortedSet<WorkflowHistoryJavaBean> processAllReleases(String before, String after) throws NumberFormatException, IOException, TerminologyException {
	 	SortedSet<WorkflowHistoryJavaBean> sortedBeans = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.ConceptWorkflowTimestampComparer());
		SortedMap<String, ComponentVersionBI> allReleasesMap = getAllReleasesMap();

		for (String timestamp : allReleasesMap.keySet())
		{
			if ((before != null && timestamp.compareTo(before) < 0) ||
				(after != null && timestamp.compareTo(after) > 0))
				continue;
				
			// Create sorted by WfId/Timestamp list of Active WfHxJavaBeans
			int releaseNid = Terms.get().uuidToNative(allReleasesMap.get(timestamp).getPrimUuid());
			
			for (I_ExtendByRef row : Terms.get().getRefsetExtensionsForComponent(refsetId, releaseNid))
			{
				WorkflowHistoryJavaBean bean = processWorkflowHistoryRow(row);
				
				if (bean != null)
				{
					sortedBeans.add(bean);
				}
			}
		}
		
		return sortedBeans;
	}

	private WorkflowHistoryJavaBean processWorkflowHistoryRow(I_ExtendByRef row) throws NumberFormatException, TerminologyException, IOException {
		WorkflowHistoryJavaBean bean = null;

		I_ExtendByRefPartStr latestVersion = (I_ExtendByRefPartStr)row.getMutableParts().get(row.getMutableParts().size() - 1);
		int currentState = latestVersion.getStatusNid();
	
		if (currentState == currentStatusNid)
		{
			bean = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(row.getComponentNid()), latestVersion.getStringValue(), new Long(latestVersion.getTime()));
		}
		
		return bean;
	}

	private boolean testSingleWorkflow (Set<WorkflowHistoryJavaBean> singleWorkflowBucket, List<I_TestWorkflowHistorySearchResults> checkList) throws TaskFailedException {

		 totalConcepts++;
		 
		for (I_TestWorkflowHistorySearchResults results : checkList) {
		
			if (!results.test(singleWorkflowBucket)) {
		   		return false;
			}
		}
		
		return true;
	 }
	 
	
	public SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> getAllWorkflowHistory() 
	{
		// TODO: Add Sort Comparer
		SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> returnHistory = new TreeMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>>();

		SortedMap<String, ComponentVersionBI> allReleases = getAllReleasesMap();
		
		try {
			for (String timestamp : allReleases.keySet())
			{
				int releaseNid = Terms.get().uuidToNative(allReleases.get(timestamp).getPrimUuid());
				SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> releaseHistory = getWorkflowHistoryForRelease(releaseNid);
				
				returnHistory.putAll(releaseHistory);
			}

			// Current Release
			I_GetConceptData snomedConcept = Terms.get().getConcept(Taxonomies.SNOMED.getUuids());
			SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> releaseHistory = getWorkflowHistoryForRelease(snomedConcept.getNid());
			returnHistory.putAll(releaseHistory);
		} catch (Exception e)  {
			AceLog.getAppLog().log(Level.SEVERE, "Failure in getting current release's workflow", e);
		}

		return returnHistory;
	}

	public SortedMap<UUID, Map<UUID, SortedSet<WorkflowHistoryJavaBean>>> getWorkflowHistoryForRelease(int relNid) 
	{
		// TODO: Add Comparer
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
					WorkflowHistoryJavaBean bean = WorkflowHelper.fillOutWorkflowHistoryJavaBean(Terms.get().nidToUuid(historyRow.getComponentNid()), latestVersion.getStringValue(), new Long(latestVersion.getTime()));
	
					if (!returnCollection.containsKey(bean.getConcept()))
					{
						// TODO: Add Comparer
						HashMap<UUID, SortedSet<WorkflowHistoryJavaBean>> newWorkflowMap = new HashMap<UUID, SortedSet<WorkflowHistoryJavaBean>>();
						
						SortedSet<WorkflowHistoryJavaBean> newWorkflow = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.createWfHxJavaBeanComparer());
						newWorkflowMap.put(bean.getWorkflowId(), newWorkflow);
						returnCollection.put(bean.getConcept(), newWorkflowMap);
					}
					
					Map<UUID, SortedSet<WorkflowHistoryJavaBean>> conceptHistory = returnCollection.get(bean.getConcept());
					populateWorkflowCollection(conceptHistory, bean);
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
			// TODO: Add Comparer
			return new TreeMap<UUID, SortedSet<WorkflowHistoryJavaBean>>();
		}
		else
		{
			return history.get(con.getPrimUuid());
		}

	}
	
	public SortedSet<WorkflowHistoryJavaBean> getLatestWorkflowHistoryForConcept(I_GetConceptData con)
	{
		SortedSet<WorkflowHistoryJavaBean> retSet = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.createWfHxJavaBeanComparer());
		Map<UUID, SortedSet<WorkflowHistoryJavaBean>> conHx = getAllWorkflowHistoryForConcept(con);
		
		for (UUID key : conHx.keySet())
		{
			SortedSet<WorkflowHistoryJavaBean> workflowSet = conHx.get(key);
			
			if ((retSet.size() == 0) || 
				(retSet.last().getWorkflowTime().longValue() < workflowSet.last().getWorkflowTime().longValue()))
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
			// TODO: Add Comparer
			return new TreeMap<UUID, SortedSet<WorkflowHistoryJavaBean>>();
		}
		else
		{
			return history.get(con.getPrimUuid());
		}
	}
 
	public SortedSet<WorkflowHistoryJavaBean> getLatestWorkflowHistoryForConceptForRelease(I_GetConceptData con, int relNid)
	{
		// TODO: Add Comparer
		SortedSet<WorkflowHistoryJavaBean> retSet = new TreeSet<WorkflowHistoryJavaBean>();
		Map<UUID, SortedSet<WorkflowHistoryJavaBean>> conHx = getAllWorkflowHistoryForConceptForRelease(con, relNid);
		
		for (UUID key : conHx.keySet())
		{
			SortedSet<WorkflowHistoryJavaBean> workflowSet = conHx.get(key);
			
			if ((retSet.size() == 0) || 
				(retSet.last().getWorkflowTime().longValue() < workflowSet.last().getWorkflowTime().longValue()))
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
	
	private SortedMap<String, ComponentVersionBI> getAllReleasesMap() {
		// @ TODO getLatestWfForConcept -- search backwards pre release till WF FOund, then till begin Action
		// @TODO Add if (currentState == currentStatusNid)
		if (releasesMap == null)
		{
			releasesMap = new TreeMap<String, ComponentVersionBI>(WorkflowHistoryRefset.createComponentStringTimestampComparer());
			String dateString = new String();
			try {
				
				I_GetConceptData snomedConcept = Terms.get().getConcept(Taxonomies.SNOMED.getUuids());
				for ( I_DescriptionVersioned desc : snomedConcept.getDescriptions())
				{
					I_DescriptionTuple tuple = desc.getLastTuple();
					int currentState = tuple.getStatusNid();
					
					if (tuple.getText().contains(releaseSearchString))
					{
						int releaseStringLocation = tuple.getText().indexOf(releaseSearchString);
						int releaseStringLength = releaseSearchString.length();
						int dateStartingPos = releaseStringLocation + releaseStringLength;
						int totalTagAndDateStringLength = dateStartingPos + dateStringLength;
						
						try {
							if (tuple.getText().length() < totalTagAndDateStringLength)
								 throw new IllegalArgumentException();

							dateString = tuple.getText().substring(dateStartingPos, totalTagAndDateStringLength);
						
							String normalizedDateString = dateString.substring(4, 6) + "/" +
														  dateString.substring(6,8) + "/" + 
							  							  dateString.substring(0, 4);
							Date d = format.parse(normalizedDateString);
							if (earliestWorkflowHistoryReleaseTimestamp <= d.getTime())
							{
								releasesMap.put(normalizedDateString, desc);
							}
						} catch (IllegalArgumentException iae) {
							AceLog.getAppLog().log(Level.WARNING, "dateString should be 8 characters long: " + dateString);
						} catch (ParseException pe) {
							AceLog.getAppLog().log(Level.WARNING, "String with <version: > tag not followed by valid date: " + dateString);
						}
					}
				}
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.SEVERE, "Failure in identifying the workflow history releases", e);
			}
		}
		
		return releasesMap;
	}
}

