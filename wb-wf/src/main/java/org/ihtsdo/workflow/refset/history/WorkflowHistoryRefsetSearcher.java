package org.ihtsdo.workflow.refset.history; 

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.cement.WorkflowAuxiliary;
import org.ihtsdo.workflow.refset.utilities.RefsetSearcherUtility;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetHelper;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefsetSearcher extends RefsetSearcherUtility {
	
	public WorkflowHistoryRefsetSearcher()
		throws TerminologyException, IOException 
	{
		refset = new WorkflowHistoryRefset();
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}

	public I_GetConceptData searchForWorkflowStateByWorkflowId(int workflowId) throws TerminologyException, IOException {
		
		return Terms.get().getConcept(WorkflowAuxiliary.Concept.FIRST_REVIEW.getUids());
		
	}

	public I_GetConceptData searchForUseCaseByWorkflowId(int workflowId) throws TerminologyException, IOException {
		
		return Terms.get().getConcept(WorkflowAuxiliary.Concept.EDIT_USE_CASE.getUids());
		
	}
	
	public boolean searchIfExistingConceptExists(I_GetConceptData con) throws TerminologyException, IOException, Exception {
		WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
		
		UUID matchConceptUid = con.getUids().iterator().next();
		
		List<I_ExtendByRefPartStr> l = helper.getAllCurrentRefsetExtensions(refsetId, Terms.get().getConcept(WorkflowAuxiliary.Concept.WORKFLOW_HISTORY_INFORMATION.getUids()).getConceptId());
		
		for (int i = 0; i < l.size(); i++)
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)l.get(i);
			UUID searchConceptUid = refset.getConceptId(props.getStringValue());
		
			if (searchConceptUid == matchConceptUid)
				return true;
		}
		
		return false;
	}

	public SortedSet<WorkflowHistoryJavaBean> searchForWFHistory(I_GetConceptData con) throws TerminologyException, IOException, Exception {
		WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
		
		UUID matchConceptUid = con.getUids().iterator().next();
		
		List<I_ExtendByRefPartStr> l = helper.getAllCurrentRefsetExtensions(refsetId, Terms.get().getConcept(WorkflowAuxiliary.Concept.WORKFLOW_HISTORY_INFORMATION.getUids()).getConceptId());
		
		ConceptWFHistoryComparer comparator = new ConceptWFHistoryComparer();
		SortedSet<WorkflowHistoryJavaBean> sortedConceptList = new TreeSet<WorkflowHistoryJavaBean>(comparator);
		
	    // Note: latest addition to WF History is first in returned list
		for (int i = l.size(); i > 0; i--)
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)l.get(i-1);
			UUID searchConceptUid = refset.getConceptId(props.getStringValue());
		
			if (searchConceptUid.equals(matchConceptUid))
				sortedConceptList.add(WorkflowRefsetHelper.fillOutWorkflowHistoryJavaBean(props));
		}
		
		return sortedConceptList;
	}
	
	public SortedMap<I_GetConceptData, WorkflowHistoryJavaBean> listWFHistoryRefsetMembers() throws TerminologyException, IOException, Exception {
		WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
		
		List<I_ExtendByRefPartStr> l = helper.getAllCurrentRefsetExtensions(refsetId, Terms.get().getConcept(WorkflowAuxiliary.Concept.WORKFLOW_HISTORY_INFORMATION.getUids()).getConceptId());
		
		WFHistoryComparer comparator = new WFHistoryComparer();
		SortedMap <I_GetConceptData, WorkflowHistoryJavaBean> retMap = new TreeMap<I_GetConceptData, WorkflowHistoryJavaBean>(comparator);

        // Take latest member in list
		for (int i = l.size(); i > 0; i--)
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)l.get(i-1);
			I_GetConceptData con = Terms.get().getConcept(refset.getConceptId(props.getStringValue()));
			WorkflowHistoryJavaBean bean = WorkflowRefsetHelper.fillOutWorkflowHistoryJavaBean(props);

			retMap.put(con, bean);
		}

		return retMap;
	}
	
	public class WFHistoryComparer implements Comparator<I_GetConceptData> {
		@Override
		public int compare(I_GetConceptData o1, I_GetConceptData o2) {
			try {
				return (o1.getInitialText().toLowerCase().compareTo(o2.getInitialText().toLowerCase()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
	}
	
	public class ConceptWFHistoryComparer implements Comparator<WorkflowHistoryJavaBean> {
		@Override
		public int compare(WorkflowHistoryJavaBean o1, WorkflowHistoryJavaBean o2) {
			return (o2.getTimeStamp().compareTo(o1.getTimeStamp()));
		}
	}

	

}

