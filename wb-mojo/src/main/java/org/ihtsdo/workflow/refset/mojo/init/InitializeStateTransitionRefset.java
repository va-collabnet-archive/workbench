package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;


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
		Map<String, UUID> categories = new HashMap<String, UUID>();
		Map<String, UUID> actions = new HashMap<String, UUID>();
		Map<String, UUID> states = new HashMap<String, UUID>();

    	try {
    		File f = new File(resourceFilePath + File.separatorChar + fileName);
	    	BufferedReader inputFile = new BufferedReader(new FileReader(f));    	
	    	I_GetConceptData useType = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_CONCEPTS.getUids());
	    	
	    	writer.setWorkflowType(useType);
	    	
	    	// initialize categories
			I_GetConceptData parentCategoryConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ROLES.getPrimoridalUid());
			for (I_GetConceptData con : WorkflowHelper.getChildren(parentCategoryConcept)) {
				categories.put(WorkflowHelper.getPrefTerm(con).toLowerCase(), con.getPrimUuid());
			}

	    	// initialize states
			I_GetConceptData parentStatesConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_STATES.getPrimoridalUid());
			for (I_GetConceptData con : WorkflowHelper.getChildren(parentStatesConcept)) {
				states.put(WorkflowHelper.getFsn(con).toLowerCase(), con.getPrimUuid());
			}

			// initialize actions
			I_GetConceptData parentActionsConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIONS.getPrimoridalUid());
			for (I_GetConceptData con : WorkflowHelper.getChildren(parentActionsConcept)) {
				actions.put(WorkflowHelper.getFsn(con).toLowerCase(), con.getPrimUuid());
			}

			while ((line = inputFile.readLine()) != null)
	        {
	        	if (line.trim().length() == 0) {
	        		continue;
	        	}
	        	
	        	String[] columns = line.split("\t");
	
	        	if (columns.length == numberOfColumns)
	        	{
		        	writer.setCategory(categories.get(columns[categoryPosition].toLowerCase()));
		        	writer.setInitialState(states.get(columns[initialStatePosition].toLowerCase()));
		        	writer.setAction(actions.get(columns[actionPosition].toLowerCase()));
		        	writer.setFinalState(states.get(columns[finalStatePosition].toLowerCase()));

		        	writer.addMember(true);
	        	} else {
	            	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into state transition refset: " + line);
	        	}
	        }

        	return true;
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into state transition refset: " + line);
        	return false;
    	}
    }
}
