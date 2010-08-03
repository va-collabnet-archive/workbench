
package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.I_WorkflowRefset;
import org.ihtsdo.workflow.refset.WorkflowRefset;



/* 
* @author Jesse Efron
* 
*/
public class EditorCategoryRefset extends WorkflowRefset implements I_WorkflowRefset {
	
	public Collection<UUID> getRefsetConcept() throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.EDITOR_CATEGORY.getUids();
	}

	@Override
	public int getRefsetId() throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.EDITOR_CATEGORY.localize().getNid();
	}

	@Override
	public String getRefsetName() {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.EDITOR_CATEGORY.toString();
	}


	/*
	"<properties>\n" +
			   	"<property>\n" +
			   		"<key>editorCategory</key>" +
			   		"<value>" + getEditorCategory().getConceptId() + "</value>" +
			   	"</property>" + 
			   	"<property>" +
		   		"<key>semanticArea</key>" +
		   		"<value>" + getSemanticArea() + "</value>" +
		   	"</property>" + 
	   	"</properties>"; 
	 */
	
	protected String getSemanticTag(String props) {
		String key = "semanticArea";
		return getProp(key, props);
	}
	
	protected I_GetConceptData getEditorCategory(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "editorCategory";
		return Terms.get().getConcept(Integer.parseInt(getProp(key, props)));
	}
}
