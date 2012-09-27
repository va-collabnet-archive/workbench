package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;

import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.workflow.refset.WorkflowRefset;



/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefsetReader extends WorkflowRefset {
	public WorkflowRefsetReader(I_ConceptualizeUniversally refsetConcept) throws IOException, TerminologyException {
		super(refsetConcept);
	}
	
	public boolean isIdenticalSap(TkRefexAbstractMember origMember, TkRefexAbstractMember testMember) {
		if (origMember.getStatusUuid().equals(testMember.getStatusUuid()) &&
			origMember.getPathUuid().equals(testMember.getPathUuid()) &&
			origMember.getAuthorUuid().equals(testMember.getAuthorUuid()) &&
			origMember.getTime() == testMember.getTime()) {
			return true;
		}
		
		return false;
	}
	
	abstract public boolean isIdenticalAutomatedAdjudication(TkRefexAbstractMember dup, TkRefexAbstractMember member);
}
