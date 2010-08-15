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
package org.dwfa.ace.task.path;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
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
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

@BeanList(specs = { @Spec(directory = "tasks/ide/path", type = BeanType.TASK_BEAN) })
public class SetEditPathFromProperty extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private String editPathPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();

    private boolean keepExistingEditPaths = false;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(editPathPropName);
        out.writeObject(keepExistingEditPaths);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion > dataVersion) {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
        editPathPropName = (String) in.readObject();
        if (objDataVersion > 1) {
            keepExistingEditPaths = (Boolean) in.readObject();
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_GetConceptData pathConcept = getProperty(process, I_GetConceptData.class, editPathPropName);

            I_TermFactory tf = Terms.get();

            PathBI editPath = tf.getPath(pathConcept.getUids());
            I_ConfigAceFrame frameConfig = tf.getActiveAceFrameConfig();

            Set<PathBI> editSet = frameConfig.getEditingPathSet();
            if (!keepExistingEditPaths) {
                editSet.clear();
            }
            frameConfig.addEditingPath(editPath);

            Set<PositionBI> viewPositionSet = frameConfig.getViewPositionSet();
            viewPositionSet.add(tf.newPosition(editPath, Integer.MAX_VALUE));

            frameConfig.fireUpdateHierarchyView();

            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getEditPathPropName() {
        return editPathPropName;
    }

    public void setEditPathPropName(String editPathPropName) {
        this.editPathPropName = editPathPropName;
    }

    public boolean isKeepExistingEditPaths() {
        return keepExistingEditPaths;
    }

    public void setKeepExistingEditPaths(boolean keepExistingEditPaths) {
        this.keepExistingEditPaths = keepExistingEditPaths;
    }

}
