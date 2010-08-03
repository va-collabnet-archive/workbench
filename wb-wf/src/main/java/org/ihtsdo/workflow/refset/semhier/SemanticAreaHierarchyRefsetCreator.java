package org.ihtsdo.workflow.refset.semhier;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.RefsetCreatorUtility;



/* 
* @author Jesse Efron
* 
*/
public class SemanticAreaHierarchyRefsetCreator extends RefsetCreatorUtility 
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
