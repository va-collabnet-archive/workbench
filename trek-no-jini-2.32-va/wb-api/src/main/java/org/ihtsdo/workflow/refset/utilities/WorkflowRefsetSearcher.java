package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;

import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefset;



/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefsetSearcher extends WorkflowRefset {
	protected WorkflowRefsetSearcher(I_ConceptualizeUniversally refsetConcept)  throws TerminologyException, IOException {
		super(refsetConcept);
	}
}
