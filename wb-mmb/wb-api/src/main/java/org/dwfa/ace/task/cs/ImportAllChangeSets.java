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
package org.dwfa.ace.task.cs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.logging.Logger;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ComponentValidator;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/change sets", type = BeanType.TASK_BEAN) })
public class ImportAllChangeSets extends AbstractTask {

    private String rootDirStr = "profiles/";

    private Boolean validateChangeSets = true;

    private String validators = null;

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 4;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(rootDirStr);
        out.writeBoolean(validateChangeSets);
        out.writeObject(validators);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            rootDirStr = (String) in.readObject();
            if (objDataVersion > 1) {
                validateChangeSets = in.readBoolean();
            } else {
                validateChangeSets = true;
            }

            if (objDataVersion > 2) {
                validators = (String) in.readObject();
            } else {
                validators = ComponentValidator.class.getName();
            }
            if (objDataVersion < 4) {
                validators = null;
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        Terms.get().suspendChangeSetWriters();
        importAllChangeSets(worker.getLogger());
        Terms.get().resumeChangeSetWriters();

        return Condition.CONTINUE;
    }

    public void importAllChangeSets(Logger log) throws TaskFailedException {
        ChangeSetImporter csi = new ChangeSetImporter() {
            @Override
            public I_ReadChangeSet getChangeSetReader(File csf) {
                try {
                    return Terms.get().newBinaryChangeSetReader(csf);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        csi.importAllChangeSets(log, validators, rootDirStr, validateChangeSets, ".eccs");
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do.

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * @return Returns the message.
     */
    public String getRootDirStr() {
        return rootDirStr;
    }

    /**
     * @param message
     *            The message to set.
     */
    public void setRootDirStr(String rootDirStr) {
        this.rootDirStr = rootDirStr;
    }

    public Boolean getValidateChangeSets() {
        return validateChangeSets;
    }

    public void setValidateChangeSets(Boolean validateChangeSets) {
        this.validateChangeSets = validateChangeSets;
    }

    public String getValidators() {
        return validators;
    }

    public void setValidators(String validators) {
        this.validators = validators;
    }
}
