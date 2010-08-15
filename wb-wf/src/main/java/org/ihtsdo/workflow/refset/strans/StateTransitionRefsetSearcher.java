package org.ihtsdo.workflow.refset.strans;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.ihtsdo.cement.WorkflowAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.RefsetSearcherUtility;



/* 
* @author Jesse Efron
* 
*/
public  class StateTransitionRefsetSearcher extends RefsetSearcherUtility 
{
	public StateTransitionRefsetSearcher()
			throws TerminologyException, IOException 
	{
		refset = new StateTransitionRefset();
		
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}

	public Map<I_GetConceptData, I_GetConceptData> searchForPossibleActionsAndFinalStates(I_GetConceptData category, I_GetConceptData testUseCase, I_GetConceptData testInitialState) throws Exception 
	{
		Map<I_GetConceptData, I_GetConceptData> results = new HashMap<I_GetConceptData, I_GetConceptData> ();
		I_ExtendByRefPartStr props = null;
		List<I_ExtendByRefPartStr> l = null;

		// Modeler's Role
		l = helper.getAllCurrentRefsetExtensions(refsetId, category.getConceptNid());
		if (l != null)
			results = addToList(results, findPossibleActions(l, testInitialState, testUseCase));
		
		// ANY Role
		l = helper.getAllCurrentRefsetExtensions(refsetId,  Terms.get().getConcept(WorkflowAuxiliary.Concept.ROLE_ANY.getUids()).getConceptNid());
		if (l != null)
			results = addToList(results, findPossibleActions(l, testInitialState, testUseCase));

		// B+ Role
		if (category != Terms.get().getConcept(WorkflowAuxiliary.Concept.ROLE_A.getUids()))
		{
			l = helper.getAllCurrentRefsetExtensions(refsetId,  Terms.get().getConcept(WorkflowAuxiliary.Concept.ROLE_BPLUS.getUids()).getConceptNid());
			results = addToList(results, findPossibleActions(l, testInitialState, testUseCase));
		}
			
		return results;
	}
	
	public Map<I_GetConceptData, I_GetConceptData> addToList(Map<I_GetConceptData, I_GetConceptData> currentList, Map<I_GetConceptData, I_GetConceptData> newList) 
	{
		Map<I_GetConceptData, I_GetConceptData> results = new HashMap<I_GetConceptData, I_GetConceptData> ();
		
		for (I_GetConceptData key : currentList.keySet())
			results.put(key, currentList.get(key));

		for (I_GetConceptData key : newList.keySet())
			results.put(key, newList.get(key));

		return results;
	}
	
	public Map<I_GetConceptData, I_GetConceptData> findPossibleActions(List<I_ExtendByRefPartStr> l, I_GetConceptData testInitialState, I_GetConceptData testUseCase) 
	{
		Map<I_GetConceptData, I_GetConceptData> results = new HashMap<I_GetConceptData, I_GetConceptData> ();
		I_ExtendByRefPartStr props = null;

		try {
			for (int i = 0; i < l.size(); i++)
			{
				props = (I_ExtendByRefPartStr)l.get(i);
				
				if (((StateTransitionRefset)refset).getInitialState(props.getStringValue()).equals(testInitialState) &&
					((StateTransitionRefset)refset).getWorkflowType(props.getStringValue()).equals(testUseCase))
				{
					I_GetConceptData action =  ((StateTransitionRefset)refset).getAction(props.getStringValue());
					I_GetConceptData finalState =  ((StateTransitionRefset)refset).getFinalState(props.getStringValue());
					
					results.put(action, finalState);
				}
			}
		} catch (Exception e) {
			System.out.println("testInitState: " + testInitialState);
			System.out.println("testInitState: " + testUseCase);
			System.out.println("On Row: " + props.getStringValue());
			e.printStackTrace();
		}
		
		return results;
	}
}
