
package org.ihtsdo.workflow.refset.semTag;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefset;



/* 
* @author Jesse Efron
* 
*/
public class SemanticTagsRefset extends WorkflowRefset  { 


	public Collection<UUID> getRefsetUids() throws TerminologyException, IOException {
		return RefsetAuxiliary.Concept.SEMANTIC_TAGS.getUids();
	}

	public SemanticTagsRefset() throws IOException, TerminologyException {
		super (RefsetAuxiliary.Concept.SEMANTIC_TAGS, true);
	}


	
	/*
	searchTerm
	*/

	protected String getSemanticTag(String props) throws NumberFormatException, TerminologyException, IOException {
		return getProp("semTag", props);
	}

	protected String getSemanticTagUUID(String props) throws NumberFormatException, TerminologyException, IOException {
		return getProp("uuid", props);
	}
}
