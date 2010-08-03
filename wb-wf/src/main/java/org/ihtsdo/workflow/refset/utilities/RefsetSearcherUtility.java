package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.I_WorkflowRefset;
import org.ihtsdo.workflow.refset.RefsetUtility;



/* 
* @author Jesse Efron
* 
*/
public abstract class RefsetSearcherUtility extends RefsetUtility{
	protected I_WorkflowRefset refset = null;

	protected RefsetSearcherUtility() throws TerminologyException, IOException {
		super();
	}

	public String listMembers() throws IOException, TerminologyException {
		RefsetReaderUtility reader = new RefsetReaderUtility();
		
		return reader.printContents(refset.getRefsetId());
	}
}
