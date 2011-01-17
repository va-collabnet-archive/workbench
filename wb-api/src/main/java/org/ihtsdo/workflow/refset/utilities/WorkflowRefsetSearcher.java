package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefset;



/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefsetSearcher extends WorkflowRefset {
	protected WorkflowRefset refset = null;

	protected WorkflowRefsetSearcher() throws TerminologyException, IOException {
		super();
	}

	public String listMembers() throws IOException, TerminologyException {
		WorkflowRefsetReader reader = new WorkflowRefsetReader(refset);
		
		return reader.printContents(refset.getRefsetId());
	}

	public Collection<UUID> getRefsetUids() throws TerminologyException, IOException
	{
		return refset.getRefsetUids();
	}
}
