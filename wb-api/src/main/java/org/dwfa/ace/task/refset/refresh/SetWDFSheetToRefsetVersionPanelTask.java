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
package org.dwfa.ace.task.refset.refresh;

import java.awt.BorderLayout;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.wfdetailsSheet.ClearWorkflowDetailsSheet;
import org.dwfa.ace.utypes.UniversalAcePosition;
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
 * PanelRefsetVersion
 * panel where the user will be asked to select the version of the refset to
 * refresh.
 * 
 * @author Perry Reid
 * @version 1.0, November 2009
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class SetWDFSheetToRefsetVersionPanelTask extends AbstractTask {

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
    private String refsetVersionPropName = ProcessAttachmentKeys.REFSET_VERSION.getAttachmentKey();
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();

    // Other Properties
    private transient Exception ex = null;

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(refsetVersionPropName);
        out.writeObject(refsetUuidPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields
                profilePropName = (String) in.readObject();
                refsetVersionPropName = (String) in.readObject();
                refsetUuidPropName = (String) in.readObject();
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
        I_ConfigAceFrame config;
        try {
            config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());

            // Clear the Workflow Details Sheet
            ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
            clear.setProfilePropName(getProfilePropName());
            clear.evaluate(process, worker);

            // Create a new panel to add to the Workflow Details Sheet
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
            int width = 475;
            int height = 590;
            workflowDetailsSheet.setSize(width, height);
            workflowDetailsSheet.setLayout(new BorderLayout());
            PanelRefsetVersion newPanel = new PanelRefsetVersion(config);

            /*----------------------------------------------------------------------------------
             *  Initialize the fields on this panel with the previously entered values (if any).
             * ----------------------------------------------------------------------------------
             */
            // Position Set Field Initialization
            try {
                Set<I_Position> previousPositions = new HashSet<I_Position>();
                I_TermFactory tf = Terms.get();

                // Retrieve the positions as Set<UniversalAcePosition> and
                // convert them back to Set<I_Position>
                Set<UniversalAcePosition> universalPositions = (Set<UniversalAcePosition>) process.getProperty(refsetVersionPropName);
                for (UniversalAcePosition univPos : universalPositions) {
                    I_Path path = tf.getPath(univPos.getPathId());
                    I_Position thinPos = tf.newPosition(path, tf.convertToThinVersion(univPos.getTime()));
                    previousPositions.add(thinPos);
                }

                if (previousPositions.size() > 0) {
                    newPanel.setPositionSet(previousPositions);
                }

            } catch (NullPointerException e) {
                // TODO Just ignore the NPE for now - remove this when you add
                // the
                // isPropertyDefined class back in.
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IntrospectionException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            // Set the Refset Label based on the name of the selected refset
            UUID selectedRefsetUUID = null;
            String selectedRefsetText;
            selectedRefsetUUID = (UUID) process.getProperty(refsetUuidPropName);
            if (selectedRefsetUUID != null) {

                I_GetConceptData selectedRefset = (I_GetConceptData) AceTaskUtil.getConceptFromObject(selectedRefsetUUID);
                selectedRefsetText = " " + selectedRefset.getInitialText();

            } else {
                selectedRefsetText = "NO REFSET SELECTED";
            }
            newPanel.setSelectedRefsetSpecLabel(selectedRefsetText);

            /*----------------------------------------------------------------------------------
             *  Add the initialized panel to the Workflow Details Sheet
             * ----------------------------------------------------------------------------------
             */
            workflowDetailsSheet.add(newPanel, BorderLayout.NORTH);
            workflowDetailsSheet.validate();

        } catch (Exception e) {
            ex = e;
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    /**
     * This method returns the name of the Working Profile property name
     * 
     * @return
     */
    public String getProfilePropName() {
        return profilePropName;
    }

    /**
     * This method sets the name of the Working Profile property name
     * 
     * @param profilePropName
     */
    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getRefsetVersionPropName() {
        return refsetVersionPropName;
    }

    public void setRefsetVersionPropName(String refsetVersionPropName) {
        this.refsetVersionPropName = refsetVersionPropName;
    }

    public String getRefsetUuidPropName() {
        return refsetUuidPropName;
    }

    public void setRefsetUuidPropName(String refsetUuidPropName) {
        this.refsetUuidPropName = refsetUuidPropName;
    }

}
