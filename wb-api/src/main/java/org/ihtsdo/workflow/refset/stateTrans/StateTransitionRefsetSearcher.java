package org.ihtsdo.workflow.refset.stateTrans;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;



/* 
* @author Jesse Efron
* 
*/
public  class StateTransitionRefsetSearcher extends WorkflowRefsetSearcher 
{
	public StateTransitionRefsetSearcher()
			throws TerminologyException, IOException 
	{
		refset = new StateTransitionRefset();
		
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}

	// From Category and InitialState
	public Map<I_GetConceptData, I_GetConceptData> searchForPossibleActionsAndFinalStates(int categoryNid, int testInitialState) throws Exception 
	{
		Map<I_GetConceptData, I_GetConceptData> results = new HashMap<I_GetConceptData, I_GetConceptData> ();
		I_ExtendByRefPartStr props = null;
		List<I_ExtendByRefPartStr> l = null;

		String categoryString = Terms.get().getConcept(categoryNid).getInitialText();
		int categoryIndex = categoryString.length() - 1;
		UUID allRoleUid = ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_ALL.getPrimoridalUid();

		for (I_GetConceptData role : Terms.get().getActiveAceFrameConfig().getWorkflowRoles())
		{
			int roleIndex = role.getInitialText().length() - 1;
			
			// TODO
			// If ALL or Last Letter of Action (A,B,C,D) is less or equal than Last Letter of CategoryNid (A,B,C,D)  
			if (allRoleUid.equals(role.getPrimUuid()) ||
				(role.getInitialText().charAt(roleIndex) <= categoryString.charAt(categoryIndex)))
			{
				l = helper.getAllCurrentRefsetExtensions(refsetId,  role.getConceptNid());
				results.putAll(findPossibleActions(l, testInitialState));
			}
		}
		
		return results;
	}
	
	private Map<I_GetConceptData, I_GetConceptData> findPossibleActions(List<I_ExtendByRefPartStr> l, int matchInitialStateNid) 
	{
		Map<I_GetConceptData, I_GetConceptData> results = new HashMap<I_GetConceptData, I_GetConceptData> ();
		I_ExtendByRefPartStr props = null;

		try {
			for (int i = 0; i < l.size(); i++)
			{
				props = (I_ExtendByRefPartStr)l.get(i);
				int testStateNid = ((StateTransitionRefset)refset).getInitialState(props.getStringValue()).getConceptNid();

				if (matchInitialStateNid == testStateNid) 
				{
					I_GetConceptData action =  ((StateTransitionRefset)refset).getAction(props.getStringValue());
					I_GetConceptData finalState =  ((StateTransitionRefset)refset).getFinalState(props.getStringValue());
					
					results.put(action, finalState);
				}
			}
		} catch (Exception e) {
			StringBuffer str = new StringBuffer();
			str.append("\ntestInitState: " + matchInitialStateNid);
			str.append("\nOn Row: " + props.getStringValue());
			AceLog.getAppLog().log(Level.WARNING, str.toString(), new Exception("Failure in updating Editor Category Refset"));
		}
		
		return results;
	}
}
