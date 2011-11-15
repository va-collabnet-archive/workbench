package org.ihtsdo.workflow.refset.semTag;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrMember;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetReader;



/*
* @author Jesse Efron
*
*/
public  class SemanticTagsRefsetReader extends WorkflowRefsetReader
{
	public SemanticTagsRefsetReader() throws TerminologyException, IOException
	{
		super(semanticTagConcept);
	}


	public String getSemanticTag(String props) throws NumberFormatException, TerminologyException, IOException {
		return getProp("semTag", props);
	}

	public String getSemanticTagUUID(String props) throws NumberFormatException, TerminologyException, IOException {
		return getProp("uuid", props);
	}

/*	return  "\nSemantic Tag: " + semanticTag + 
	"\nUUID: " + uid;
*/
	
	@Override
	public boolean isIdenticalAutomatedAdjudication(TkRefsetAbstractMember origMember, TkRefsetAbstractMember testMember) {
		
		if (isIdenticalSap(origMember, testMember)) {
			return false;
		} else {
			String orig = ((TkRefsetStrMember)origMember).getStrValue();
			String test = ((TkRefsetStrMember)testMember).getStrValue();
			
			// For this Refset, IGNORE a) EffectiveTimestamp b) Path c) RxMemberId
			try {
				if (origMember.getComponentUuid().equals(testMember.getComponentUuid()) &&
					this.getSemanticTag(orig).equals(this.getSemanticTag(test)) && 
					this.getSemanticTagUUID(orig) == this.getSemanticTagUUID(test)) {
					return true;
				}
			} catch (Exception e) {
				
			}
		}
		
		return false;
	}
}
