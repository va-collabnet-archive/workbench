package org.ihtsdo.workflow.refset;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;

/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefset 
{
	public final static I_ConceptualizeUniversally editorCategoryConcept = RefsetAuxiliary.Concept.EDITOR_CATEGORY;
	public final static I_ConceptualizeUniversally semanticHierarchyConcept = RefsetAuxiliary.Concept.SEMANTIC_HIERARCHY;
	public final static I_ConceptualizeUniversally semanticTagConcept = RefsetAuxiliary.Concept.SEMANTIC_TAGS;
	public final static I_ConceptualizeUniversally stateTransitionConcept = RefsetAuxiliary.Concept.STATE_TRANSITION;
	public final static I_ConceptualizeUniversally workflowHistoryConcept = RefsetAuxiliary.Concept.WORKFLOW_HISTORY;

	protected Collection<UUID> refsetUids = null;
	protected int refsetNid = 0;
	protected String refsetName = null;
	protected I_GetConceptData refsetConcept = null;

	protected WorkflowRefsetFields fields = null;
	
	public WorkflowRefset(I_ConceptualizeUniversally initializingRefset) throws IOException, TerminologyException {
		if (initializingRefset != null) {
			refsetUids = initializingRefset.getUids();
			refsetName = initializingRefset.toString();

			if (Terms.get() != null) {
				refsetNid = Terms.get().uuidToNative(refsetUids);
				refsetConcept = Terms.get().getConcept(refsetUids);
			}
		} else {
			throw new TerminologyException("Refset concept is null");
		}
	}

	public void setRefsetConcept(int id) {
		setRefsetConcept(id, true);
	}
	
	public void setFields(WorkflowRefsetFields p) {
		fields = p;
	}

	public I_GetConceptData getRefsetConcept() {
		return refsetConcept;
	}
	
	public int getRefsetNid() {
		return refsetNid;
	}
	
	public Collection<UUID> getRefsetUids() {
		return refsetUids;
	}
	
	public String getRefsetName() {
		return refsetName;
	}
	
	public WorkflowRefsetFields getProperties() {
		return fields;
	}

	public String toString() {
		return "Refset: " + refsetName + " (refsetId: " + refsetNid + ") with fields: " + 
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
				refsetConcept = Terms.get().getConcept(uid);
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
