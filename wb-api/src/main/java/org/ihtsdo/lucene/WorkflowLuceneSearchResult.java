package org.ihtsdo.lucene;

import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;
import java.util.logging.Level;

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
	
    public WorkflowLuceneSearchResult(WorkflowHistoryJavaBean bean) {
		action = bean.getAction().toString();
		state = bean.getState().toString();
		modeler = bean.getModeler().toString();
		time = bean.getWorkflowTime();
		concept = bean.getConcept().toString();
		fsn = bean.getFSN();
	}
	
	public WorkflowLuceneSearchResult(String[] row, ViewCoordinate vc) {
		
		try {
			if (WorkflowHelper.hasBeenInitialized()) {
				action = WorkflowHelper.lookupAction(row[WorkflowHelper.actionPosition], vc).getPreferredDescription().getText();
				state = WorkflowHelper.lookupState(row[WorkflowHelper.statePosition], vc).getPreferredDescription().getText();
				modeler = WorkflowHelper.lookupModeler(row[WorkflowHelper.modelerPosition]).getPreferredDescription().getText();
			} else {
				// Should only be used when building database
				action = lookupAction(row[WorkflowHelper.actionPosition]).toString();
	    		state = lookupState(row[WorkflowHelper.statePosition]).toString();
	    		modeler = lookupModeler(row[WorkflowHelper.modelerPosition]).toString();
			}
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
	

    private UUID lookupModeler(String modeler) throws IOException, TerminologyException {
    	if (modeler.equalsIgnoreCase("IHTSDO")) {
    		return ArchitectonicAuxiliary.Concept.IHTSDO.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("spackman")) {
    		return ArchitectonicAuxiliary.Concept.KENT_SPACKMAN.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("mvanber")) {
    		return ArchitectonicAuxiliary.Concept.MONIQUE_VAN_BERKUM.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("khaake")) {
    		return ArchitectonicAuxiliary.Concept.KIRSTEN_HAAKE.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("jmirza")) {
    		return ArchitectonicAuxiliary.Concept.JALEH_MIZRA.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("llivesa")) {
    		return ArchitectonicAuxiliary.Concept.PENNY_LIVESAY.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("mgerard")) {
    		return ArchitectonicAuxiliary.Concept.MARY_GERARD.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("msmith")) {
    		return ArchitectonicAuxiliary.Concept.MIKE_SMITH.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("rturnbu")) {
    		return ArchitectonicAuxiliary.Concept.ROBERT_TURNBULL.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("phought")) {
    		return ArchitectonicAuxiliary.Concept.PATRICIA_HOUGHTON.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("pbrottm")) {
    		return ArchitectonicAuxiliary.Concept.PHILLIP_BROTTMAN.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("greynos")) {
    		return ArchitectonicAuxiliary.Concept.GUILLERMO_REYNOSO.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("alopez")) {
    		return ArchitectonicAuxiliary.Concept.ALEJANDRO_LOPEZ.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("emme")) {
    		return ArchitectonicAuxiliary.Concept.EMMA_MELHUISH.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("dkonice")) {
    		return ArchitectonicAuxiliary.Concept.DEBORAH_KONICEK.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("jogo")) {
    		return ArchitectonicAuxiliary.Concept.JO_GOULDING.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("clundbe")) {
    		return ArchitectonicAuxiliary.Concept.CYNDIE_LUNDBERG.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("rmoldwi")) {
    		return ArchitectonicAuxiliary.Concept.RICHARD_MOLDWIN.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("nalbarr")) {
    		return ArchitectonicAuxiliary.Concept.NARCISO_ALBARRACIN.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("vparekh")) {
    		return ArchitectonicAuxiliary.Concept.VARSHA_PAREKH.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("cspisla")) {
    		return ArchitectonicAuxiliary.Concept.CHRISTINE_SPISLA.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("dmcginn")) {
    		return ArchitectonicAuxiliary.Concept.DORIS_MCGINNESS.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("asyed")) {
    		return ArchitectonicAuxiliary.Concept.ASIF_SYED.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("cvalles")) {
    		return ArchitectonicAuxiliary.Concept.CECILIA_VALLESE.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("alejandro")) {
    		return ArchitectonicAuxiliary.Concept.ALEJANDRO_RODRIGUEZ.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("NHS")) {
    		return ArchitectonicAuxiliary.Concept.NHS.getPrimoridalUid();
    	}

    	
    	return null;
    }
    	
	public  UUID lookupAction(String action) throws TerminologyException, IOException {
		if (action.equalsIgnoreCase("Accept workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Chief Terminologist review workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHIEF_TERMINOLOGIST_REVIEW_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Commit workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Commit in batch workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_IN_BATCH_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Discuss workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_DISCUSS_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Escalate workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_ESCALATE_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Review workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_REVIEW_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Override workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_OVERRIDE_ACTION.getPrimoridalUid();
		} else {
			return null;
		} 
				
	}

	public  UUID lookupState(String state) throws TerminologyException, IOException {
		if (state.equalsIgnoreCase("Approved workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_APPROVED_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("Changed workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHANGED_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("Changed in batch workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHANGED_IN_BATCH_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("For Chief Terminologist review workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHIEF_TERMINOLOGIST_REVIEW_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("Concept having no prior workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_INITIAL_HISTORY_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("Concept not previously existing workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CONCEPT_CREATION_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("Escalated workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_ESCALATED_STATE.getPrimoridalUid();
		} else if ((state.equalsIgnoreCase("New workflow state")) || (state.equalsIgnoreCase("first review")))  {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_NEW_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("For review workflow state") || state.equalsIgnoreCase("review chief term")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_REVIEW_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("For discussion workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_DISCUSSION_STATE.getPrimoridalUid();
		} else {
			return null;		
		}
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
}

