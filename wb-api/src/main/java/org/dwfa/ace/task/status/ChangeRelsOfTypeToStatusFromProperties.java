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
package org.dwfa.ace.task.status;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
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
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

@BeanList(specs = { @Spec(directory = "tasks/ide/status", type = BeanType.TASK_BEAN) })
public class ChangeRelsOfTypeToStatusFromProperties extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    private String relTypePropName = ProcessAttachmentKeys.REL_TYPE.getAttachmentKey();
    private String newStatusPropName = ProcessAttachmentKeys.NEW_STATUS.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(relTypePropName);
        out.writeObject(newStatusPropName);
        out.writeObject(activeConceptPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            relTypePropName = (String) in.readObject();
            newStatusPropName = (String) in.readObject();
            activeConceptPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            I_GetConceptData concept = (I_GetConceptData) process.getProperty(activeConceptPropName);
            I_GetConceptData relTypeConcept = (I_GetConceptData) process.getProperty(relTypePropName);
            I_GetConceptData newStatusConcept = (I_GetConceptData) process.getProperty(newStatusPropName);
            if (config.getEditingPathSet().isEmpty()) {
                throw new TaskFailedException("You must select at least one editing path. ");
            }

            Set<PositionBI> positionSet = new HashSet<PositionBI>();
            for (PathBI editPath : config.getEditingPathSet()) {
                positionSet.add(Terms.get().newPosition(editPath, Long.MAX_VALUE));
            }
            PositionSetReadOnly positionsForEdit = new PositionSetReadOnly(positionSet);
            I_IntSet typeSet = Terms.get().newIntSet();
            typeSet.add(relTypeConcept.getConceptNid());

            for (I_RelTuple relTuple : concept.getSourceRelTuples(config.getAllowedStatus(), typeSet, positionsForEdit,
                config.getPrecedence(), config.getConflictResolutionStrategy())) {
                for (PathBI editPath : config.getEditingPathSet()) {
                    List<? extends I_RelTuple> editTuples = concept.getSourceRelTuples(config.getAllowedStatus(), typeSet,
                        positionsForEdit, config.getPrecedence(), config.getConflictResolutionStrategy());
                    Set<I_RelPart> partsToAdd = new HashSet<I_RelPart>();
                    for (I_RelTuple t : editTuples) {
                        if (t.getStatusNid() != newStatusConcept.getConceptNid()) {
                            I_RelPart newPart = (I_RelPart) t.makeAnalog(newStatusConcept.getConceptNid(), editPath.getConceptNid(), Long.MAX_VALUE);
                            partsToAdd.add(newPart);
                        }
                    }
                    for (I_RelPart p : partsToAdd) {
                        relTuple.getFixedPart().addVersion(p);
                    }
                }
            }
            Terms.get().addUncommitted(concept);
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getActiveConceptPropName() {
        return activeConceptPropName;
    }

    public void setActiveConceptPropName(String propName) {
        this.activeConceptPropName = propName;
    }

    public String getNewStatusPropName() {
        return newStatusPropName;
    }

    public void setNewStatusPropName(String newStatusPropName) {
        this.newStatusPropName = newStatusPropName;
    }

    public String getRelTypePropName() {
        return relTypePropName;
    }

    public void setRelTypePropName(String relTypePropName) {
        this.relTypePropName = relTypePropName;
    }

}
