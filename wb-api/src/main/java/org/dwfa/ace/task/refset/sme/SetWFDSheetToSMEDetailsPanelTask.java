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
package org.dwfa.ace.task.refset.sme;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.commit.TestForCreateNewRefsetPermission;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.wfdetailsSheet.ClearWorkflowDetailsSheet;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task prepares the Workflow Details Sheet to display the SMEDetailsPanel
 * where the user will be asked to enter a number of fields required to start
 * the
 * SME review process.
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf/sme", type = BeanType.TASK_BEAN) })
public class SetWFDSheetToSMEDetailsPanelTask extends AbstractTask {

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
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private String commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();

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
        out.writeObject(commentsPropName);
        out.writeObject(refsetUuidPropName);
        out.writeObject(ownerUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
                profilePropName = (String) in.readObject();
                commentsPropName = (String) in.readObject();
                refsetUuidPropName = (String) in.readObject();
                ownerUuidPropName = (String) in.readObject();
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

            // Get a list of valid Refset Specs
            TreeSet<I_GetConceptData> refsetSpecs = getValidRefsetSpecs();

            TreeMap<String, String> users = getCollabnetUsers();

            // Clear the Workflow Details Sheet
            ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
            clear.setProfilePropName(getProfilePropName());
            clear.evaluate(process, worker);

            // Create a new panel to add to the Workflow Details Sheet
            JPanel workflowDetailsSheet = this.config.getWorkflowDetailsSheet();
            int width = 475;
            int height = 150;
            workflowDetailsSheet.setSize(width, height);
            workflowDetailsSheet.setLayout(new GridLayout(1, 1));
            SMEDetailsPanel newPanel = new SMEDetailsPanel(refsetSpecs, users);

            workflowDetailsSheet.add(newPanel);
        } catch (Exception e) {
            ex = e;
        }
    }

    private TreeMap<String, String> getCollabnetUsers() {
        try {
            TreeMap<String, String> users = new TreeMap<String, String>();
            String fileName = "config" + File.separator + "collabnet-users.txt";
            File file = new File(fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedFileReader = new BufferedReader(fileReader);

            String line = bufferedFileReader.readLine();
            while (line != null) {
                String[] lineParts = line.split(",");
                if (lineParts.length == 2) {
                    users.put(lineParts[0], lineParts[1]);
                }
                line = bufferedFileReader.readLine();
            }

            bufferedFileReader.close();
            return users;
        } catch (IOException e) {
            e.printStackTrace();
            return new TreeMap<String, String>();
        }
    }

    private TreeSet<I_GetConceptData> getValidRefsetSpecs() throws Exception {
        TreeSet<I_GetConceptData> refsetSpecs = new TreeSet<I_GetConceptData>(new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        TestForCreateNewRefsetPermission permissionTest = new TestForCreateNewRefsetPermission();
        Set<I_GetConceptData> permissibleRefsetParents = new HashSet<I_GetConceptData>();
        permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromIndividualUserPermissions(this.owner));
        permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromRolePermissions(this.owner));

        I_IntSet allowedTypes = config.getDestRelTypes();

        for (I_GetConceptData parent : permissibleRefsetParents) {
            Set<? extends I_GetConceptData> children =
                    parent.getDestRelOrigins(null, allowedTypes, null, config.getPrecedence(), config
                        .getConflictResolutionStrategy());
            for (I_GetConceptData child : children) {
                if (isRefset(child)) {
                    RefsetSpec spec = new RefsetSpec(child, true, config);
                    if (spec.isEditableRefset()) {
                        refsetSpecs.add(child);
                    }
                }
            }
        }

        return refsetSpecs;
    }

    private boolean isRefset(I_GetConceptData child) throws TerminologyException, IOException {
        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(RefsetAuxiliary.Concept.SPECIFIES_REFSET.localize().getNid());

        List<? extends I_RelTuple> relationships =
                child.getDestRelTuples(null, allowedTypes, null, config.getPrecedence(), config
                    .getConflictResolutionStrategy());
        if (relationships.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    /*
     * ---------------------------------------------------------
     * Getters and Setters for Property Names
     * ---------------------------------------------------------
     */
    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getRefsetUuidPropName() {
        return refsetUuidPropName;
    }

    public void setRefsetUuidPropName(String refsetUuidPropName) {
        this.refsetUuidPropName = refsetUuidPropName;
    }

    public String getCommentsPropName() {
        return commentsPropName;
    }

    public void setCommentsPropName(String commentsPropName) {
        this.commentsPropName = commentsPropName;
    }

    public String getOwnerUuidPropName() {
        return ownerUuidPropName;
    }

    public void setOwnerUuidPropName(String ownerUuidPropName) {
        this.ownerUuidPropName = ownerUuidPropName;
    }
}
