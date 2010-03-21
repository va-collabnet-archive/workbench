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
package org.dwfa.ace.task.wfpanel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task enables a user to select the "purpose" of a refset using a
 * ComboBox.
 * The user can only select one purpose per refset.
 * 
 * The purpose taxonomy is located: Refset Auxiliary Concept->refset purpose
 * Once selected, the purpose should be stored as a relationship to the current
 * refset.
 * 
 * @author Perry Reid
 * @version 1.0, October 2009
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/instruct", type = BeanType.TASK_BEAN),
                   @Spec(directory = "tasks/ide/wfpanel", type = BeanType.TASK_BEAN) })
public class SelectRefsetPurpose extends PreviousNextOrCancel {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    // Task Attribute Properties
    private String instruction = "<html>Select Purpose:";
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String refsetUuidPropName = ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey();

    // Other Properties
    private JComboBox refsetSelectionComboBox;
    private I_GetConceptData selectedPurposeConcept;
    private transient TermEntry relType = new TermEntry(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
    private transient TermEntry relCharacteristic = new TermEntry(
        ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
    private transient TermEntry relRefinability = new TermEntry(
        ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
    private transient TermEntry relStatus = new TermEntry(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids());

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(instruction);
        out.writeObject(refsetUuidPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields
                profilePropName = (String) in.readObject();
                instruction = (String) in.readObject();
                refsetUuidPropName = (String) in.readObject();
            } else {
                // Set version 1 default values
                instruction = "<html>Select Purpose:";
                profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
                refsetUuidPropName = ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey();
            }

            // Now initialize transient properties
            relType = new TermEntry(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
            relCharacteristic = new TermEntry(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            relRefinability = new TermEntry(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            relStatus = new TermEntry(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids());

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
     * Performs the primary action of the task, which in this case is to present
     * a small user interface to the user which allows them to select a purpose
     * for this
     * Refset. Once the purpose is selected, the concept associated with this
     * purpose
     * is added as a relationship to the current Refset.
     * 
     * @return The exit condition of the task
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

        try {
            // Present the user interface in the Workflow panel
            DoSwing swinger = new DoSwing(process);
            swinger.start();
            swinger.get();
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }
            restore();

            // check return condition for CONTINUE or ITEM_CANCELLED
            if (returnCondition == Condition.CONTINUE) {

                /*
                 * --------------------------------------------------------------
                 * ---------
                 * Add relationship to the concept that identifies the current
                 * Refset Spec
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * --------------------------------------------------------------
                 * ---------
                 */
                // get selected item from ComboBox presented to the user
                selectedPurposeConcept = (I_GetConceptData) refsetSelectionComboBox.getSelectedItem();

                // Turn the provided refsetSpecUuidPropName (provided in this
                // task) into a refsetSpecConcept
                I_TermFactory termFactory = LocalVersionedTerminology.get();
                UUID refsetUuid = (UUID) process.readProperty(refsetUuidPropName);
                I_GetConceptData refsetConcept = termFactory.getConcept(new UUID[] { refsetUuid });

                // Get the config from the worker
                I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
                if (config.getEditingPathSet().size() == 0) {
                    throw new TaskFailedException("You must select at least one editing path. ");
                }

                // Add the relationship
                LocalVersionedTerminology.get().newRelationship(UUID.randomUUID(), refsetConcept,
                    LocalVersionedTerminology.get().getConcept(relType.ids), selectedPurposeConcept,
                    LocalVersionedTerminology.get().getConcept(relCharacteristic.ids),
                    LocalVersionedTerminology.get().getConcept(relRefinability.ids),
                    LocalVersionedTerminology.get().getConcept(relStatus.ids), 0, config);

                LocalVersionedTerminology.get().addUncommitted(selectedPurposeConcept);
            }

        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (ExecutionException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        }

        return returnCondition;
    }

    /**
     * This method overrides a method by the same name in the parent class. It
     * is used
     * to tell the parent class (PreviousNextOrCancel) whether to show
     * the previous button or not. Since we only want the Next and Cancel
     * buttons,
     * this method returns false .
     */
    @Override
    protected boolean showPrevious() {
        return false;
    }

    /**
     * This subclass allows the task to perform GUI-related work in a dedicated
     * thread
     * using a SwingWorker.
     * 
     * @author Perry Reid
     * @version 1.0, October 2009
     */
    private class DoSwing extends SwingWorker<Boolean> {

        I_EncodeBusinessProcess process;

        public DoSwing(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
        }

        @Override
        protected Boolean construct() throws Exception {
            setup(process);
            return true;
        }

        @Override
        protected void finished() {
            Component[] components = workflowPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                workflowPanel.remove(components[i]);
            }

            workflowPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            // Add the Instructions
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            c.weighty = 0;
            c.anchor = GridBagConstraints.EAST;
            workflowPanel.add(new JLabel(instruction), c);

            // Add the Refset Purpose List ComboBox
            c.gridx++;
            I_GetConceptData purposeList[] = new I_GetConceptData[8];
            try {
                // Keith's original suggested list of purposes
                // purposeList[0] =
                // LocalVersionedTerminology.get().getConcept(RefsetAuxiliary.Concept.ANNOTATION_PURPOSE.getUids());
                // purposeList[1] =
                // LocalVersionedTerminology.get().getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE.getUids());
                // purposeList[2] =
                // LocalVersionedTerminology.get().getConcept(RefsetAuxiliary.Concept.ENUMERATED_ANNOTATION_PURPOSE.getUids());

                // Fetch purpose concepts... and place them in an array
                purposeList[0] = LocalVersionedTerminology.get().getConcept(
                    RefsetAuxiliary.Concept.SIMPLE_COMPONENT.getUids());
                purposeList[1] = LocalVersionedTerminology.get().getConcept(
                    RefsetAuxiliary.Concept.NAVIGATION.getUids());
                // Missing: Reference set descriptor
                purposeList[2] = LocalVersionedTerminology.get().getConcept(
                    RefsetAuxiliary.Concept.ATTRIBUTE_VALUE.getUids());
                // Missing: Simple Map
                purposeList[3] = LocalVersionedTerminology.get().getConcept(
                    RefsetAuxiliary.Concept.ALTERNATIVE_MAP_STATUS.getUids());
                // Missing: Language dialect
                purposeList[4] = LocalVersionedTerminology.get().getConcept(
                    RefsetAuxiliary.Concept.QUERY_SPECIFICATION.getUids());
                purposeList[5] = LocalVersionedTerminology.get().getConcept(
                    RefsetAuxiliary.Concept.ANNOTATION_PURPOSE.getUids());
                // Missing: Association
                purposeList[6] = LocalVersionedTerminology.get().getConcept(
                    RefsetAuxiliary.Concept.MODULE_DEPENDENCY.getUids());
                purposeList[7] = LocalVersionedTerminology.get().getConcept(
                    RefsetAuxiliary.Concept.DESC_TYPE_IS.getUids());
            } catch (TerminologyException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            refsetSelectionComboBox = new JComboBox(purposeList);
            workflowPanel.add(refsetSelectionComboBox, c);

            // Add the processing buttons
            c.weightx = 0.0;
            setupPreviousNextOrCancelButtons(workflowPanel, c);
            workflowPanel.setVisible(true);
        }

    }

    /**
     * Get the instructions for this task
     * 
     * @return The instructions for this task.
     */
    public String getInstruction() {
        return instruction;
    }

    /**
     * Set the instructions for this task
     * 
     * @param instruction The instructions for this task.
     * @return void
     */
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    /**
     * Get the current user's profile name
     * 
     * @return The name of the current user's profile.
     */
    public String getProfilePropName() {
        return profilePropName;
    }

    /**
     * Set the current user's profile name
     * 
     * @param profilePropName The name of the current profile.
     * @return void
     */
    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    /**
     * Get the UUID for the current Refset
     * 
     * @return The UUID for the current Refset
     */
    public String getRefsetUuidPropName() {
        return refsetUuidPropName;
    }

    /**
     * Set the UUID for the current Refset
     * 
     * @param refsetUuidPropName The UUID for the current Refset .
     * @return void
     */
    public void setRefsetUuidPropName(String refsetUuidPropName) {
        this.refsetUuidPropName = refsetUuidPropName;
    }

}
