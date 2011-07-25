package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
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

    private String fileName = "";

    private StateTransitionRefsetWriter writer = null;
	private ViewCoordinate viewCoord;
    
    public InitializeStateTransitionRefset()  
    {
        try {
            viewCoord = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
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
		        	ConceptVersionBI category = WorkflowHelper.lookupEditorCategory(columns[categoryPosition], viewCoord);
	
		        	writer.setCategory(category);
	    			writer.setInitialState(WorkflowHelper.lookupState(columns[initialStatePosition], viewCoord));
		        	writer.setAction(WorkflowHelper.lookupAction(columns[actionPosition], viewCoord));
		        	writer.setFinalState(WorkflowHelper.lookupState(columns[finalStatePosition], viewCoord));

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
}
