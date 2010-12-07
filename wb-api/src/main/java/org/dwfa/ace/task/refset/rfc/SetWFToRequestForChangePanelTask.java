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
package org.dwfa.ace.task.refset.rfc;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.commit.TestForCreateNewRefsetPermission;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
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
 * Shows the RFC panel in the WF panel.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class SetWFToRequestForChangePanelTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 2;

    private transient Exception ex = null;
    private I_TermFactory termFactory;
    private I_ConfigAceFrame config;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        } else if (objDataVersion == 2) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

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
            termFactory = Terms.get();
            config = (I_ConfigAceFrame) termFactory.getActiveAceFrameConfig();

            Set<I_GetConceptData> refsets = getValidRefsets();

            JPanel wfSheet = config.getWorkflowDetailsSheet();
            Component[] components = wfSheet.getComponents();
            for (int i = 0; i < components.length; i++) {
                wfSheet.remove(components[i]);
            }

            int width = 475;
            int height = 475;

            wfSheet.setSize(width, height);
            wfSheet.setLayout(new GridLayout(1, 1));

            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
            Set<? extends I_GetConceptData> allValidUsers = helper.getAllValidUsers();

            RequestForChangePanel rfcp = new RequestForChangePanel(refsets, allValidUsers);
            wfSheet.add(rfcp);
            rfcp.focusOnRefsetName();
            wfSheet.repaint();
        } catch (Exception e) {
            ex = e;
        }
    }

    private TreeSet<I_GetConceptData> getValidRefsets() throws Exception {
        TreeSet<I_GetConceptData> refsets = new TreeSet<I_GetConceptData>();

        I_GetConceptData owner = config.getDbConfig().getUserConcept();
        TestForCreateNewRefsetPermission permissionTest = new TestForCreateNewRefsetPermission();
        Set<I_GetConceptData> permissibleRefsetParents = new HashSet<I_GetConceptData>();
        permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromIndividualUserPermissions(owner));
        permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromRolePermissions(owner));

        I_IntSet allowedTypes = config.getDestRelTypes();

        for (I_GetConceptData parent : permissibleRefsetParents) {
            Set<? extends I_GetConceptData> children =
                    parent.getDestRelOrigins(null, allowedTypes, null, config.getPrecedence(), config
                        .getConflictResolutionStrategy());
            for (I_GetConceptData child : children) {
                if (isRefset(child)) {
                    RefsetSpec spec = new RefsetSpec(child, true, config);
                    if (spec.isEditableRefset()) {
                        refsets.add(child);
                    }
                }
            }
        }

        return refsets;
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

}
