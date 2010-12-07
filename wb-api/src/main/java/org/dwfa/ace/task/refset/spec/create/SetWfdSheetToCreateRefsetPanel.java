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
package org.dwfa.ace.task.refset.spec.create;

import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.DetailSheetClientProperties;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.commit.TestForCreateNewRefsetPermission;
import org.dwfa.ace.task.wfdetailsSheet.ClearWorkflowDetailsSheet;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task prepares the Workflow Details Sheet to display the
 * CreateRefsetPanel
 * panel where the user will be asked to enter a number of fields required to
 * start the
 * Create Refset process.
 * 
 * @author Perry Reid
 * @version 1.0, December 2009
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class SetWfdSheetToCreateRefsetPanel extends AbstractTask {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    // Task Attribute Properties
    private String profilePropName = ProcessAttachmentKeys.CURRENT_PROFILE.getAttachmentKey();

    // Other Properties
    private transient Exception ex = null;
    private I_TermFactory termFactory;

    private I_ConfigAceFrame config;
    private I_GetConceptData owner;

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
                profilePropName = (String) in.readObject();
            }
            // Initialize transient properties...
            ex = null;

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * Handles actions required by the task after normal task completion (such
     * as moving a
     * process to another user's input queue).
     * 
     * @return void
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    /**
     * Performs the primary action of the task, which in this case is to gather
     * and
     * validate data that has been entered by the user on the Workflow Details
     * Sheet.
     * 
     * @return The exit condition of the task
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

        try {
            ex = null;
            if (SwingUtilities.isEventDispatchThread()) {
                doRun(process, worker);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        doRun(process, worker);
                    }
                });
            }
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        }
        if (ex != null) {
            throw new TaskFailedException(ex);
        }
        return Condition.CONTINUE;
    }

    private void doRun(final I_EncodeBusinessProcess process, final I_Work worker) {

        try {

            // Initialize class variables
            this.config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
            this.owner = this.config.getDbConfig().getUserConcept();
            this.termFactory = Terms.get();

            // create list of all users
            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(this.config);
            Set<? extends I_GetConceptData> allValidUsers = helper.getAllValidUsers();
            TreeSet<I_GetConceptData> allValidUsersSorted = new TreeSet<I_GetConceptData>();
            for (I_GetConceptData concept : allValidUsers) {
                allValidUsersSorted.add(concept);
            }

            // check permissions for the current user - they require
            // "create new refset" permission either as a user role or
            // individual permission
            TestForCreateNewRefsetPermission permissionTest = new TestForCreateNewRefsetPermission();
            TreeSet<I_GetConceptData> permissibleRefsetParents = new TreeSet<I_GetConceptData>();
            permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromIndividualUserPermissions(owner));
            permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromRolePermissions(owner));

            // Clear the Workflow Details Sheet
            ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
            clear.setProfilePropName(getProfilePropName());
            clear.evaluate(process, worker);

            // Create a new panel to add to the Workflow Details Sheet
            JPanel workflowDetailsSheet = this.config.getWorkflowDetailsSheet();
            int width = 475;
            int height = 590;
            workflowDetailsSheet.setSize(width, height);
            workflowDetailsSheet.setLayout(new GridLayout(1, 1));
            CreateRefsetPanel newPanel = new CreateRefsetPanel(allValidUsersSorted, permissibleRefsetParents);

            /*----------------------------------------------------------------------------------
             *  Add the initialized panel to the Workflow Details Sheet
             * ----------------------------------------------------------------------------------
             */
            workflowDetailsSheet.add(newPanel);
            workflowDetailsSheet.putClientProperty(DetailSheetClientProperties.COMPONENT_FOR_FOCUS, newPanel
                .getRequestedFocus());

        } catch (Exception e) {
            ex = e;
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    /*
     * ====================================================================
     * Getters and Setters
     * ====================================================================
     */

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public I_TermFactory getTermFactory() {
        return termFactory;
    }

    public void setTermFactory(I_TermFactory termFactory) {
        this.termFactory = termFactory;
    }

}
