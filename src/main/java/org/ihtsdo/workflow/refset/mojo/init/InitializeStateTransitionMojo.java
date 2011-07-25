package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;


/**
 * @author Jesse Efron
 * 
 * @goal initialize-state-transition-refset
 * @requiresDependencyResolution compile
 */
  
public class InitializeStateTransitionMojo extends AbstractMojo {

    /**
     * The name of the database to create. All sql inserts will be against this
     * database.
     * 
     * @parameter
     * @required
     */
    private String filePath;

    /**
     * Whether to alert user of a bad row that can't be imported into the database
     * 
     * @parameter
     * default-value=true
     * @required
     */
    private boolean reportErrors;

    private static final int categoryPosition = 0;								// 0
    private static final int initialStatePosition = categoryPosition + 1;		// 1
    private static final int actionPosition = initialStatePosition + 1;			// 2
    private static final int finalStatePosition = actionPosition + 1;			// 3

    private static final int numberOfColumns = finalStatePosition + 1;			// 4

    private StateTransitionRefsetWriter writer = null;

	private ViewCoordinate viewCoord;
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        System.setProperty("java.awt.headless", "true");
        try {
            viewCoord = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
            writer = new StateTransitionRefsetWriter();
            processStateTransitions(filePath);
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to initialize state transition refset with error: " + e.getMessage());
		}
	}

    private void processStateTransitions(String resourceFilePath) throws TerminologyException, IOException, ContraditionException {
    	processTransitions(new File(resourceFilePath), Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_CONCEPTS.getUids()));
    }

    private void processTransitions(File f, I_GetConceptData useType) throws TerminologyException, IOException, ContraditionException {
    	BufferedReader inputFile = new BufferedReader(new FileReader(f));    	
    	writer.setWorkflowType(useType);
    	String line = null;
    	
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
        	}
    		else if (reportErrors) {
            	AceLog.getAppLog().log(Level.WARNING, line, new Exception("Unable to import this row into state transition refset"));        
        	}
        }
    	try {
        	Terms.get().addUncommitted(writer.getRefsetConcept());
    	} catch (Exception e) {
    		System.out.println("Writer: " + writer + "\nwith refset writer Concept" + writer.getRefsetConcept().getInitialText());
    	}
    }
}
