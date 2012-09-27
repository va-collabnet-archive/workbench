package org.ihtsdo.lucene;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WorkflowLuceneSearchResult {
	String action;
	String state;
	String modeler;
	long time;
	String concept;
	String fsn;
	
	static Map<String, UUID> users = new HashMap<String, UUID>();
	static Map<String, UUID> states = new HashMap<String, UUID>();
	static Map<String, UUID> actions = new HashMap<String, UUID>();

    public WorkflowLuceneSearchResult(WorkflowHistoryJavaBean bean) {
		action = bean.getAction().toString();
		state = bean.getState().toString();
		modeler = bean.getModeler().toString();
		time = bean.getWorkflowTime();
		concept = bean.getConcept().toString();
		fsn = bean.getFullySpecifiedName();
	}
	
	public WorkflowLuceneSearchResult(String[] row, ViewCoordinate vc) {

		try {
			// Should only be used when building database
			action = actions.get(row[WorkflowHelper.actionPosition].toLowerCase()).toString();
    		state = states.get(row[WorkflowHelper.statePosition].toLowerCase()).toString();
    		modeler = users.get(row[WorkflowHelper.modelerPosition].toLowerCase()).toString();

			time = parseTimestampFromFile(row[WorkflowHelper.refsetColumnTimeStampPosition]);
    		concept = row[WorkflowHelper.conceptIdPosition];
    		fsn = row[WorkflowHelper.fsnPosition];
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in processing Workflow Search Result with error: " + e.getMessage());
		}
	}

	public WorkflowLuceneSearchResult(String action, String state, String modeler, long time, String concept, String fsn) {
		this.action = action;
		this.state = state;
		this.modeler = modeler;
		this.time = time;
		this.concept = concept;
		this.fsn = fsn;
	}

	private long parseTimestampFromFile(String time) {
		try {
			return WorkflowHelper.format.parse(time).getTime();
		} catch (ParseException e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in parsing Timestamp from file for time: " + time + " with error: " + e.getMessage());
		}
		
		return 0;
	}

	public WorkflowLuceneSearchResult createLastWfIdLucVals(
			WorkflowHistoryJavaBean bean) {
		return new WorkflowLuceneSearchResult(bean);
	}

	public String getAction() {
		return action;
	}

	public String getState() {
		return state;
	}

	public String getModeler() {
		return modeler;
	}

	public long getTime() {
		return time;
	}

	public String getConcept() {
		return concept;
	}

	public String getFsn() {
		return fsn;
	}

	public int compareTo(WorkflowLuceneSearchResult wfMatchA) {
		// return val < 0 means:     wfMatchA is older than THIS
		return new Long(this.getTime()).compareTo(new Long(wfMatchA.getTime()));	
	}

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();

        str.append("Concept: " + getConcept() + "\n");
        str.append("Modeler: " + getModeler() + "\n");
        str.append("Action: " + getAction() + "\n");
        str.append("State: " + getState() + "\n");
        str.append("Time: " + getTime() + "\n");

        return str.toString();
    }

	public static void initializePossibleResults() throws IOException, TerminologyException {
		users.clear();
		states.clear();
		actions.clear();
    	// initialize users
		
		I_GetConceptData parentEditorConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid());
		for (I_GetConceptData con : WorkflowHelper.getChildren(parentEditorConcept)) {
			users.put(WorkflowHelper.getPrefTerm(con).toLowerCase(), con.getPrimUuid());
		}

    	// initialize states
		I_GetConceptData parentStatesConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_STATES.getPrimoridalUid());
		for (I_GetConceptData con : WorkflowHelper.getChildren(parentStatesConcept)) {
			states.put(WorkflowHelper.getPrefTerm(con).toLowerCase(), con.getPrimUuid());
		}

		// initialize actions
		I_GetConceptData parentActionsConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIONS.getPrimoridalUid());
		for (I_GetConceptData con : WorkflowHelper.getChildren(parentActionsConcept)) {
			actions.put(WorkflowHelper.getPrefTerm(con).toLowerCase(), con.getPrimUuid());
		}
	}

}

