package org.ihtsdo.workflow.refset.semArea;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;



/* 
* @author Jesse Efron
* 
*/
public  class SemanticAreaSearchRefsetSearcher extends WorkflowRefsetSearcher 
{
	public SemanticAreaSearchRefsetSearcher()
			throws TerminologyException, IOException 
	{
		refset = new SemanticAreaSearchRefset();
		
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId(), true);
	}

	
}
