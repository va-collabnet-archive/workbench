
package org.ihtsdo.workflow.refset.history;

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
public class WorkflowHistoryRefset extends WorkflowRefset implements I_WorkflowRefset {
	
	public Collection<UUID> getRefsetConcept() throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids();
	}

	@Override
	public int getRefsetId() throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.WORKFLOW_HISTORY.localize().getNid();
	}

	@Override
	public String getRefsetName() {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.WORKFLOW_HISTORY.toString();
	}


	
	public UUID getConceptId(String props) {
		String key = "conceptId";
		return UUID.fromString(getProp(key, props));
	}
	
	public UUID getWorkflowId(String props) {
		String key = "workflowId";
		return UUID.fromString(getProp(key, props));
	}

	public I_GetConceptData getUseCase(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "useCase";
		return Terms.get().getConcept(Integer.parseInt(getProp(key, props)));
	}
	public I_GetConceptData getState(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "state";
		return Terms.get().getConcept(Integer.parseInt(getProp(key, props)));
	}
	public I_GetConceptData getAction(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "action";
		return Terms.get().getConcept(Integer.parseInt(getProp(key, props)));
	}
	public I_GetConceptData getPath(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "path";
		return Terms.get().getConcept(Integer.parseInt(getProp(key, props)));
	}
	public I_GetConceptData getModeler(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "modeler";
		return Terms.get().getConcept(Integer.parseInt(getProp(key, props)));
	}
	public String getFSN(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "fsn";
		return getProp(key, props);
	}
	public String getTimeStamp(String props) throws NumberFormatException, TerminologyException, IOException {
		String key = "timeStamp";
		return getProp(key, props);
	}
}
