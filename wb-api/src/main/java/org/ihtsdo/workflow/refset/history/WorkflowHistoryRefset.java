
package org.ihtsdo.workflow.refset.history;

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
public class WorkflowHistoryRefset extends WorkflowRefset  {
	
	public WorkflowHistoryRefset() throws IOException, TerminologyException {
		super (RefsetAuxiliary.Concept.WORKFLOW_HISTORY);
	}

	public Collection<UUID> getRefsetUids() throws TerminologyException, IOException {
		return RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids();
	}

	
	// Workflow ID Only a UUID (No Concept)
	public UUID getWorkflowId(String props) {
		return getUUID("workflowId", props);
	}

	// I_GetConceptData values where appropriate
	public I_GetConceptData getConcept(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("concept", props);
	}
	public I_GetConceptData getState(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("state", props);
	}
	public I_GetConceptData getAction(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("action", props);
	}
	public I_GetConceptData getPath(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("path", props);
	}
	public I_GetConceptData getModeler(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("modeler", props);
	}
	public String getFSN(String props) throws NumberFormatException, TerminologyException, IOException {
		return getProp("fsn", props);
	}
	public Long getWorkflowTime(String props) throws NumberFormatException, TerminologyException, IOException {
		return Long.parseLong(getProp("workflowTime", props));
	}
	public Long getEffectiveTime(String props) throws NumberFormatException, TerminologyException, IOException {
		return Long.parseLong(getProp("effectiveTime", props));
	}
	public UUID getConceptUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("concept", props);
	}
	public UUID getStateUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("state", props);
	}
	public UUID getActionUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("action", props);
	}
	public UUID getPathUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("path", props);
	}
	public UUID getModelerUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("modeler", props);
	}
	
	public boolean getAutoApproved(String props) throws NumberFormatException, TerminologyException, IOException {
		
		try {

			String key = "autoApproved";
			String prop = getProp(key, props);
		
			if (prop.equals("true"))
				return true;
			else 
				return false;

		} catch (IndexOutOfBoundsException ioob) {
			return false;
		}
	}
	
	public boolean getOverridden(String props) throws NumberFormatException, TerminologyException, IOException {
		
		try {

			String key = "overridden";
			String prop = getProp(key, props);
		
			if (prop.equals("true"))
				return true;
			else 
				return false;

		} catch (IndexOutOfBoundsException ioob) {
			return false;
		}
	}
}