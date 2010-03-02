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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.status.SetStatusUtil;
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
import org.dwfa.util.id.Type5UuidFactory;

@BeanList(specs = { @Spec(directory = "tasks/ide/path", type = BeanType.TASK_BEAN) })
public class NewEditPathForUserFromProperty extends AbstractTask {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String parentPathPropertyName = ProcessAttachmentKeys.PARENT_PATH.getAttachmentKey();

    private String userPropName = ProcessAttachmentKeys.USERNAME.getAttachmentKey();

    private String originTime = "latest";

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(parentPathPropertyName);
        out.writeObject(originTime);
        out.writeObject(profilePropName);
        out.writeObject(userPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            parentPathPropertyName = (String) in.readObject();
            originTime = (String) in.readObject();
            profilePropName = (String) in.readObject();
            userPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            String username = (String) process.getProperty(userPropName);
            I_TermFactory tf = Terms.get();
            I_ConfigAceFrame activeProfile = tf.getActiveAceFrameConfig();
            Set<I_Path> savedEditingPaths = new HashSet<I_Path>(activeProfile.getEditingPathSet());
            try {
                activeProfile.getEditingPathSet().clear();
                activeProfile.getEditingPathSet().add(
                    tf.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()));
                I_GetConceptData parentPathConcept = AceTaskUtil.getConceptFromProperty(process, parentPathPropertyName);
                TermEntry parentPathTermEntry = new TermEntry(parentPathConcept.getUids());

                I_GetConceptData newPathConcept = createComponents(username, tf, activeProfile, parentPathTermEntry);

                tf.commit();

                Set<I_Position> origins = new HashSet<I_Position>();

                I_Path parentPath = tf.getPath(parentPathTermEntry.ids);
                origins.add(tf.newPosition(parentPath, tf.convertToThinVersion(originTime)));

                I_Path editPath = tf.newPath(origins, newPathConcept);
                I_ConfigAceFrame profile = (I_ConfigAceFrame) process.getProperty(profilePropName);
                if (profile == null) {
                    profile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
                }
                profile.getEditingPathSet().clear();
                profile.addEditingPath(editPath);
                profile.getViewPositionSet().clear();
                profile.addViewPosition(tf.newPosition(editPath, Integer.MAX_VALUE));
                tf.commit();

            } catch (Exception e) {
                throw new TaskFailedException(e);
            }
            activeProfile.getEditingPathSet().clear();
            activeProfile.getEditingPathSet().addAll(savedEditingPaths);
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    protected static I_GetConceptData createComponents(String username, I_TermFactory tf,
            I_ConfigAceFrame activeProfile, TermEntry parentPathTermEntry) throws NoSuchAlgorithmException,
            UnsupportedEncodingException, TerminologyException, IOException {
        String fsDescription = username + " development editing path";

        UUID type5ConceptId = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, fsDescription);

        I_GetConceptData newPathConcept = tf.newConcept(type5ConceptId, false, activeProfile);

        I_GetConceptData statusConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.INTERNAL_USE_ONLY.getUids());

        SetStatusUtil.setStatusOfConceptInfo(statusConcept, newPathConcept.getConceptAttributes().getTuples());

        I_DescriptionVersioned idv = tf.newDescription(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
            parentPathTermEntry.ids[0] + fsDescription), newPathConcept, "en", fsDescription,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), activeProfile);
        SetStatusUtil.setStatusOfDescriptionInfo(statusConcept, idv.getTuples());

        String prefDesc = username + " dev path";

        idv = tf.newDescription(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, parentPathTermEntry.ids[0]
            + prefDesc), newPathConcept, "en", prefDesc,
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), activeProfile);
        SetStatusUtil.setStatusOfDescriptionInfo(statusConcept, idv.getTuples());

        idv = tf.newDescription(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, parentPathTermEntry.ids[0]
            + username), newPathConcept, "en", username, ArchitectonicAuxiliary.Concept.USER_NAME.localize(),
            activeProfile);
        SetStatusUtil.setStatusOfDescriptionInfo(statusConcept, idv.getTuples());

        idv = tf.newDescription(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, parentPathTermEntry.ids[0]
            + username + "inbox"), newPathConcept, "en", username,
            ArchitectonicAuxiliary.Concept.USER_INBOX.localize(), activeProfile);
        SetStatusUtil.setStatusOfDescriptionInfo(statusConcept, idv.getTuples());

        I_GetConceptData relType = tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
        I_GetConceptData relDestination = tf.getConcept(parentPathTermEntry.ids);
        I_GetConceptData relCharacteristic = tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
        I_GetConceptData relRefinability = tf.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
        // I_GetConceptData relStatus =
        // tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

        UUID relId = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, parentPathTermEntry.ids[0] + username
            + "relid");
        tf.newRelationship(relId, newPathConcept, relType, relDestination, relCharacteristic, relRefinability,
            statusConcept, 0, activeProfile);
        return newPathConcept;
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getUserPropName() {
        return userPropName;
    }

    public void setUserPropName(String profilePropName) {
        this.userPropName = profilePropName;
    }

    public String getOriginTime() {
        return originTime;
    }

    public void setOriginTime(String originTime) {
        this.originTime = originTime;
    }

    public String getParentPathPropertyName() {
        return parentPathPropertyName;
    }

    public void setParentPathPropertyName(String parentPath) {
        this.parentPathPropertyName = parentPath;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

}
