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
package org.ihtsdo.project.tasks;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
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
 * The Class.
 */
@BeanList(specs = {
    @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class VerifyAttachedAddresses extends AbstractTask {

    /**
     * The profile prop name.
     */
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    /**
     * The translator.
     */
    public Boolean translator = true;
    /**
     * The reviewer1.
     */
    public Boolean reviewer1 = true;
    /**
     * The reviewer2.
     */
    public Boolean reviewer2 = true;
    /**
     * The sme.
     */
    public Boolean sme = true;
    /**
     * The editorial board.
     */
    public Boolean editorialBoard = true;
    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1;
    /**
     * The Constant dataVersion.
     */
    private static final int dataVersion = 1;

    /**
     * Write object.
     *
     * @param out the out
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(translator);
        out.writeObject(reviewer1);
        out.writeObject(reviewer2);
        out.writeObject(sme);
        out.writeObject(editorialBoard);
        out.writeObject(profilePropName);
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
            translator = (Boolean) in.readObject();
            reviewer1 = (Boolean) in.readObject();
            reviewer2 = (Boolean) in.readObject();
            sme = (Boolean) in.readObject();
            editorialBoard = (Boolean) in.readObject();
            profilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        I_ConfigAceFrame config;

        String translatorStr = null;
        String rev1Str = null;
        String rev2Str = null;
        String smeStr = null;
        String ebStr = null;

        try {
            translatorStr = (String) process.getProperty(ProcessAttachmentKeys.TRANSLATOR_ROLE_INBOX.getAttachmentKey());
            rev1Str = (String) process.getProperty(ProcessAttachmentKeys.REVIEWER_1_ROLE_INBOX.getAttachmentKey());
            rev2Str = (String) process.getProperty(ProcessAttachmentKeys.REVIEWER_2_ROLE_INBOX.getAttachmentKey());
            smeStr = (String) process.getProperty(ProcessAttachmentKeys.SME_ROLE_INBOX.getAttachmentKey());
            ebStr = (String) process.getProperty(ProcessAttachmentKeys.EDITORIAL_BOARD_ROLE_INBOX.getAttachmentKey());

        } catch (IllegalArgumentException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IntrospectionException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IllegalAccessException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (InvocationTargetException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }

        if ((translator && (translatorStr == null || translatorStr.isEmpty()))
                || (reviewer1 && (rev1Str == null || rev1Str.isEmpty()))
                || (reviewer2 && (rev2Str == null || rev2Str.isEmpty()))
                || (sme && (smeStr == null || smeStr.isEmpty()))
                || (editorialBoard && (ebStr == null || ebStr.isEmpty()))) {
            // data already selected
            return Condition.ITEM_CANCELED;
        } else {
            return Condition.ITEM_COMPLETE;
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
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[]{};
    }

    /**
     * Gets the profile prop name.
     *
     * @return the profile prop name
     */
    public String getProfilePropName() {
        return profilePropName;
    }

    /**
     * Sets the profile prop name.
     *
     * @param profilePropName the new profile prop name
     */
    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    /**
     * Gets the translator.
     *
     * @return the translator
     */
    public Boolean getTranslator() {
        return translator;
    }

    /**
     * Sets the translator.
     *
     * @param translator the new translator
     */
    public void setTranslator(Boolean translator) {
        this.translator = translator;
    }

    /**
     * Gets the reviewer1.
     *
     * @return the reviewer1
     */
    public Boolean getReviewer1() {
        return reviewer1;
    }

    /**
     * Sets the reviewer1.
     *
     * @param reviewer1 the new reviewer1
     */
    public void setReviewer1(Boolean reviewer1) {
        this.reviewer1 = reviewer1;
    }

    /**
     * Gets the reviewer2.
     *
     * @return the reviewer2
     */
    public Boolean getReviewer2() {
        return reviewer2;
    }

    /**
     * Sets the reviewer2.
     *
     * @param reviewer2 the new reviewer2
     */
    public void setReviewer2(Boolean reviewer2) {
        this.reviewer2 = reviewer2;
    }

    /**
     * Gets the sme.
     *
     * @return the sme
     */
    public Boolean getSme() {
        return sme;
    }

    /**
     * Sets the sme.
     *
     * @param sme the new sme
     */
    public void setSme(Boolean sme) {
        this.sme = sme;
    }

    /**
     * Gets the editorial board.
     *
     * @return the editorial board
     */
    public Boolean getEditorialBoard() {
        return editorialBoard;
    }

    /**
     * Sets the editorial board.
     *
     * @param editorialBoard the new editorial board
     */
    public void setEditorialBoard(Boolean editorialBoard) {
        this.editorialBoard = editorialBoard;
    }
}