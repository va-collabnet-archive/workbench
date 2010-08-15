
package org.ihtsdo.workflow.refset.semhier;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.I_WorkflowRefset;
import org.ihtsdo.workflow.refset.WorkflowRefset;



/* 
* @author Jesse Efron
* 
*/
public class SemanticAreaHierarchyRefset extends WorkflowRefset implements I_WorkflowRefset {

	public Collection<UUID> getRefsetConcept() throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.SEMANTIC_AREA_HIERARCHY.getUids();
	}

	@Override
	public int getRefsetId() throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.SEMANTIC_AREA_HIERARCHY.localize().getNid();
	}

	@Override
	public String getRefsetName() {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.SEMANTIC_AREA_HIERARCHY.toString();
	}

	public I_GetConceptData getSemanticTagParentRelationship() throws TerminologyException, IOException {
		return Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SEMANTIC_PARENT_REL.getUids());
	}
	/*
	"<properties>\n" +
			   	"<property>\n" +
			   		"<key>childSemanticArea</key>" +
			   		"<value>" + getSemanticAreaHierarchy().getConceptNid() + "</value>" +
			   	"</property>" + 
			   	"<property>" +
		   		"<key>semanticArea</key>" +
		   		"<value>" + getSemanticArea() + "</value>" +
		   	"</property>" + 
	   	"</properties>"; 
	*/


	protected String getChildSemanticTag(String props) {
		String key = "childSemanticArea";
		return getProp(key, props);
	}
	
	protected String getParentSemanticTag(String props) {
		String key = "parentSemanticArea";
		return getProp(key, props);
	}
}
