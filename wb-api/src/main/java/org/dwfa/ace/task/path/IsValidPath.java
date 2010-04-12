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
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@Deprecated
@BeanList(specs = { @Spec(directory = "tasks/ide/path", type = BeanType.TASK_BEAN) })
public class IsValidPath extends AbstractTask {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    private String propName = ProcessAttachmentKeys.PATH_UUID.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(propName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            //
            propName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
           I_TermFactory termFactory = Terms.get();
            Object obj = process.getProperty(propName);
            UUID uuid = null;
            if (obj == null) {
                uuid = null;
            } else {
                uuid = (UUID) obj;
            }
            if (uuid != null) {
                I_GetConceptData path = termFactory.getConcept(new UUID[] { uuid });
                I_GetConceptData pathTopHierarchy = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PATH.getUids());

                I_IntSet allowedStatus = termFactory.getActiveAceFrameConfig().getAllowedStatus();
                I_IntSet allowedTypes = termFactory.newIntSet();
                allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids())
                    .getConceptId());
                PositionSetReadOnly positions = termFactory.getActiveAceFrameConfig().getViewPositionSetReadOnly();

                if (pathTopHierarchy.isParentOf(path, allowedStatus, allowedTypes, positions, 
                    config.getPrecedence(), config.getConflictResolutionStrategy())) {
                    return Condition.TRUE;
                } else {
                    process.setProperty(propName, null);
                    return Condition.FALSE;
                }
            } else {
                process.setProperty(propName, null);
                return Condition.FALSE;
            }
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getPropName() {
        return propName;
    }

    public void setPropName(String propName) {
        this.propName = propName;
    }

}
