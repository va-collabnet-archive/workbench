/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.ace.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefset;
import org.ihtsdo.workflow.refset.stateTrans.StateTransitionRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

@BeanList(specs = { @Spec(directory = "tasks/workflow", type = BeanType.TASK_BEAN) })
public class UpdateStateTransitionRefset extends AbstractTask {
 
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }
    
    
    
    
    /**
     * @TODO use a type 1 uuid generator instead of a random uuid...
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException 
    {
        System.setProperty("java.awt.headless", "true");
        try {


        	StateTransitionRefset refset = new StateTransitionRefset();
            I_TermFactory tf = Terms.get();

         	 WorkflowHelper.updateWorkflowStates();
          	 WorkflowHelper.updateWorkflowActions();

        	File f = new File("workflow/workflowStateTransitions.txt");
        	processTransitions(f, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_CONCEPTS.getUids()));

	        tf.addUncommitted(refset.getRefsetConcept());
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Couldn't Update State Transitions", e);
		}
        
        return Condition.CONTINUE;
    }

    private void processTransitions(File f, I_GetConceptData useType) throws TerminologyException, IOException {
    	BufferedReader inputFile = new BufferedReader(new FileReader(f));    	

        StateTransitionRefsetWriter writer = new StateTransitionRefsetWriter();
        writer.setWorkflowType(useType);
        String line = null;
        
    	while ((line = inputFile.readLine()) != null)
        {
    		if (line.trim().length() == 0) {
    			continue;
    		}

        	String[] columns = line.split(",");

        	//Get rid of "User permission"
        	columns[0] = (String) columns[0].subSequence("Workflow state transition (".length(), columns[0].length());
        	//remove ")"
        	columns[3] = columns[3].trim();
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


        	try {
	        	writer.setCategory(WorkflowHelper.lookupEditorCategory(columns[0]));
	        	writer.setInitialState(WorkflowHelper.lookupState(columns[1]));
	        	writer.setAction(WorkflowHelper.lookupAction(columns[2]));
	        	writer.setFinalState(WorkflowHelper.lookupState(columns[3]));
	
	        	writer.addMember();
        	} catch (Exception e) {
        		AceLog.getAppLog().log(Level.WARNING, line, e);
        	}
        };

        Terms.get().addUncommitted(writer.getRefsetConcept());
        writer.setWorkflowType(null);
    }
     public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
