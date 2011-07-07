package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;
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
/*
 * 	"<properties>\n" +
   	"<property>\n" +	nid
-
   		"<key>editorCategory</key>" +
   		"<value>" + getEditorCategory().getConceptUid() + "</value>" +
   	"</property>" + 
   	"<property>" +
		"<key>semanticArea</key>" +
		"<value>" + getSemanticArea() + "</value>" +
	"</property>" + 
"</properties>"; 

*
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
