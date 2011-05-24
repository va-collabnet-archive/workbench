package org.ihtsdo.workflow.refset.semTag;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetCreator;



/* 
* @author Jesse Efron
* 
*/
public class SemanticTagsRefsetCreator extends WorkflowRefsetCreator 
{
	public SemanticTagsRefsetCreator() throws IOException,
			TerminologyException {
		super();
		// TODO Auto-generated constructor stub
	}

	public int create() throws IOException, TerminologyException {
		SemanticTagsRefset refset = new SemanticTagsRefset();
		return createRefsetConcept(refset.getRefsetName());
	}
}
