package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetReader;



/*
* @author Jesse Efron
*
*/
public  class EditorCategoryRefsetReader extends WorkflowRefsetReader
{
	public EditorCategoryRefsetReader() throws TerminologyException, IOException
	{
		super(editorCategoryConcept);
	}

	public String getSemanticTag(String props) {
		String key = "semanticArea";
		return getProp(key, props);
	}
	
	public I_GetConceptData getEditorCategory(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("editorCategory", props);
	}
	
	public UUID getEditorCategoryUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("editorCategory", props);
	}
	
	/*
		return "\nReferenced Component Id(Editor SCT) = " + Terms.get().getConcept(getReferencedComponentUid()).getInitialText() + 
		   "(" + getReferencedComponentUid() + ")" +
		   "\nSemantic Area = " + semanticArea +
		   "\nEditor Category = " + Terms.get().getConcept(editorCategory).getInitialText() +
		   "(" + editorCategory + ")";
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
					this.getEditorCategory(orig).equals(this.getEditorCategory(test)) && 
					this.getSemanticTag(orig).equals(this.getSemanticTag(test))) {
					return true;
				}
			} catch (Exception e) {
				
			}
		}
		
		return false;
	}
}
