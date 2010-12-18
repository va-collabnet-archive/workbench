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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.wfdetailsSheet.ClearWorkflowDetailsSheet;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task prepares the Workflow Details Sheet to display the
 * NewCollabnetQueuePanel where the user will be asked to enter a number of
 * fields required to create a
 * new Collabnet queue.
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf/sme", type = BeanType.TASK_BEAN) })
public class SetWFDSheetToNewCollabnetQueuePanelTask extends AbstractTask {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    // Other Properties
    private transient Exception ex = null;
    private I_TermFactory termFactory;
    private I_ConfigAceFrame config;

    private String profilePropName = ProcessAttachmentKeys.CURRENT_PROFILE.getAttachmentKey();

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
     * as moving a process to another user's input queue).
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
     * and validate data that has been entered by the user on the Workflow
     * Details
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
            this.termFactory = Terms.get();

            TreeSet<I_GetConceptData> workbenchUsers = getWorkbenchUsers();
            TreeMap<String, String> collabnetUsers = getCollabnetUsers();

            // Clear the Workflow Details Sheet
            ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
            clear.setProfilePropName(getProfilePropName());
            clear.evaluate(process, worker);

            // Create a new panel to add to the Workflow Details Sheet
            JPanel workflowDetailsSheet = this.config.getWorkflowDetailsSheet();
            int width = 350;
            int height = 150;
            workflowDetailsSheet.setSize(width, height);
            workflowDetailsSheet.setLayout(new GridLayout(1, 1));
            NewCollabNetQueuePanel newPanel = new NewCollabNetQueuePanel(workbenchUsers, collabnetUsers);

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

    private TreeSet<I_GetConceptData> getWorkbenchUsers() throws Exception {
        TreeSet<I_GetConceptData> workbenchUsers = new TreeSet<I_GetConceptData>(new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        I_GetConceptData userConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.USER.localize().getNid());

        return getChildren(userConcept);
    }

    private TreeSet<I_GetConceptData> getChildren(I_GetConceptData parent) throws Exception {
        TreeSet<I_GetConceptData> childrenTreeSet = new TreeSet<I_GetConceptData>(new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        I_IntSet activeSet = Terms.get().newIntSet();
        activeSet.addAll(config.getAllowedStatus().getSetValues());
        Set<? extends I_GetConceptData> children =
                parent.getDestRelOrigins(activeSet, termFactory.getActiveAceFrameConfig().getDestRelTypes(), config
                    .getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_GetConceptData child : children) {
            childrenTreeSet.add(child);
            childrenTreeSet.addAll(getChildren(child));
        }

        return childrenTreeSet;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }
}
