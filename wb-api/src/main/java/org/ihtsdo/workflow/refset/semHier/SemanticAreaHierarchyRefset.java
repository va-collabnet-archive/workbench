
package org.ihtsdo.workflow.refset.semHier;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefset;



/* 
* @author Jesse Efron
* 
*/
public class SemanticAreaHierarchyRefset extends WorkflowRefset 
{
	public SemanticAreaHierarchyRefset() throws IOException, TerminologyException {
		super (RefsetAuxiliary.Concept.SEMANTIC_AREA_HIERARCHY);
	}
	
	public Collection<UUID> getRefsetUids() throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.SEMANTIC_AREA_HIERARCHY.getUids();
	}

	public I_GetConceptData getSemanticTagParentRelationship() throws TerminologyException, IOException {
		return Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMANTIC_PARENT_REL.getUids());
	}

	protected String getChildSemanticTag(String props) {
		return getProp("childSemanticArea", props);
	}
	
	protected String getParentSemanticTag(String props) {
		return getProp("parentSemanticArea", props);
	}
}