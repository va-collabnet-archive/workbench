package org.ihtsdo.workflow.refset.semHier;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
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
	public I_GetConceptData getRefCompConcept() throws TerminologyException, IOException {
		return Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMANTIC_PARENT_REL.getUids());
	}

	protected String getChildSemanticTag(String props) {
		return getProp("childSemanticArea", props);
	}
	
	protected String getParentSemanticTag(String props) {
		return getProp("parentSemanticArea", props);
	}
}
