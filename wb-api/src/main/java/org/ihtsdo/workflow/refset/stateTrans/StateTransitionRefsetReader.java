package org.ihtsdo.workflow.refset.stateTrans;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
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

	public I_GetConceptData getWorkflowType(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("workflowType", props);
	}
	
	public I_GetConceptData getInitialState(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("initialState", props);
	}
	
	public I_GetConceptData getAction(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("action", props);
	}

	public I_GetConceptData getFinalState(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("finalState", props);
	}
	
	public UUID getWorkflowTypeUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("workflowType", props);
	}
	
	public UUID getInitialStateUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("initialState", props);
	}
	
	public UUID getActionUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("action", props);
	}

	public UUID getFinalStateUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("finalState", props);
	}

	/*	return "\nReferenced Component Id (Editor Category) = " + con.getInitialText() + 
	   "(" + con.getConceptNid() + ")" +
	   "\nWorkflow Type = " + Terms.get().getConcept(workflowType).getInitialText() +
	   "\nInitial State = " + Terms.get().getConcept(initialState).getInitialText() +
	   "\nAction = " + Terms.get().getConcept(action).getInitialText() +
	   "\nFinal State= " + Terms.get().getConcept(finalState).getInitialText();
	 */
	
	@Override
	public boolean isIdenticalAutomatedAdjudication(TkRefexAbstractMember origMember, TkRefexAbstractMember testMember) {
		if (isIdenticalSap(origMember, testMember)) {
			return false;
		} else {
			String orig = ((TkRefsetStrMember)origMember).getString1();
			String test = ((TkRefsetStrMember)testMember).getString1();
			
			try {
				if (origMember.getComponentUuid().equals(testMember.getComponentUuid()) &&
					this.getWorkflowType(orig).equals(this.getWorkflowType(test)) && 
					this.getInitialState(orig).equals(this.getInitialState(test)) &&
					this.getAction(orig).equals(this.getAction(test)) &&
					this.getFinalState(orig).equals(this.getFinalState(test))) {
					return true;
				}
			} catch (Exception e) {
				
			}
		}
		
		return false;
	}
}
