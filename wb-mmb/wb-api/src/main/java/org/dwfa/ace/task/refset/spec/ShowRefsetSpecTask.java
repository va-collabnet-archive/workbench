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
package org.dwfa.ace.task.refset.spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
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
 * Shows the refset spec panel. An optional refset spec can be specified to show
 * in the panel.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class ShowRefsetSpecTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 2;

    private String refsetUuidPropName = ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(refsetUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion < 2) {
                refsetUuidPropName = ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey();
            } else {
                refsetUuidPropName = (String) in.readObject();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        worker.getLogger().info("Starting: " + this.toString());
        try {

            I_TermFactory termFactory = Terms.get();

            Object obj = process.getProperty(refsetUuidPropName);
            UUID uuid = null;
            if (obj == null) {
                uuid = null;
            } else {
                uuid = (UUID) obj;
            }

            if (uuid != null) {
                I_GetConceptData refset = termFactory.getConcept(new UUID[] { uuid });

                // set new spec as focus
                termFactory.getActiveAceFrameConfig().setRefsetInSpecEditor(refset);
                termFactory.getActiveAceFrameConfig().setShowQueueViewer(false);
                termFactory.getActiveAceFrameConfig().showRefsetSpecPanel();
            } else {
                termFactory.getActiveAceFrameConfig().setShowQueueViewer(false);
                termFactory.getActiveAceFrameConfig().showRefsetSpecPanel();
            }

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getRefsetUuidPropName() {
        return refsetUuidPropName;
    }

    public void setRefsetUuidPropName(String refsetUuidPropName) {
        this.refsetUuidPropName = refsetUuidPropName;
    }
}
