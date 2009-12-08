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
package org.dwfa.ace.task.refset.members;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.refset.MemberRefsetHelper;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/refset/membership", type = BeanType.TASK_BEAN) })
public class AddConceptToRefset extends AbstractTask {

    private static final long serialVersionUID = -1488580246193922770L;

    private static final int dataVersion = 1;

    /** the refset we are adding to */
    private String refsetConceptPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();

    /** the concept to be added to the refset */
    private String memberConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();

    /** the value to be given to the new concept extension */
    private String conceptExtValuePropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.refsetConceptPropName);
        out.writeObject(this.memberConceptPropName);
        out.writeObject(this.conceptExtValuePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            this.refsetConceptPropName = (String) in.readObject();
            this.memberConceptPropName = (String) in.readObject();
            this.conceptExtValuePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_GetConceptData refset = (I_GetConceptData) process.readProperty(refsetConceptPropName);
            I_GetConceptData member = (I_GetConceptData) process.readProperty(memberConceptPropName);
            I_GetConceptData value = (I_GetConceptData) process.readProperty(conceptExtValuePropName);

            if (refset == null) {
                throw new TerminologyException("A working refset has not been selected.");
            }

            if (member == null) {
                throw new TerminologyException("No member concept selected.");
            }

            if (value == null) {
                throw new TerminologyException("No concept extension value selected.");
            }

            getLogger().info(
                "Adding concept '" + member.getInitialText() + "' as member of refset '" + refset.getInitialText()
                    + "' with a value '" + value.getInitialText() + "'.");

            new MemberRefsetHelper(refset.getConceptId(), value.getConceptId()).addToRefset(member.getConceptId());

            // use commit in business process

            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException("Unable to add concept to refset. " + e.getMessage(), e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getRefsetConceptPropName() {
        return refsetConceptPropName;
    }

    public void setRefsetConceptPropName(String refsetConceptPropName) {
        this.refsetConceptPropName = refsetConceptPropName;
    }

    public String getMemberConceptPropName() {
        return memberConceptPropName;
    }

    public void setMemberConceptPropName(String memberConceptPropName) {
        this.memberConceptPropName = memberConceptPropName;
    }

    public String getConceptExtValuePropName() {
        return conceptExtValuePropName;
    }

    public void setConceptExtValuePropName(String conceptExtValuePropName) {
        this.conceptExtValuePropName = conceptExtValuePropName;
    }

}
