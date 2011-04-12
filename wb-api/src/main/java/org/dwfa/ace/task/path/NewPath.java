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
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.status.TupleListUtil;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.tk.api.PathBI;

/**
 * @author Ming Zhang
 * 
 * @created 15/01/2008
 */
@AllowDataCheckSuppression
@BeanList(specs = { @Spec(directory = "tasks/ide/path", type = BeanType.TASK_BEAN) })
public class NewPath extends AbstractTask {
    /*
     * This task has the same function with the "new path" in the preference
     * panel
     */

    private static final long serialVersionUID = 1L;

    private static final int DATA_VERSION = 3;

    // parent in the hierarchy, it could be any concept
    private TermEntry parentPathTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.DEVELOPMENT.getUids());

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private String newConceptPropName = ProcessAttachmentKeys.NEW_CONCEPT.getAttachmentKey();

    private String PathDescription = "Use Attachement";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(DATA_VERSION);
        out.writeObject(profilePropName);
        out.writeObject(parentPathTermEntry);
        out.writeObject(PathDescription);
        out.writeObject(newConceptPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion > DATA_VERSION) {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

        if (objDataVersion < 3) {
            in.readObject();
            in.readObject();
        }

        profilePropName = (String) in.readObject();
        parentPathTermEntry = (TermEntry) in.readObject();
        PathDescription = (String) in.readObject();

        if (objDataVersion >= 2) {
            newConceptPropName = (String) in.readObject();
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            String descriptionForNewPath = (String) process.getProperty(PathDescription);
            I_TermFactory tf = Terms.get();
            I_ConfigAceFrame activeProfile = tf.getActiveAceFrameConfig();
            Set<PathBI> savedEditingPaths = new HashSet<PathBI>(activeProfile.getEditingPathSet());

            // create parent of path
            I_GetConceptData newPathConcept = createComponents(descriptionForNewPath, tf, activeProfile,
                parentPathTermEntry);

            PathBI editPath = tf.newPath(null, newPathConcept);

            if (!isBlank(profilePropName)) {
                I_ConfigAceFrame profile = (I_ConfigAceFrame) process.getProperty(profilePropName);
                if (profile == null) {
                    profile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
                }
                profile.getEditingPathSet().clear();
                profile.addEditingPath(editPath);
                profile.getViewPositionSet().clear();
                profile.addViewPosition(tf.newPosition(editPath, Long.MAX_VALUE));
            }

            tf.commit();

            // pass on the new path concept for subsequent tasks that may wish
            // to use it
            if (!isBlank(newConceptPropName)) {
                process.setProperty(newConceptPropName, tf.getConcept(editPath.getConceptNid()));
            }

            if (!isBlank(profilePropName)) {
                activeProfile.getEditingPathSet().clear();
                activeProfile.getEditingPathSet().addAll(savedEditingPaths);
            }

            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private boolean isBlank(String value) {
        return ((value == null) || (value.trim().length() == 0));
    }

    protected static I_GetConceptData createComponents(String description, I_TermFactory tf,
            I_ConfigAceFrame activeProfile, TermEntry parent) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, TerminologyException, IOException {

        UUID type5ConceptId = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, description);

        List<I_AmTuple> newTuples = new ArrayList<I_AmTuple>();

        I_GetConceptData newPathConcept = tf.newConcept(type5ConceptId, false, activeProfile);

        I_GetConceptData statusConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

        newTuples.addAll(newPathConcept.getConceptAttributes().getTuples());

        I_DescriptionVersioned idvFsn = tf.newDescription(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
            parent.ids[0] + description), newPathConcept, "en", description,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), activeProfile);

        I_DescriptionVersioned idvPt = tf.newDescription(UUID.randomUUID(), newPathConcept, "en", description,
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), activeProfile);

        newTuples.addAll(idvFsn.getTuples());
        newTuples.addAll(idvPt.getTuples());

        I_GetConceptData relType = tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
        I_GetConceptData relDestination = tf.getConcept(parent.ids);
        I_GetConceptData relCharacteristic = tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
        I_GetConceptData relRefinability = tf.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());

        UUID relId = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, parent.ids[0] + description + "relid");
        tf.newRelationship(relId, newPathConcept, relType, relDestination, relCharacteristic, relRefinability,
            statusConcept, 0, activeProfile);

        TupleListUtil.setStatus(statusConcept, newTuples);

        return newPathConcept;
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public TermEntry getParentPathTermEntry() {
        return parentPathTermEntry;
    }

    public void setParentPathTermEntry(TermEntry parentPath) {
        this.parentPathTermEntry = parentPath;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getPathDescription() {
        return PathDescription;
    }

    public void setPathDescription(String pathDescription) {
        PathDescription = pathDescription;
    }

    public String getNewConceptPropName() {
        return newConceptPropName;
    }

    public void setNewConceptPropName(String newConceptPropName) {
        this.newConceptPropName = newConceptPropName;
    }

}
