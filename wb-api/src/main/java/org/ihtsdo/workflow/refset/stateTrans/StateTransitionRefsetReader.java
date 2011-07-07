package org.ihtsdo.workflow.refset.stateTrans;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetReader;



/*
* @author Jesse Efron
*
*/
public  class StateTransitionRefsetReader extends WorkflowRefsetReader
{
	public StateTransitionRefsetReader() throws TerminologyException, IOException
	{
		super(stateTransitionConcept);
	}

	/*
	"<properties>\n" +
	   	"<property>\n" +
	   		"<key>workflowType</key>" +
	   		"<value>" + getWorkflowType().getConceptUid() + "</value>" +
	   	"</property>" + 
	   	"<property>" +
				"<key>initialState</key>" +
				"<value>" + getInitialState().getConceptUid() + "</value>" +
			"</property>" + 
		   	"<property>" +
	   		"<key>action</key>" +
	   		"<value>" + getAction().getConceptUid() + "</value>" +
	   	"</property>" + 
	   	"<property>" +
	   		"<key>finalState</key>" +
	   		"<value>" + getFinalState().getConceptUid() + "</value>" +
	   	"</property>" +
	"</properties>"; 
		
	*/
	protected I_GetConceptData getWorkflowType(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("workflowType", props);
	}
	
	protected I_GetConceptData getInitialState(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("initialState", props);
	}
	
	protected I_GetConceptData getAction(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("action", props);
	}

	protected I_GetConceptData getFinalState(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("finalState", props);
	}
	
	protected UUID getWorkflowTypeUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("workflowType", props);
	}
	
	protected UUID getInitialStateUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("initialState", props);
	}
	
	protected UUID getActionUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("action", props);
	}

	protected UUID getFinalStateUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("finalState", props);
	}
}
