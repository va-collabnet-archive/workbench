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
package org.dwfa.ace.task.profile;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile", type = BeanType.TASK_BEAN) })
public class EditSetDefault extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private TermEntry defaultConcept = new TermEntry(
        ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());

    private EditDefaultTypes type = EditDefaultTypes.DESC_TYPE;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(defaultConcept);
        out.writeObject(type);
        out.writeObject(profilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            defaultConcept = (TermEntry) in.readObject();
            type = (EditDefaultTypes) in.readObject();
            profilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ImplementTermFactory termFactory = (I_ImplementTermFactory) Terms.get();
            I_ConfigAceFrame profile = (I_ConfigAceFrame) process.getProperty(profilePropName);
            if (profile == null) {
                profile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            }
            switch (type) {
            case REL_CHARACTERISTIC:
                profile.setDefaultRelationshipCharacteristic(termFactory.getConcept(defaultConcept.ids));
                break;
            case REL_REFINABILITY:
                profile.setDefaultRelationshipRefinability(termFactory.getConcept(defaultConcept.ids));
                break;
            case REL_TYPE:
                profile.setDefaultRelationshipType(termFactory.getConcept(defaultConcept.ids));
                break;
            case STATUS:
                profile.setDefaultStatus(termFactory.getConcept(defaultConcept.ids));
                break;
            case DESC_TYPE:
                profile.setDefaultDescriptionType(termFactory.getConcept(defaultConcept.ids));
                break;
            case IMG_TYPE:
                profile.setDefaultImageType(termFactory.getConcept(defaultConcept.ids));
                break;
            default:
                throw new TaskFailedException("Can't handle type: " + type);
            }
            return Condition.CONTINUE;
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public TermEntry getDefaultConcept() {
        return defaultConcept;
    }

    public void setDefaultConcept(TermEntry defaultConcept) {
        this.defaultConcept = defaultConcept;
    }

    public EditDefaultTypes getType() {
        return type;
    }

    public void setType(EditDefaultTypes type) {
        this.type = type;
    }
}
