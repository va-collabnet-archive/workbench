
package org.ihtsdo.workflow.refset.history;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.WorkflowRefset;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefset extends WorkflowRefset  {
	
	public WorkflowHistoryRefset() throws IOException, TerminologyException {
		super (RefsetAuxiliary.Concept.WORKFLOW_HISTORY, true);
	}
	
	public WorkflowHistoryRefset(boolean setupHelper) throws IOException, TerminologyException {
		super (RefsetAuxiliary.Concept.WORKFLOW_HISTORY, setupHelper);
	}

	public Collection<UUID> getRefsetUids() throws TerminologyException, IOException {
		return RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids();
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
	
	public static String generateXmlForXslt(WorkflowHistoryJavaBean bean) {
		StringBuffer retStr = new StringBuffer();
		
		try { 
			retStr.append("<workflow>");
			retStr.append("<id>");
			retStr.append(bean.getWorkflowId().toString());
			retStr.append("</id>");
			retStr.append("<action>");
			retStr.append(processMetaForDisplay(Terms.get().getConcept(bean.getAction())));
			retStr.append("</action>");
			retStr.append("<state>");
			retStr.append(processMetaForDisplay(Terms.get().getConcept(bean.getState())));
			retStr.append("</state>");
			retStr.append("<modeler>");
			retStr.append(Terms.get().getConcept(bean.getModeler()).getInitialText());
			retStr.append("</modeler>");
			retStr.append("<time>");
			retStr.append(WorkflowHelper.format.format(new Date(bean.getWorkflowTime())));
			retStr.append("</time>");
			retStr.append("</workflow>");
		} catch (Exception e) { 
			AceLog.getAppLog().log(Level.WARNING, "Unable to get process row for Xslt for bean: " + bean.toString() + " with error: " + e.getMessage());
		}
		
		return retStr.toString();
	}

	private static String processMetaForDisplay(I_GetConceptData metaCon) throws IOException {
		try {
			String term = WorkflowHelper.getPreferredTerm(metaCon);
			
			return WorkflowHelper.shrinkTermForDisplay(term);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}