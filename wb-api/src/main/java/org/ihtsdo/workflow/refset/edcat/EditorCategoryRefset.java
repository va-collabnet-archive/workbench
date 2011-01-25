
package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefset;



/* 
* @author Jesse Efron
* 
*/
public class EditorCategoryRefset extends WorkflowRefset {
	
	public EditorCategoryRefset() throws IOException, TerminologyException {
		super (RefsetAuxiliary.Concept.EDITOR_CATEGORY);
	}

	public Collection<UUID> getRefsetUids() throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.EDITOR_CATEGORY.getUids();
	}

	/*
	"<properties>\n" +
			   	"<property>\n" +
			   		"<key>editorCategory</key>" +
			   		"<value>" + getEditorCategory().getConceptUid() + "</value>" +
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
	
	public I_GetConceptData getEditorCategory(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("editorCategory", props);
	}

	public UUID getEditorCategoryuID(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("editorCategory", props);
	}
	
}
