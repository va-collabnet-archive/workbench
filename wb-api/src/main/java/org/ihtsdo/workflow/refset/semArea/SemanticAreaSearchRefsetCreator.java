package org.ihtsdo.workflow.refset.semArea;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetCreator;



/* 
* @author Jesse Efron
* 
*/
public class SemanticAreaSearchRefsetCreator extends WorkflowRefsetCreator 
{
	public SemanticAreaSearchRefsetCreator() throws IOException,
			TerminologyException {
		super();
		// TODO Auto-generated constructor stub
	}

	public int create() throws IOException, TerminologyException {
		SemanticAreaSearchRefset refset = new SemanticAreaSearchRefset();
		return createRefsetConcept(refset.getRefsetName());
	}
}
