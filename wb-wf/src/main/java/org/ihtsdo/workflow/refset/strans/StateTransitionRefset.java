
package org.ihtsdo.workflow.refset.strans;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.I_WorkflowRefset;
import org.ihtsdo.workflow.refset.WorkflowRefset;



/* 
* @author Jesse Efron
* 
*/
public class StateTransitionRefset extends WorkflowRefset implements I_WorkflowRefset { 
	public Collection<UUID> getRefsetConcept() throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.STATE_TRANSITION.getUids();
	}

	@Override
	public int getRefsetId() throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.STATE_TRANSITION.localize().getNid();
	}

	@Override
	public String getRefsetName() {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.STATE_TRANSITION.toString();
	}
	
	/*
	"<properties>\n" +
	   	"<property>\n" +
	   		"<key>workflowType</key>" +
	   		"<value>" + getWorkflowType().getConceptNid() + "</value>" +
	   	"</property>" + 
	   	"<property>" +
				"<key>initialState</key>" +
				"<value>" + getInitialState().getConceptNid() + "</value>" +
			"</property>" + 
		   	"<property>" +
	   		"<key>action</key>" +
	   		"<value>" + getAction().getConceptNid() + "</value>" +
	   	"</property>" + 
	   	"<property>" +
	   		"<key>finalState</key>" +
	   		"<value>" + getFinalState().getConceptNid() + "</value>" +
	   	"</property>" +
	"</properties>"; 
		
	*/

	protected I_GetConceptData getWorkflowType(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "workflowType";
		return Terms.get().getConcept(Integer.parseInt(getProp(key, props)));
	}
	
	protected I_GetConceptData getInitialState(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "initialState";
		return Terms.get().getConcept(Integer.parseInt(getProp(key, props)));
	}
	
	protected I_GetConceptData getAction(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "action";
		return Terms.get().getConcept(Integer.parseInt(getProp(key, props)));
	}

	protected I_GetConceptData getFinalState(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "finalState";
		return Terms.get().getConcept(Integer.parseInt(getProp(key, props)));
	}
	
}
