package org.ihtsdo.workflow.refset;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.TerminologyException;



/* 
* @author Jesse Efron
* 
*/
public interface I_WorkflowRefset {

	public Collection<UUID> getRefsetConcept() throws TerminologyException, IOException;
	public String getRefsetName();
	public int getRefsetId() throws IOException, TerminologyException;
}
