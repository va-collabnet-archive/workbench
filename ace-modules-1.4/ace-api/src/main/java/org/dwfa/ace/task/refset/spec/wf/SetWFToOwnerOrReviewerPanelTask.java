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
package org.dwfa.ace.task.refset.spec.wf;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Select the next person the BP will go to. If there are no selected reviewers,
 * than this will automatically be the owner of the BP. Otherwise, the user is
 * prompted to select either the owner or one of the reviewers.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class SetWFToOwnerOrReviewerPanelTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String reviewerUuidPropName = ProcessAttachmentKeys.REVIEWER_UUID.getAttachmentKey();
    private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();

    private transient Exception ex = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(reviewerUuidPropName);
        out.writeObject(ownerUuidPropName);

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            reviewerUuidPropName = (String) in.readObject();
            ownerUuidPropName = (String) in.readObject();
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
        I_ConfigAceFrame config;
        try {
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            config = (I_ConfigAceFrame) termFactory.getActiveAceFrameConfig();

            UUID[] reviewerUuids = (UUID[]) process.readProperty(reviewerUuidPropName);
            I_GetConceptData owner = termFactory.getConcept((UUID[]) process.readProperty(ownerUuidPropName));

            JPanel wfSheet = config.getWorkflowDetailsSheet();
            Component[] components = wfSheet.getComponents();
            for (int i = 0; i < components.length; i++) {
                wfSheet.remove(components[i]);
            }

            int width = 475;
            int height = 400;

            wfSheet.setSize(width, height);
            wfSheet.setLayout(new GridLayout(1, 1));
            wfSheet.add(new SelectOwnerOrReviewerPanel(reviewerUuids, owner));
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

    public String getReviewerUuidPropName() {
        return reviewerUuidPropName;
    }

    public void setReviewerUuidPropName(String reviewerUuidPropName) {
        this.reviewerUuidPropName = reviewerUuidPropName;
    }

    public String getOwnerUuidPropName() {
        return ownerUuidPropName;
    }

    public void setOwnerUuidPropName(String ownerUuidPropName) {
        this.ownerUuidPropName = ownerUuidPropName;
    }
}
