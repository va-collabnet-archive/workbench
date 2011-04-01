package org.ihtsdo.workflow.refset;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary.Concept;
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
	
	public abstract Collection<UUID> getRefsetUids() throws TerminologyException, IOException;

	public WorkflowRefset(Concept con, boolean setupDatabaseObjects) throws IOException, TerminologyException {
		this(con.localize().getNid(), con.toString(), setupDatabaseObjects);
		if (setupDatabaseObjects) {
			helper = Terms.get().getRefsetHelper(Terms.get().getActiveAceFrameConfig());
		}
	}
	
	public WorkflowRefset (int id, String name, boolean setupDatabaseObjects) {
		setRefsetName(name);
		setRefsetId(id, setupDatabaseObjects);
		
		if (setupDatabaseObjects) {
			setRefsetConcept(id, setupDatabaseObjects);
		}
	}
	
	public WorkflowRefset() {

	}

	public void setRefsetId(int id) {
		refsetId = id;
		setRefsetConcept(id, true);
	}

	public void setRefsetId(int id, boolean setupDatabaseObjects) {
		refsetId = id;
		setRefsetConcept(id, setupDatabaseObjects);
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
		if (idx < 0)
			return "";
		
		String s = props.substring(idx);
		
		int startIndex = s.indexOf("<value>");
		int endIndex = s.indexOf("</value>");
		
		return s.substring(startIndex + "<value>".length(), endIndex);
	}

	private void setRefsetConcept(int uid, boolean setupDatabaseObjects) {
		if (setupDatabaseObjects) {
			try {
				refset = Terms.get().getConcept(uid);
			} catch (Exception e) {
	        	AceLog.getAppLog().log(Level.WARNING, "Error retrieving Refset Concept: " + uid + " with error: " + e.getMessage());
			}
		}
	}
	
	protected I_GetConceptData getConcept(String key, String props) {
		String UidString = getProp(key, props);
		
		if (UidString.length() > 0)
		{
			try {
				return Terms.get().getConcept(UUID.fromString(UidString));
			} catch (Exception e) {
	        	AceLog.getAppLog().log(Level.WARNING, "Error retrieving Concept: " + UidString + "  with error: " + e.getMessage());
			}
		}
		
		return null;
	}

	protected UUID getUUID(String key, String props) {
		String UidString = getProp(key, props);
		
		if (UidString.length() < 0)
			return null;
		
		try {
			return UUID.fromString(UidString);
		} catch (Exception e) {
		}
		
		return null;
	}
}
