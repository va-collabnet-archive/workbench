package org.ihtsdo.workflow.refset.history;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetReader;



/*
* @author Jesse Efron
*
*/
public  class WorkflowHistoryRefsetReader extends WorkflowRefsetReader
{
	private static final String chiefTermSearchTerm = "Chief Terminologist";
	public static final String chiefTermReplaceTerm = "C. T.";

	public WorkflowHistoryRefsetReader() throws TerminologyException, IOException
	{
		super(workflowHistoryConcept);
	}

	// Workflow ID Only a UUID (No Concept)
	public UUID getWorkflowId(String props) {
		return getUUID("workflowId", props);
	}

	// Workflow ID Only a UUID (No Concept)
	public String getWorkflowIdAsString(String props) {
		return getProp("workflowId", props);
	}

	// I_GetConceptData values where appropriate
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
	
	public String processMetaForDisplay(I_GetConceptData metaCon) throws IOException {
		try {
			String term = WorkflowHelper.getPreferredTerm(metaCon);
			
			if (term.contains(" Workflow ")) {
				term = WorkflowHelper.shrinkTermForDisplay(term);
			}

			if (term.contains(chiefTermSearchTerm)) {
				StringBuffer retBuf = new StringBuffer();
				
				int searchTermBeginIdx = term.indexOf(chiefTermSearchTerm); 
				int searchTermEndIdx = term.indexOf(chiefTermSearchTerm) + chiefTermSearchTerm.length(); 
				
				retBuf.append(term.substring(0, searchTermBeginIdx)); 
				retBuf.append(chiefTermReplaceTerm); 
				retBuf.append(term.substring(searchTermEndIdx));
				
				term = retBuf.toString();
			}
			return term;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
