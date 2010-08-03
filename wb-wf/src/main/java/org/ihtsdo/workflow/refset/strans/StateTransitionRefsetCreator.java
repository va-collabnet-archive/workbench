package org.ihtsdo.workflow.refset.strans;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.RefsetCreatorUtility;



/* 
* @author Jesse Efron
* 
*/
public class StateTransitionRefsetCreator extends RefsetCreatorUtility 
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
