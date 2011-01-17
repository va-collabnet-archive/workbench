package org.ihtsdo.workflow.refset.mojo.init;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefset;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * @author Alex Byrnes
 * @goal initialize-state-transition-bnf-refset
 * @requiresDependencyResolution compile
 */

public class InitializeStateTransitionBNFMojo extends AbstractMojo {



    private StateTransitionRefsetWriter writer = null;
    private HashMap<String, I_GetConceptData> modelers = null;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        System.setProperty("java.awt.headless", "true");
        try {


        	StateTransitionRefset refset = new StateTransitionRefset();
            I_TermFactory tf = Terms.get();

            writer = new StateTransitionRefsetWriter();

            modelers = new HashMap<String, I_GetConceptData>();
            modelers = WorkflowHelper.getModelers();

            //processNewStateTransitions();
            //processEditStateTransitions();
            processV2StateTransitions();

	        tf.addUncommitted(refset.getRefsetConcept());
	        //RefsetReaderUtility.getContents(refset.getRefsetId());
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());

		}
	}

    private void processV2StateTransitions() throws TerminologyException, IOException {
    	File f = new File("src/main/resources/textRefset/workflowStateTransitions.txt");
    	processTransitions(f, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_CONCEPTS.getUids()));
    }

    private void processTransitions(File f, I_GetConceptData useType) throws TerminologyException, IOException {
    	StateTransitionRefset refset = new StateTransitionRefset();
        Scanner scanner = new Scanner(f);
    	writer.setWorkflowType(useType);

        while (scanner.hasNextLine())
        {
        	String line = scanner.nextLine();

        	String[] columns = line.split(",");
        	//Get rid of "User permission"
        	columns[0] = (String) columns[0].subSequence("Workflow state transition (".length(), columns[0].length());
        	//remove ")"
        	columns[3] = columns[3].substring(0, columns[3].length() - 1);

        	int i = 0;
        	for (String c : columns) {
        		columns[i++] = c.split("=")[1].trim();
        	}

        	////////columns//////////
        	//0: Workflow user role
        	//1: Initial workflow state
        	//2: Workflow action
        	//3: Final workflow state

        	
        	if (line.trim().length() == 0)
        		continue;
        	
        	//System.out.println(columns[0] + "\t" + columns[1] + "\t" + columns[2] + "\n");
            
        	UUID uuid = (UUID)ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids().iterator().next();
        	
        	Iterator architectonicIterator = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids().iterator();
        	while (architectonicIterator.hasNext()) {
        		System.out.println(architectonicIterator.next());
        		
        	}
        	
        	
        	try {
        
        	writer.setCategory(WorkflowHelper.lookupEditorCategory(columns[0]));
        	writer.setInitialState(WorkflowHelper.lookupState(columns[1]));
        	writer.setAction(WorkflowHelper.lookupAction(columns[2]));
        	writer.setFinalState(WorkflowHelper.lookupState(columns[3]));

        	writer.addMember();


        	} catch (Exception e) {
        		e.printStackTrace();
        		System.out.println("Row: " + line);
        		
        	}
        };
        
        Terms.get().addUncommitted(writer.getRefsetConcept());
        writer.setWorkflowType(null);
    }
}
