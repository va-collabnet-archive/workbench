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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
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
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/copy", type = BeanType.TASK_BEAN) })
public class CopyPathToPathWithStatusMaps extends AbstractTask {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String toPathPropName = ProcessAttachmentKeys.TO_PATH_CONCEPT.getAttachmentKey();
    private String conceptPropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();
    private String conceptStatusMapPropName = ProcessAttachmentKeys.CON_STATUS_MAP.getAttachmentKey();
    private String elementStatusMapPropName = ProcessAttachmentKeys.ELEM_STATUS_MAP.getAttachmentKey();
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(toPathPropName);
        out.writeObject(conceptPropName);
        out.writeObject(conceptStatusMapPropName);
        out.writeObject(elementStatusMapPropName);
        out.writeObject(profilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            toPathPropName = (String) in.readObject();
            conceptPropName = (String) in.readObject();
            conceptStatusMapPropName = (String) in.readObject();
            elementStatusMapPropName = (String) in.readObject();
            profilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    @SuppressWarnings("unchecked")
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_TermFactory tf = LocalVersionedTerminology.get();
            I_Path toPath = tf.getPath(AceTaskUtil.getConceptFromObject(process.readProperty(toPathPropName)).getUids());
            I_GetConceptData concept = AceTaskUtil.getConceptFromObject(process.readProperty(conceptPropName));
            Map<TermEntry, TermEntry> conceptStatusMap = (Map<TermEntry, TermEntry>) process.readProperty(conceptStatusMapPropName);
            Map<Integer, Integer> conceptStatusNidMap = makeNidMap(conceptStatusMap);
            Map<TermEntry, TermEntry> elementStatusMap = (Map<TermEntry, TermEntry>) process.readProperty(elementStatusMapPropName);
            Map<Integer, Integer> elementStatusNidMap = makeNidMap(elementStatusMap);
            I_ConfigAceFrame profile = (I_ConfigAceFrame) process.readProperty(profilePropName);
            copyFromPathToPath(tf, toPath, concept, profile.getViewPositionSet(), conceptStatusNidMap,
                elementStatusNidMap);
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

    private Map<Integer, Integer> makeNidMap(Map<TermEntry, TermEntry> termEntryMap) throws Exception {
        if (termEntryMap == null) {
            return null;
        }
        Map<Integer, Integer> conceptStatusNidMap = new HashMap<Integer, Integer>(termEntryMap.size());
        for (TermEntry key : termEntryMap.keySet()) {
            int keyNid = key.getLocalConcept().getNid();
            int valueNid = termEntryMap.get(key).getLocalConcept().getNid();
            conceptStatusNidMap.put(keyNid, valueNid);
        }
        return conceptStatusNidMap;
    }

    public static void copyFromPathToPath(I_TermFactory tf, I_Path toPath, I_GetConceptData concept,
            Set<I_Position> fromSet, Map<Integer, Integer> conceptStatusNidMap,
            Map<Integer, Integer> elementStatusNidMap) throws IOException, TerminologyException {
        copyId(toPath, concept.getId(), elementStatusNidMap);
        for (I_ConceptAttributeTuple t : concept.getConceptAttributeTuples(null, fromSet)) {
            I_ConceptAttributePart newPart = t.duplicatePart();
            newPart.setPathId(toPath.getConceptId());
            newPart.setVersion(Integer.MAX_VALUE);
            if (conceptStatusNidMap != null) {
                if (conceptStatusNidMap.get(newPart.getConceptStatus()) != null) {
                    newPart.setConceptStatus(conceptStatusNidMap.get(newPart.getConceptStatus()));
                }
            }
            t.getConVersioned().addVersion(newPart);
        }
        I_IntSet idsToCopy = tf.newIntSet();

        for (I_DescriptionTuple t : concept.getDescriptionTuples(null, null, fromSet)) {
            idsToCopy.add(t.getDescId());
            I_DescriptionPart newPart = t.duplicatePart();
            newPart.setPathId(toPath.getConceptId());
            newPart.setVersion(Integer.MAX_VALUE);
            if (elementStatusNidMap != null) {
                if (elementStatusNidMap.get(newPart.getStatusId()) != null) {
                    newPart.setStatusId(elementStatusNidMap.get(newPart.getStatusId()));
                }
            }
            t.getDescVersioned().addVersion(newPart);
        }
        for (I_RelTuple t : concept.getSourceRelTuples(null, null, fromSet, false)) {
            idsToCopy.add(t.getRelId());
            I_RelPart newPart = t.duplicatePart();
            newPart.setPathId(toPath.getConceptId());
            newPart.setVersion(Integer.MAX_VALUE);
            if (elementStatusNidMap != null) {
                if (elementStatusNidMap.get(newPart.getStatusId()) != null) {
                    newPart.setStatusId(elementStatusNidMap.get(newPart.getStatusId()));
                }
            }
            t.getRelVersioned().addVersion(newPart);
        }
        for (I_ImageTuple t : concept.getImageTuples(null, null, fromSet)) {
            idsToCopy.add(t.getImageId());
            I_ImagePart newPart = t.duplicatePart();
            newPart.setPathId(toPath.getConceptId());
            newPart.setVersion(Integer.MAX_VALUE);
            if (elementStatusNidMap != null) {
                if (elementStatusNidMap.get(newPart.getStatusId()) != null) {
                    newPart.setStatusId(elementStatusNidMap.get(newPart.getStatusId()));
                }
            }
            t.getVersioned().addVersion(newPart);
        }
        for (int id : idsToCopy.getSetValues()) {
            copyId(toPath, tf.getId(id), elementStatusNidMap);
        }
    }

    public static void copyId(I_Path toPath, I_IdVersioned id, Map<Integer, Integer> statusMap) {
        for (I_IdTuple t : id.getTuples()) {
            I_IdPart newPart = t.duplicatePart();
            newPart.setPathId(toPath.getConceptId());
            newPart.setVersion(Integer.MAX_VALUE);
            if (statusMap != null) {
                if (statusMap.get(newPart.getIdStatus()) != null) {
                    newPart.setIdStatus(statusMap.get(newPart.getIdStatus()));
                }
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

    public String getToPathPropName() {
        return toPathPropName;
    }

    public void setToPathPropName(String toPathPropName) {
        this.toPathPropName = toPathPropName;
    }

    public String getConceptStatusMapPropName() {
        return conceptStatusMapPropName;
    }

    public void setConceptStatusMapPropName(String statusPropName) {
        this.conceptStatusMapPropName = statusPropName;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getElementStatusMapPropName() {
        return elementStatusMapPropName;
    }

    public void setElementStatusMapPropName(String elementStatusMapPropName) {
        this.elementStatusMapPropName = elementStatusMapPropName;
    }

}
