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

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.swing.JOptionPane;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import net.jini.core.entry.Entry;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.queue.QueueServer;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile", type = BeanType.TASK_BEAN) })
public class CreateUserPathAndQueues extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 3;

    private String commitProfilePropName = ProcessAttachmentKeys.COMMIT_PROFILE.getAttachmentKey();
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String positionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();
    private String errorsAndWarningsPropName = ProcessAttachmentKeys.ERRORS_AND_WARNINGS.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(commitProfilePropName);
        out.writeObject(positionSetPropName);
        out.writeObject(errorsAndWarningsPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            profilePropName = (String) in.readObject();
            if (objDataVersion >= 2) {
                commitProfilePropName = (String) in.readObject();
                positionSetPropName = (String) in.readObject();
            } else {
                profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
                positionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();
            }
            if (objDataVersion >= 3) {
                errorsAndWarningsPropName = (String) in.readObject();
            } else {
                errorsAndWarningsPropName = ProcessAttachmentKeys.ERRORS_AND_WARNINGS.getAttachmentKey();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) process.getProperty(profilePropName);
            I_ConfigAceFrame commitConfig = (I_ConfigAceFrame) process.getProperty(commitProfilePropName);
            Set<PositionBI> positionSet = (Set<PositionBI>) process.getProperty(positionSetPropName);

            String userDirStr = "profiles" + File.separator + config.getUsername();
            File userDir = new File(userDirStr);
            File userQueueRoot = new File(userDir, "queues");
            userQueueRoot.mkdirs();

            // Create new concept for user...
            createUser(config, commitConfig);

            // Create new path for user...
            createUserPath(config, commitConfig, positionSet);

            createClassifierPath(config, commitConfig, positionSet);

            List<AlertToDataConstraintFailure> errorsAndWarnings = Terms.get()
                .getCommitErrorsAndWarnings();
            process.setProperty(errorsAndWarningsPropName, errorsAndWarnings);
            if (errorsAndWarnings.size() > 0) {
                Terms.get().cancel();
                return Condition.FALSE;
            }
            try {
                Terms.get().commit();
            } catch (Exception e) {
                throw new TaskFailedException();
            }

            // Set roots
            for (int rootNid : commitConfig.getRoots().getSetValues()) {
                config.getRoots().add(rootNid);
            }
            // Set src rels
            for (int relTypeNid : commitConfig.getSourceRelTypes().getSetValues()) {
                config.getSourceRelTypes().add(relTypeNid);
            }

            // Set dest rels
            for (int relTypeNid : commitConfig.getDestRelTypes().getSetValues()) {
                config.getDestRelTypes().add(relTypeNid);
            }

            // Set path colors
            for (Integer pathNid : commitConfig.getPathColorMap().keySet()) {
                config.setColorForPath(pathNid, commitConfig.getColorForPath(pathNid));
            }

            // clear the user's path color
            if (commitConfig.getDbConfig().getUserPath() != null) {
                Color userColor = config.getPathColorMap().remove(
                    commitConfig.getDbConfig().getUserPath().getConceptNid());
                config.setColorForPath(config.getDbConfig().getUserPath().getConceptNid(), userColor);
            }

            // Create inbox
            createInbox(config, config.getUsername() + ".inbox", userQueueRoot, config.getUsername() + ".inbox");
            // Create todo box
            createInbox(config, config.getUsername() + ".todo", userQueueRoot, config.getUsername() + ".inbox");
            // Create outbox box
            createOutbox(config, config.getUsername() + ".outbox", userQueueRoot, config.getUsername() + ".inbox");

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        return Condition.TRUE;
    }

    String[] QueueTypes = new String[] { "aging", "archival", "compute", "inbox", "launcher", "outbox" };

    private void createInbox(I_ConfigAceFrame config, String inboxName, File userQueueRoot, String nodeInboxAddress) {
        config.getQueueAddressesToShow().add(inboxName);
        createQueue(config, "inbox", inboxName, userQueueRoot, nodeInboxAddress);
    }

    private void createOutbox(I_ConfigAceFrame config, String outboxName, File userQueueRoot, String nodeInboxAddress) {
        config.getQueueAddressesToShow().add(outboxName);
        createQueue(config, "outbox", outboxName, userQueueRoot, nodeInboxAddress);
    }

    private void createQueue(I_ConfigAceFrame config, String queueType, String queueName, File userQueueRoot,
            String nodeInboxAddress) {

        try {

            if (userQueueRoot.exists() == false) {
                userQueueRoot.mkdirs();
            }

            File queueDirectory = new File(userQueueRoot, queueName);

            queueDirectory.mkdirs();

            Map<String, String> substutionMap = new TreeMap<String, String>();
            substutionMap.put("**queueName**", queueDirectory.getName());
            substutionMap.put("**inboxAddress**", queueDirectory.getName());
            substutionMap.put("**directory**", FileIO.getRelativePath(queueDirectory).replace('\\', '/'));
            substutionMap.put("**nodeInboxAddress**", nodeInboxAddress);

            String fileName = "template.queue.config";
            if (queueType.equals("aging")) {
                fileName = "template.queueAging.config";
            } else if (queueType.equals("archival")) {
                fileName = "template.queueArchival.config";
            } else if (queueType.equals("compute")) {
                fileName = "template.queueCompute.config";
            } else if (queueType.equals("inbox")) {
                substutionMap.put("**mailPop3Host**", "**mailPop3Host**");
                substutionMap.put("**mailUsername**", "**mailUsername**");
                fileName = "template.queueInbox.config";
            } else if (queueType.equals("launcher")) {
                fileName = "template.queueLauncher.config";
            } else if (queueType.equals("outbox")) {
                substutionMap.put("//**allGroups**mailHost", "//**allGroups**mailHost");
                substutionMap.put("//**outbox**mailHost", "//**outbox**mailHost");
                substutionMap.put("**mailHost**", "**mailHost**");
                fileName = "template.queueOutbox.config";
            }

            File queueConfigTemplate = new File("config", fileName);
            String configTemplateString = FileIO.readerToString(new FileReader(queueConfigTemplate));

            for (String key : substutionMap.keySet()) {
                configTemplateString = configTemplateString.replace(key, substutionMap.get(key));
            }

            File newQueueConfig = new File(queueDirectory, "queue.config");
            FileWriter fw = new FileWriter(newQueueConfig);
            fw.write(configTemplateString);
            fw.close();

            config.getDbConfig().getQueues().add(FileIO.getRelativePath(newQueueConfig));
            Configuration queueConfig = ConfigurationProvider.getInstance(new String[] { newQueueConfig.getAbsolutePath() });
            Entry[] entries = (Entry[]) queueConfig.getEntry("org.dwfa.queue.QueueServer", "entries", Entry[].class,
                new Entry[] {});
            for (Entry entry : entries) {
                if (ElectronicAddress.class.isAssignableFrom(entry.getClass())) {
                    ElectronicAddress ea = (ElectronicAddress) entry;
                    config.getQueueAddressesToShow().add(ea.address);
                    break;
                }
            }
            if (QueueServer.started(newQueueConfig)) {
                AceLog.getAppLog().info("Queue already started: " + newQueueConfig.toURI().toURL().toExternalForm());
            } else {
                new QueueServer(new String[] { newQueueConfig.getCanonicalPath() }, null);
            }

        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    private void createUser(I_ConfigAceFrame config, I_ConfigAceFrame commitConfig) throws TaskFailedException,
            TerminologyException, IOException {
        AceLog.getAppLog().info("Create new path for user: " + config.getDbConfig().getFullName());
        if (config.getDbConfig().getFullName() == null || config.getDbConfig().getFullName().length() == 0) {
            JOptionPane.showMessageDialog(config.getWorkflowPanel().getTopLevelAncestor(), "Full name cannot be empty.");
            throw new TaskFailedException();
        }
        I_TermFactory tf = Terms.get();
        // Needs a concept record...
        I_GetConceptData userConcept = tf.newConcept(UUID.randomUUID(), false, commitConfig);

        // Needs a description record...
        tf.newDescription(UUID.randomUUID(), userConcept, "en", config.getDbConfig().getFullName(),
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), commitConfig);
        tf.newDescription(UUID.randomUUID(), userConcept, "en", config.getUsername(),
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), commitConfig);
        tf.newDescription(UUID.randomUUID(), userConcept, "en", config.getUsername() + ".inbox",
            ArchitectonicAuxiliary.Concept.USER_INBOX.localize(), commitConfig);

        // Needs a relationship record...
        tf.newRelationship(UUID.randomUUID(), userConcept,
            tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
            tf.getConcept(ArchitectonicAuxiliary.Concept.USER.getUids()),
            tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()),
            tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
            tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()), 0, commitConfig);
        config.getDbConfig().setUserConcept(userConcept);
    }

    private void createUserPath(I_ConfigAceFrame config, I_ConfigAceFrame commitConfig, Set<PositionBI> positionSet)
            throws Exception {
        PathBI userPath = createNewPath(config, commitConfig, positionSet, " user path");
        config.addEditingPath(userPath);
        I_GetConceptData userPathConcept = Terms.get().getConcept(userPath.getConceptNid());
        config.setClassifierInputPath(userPathConcept);
        config.getViewPositionSet().add(Terms.get().newPosition(userPath, Integer.MAX_VALUE));
        config.getDbConfig().setUserPath(userPathConcept);
    }

    private void createClassifierPath(I_ConfigAceFrame config, I_ConfigAceFrame commitConfig,
            Set<PositionBI> positionSet) throws Exception {
        PathBI classifierPath = createNewPath(config, commitConfig, positionSet, " classifier path");
        config.setClassifierOutputPath(Terms.get().getConcept(classifierPath.getConceptNid()));
    }

    private PathBI createNewPath(I_ConfigAceFrame config, I_ConfigAceFrame commitConfig, Set<PositionBI> positionSet,
            String suffix) throws TaskFailedException, TerminologyException, IOException {
        AceLog.getAppLog().info("Create new path for user: " + config.getDbConfig().getFullName());
        if (config.getDbConfig().getFullName() == null || config.getDbConfig().getFullName().length() == 0) {
            JOptionPane.showMessageDialog(config.getWorkflowPanel().getTopLevelAncestor(), "Full name cannot be empty.");
            throw new TaskFailedException();
        }
        AceLog.getAppLog().info(positionSet.toString());
        if (positionSet.size() == 0) {
            throw new TaskFailedException("You must select at least one origin for path.");
        }
        UUID newPathUid = UUID.randomUUID();

        I_TermFactory tf = Terms.get();

        // Needs a concept record...
        I_GetConceptData pathConcept = tf.newConcept(newPathUid, false, commitConfig);

        // Needs a description record...
        tf.newDescription(UUID.randomUUID(), pathConcept, "en", config.getDbConfig().getFullName() + suffix,
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), commitConfig);
        tf.newDescription(UUID.randomUUID(), pathConcept, "en", config.getUsername() + suffix,
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), commitConfig);

        // Needs a relationship record...
        tf.newRelationship(UUID.randomUUID(), pathConcept,
            tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
            tf.getConcept(ArchitectonicAuxiliary.Concept.DEVELOPMENT.getUids()),
            tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()),
            tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
            tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()), 0, commitConfig);

        return tf.newPath(positionSet, pathConcept);
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONDITIONAL_TEST_CONDITIONS;
    }

    public String getPositionSetPropName() {
        return positionSetPropName;
    }

    public void setPositionSetPropName(String positionSetPropName) {
        this.positionSetPropName = positionSetPropName;
    }

    public String getCommitProfilePropName() {
        return commitProfilePropName;
    }

    public void setCommitProfilePropName(String commitProfilePropName) {
        this.commitProfilePropName = commitProfilePropName;
    }

    public String getErrorsAndWarningsPropName() {
        return errorsAndWarningsPropName;
    }

    public void setErrorsAndWarningsPropName(String errorsAndWarningsPropName) {
        this.errorsAndWarningsPropName = errorsAndWarningsPropName;
    }

}
