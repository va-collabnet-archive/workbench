package org.ihtsdo.workflow.refset.semHier;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetReader;



/*
* @author Jesse Efron
*
*/
public  class SemanticHierarchyRefsetReader extends WorkflowRefsetReader
{
	public SemanticHierarchyRefsetReader() throws TerminologyException, IOException
	{
		super(semanticHierarchyConcept);
	}

	public String getChildSemanticTag(String props) {
		return getProp("childSemanticArea", props);
	}
	
	public String getParentSemanticTag(String props) {
		return getProp("parentSemanticArea", props);
	}

	/*
	return "\nReferenced Component Id (Same for each row -- HardCoded) = " + getReferencedComponentUid() + 
		   "(" + getReferencedComponentUid() + ")" +
		   "\nChild Semantic Area = " + childSemanticArea +
		   "\nParentSemantic Area = " + parentSemanticArea;
	 */
	
	@Override
	public boolean isIdenticalAutomatedAdjudication(TkRefexAbstractMember origMember, TkRefexAbstractMember testMember) {
		if (isIdenticalSap(origMember, testMember)) {
			return false;
		} else {
			String orig = ((TkRefsetStrMember)origMember).getString1();
			String test = ((TkRefsetStrMember)testMember).getString1();
			
			try {
				if (origMember.getComponentUuid().equals(testMember.getComponentUuid()) &&
					this.getChildSemanticTag(orig).equals(this.getChildSemanticTag(test)) && 
					this.getParentSemanticTag(orig).equals(this.getParentSemanticTag(test))) {
					return true;
				}
			} catch (Exception e) {
				
			}
		}
		
		return false;
	}
}
