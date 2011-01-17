package org.ihtsdo.workflow.refset.semHier;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetCreator;



/* 
* @author Jesse Efron
* 
*/
public class SemanticAreaHierarchyRefsetCreator extends WorkflowRefsetCreator 
{
	public SemanticAreaHierarchyRefsetCreator() throws IOException,
			TerminologyException {
		super();
		// TODO Auto-generated constructor stub
	}

	public int create() throws IOException, TerminologyException {
		SemanticAreaHierarchyRefset refset = new SemanticAreaHierarchyRefset();
		return createRefsetConcept(refset.getRefsetName());
	}
}
