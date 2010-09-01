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
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
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
public class CopyConceptFromPathToPath extends AbstractTask {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String fromPathPropName = ProcessAttachmentKeys.FROM_PATH_CONCEPT.getAttachmentKey();
    private String toPathPropName = ProcessAttachmentKeys.TO_PATH_CONCEPT.getAttachmentKey();
    private String conceptPropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(fromPathPropName);
        out.writeObject(toPathPropName);
        out.writeObject(conceptPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            fromPathPropName = (String) in.readObject();
            toPathPropName = (String) in.readObject();
            conceptPropName = (String) in.readObject();
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
            I_Path fromPath = tf.getPath(AceTaskUtil.getConceptFromObject(process.readProperty(fromPathPropName))
                .getUids());
            I_Path toPath = tf.getPath(AceTaskUtil.getConceptFromObject(process.readProperty(toPathPropName)).getUids());
            I_GetConceptData concept = AceTaskUtil.getConceptFromObject(process.readProperty(conceptPropName));
            Set<I_Position> fromSet = new HashSet<I_Position>();
            fromSet.add(tf.newPosition(fromPath, Integer.MAX_VALUE));
            copyFromPathToPath(tf, toPath, concept, fromSet, null, null);

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

    public static void copyFromPathToPath(I_TermFactory tf, I_Path toPath, I_GetConceptData concept,
            Set<I_Position> fromSet, I_GetConceptData conceptStatus, I_GetConceptData copyStatus) throws IOException,
            TerminologyException {
        copyId(toPath, concept.getId(), copyStatus);
        for (I_ConceptAttributeTuple t : concept.getConceptAttributeTuples(null, fromSet)) {
            I_ConceptAttributePart newPart = t.duplicatePart();
            newPart.setPathId(toPath.getConceptId());
            newPart.setVersion(Integer.MAX_VALUE);
            if (conceptStatus != null) {
                newPart.setConceptStatus(conceptStatus.getConceptId());
            }
            t.getConVersioned().addVersion(newPart);
        }
        I_IntSet idsToCopy = tf.newIntSet();

        for (I_DescriptionTuple t : concept.getDescriptionTuples(null, null, fromSet)) {
            idsToCopy.add(t.getDescId());
            I_DescriptionPart newPart = t.duplicatePart();
            newPart.setPathId(toPath.getConceptId());
            newPart.setVersion(Integer.MAX_VALUE);
            if (copyStatus != null) {
                newPart.setStatusId(copyStatus.getConceptId());
            }
            t.getDescVersioned().addVersion(newPart);
        }
        for (I_RelTuple t : concept.getSourceRelTuples(null, null, fromSet, false)) {
            idsToCopy.add(t.getRelId());
            I_RelPart newPart = t.duplicatePart();
            newPart.setPathId(toPath.getConceptId());
            newPart.setVersion(Integer.MAX_VALUE);
            if (copyStatus != null) {
                newPart.setStatusId(copyStatus.getConceptId());
            }
            t.getRelVersioned().addVersion(newPart);
        }
        for (I_ImageTuple t : concept.getImageTuples(null, null, fromSet)) {
            idsToCopy.add(t.getImageId());
            I_ImagePart newPart = t.duplicatePart();
            newPart.setPathId(toPath.getConceptId());
            newPart.setVersion(Integer.MAX_VALUE);
            if (copyStatus != null) {
                newPart.setStatusId(copyStatus.getConceptId());
            }
            t.getVersioned().addVersion(newPart);
        }
        for (int id : idsToCopy.getSetValues()) {
            copyId(toPath, tf.getId(id), copyStatus);
        }
    }

    public static void copyId(I_Path toPath, I_IdVersioned id, I_GetConceptData copyStatus) {
        for (I_IdTuple t : id.getTuples()) {
            I_IdPart newPart = t.duplicate();
            newPart.setPathId(toPath.getConceptId());
            newPart.setVersion(Integer.MAX_VALUE);
            if (copyStatus != null) {
                newPart.setStatusId(copyStatus.getConceptId());
            }
            t.getIdVersioned().addVersion(newPart);
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

    public String getFromPathPropName() {
        return fromPathPropName;
    }

    public void setFromPathPropName(String fromPathPropName) {
        this.fromPathPropName = fromPathPropName;
    }

    public String getToPathPropName() {
        return toPathPropName;
    }

    public void setToPathPropName(String toPathPropName) {
        this.toPathPropName = toPathPropName;
    }

}
