package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetSearcher;
import org.ihtsdo.workflow.refset.semhier.SemanticAreaHierarchyRefsetSearcher;
import org.ihtsdo.workflow.refset.strans.StateTransitionRefsetSearcher;
import org.ihtsdo.workflow.refset.utilities.RefsetSearcherUtility;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetHelper;



/* 
* @author Jesse Efron
* 
*/
public  class EditorCategoryRefsetSearcher extends RefsetSearcherUtility 
{
	public EditorCategoryRefsetSearcher()
			throws TerminologyException, IOException 
	{
		refset = new EditorCategoryRefset();
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}

	
	public I_GetConceptData searchForCategoryByModelerForCurrentConcept(I_GetConceptData modeler) throws Exception 
	{
		I_GetConceptData currentConcept = null;
		return findCategory(modeler, currentConcept);
	}
			
	public I_GetConceptData searchForCategoryByCurrentModelerForCurrentConcept() throws Exception 
	{
		I_GetConceptData currentConcept = null;
		return findCategory(WorkflowRefsetHelper.getCurrentModeler(), currentConcept);
	}
			
	public I_GetConceptData searchForCategoryByModelerForConcept(I_GetConceptData modeler, I_GetConceptData con) throws Exception 
	{
		return findCategory(modeler, con);
	}
			
	public I_GetConceptData searchForCategoryByCurrentModelerForConcept(I_GetConceptData con) throws Exception 
	{
		return findCategory(WorkflowRefsetHelper.getCurrentModeler(), con);
	}
			
	private I_GetConceptData findCategory(I_GetConceptData modeler, I_GetConceptData con) throws Exception
	{
		Set<String> currentModelerPropertySet = searchForEditorCategoryListByModeler(modeler);
		
		if (currentModelerPropertySet.size() == 0)
			throw new Exception("Couldn't find Modeler requested");
		else if (currentModelerPropertySet.size() == 1)
		{
			String tag = ((EditorCategoryRefset)refset).getSemanticTag(currentModelerPropertySet.iterator().next());
			
			if (!tag.equalsIgnoreCase("all"))
				throw new Exception("Must be All Tag if only single result");
			else
				return ((EditorCategoryRefset)refset).getEditorCategory(currentModelerPropertySet.iterator().next());
		}
		else
		{
			Map<String, I_GetConceptData> tagToCategoryMap = getPossibleModelerCategories(currentModelerPropertySet); 
			
			SemanticAreaHierarchyRefsetSearcher searcher = new SemanticAreaHierarchyRefsetSearcher();
			
			String tag = searcher.searchForMostDetailedTags(con, tagToCategoryMap.keySet());
			
			return findCategory(modeler, tag);
			
			/*
				Set<String> possibleTags = searcher.searchForMostDetailedTags(con, tagToCategoryMap.keySet());
				
				if (possibleTags == null)
					return tagToCategoryMap.get("all");
				else if (possibleTags.size() == 1)
					return tagToCategoryMap.get(possibleTags.iterator().next());
				else
					throw new Exception("Multiple Parent Semtag Permission");
			*/
		}
		
	}

	private I_GetConceptData findCategory(I_GetConceptData modeler, String tag) throws Exception {
		I_GetConceptData category = null;
		List<I_ExtendByRefPartStr> l = helper.getAllCurrentRefsetExtensions(refsetId, modeler.getNid());
		
		for (int i = 0; i < l.size(); i++)
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)l.get(i);
			String key = ((EditorCategoryRefset)refset).getSemanticTag(props.getStringValue());
			
			if (key.equalsIgnoreCase(tag))
			{
				return ((EditorCategoryRefset)refset).getEditorCategory(props.getStringValue());
			}
			else if (key.equals("all"))
			{
				category = ((EditorCategoryRefset)refset).getEditorCategory(props.getStringValue());
			}
		}

		return category;
	}
	
	public Set<String> searchForEditorCategoryListByModeler(I_GetConceptData modeler) throws Exception 
	{
		List<I_ExtendByRefPartStr> l = helper.getAllCurrentRefsetExtensions(refsetId, modeler.getNid());
		Set<String> results = new HashSet<String>();
		
		for (int i = 0; i < l.size(); i++)
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)l.get(i);
			results.add(props.getStringValue());
		}
		
		return results;
	}
	
	private Map<String, I_GetConceptData> getPossibleModelerCategories(Set<String> set) throws Exception {
		Map<String, I_GetConceptData> results = new HashMap<String, I_GetConceptData>();
		
		for (String props : set) {
			String key = ((EditorCategoryRefset)refset).getSemanticTag(props);
			I_GetConceptData val = ((EditorCategoryRefset)refset).getEditorCategory(props);
			results.put(key, val);
		}
	
		if (!results.containsKey("all"))
			throw new Exception("Multiple Hierarchies must have \"ANY\" Category");
		
		return results;
	}

	public List<WorkflowHistoryJavaBean> searchForPossibleActions(I_GetConceptData modeler, I_GetConceptData concept) throws Exception
	{
		// Get Editor Category by modeler and Concept
		EditorCategoryRefsetSearcher categegorySearcher = new EditorCategoryRefsetSearcher();
        I_GetConceptData category = categegorySearcher.searchForCategoryByModelerForConcept(modeler, concept);

		// Get Current WF Status for Concept
        WorkflowHistoryRefsetSearcher historySearcher = new WorkflowHistoryRefsetSearcher();
        SortedSet<WorkflowHistoryJavaBean> beanList = historySearcher.searchForWFHistory(concept);
        ArrayList<WorkflowHistoryJavaBean> retList = new ArrayList<WorkflowHistoryJavaBean>();

        if (beanList.size() > 0)
        {
	        WorkflowHistoryJavaBean latestBean = beanList.first();
	        
	        // Get Possible Next Actions to Next State Map from Editor Category and Current WF's useCase and state (which now will mean INITIAL-State)
	        I_GetConceptData initialState = latestBean.getState();
	        I_GetConceptData useCase = latestBean.getUseCase();
	        StateTransitionRefsetSearcher stateTransitionSearcher = new StateTransitionRefsetSearcher();
	        Map<I_GetConceptData, I_GetConceptData> actionMap = stateTransitionSearcher.searchForPossibleActionsAndFinalStates(category, useCase, initialState);
	        
	        
	        // Create Beans for future update.  Only differences in Beans will be action & state (which now will mean NEXT-State)
	
	        for (I_GetConceptData key : actionMap.keySet()) 
	        {
	            WorkflowHistoryJavaBean templateBean = new WorkflowHistoryJavaBean();
	
	            templateBean.setConceptId(latestBean.getConceptId());
	            templateBean.setWorkflowId(latestBean.getWorkflowId());
	            templateBean.setFSN(latestBean.getFSN());
	            templateBean.setModeler(latestBean.getModeler());
	            templateBean.setPath(latestBean.getPath());
	            templateBean.setTimeStamp(latestBean.getTimeStamp());
	            templateBean.setUseCase(latestBean.getUseCase());
	            
	            templateBean.setAction(key);
	            templateBean.setState(actionMap.get(key));
	            
	            retList.add(templateBean);
	        }
        }
        
        return retList;
    }
}
