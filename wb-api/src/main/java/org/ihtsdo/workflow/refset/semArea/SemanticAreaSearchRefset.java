
package org.ihtsdo.workflow.refset.semArea;
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
public class SemanticAreaSearchRefset extends WorkflowRefset  { 


	public Collection<UUID> getRefsetUids() throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.SEMANTIC_AREA_SEARCH.getUids();
	}

	public SemanticAreaSearchRefset() throws IOException, TerminologyException {
		super (RefsetAuxiliary.Concept.SEMANTIC_AREA_SEARCH.localize().getNid(),
			RefsetAuxiliary.Concept.SEMANTIC_AREA_SEARCH.toString());
	}


	@Override
	public String getRefsetName() {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.SEMANTIC_AREA_SEARCH.toString();
	}
	
	/*
	searchTerm
	*/

	protected String getSearchTerm(String props) throws NumberFormatException, TerminologyException, IOException {
		return props;
	}
}
