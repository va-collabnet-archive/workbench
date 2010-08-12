package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.ihtsdo.cement.WorkflowAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.workflow.refset.strans.StateTransitionRefset;
import org.ihtsdo.workflow.refset.strans.StateTransitionRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetHelper;

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

    private StateTransitionRefsetWriter writer = null;
    private HashMap<String, I_GetConceptData> modelers = null;
    
    public void execute() throws MojoExecutionException, MojoFailureException 
    {
    	StateTransitionRefset refset = new StateTransitionRefset();
        System.setProperty("java.awt.headless", "true");
        try {
            try {
                if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(), this.getClass(), targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }

            I_TermFactory tf = Terms.get();
            
            writer = new StateTransitionRefsetWriter();

            modelers = new HashMap<String, I_GetConceptData>();
            modelers = WorkflowRefsetHelper.getModelers();

            processNewStateTransitions();
            processEditStateTransitions();
                                
	        tf.addUncommitted(tf.getConcept(refset.getRefsetConcept()));
	        //RefsetReaderUtility.getContents(refset.getRefsetId());
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
	}

    private void processNewStateTransitions() throws TerminologyException, IOException {
    	File f = new File("src/main/resources/textRefset/newStateTransRefset.txt");
    	processTransitions(f, Terms.get().getConcept(WorkflowAuxiliary.Concept.NEW_USE_CASE.getUids()));
    }

    private void processEditStateTransitions() throws TerminologyException, IOException {
    	File f = new File("src/main/resources/textRefset/editStateTransRefset.txt");
    	processTransitions(f, Terms.get().getConcept(WorkflowAuxiliary.Concept.EDIT_USE_CASE.getUids()));
    }
    
    private void processTransitions(File f, I_GetConceptData useType) throws TerminologyException, IOException {
    	StateTransitionRefset refset = new StateTransitionRefset();
        Scanner scanner = new Scanner(f);
    	writer.setWorkflowType(useType);

        while (scanner.hasNextLine())
        {
        	String line = scanner.nextLine();
        	String[] columns = line.split("\t");
        
        	writer.setEditorCategory(WorkflowRefsetHelper.getModelerCategory(columns[0]));
        	writer.setInitialState(WorkflowRefsetHelper.getState(columns[1]));
        	writer.setAction(WorkflowRefsetHelper.getAction(columns[2]));
        	writer.setFinalState(WorkflowRefsetHelper.getState(columns[3]));

        	writer.addMember();

	        Collection<? extends I_ExtendByRef> extVersions = Terms.get().getRefsetExtensionMembers(refset.getRefsetId());
	        int size = extVersions.size();
        };
        
        writer.setWorkflowType(null);
    }
}
