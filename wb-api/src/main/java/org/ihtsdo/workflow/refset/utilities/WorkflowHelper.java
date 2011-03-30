package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.example.binding.Taxonomies;
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
	private static HashMap<String, I_GetConceptData> actions = null;
	private static HashMap<String, I_GetConceptData> states = null;
	
	private static I_GetConceptData leadModeler = null;
	private static I_GetConceptData defaultModeler = null;
	private static I_GetConceptData snomedConcept = null;

	private static int currentNid = 0;
	private static int isARelNid = 0;
	private static Set<UUID> beginWorkflowActions = null;
	private static UUID endWorkflowActionUid = null;

	public static final int EARLIEST_WORKFLOW_HISTORY_YEAR = 2007;
	public static final int EARLIEST_WORKFLOW_HISTORY_MONTH = Calendar.OCTOBER; 
	public static final int EARLIEST_WORKFLOW_HISTORY_DATE = 19;
	private static final String unrecognizedLoginMessage = "Login is unrecognlized.  You will be defaulted to generic-user workflow permissions";

	public WorkflowHelper() {
		initialize();
	}
	
	public static void initialize() {

		try {
			currentNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid());
			isARelNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid());
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.SEVERE, "Error in creating WF Class WorkflowHelper", e);
		}
	}

	
	private static class WfHxConceptComparer implements Comparator<I_GetConceptData> {
		@Override
		public int compare(I_GetConceptData o1, I_GetConceptData o2) {
			try {
				return (o1.getInitialText().toLowerCase().compareTo(o2.getInitialText().toLowerCase()));
			} catch (IOException e) {
	        	AceLog.getAppLog().log(Level.SEVERE, "Error in creating WF Class WfHxConceptComparer", e);
			}
			return -1;
		}
	}

	private static class WfHxUidConceptComparer implements Comparator<UUID> {
		@Override
		public int compare(UUID o1, UUID o2) {
			try {
				return (Terms.get().getConcept(o1).getInitialText().toLowerCase().compareTo(Terms.get().getConcept(o2).getInitialText().toLowerCase()));
			} catch (Exception e) {
	        	AceLog.getAppLog().log(Level.SEVERE, "Error in creating WF Class WfHxUidConceptComparer", e);
			}
			return -1;
		}
	}

	public static I_GetConceptData getCurrentModeler() throws TerminologyException, IOException {
		return modelers.get(Terms.get().getActiveAceFrameConfig().getUsername());
	}
	
	@SuppressWarnings("unchecked")
	public static String identifyPrefTerm(I_GetConceptData con)  {
        try {
			for (I_DescriptionVersioned<?> descv: con.getDescriptions()) {
			    for (I_DescriptionTuple p: descv.getTuples()) {
					if (p.getTypeNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getNid() &&
						p.getLang().equalsIgnoreCase("en"))
						return p.getText();
			    }
			}
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.SEVERE, "Error in identifying current editor", e);
		}

        return "";
    }
	@SuppressWarnings("unchecked")
	public static String identifyFSN(I_GetConceptData con)  {
		try {
			for (I_DescriptionVersioned<?> descv: con.getDescriptions()) {
			    for (I_DescriptionTuple p: descv.getTuples()) {
					if (p.getTypeNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNid() &&
						p.getLang().equalsIgnoreCase("en"))
						return p.getText();
		   		}
	   		}
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.SEVERE, "Error in identifying current concept's FSN", e);
		}

   		return "";
		
    }

	public static void retireWorkflowHistoryRow(WorkflowHistoryJavaBean bean)
 	{
		WorkflowHistoryRefsetWriter writer;

		try {
			writer = new WorkflowHistoryRefsetWriter(true);

			writer.setPathUid(bean.getPath());
			writer.setModelerUid(bean.getModeler());
			writer.setConceptUid(bean.getConcept());
			writer.setFSN(bean.getFSN());
			writer.setActionUid(bean.getAction());
			writer.setStateUid(bean.getState());

			writer.setWorkflowUid(bean.getWorkflowId());

			writer.setEffectiveTime(Long.MAX_VALUE);
			// Must use previous Refset Timestamp to revert proper Str
			writer.setWorkflowTime(bean.getWorkflowTime());

			writer.setAutoApproved(bean.getAutoApproved());
			writer.setOverride(bean.getOverridden());
			
			WorkflowHistoryRefsetWriter.lockMutex();
            writer.retireMember();
			Terms.get().addUncommitted(writer.getRefsetConcept());
			Terms.get().commit();

		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.SEVERE, "Error in retiring workflow history row: " + bean.toString(), e);
		}
		WorkflowHistoryRefsetWriter.unLockMutex();
	}

	public static void updateModelers() 
	{
		try {
	    	modelers = new HashMap<String, I_GetConceptData>();
	
			I_GetConceptData parentEditorConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid());
			Set<I_GetConceptData> editors = getChildren(parentEditorConcept);
			editors.remove(parentEditorConcept);
	
			for (I_GetConceptData editor : editors)
			{
				if (defaultModeler == null && isDefaultModeler(editor))
				{
					setDefaultModeler(editor);
				}
				
		    	modelers.put(getLoginId(editor), editor);
			}
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in updating modelers", e);
		}
    }
	

	private static String getLoginId(I_GetConceptData con) throws TerminologyException, IOException {
    	String id = identifyPrefTerm(con);

   		return id;
	}

	public static boolean isActiveModeler(String name) throws Exception
	{
		I_GetConceptData modeler = lookupModeler(name);

		return isActiveModeler(modeler);
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
	public static I_GetConceptData lookupModeler(String name) 
	{
		if (modelers == null)
			updateModelers();

		if (!modelers.containsKey(name))
		{
			try {
				if (getDefaultModeler() != null && !getDefaultModeler().getInitialText().equalsIgnoreCase(name))
				{
					AceLog.getAppLog().log(Level.WARNING, unrecognizedLoginMessage);
	
					for (I_GetConceptData modeler : modelers.values())
					{
						List<I_RelVersioned> relList = WorkflowHelper.getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_MODELER_VALUE);
	
						for (I_RelVersioned rel : relList)
						{
							if (rel != null &&
							    rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_DEFAULT_MODELER.getPrimoridalUid()).getConceptNid())
								return modeler;
						}
	
					}
				}
			} catch (Exception e ) {
            	AceLog.getAppLog().log(Level.WARNING, "Unable to lookup modeler: " + name, e);
			}

			return defaultModeler;
		}
		else
			return modelers.get(name);
    }

	public static I_GetConceptData getLeadModeler()
	{
		try {
			if (leadModeler  == null)
			{
				if (modelers == null)
					updateModelers();
	
				for (I_GetConceptData modeler : modelers.values())
				{
					List<I_RelVersioned> relList = WorkflowHelper.getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_MODELER_VALUE);
	
					for (I_RelVersioned rel : relList)
					{
						if (rel != null &&
						    rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_LEAD_MODELER.getPrimoridalUid()).getConceptNid())
							leadModeler = modeler;
					}
				}
			}
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying lead modeler", e);
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

    	bean.setWorkflowId(refset.getWorkflowId(fieldValues));
    	bean.setConcept(refComponentId);
    	bean.setState(refset.getStateUid(fieldValues));
    	bean.setPath(refset.getPathUid(fieldValues));
    	bean.setModeler(refset.getModelerUid(fieldValues));
    	bean.setAction(refset.getActionUid(fieldValues));
    	bean.setFSN(refset.getFSN(fieldValues));
    	bean.setWorkflowTime(refset.getWorkflowTime(fieldValues));
    	bean.setAutoApproved(refset.getAutoApproved(fieldValues));
    	bean.setOverridden(refset.getOverridden(fieldValues));
    	
    	bean.setEffectiveTime(timeStamp);

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
        	AceLog.getAppLog().log(Level.SEVERE, "Error in updating workflow user roles", e);
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
        	AceLog.getAppLog().log(Level.SEVERE, "Error in updating workflow states", e);
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
    			List<I_RelVersioned> relList = WorkflowHelper.getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE);

    			boolean foundCommitValue = false;
				for (I_RelVersioned rel : relList)
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
        	AceLog.getAppLog().log(Level.SEVERE, "Error in updating workflow actions", e);
    	}
    }

    @SuppressWarnings("unchecked")
	public static Set<I_GetConceptData> getChildren(I_GetConceptData concept) throws IOException, TerminologyException
    {
    	I_GetConceptData child = null;
		Set<I_GetConceptData> resultSet = new HashSet<I_GetConceptData>();

		if (currentNid == 0)
			initialize();

		resultSet.add(concept);
		Collection<? extends I_RelVersioned> relationships = concept.getDestRels();


    	if (relationships == null || relationships.size() == 0)
    		return resultSet;

    	Iterator itr = relationships.iterator();
    	while (itr.hasNext())
    	{
    		I_RelVersioned rel = (I_RelVersioned)itr.next();
    		 
    		if (rel.getStatusNid() == currentNid &&
    			rel.getTypeNid() == isARelNid)
    		{
    			child = Terms.get().getConcept(rel.getC1Id());
    			resultSet.addAll(getChildren(child));
    		}
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
		if (states == null)
		{
			states = new HashMap<String, I_GetConceptData>();

			Set<? extends I_GetConceptData> allStates = Terms.get().getActiveAceFrameConfig().getWorkflowStates();
	
			for (I_GetConceptData stateConcept : allStates)
			{
				states.put(identifyFSN(stateConcept).toLowerCase(), stateConcept);
			}
		}
		
		return states.get(state.toLowerCase());
	}

	public static I_GetConceptData lookupAction(String action) throws TerminologyException, IOException {
		if (actions == null)
		{
			actions = new HashMap<String, I_GetConceptData>();

			Set<? extends I_GetConceptData> allActions = Terms.get().getActiveAceFrameConfig().getWorkflowActions();
		
			for (I_GetConceptData actionConcept : allActions)
			{
				actions.put(identifyFSN(actionConcept).toLowerCase(), actionConcept);
			}
		}

		return actions.get(action.toLowerCase());
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

	public static List<I_RelVersioned> getWorkflowRelationship(I_GetConceptData concept, Concept desiredRelationship) 
	{
		if (currentNid == 0)
			initialize();
		
		List<I_RelVersioned> rels = new LinkedList<I_RelVersioned>();
		try 
		{
			int searchRelId = Terms.get().uuidToNative(desiredRelationship.getPrimoridalUid());
			
			I_IntSet relType = Terms.get().newIntSet();
			relType.add(Terms.get().getConcept(desiredRelationship.getPrimoridalUid()).getConceptNid());
	
			Collection<? extends I_RelVersioned> relList = concept.getSourceRels();
			
			for (I_RelVersioned rel : relList)
			{
				I_RelTuple tuple = rel.getLastTuple();
				int relId = tuple.getTypeNid();
				int statusId = tuple.getStatusNid();
				
				if ((relId == searchRelId) && (statusId == currentNid)) 
					rels.add(rel);
			}
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in getting workflow-based attribute", e);
		}
		
		return rels;
	}

    public static boolean isBeginWorkflowAction(I_GetConceptData actionConcept) {
    	if (beginWorkflowActions  == null)
		{
    		beginWorkflowActions = new HashSet<UUID>();
    		
    		try
	    	{
    	    	for (I_GetConceptData action : Terms.get().getActiveAceFrameConfig().getWorkflowActions())
    	    	{
					for (I_RelVersioned rel : getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE))
					{
						if (rel != null &&
			    			rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_BEGIN_WF_CONCEPT.getPrimoridalUid()).getConceptNid())
						{
    						beginWorkflowActions.add(action.getPrimUuid());
							break;
						}
					}
    	    	}
	    	} catch (Exception e) {
	        	AceLog.getAppLog().log(Level.SEVERE, "Error in identifying if current action is a BEGIN-WORKFLOW action", e);
	    	}
		}

    	if (beginWorkflowActions != null)
    		return (beginWorkflowActions.contains(actionConcept.getPrimUuid()));
    	else
    		return false;
	}

    public static boolean isEndWorkflowAction(I_GetConceptData actionConcept) {
		
    	if (endWorkflowActionUid  == null)
		{
			try
	    	{
				List<I_RelVersioned> relList = getWorkflowRelationship(actionConcept, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);
	
				for (I_RelVersioned rel : relList)
				{
					if (rel != null &&
		    			 rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_END_WF_CONCEPT.getPrimoridalUid()).getConceptNid())
					{
						endWorkflowActionUid = actionConcept.getPrimUuid();
						break;
					}
				}
	    	} catch (Exception e) {
	        	AceLog.getAppLog().log(Level.SEVERE, "Error in identifying if current action is a END-WORKFLOW action", e);
	    	}
		}

    	if (endWorkflowActionUid != null)
    		return (endWorkflowActionUid.equals(actionConcept.getPrimUuid()));
    	else
    		return false;
    }

	public static List<WorkflowHistoryJavaBean> searchForPossibleActions(I_GetConceptData modeler, I_GetConceptData concept) throws Exception
	{
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
        WorkflowHistoryJavaBean latestBean= historySearcher.getLatestWfHxJavaBeanForConcept(concept);

        if ((latestBean != null) && (!WorkflowHelper.isEndWorkflowAction(Terms.get().getConcept(latestBean.getAction()))))
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

	            templateBean.setConcept(latestBean.getConcept());
	            templateBean.setWorkflowId(latestBean.getWorkflowId());
	            templateBean.setFSN(latestBean.getFSN());
	            templateBean.setModeler(latestBean.getModeler());
	            templateBean.setPath(latestBean.getPath());
	            templateBean.setAction(key.getUids().get(0));
	            templateBean.setState(actionMap.get(key).getUids().get(0));
	            templateBean.setEffectiveTime(latestBean.getEffectiveTime());
	            templateBean.setWorkflowTime(latestBean.getWorkflowTime());
	            templateBean.setOverridden(latestBean.getOverridden());
	            templateBean.setAutoApproved(latestBean.getAutoApproved());
	            retList.add(templateBean);
	        }
        }

        return retList;
    }

	public static boolean isActiveModeler(I_GetConceptData modeler) throws TerminologyException, IOException {
		List<I_RelVersioned> relList = WorkflowHelper.getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_MODELER_VALUE);

		for (I_RelVersioned rel : relList)
		{
			if (rel != null &&
    			rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getPrimoridalUid()).getConceptNid())
				return true;
		}

		return false;
	}

	public static boolean isDefaultModeler(I_GetConceptData modeler) throws TerminologyException, IOException {
		List<I_RelVersioned> relList = WorkflowHelper.getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_MODELER_VALUE);

		for (I_RelVersioned rel : relList)
		{
			if (rel != null &&
    			rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_DEFAULT_MODELER.getPrimoridalUid()).getConceptNid())
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

	public void initializeWorkflowForConcept(I_GetConceptData concept, boolean inBatch) throws TerminologyException, IOException {
    	if (snomedConcept  == null)
    		snomedConcept = Terms.get().getConcept(Taxonomies.SNOMED.getUuids());
    	
    	if (inBatch || !WorkflowHistoryRefsetWriter.isInUse()) // Not in the middle of an existing commit
    	{
        	WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();

        	I_GetConceptData modeler = WorkflowHelper.getCurrentModeler();

        	if (modeler != null && WorkflowHelper.isActiveModeler(modeler))
        	{
        		I_TermFactory tf = Terms.get();
        		WorkflowHistoryRefsetWriter writer = new WorkflowHistoryRefsetWriter(true);

				WorkflowHistoryRefsetWriter.lockMutex();

				// Path
	            writer.setPathUid(Terms.get().nidToUuid(concept.getConceptAttributes().getPathNid()));

	            // Modeler
	            writer.setModelerUid(WorkflowHelper.getCurrentModeler().getPrimUuid());

	            // Concept & FSN
	            writer.setConceptUid(concept.getUids().iterator().next());
	            writer.setFSN(WorkflowHelper.identifyFSN(concept));

            	// Action
            	UUID actionUid = identifyAction();
                writer.setActionUid(actionUid);

                // State
                UUID initialState = identifyNextState(writer.getModelerUid(), concept, actionUid);
                writer.setStateUid(initialState);

                // Worfklow Id
                WorkflowHistoryJavaBean latestBean = searcher.getLatestWfHxJavaBeanForConcept(concept);
	            if (latestBean == null || !WorkflowHelper.isEndWorkflowAction(Terms.get().getConcept(latestBean.getAction())))
	            	writer.setWorkflowUid(UUID.randomUUID());
	            else
	            	writer.setWorkflowUid(latestBean.getWorkflowId());

	            // Set auto approved based on AceFrameConfig setting
	            if (tf.getActiveAceFrameConfig().isAutoApproveOn()) {
	            	writer.setAutoApproved(true);

	            	// Identify and overwrite Accept Action
	            	UUID acceptActionUid = identifyAcceptAction();
	            	writer.setActionUid(acceptActionUid);

	            	// Identify and overwrite Next State
	            	UUID nextState = identifyNextState(writer.getModelerUid(), concept, acceptActionUid);
					writer.setStateUid(nextState);
	            } else
	            	writer.setAutoApproved(false);

	            // Override
	            writer.setOverride(tf.getActiveAceFrameConfig().isOverrideOn());

	            // TimeStamps
		        java.util.Date today = new java.util.Date();
		        writer.setEffectiveTime(today.getTime());
		        writer.setWorkflowTime(today.getTime());

		        // Write Member
				WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
				writer.addMember();
		        Terms.get().addUncommitted(refset.getRefsetConcept());
			}
        }	
	}

	private UUID identifyNextState(UUID modelerUid, I_GetConceptData concept, UUID commitActionUid)
	{
		I_GetConceptData initialState = null;
		boolean existsInDb = isConceptInDatabase(concept);
		
		try {
			WorkflowHistoryRefsetSearcher wfSearcher = new WorkflowHistoryRefsetSearcher();
			WorkflowHistoryJavaBean bean = wfSearcher.getLatestWfHxJavaBeanForConcept(concept);
			if (bean != null)
				initialState = Terms.get().getConcept(bean.getState());
			else
			{
				for (I_GetConceptData state : Terms.get().getActiveAceFrameConfig().getWorkflowStates())
				{
					List<I_RelVersioned> relList = WorkflowHelper.getWorkflowRelationship(state, ArchitectonicAuxiliary.Concept.WORKFLOW_USE_CASE);
		
		    		for (I_RelVersioned rel : relList)
		    		{
		    			if (rel != null &&
							((existsInDb && (rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_EXISTING_CONCEPT.getPrimoridalUid()).getConceptNid())) ||
							 (!existsInDb && (rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_NEW_CONCEPT.getPrimoridalUid()).getConceptNid()))))
		    			{
							initialState = state;
		    			}
		    		}
		
		    		if (initialState != null)
		    			break;
				}
			}
		
			EditorCategoryRefsetSearcher categorySearcher = new EditorCategoryRefsetSearcher();
			I_GetConceptData modeler = Terms.get().getConcept(modelerUid);
			I_GetConceptData category = categorySearcher.searchForCategoryForConceptByModeler(modeler, concept);
		
			StateTransitionRefsetSearcher nextStateSearcher = new StateTransitionRefsetSearcher();
			Map<I_GetConceptData, I_GetConceptData> possibleActions = nextStateSearcher.searchForPossibleActionsAndFinalStates(category.getConceptNid(), initialState.getConceptNid());
		
			for (I_GetConceptData transitionAction : possibleActions.keySet())
			{
				if (transitionAction.getPrimUuid().equals(commitActionUid))
				{
					return possibleActions.get(transitionAction).getPrimUuid();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
		
	private UUID identifyAction() 
	{
    	UUID commitActionUid = null;
    	try
    	{
	    	for (I_GetConceptData action : Terms.get().getActiveAceFrameConfig().getWorkflowActions())
	    	{
	    		if (WorkflowHelper.isBeginWorkflowAction(action))
	    		{
	    			List<I_RelVersioned> commitRelList = WorkflowHelper.getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE);

    	    		for (I_RelVersioned commitRel : commitRelList)
    	    		{
						if (commitRel != null &&
							commitRel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_SINGLE_COMMIT.getPrimoridalUid()).getConceptNid())
						{
								commitActionUid = action.getPrimUuid();
						}
    	    		}

    	    		if (commitActionUid != null)
    	    			break;
				}
	    	}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	return commitActionUid;
	}
	
	private boolean isConceptInDatabase(I_GetConceptData concept) {
		boolean hasBeenReleased = false;

		try {
			WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();
			int SnomedId = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());

			I_Identify idVersioned = Terms.get().getId(concept.getConceptNid());
	        for (I_IdPart idPart : idVersioned.getMutableIdParts()) {
	            if (idPart.getAuthorityNid() == SnomedId)
	            	hasBeenReleased = true;
	        }

			if (!hasBeenReleased && (searcher.getLatestWfHxJavaBeanForConcept(concept) == null))
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return true;
    }

	private UUID identifyAcceptAction() 
	{
		if (endWorkflowActionUid  == null)
		{
			try
			{
				for (I_GetConceptData action : Terms.get().getActiveAceFrameConfig().getWorkflowActions())
				{
					if (WorkflowHelper.isEndWorkflowAction(action))
					{
						endWorkflowActionUid = action.getPrimUuid();
						break;
					}
				}
			} catch (Exception e ) {
				e.printStackTrace();
			}
		}
		
		return endWorkflowActionUid;
    }
}