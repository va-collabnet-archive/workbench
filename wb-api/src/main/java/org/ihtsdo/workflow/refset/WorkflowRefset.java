package org.ihtsdo.workflow.refset;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;




/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefset 
{
	protected I_GetConceptData refset = null;
	protected int refsetId = 0;
	protected WorkflowRefsetFields fields = null;
	protected String refsetName = null;
	protected I_HelpRefsets helper = null;

	public WorkflowRefset() throws TerminologyException, IOException {
		helper = Terms.get().getRefsetHelper(Terms.get().getActiveAceFrameConfig());		
	}
	
	public WorkflowRefset (int id, String name) {
		setRefsetName(name);
		setRefsetId(id);
		setRefsetConcept(id);
	}
	
	public void setRefsetId(int id) {
		refsetId = id;
		setRefsetConcept(id);
	}
	
	public void setRefsetName(String name) {
		refsetName = name;
	}
	
	public I_GetConceptData getRefsetConcept() {
		return refset;
	}
	
	public int getRefsetId() {
		return refsetId;
	}
	
	public String getRefsetName() {
		return refsetName;
	}
	
	public void setFields(WorkflowRefsetFields p) {
		fields = p;
	}
	
	public WorkflowRefsetFields getProperties() {
		return fields;
	}

	public String toString() {
		return "Refset: " + refsetName + " (refsetId: " + refsetId + ") with fields: " + 
			   "\n" + fields.toString();
	}

	public String getProp(String key, String props) {
		String fullKey = "<key>" + key + "</key>";
		
		int idx = props.indexOf(fullKey);
		String s = props.substring(idx);
		
		int startIndex = s.indexOf("<value>");
		int endIndex = s.indexOf("</value>");
		
		return s.substring(startIndex + "<value>".length(), endIndex);
	}

	private void setRefsetConcept(int nid) {
		try {
			refset = Terms.get().getConcept(nid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public abstract Collection<UUID> getRefsetUids() throws TerminologyException, IOException;

	protected I_GetConceptData getConcept(String key, String props) {
		String UidString = getProp(key, props);
		try {
			return Terms.get().getConcept(UUID.fromString(UidString));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	protected UUID getUUID(String key, String props) {
		String UidString = getProp(key, props);
		return UUID.fromString(UidString);
	}
}
