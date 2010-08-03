package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.RefsetCreatorUtility;



/* 
* @author Jesse Efron
* 
*/
public class EditorCategoryRefsetCreator extends RefsetCreatorUtility  
{
	public EditorCategoryRefsetCreator() throws IOException, TerminologyException 
	{
		super();
	}

	public int create() throws IOException, TerminologyException {
		EditorCategoryRefset refset = new EditorCategoryRefset();
		return createRefsetConcept(refset.getRefsetName());
	}
}
