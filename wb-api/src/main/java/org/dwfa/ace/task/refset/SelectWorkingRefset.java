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
package org.dwfa.ace.task.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
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
 * Picks up a refset concept from a property and sets into the users
 * configuration context so other
 * business processes can pick up on it later on.
 * 
 * The users configuration is also set to ensure they can correct view and edit
 * concept refsets.
 * 
 * @see GetWorkingRefset
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/refset", type = BeanType.TASK_BEAN) })
public class SelectWorkingRefset extends AbstractTask {

    private static final long serialVersionUID = -3119550198197703394L;
    private static final int dataVersion = 1;

    private String componentPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.componentPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            this.componentPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_GetConceptData refset = (I_GetConceptData) process.getProperty(componentPropName);

            I_TermFactory tf = Terms.get();

            I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

            config.setContext(refset);

            // TODO: view all paths, edit only own edit path

            // show viewer images and refset info in taxonomy view
            config.setShowRefsetInfoInTaxonomy(true);
            config.setShowViewerImagesInTaxonomy(true);

            // view selected refset
            I_IntList refsetsToShow = config.getRefsetsToShowInTaxonomy();
            refsetsToShow.clear();
            refsetsToShow.add(refset.getConceptNid());

            // view ancillary refsets
            int parentRefsetId = Terms.get().getMarkedParentRefsetHelper(config, refset.getConceptNid(), 0).getParentRefset();
            refsetsToShow.add(parentRefsetId);

            // TODO: enable concept refsets

            // TODO: set concept refset defaults

            // TODO: set refset status types (current & retired)

            // TODO: set normal member concept type

            // TODO: set refset type to selected

            // TODO: enable refset toggle in component panel
            // config.setRefsetInToggleVisible(REFSET_TYPES.CONCEPT,
            // TOGGLES.REFSETS, true);

            config.setStatusMessage("Now working on the refset: " + refset.getInitialText());

            config.fireCommit();

            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException("Unable to select working refset.", e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getComponentPropName() {
        return componentPropName;
    }

    public void setComponentPropName(String componentPropName) {
        this.componentPropName = componentPropName;
    }

}
