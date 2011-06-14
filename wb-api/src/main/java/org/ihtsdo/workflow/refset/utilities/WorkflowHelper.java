package org.ihtsdo.workflow.refset.utilities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetSearcher;



/*
* @author Jesse Efron
*
*/
public class WorkflowHelper {

	public final static int workflowIdPosition = 0;								// 0
    public final static int conceptIdPosition = workflowIdPosition + 1;			// 1
    public final static int useCaseIgnorePosition = conceptIdPosition + 1;			// 2
    public final static int pathPosition = useCaseIgnorePosition + 1;				// 3
    public final static int modelerPosition = pathPosition + 1;					// 4
    public final static int actionPosition = modelerPosition + 1;					// 5
    public final static int statePosition = actionPosition + 1;					// 6
    public final static int fsnPosition = statePosition + 1;						// 7
    public final static int refsetColumnTimeStampPosition = fsnPosition + 1;		// 8
    public final static int timeStampPosition = refsetColumnTimeStampPosition + 1;	// 9

    public final static int numberOfColumns = timeStampPosition + 1;				// 10

	public final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static HashMap<String, I_GetConceptData> modelers = null;
	private static HashMap<String, I_GetConceptData> actions = null;
	private static HashMap<String, I_GetConceptData> states = null;
	
	private static I_GetConceptData leadModeler = null;
	private static I_GetConceptData defaultModeler = null;

	private static int currentNid = 0;
	private static int isARelNid = 0;
	private static Set<UUID> beginWorkflowActions = null;
	private static UUID endWorkflowActionUid = null;
	private static UUID endWorkflowStateUid = null;

	public static final int EARLIEST_WORKFLOW_HISTORY_YEAR = 2007;
	public static final int EARLIEST_WORKFLOW_HISTORY_MONTH = Calendar.OCTOBER; 
	public static final int EARLIEST_WORKFLOW_HISTORY_DATE = 19;
	private static final String unrecognizedLoginMessage = "Login is unrecognlized.  You will be defaulted to generic-user workflow permissions";
	
	private static int fullySpecifiedTermDescriptionTypeNid = 0;
	private static UUID overrideActionUid = null;
    

	public WorkflowHelper() {
		initialize();
	}
	
