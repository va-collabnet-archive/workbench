package org.ihtsdo.workflow.refset.stateTrans;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;



/* 
* @author Jesse Efron
* 
*/
public  class StateTransitionRefsetSearcher extends WorkflowRefsetSearcher 
{
	private StateTransitionRefsetReader reader;

	public StateTransitionRefsetSearcher()
			throws TerminologyException, IOException 
	{
		super(stateTransitionConcept);
		reader =  new StateTransitionRefsetReader();
	}

	// From Category and InitialState
	public Map<UUID, UUID> searchForPossibleActionsAndFinalStates(int categoryNid, int testInitialState, ViewCoordinate vc) throws Exception 
	{
		Map<UUID, UUID> results = new HashMap<UUID, UUID> ();
		I_ExtendByRefPartStr props = null;
		List<? extends I_ExtendByRef> l = null;

		String categoryString = WorkflowHelper.identifyPrefTerm(categoryNid, vc);
		int categoryIndex = categoryString.length() - 1;
		UUID allRoleUid = ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_ALL.getPrimoridalUid();

		for (ConceptVersionBI role : Terms.get().getActiveAceFrameConfig().getWorkflowRoles())
		{
			int roleIndex = role.getPreferredDescription().getText().length() - 1;
			
			// TODO
			// If ALL or Last Letter of Action (A,B,C,D) is less or equal than Last Letter of CategoryNid (A,B,C,D)  
			if (allRoleUid.equals(role.getPrimUuid()) ||
				(role.getPreferredDescription().getText().charAt(roleIndex) <= categoryString.charAt(categoryIndex)))
			{
				l = Terms.get().getRefsetExtensionsForComponent(refsetNid,  role.getConceptNid());
				results.putAll(findPossibleActions(l, testInitialState));
			}
		}
		
		return results;
	}
	
	private Map<UUID, UUID> findPossibleActions(List<? extends I_ExtendByRef> l, int matchInitialStateNid) 
	{
		Map<UUID, UUID> results = new HashMap<UUID, UUID> ();
		I_ExtendByRefPartStr props = null;

		try {
			
			boolean addAutoApproval = Terms.get().getActiveAceFrameConfig().isAutoApproveOn();
			
			for (int i = 0; i < l.size(); i++)
			{
				props = (I_ExtendByRefPartStr)l.get(i);
				int testStateNid = reader.getInitialState(props.getStringValue()).getConceptNid();

				if (matchInitialStateNid == testStateNid) 
				{
					I_GetConceptData action =  reader.getAction(props.getStringValue());
					I_GetConceptData finalState =  reader.getFinalState(props.getStringValue());
					
					if (addAutoApproval && action.getPrimUuid().equals(WorkflowHelper.getAcceptAction())) {
						// On, but already have it, avoid DB hit (till use more UUID and lesse I_GetConceptData)
						addAutoApproval = false;
					}
					
					results.put(action.getPrimUuid(), finalState.getPrimUuid());
				}
			}

			if (addAutoApproval) {
				I_GetConceptData action = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION.getPrimoridalUid());
				I_GetConceptData state = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_APPROVED_STATE.getPrimoridalUid());
				results.put(action.getPrimUuid(),state.getPrimUuid());
            }
		} catch (Exception e) {
			StringBuffer str = new StringBuffer();
			str.append("\ntestInitState: " + matchInitialStateNid);
			str.append("\nOn Row: " + props.getStringValue());
			AceLog.getAppLog().log(Level.WARNING, str.toString() + " with error: " + e.getMessage());
		}
		
		return results;
	}
}
