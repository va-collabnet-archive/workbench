package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefset;
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
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private File baseDirectory;

    /**
     * The name of the database to create. All sql inserts will be against this
     * database.
     * 
     * @parameter
     * @required
     */
    private String filePath;

    private String basePath = "/../resources/";

    private StateTransitionRefsetWriter writer = null;
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
        System.setProperty("java.awt.headless", "true");
        try {

            String resourceFilePath = baseDirectory.getAbsoluteFile() + basePath + filePath;

        	StateTransitionRefset refset = new StateTransitionRefset();
            I_TermFactory tf = Terms.get();
            
            writer = new StateTransitionRefsetWriter();
            processStateTransitions(resourceFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
	}

    private void processStateTransitions(String resourceFilePath) throws TerminologyException, IOException {
    	processTransitions(new File(resourceFilePath), Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_CONCEPTS.getUids()));
    }

    private void processTransitions(File f, I_GetConceptData useType) throws TerminologyException, IOException {
    	I_GetConceptData currentReferencedCompId = null;
    
        Scanner scanner = new Scanner(f);
    	writer.setWorkflowType(useType);

        while (scanner.hasNextLine())
        {
        	String line = scanner.nextLine();
        	
        	if (line.trim().length() == 0)
        		continue;
        	
        	
        	String[] columns = line.split("\t");
        
        	try 
        	{
	        	I_GetConceptData category = WorkflowHelper.lookupEditorCategory(columns[0]);
    			
	        	writer.setCategory(category);
    			writer.setInitialState(WorkflowHelper.lookupState(columns[1]));
	        	writer.setAction(WorkflowHelper.lookupAction(columns[2]));
	        	writer.setFinalState(WorkflowHelper.lookupState(columns[3]));

        	writer.addMember();
        	} catch (Exception e) {
            	AceLog.getAppLog().alertAndLog(Level.SEVERE, line, e);
        	}
        };
        
        Terms.get().addUncommitted(writer.getRefsetConcept());
    }
}