	public static void initialize() {

		try {
			currentNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid());
			isARelNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid());
			fullySpecifiedTermDescriptionTypeNid = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNid();	
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in creating WF Class WorkflowHelper with error: " + e.getMessage());
		}
	}

	private static class WfHxConceptComparer implements Comparator<I_GetConceptData> {
		@Override
		public int compare(I_GetConceptData o1, I_GetConceptData o2) {
			try {
				return (o1.getInitialText().toLowerCase().compareTo(o2.getInitialText().toLowerCase()));
			} catch (IOException e) {
	        	AceLog.getAppLog().log(Level.WARNING, "Error in creating WF Class WfHxConceptComparer with error: " + e.getMessage());
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
	        	AceLog.getAppLog().log(Level.WARNING, "Error in creating WF Class WfHxUidConceptComparer with error: " + e.getMessage());
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
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying current editor with error: " + e.getMessage());
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
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying current concept's FSN with error: " + e.getMessage());
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
        	AceLog.getAppLog().log(Level.WARNING, "Error in retiring workflow history row: " + bean.toString() + "  with error: " + e.getMessage());
		}
		WorkflowHistoryRefsetWriter.unLockMutex();
	}

	public static void updateModelers() 
	{
		try {
	    	modelers = new HashMap<String, I_GetConceptData>();
	
			I_GetConceptData parentEditorConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IHTSDO.getPrimoridalUid());
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
        	AceLog.getAppLog().log(Level.WARNING, "Error in updating modelers with error: " + e.getMessage());
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
            	AceLog.getAppLog().log(Level.WARNING, "Unable to lookup modeler: " + name + " with error: " + e.getMessage());
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
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying lead modeler with error: " + e.getMessage());
		}
		
		return leadModeler;
    }

	public static UUID lookupModelerUid(String name) throws TerminologyException, IOException
	{
		return lookupModeler(name).getPrimUuid();
    }

	public static WorkflowHistoryJavaBean populateWorkflowHistoryJavaBean(int id, UUID refComponentId, String fieldValues, long timeStamp) throws NumberFormatException, TerminologyException, IOException
    {
    	WorkflowHistoryJavaBean bean = new WorkflowHistoryJavaBean();
    	WorkflowHistoryRefset refset = new WorkflowHistoryRefset();

    	bean.setConcept(refComponentId);

    	bean.setWorkflowId(refset.getWorkflowId(fieldValues));
    	bean.setState(refset.getStateUid(fieldValues));
    	bean.setPath(refset.getPathUid(fieldValues));
    	bean.setModeler(refset.getModelerUid(fieldValues));
    	bean.setAction(refset.getActionUid(fieldValues));
    	bean.setFSN(refset.getFSN(fieldValues));
    	bean.setWorkflowTime(refset.getWorkflowTime(fieldValues));
    	bean.setAutoApproved(refset.getAutoApproved(fieldValues));
    	bean.setOverridden(refset.getOverridden(fieldValues));
    	bean.setEffectiveTime(timeStamp);
        bean.setRxMemberId(id);

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
        	AceLog.getAppLog().log(Level.WARNING, "Error in updating workflow user roles with error: " + e.getMessage());
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
        	AceLog.getAppLog().log(Level.WARNING, "Error in updating workflow states with error: " + e.getMessage());
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
        	AceLog.getAppLog().log(Level.WARNING, "Error in updating workflow actions with error: " + e.getMessage());
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
        	AceLog.getAppLog().log(Level.WARNING, "Error in getting workflow-based attribute with error: " + e.getMessage());
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
	        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying if current action is a BEGIN-WORKFLOW action with error: " + e.getMessage());
	    	}
		}

    	if (beginWorkflowActions != null)
    		return (beginWorkflowActions.contains(actionConcept.getPrimUuid()));
    	else
    		return false;
	}

    private static boolean isEndWorkflowAction(I_GetConceptData actionConcept) {
		
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
	        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying if current action is a END-WORKFLOW action with error: " + e.getMessage());
	    	}
		}

    	if (endWorkflowActionUid != null)
    		return (endWorkflowActionUid.equals(actionConcept.getPrimUuid()));
    	else
    		return false;
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
		if (inBatch || !WorkflowHistoryRefsetWriter.isInUse()) // Not in the middle of an existing commit
    	{
        	I_GetConceptData modeler = WorkflowHelper.getCurrentModeler();

        	if (modeler != null && WorkflowHelper.isActiveModeler(modeler))
        	{
        		I_TermFactory tf = Terms.get();
        		WorkflowHistoryRefsetWriter writer = new WorkflowHistoryRefsetWriter(true);

				WorkflowHistoryRefsetWriter.lockMutex();

				// Path
				// TODO: Update Path properly
	            writer.setPathUid(Terms.get().nidToUuid(concept.getConceptAttributes().getPathNid()));

	            // Modeler
	            writer.setModelerUid(WorkflowHelper.getCurrentModeler().getPrimUuid());

	            // Concept & FSN
	            writer.setConceptUid(concept.getPrimUuid());
	            writer.setFSN(WorkflowHelper.identifyFSN(concept));

            	// Action
            	UUID actionUid = identifyAction();
                writer.setActionUid(actionUid);

                // State
                UUID initialState = identifyNextState(writer.getModelerUid(), concept, actionUid);
                writer.setStateUid(initialState);

                // Worfklow Id
                WorkflowHistoryJavaBean latestBean = getLatestWfHxJavaBeanForConcept(concept);
                
                
	            if (latestBean == null || !WorkflowHelper.getAcceptAction().equals(latestBean.getAction()))
	            	writer.setWorkflowUid(UUID.randomUUID());
	            else
	            	writer.setWorkflowUid(latestBean.getWorkflowId());

	            // Set auto approved based on AceFrameConfig setting
	            if (tf.getActiveAceFrameConfig().isAutoApproveOn()) {
	            	writer.setAutoApproved(true);

	            	// Identify and overwrite Accept Action
	            	UUID acceptActionUid = getAcceptAction();
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
				writer.addMember();
				
				WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
		        Terms.get().addUncommitted(refset.getRefsetConcept());
        	}
    	}	
	}

	private UUID identifyNextState(UUID modelerUid, I_GetConceptData concept, UUID commitActionUid)
	{
		I_GetConceptData initialState = null;
		boolean existsInDb = isConceptInDatabase(concept);
		
		try {
			WorkflowHistoryJavaBean bean = getLatestWfHxJavaBeanForConcept(concept);
			if (bean != null) {
				initialState = Terms.get().getConcept(bean.getState());
			} else {
				
				for (I_GetConceptData state : Terms.get().getActiveAceFrameConfig().getWorkflowStates())
				{
					List<I_RelVersioned> relList = WorkflowHelper.getWorkflowRelationship(state, ArchitectonicAuxiliary.Concept.WORKFLOW_USE_CASE);
		
		    		for (I_RelVersioned rel : relList)
		    		{
		    			if ((rel != null) &&
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
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying next state: " + e.getMessage());
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
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying action: " + e.getMessage());
    	}

    	return commitActionUid;
	}
	
	public static UUID getApprovedState() 
	{
		if (endWorkflowStateUid  == null)
		{
			try
			{
				for (I_GetConceptData state : Terms.get().getActiveAceFrameConfig().getWorkflowStates())
				{
					if (WorkflowHelper.isEndWorkflowState(state))
					{
						endWorkflowStateUid = state.getPrimUuid();
						break;
					}
				}
			} catch (Exception e ) {
				return UUID.randomUUID();
			}
		}
		
		return endWorkflowStateUid;
    }

	private static boolean isEndWorkflowState(I_GetConceptData stateConcept) throws IOException, TerminologyException {
    	if (endWorkflowStateUid  == null)
		{
    		// TODO: Remove hardcode and add to metadata
    		endWorkflowStateUid = ArchitectonicAuxiliary.Concept.WORKFLOW_APPROVED_STATE.getPrimoridalUid();
		}

    	if (endWorkflowStateUid != null)
    		return (endWorkflowStateUid.equals(stateConcept.getPrimUuid()));
    	else
    		return false;
	}

	public static UUID getAcceptAction() 
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
				return UUID.randomUUID();
			}
		}
		
		return endWorkflowActionUid;
    }

	public static WorkflowHistoryJavaBean populateWorkflowHistoryJavaBean(I_ExtendByRef ref) {
		WorkflowHistoryJavaBean bean = null;
		
		try {
			bean = populateWorkflowHistoryJavaBean(ref.getMemberId(), 
												   Terms.get().nidToUuid(ref.getComponentNid()), 
												   ((I_ExtendByRefPartStr)ref).getStringValue(), 
												   new Long(ref.getMutableParts().get(0).getTime()));
			} catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Failure to read WfHx Java Bean from Refset Member");
		}
		
		return bean;
	}

	public static String parseSemanticTag(I_GetConceptData con) throws IOException, TerminologyException {
   		I_IntList descType = Terms.get().newIntList();
   		descType.add(fullySpecifiedTermDescriptionTypeNid);
   		I_DescriptionTuple tuple = con.getDescTuple(descType, Terms.get().getActiveAceFrameConfig());

		String s = tuple.getText();

    	return parseSemanticTag(s);
	}

	public static String parseSemanticTag(String potentialTag) {
		
		int startIndex = potentialTag.lastIndexOf('(');
		
		if (startIndex >= 0)
		{
			int endIndex = potentialTag.lastIndexOf(')');
			String retTag = potentialTag.substring(startIndex + 1, endIndex);

			return WorkflowHelper.parseSpaces(retTag);
		} else {
			return "";
		}
	}

	public static WorkflowHistoryJavaBean getLatestWfHxJavaBeanForConcept(I_GetConceptData con, UUID workflowId) throws IOException, TerminologyException 
	{
		SortedSet<WorkflowHistoryJavaBean> wfSet = getLatestWfHxForConcept(con, workflowId);
		
		if (wfSet != null) {
			return wfSet.last();
		} else {
			return null;
		}
	}

	public static WorkflowHistoryJavaBean getLatestWfHxJavaBeanForConcept(I_GetConceptData con) throws IOException, TerminologyException 
	{
		SortedSet<WorkflowHistoryJavaBean> wfSet = getLatestWfHxForConcept(con);
		
		if (wfSet == null || wfSet.size() == 0) {
			return null;
		} else {
			return wfSet.last();
		}
	}

	private static TreeSet<WorkflowHistoryJavaBean> getLatestWfHxForConcept(
			I_GetConceptData con) throws IOException, TerminologyException {
		TreeSet<WorkflowHistoryJavaBean> returnSet = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxJavaBeanComparer());
		Set<String> ignoredWorkflows = new HashSet<String>();
		WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
		
		long latestTimestamp = 0;
		String currentWorkflowId = null;

		List<? extends I_ExtendByRef> members = Terms.get().getRefsetExtensionsForComponent(Terms.get().uuidToNative(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids()), con.getConceptNid());
		
		for (I_ExtendByRef row : members) {
			int idx = row.getTuples().size() - 1;
			if (idx >= 0) {
				if (row.getTuples().get(idx).getStatusNid() == currentNid) {
					if (!ignoredWorkflows.contains(refset.getWorkflowId(((I_ExtendByRefPartStr)row).getStringValue()))) {
						WorkflowHistoryJavaBean bean = populateWorkflowHistoryJavaBean(row);
						
						if (latestTimestamp == 0 || latestTimestamp < bean.getWorkflowTime() && !currentWorkflowId.equals(bean.getWorkflowId())) {
							returnSet.clear();
							ignoredWorkflows.add(currentWorkflowId);
							
							currentWorkflowId = bean.getWorkflowId().toString();
							latestTimestamp = bean.getWorkflowTime();
						} 				
						
						returnSet.add(bean);
					}
				}
			}
		}
		
		return returnSet;
	}

	public static SortedSet<WorkflowHistoryJavaBean> getLatestWfHxForConcept(
			I_GetConceptData con, UUID workflowId) throws IOException, TerminologyException {
		WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
		TreeSet<WorkflowHistoryJavaBean> returnSet = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxJavaBeanComparer());

		List<? extends I_ExtendByRef> members = Terms.get().getRefsetExtensionsForComponent(Terms.get().uuidToNative(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids()), con.getConceptNid());
		
		for (I_ExtendByRef row : members) {
			int idx = row.getTuples().size() - 1;
			if (idx >= 0) {
				if (row.getTuples().get(idx).getStatusNid() == currentNid) {
					if (workflowId.equals(UUID.fromString(refset.getWorkflowIdAsString(((I_ExtendByRefPartStr)row).getStringValue())))) {
						returnSet.add(populateWorkflowHistoryJavaBean(row));
					}
				}
			}
		}
			
		return returnSet;
	}

	 
	public static void listWorkflowHistory(UUID uuid) throws NumberFormatException, IOException, TerminologyException 
	{
		Writer outputFile = new OutputStreamWriter(new FileOutputStream("C:\\Users\\jefron\\Desktop\\wb-bundle\\log\\Output.txt"));
		int counter = 0;
		WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
		for (I_ExtendByRef row : Terms.get().getRefsetExtensionsForComponent(refset.getRefsetId(), Terms.get().uuidToNative(uuid))) 
		{
			WorkflowHistoryJavaBean bean = populateWorkflowHistoryJavaBean(row);
			System.out.println("\n\nBean #: " + counter++ + " = " + bean.toString());
			outputFile.write("\n\nBean #: " + counter++ + " = " + bean.toString());
		}
		outputFile.flush();
		outputFile.close();
	}

	public static Object getOverrideAction() {
	   	if (overrideActionUid   == null)
		{
    		// TODO: Remove hardcode and add to metadata
	   		try {
				overrideActionUid = ArchitectonicAuxiliary.Concept.WORKFLOW_OVERRIDE_ACTION.getPrimoridalUid();
			} catch (Exception e) {
	        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying override action: " + e.getMessage());
			}
		}
		
	   	return overrideActionUid;
    }
 
	public static boolean hasBeenInitialized() {
		return (states != null && actions != null && modelers != null);
	}

	public static String parseSpaces(String semTag) {
		semTag.trim();
		
		while (semTag.contains(" ")) {
			int idx = semTag.indexOf(' ');
			semTag = semTag.substring(0, idx) + semTag.substring(idx + 1);
		}
		
		return semTag;
	}

	public static TreeSet<WorkflowHistoryJavaBean> getAllWorkflowHistory(I_GetConceptData concept) {
		TreeSet<WorkflowHistoryJavaBean> retSet = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxEarliestFirstTimeComparer());
		
		try {
			List<? extends I_ExtendByRef> members = 
					Terms.get().getRefsetExtensionsForComponent(Terms.get().uuidToNative(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids()), 
																concept.getConceptNid());
			
			for (I_ExtendByRef row : members) {
				int idx = row.getTuples().size() - 1;
	
				if (idx >= 0) {
					if (row.getTuples().get(idx).getStatusNid() == currentNid) {
						retSet.add(populateWorkflowHistoryJavaBean(row));
					}
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Cannot access Workflow History Refset members with error: " + e.getMessage());
		}
		return retSet;
	}

	public static String getPreferredTerm(I_GetConceptData con) throws IOException, TerminologyException {
		for (I_DescriptionVersioned<?> descv: con.getDescriptions()) {
		    for (I_DescriptionTuple p: descv.getTuples()) {
				if (p.getTypeNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getNid() &&
					p.getLang().equalsIgnoreCase("en"))
					return p.getText();
		    }
		}
		
		return "Unidentifiable";
	}

	public static String shrinkTermForDisplay(String term) {
		StringBuffer retBuf = new StringBuffer();
		
		String words[] = term.split(" ");
		for (int i = 0; i < words.length; i++) {
			if (words[i].equalsIgnoreCase("workflow")) {
				return retBuf.toString().trim();
			} else {
				retBuf.append(words[i] + " ");
			}
		}
		
		return "Unidentifiable";
	}

	private boolean isConceptInDatabase(I_GetConceptData concept) {
		boolean hasBeenReleased = false;

		try {
			int snomedId = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());

			I_Identify idVersioned = Terms.get().getId(concept.getConceptNid());
	        for (I_IdPart idPart : idVersioned.getMutableIdParts()) {
	            if (idPart.getAuthorityNid() == snomedId)
	            	hasBeenReleased = true;
	        }

			if (!hasBeenReleased && (getLatestWfHxForConcept(concept) == null))
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return true;
    }

	public static WorkflowHistoryJavaBean populateWorkflowHistoryJavaBean(I_ExtendByRefVersion refsetVersion) {
			WorkflowHistoryJavaBean bean = null;

			try {
				I_ExtendByRefPartStr strPart = (I_ExtendByRefPartStr)refsetVersion.getMutablePart(); 

				bean = populateWorkflowHistoryJavaBean(refsetVersion.getMemberId(), 
													   Terms.get().nidToUuid(refsetVersion.getComponentId()), 
													   strPart.getStringValue(), 
													   new Long(refsetVersion.getTime()));
			} catch (Exception e) {
	            AceLog.getAppLog().log(Level.WARNING, "Failure to read WfHx Java Bean from Refset Member");
			}
			
			return bean;
		}

	}
