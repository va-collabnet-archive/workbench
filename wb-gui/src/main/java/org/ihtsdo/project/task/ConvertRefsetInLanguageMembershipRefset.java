/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.project.task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.refset.LanguageMembershipRefset;

/**
 * The Class CreateNewProject.
 */
@BeanList(specs = {
    @Spec(directory = "tasks/project tasks", type = BeanType.TASK_BEAN)})
public class ConvertRefsetInLanguageMembershipRefset extends AbstractTask {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1;
    /**
     * The Constant dataVersion.
     */
    private static final int dataVersion = 1;
    /**
     * The lang code.
     */
    private String langCode;

    /**
     * Write object.
     *
     * @param out the out
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(langCode);
    }

    /**
     * Read object.
     *
     * @param in the in
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            langCode = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * Instantiates a new creates the new project.
     *
     * @throws MalformedURLException the malformed url exception
     */
    public ConvertRefsetInLanguageMembershipRefset() throws MalformedURLException {
        super();
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker
                    .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            LanguageMembershipRefset.createLanguageMembershipRefsetFromConcept(config.getHierarchySelection(),
                    langCode.trim(), config);
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[]{};
    }

    /**
     * Gets the lang code.
     *
     * @return the lang code
     */
    public String getLangCode() {
        return langCode;
    }

    /**
     * Sets the lang code.
     *
     * @param langCode the new lang code
     */
    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }
}