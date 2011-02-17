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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map.Entry;

import org.dwfa.ace.api.BundleType;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.svn.CommitAllSvnEntries;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.io.FileIO;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile", type = BeanType.TASK_BEAN) })
public class SaveNewProfile extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
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
            I_ConfigAceFrame profileToSave = (I_ConfigAceFrame) process.getProperty(profilePropName);
            if (profileToSave == null) {
                profileToSave = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            }

            profileToSave.setFrameName(profileToSave.getUsername() + " editor");

            I_ConfigAceFrame currentProfile =
                    (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            I_ConfigAceDb currentDbProfile = currentProfile.getDbConfig();

            String userDirStr = "profiles" + File.separator + profileToSave.getUsername();
            File userDir = new File(userDirStr);
            File changeSetRoot = new File(userDir, "changesets");
            changeSetRoot.mkdirs();
            File profileFile = new File(userDir, profileToSave.getUsername() + ".ace");
            I_ConfigAceDb newDbProfile = profileToSave.getDbConfig();
            newDbProfile.getAceFrames().clear();
            newDbProfile.getAceFrames().add(profileToSave);
            newDbProfile.setDbFolder(currentDbProfile.getDbFolder());
            newDbProfile.setProfileFile(profileFile);
            newDbProfile.setUsername(profileToSave.getUsername());

            for (String queueToShow : profileToSave.getQueueAddressesToShow()) {
                if (queueToShow.contains("-collabnet")) {
                    newDbProfile.getQueues().add("queues" + File.separator + queueToShow + File.separator + "queue.config");
                }
            }

            // write new profile to disk
            FileOutputStream fos = new FileOutputStream(profileFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(newDbProfile);
            oos.close();

            File startupFolder = new File(userDir, "startup");
            startupFolder.mkdirs();

            File shutdownFolder = new File(userDir, "shutdown");
            shutdownFolder.mkdirs();

            BundleType bundleType = currentProfile.getBundleType();
            String workingCopyStr =
                    FileIO.getNormalizedRelativePath(currentProfile.getDbConfig().getProfileFile().getParentFile());
            switch (bundleType) {
            case STAND_ALONE:
                break;
            default:
                SubversionData creatorSvd = new SubversionData(null, workingCopyStr);
                currentProfile.svnCompleteRepoInfo(creatorSvd);
                String sequenceToFind = "src/main/profiles/";
                int sequenceLocation = -1;
                if (creatorSvd.getRepositoryUrlStr() == null) {
                    File f = new File(workingCopyStr).getParentFile();
                    creatorSvd = new SubversionData(null, f.getAbsolutePath());
                    currentProfile.svnCompleteRepoInfo(creatorSvd);
                    if (creatorSvd.getRepositoryUrlStr() == null) {
                        AceLog.getAppLog().alertAndLogException(new Exception("No svn url for: " + f.toString()));
                        break;
                    }
                }
                sequenceLocation = creatorSvd.getRepositoryUrlStr().indexOf(sequenceToFind);
                if (sequenceLocation == -1) {
                    sequenceToFind = "src/main/profiles";
                    sequenceLocation = creatorSvd.getRepositoryUrlStr().indexOf(sequenceToFind);
                }
                if (sequenceLocation == -1) {
                    sequenceToFind = "src/main/resources/profiles/";
                    sequenceLocation = creatorSvd.getRepositoryUrlStr().indexOf(sequenceToFind);
                }
                int sequenceEnd = sequenceLocation + sequenceToFind.length();
                String profileDirRepoUrl = creatorSvd.getRepositoryUrlStr().substring(0, sequenceEnd);
                String repositoryUrlStr = profileDirRepoUrl + profileToSave.getUsername();
                String workingCopyStrForNewUser = "profiles" + File.separator + profileToSave.getUsername();

                SubversionData svd = new SubversionData(repositoryUrlStr, workingCopyStrForNewUser);
                currentProfile.svnImport(svd);
                worker.getLogger().info("Recursive delete for: " + newDbProfile.getProfileFile().getParent());
                FileIO.recursiveDelete(newDbProfile.getProfileFile().getParentFile());

                worker.getLogger().info("Starting commits for: " + currentProfile.getSubversionMap().keySet());

                for (Entry<String, SubversionData> entry : currentProfile.getSubversionMap().entrySet()) {

                    if (!entry.getKey().replace('\\', '/').equalsIgnoreCase(I_ConfigAceDb.MUTABLE_DB_LOC)) {
                        worker.getLogger().info("commit: " + entry);
                        CommitAllSvnEntries.commit(currentProfile, entry.getValue(), entry.getKey());
                    } else {
                        worker.getLogger().info("suppressed commit for: " + entry);
                    }
                }
            }

            return Condition.CONTINUE;
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

    @Override
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
}
