package org.ihtsdo.workflow.refset.stateTrans;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetCreator;



/* 
* @author Jesse Efron
* 
*/
public class StateTransitionRefsetCreator extends WorkflowRefsetCreator 
{
	public StateTransitionRefsetCreator() throws IOException,
			TerminologyException {
		super();
		// TODO Auto-generated constructor stub
	}

	public int create() throws IOException, TerminologyException {
		StateTransitionRefset refset = new StateTransitionRefset();
		return createRefsetConcept(refset.getRefsetName());
	}
}
