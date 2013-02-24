package org.ihtsdo.workflow.refset.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
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
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.workflow.WorkflowHistoryJavaBeanBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetReader;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetWriter;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetReader;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.semHier.SemanticHierarchyRefsetReader;
import org.ihtsdo.workflow.refset.semHier.SemanticHierarchyRefsetWriter;
import org.ihtsdo.workflow.refset.semTag.SemanticTagsRefsetReader;
import org.ihtsdo.workflow.refset.semTag.SemanticTagsRefsetWriter;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetReader;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetSearcher;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetWriter;



/*
* @author Jesse Efron
*
*/
public class WorkflowHelper {

	public final static int workflowIdPosition = 0;									// 0
    public final static int conceptIdPosition = workflowIdPosition + 1;				// 1
    public final static int modelerPosition = conceptIdPosition + 1;				// 2
    public final static int actionPosition = modelerPosition + 1;					// 3
    public final static int statePosition = actionPosition + 1;						// 4
    public final static int fsnPosition = statePosition + 1;						// 5
    public final static int refsetColumnTimeStampPosition = fsnPosition + 1;		// 6
    public final static int timeStampPosition = refsetColumnTimeStampPosition + 1;	// 7

    public final static int numberOfColumns = timeStampPosition + 1;				// 8

    private static int activeNidRf1 = 0;
    private static int activeNidRf2 = 0;
    private static int is_a_relType = 0;
    
	public final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static HashMap<String, ConceptVersionBI> modelers = null;
	private static HashMap<String, ConceptVersionBI> actions = null;
	private static HashMap<String, ConceptVersionBI> states = null;
	
	private static ConceptVersionBI leadModeler = null;
	private static ConceptVersionBI defaultModeler = null;

	private static Set<UUID> beginWorkflowActions = null;
	private static Set<UUID> commitWorkflowActions = null;
	private static Set<UUID> beginWorkflowStateUids = null;
	private static UUID endWorkflowActionUid = null;
	private static UUID endWorkflowStateUid = null;

	public static final int EARLIEST_WORKFLOW_HISTORY_YEAR = 2007;
	public static final int EARLIEST_WORKFLOW_HISTORY_MONTH = Calendar.OCTOBER; 
	public static final int EARLIEST_WORKFLOW_HISTORY_DATE = 19;
	private static final String unrecognizedLoginMessage = "Login is unrecognlized.  You will be defaulted to generic-user workflow permissions";
	
	private static UUID overrideActionUid = null;

	private static int wfHistoryRefsetNid = 0;
	private static UUID wfHistoryRefsetUid = null;
	private static boolean advancingWorkflowLock = false;
	private static boolean wfCapabilitiesAvailable = false;
	private static boolean wfCapabilitiesInitialized = false;
	private static Map<String, BufferedWriter> logFiles = new HashMap<String, BufferedWriter>();
	private static HashSet<UUID> wfRefsetUidList = null;
	
	public static ConceptVersionBI getCurrentModeler() throws TerminologyException, IOException {
		if (modelers == null) {
			updateModelers(Terms.get().getActiveAceFrameConfig().getViewCoordinate());
		}
		
		if (modelers != null) {
			
			String prefTerm = null;
			I_GetConceptData parentEditorConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid());
			
			for (I_GetConceptData con : getChildren(parentEditorConcept)) {
				if (Terms.get().getActiveAceFrameConfig().getUsername().equalsIgnoreCase(WorkflowHelper.getPrefTerm(con))) {
					prefTerm = WorkflowHelper.getPrefTerm(con);
					break;
				}
			}
			
			
			if (prefTerm == null) {
	        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying current user's workflow login");
			}
			
			return modelers.get(prefTerm);
		} 
			
