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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_HostConceptPlugins;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ComponentValidator;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;

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
        try {
            Terms.get().suspendChangeSetWriters();
            importAllChangeSets(worker.getLogger(), false);
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            
            I_AmTermComponent termComponent1 = config.getConceptViewer(1).getTermComponent();
            config.getConceptViewer(1).setTermComponent(termComponent1);

            I_AmTermComponent termComponent2 = config.getConceptViewer(2).getTermComponent();
            config.getConceptViewer(2).setTermComponent(termComponent2);

            I_AmTermComponent termComponent3 = config.getConceptViewer(3).getTermComponent();
            config.getConceptViewer(3).setTermComponent(termComponent3);

            I_AmTermComponent termComponent4 = config.getConceptViewer(4).getTermComponent();
            config.getConceptViewer(4).setTermComponent(termComponent4);
            
            Terms.get().resumeChangeSetWriters();

            return Condition.CONTINUE;
        } catch (TerminologyException ex) {
            throw new TaskFailedException(ex);
        } catch (IOException ex) {
            throw new TaskFailedException(ex);
        }
    }

    public void importAllChangeSets(Logger log, boolean fromMojo) throws TaskFailedException {
        ChangeSetImporter csi = new ChangeSetImporter(fromMojo) {
            @Override
            public I_ReadChangeSet getChangeSetReader(File csf) {
                try {
                    return Terms.get().newBinaryChangeSetReader(csf);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public I_ReadChangeSet getChangeSetWfHxReader(File csf) {
                try {
                    return Terms.get().newWfHxLuceneChangeSetReader(csf);
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
