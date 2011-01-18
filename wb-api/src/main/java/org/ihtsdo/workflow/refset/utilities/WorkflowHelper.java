package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetSearcher;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetSearcher;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowHelper {
	private static HashMap<String, I_GetConceptData> modelers = null;
	private static HashMap<String, UUID> modelersUuids = null;
	private static I_IntSet allowedTypes = null;
	private static I_IntSet allowedStatuses = null;
	private static PositionSetReadOnly viewPositions = null;
	private static Precedence precedencePolicy = null;
	private static I_ManageContradiction contractionResolutionStrategy = null;
	private static I_GetConceptData leadModeler = null;
	private static final String unrecognizedLoginMessage = "Login is unrecognlized.  You will be defaulted to generic-user workflow permissions";
	private static int fsnDescriptionTypeNid = 0;
	private static I_GetConceptData defaultModeler = null;
	
	private static class WfHxConceptComparer implements Comparator<I_GetConceptData> {
		@Override
		public int compare(I_GetConceptData o1, I_GetConceptData o2) {
			try {
				return (o1.getInitialText().toLowerCase().compareTo(o2.getInitialText().toLowerCase()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return -1;
		}
	}

	private static class WfHxUidConceptComparer implements Comparator<UUID> {
		@Override
		public int compare(UUID o1, UUID o2) {
			try {
				return (Terms.get().getConcept(o1).getInitialText().toLowerCase().compareTo(Terms.get().getConcept(o2).getInitialText().toLowerCase()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return -1;
		}
	}
	
	public WorkflowHelper() {
		allowedTypes = Terms.get().newIntSet();

		try {
			allowedTypes.add(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid()).getConceptNid());
			allowedStatuses = Terms.get().getActiveAceFrameConfig().getAllowedStatus();
			viewPositions = Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly();
			precedencePolicy = Terms.get().getActiveAceFrameConfig().getPrecedence();
			contractionResolutionStrategy = Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy();
			fsnDescriptionTypeNid =  Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNid();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static I_GetConceptData getCurrentModeler() throws TerminologyException, IOException {
		return modelers.get(Terms.get().getActiveAceFrameConfig().getUsername());
	}

	@SuppressWarnings("unchecked")
	public static String identifyFSN(I_GetConceptData con)  {
    	Collection<I_DescriptionVersioned> c;
		try {
   	   		I_IntList descType = Terms.get().newIntList();
   	   		descType.add(fsnDescriptionTypeNid);
   	   		return con.getDescTuple(descType, Terms.get().getActiveAceFrameConfig()).getText();
/*
			c = (Collection<I_DescriptionVersioned>) con.getDescriptions();
    	
	    	Iterator<I_DescriptionVersioned> itr = c.iterator();
	    	I_TermFactory tf = Terms.get();
	    	
	   		while (itr.hasNext())
	   		{
	   	   		I_DescriptionVersioned v = (I_DescriptionVersioned)itr.next();
	
		   		for (I_DescriptionTuple tuple : v.getTuples())
		   		{
		   			if (tuple.getTypeNid() == tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNid())
		   				return tuple.getText();
		   		}
	   		}
*/
   	   	} catch (Exception e) {
			e.printStackTrace();
		}
   		
   		return "";
    }
/*
	public static void retireWorkflowHistoryRow(WorkflowHistoryJavaBean wfhjb) {
		System.out.println(wfhjb);

		I_TermFactory tf = Terms.get(); 
		WorkflowHistoryRefsetWriter writer;

		try {
			writer = new WorkflowHistoryRefsetWriter();
			        	
			if (!WorkflowHistoryRefsetWriter.isInUse()) {
		
				WorkflowHistoryRefsetWriter.lockMutex();
				
				writer.setPathUid(wfhjb.getPath());
				writer.setModelerUid(wfhjb.getModeler());
				writer.setConceptUid(wfhjb.getConceptId());
				writer.setFSN(wfhjb.getFSN());
				writer.setUseCaseUid(wfhjb.getUseCase());
				writer.setActionUid(wfhjb.getAction());
				writer.setStateUid(wfhjb.getState());
				
				writer.setWorkflowUid(wfhjb.getWorkflowId());
				
				writer.setTimeStamp(System.currentTimeMillis());
				writer.setRefsetColumnTimeStamp(wfhjb.getRefsetColumnTimeStamp());

				writer.retireMember();
				WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
				tf.addUncommitted(refset.getRefsetConcept());
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		
		WorkflowHistoryRefsetWriter.unLockMutex();
		
	}
*/
	public static void retireWorkflowHistoryRow(WorkflowHistoryJavaBean wfhjb) 
 	{
		WorkflowHistoryRefsetWriter writer;

		try {
			writer = new WorkflowHistoryRefsetWriter();

			writer.setPathUid(wfhjb.getPath());
			writer.setModelerUid(wfhjb.getModeler());
			writer.setConceptUid(wfhjb.getConceptId());
			writer.setFSN(wfhjb.getFSN());
			writer.setUseCaseUid(wfhjb.getUseCase());
			writer.setActionUid(wfhjb.getAction());
			writer.setStateUid(wfhjb.getState());

			writer.setWorkflowUid(wfhjb.getWorkflowId());

			java.util.Date today = new java.util.Date();
			writer.setTimeStamp(today.getTime());
			// Must use previous Refset Timestamp to revert proper Str 
			writer.setRefsetColumnTimeStamp(wfhjb.getRefsetColumnTimeStamp());

			WorkflowHistoryRefsetWriter.lockMutex();
			writer.retireMember();
			Terms.get().addUncommitted(writer.getRefsetConcept());
			Terms.get().commit();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		WorkflowHistoryRefsetWriter.unLockMutex();
	}

	private static void updateModelers() throws TerminologyException, IOException 
	{
    	modelers = new HashMap<String, I_GetConceptData>();
    	modelersUuids = new HashMap<String, UUID>();
    	I_TermFactory tf = Terms.get();
		
		I_GetConceptData parentEditorConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IHTSDO.getPrimoridalUid());
		Set<I_GetConceptData> editors = getChildren(parentEditorConcept);
		editors.remove(parentEditorConcept);
		
		for (I_GetConceptData editor : editors)
		{
	    	modelers.put(getSynonym(editor), editor);
		}
    }

	private static String getSynonym(I_GetConceptData con) throws TerminologyException, IOException {
    	Collection<? extends I_DescriptionVersioned> c =  con.getDescriptions();
/*
    	I_TermFactory tf = Terms.get();
   		
    	Iterator itr = c.iterator();
    	
    	while (itr.hasNext())
   		{
   	   		I_DescriptionVersioned v = (I_DescriptionVersioned)itr.next();

	   		for (I_DescriptionTuple tuple : v.getTuples())
	   		{
	   			I_DescriptionTuple t = tuple;

	   			if (tuple.getTypeNid() == tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getNid())
	   				return tuple.getText();
	   		}
   		}
   		
   		return "";
*/
    		if (fsnDescriptionTypeNid == 0)
    			fsnDescriptionTypeNid =  Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNid();
    		
			int prefTermDescriptionTypeNid =  Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getNid();
    		
    		I_IntList descType = Terms.get().newIntList();
   	   		descType.add(prefTermDescriptionTypeNid);
   	   		
   	   		String prefTermStr = con.getDescTuple(descType, Terms.get().getActiveAceFrameConfig()).getText();
   	   		
   	   		// TODO Remove this as fix to deal with gen-user & two Alos
   	   		if (prefTermStr.contains("-"))
				WorkflowHelper.setDefaultModeler(con);

   	   		if (prefTermStr.equalsIgnoreCase("alopez"))
   	   			prefTermStr = "alejandro";

   	   		return prefTermStr;
	}

	public static boolean isCurrentModeler(String name) throws Exception
	{
		I_GetConceptData modeler = lookupModeler(name);

		return isCurrentModeler(modeler);
	}

	public static Set<String> getModelerKeySet() {
		return modelers.keySet();
	}
	
	/**
	 * @param name
	 * @return
	 * @throws TerminologyException
	 * @throws IOException
	 */
	/**
	 * @param name
	 * @return
	 * @throws TerminologyException
	 * @throws IOException
	 */
	public static I_GetConceptData lookupModeler(String name) throws TerminologyException, IOException 
	{
		if (modelers == null)
			updateModelers();

   		if (name.equalsIgnoreCase("alopez"))
   			name = "alejandro";

		if (!modelers.containsKey(name))
		{
			if (getDefaultModeler() != null && !getDefaultModeler().getInitialText().equalsIgnoreCase(name))
			{
				AceLog.getAppLog().alertAndLog(Level.WARNING, unrecognizedLoginMessage, new Exception(unrecognizedLoginMessage));
	
				for (I_GetConceptData modeler : modelers.values())
				{
					List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_MODELER_VALUE);
					
					for (I_RelTuple rel : relList)
					{
						if (rel != null &&
						    rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_DEFAULT_MODELER.getPrimoridalUid()).getConceptNid())
							return modeler;
					}
	
				}
			}
			
			return defaultModeler;
		}
		else
			return modelers.get(name);			
    }

	public static I_GetConceptData getLeadModeler() throws TerminologyException, IOException 
	{
		if (leadModeler  == null)
		{
			if (modelers == null)
				updateModelers();
	
			for (I_GetConceptData modeler : modelers.values())
			{
				List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_MODELER_VALUE);
				
				for (I_RelTuple rel : relList)
				{
					if (rel != null &&
					    rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_LEAD_MODELER.getPrimoridalUid()).getConceptNid())
						leadModeler = modeler;
				}
			}
		}
		
		return leadModeler;
    }

	public static UUID lookupModelerUid(String name) throws TerminologyException, IOException 
	{
		return lookupModeler(name).getPrimUuid();			
    }

    public static WorkflowHistoryJavaBean fillOutWorkflowHistoryJavaBean(UUID refComponentId, String fieldValues, long timeStamp) throws NumberFormatException, TerminologyException, IOException
    {
    	WorkflowHistoryJavaBean bean = new WorkflowHistoryJavaBean();
    	WorkflowHistoryRefset refset = new WorkflowHistoryRefset();

    	bean.setConceptId(refComponentId);
    	bean.setWorkflowId(refset.getWorkflowId(fieldValues));
    	
    	bean.setAction(refset.getActionUid(fieldValues));
    	bean.setState(refset.getStateUid(fieldValues));
    	bean.setPath(refset.getPathUid(fieldValues));
    	bean.setModeler(refset.getModelerUid(fieldValues));
    	bean.setAction(refset.getActionUid(fieldValues));
    	bean.setUseCase(refset.getUseCaseUid(fieldValues));
    	bean.setState(refset.getStateUid(fieldValues));
    	bean.setFSN(refset.getFSN(fieldValues));
    	bean.setRefsetColumnTimeStamp(refset.getRefsetColumnTimeStamp(fieldValues));
    	bean.setTimeStamp(timeStamp);
    	bean.setAutoApproved(refset.getAutoApproved(fieldValues));
    	bean.setOverridden(refset.getOverridden(fieldValues));
    	
    	return bean;
    }

    public static void updateWorkflowUserRoles() {
		TreeSet <I_GetConceptData> sortedRoles = new TreeSet<I_GetConceptData>(new WfHxConceptComparer());
    		
   	   	try {
   			I_GetConceptData rolesParentConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ROLES.getPrimoridalUid());

   			Set<? extends I_GetConceptData> workflowRoles = getChildren(rolesParentConcept);
    		workflowRoles.remove(rolesParentConcept);
    		
    		sortedRoles.addAll(workflowRoles);

    		Terms.get().getActiveAceFrameConfig().setWorkflowRoles(sortedRoles);    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public static void updateWorkflowStates() {
		TreeSet <I_GetConceptData> sortedStates = new TreeSet<I_GetConceptData>(new WfHxConceptComparer());

		try {
			I_GetConceptData statesParentConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_STATES.getPrimoridalUid());

    		Set<? extends I_GetConceptData> workflowStates = getChildren(statesParentConcept);
    		workflowStates.remove(statesParentConcept);

    		sortedStates.addAll(workflowStates);

    		Terms.get().getActiveAceFrameConfig().setWorkflowStates(sortedStates);    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public static void updateWorkflowActions() {
		Set<UUID> availableActions = new HashSet<UUID>();
		TreeSet <I_GetConceptData> sortedActions = new TreeSet<I_GetConceptData>(new WfHxConceptComparer());
		TreeSet <UUID> sortedAvailableActions = new TreeSet<UUID>(new WfHxUidConceptComparer());

		try {
    		I_GetConceptData actionParentConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIONS.getPrimoridalUid());
    		
    		Set<? extends I_GetConceptData> workflowActions = getChildren(actionParentConcept);
    		workflowActions.remove(actionParentConcept);

    		for (I_GetConceptData action : workflowActions)
    		{
    			// Only add non-Commit actions
    			List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE);

    			boolean foundCommitValue = false;
				for (I_RelTuple rel : relList)
				{
					if (rel != null && 
						(rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_SINGLE_COMMIT.getPrimoridalUid()).getConceptNid() ||
						 rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_BATCH_COMMIT.getPrimoridalUid()).getConceptNid()))
						foundCommitValue = true;
				}

    			if (!foundCommitValue)
    				availableActions.add(action.getPrimUuid());
    		}
    		
    		sortedActions.addAll(workflowActions);
    		sortedAvailableActions.addAll(availableActions);

    		Terms.get().getActiveAceFrameConfig().setWorkflowActions(sortedActions);    		
    		Terms.get().getActiveAceFrameConfig().setAllAvailableWorkflowActionUids(sortedAvailableActions);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    @SuppressWarnings("unchecked")
	public static Set<I_GetConceptData> getChildren(I_GetConceptData concept) throws IOException, TerminologyException 
    {
    	I_GetConceptData child = null;
		Set<I_GetConceptData> resultSet = new HashSet<I_GetConceptData>();

		resultSet.add(concept);
		Set childSet = concept.getDestRelOrigins(allowedStatuses, allowedTypes, viewPositions, precedencePolicy, contractionResolutionStrategy); 
		
    	if (childSet == null || childSet.size() == 0)
    		return resultSet;
    	
    	Iterator itr = childSet.iterator();
    	while (itr.hasNext())
    	{
    		child = (I_GetConceptData)itr.next();
    		resultSet.addAll(getChildren(child));
    	}
    	
    	return resultSet;
    }

	public static I_GetConceptData lookupEditorCategory(String role) throws TerminologyException, IOException {
		Set<? extends I_GetConceptData> allRoles = Terms.get().getActiveAceFrameConfig().getWorkflowRoles();
		
		for (I_GetConceptData roleConcept : allRoles)
		{
			if (identifyFSN(roleConcept).equalsIgnoreCase(role.trim()))
				return roleConcept;
		}
		
		return null;
	}

	public static I_GetConceptData lookupState(String state) throws TerminologyException, IOException {
		Set<? extends I_GetConceptData> allStates = Terms.get().getActiveAceFrameConfig().getWorkflowStates();
		
		for (I_GetConceptData stateConcept : allStates)
		{
			if (identifyFSN(stateConcept).equalsIgnoreCase(state.trim())) {
				return stateConcept;
			}
		}
		
		return null;
	}

	public static I_GetConceptData lookupAction(String action) throws TerminologyException, IOException {
		Set<? extends I_GetConceptData> allActions = Terms.get().getActiveAceFrameConfig().getWorkflowActions();
		
		for (I_GetConceptData actionConcept : allActions)
		{
			if (identifyFSN(actionConcept).equalsIgnoreCase(action.trim()))
				return actionConcept;
		}
		
		return null;
	}

	public static I_GetConceptData lookupRoles(String role) throws TerminologyException, IOException {
		Set<? extends I_GetConceptData> allActions = Terms.get().getActiveAceFrameConfig().getWorkflowRoles();
		
		for (I_GetConceptData actionConcept : allActions)
		{
			if (identifyFSN(actionConcept).equalsIgnoreCase(role.trim()))
				return actionConcept;
		}
		
		return null;
	}

	public static List<? extends I_RelTuple> getWorkflowRelationship(I_GetConceptData concept, Concept desiredRelationship) throws TerminologyException, IOException 
	{
		I_IntSet relType = Terms.get().newIntSet();
		relType.add(Terms.get().getConcept(desiredRelationship.getPrimoridalUid()).getConceptNid());
		
		List<? extends I_RelTuple> rels =  concept.getSourceRelTuples(allowedStatuses, relType, viewPositions, precedencePolicy, contractionResolutionStrategy); 
		
		if (rels.size() == 0)
			return new LinkedList<I_RelTuple>();
		else
			return rels;
	}

    public static boolean isBeginEndAction(UUID action) {
		try 
    	{
	        I_GetConceptData actionConcept = Terms.get().getConcept(action);
			
			List<? extends I_RelTuple> relList = getWorkflowRelationship(actionConcept, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);
	
			for (I_RelTuple rel : relList)
			{
				if (rel != null && 
	    			(rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_END_WF_CONCEPT.getPrimoridalUid()).getConceptNid()) ||
	    			 rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_BEGIN_WF_CONCEPT.getPrimoridalUid()).getConceptNid())
					return true;
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return false;
    }

	public static List<WorkflowHistoryJavaBean> searchForPossibleActions(I_GetConceptData modeler, I_GetConceptData concept) throws Exception
	{
		boolean isAcceptAction = false;
		EditorCategoryRefsetSearcher categegorySearcher = new EditorCategoryRefsetSearcher();
        WorkflowHistoryRefsetSearcher historySearcher = new WorkflowHistoryRefsetSearcher();
        ArrayList<WorkflowHistoryJavaBean> retList = new ArrayList<WorkflowHistoryJavaBean>();

		// Get Editor Category by modeler and Concept
        I_GetConceptData category = categegorySearcher.searchForCategoryForConceptByModeler(modeler, concept);
        if (category == null) {
            return new ArrayList<WorkflowHistoryJavaBean>();
        }
        
        int categoryNid = category.getConceptNid();

		// Get Current WF Status for Concept
        SortedSet<WorkflowHistoryJavaBean> beanList = historySearcher.getWfHxByConcept(concept);
        Iterator<WorkflowHistoryJavaBean> itr = beanList.iterator();        


        if (beanList.size() > 0)
        {
            WorkflowHistoryJavaBean latestBean = beanList.first();

            List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(Terms.get().getConcept(latestBean.getAction()), ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);
	
			for (I_RelTuple rel : relList)
			{
				if (rel != null && 
	    			rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_END_WF_CONCEPT.getPrimoridalUid()).getConceptNid())
					isAcceptAction  = true;
			}
		
			if (!isAcceptAction)
	        {
		        // Get Possible Next Actions to Next State Map from Editor Category and Current WF's useCase and state (which now will mean INITIAL-State)
		        int initialStateNid = Terms.get().uuidToNative(latestBean.getState());
		        StateTransitionRefsetSearcher stateTransitionSearcher = new StateTransitionRefsetSearcher();
		        Map<I_GetConceptData, I_GetConceptData> actionMap = stateTransitionSearcher.searchForPossibleActionsAndFinalStates(categoryNid, initialStateNid);
		        
		        
		        // Create Beans for future update.  Only differences in Beans will be action & state (which now will mean NEXT-State)
		        for (I_GetConceptData key : actionMap.keySet()) 
		        {
		        	// Such as done via Commit
		    		WorkflowHistoryJavaBean templateBean = new WorkflowHistoryJavaBean();
		
		            templateBean.setConceptId(latestBean.getConceptId());
		            templateBean.setWorkflowId(latestBean.getWorkflowId());
		            templateBean.setFSN(latestBean.getFSN());
		            templateBean.setModeler(latestBean.getModeler());
		            templateBean.setPath(latestBean.getPath());
		            templateBean.setTimeStamp(latestBean.getTimeStamp());
		            templateBean.setRefsetColumnTimeStamp(latestBean.getTimeStamp());
		            templateBean.setUseCase(latestBean.getUseCase());
		            
		            templateBean.setAction(key.getUids().get(0));
		            templateBean.setState(actionMap.get(key).getUids().get(0));	            
		            retList.add(templateBean);
		    	}
	        }
        }
        
        return retList;
    }

	public static boolean isCurrentModeler(I_GetConceptData modeler) throws TerminologyException, IOException {
		List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_MODELER_VALUE);
		
		for (I_RelTuple rel : relList)
		{
			if (rel != null && 
    			rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getPrimoridalUid()).getConceptNid())
				return true;
		}

		return false;
	}

	public static HashMap<String, I_GetConceptData> getModelers() {
		return modelers;
	}

	public static void setDefaultModeler(I_GetConceptData defMod) {
		defaultModeler  = defMod;		
	}
	
	public static I_GetConceptData getDefaultModeler() {
		return defaultModeler;		
	}
	
}