		return null;
	}
	
	public static String identifyPrefTerm(int conceptNid, ViewCoordinate vc)  {
		try {
			TerminologySnapshotDI dbSnapshot = Ts.get().getSnapshot(vc);
			return dbSnapshot.getConceptVersion(conceptNid).getDescriptionPreferred().getText();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying current concept's Preferred with msg: \n" + e.getMessage());
		}

   		return "";
		
    }

	public static String identifyFSN(int conceptNid, ViewCoordinate vc)  {
		try {
			TerminologySnapshotDI dbSnapshot = Ts.get().getSnapshot(vc);
			return dbSnapshot.getConceptVersion(conceptNid).getDescriptionFullySpecified().getText();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying current concept's FSN with msg: \n" + e.getMessage());
		}

   		return "";		
    }

	public static void retireWorkflowHistoryRow(WorkflowHistoryJavaBean bean, ViewCoordinate vc)
 	{
		try {
			UUID currentWfId = bean.getWorkflowId();
			boolean precedingCommitExists = isCommitWorkflowAction(Terms.get().getConcept(bean.getAction()).getVersion(vc));
			
			I_ExtendByRef ref = retireWfHxRow(bean);

			if (ref != null && precedingCommitExists) {
				// Just retired preceding commit
		    	WorkflowHistoryJavaBean latestBean = getLatestWfHxJavaBeanForConcept(Terms.get().getConcept(bean.getConcept()));

		    	// Retire original Row as previously retired original
				if (latestBean != null && currentWfId.equals(latestBean.getWorkflowId())) {
					retireWfHxRow(latestBean);
				}
			}
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in retiring workflow history row: " + bean.toString() + "  with error: " + e.getMessage());
		}
	}

	public static I_ExtendByRef retireWfHxRow(WorkflowHistoryJavaBean bean) throws Exception {
		WorkflowHistoryRefsetWriter writer;
		writer = new WorkflowHistoryRefsetWriter();

		writer.setPathUid(bean.getPath());

		// Always retire on current modeler regardless of bean's request 
		writer.setModelerUid(bean.getModeler());

		writer.setConceptUid(bean.getConcept());
		writer.setFSN(bean.getFullySpecifiedName());
		writer.setActionUid(bean.getAction());
		writer.setStateUid(bean.getState());

		writer.setWorkflowUid(bean.getWorkflowId());

		writer.setEffectiveTime(Long.MAX_VALUE);

		// Must use previous Refset Timestamp to revert proper Str
		writer.setWorkflowTime(bean.getWorkflowTime());

		writer.setAutoApproved(bean.getAutoApproved());
		writer.setOverride(bean.getOverridden());
		
		I_ExtendByRef ref = writer.retireMember();

		return ref;
	}

	public static void updateModelers(ViewCoordinate vc) 
	{
    	modelers = new HashMap<String, ConceptVersionBI>();

    	try {
			I_GetConceptData parentEditorConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid());
			Set<ConceptVersionBI> editors = new HashSet<ConceptVersionBI>();
			
			for (ConceptVersionBI con : getChildren(parentEditorConcept.getVersion(vc))) {
				if (!con.getPrimUuid().equals(parentEditorConcept.getPrimUuid())) {
					editors.add(con);
				}
			}
			
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
	

	private static String getLoginId(ConceptVersionBI con) throws ContradictionException, IOException {
    	return con.getDescriptionPreferred().getText();
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
				if (getDefaultModeler() != null && !getDefaultModeler().getDescriptionPreferred().getText().equalsIgnoreCase(name))
				{
					AceLog.getAppLog().log(Level.WARNING, unrecognizedLoginMessage);
	
					for (ConceptVersionBI modeler : modelers.values())
					{
						List<RelationshipVersionBI<?>> relList = getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS);
	
						for (RelationshipVersionBI<?> rel : relList)
						{
							if (rel != null &&
							    rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_DEFAULT_MODELER.getPrimoridalUid()).getConceptNid())
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
					List<RelationshipVersionBI<?>> relList = getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS);
	
					for (RelationshipVersionBI<?> rel : relList)
					{
						if (rel != null &&
						    rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_LEAD_MODELER.getPrimoridalUid()).getConceptNid())
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
    	bean.setFullySpecifiedName(reader.getFSN(fieldValues));
    	bean.setWorkflowTime(reader.getWorkflowTime(fieldValues));
    	bean.setAutoApproved(reader.getAutoApproved(fieldValues));
    	bean.setOverridden(reader.getOverridden(fieldValues));
    	bean.setEffectiveTime(timeStamp);
        bean.setRefexMemberNid(id);

    	return bean;
    }

    public static void updateWorkflowUserRoles(ViewCoordinate vc) {
    	TreeSet <ConceptVersionBI> sortedRoles = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createVersionedPreferredTermComparer());

   	   	try {
   			I_GetConceptData rolesParentConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ROLES.getPrimoridalUid());

   			Set<ConceptVersionBI> workflowRoles =new HashSet<ConceptVersionBI>();
   			
   			for (ConceptVersionBI con: getChildren(rolesParentConcept.getVersion(vc))) {
   				if (!con.getPrimUuid().equals(rolesParentConcept.getPrimUuid())) {
   					workflowRoles.add(con);
   				}
   			}

    		sortedRoles.addAll(workflowRoles);

    		Terms.get().getActiveAceFrameConfig().setWorkflowRoles(sortedRoles);
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in updating workflow user roles with error: " + e.getMessage());
    	}
    }

    public static void updateWorkflowStates(ViewCoordinate vc) {
    	TreeSet <ConceptVersionBI> sortedStates = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createVersionedPreferredTermComparer());

		try {
			I_GetConceptData statesParentConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_STATES.getPrimoridalUid());

   			Set<ConceptVersionBI> workflowStates = new HashSet<ConceptVersionBI>();
   			
   			for (ConceptVersionBI con: getChildren(statesParentConcept.getVersion(vc))) {
   				if (!con.getPrimUuid().equals(statesParentConcept.getPrimUuid())) {
   					workflowStates.add(con);
   				}
   			}

    		sortedStates.addAll(workflowStates);

    		Terms.get().getActiveAceFrameConfig().setWorkflowStates(sortedStates);
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in updating workflow states with error: " + e.getMessage());
    	}
    }

    public static void updateWorkflowActions(ViewCoordinate vc) {
		Set<UUID> availableActions = new HashSet<UUID>();
    	TreeSet <ConceptVersionBI> sortedActions = new TreeSet<ConceptVersionBI>(WfComparator.getInstance().createVersionedPreferredTermComparer());
		List<UUID> sortedAvailableActions = new LinkedList<UUID>();

		try {
    		I_GetConceptData actionParentConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIONS.getPrimoridalUid());

    		Set<ConceptVersionBI> workflowActions = new HashSet<ConceptVersionBI>();
    		
    		for (ConceptVersionBI con : getChildren(actionParentConcept.getVersion(vc))) {
    			if (!con.getPrimUuid().equals(actionParentConcept.getPrimUuid())) { 
    				workflowActions.add(con);
	    		}
			}

    		for (ConceptVersionBI action : workflowActions)
    		{
    			// Only add non-Commit actions
    			List<RelationshipVersionBI<?>> relList = getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE);

    			boolean foundCommitValue = false;
				for (RelationshipVersionBI<?> rel : relList)
				{
					if (rel != null &&
						(rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_SINGLE_COMMIT.getPrimoridalUid()).getConceptNid() ||
						 rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_BATCH_COMMIT.getPrimoridalUid()).getConceptNid()))
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

	public static Set<ConceptVersionBI> getChildren(ConceptVersionBI concept) throws IOException, ContradictionException 
    {
		Set<ConceptVersionBI> resultSet = new HashSet<ConceptVersionBI>();

		if (concept != null) {		
			resultSet.add(concept);
			
			ViewCoordinate vc = concept.getViewCoordinate();

			if (vc.getRelationshipAssertionType() == RelAssertionType.STATED || vc.getRelationshipAssertionType() == RelAssertionType.INFERRED_THEN_STATED) {
				Collection<? extends ConceptVersionBI> children = concept.getRelationshipsIncomingSourceConceptsActiveIsa();
	
		    	if (children == null || children.size() == 0) {
		    		return resultSet;
				}
		    	
		    	for (ConceptVersionBI child : children) {
		    		 if (child.getConceptNid() != concept.getConceptNid()) {
		    			resultSet.addAll(getChildren(child));
		    		 }
		    	}
			} else {
				vc = new ViewCoordinate(vc);
				vc.setRelationshipAssertionType(RelAssertionType.INFERRED_THEN_STATED);

				Collection<? extends RelationshipChronicleBI> children = concept.getRelationshipsIncoming();

		    	if (children == null || children.size() == 0) {
		    		return resultSet;
				}
		    	
				try {
					if (activeNidRf1 == 0) {
						 activeNidRf1 = Terms.get().uuidToNative(SnomedMetadataRf1.CURRENT_RF1.getUuids()[0]);
						 activeNidRf2 = Terms.get().uuidToNative(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0]);
					}
					
					if (is_a_relType == 0) {
						is_a_relType = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
					}
					
			    	for (RelationshipChronicleBI child : children) {
			    		 if (child.getConceptNid() != concept.getConceptNid()) {
			    			 RelationshipVersionBI<?> latestVersion = child.getVersion(vc);
			    			
			    			 if ((latestVersion.getTypeNid() == is_a_relType) &&
			    				 (latestVersion.getStatusNid() == activeNidRf1 || latestVersion.getStatusNid() == activeNidRf2)) {
			    				 ConceptVersionBI childToExamine = Terms.get().getConcept(latestVersion.getSourceNid()).getVersion(vc);
			    				 resultSet.addAll(getChildren(childToExamine));
			    			 }
			    		 }
			    	}
				} catch (Exception ee) {
		        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying workflow children" + ee.getMessage());
				}
			}
		}
		
    	return resultSet;
    }

	public static Set<I_GetConceptData> getChildren(I_GetConceptData concept) {
		
		Set<I_GetConceptData> resultSet = new HashSet<I_GetConceptData>();
		
		try {
			if (concept != null) {		
				resultSet.add(concept);
				Collection<? extends I_RelVersioned> rels = concept.getDestRels();
	
		    	if (rels == null || rels.size() == 0) {
		    		return resultSet;
				}
		    	
				if (activeNidRf1 == 0) {
					 activeNidRf1 = Terms.get().uuidToNative(SnomedMetadataRf1.CURRENT_RF1.getUuids()[0]);
					 activeNidRf2 = Terms.get().uuidToNative(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0]);
				}
				
				if (is_a_relType == 0) {
					is_a_relType = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
				}
	
		    	for (I_RelVersioned<?> rel : rels) {
		    		if ((rel.getTypeNid() == is_a_relType) &&
						((rel.getStatusNid() == activeNidRf1) || (rel.getStatusNid() == activeNidRf2))) {
		    			resultSet.addAll(getChildren(Terms.get().getConcept(rel.getC1Id())));
		    		 }
		    	}
			}
		} catch (Exception e) {
			return new HashSet<I_GetConceptData>();
		}
		
		return resultSet;
	}


	public static ConceptVersionBI lookupEditorCategory(String role, ViewCoordinate vc) throws TerminologyException, IOException, ContradictionException {
		Set<? extends ConceptVersionBI> allRoles = Terms.get().getActiveAceFrameConfig().getWorkflowRoles();

		if (role != null) {
			for (ConceptVersionBI roleConcept : allRoles)
			{
				if (roleConcept.getDescriptionFullySpecified().getText().equalsIgnoreCase(role.trim())) {
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

	public static List<RelationshipVersionBI<?>> getWorkflowRelationship(ConceptVersionBI concept, Concept desiredRelationship) 
	{
		ViewCoordinate vc = concept.getViewCoordinate();
		if (vc.getRelationshipAssertionType() != RelAssertionType.STATED && vc.getRelationshipAssertionType() != RelAssertionType.INFERRED_THEN_STATED) {
			vc = new ViewCoordinate(vc);
			vc.setRelationshipAssertionType(RelAssertionType.INFERRED_THEN_STATED);
		}

		List<RelationshipVersionBI<?>> rels = new LinkedList<RelationshipVersionBI<?>>();

		if (concept != null && desiredRelationship != null) {
			try 
			{
				int searchRelId = Terms.get().uuidToNative(desiredRelationship.getPrimoridalUid());
				
				I_GetConceptData con = Terms.get().getConcept(concept.getPrimUuid());
				
				Collection<? extends I_RelVersioned> allRels = con.getSourceRels();
				for (I_RelVersioned<?> rel : allRels)
				{
					RelationshipVersionBI<?> relVersion = (RelationshipVersionBI<?>) rel.getVersion(vc);
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
    	return isBeginWorkflowAction(actionConcept.getPrimUuid());
    }
    
    public static boolean isBeginWorkflowAction(UUID actionConcept) {
    	if (beginWorkflowActions  == null)
		{
    		beginWorkflowActions = new HashSet<UUID>();
    		
    		try
	    	{
    	    	for (ConceptVersionBI action : Terms.get().getActiveAceFrameConfig().getWorkflowActions())
    	    	{
					for (RelationshipVersionBI<?> rel : getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE))
					{
						if (rel != null &&
			    			rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_BEGIN_WF_CONCEPT.getPrimoridalUid()).getConceptNid())
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
    		return (beginWorkflowActions.contains(actionConcept));
    	} else {
    		return false;
    	}
    }
    
    public static boolean isCommitWorkflowAction(ConceptVersionBI actionConcept) {
    	return isCommitWorkflowAction(actionConcept.getPrimUuid());
    }
    
    public static boolean isCommitWorkflowAction(UUID actionConcept) {
    	if (commitWorkflowActions  == null)
		{
    		commitWorkflowActions = new HashSet<UUID>();
    		
    		try
	    	{
    	    	for (ConceptVersionBI action : Terms.get().getActiveAceFrameConfig().getWorkflowActions())
    	    	{
					for (RelationshipVersionBI<?> rel : getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE))
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
    		return (commitWorkflowActions.contains(actionConcept));
    	} else {
    		return false;
    	}
    }
    
    public static boolean isEndWorkflowAction(ConceptVersionBI actionConcept) {
    	return isEndWorkflowAction(actionConcept.getPrimUuid());
    }
    
    public static boolean isEndWorkflowAction(UUID actionConcept) {
    	if (endWorkflowActionUid  == null && actionConcept != null)
		{
			try
	    	{
    	    	for (ConceptVersionBI action : Terms.get().getActiveAceFrameConfig().getWorkflowActions())
    	    	{
					for (RelationshipVersionBI<?> rel : getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE))
					{
						if (rel != null &&
			    			rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_END_WF_CONCEPT.getPrimoridalUid()).getConceptNid())
						{
							endWorkflowActionUid = action.getPrimUuid();
							break;
						}
					}
    	    	}
	    	} catch (Exception e) {
	        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying if current action is a END-WORKFLOW action with error: " + e.getMessage());
	    	}
		}

    	if (endWorkflowActionUid != null && actionConcept != null) {
    		return (endWorkflowActionUid.equals(actionConcept));
    	} else {
    		return false;
    	}
	}

	public static boolean isActiveModeler(ConceptVersionBI modeler) throws TerminologyException, IOException {
		List<RelationshipVersionBI<?>> relList = getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS);

		for (RelationshipVersionBI<?> rel : relList)
		{
			if (rel != null &&
    			rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getPrimoridalUid()).getConceptNid())
				return true;
		}

		return false;
	}

	public static boolean isDefaultModeler(ConceptVersionBI modeler) throws TerminologyException, IOException {
		List<RelationshipVersionBI<?>> relList = getWorkflowRelationship(modeler, ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS);

		for (RelationshipVersionBI<?> rel : relList)
		{
			if (rel != null &&
    			rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_DEFAULT_MODELER.getPrimoridalUid()).getConceptNid())
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

	public static void initializeWorkflowForConcept(I_GetConceptData concept) throws TerminologyException, IOException {
		if (concept != null)
    	{
			ConceptVersionBI modeler = getCurrentModeler();
        	
        	if (modeler != null && isActiveModeler(modeler))
        	{
            	ViewCoordinate vc = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
        		WorkflowHistoryRefsetWriter writer = new WorkflowHistoryRefsetWriter();

				// Path
	            writer.setPathUid(Terms.get().nidToUuid(concept.getConAttrs().getPathNid()));

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
	            if (Terms.get().getActiveAceFrameConfig().isAutoApproveOn()) {
	            	writer.setAutoApproved(true);

	            	// Identify and overwrite Accept Action
	            	// Identify and overwrite Next State
	            	UUID nextState = getApprovedState();
					writer.setStateUid(nextState);
	            } else
	            	writer.setAutoApproved(false);

	            // Override
	            writer.setOverride(Terms.get().getActiveAceFrameConfig().isOverrideOn());

	            // TimeStamps
		        java.util.Date today = new java.util.Date();
		        writer.setEffectiveTime(today.getTime());
		        writer.setWorkflowTime(today.getTime());

		        // Write Member
				writer.addMember(false);
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
					List<RelationshipVersionBI<?>> relList = getWorkflowRelationship(state, ArchitectonicAuxiliary.Concept.WORKFLOW_USE_CASE);
		
		    		for (RelationshipVersionBI<?> rel : relList)
		    		{
		    			if ((rel != null) &&
							((existsInDb && (rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_EXISTING_CONCEPT.getPrimoridalUid()).getConceptNid())) ||
							 (!existsInDb && (rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_NEW_CONCEPT.getPrimoridalUid()).getConceptNid()))))
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
		
			if (category == null) {
	        	AceLog.getAppLog().log(Level.WARNING, "The user does not have a valid Editor Category.  Therefore, workflow will not be advanced.  Solution: Update Workflow Editor Category file.");
	        }
			
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
	    			List<RelationshipVersionBI<?>> commitRelList = getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_VALUE);

    	    		for (RelationshipVersionBI<?> commitRel : commitRelList)
    	    		{
						if (commitRel != null &&
							commitRel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_SINGLE_COMMIT.getPrimoridalUid()).getConceptNid())
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

	public static boolean isEndWorkflowState(ConceptVersionBI stateConcept) throws IOException, TerminologyException {
		return isEndWorkflowState(stateConcept.getPrimUuid());
	}
	
	public static boolean isEndWorkflowState(UUID stateConcept) throws IOException, TerminologyException {
    	if (endWorkflowStateUid  == null)
		{
    		// TODO: Remove hardcode and add to metadata
    		endWorkflowStateUid = ArchitectonicAuxiliary.Concept.WORKFLOW_APPROVED_STATE.getPrimoridalUid();
		}

    	if (endWorkflowStateUid != null && stateConcept != null) {
    		return (endWorkflowStateUid.equals(stateConcept));
    	} else {
    		return false;
    	}
    }
    	
	public static boolean isBeginWorkflowState(ConceptVersionBI stateConcept) throws IOException, TerminologyException {
		return isBeginWorkflowState(stateConcept.getPrimUuid());
	}
	
	public static boolean isBeginWorkflowState(UUID stateConcept) throws IOException, TerminologyException {
    	if (beginWorkflowStateUids  == null)
		{
    		beginWorkflowStateUids = new HashSet<UUID>();
    		
    		beginWorkflowStateUids.add(ArchitectonicAuxiliary.Concept.WORKFLOW_NEW_STATE.getPrimoridalUid());
    		beginWorkflowStateUids.add(ArchitectonicAuxiliary.Concept.WORKFLOW_CHANGED_STATE.getPrimoridalUid());
			beginWorkflowStateUids.add(ArchitectonicAuxiliary.Concept.WORKFLOW_CHANGED_IN_BATCH_STATE.getPrimoridalUid());
    				
			for (ConceptVersionBI state : Terms.get().getActiveAceFrameConfig().getWorkflowStates())
			{
				List<RelationshipVersionBI<?>> relList = getWorkflowRelationship(state, ArchitectonicAuxiliary.Concept.WORKFLOW_USE_CASE);
	
	    		for (RelationshipVersionBI<?> rel : relList)
	    		{
	    			if (rel != null) {
	    				beginWorkflowStateUids.add(state.getPrimUuid());
	    			}
	    		}
			}
		}

    	if (beginWorkflowStateUids != null && stateConcept != null) {
    		return (beginWorkflowStateUids.contains(stateConcept));
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

	public static WorkflowHistoryJavaBean populateWorkflowHistoryJavaBean(RefexVersionBI<?> version) {
		WorkflowHistoryJavaBean bean = null;
		
		try {
			bean = populateWorkflowHistoryJavaBean(version.getNid(), 
												   Terms.get().nidToUuid(version.getReferencedComponentNid()), 
												   ((RefexStringVersionBI<?>)version).getString1(), 
												   version.getTime());

		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Failure to read WfHx Java Bean from Refset Member:" + version);
		}
		
		return bean;
	}

	public static WorkflowHistoryJavaBean populateWorkflowHistoryJavaBean(I_ExtendByRef ref) {
		WorkflowHistoryJavaBean bean = null;
		
		try {
			// Latest version of ref into bean
            I_ExtendByRefPartStr latestPart = null;
            for (I_ExtendByRefPart part : ref.getMutableParts()) {
                if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
                    latestPart = (I_ExtendByRefPartStr) part;
                }
            }

			bean = populateWorkflowHistoryJavaBean(ref.getMemberId(), 
												   Terms.get().nidToUuid(ref.getComponentNid()), 
												   latestPart.getStringValue(), 
												   new Long(latestPart.getTime()));
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Failure to read WfHx Java Bean from Refset Member:" + ref);
		}
		
		return bean;
	}

	public static WorkflowHistoryJavaBean populateWorkflowHistoryJavaBean(TkRefexAbstractMember<?> mem) throws NumberFormatException, TerminologyException, IOException {
		TkRefsetStrMember member = (TkRefsetStrMember) mem;
		
		// Primordial is RefsetMember & membercomponentUid is RefComp
		return  populateWorkflowHistoryJavaBean(Terms.get().uuidToNative(member.getPrimordialComponentUuid()), 
												member.componentUuid, 
												member.getString1(),
												member.getTime());	
	}

	public static String parseSemanticTag(ConceptVersionBI conceptVersionBI) throws ContradictionException, IOException {
		if (conceptVersionBI != null) {
			String s = conceptVersionBI.getDescriptionPreferred().getText();
	
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

	public static WorkflowHistoryJavaBean getLatestWfHxJavaBeanForWorkflowId(I_GetConceptData con, UUID workflowId) throws IOException, TerminologyException 
	{
		SortedSet<WorkflowHistoryJavaBean> wfSet = getWfHxForWorkflowId(con, workflowId);
		
		if (wfSet == null || wfSet.size() == 0) {
			return null;
		} else {
			return wfSet.last();
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

	public static TreeSet<WorkflowHistoryJavaBean> getLatestWfHxForConcept(I_GetConceptData con) throws IOException, TerminologyException 
	{
		TreeSet<WorkflowHistoryJavaBean> returnSet = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxJavaBeanComparer());

		if (con != null) {
			Set<String> processedIds = new HashSet<String>();
			long latestTimestamp = 0;
			String currentWorkflowId = null;
			
			for (WorkflowHistoryJavaBean bean : getWfHxMembersAsBeans(con)) {
				if (!processedIds.contains(bean.getWorkflowId())) {
					if (latestTimestamp == 0 || 
						(latestTimestamp < bean.getWorkflowTime() && !currentWorkflowId.equals(bean.getWorkflowId().toString()))) {
						returnSet.clear();
						processedIds.add(currentWorkflowId);
						
						currentWorkflowId = bean.getWorkflowId().toString();
						latestTimestamp = bean.getWorkflowTime();
					} 				
					
					returnSet.add(bean);
				}
			}
		}
		
		return returnSet;
	}

	public static SortedSet<WorkflowHistoryJavaBean> getWfHxForWorkflowId(
			I_GetConceptData con, UUID workflowId) throws IOException, TerminologyException {
		TreeSet<WorkflowHistoryJavaBean> returnSet = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxJavaBeanComparer());
  
		if (con != null && workflowId != null) {
			TreeSet<WorkflowHistoryJavaBean> members = getWfHxMembersAsBeans(con);

			for (WorkflowHistoryJavaBean bean : members) {
				if (workflowId.equals(bean.getWorkflowId())) {
					returnSet.add(bean);
				}
			}
		}
			
		return returnSet;
	}

	 
	public static TreeSet<WorkflowHistoryJavaBean> getWfHxMembersAsBeans(I_GetConceptData con) throws IOException, TerminologyException {
		TreeSet<WorkflowHistoryJavaBean> retSet = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxEarliestFirstTimeComparer());

		if (activeNidRf1 == 0) {
			activeNidRf1 = Terms.get().uuidToNative(SnomedMetadataRf1.CURRENT_RF1.getUuids()[0]);
			activeNidRf2 = Terms.get().uuidToNative(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0]);
		}		

		if (con != null) {
			if (con.getConAttrs() != null) {
				try {
					// From Annotations
					Collection<? extends RefexChronicleBI<?>> annotations = con.getConAttrs().getAnnotations();
					
					for (RefexChronicleBI<?> annot : annotations) {
						if (annot.getRefexNid() == getWorkflowRefsetNid()) {						
							// Setup sorted beans
				            RefexVersionBI<?> latestPart = null;
				            for (RefexVersionBI<?> part : annot.getVersions()) {
				                if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
				                    latestPart = part;
				                }
				            }

							
							// add if latestPart is active
				            if (latestPart != null) {
								if (latestPart.getStatusNid() == activeNidRf1 || latestPart.getStatusNid() == activeNidRf2) {
									WorkflowHistoryJavaBean bean = WorkflowHelper.populateWorkflowHistoryJavaBean(latestPart);
									retSet.add(bean);
								}
							}
						}
					}				
					// From WfHx Concept's Refset Members (till update Import mechanism)
					List<? extends I_ExtendByRef> members = Terms.get().getRefsetExtensionsForComponent(getWorkflowRefsetNid(), con.getConceptNid());
					
					for (I_ExtendByRef member : members) {
			            I_ExtendByRefPart latestPart = null;
			            for (I_ExtendByRefPart part : member.getMutableParts()) {
			                if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
			                    latestPart = part;
			                }
			            }

			            if (latestPart != null) {
			            	if (latestPart.getStatusNid() == activeNidRf1 || latestPart.getStatusNid() == activeNidRf2) {
			            		WorkflowHistoryJavaBean bean = populateWorkflowHistoryJavaBean(latestPart);
			            		retSet.add(bean);
			            	}
			            }
					}
				} catch (Exception e) {
		        	AceLog.getAppLog().log(Level.WARNING, "Error retrieving wfHx refset members/annotations: ", e);
				}
			}
		}
		
		return retSet;
	}

	public static void listWorkflowHistory(UUID conceptId) throws NumberFormatException, IOException, TerminologyException 
	{
		int counter = 0;

		TreeSet<WorkflowHistoryJavaBean> members = getWfHxMembersAsBeans(Terms.get().getConcept(conceptId));
		
		for (WorkflowHistoryJavaBean bean : members) {
			System.out.println("\n\nBean #: " + counter + " = " + bean.toString());
		}
	}

	public static Object getOverrideAction() {
	   	if (overrideActionUid   == null)
		{
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
		
		try {
			retSet = getWfHxMembersAsBeans(concept);
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Cannot access Workflow History Refset members with error: " + e.getMessage());
		}
		return retSet;
	}

	public static String shrinkTermForDisplay(String term) {
		StringBuffer retBuf = new StringBuffer();
		if (term != null) {
			
			String words[] = term.split(" ");
			for (int i = 0; i < words.length; i++) {
				if (words[i].equalsIgnoreCase("workflow")) {
					return retBuf.toString().trim();
				} else {
					retBuf.append(words[i] + " ");
				}
			}
		}
		
		return retBuf.toString();
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

	public static WorkflowHistoryJavaBean populateWorkflowHistoryJavaBean(I_ExtendByRefVersion<?> refsetVersion) {
		WorkflowHistoryJavaBean bean = null;

		try {
			I_ExtendByRefPartStr<?> strPart = (I_ExtendByRefPartStr<?>)refsetVersion.getMutablePart(); 

			bean = populateWorkflowHistoryJavaBean(refsetVersion.getMemberId(), 
												   Terms.get().nidToUuid(refsetVersion.getComponentId()), 
												   strPart.getStringValue(), 
												   new Long(refsetVersion.getTime()));
		} catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Failure to read WfHx Java Bean from Refset Member");
		}
		
		return bean;
	}

	public static boolean isActiveAction(Collection<? extends WorkflowHistoryJavaBeanBI> possibleActions, UUID action) 
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

	public static Collection<? extends WorkflowHistoryJavaBeanBI> getAvailableWorkflowActions(ConceptVersionBI concept, ViewCoordinate vc) throws IOException, ContradictionException {
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
	            templateBean.setFullySpecifiedName(latestBean.getFullySpecifiedName());
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

	public static void setAdvancingWorkflowLock(boolean lock) {
		advancingWorkflowLock = lock;
	}

	public static boolean isAdvancingWorkflowLock() {
		return advancingWorkflowLock;
	}

	public static boolean isWorkflowCapabilityAvailable() {
		if (new File("sampleProcesses/legacyWorkflowSuspended.txt").exists()) {
			//AceLog.getAppLog().log(Level.INFO, "Legacy Workflow Capability disabled");
			return false;
		}
		if (!wfCapabilitiesInitialized) {
			try {
				wfCapabilitiesInitialized = true;
			
				UUID testUid = ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS.getPrimoridalUid();
		         if (Ts.get().hasUuid(testUid)) {
		        	 wfCapabilitiesAvailable = true;
		         }
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.INFO, "Workflow Capability not present");
			} catch (Error assertionCatch) {
				AceLog.getAppLog().log(Level.INFO, "Workflow Capability not present");
			}
		}

		return wfCapabilitiesAvailable;
	}

	public static UUID getWorkflowRefsetUid() {
		if (wfHistoryRefsetUid == null) {
			
			try {
				wfHistoryRefsetUid = RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid();
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.INFO, "Unable to access Workflow History Refset");
			}
		}

		return wfHistoryRefsetUid;
	}

	public static int getWorkflowRefsetNid() {
		if (wfHistoryRefsetNid != 0) {
			return wfHistoryRefsetNid;
		} else {
			if (Ts.get() != null) {
				try {
					if (Ts.get().hasUuid(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids().iterator().next())) {
						wfHistoryRefsetNid = Terms.get().uuidToNative(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid());
						
				        return wfHistoryRefsetNid;
					}
				} catch (Exception e) {
					AceLog.getAppLog().log(Level.INFO, "Unable to access Workflow History Refset");
				}
			}
		}

		return 0;
	}

	public static BufferedWriter createLogFile(String filePath) throws IOException {
		if (!logFiles.containsKey(filePath)) {
			BufferedWriter logFile = new BufferedWriter(new FileWriter(filePath));
			logFiles.put(filePath, logFile);
		}
		
		return logFiles.get(filePath);
	}

	public static void closeLogFile(String filePath) {
		try {
			logFiles.get(filePath).flush();
			logFiles.get(filePath).close();
			logFiles.remove(filePath);
		} catch (IOException e) {
			AceLog.getAppLog().log(Level.INFO, "Error closing logfile: " + filePath);
		}
	}
	public static void closeAllLogFiles() {
		for (String filePath : logFiles.keySet()) {
			closeLogFile(filePath);
		}
	}

	public static Collection<? extends UUID> getRefsetUidList() {
		if (wfRefsetUidList  == null) {			
			try {
				wfRefsetUidList = new HashSet<UUID>();
				wfRefsetUidList.add(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid());
				wfRefsetUidList.add(RefsetAuxiliary.Concept.EDITOR_CATEGORY.getPrimoridalUid());
				wfRefsetUidList.add(RefsetAuxiliary.Concept.STATE_TRANSITION.getPrimoridalUid());
				wfRefsetUidList.add(RefsetAuxiliary.Concept.SEMANTIC_HIERARCHY.getPrimoridalUid());
				wfRefsetUidList.add(RefsetAuxiliary.Concept.SEMANTIC_TAGS.getPrimoridalUid());
			} catch (Exception e) {
	
			}
		}
		
		return wfRefsetUidList;
	}

	// Search for latest Approved State for Concept's WF
	public static long getLatestApprovedTimeStamp(int conceptNid) {
		try {
			TreeSet<WorkflowHistoryJavaBean> members = getWfHxMembersAsBeans(Terms.get().getConcept(conceptNid));
			Iterator<WorkflowHistoryJavaBean> itr = members.descendingIterator();

			while (itr.hasNext()) {
				WorkflowHistoryJavaBean bean = (WorkflowHistoryJavaBean) itr.next();
				if (bean.getState().equals(getApprovedState())) {
					// Query in reverse order so return first visited Approved State's WF Timestamp 
					return bean.getWorkflowTime();
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.INFO, "Unable to access Workflow History for concept: " + conceptNid);
		}
		
		// No Workflow exists for concept or it exists without Approved State
		return 0;
	}

	public static TkRefexAbstractMember<?> retireWorkflowRefsetRow(TkRefexAbstractMember<?> member) throws NumberFormatException, Exception {
		wfRefsetUidList.add(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid());
		wfRefsetUidList.add(RefsetAuxiliary.Concept.EDITOR_CATEGORY.getPrimoridalUid());
		wfRefsetUidList.add(RefsetAuxiliary.Concept.STATE_TRANSITION.getPrimoridalUid());
		wfRefsetUidList.add(RefsetAuxiliary.Concept.SEMANTIC_HIERARCHY.getPrimoridalUid());
		wfRefsetUidList.add(RefsetAuxiliary.Concept.SEMANTIC_TAGS.getPrimoridalUid());

		if (member.getRefexUuid().equals(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid())) {
			retireWfHxRow(populateWorkflowHistoryJavaBean(member));
		} else {
			WorkflowRefsetWriter writer = null;
			if (member.getRefexUuid().equals(RefsetAuxiliary.Concept.EDITOR_CATEGORY.getPrimoridalUid())) {
				EditorCategoryRefsetWriter edCatWriter = new EditorCategoryRefsetWriter();
				edCatWriter.setReferencedComponentId(Terms.get().nidToUuid(((I_ExtendByRefVersion<?>)member).getReferencedComponentNid()));

				String s = ((I_ExtendByRefPartStr<?>)member).getStringValue();
				
				EditorCategoryRefsetReader reader = new EditorCategoryRefsetReader();
				edCatWriter.setCategory(reader.getEditorCategoryUid(s));
				edCatWriter.setSemanticArea(reader.getSemanticTag(s));
				writer = edCatWriter;
			} else if (member.getRefexUuid().equals(RefsetAuxiliary.Concept.STATE_TRANSITION.getPrimoridalUid())) {
				StateTransitionRefsetWriter stateTransWriter = new StateTransitionRefsetWriter();
				stateTransWriter.setReferencedComponentId(Terms.get().nidToUuid(((I_ExtendByRefVersion<?>)member).getReferencedComponentNid()));

				String s = ((I_ExtendByRefPartStr<?>)member).getStringValue();
				
				StateTransitionRefsetReader reader = new StateTransitionRefsetReader();
				stateTransWriter.setWorkflowType(ArchitectonicAuxiliary.Concept.WORKFLOW_USE_CASE.getPrimoridalUid());
				stateTransWriter.setInitialState(reader.getInitialState(s));
				stateTransWriter.setAction(reader.getAction(s));
				stateTransWriter.setFinalState(reader.getFinalState(s));

				writer = stateTransWriter;
			} else if (member.getRefexUuid().equals(RefsetAuxiliary.Concept.SEMANTIC_HIERARCHY.getPrimoridalUid())) {
				SemanticHierarchyRefsetWriter semHierWriter = new SemanticHierarchyRefsetWriter();
				semHierWriter.setReferencedComponentId(Terms.get().nidToUuid(((I_ExtendByRefVersion<?>)member).getReferencedComponentNid()));

				String s = ((I_ExtendByRefPartStr<?>)member).getStringValue();
				
				SemanticHierarchyRefsetReader reader = new SemanticHierarchyRefsetReader();
				semHierWriter.setChildSemanticArea(reader.getChildSemanticTag(s));
				semHierWriter.setParentSemanticArea(reader.getParentSemanticTag(s));

				writer = semHierWriter;
			} else if (member.getRefexUuid().equals(RefsetAuxiliary.Concept.SEMANTIC_TAGS.getPrimoridalUid())) {
				SemanticTagsRefsetWriter semTagWriter = new SemanticTagsRefsetWriter();
				semTagWriter.setReferencedComponentId(Terms.get().nidToUuid(((I_ExtendByRefVersion<?>)member).getReferencedComponentNid()));

				String s = ((I_ExtendByRefPartStr<?>)member).getStringValue();
				
				SemanticTagsRefsetReader reader = new SemanticTagsRefsetReader();
				semTagWriter.setSemanticTag(reader.getSemanticTag(s));

				writer = semTagWriter;
			}
			
			writer.retireMember();
		}
		
		TkRefexAbstractMember<?> retiree = member;
		retiree.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids()[0]);
		
		return retiree;
	}

	public static String getPrefTerm(I_GetConceptData con) throws IOException, TerminologyException {
		int descTypeNid = ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid();
		int rf2DescTypeNid = Terms.get().uuidToNative(SnomedMetadataRf2.SYNONYM_RF2.getUuids()[0]);

		Collection<? extends I_DescriptionVersioned> descs = con.getDescs();
		
		for (I_DescriptionVersioned<?> desc : descs) {
			if ((desc.getTypeNid() == descTypeNid || desc.getTypeNid() == rf2DescTypeNid) &&
			    (desc.getLang().equals("en") || desc.getLang().equals("en-us"))) {
				return desc.getText();
			}
		}
		
		return null;
	}

	public static String getFsn(I_GetConceptData con) throws IOException, TerminologyException {
		int descTypeNid = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid();
		int rf2DescTypeNid = Terms.get().uuidToNative(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()[0]);

		Collection<? extends I_DescriptionVersioned> descs = con.getDescs();
			
		for (I_DescriptionVersioned<?> desc : descs) {
			if (desc.getTypeNid() == descTypeNid || desc.getTypeNid() == rf2DescTypeNid) {
				return desc.getText();
			}
		}
		
		return null;
	}
	
}
 
