package org.ihtsdo.workflow.refset.utilities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
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
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.workflow.WorkflowHistoryJavaBeanBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetReader;
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

    private static int activeNidRf1 = 0;
    private static int activeNidRf2 = 0;
    
	public final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static HashMap<String, ConceptVersionBI> modelers = null;
	private static HashMap<String, ConceptVersionBI> actions = null;
	private static HashMap<String, ConceptVersionBI> states = null;
	
	private static ConceptVersionBI leadModeler = null;
	private static ConceptVersionBI defaultModeler = null;

	private static Set<UUID> beginWorkflowActions = null;
	private static Set<UUID> commitWorkflowActions = null;
	private static UUID endWorkflowActionUid = null;
	private static UUID endWorkflowStateUid = null;

	public static final int EARLIEST_WORKFLOW_HISTORY_YEAR = 2007;
	public static final int EARLIEST_WORKFLOW_HISTORY_MONTH = Calendar.OCTOBER; 
	public static final int EARLIEST_WORKFLOW_HISTORY_DATE = 19;
	private static final String unrecognizedLoginMessage = "Login is unrecognlized.  You will be defaulted to generic-user workflow permissions";
	
	private static UUID overrideActionUid = null;
	
	public static ConceptVersionBI getCurrentModeler() throws TerminologyException, IOException {
		return modelers.get(Terms.get().getActiveAceFrameConfig().getUsername());
	}
	
	public static String identifyPrefTerm(int conceptNid, ViewCoordinate vc)  {
		try {
			TerminologySnapshotDI dbSnapshot = Ts.get().getSnapshot(vc);
			return dbSnapshot.getConceptVersion(conceptNid).getPreferredDescription().getText();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying current concept's Preferred with msg: \n" + e.getMessage() + "\n");
		}

   		return "";
		
    }

	public static String identifyFSN(int conceptNid, ViewCoordinate vc)  {
		try {
			TerminologySnapshotDI dbSnapshot = Ts.get().getSnapshot(vc);
			return dbSnapshot.getConceptVersion(conceptNid).getFullySpecifiedDescription().getText();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying current concept's FSN with msg: \n" + e.getMessage() + "\n");
		}

   		return "";		
    }

	public static void retireWorkflowHistoryRow(WorkflowHistoryJavaBean bean, ViewCoordinate vc)
 	{
		try {
			boolean precedingCommitExists = isCommitWorkflowAction(Terms.get().getConcept(bean.getAction()).getVersion(vc));
			
			retireRow(bean);

			if (precedingCommitExists) {
				// Just retired preceding commit
		    	WorkflowHistoryJavaBean latestBean = getLatestWfHxJavaBeanForConcept(Terms.get().getConcept(bean.getConcept()));

		    	// Retire original Row as previously retired original
				retireRow(latestBean);
	    	}	    	
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in retiring workflow history row: " + bean.toString() + "  with error: " + e.getMessage());
		}
	}

	private static void retireRow(WorkflowHistoryJavaBean bean) throws Exception {
		WorkflowHistoryRefsetWriter writer;
		writer = new WorkflowHistoryRefsetWriter();

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
		
		WorkflowHistoryRefsetWriter.unLockMutex();
	}

	public static void updateModelers(ViewCoordinate vc) 
	{
    	modelers = new HashMap<String, ConceptVersionBI>();

    	try {
			I_GetConceptData parentEditorConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid());
			Set<ConceptVersionBI> editors = getChildren(parentEditorConcept.getVersion(vc));
			editors.remove(parentEditorConcept);
	
			for (ConceptVersionBI editor : editors)
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
	

	private static String getLoginId(ConceptVersionBI con) throws ContraditionException, IOException {
    	return con.getPreferredDescription().getText();
	}

	public static boolean isActiveModeler(String name) throws Exception
	{
		ConceptVersionBI modeler = lookupModeler(name);

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
	 * @throws IOException 
	 * @throws TerminologyException 
	 * @throws TerminologyException
	 * @throws IOException
	 */
	public static ConceptVersionBI lookupModeler(String name) throws TerminologyException, IOException 
	{
		if (modelers == null) {
			updateModelers(Terms.get().getActiveAceFrameConfig().getViewCoordinate());
		}
		
		if (name == null) {
			return defaultModeler;
		} else if (modelers.containsKey(name)) {
			return modelers.get(name);
		} else {
			try {
				if (getDefaultModeler() != null && !getDefaultModeler().getPreferredDescription().getText().equalsIgnoreCase(name))
				{
					AceLog.getAppLog().log(Level.WARNING, unrecognizedLoginMessage);
	
					for (ConceptVersionBI modeler : modelers.values())
					{
						List<RelationshipVersionBI> relList = getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS);
	
						for (RelationshipVersionBI rel : relList)
						{
							if (rel != null &&
							    rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_DEFAULT_MODELER.getPrimoridalUid()).getConceptNid())
								return modeler;
						}
	
					}
				}
			} catch (Exception e ) {
            	AceLog.getAppLog().log(Level.WARNING, "Unable to lookup modeler: " + name + " with error: " + e.getMessage());
			}

			// Couldn't find modeler in list, so return default
			return defaultModeler;
    	}
	}

	public static ConceptVersionBI getLeadModeler(ViewCoordinate vc)
	{
		try {
			if (leadModeler  == null)
			{
				if (modelers == null)
					updateModelers(vc);
	
				for (ConceptVersionBI modeler : modelers.values())
				{
					List<RelationshipVersionBI> relList = getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS);
	
					for (RelationshipVersionBI rel : relList)
					{
						if (rel != null &&
						    rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_LEAD_MODELER.getPrimoridalUid()).getConceptNid())
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
    	WorkflowHistoryRefsetReader reader = new WorkflowHistoryRefsetReader();

    	bean.setConcept(refComponentId);

    	bean.setWorkflowId(reader.getWorkflowId(fieldValues));
    	bean.setState(reader.getStateUid(fieldValues));
    	bean.setPath(reader.getPathUid(fieldValues));
    	bean.setModeler(reader.getModelerUid(fieldValues));
    	bean.setAction(reader.getActionUid(fieldValues));
    	bean.setFSN(reader.getFSN(fieldValues));
    	bean.setWorkflowTime(reader.getWorkflowTime(fieldValues));
    	bean.setAutoApproved(reader.getAutoApproved(fieldValues));
    	bean.setOverridden(reader.getOverridden(fieldValues));
    	bean.setEffectiveTime(timeStamp);
        bean.setRxMemberId(id);

    	return bean;
    }

    public static void updateWorkflowUserRoles(ViewCoordinate vc) {
    	TreeSet <ConceptVersionBI> sortedRoles = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());

   	   	try {
   			I_GetConceptData rolesParentConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ROLES.getPrimoridalUid());

   			Set<? extends ConceptVersionBI> workflowRoles = getChildren(rolesParentConcept.getVersion(vc));
    		workflowRoles.remove(rolesParentConcept);

    		sortedRoles.addAll(workflowRoles);

    		Terms.get().getActiveAceFrameConfig().setWorkflowRoles(sortedRoles);
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in updating workflow user roles with error: " + e.getMessage());
    	}
    }

    public static void updateWorkflowStates(ViewCoordinate vc) {
    	TreeSet <ConceptVersionBI> sortedStates = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());

		try {
			I_GetConceptData statesParentConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_STATES.getPrimoridalUid());

    		Set<? extends ConceptVersionBI> workflowStates = getChildren(statesParentConcept.getVersion(vc));
    		workflowStates.remove(statesParentConcept);

    		sortedStates.addAll(workflowStates);

    		Terms.get().getActiveAceFrameConfig().setWorkflowStates(sortedStates);
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in updating workflow states with error: " + e.getMessage());
    	}
    }

    public static void updateWorkflowActions(ViewCoordinate vc) {
		Set<UUID> availableActions = new HashSet<UUID>();
    	TreeSet <ConceptVersionBI> sortedActions = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createPreferredTermComparer());
		List<UUID> sortedAvailableActions = new LinkedList<UUID>();

		try {
    		I_GetConceptData actionParentConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIONS.getPrimoridalUid());

    		Set<? extends ConceptVersionBI> workflowActions = getChildren(actionParentConcept.getVersion(vc));
    		workflowActions.remove(actionParentConcept);

    		for (ConceptVersionBI action : workflowActions)
    		{
    			// Only add non-Commit actions
    			List<RelationshipVersionBI> relList = getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE);

    			boolean foundCommitValue = false;
				for (RelationshipVersionBI rel : relList)
				{
					if (rel != null &&
						(rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_SINGLE_COMMIT.getPrimoridalUid()).getConceptNid() ||
						 rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_BATCH_COMMIT.getPrimoridalUid()).getConceptNid()))
						foundCommitValue = true;
				}

    			if (!foundCommitValue)
    				availableActions.add(action.getPrimUuid());
    		}

    		sortedActions.addAll(workflowActions);
    		
    		for (ConceptVersionBI action : sortedActions) {
    			if (availableActions.contains(action.getPrimUuid())) {
    				sortedAvailableActions.add(action.getPrimUuid());
    			}
    		}

    		Terms.get().getActiveAceFrameConfig().setWorkflowActions(sortedActions);
    		Terms.get().getActiveAceFrameConfig().setAllAvailableWorkflowActionUids(sortedAvailableActions);
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in updating workflow actions with error: " + e.getMessage());
    	}
    }

	public static Set<ConceptVersionBI> getChildren(ConceptVersionBI concept) throws IOException, ContraditionException 
    {
		Set<ConceptVersionBI> resultSet = new HashSet<ConceptVersionBI>();

		if (concept != null) {		
			resultSet.add(concept);
			Collection<? extends ConceptVersionBI> children = concept.getRelsIncomingOriginsActiveIsa();

	
	    	if (children == null || children.size() == 0) {
	    		return resultSet;
			}
	    	
	    	for (ConceptVersionBI child : children) {
	    		 if (child.getConceptNid() != concept.getConceptNid()) {
	    			resultSet.addAll(getChildren(child));
	    		 }
	    	}
		}
		
    	return resultSet;
    }

	public static ConceptVersionBI lookupEditorCategory(String role, ViewCoordinate vc) throws TerminologyException, IOException, ContraditionException {
		Set<? extends ConceptVersionBI> allRoles = Terms.get().getActiveAceFrameConfig().getWorkflowRoles();

		if (role != null) {
			for (ConceptVersionBI roleConcept : allRoles)
			{
				if (roleConcept.getFullySpecifiedDescription().getText().equalsIgnoreCase(role.trim())) {
					return roleConcept;
				}
			}
		}

		return null;
	}

	public static ConceptVersionBI lookupState(String state, ViewCoordinate vc) throws TerminologyException, IOException {
		if (states == null)
		{
			states = new HashMap<String, ConceptVersionBI>();

			Set<? extends ConceptVersionBI> allStates = Terms.get().getActiveAceFrameConfig().getWorkflowStates();
	
			for (ConceptVersionBI stateConcept : allStates)
			{
				states.put(identifyFSN(stateConcept.getConceptNid(), vc).toLowerCase(), stateConcept);
			}
		}
		
		if (state != null) {
			return states.get(state.toLowerCase());
		} else {
			return null;
		}
	}
	
	public static ConceptVersionBI lookupAction(String action, ViewCoordinate vc) throws TerminologyException, IOException {
		if (actions == null)
		{
			actions = new HashMap<String, ConceptVersionBI>();

			Set<? extends ConceptVersionBI> allActions = Terms.get().getActiveAceFrameConfig().getWorkflowActions();
		
			for (ConceptVersionBI actionConcept : allActions)
			{
				actions.put(identifyFSN(actionConcept.getConceptNid(), vc).toLowerCase(), actionConcept);
			}
		}

		if (action != null) {
			return actions.get(action.toLowerCase());
		} else {
			return null;
		}
	}
	


	public static ConceptVersionBI lookupRoles(String role, ViewCoordinate vc) throws TerminologyException, IOException {
		Set<? extends ConceptVersionBI> allActions = Terms.get().getActiveAceFrameConfig().getWorkflowRoles();

		if (role != null) {
			for (ConceptVersionBI actionConcept : allActions)
			{
				if (identifyFSN(actionConcept.getConceptNid(), vc).equalsIgnoreCase(role.trim()))
					return actionConcept;
			}
		}

		return null;
	}

	public static List<RelationshipVersionBI> getWorkflowRelationship(ConceptVersionBI concept, Concept desiredRelationship) 
	{
		List<RelationshipVersionBI> rels = new LinkedList<RelationshipVersionBI>();

		if (concept != null && desiredRelationship != null) {
			try 
			{
				int searchRelId = Terms.get().uuidToNative(desiredRelationship.getPrimoridalUid());
				
				I_IntSet relType = Terms.get().newIntSet();
				int relTypeNid = Terms.get().getConcept(desiredRelationship.getPrimoridalUid()).getConceptNid();
		
				I_GetConceptData con = Terms.get().getConcept(concept.getPrimUuid());
				
				Collection<? extends I_RelVersioned> allRels = con.getSourceRels();
				for (I_RelVersioned rel : allRels)
				{
					RelationshipVersionBI relVersion = rel.getVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate());
					if (relVersion != null && 
						relVersion.getTypeNid() == searchRelId &&
						relVersion.isActive(Terms.get().getActiveAceFrameConfig().getAllowedStatus())) {
						rels.add(rel);
					}
				}
			} catch (Exception e) {
	        	AceLog.getAppLog().log(Level.WARNING, "Error in getting workflow-based attribute with error: " + e.getMessage());
			}
		}
		
		return rels;
	}

    public static boolean isBeginWorkflowAction(ConceptVersionBI actionConcept) {
    	if (beginWorkflowActions  == null)
		{
    		beginWorkflowActions = new HashSet<UUID>();
    		
    		try
	    	{
    	    	for (ConceptVersionBI action : Terms.get().getActiveAceFrameConfig().getWorkflowActions())
    	    	{
					for (RelationshipVersionBI rel : getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE))
					{
						if (rel != null &&
			    			rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_BEGIN_WF_CONCEPT.getPrimoridalUid()).getConceptNid())
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

    	if (beginWorkflowActions != null && actionConcept != null) {
    		return (beginWorkflowActions.contains(actionConcept.getPrimUuid()));
    	} else {
    		return false;
    	}
    }
    
    public static boolean isCommitWorkflowAction(ConceptVersionBI actionConcept) {
    	if (commitWorkflowActions  == null)
		{
    		commitWorkflowActions = new HashSet<UUID>();
    		
    		try
	    	{
    	    	for (ConceptVersionBI action : Terms.get().getActiveAceFrameConfig().getWorkflowActions())
    	    	{
					for (RelationshipVersionBI rel : getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE))
					{
						if (rel != null) {
    						commitWorkflowActions.add(action.getPrimUuid());
							break;
						}
					}
    	    	}
	    	} catch (Exception e) {
	        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying if current action is a BEGIN-WORKFLOW action with error: " + e.getMessage());
	    	}
		}

    	if (commitWorkflowActions != null && actionConcept != null) {
    		return (commitWorkflowActions.contains(actionConcept.getPrimUuid()));
    	} else {
    		return false;
    	}
    }
    
    private static boolean isEndWorkflowAction(ConceptVersionBI actionConcept) {
		
    	if (endWorkflowActionUid  == null && actionConcept != null)
		{
			try
	    	{
				List<RelationshipVersionBI> relList = getWorkflowRelationship(actionConcept, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);
	
				for (RelationshipVersionBI rel : relList)
				{
					if (rel != null &&
		    			 rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_END_WF_CONCEPT.getPrimoridalUid()).getConceptNid())
					{
						endWorkflowActionUid = actionConcept.getPrimUuid();
						break;
					}
				}
	    	} catch (Exception e) {
	        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying if current action is a END-WORKFLOW action with error: " + e.getMessage());
	    	}
		}

    	if (endWorkflowActionUid != null && actionConcept != null) {
    		return (endWorkflowActionUid.equals(actionConcept.getPrimUuid()));
    	} else {
    		return false;
    	}
	}

	public static boolean isActiveModeler(ConceptVersionBI modeler) throws TerminologyException, IOException {
		List<RelationshipVersionBI> relList = getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS);

		for (RelationshipVersionBI rel : relList)
		{
			if (rel != null &&
    			rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getPrimoridalUid()).getConceptNid())
				return true;
		}

		return false;
	}

	public static boolean isDefaultModeler(ConceptVersionBI modeler) throws TerminologyException, IOException {
		List<RelationshipVersionBI> relList = getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS);

		for (RelationshipVersionBI rel : relList)
		{
			if (rel != null &&
    			rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_DEFAULT_MODELER.getPrimoridalUid()).getConceptNid())
				return true;
		}

		return false;
	}

	public static HashMap<String, ConceptVersionBI> getModelers() {
		return modelers;
	}

	public static void setDefaultModeler(ConceptVersionBI defMod) {
		defaultModeler = defMod;
	}

	public static ConceptVersionBI getDefaultModeler() {
		return defaultModeler;
	}

	public static void initializeWorkflowForConcept(I_GetConceptData concept, boolean inBatch) throws TerminologyException, IOException {
		if ((concept != null) && 
			(inBatch || !WorkflowHistoryRefsetWriter.isInUse())) // Not in the middle of an existing commit
    	{
			ConceptVersionBI modeler = getCurrentModeler();
        	ViewCoordinate vc = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
        	
        	if (modeler != null && isActiveModeler(modeler))
        	{
        		I_TermFactory tf = Terms.get();
        		WorkflowHistoryRefsetWriter writer = new WorkflowHistoryRefsetWriter();

				WorkflowHistoryRefsetWriter.lockMutex();

				// Path
				// TODO: Update Path properly
	            writer.setPathUid(Terms.get().nidToUuid(concept.getConceptAttributes().getPathNid()));

	            // Modeler
	            writer.setModelerUid(getCurrentModeler().getPrimUuid());

	            // Concept & FSN
	            writer.setConceptUid(concept.getPrimUuid());
	            writer.setFSN(identifyFSN(concept.getConceptNid(), vc));

            	// Action
            	UUID actionUid = identifyAction();
                writer.setActionUid(actionUid);

                // State
                UUID initialState = identifyNextState(writer.getModelerUid(), concept, actionUid, vc);
                writer.setStateUid(initialState);

                // Worfklow Id
                WorkflowHistoryJavaBean latestBean = getLatestWfHxJavaBeanForConcept(concept);
                
                
	            if (latestBean == null || getAcceptAction().equals(latestBean.getAction()))
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
	            	UUID nextState = identifyNextState(writer.getModelerUid(), concept, acceptActionUid, vc);
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
				
		        Terms.get().addUncommitted(writer.getRefsetConcept());
        	}
    	}	
	}

	private static UUID identifyNextState(UUID modelerUid, I_GetConceptData concept, UUID commitActionUid, ViewCoordinate vc)
	{
		ConceptVersionBI initialState = null;
		boolean existsInDb = isConceptInDatabase(concept);
		
		try {
			WorkflowHistoryJavaBean bean = getLatestWfHxJavaBeanForConcept(concept);
			if (bean != null) {
				initialState = Terms.get().getConcept(bean.getState()).getVersion(vc);
			} else {
				
				for (ConceptVersionBI state : Terms.get().getActiveAceFrameConfig().getWorkflowStates())
				{
					List<RelationshipVersionBI> relList = getWorkflowRelationship(state, ArchitectonicAuxiliary.Concept.WORKFLOW_USE_CASE);
		
		    		for (RelationshipVersionBI rel : relList)
		    		{
		    			if ((rel != null) &&
							((existsInDb && (rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_EXISTING_CONCEPT.getPrimoridalUid()).getConceptNid())) ||
							 (!existsInDb && (rel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_NEW_CONCEPT.getPrimoridalUid()).getConceptNid()))))
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
			ConceptVersionBI category = categorySearcher.searchForCategoryForConceptByModeler(modeler.getVersion(vc), concept.getVersion(vc), vc);
		
			StateTransitionRefsetSearcher nextStateSearcher = new StateTransitionRefsetSearcher();
			Map<UUID, UUID> possibleActions = nextStateSearcher.searchForPossibleActionsAndFinalStates(category.getConceptNid(), initialState.getConceptNid(), vc);
		
			for (UUID transitionAction : possibleActions.keySet())
			{
				if (transitionAction.equals(commitActionUid))
				{
					return possibleActions.get(transitionAction);
				}
			}
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying next state: " + e.getMessage());
		}
		
		return null;
	}
		
	private static UUID identifyAction() 
	{
    	UUID commitActionUid = null;
    	try
    	{
	    	for (ConceptVersionBI action : Terms.get().getActiveAceFrameConfig().getWorkflowActions())
	    	{
	    		if (isBeginWorkflowAction(action))
	    		{
	    			List<RelationshipVersionBI> commitRelList = getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE);

    	    		for (RelationshipVersionBI commitRel : commitRelList)
    	    		{
						if (commitRel != null &&
							commitRel.getDestinationNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_SINGLE_COMMIT.getPrimoridalUid()).getConceptNid())
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
				for (ConceptVersionBI state : Terms.get().getActiveAceFrameConfig().getWorkflowStates())
				{
					if (isEndWorkflowState(state))
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

	private static boolean isEndWorkflowState(ConceptVersionBI stateConcept) throws IOException, TerminologyException {
    	if (endWorkflowStateUid  == null)
		{
    		// TODO: Remove hardcode and add to metadata
    		endWorkflowStateUid = ArchitectonicAuxiliary.Concept.WORKFLOW_APPROVED_STATE.getPrimoridalUid();
		}

    	if (endWorkflowStateUid != null && stateConcept != null) {
    		return (endWorkflowStateUid.equals(stateConcept.getPrimUuid()));
    	} else {
    		return false;
    	}
    }
    	
	public static UUID getAcceptAction() 
	{
		if (endWorkflowActionUid  == null)
		{
			try
			{
				TreeSet<? extends ConceptVersionBI> allActions = Terms.get().getActiveAceFrameConfig().getWorkflowActions();
				for (ConceptVersionBI action : allActions)
				{
					if (isEndWorkflowAction(action))
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

	public static String parseSemanticTag(ConceptVersionBI conceptVersionBI) throws ContraditionException, IOException {
		if (conceptVersionBI != null) {
			String s = conceptVersionBI.getPreferredDescription().getText();
	
	    	return parseSemanticTag(s);
		} else {
			return "";
		}
	}

	public static String parseSemanticTag(String potentialTag) {
		
		if (potentialTag != null) {
			int startIndex = potentialTag.lastIndexOf('(');
			
			if (startIndex >= 0)
			{
				int endIndex = potentialTag.lastIndexOf(')');
				String retTag = potentialTag.substring(startIndex + 1, endIndex);
	
				return parseSpaces(retTag);
			} 	
		}
		
		return "";
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

	public static TreeSet<WorkflowHistoryJavaBean> getLatestWfHxForConcept(I_GetConceptData con) 
		throws IOException, TerminologyException 
	{
		if (activeNidRf1 == 0) {
			 activeNidRf1 = Terms.get().uuidToNative(SnomedMetadataRf1.CURRENT_RF1.getUuids()[0]);
			 activeNidRf2 = Terms.get().uuidToNative(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0]);
		}

		TreeSet<WorkflowHistoryJavaBean> returnSet = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxJavaBeanComparer());

		if (con != null) {
			Set<String> ignoredWorkflows = new HashSet<String>();
			WorkflowHistoryRefsetReader reader = new WorkflowHistoryRefsetReader();
			
			long latestTimestamp = 0;
			String currentWorkflowId = null;
	
			List<? extends I_ExtendByRef> members = Terms.get().getRefsetExtensionsForComponent(Terms.get().uuidToNative(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids()), con.getConceptNid());
			
			for (I_ExtendByRef row : members) {
				int idx = row.getTuples().size() - 1;
				if (idx >= 0) {
					int statusNid = row.getTuples().get(idx).getStatusNid();
					if ((statusNid == activeNidRf1 || statusNid == activeNidRf2)) {
						if (!ignoredWorkflows.contains(reader.getWorkflowId(((I_ExtendByRefPartStr)row).getStringValue()))) {
							WorkflowHistoryJavaBean bean = populateWorkflowHistoryJavaBean(row);
							
							if (latestTimestamp == 0 || 
								(latestTimestamp < bean.getWorkflowTime() && !currentWorkflowId.equals(bean.getWorkflowId().toString()))) {
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
		}
		
		return returnSet;
	}

	public static SortedSet<WorkflowHistoryJavaBean> getLatestWfHxForConcept(
			I_GetConceptData con, UUID workflowId) throws IOException, TerminologyException {
		if (activeNidRf1 == 0) {
			 activeNidRf1 = Terms.get().uuidToNative(SnomedMetadataRf1.CURRENT_RF1.getUuids()[0]);
			 activeNidRf2 = Terms.get().uuidToNative(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0]);
		}
		TreeSet<WorkflowHistoryJavaBean> returnSet = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxJavaBeanComparer());

		if (con != null && workflowId != null) {
			WorkflowHistoryRefsetReader reader = new WorkflowHistoryRefsetReader();
	
			List<? extends I_ExtendByRef> members = Terms.get().getRefsetExtensionsForComponent(Terms.get().uuidToNative(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids()), con.getConceptNid());
			
			for (I_ExtendByRef row : members) {
				int idx = row.getTuples().size() - 1;
				if (idx >= 0) {
					int statusNid = row.getTuples().get(idx).getStatusNid();
					if ((statusNid == activeNidRf1 || statusNid == activeNidRf2)) {
						if (workflowId.equals(UUID.fromString(reader.getWorkflowIdAsString(((I_ExtendByRefPartStr)row).getStringValue())))) {
							returnSet.add(populateWorkflowHistoryJavaBean(row));
						}
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
		WorkflowHistoryRefsetReader reader = new WorkflowHistoryRefsetReader();
		for (I_ExtendByRef row : Terms.get().getRefsetExtensionsForComponent(reader.getRefsetNid(), Terms.get().uuidToNative(uuid))) 
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

	public static TreeSet<WorkflowHistoryJavaBean> getAllWorkflowHistory(I_GetConceptData concept) throws TerminologyException, IOException {
		TreeSet<WorkflowHistoryJavaBean> retSet = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxEarliestFirstTimeComparer());
		if (activeNidRf1 == 0) {
			 activeNidRf1 = Terms.get().uuidToNative(SnomedMetadataRf1.CURRENT_RF1.getUuids()[0]);
			 activeNidRf2 = Terms.get().uuidToNative(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0]);
		}
		
		try {
			List<? extends I_ExtendByRef> members = 
					Terms.get().getRefsetExtensionsForComponent(Terms.get().uuidToNative(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids()), 
																concept.getConceptNid());
			
			for (I_ExtendByRef row : members) {
				int idx = row.getTuples().size() - 1;
	
				if (idx >= 0) {
					int statusNid = row.getTuples().get(idx).getStatusNid();
					if ((statusNid == activeNidRf1 || statusNid == activeNidRf2)) {
						retSet.add(populateWorkflowHistoryJavaBean(row));
					}
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Cannot access Workflow History Refset members with error: " + e.getMessage());
		}
		return retSet;
	}

	public static String shrinkTermForDisplay(String term) {
		if (term != null) {
			StringBuffer retBuf = new StringBuffer();
			
			String words[] = term.split(" ");
			for (int i = 0; i < words.length; i++) {
				if (words[i].equalsIgnoreCase("workflow")) {
					return retBuf.toString().trim();
				} else {
					retBuf.append(words[i] + " ");
				}
			}
		}
		
		return "";
	}

	private static boolean isConceptInDatabase(I_GetConceptData concept) {
		boolean hasBeenReleased = false;

		try {
			int snomedId = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());

			I_Identify idVersioned = Terms.get().getId(concept.getConceptNid());
	        for (I_IdPart idPart : idVersioned.getMutableIdParts()) {
	            if (idPart.getAuthorityNid() == snomedId)
	            	hasBeenReleased = true;
	        }

			if (!hasBeenReleased && (getLatestWfHxForConcept(concept).size() == 0))
				return false;
		} catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Cannot identify if concept is in already in wfHx database");
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

	public static boolean isActiveAction(
			Collection<? extends WorkflowHistoryJavaBeanBI> possibleActions, UUID action) 
	{
			for (WorkflowHistoryJavaBeanBI bean : possibleActions)
			{
				if (bean.getAction().equals(action))
					return true;
			}

 		return false;
	}

	public static List<UUID> getAllAvailableWorkflowActionUids() 
	{
		try {
			return Terms.get().getActiveAceFrameConfig().getAllAvailableWorkflowActionUids();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error retrieving ActiveAceFrameConfig: ", e);
		}
		
		return null;
	}

	public static Collection<? extends WorkflowHistoryJavaBeanBI> getAvailableWorkflowActions(ConceptVersionBI concept, ViewCoordinate vc) throws IOException, ContraditionException {
		
		EditorCategoryRefsetSearcher searcher = null;		
		List<WorkflowHistoryJavaBean> retSet = new ArrayList<WorkflowHistoryJavaBean>();
		
		try {
            ConceptVersionBI modeler = WorkflowHelper.getCurrentModeler();

			List<WorkflowHistoryJavaBean> possibleActions = searchForPossibleActions(modeler, concept, vc);
			
			for (int i = 0; i < possibleActions.size();i++) {
				retSet.add(possibleActions.get(i));
			}
		} catch (Exception e) {
			throw new IOException("Unable to search for possible Actions", e);
		}

		return retSet;
	}


	private static List<WorkflowHistoryJavaBean> searchForPossibleActions(ConceptVersionBI modeler, ConceptVersionBI concept, ViewCoordinate vc) throws Exception
	{
        ArrayList<WorkflowHistoryJavaBean> retList = new ArrayList<WorkflowHistoryJavaBean>();

		// Get Editor Category by modeler and Concept
        EditorCategoryRefsetSearcher categegorySearcher = new EditorCategoryRefsetSearcher();

        ConceptVersionBI category = categegorySearcher.searchForCategoryForConceptByModeler(modeler, concept, vc);
        if (category == null) {
            return new ArrayList<WorkflowHistoryJavaBean>();
        }

        int categoryNid = category.getConceptNid();

		// Get Current WF Status for Concept
        WorkflowHistoryJavaBean latestBean = WorkflowHelper.getLatestWfHxJavaBeanForConcept(Terms.get().getConcept(concept.getConceptNid()));

        if ((latestBean != null) && (!WorkflowHelper.getAcceptAction().equals(latestBean.getAction())))
	    {
	        // Get Possible Next Actions to Next State Map from Editor Category and Current WF's useCase and state (which now will mean INITIAL-State)
	        int initialStateNid = Terms.get().uuidToNative(latestBean.getState());
	        StateTransitionRefsetSearcher stateTransitionSearcher = new StateTransitionRefsetSearcher();
	        Map<UUID, UUID> actionMap = stateTransitionSearcher.searchForPossibleActionsAndFinalStates(categoryNid, initialStateNid, vc);


	        // Create Beans for future update.  Only differences in Beans will be action & state (which now will mean NEXT-State)
	        for (UUID key : actionMap.keySet())
	        {
	        	// Such as done via Commit
	    		WorkflowHistoryJavaBean templateBean = new WorkflowHistoryJavaBean();

	            templateBean.setConcept(latestBean.getConcept());
	            templateBean.setWorkflowId(latestBean.getWorkflowId());
	            templateBean.setFSN(latestBean.getFSN());
	            templateBean.setModeler(latestBean.getModeler());
	            templateBean.setPath(latestBean.getPath());
	            templateBean.setAction(key);
	            templateBean.setState(actionMap.get(key));
	            templateBean.setEffectiveTime(latestBean.getEffectiveTime());
	            templateBean.setWorkflowTime(latestBean.getWorkflowTime());
	            templateBean.setOverridden(latestBean.getOverridden());
	            templateBean.setAutoApproved(latestBean.getAutoApproved());
	            
	            retList.add(templateBean);
	        }
        }

        return retList;
    }

}
 
