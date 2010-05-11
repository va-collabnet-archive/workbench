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
package org.dwfa.ace.task.copy;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.AceTaskUtil;
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

@BeanList(specs = { @Spec(directory = "tasks/ide/copy", type = BeanType.TASK_BEAN) })
public class CopyConceptFromViewsInProfileToPathWithStatus extends AbstractTask {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String toPathPropName = ProcessAttachmentKeys.TO_PATH_CONCEPT.getAttachmentKey();
    private String conceptPropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();
    private String statusPropName = ProcessAttachmentKeys.NEW_STATUS.getAttachmentKey();
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(toPathPropName);
        out.writeObject(conceptPropName);
        out.writeObject(statusPropName);
        out.writeObject(profilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            toPathPropName = (String) in.readObject();
            conceptPropName = (String) in.readObject();
            statusPropName = (String) in.readObject();
            profilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_TermFactory tf = LocalVersionedTerminology.get();
            I_Path toPath = tf.getPath(AceTaskUtil.getConceptFromObject(process.readProperty(toPathPropName)).getUids());
            I_GetConceptData concept = AceTaskUtil.getConceptFromObject(process.readProperty(conceptPropName));
            I_GetConceptData newStatus = AceTaskUtil.getConceptFromObject(process.readProperty(statusPropName));
            I_ConfigAceFrame profile = (I_ConfigAceFrame) process.readProperty(profilePropName);
            CopyConceptFromPathToPath.copyFromPathToPath(tf, toPath, concept, profile.getViewPositionSet(), newStatus,
                null);
            tf.addUncommitted(concept);
            tf.commit();

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
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

    public String getConceptPropName() {
        return conceptPropName;
    }

    public void setConceptPropName(String conceptPropName) {
        this.conceptPropName = conceptPropName;
    }

    public String getToPathPropName() {
        return toPathPropName;
    }

    public void setToPathPropName(String toPathPropName) {
        this.toPathPropName = toPathPropName;
    }

    public String getStatusPropName() {
        return statusPropName;
    }

    public void setStatusPropName(String statusPropName) {
        this.statusPropName = statusPropName;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

}
