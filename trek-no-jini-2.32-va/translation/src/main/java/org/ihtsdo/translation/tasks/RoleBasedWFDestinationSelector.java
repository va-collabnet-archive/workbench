/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
/*
 * Created on Mar 24, 2005
 */
package org.ihtsdo.translation.tasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.ProjectPermissionsAPI;

/**
 * Moves the process to the first identified queue with the outbox attribute. No
 * user interaction is provided.
 * 
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN) })
public class RoleBasedWFDestinationSelector extends AbstractTask {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1;

    /** The Constant dataVersion. */
    private static final int dataVersion = 1;
    
    /** The exit1 prop name. */
    private String exit1PropName;
    
    /** The step role. */
	private TermEntry stepRole;

    /**
     * Write object.
     *
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(exit1PropName);
        out.writeObject(stepRole);
    }

    /**
     * Read object.
     *
     * @param in the in
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        	exit1PropName = (String) in.readObject();
        	stepRole = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * Evaluate.
     *
     * @param process the process
     * @param worker the worker
     * @return the condition
     * @throws TaskFailedException the task failed exception
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     * org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        return Condition.CONTINUE;
    }

    /**
     * Complete.
     *
     * @param process the process
     * @param worker the worker
     * @throws TaskFailedException the task failed exception
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     * org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
    	try {
    		if (stepRole == null || exit1PropName == null) {
				throw new TaskFailedException("Incomplete step data (null)");
			}
    		
    		I_TermFactory tf = Terms.get();
    		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
    		
    		ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);
    		
    		I_GetConceptData projectsRootConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.getUids());

    		I_GetConceptData role=Terms.get().getConcept(stepRole.ids);
    		if (role == null) {
				throw new TaskFailedException("No role (null)");
			}
    		
//    		JComboBox smeCombo = new JComboBox(
//					permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
//							ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids()), 
//							projectsRootConcept).toArray());
    		
    		Object[] options = permissionsApi.getUsersInboxAddressesForRole(role, 
							projectsRootConcept).toArray();


    		//String outboxQueueName = config.getUsername() + ".outbox";
    		String destinationQueueName = (String)JOptionPane.showInputDialog(
                    null,
                    "Choose next destination:",
                    "Destination selection dialog",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    null);
    		
    		process.setProperty(exit1PropName, destinationQueueName);
    		process.setDestination(destinationQueueName);
			process.validateDestination();

        } catch (Exception e) {
        	throw new TaskFailedException(e);
        }
    }

    /**
     * Gets the conditions.
     *
     * @return the conditions
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * Gets the data container ids.
     *
     * @return the data container ids
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[0];
    }

	/**
	 * Gets the exit1 prop name.
	 *
	 * @return the exit1 prop name
	 */
	public String getExit1PropName() {
		return exit1PropName;
	}

	/**
	 * Sets the exit1 prop name.
	 *
	 * @param exit1PropName the new exit1 prop name
	 */
	public void setExit1PropName(String exit1PropName) {
		this.exit1PropName = exit1PropName;
	}

	/**
	 * Gets the step role.
	 *
	 * @return the step role
	 */
	public TermEntry getStepRole() {
		return stepRole;
	}

	/**
	 * Sets the step role.
	 *
	 * @param stepRole the new step role
	 */
	public void setStepRole(TermEntry stepRole) {
		this.stepRole = stepRole;
	}

}
