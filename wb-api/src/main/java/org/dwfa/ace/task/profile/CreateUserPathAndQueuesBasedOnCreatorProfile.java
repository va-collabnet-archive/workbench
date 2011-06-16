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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import net.jini.core.entry.Entry;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
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
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile", type = BeanType.TASK_BEAN) })
public class CreateUserPathAndQueuesBasedOnCreatorProfile extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String creatorProfilePropName = ProcessAttachmentKeys.COMMIT_PROFILE.getAttachmentKey();
    private String newProfilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String errorsAndWarningsPropName = ProcessAttachmentKeys.ERRORS_AND_WARNINGS.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(newProfilePropName);
        out.writeObject(creatorProfilePropName);
        out.writeObject(errorsAndWarningsPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            newProfilePropName = (String) in.readObject();
            creatorProfilePropName = (String) in.readObject();
            errorsAndWarningsPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public String getProfilePropName() {
        return newProfilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.newProfilePropName = profilePropName;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    @SuppressWarnings("unchecked")
	public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        I_ConfigAceFrame newConfig;
        try {
            newConfig = (I_ConfigAceFrame) process.getProperty(newProfilePropName);
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new TaskFailedException(e2);
        }
        try {

            newConfig.getViewPositionSet().clear();
            newConfig.getEditingPathSet().clear();
            I_ConfigAceFrame creatorConfig = (I_ConfigAceFrame) process.getProperty(creatorProfilePropName);
            newConfig.setClassificationRoleRoot(creatorConfig.getClassificationRoleRoot());
            newConfig.setClassificationRoot(creatorConfig.getClassificationRoot());
            newConfig.setClassifierInputPath(creatorConfig.getClassifierInputPath());
            newConfig.setClassifierIsaType(creatorConfig.getClassifierIsaType());
            newConfig.setClassifierOutputPath(creatorConfig.getClassifierOutputPath());
            
            I_TermFactory tf = Terms.get();

            String userDirStr = "profiles" + File.separator + newConfig.getUsername();
            File userDir = new File(userDirStr);
            File userQueueRoot = new File("queues", newConfig.getUsername());
            userQueueRoot.mkdirs();
            EditOnRootPath rootPathProfile = new EditOnRootPath(creatorConfig);

            // Create new concept for user...
            createUser(newConfig, rootPathProfile);

            // Create new paths for user...
            createDevPath(newConfig, rootPathProfile);
            if (creatorConfig.getPromotionPathSet().size() > 1) {
                throw new TaskFailedException("This task only supports a single promotion path...\nFound: "
                    + creatorConfig.getPromotionPathSet().size());
            	
            }
            if (creatorConfig.getEditingPathSet().size() > 1) {
                throw new TaskFailedException("This task only supports a single editing path...\nFound: "
                    + creatorConfig.getPromotionPathSet().size());
            }

            newConfig.getPromotionPathSet().addAll(creatorConfig.getPromotionPathSet());
 
            List<AlertToDataConstraintFailure> errorsAndWarnings = Terms.get().getCommitErrorsAndWarnings();
            process.setProperty(errorsAndWarningsPropName, errorsAndWarnings);
            if (errorsAndWarnings.size() > 0) {
                AceLog.getAppLog().warning(errorsAndWarnings.toString());
            	Terms.get().cancel();
                return Condition.FALSE;
            }
            
            File changeSetRoot = new File(userDir, "changesets");
            changeSetRoot.mkdirs();
            I_ConfigAceDb newDbProfile = newConfig.getDbConfig();
            newDbProfile.setChangeSetRoot(changeSetRoot);
            newDbProfile.setChangeSetWriterFileName(newConfig.getUsername() + "#1#" + 
            		UUID.randomUUID().toString() + ".eccs");
            newDbProfile.setUsername(newConfig.getUsername());

            
            String tempKey = UUID.randomUUID().toString();
            
            ChangeSetGeneratorBI generator = Ts.get().createDtoChangeSetGenerator(
					new File(newConfig.getDbConfig().getChangeSetRoot(),
							newConfig.getDbConfig().getChangeSetWriterFileName()), 
							new File(newConfig.getDbConfig().getChangeSetRoot(), "#0#"
									+ newConfig.getDbConfig().getChangeSetWriterFileName()),
									ChangeSetGenerationPolicy.MUTABLE_ONLY);
            List<ChangeSetGeneratorBI> extraGeneratorList = (List<ChangeSetGeneratorBI>) process.readAttachement(ProcessAttachmentKeys.EXTRA_CHANGE_SET_GENERATOR_LIST.getAttachmentKey());
            if (extraGeneratorList == null) {
            	extraGeneratorList = new ArrayList<ChangeSetGeneratorBI>();
            }
            extraGeneratorList.add(generator);
            process.writeAttachment(ProcessAttachmentKeys.EXTRA_CHANGE_SET_GENERATOR_LIST.getAttachmentKey(), extraGeneratorList);

            Ts.get().addChangeSetGenerator(tempKey, generator);
           try {
            	Terms.get().commit();
            } catch (Exception e) {
                throw new TaskFailedException();
            } finally {
                Ts.get().removeChangeSetGenerator(tempKey);
            }

            // Set roots
            for (int rootNid : creatorConfig.getRoots().getSetValues()) {
                newConfig.getRoots().add(rootNid);
            }
            // Set src rels
            for (int relTypeNid : creatorConfig.getSourceRelTypes().getSetValues()) {
                newConfig.getSourceRelTypes().add(relTypeNid);
            }

            // Set dest rels
            for (int relTypeNid : creatorConfig.getDestRelTypes().getSetValues()) {
                newConfig.getDestRelTypes().add(relTypeNid);
            }

            // Set path colors
            for (Integer pathNid : creatorConfig.getPathColorMap().keySet()) {
                newConfig.setColorForPath(pathNid, creatorConfig.getColorForPath(pathNid));
            }
            
            // Set desc types
            for (Integer descTypeNid : creatorConfig.getDescTypes().getSetValues()) {
                newConfig.getDescTypes().add(descTypeNid);
            }
            
            newConfig.getShortLabelDescPreferenceList().clear();
            newConfig.getShortLabelDescPreferenceList().addAll(
                creatorConfig.getShortLabelDescPreferenceList().getListValues());

            newConfig.getLongLabelDescPreferenceList().clear();
            newConfig.getLongLabelDescPreferenceList().addAll(
                creatorConfig.getLongLabelDescPreferenceList().getListValues());

            newConfig.getTreeDescPreferenceList().clear();
            newConfig.getTreeDescPreferenceList().addAll(creatorConfig.getTreeDescPreferenceList().getListValues());

            newConfig.getTableDescPreferenceList().clear();
            newConfig.getTableDescPreferenceList().addAll(creatorConfig.getTableDescPreferenceList().getListValues());

            newConfig.getLanguagePreferenceList().clear();
            newConfig.getLanguagePreferenceList().addAll(creatorConfig.getLanguagePreferenceList().getListValues());
            
            newConfig.setShowViewerImagesInTaxonomy(creatorConfig.getShowViewerImagesInTaxonomy());

            newConfig.getRefsetsToShowInTaxonomy().clear();
            newConfig.getRefsetsToShowInTaxonomy().addAll(creatorConfig.getRefsetsToShowInTaxonomy().getListValues());
            
            newConfig.setShowPathInfoInTaxonomy(creatorConfig.getShowPathInfoInTaxonomy());
            newConfig.setShowRefsetInfoInTaxonomy(creatorConfig.getShowRefsetInfoInTaxonomy());
            newConfig.getDescTypes().addAll(creatorConfig.getDescTypes().getSetValues());
            newConfig.getPrefFilterTypesForRel().addAll(creatorConfig.getPrefFilterTypesForRel().getSetValues());
            newConfig.setHighlightConflictsInComponentPanel(creatorConfig.getHighlightConflictsInComponentPanel());
            newConfig.setHighlightConflictsInTaxonomyView(creatorConfig.getHighlightConflictsInTaxonomyView());
            newConfig.setConflictResolutionStrategy(creatorConfig.getConflictResolutionStrategy());
            
            newConfig.getDbConfig().setClassifierChangesChangeSetPolicy(
                creatorConfig.getDbConfig().getClassifierChangesChangeSetPolicy());
            newConfig.getDbConfig().setRefsetChangesChangeSetPolicy(
                creatorConfig.getDbConfig().getRefsetChangesChangeSetPolicy());
            newConfig.getDbConfig().setUserChangesChangeSetPolicy(
                creatorConfig.getDbConfig().getUserChangesChangeSetPolicy());
            newConfig.getDbConfig().setChangeSetWriterThreading(
                creatorConfig.getDbConfig().getChangeSetWriterThreading());
            newConfig.setPrecedence(creatorConfig.getPrecedence());

            // clear the user's path color
            if (creatorConfig.getDbConfig().getUserPath() != null) {
                Color userColor = newConfig.getPathColorMap().remove(
                    creatorConfig.getDbConfig().getUserPath().getConceptNid());
                newConfig.setColorForPath(newConfig.getDbConfig().getUserPath().getConceptNid(), userColor);
            }

            // Create inbox
            createInbox(newConfig, newConfig.getUsername() + ".inbox", userQueueRoot, newConfig.getUsername()
                + ".inbox");
            // Create todo box
            createInbox(newConfig, newConfig.getUsername() + ".todo", userQueueRoot, newConfig.getUsername() + ".inbox");
            // Create outbox box
            createOutbox(newConfig, newConfig.getUsername() + ".outbox", userQueueRoot, newConfig.getUsername()
                + ".inbox");

        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);

            List<AlertToDataConstraintFailure> errorsAndWarnings = Terms.get().getCommitErrorsAndWarnings();

            errorsAndWarnings.add(new AlertToDataConstraintFailure(AlertToDataConstraintFailure.ALERT_TYPE.ERROR,
                "<html>Error while creating user path and queues: <br>" + e.getMessage(), newConfig.getDbConfig()
                    .getUserConcept()));

            try {
                process.setProperty(errorsAndWarningsPropName, errorsAndWarnings);
                Terms.get().cancel();
            } catch (Exception inner) {
                inner.printStackTrace();
                throw new TaskFailedException(inner);
        }
            return Condition.FALSE;
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
            Configuration queueConfig =
                    ConfigurationProvider.getInstance(new String[] { newQueueConfig.getAbsolutePath() });
            Entry[] entries =
                    (Entry[]) queueConfig.getEntry("org.dwfa.queue.QueueServer", "entries", Entry[].class,
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

    private void createUser(I_ConfigAceFrame newConfig, I_ConfigAceFrame creatorConfig) throws TaskFailedException,
            TerminologyException, IOException {
        AceLog.getAppLog().info("Create new path for user: " + newConfig.getDbConfig().getFullName());
        if (newConfig.getDbConfig().getFullName() == null || newConfig.getDbConfig().getFullName().length() == 0) {
            JOptionPane.showMessageDialog(newConfig.getWorkflowPanel().getTopLevelAncestor(),
                "Full name cannot be empty.");
            throw new TaskFailedException();
        }
        I_TermFactory tf = Terms.get();
        // Needs a concept record...
        I_GetConceptData userConcept = tf.newConcept(UUID.randomUUID(), false, creatorConfig);

        // Needs a description record...
        tf.newDescription(UUID.randomUUID(), userConcept, "en", newConfig.getDbConfig().getFullName(), Terms.get()
            .getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), creatorConfig);
        tf.newDescription(UUID.randomUUID(), userConcept, "en", newConfig.getUsername(), Terms.get().getConcept(
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), creatorConfig);
        tf.newDescription(UUID.randomUUID(), userConcept, "en", newConfig.getUsername() + ".inbox", Terms.get()
            .getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids()), creatorConfig);

        // Needs a relationship record...
        tf.newRelationship(UUID.randomUUID(), userConcept, tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
            .getUids()), tf.getConcept(ArchitectonicAuxiliary.Concept.USER.getUids()), tf
            .getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), tf
            .getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()), tf
            .getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, creatorConfig);
        newConfig.getDbConfig().setUserConcept(userConcept);
        tf.addUncommitted(userConcept);

    }

    private void createDevPath(I_ConfigAceFrame newConfig, I_ConfigAceFrame creatorConfig) throws Exception {
        Set<PositionBI> inputSet = getDeveloperOrigins(creatorConfig);
        PathBI userPath = createNewPath(newConfig, creatorConfig, inputSet, " dev path");
        newConfig.addEditingPath(userPath);
        I_GetConceptData userPathConcept = Terms.get().getConcept(userPath.getConceptNid());
        newConfig.setClassifierInputPath(userPathConcept);
        newConfig.getViewPositionSet().add(Terms.get().newPosition(userPath, Long.MAX_VALUE));
        newConfig.getDbConfig().setUserPath(userPathConcept);
    }

    private Set<PositionBI> getDeveloperOrigins(I_ConfigAceFrame creatorConfig) throws TerminologyException,
            IOException, TaskFailedException {
    	PositionBI developerViewPosition = creatorConfig.getViewPositionSet().iterator().next();
        if (developerViewPosition == null) {
            throw new TaskFailedException("developerViewPosition input path is null..."
                + "You must set the view position prior to running the new user process...");
        }
        Set<PositionBI> inputSet = new HashSet<PositionBI>(developerViewPosition.getPath().getOrigins());
        return inputSet;
    }

    private PathBI createNewPath(I_ConfigAceFrame config, I_ConfigAceFrame commitConfig, 
    		Collection<? extends PositionBI> positionSet,
            String suffix) throws TaskFailedException, TerminologyException, IOException {
        AceLog.getAppLog().info("Create new path for user: " + config.getDbConfig().getFullName());
        if (config.getDbConfig().getFullName() == null || config.getDbConfig().getFullName().length() == 0) {
            JOptionPane
                .showMessageDialog(config.getWorkflowPanel().getTopLevelAncestor(), "Full name cannot be empty.");
            throw new TaskFailedException();
        }
        AceLog.getAppLog().info(positionSet.toString());
        if (positionSet.isEmpty()) {
            throw new TaskFailedException("You must select at least one origin for path.");
        }
        UUID newPathUid = UUID.randomUUID();

        // Needs a concept record...
        I_GetConceptData pathConcept = Terms.get().newConcept(newPathUid, false, commitConfig);

        // Needs a description record...
        Terms.get().newDescription(UUID.randomUUID(), pathConcept, "en", config.getDbConfig().getFullName() + suffix,
        		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), 
        		commitConfig);
        Terms.get().newDescription(UUID.randomUUID(), pathConcept, "en", config.getUsername() + suffix,
            Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), commitConfig);

        // Needs a relationship record...
        Terms.get().newRelationship(UUID.randomUUID(), pathConcept,
        		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
        		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.DEVELOPMENT.getUids()),
        		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
        		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
        		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, commitConfig);

        Terms.get().addUncommitted(pathConcept);
        
        return Terms.get().newPath(positionSet, pathConcept, commitConfig);
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

    public String getErrorsAndWarningsPropName() {
        return errorsAndWarningsPropName;
    }

    public void setErrorsAndWarningsPropName(String errorsAndWarningsPropName) {
        this.errorsAndWarningsPropName = errorsAndWarningsPropName;
    }

    public String getCreatorProfilePropName() {
        return creatorProfilePropName;
    }

    public void setCreatorProfilePropName(String creatorProfilePropName) {
        this.creatorProfilePropName = creatorProfilePropName;
    }

    public String getNewProfilePropName() {
        return newProfilePropName;
    }

    public void setNewProfilePropName(String newProfilePropName) {
        this.newProfilePropName = newProfilePropName;
    }

}
