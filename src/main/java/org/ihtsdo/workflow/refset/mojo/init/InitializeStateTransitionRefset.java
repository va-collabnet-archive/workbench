package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetWriter;


/**
 * @author Jesse Efron
 * 
 */
  
public class InitializeStateTransitionRefset implements I_InitializeWorkflowRefset {

    private static final int categoryPosition = 0;								// 0
    private static final int initialStatePosition = categoryPosition + 1;		// 1
    private static final int actionPosition = initialStatePosition + 1;			// 2
    private static final int finalStatePosition = actionPosition + 1;			// 3
    private static final int numberOfColumns = finalStatePosition + 1;			// 4

    private String fileName = "stateTransRefset.txt";

    private StateTransitionRefsetWriter writer = null;
    
    public InitializeStateTransitionRefset()  
    {
        try {
            writer = new StateTransitionRefsetWriter();
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to initialize state transition refset with error: " + e.getMessage());
		}
	}

    @Override
    public boolean initializeRefset(String resourceFilePath)  {
    	String line = null;

    	try {
    		File f = new File(resourceFilePath + File.separatorChar + fileName);
	    	BufferedReader inputFile = new BufferedReader(new FileReader(f));    	
	    	I_GetConceptData useType = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_CONCEPTS.getUids());
	    	
	    	writer.setWorkflowType(useType);
	    	
	    	while ((line = inputFile.readLine()) != null)
	        {
	        	if (line.trim().length() == 0) {
	        		continue;
	        	}
	        	
	        	String[] columns = line.split("\t");
	
	        	if (columns.length == numberOfColumns)
	        	{
		        	writer.setCategory(lookupRole(columns[categoryPosition]));
		        	writer.setInitialState(lookupState(columns[initialStatePosition]));
		        	writer.setAction(lookupAction(columns[actionPosition]));
		        	writer.setFinalState(lookupState(columns[finalStatePosition]));

		        	writer.addMember();
	        	} else {
	            	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into state transition refset: " + line);
	        	}
	        }

        	Terms.get().addUncommitted(writer.getRefsetConcept());
        	return true;
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into state transition refset: " + line);
        	return false;
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
		} else if (state.equalsIgnoreCase("Initial history workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_INITIAL_HISTORY_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("Create concept workflow state")) {
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
		}
		else {
			return null;
		} 
	}

	private UUID lookupRole(String role) throws IOException, TerminologyException {
	   	if (role.equalsIgnoreCase("Clinical editor role A")) {
	   		return ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_A.getPrimoridalUid();
	   	} else if (role.equalsIgnoreCase("Clinical editor role B")) {
	   		return ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_B.getPrimoridalUid();
	   	} else if (role.equalsIgnoreCase("Clinical editor role C")) {
	   		return ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_C.getPrimoridalUid();
	   	} else if (role.equalsIgnoreCase("Clinical editor role D")) {
	   		return ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_D.getPrimoridalUid();
	   	} else if (role.equalsIgnoreCase("Clinical editor role All")) {
	   		return ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_ALL.getPrimoridalUid();
	   	}
	   	
	   	return null;
   	}
}
