/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.profile;

//~--- non-JDK imports --------------------------------------------------------
import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import net.jini.core.entry.Entry;

import org.dwfa.ace.api.*;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.queue.QueueServer;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.id.Type5UuidFactory;
import org.dwfa.util.io.FileIO;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;

import java.io.*;

import java.security.NoSuchAlgorithmException;

import java.util.*;

import javax.swing.JOptionPane;

/**
 *
 * @author kec
 */
@BeanList(specs = {
    @Spec(directory = "tasks/ide/profile",
    type = BeanType.TASK_BEAN)})
public class CreateUserAndQueuesInBatchBasedOnCreatorProfile extends AbstractTask {

    private static final int dataVersion = 1;
    private static final long serialVersionUID = 1;

    //~--- methods -------------------------------------------------------------
    public static void addIfNotNull(I_IntList roots, I_ConceptualizeUniversally concept, I_TermFactory tf)
            throws TerminologyException, IOException {
        try {
            roots.add(tf.uuidToNative(concept.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }
    }

    public static void addIfNotNull(I_IntSet intSet, I_ConceptualizeUniversally concept, I_TermFactory tf)
            throws TerminologyException, IOException {
        try {
            intSet.add(tf.uuidToNative(concept.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }
    }

    /**
     * @see
     * org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     * org.dwfa.bpa.process.I_Work)
     */
    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    private void createInbox(I_ConfigAceFrame config, String inboxName, File userQueueRoot,
            String nodeInboxAddress) {
        config.getQueueAddressesToShow().add(inboxName);
        createQueue(config, "inbox", inboxName, userQueueRoot, nodeInboxAddress);
    }

    private void createOutbox(I_ConfigAceFrame config, String outboxName, File userQueueRoot,
            String nodeInboxAddress) {
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

            Configuration queueConfig = ConfigurationProvider.getInstance(new String[]{
                        newQueueConfig.getAbsolutePath()});
            Entry[] entries = (Entry[]) queueConfig.getEntry("org.dwfa.queue.QueueServer", "entries",
                    Entry[].class, new Entry[]{});

            for (Entry entry : entries) {
                if (ElectronicAddress.class.isAssignableFrom(entry.getClass())) {
                    ElectronicAddress ea = (ElectronicAddress) entry;

                    config.getQueueAddressesToShow().add(ea.address);

                    break;
                }
            }

            if (QueueServer.started(newQueueConfig)) {
                AceLog.getAppLog().info("Queue already started: "
                        + newQueueConfig.toURI().toURL().toExternalForm());
            } else {
                new QueueServer(new String[]{newQueueConfig.getCanonicalPath()}, null);
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    private I_GetConceptData createUser(I_ConfigAceFrame newConfig, I_ConfigAceFrame creatorConfig)
            throws TaskFailedException, TerminologyException, IOException, UnsupportedEncodingException,
            NoSuchAlgorithmException {
        AceLog.getAppLog().info("Create new path for user: " + newConfig.getDbConfig().getFullName());

        if ((newConfig.getDbConfig().getFullName() == null)
                || (newConfig.getDbConfig().getFullName().length() == 0)) {
            JOptionPane.showMessageDialog(newConfig.getWorkflowPanel().getTopLevelAncestor(),
                    "Full name cannot be empty.");

            throw new TaskFailedException();
        }

        I_TermFactory tf = Terms.get();

        // Needs a concept record...
        UUID userUuid = Type5UuidFactory.get(Type5UuidFactory.USER_FULLNAME_NAMESPACE,
                newConfig.getDbConfig().getFullName());
        I_GetConceptData userConcept;

        if (Ts.get().hasUuid(userUuid)) {
            return (I_GetConceptData) Ts.get().getConcept(userUuid);
        } else {
            userConcept = tf.newConcept(userUuid, false, creatorConfig);

            // Needs a description record...
            tf.newDescription(
                    UUID.randomUUID(), userConcept, "en", newConfig.getDbConfig().getFullName(),
                    Terms.get().getConcept(
                    ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), creatorConfig);
            tf.newDescription(
                    UUID.randomUUID(), userConcept, "en", newConfig.getUsername(),
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()),
                    creatorConfig);
            tf.newDescription(UUID.randomUUID(), userConcept, "en", newConfig.getUsername() + ".inbox",
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids()),
                    creatorConfig);

            // Needs a relationship record...
            tf.newRelationship(UUID.randomUUID(), userConcept,
                    tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.USER.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0,
                    creatorConfig);

            //add workflow relationship
            tf.newRelationship(UUID.randomUUID(), userConcept,
                    tf.getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0,
                    creatorConfig);

            newConfig.getDbConfig().setUserConcept(userConcept);
            tf.addUncommitted(userConcept);

            return userConcept;
        }
    }

    private void addWfRelIfDoesNotExist(String userUuidString, I_ConfigAceFrame creatorConfig) throws TerminologyException, IOException {
        I_GetConceptData userConcept = Terms.get().getConcept(UUID.fromString(userUuidString));
        int wfNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getUids());
        boolean found = false;
        for (I_RelVersioned rel : userConcept.getSourceRels()) {
            if (rel.getDestinationNid() == wfNid) {
                found = true;
            }
        }
        if (!found) {
            I_TermFactory tf = Terms.get();
            tf.newRelationship(UUID.randomUUID(), userConcept,
                    tf.getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0,
                    creatorConfig);
        }
    }

    private void setUserConcept(String userUuidString, I_ConfigAceFrame newConfig,
            I_ConfigAceFrame creatorConfig) throws TerminologyException, IOException {
        I_GetConceptData userConcept = Terms.get().getConcept(UUID.fromString(userUuidString));
        I_TermFactory tf = Terms.get();
        tf.newDescription(UUID.randomUUID(), userConcept, "en", newConfig.getUsername() + ".inbox",
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids()),
                    creatorConfig);
        newConfig.getDbConfig().setUserConcept(userConcept);
    }

    /**
     * @see
     * org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     * org.dwfa.bpa.process.I_Work)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker)
            throws TaskFailedException {
        File userFile = new File("../../src/main/users/users.txt");

        try {
            FileReader fr = new FileReader(userFile);
            BufferedReader br = new BufferedReader(fr);

            // Read the header line...
            br.readLine();

            String userLine = br.readLine();

            while (userLine != null) {
                String[] parts = userLine.split("\t");

                setupUser(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
                userLine = br.readLine();
            }
        } catch (Exception ex) {
            throw new TaskFailedException(ex);
        }

        return Condition.TRUE;
    }

    public static I_ConfigAceFrame newProfile(String fullName, String username, String password,
            String adminUsername, String adminPassword)
            throws TerminologyException, IOException {
        I_ImplementTermFactory tf = (I_ImplementTermFactory) Terms.get();
        I_ConfigAceFrame activeConfig = tf.newAceFrameConfig();

        for (I_HostConceptPlugins.HOST_ENUM h : I_HostConceptPlugins.HOST_ENUM.values()) {
            for (I_PluginToConceptPanel plugin : activeConfig.getDefaultConceptPanelPluginsForEditor()) {
                activeConfig.addConceptPanelPlugins(h, plugin.getId(), plugin);
            }
        }

        I_ConfigAceDb newDbProfile = tf.newAceDbConfig();

        newDbProfile.setUsername(username);
        newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
        newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
        newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
        newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
        activeConfig.setDbConfig(newDbProfile);
        newDbProfile.getAceFrames().add(activeConfig);

        if ((fullName == null) || (fullName.length() < 2)) {
            fullName = "Full Name";
        }

        if ((username == null) || (username.length() < 2)) {
            username = "username";
        }

        if ((adminUsername == null) || (adminUsername.length() < 2)) {
            adminUsername = "admin";
            adminPassword = "visit.bend";
        }

        activeConfig.getDbConfig().setFullName(fullName);
        activeConfig.setUsername(username);
        activeConfig.setPassword(password);
        activeConfig.setAdminPassword(adminPassword);
        activeConfig.setAdminUsername(adminUsername);

        I_IntList statusPopupTypes = tf.newIntList();

        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.ACTIVE, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.CURRENT, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.FLAGGED_FOR_REVIEW, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.LIMITED, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.PENDING_MOVE, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.INACTIVE, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.RETIRED, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.DUPLICATE, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.OUTDATED, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.AMBIGUOUS, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.ERRONEOUS, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.LIMITED, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.INAPPROPRIATE, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE, tf);
        addIfNotNull(statusPopupTypes, ArchitectonicAuxiliary.Concept.PENDING_MOVE, tf);
        activeConfig.setEditStatusTypePopup(statusPopupTypes);

        I_IntList descPopupTypes = tf.newIntList();

        descPopupTypes.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.ENTRY_DESCRIPTION_TYPE.getUids()));
        descPopupTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.XHTML_DEF.getUids()));
        activeConfig.setEditDescTypePopup(descPopupTypes);

        I_IntList imagePopupTypes = tf.newIntList();

        addIfNotNull(imagePopupTypes, ArchitectonicAuxiliary.Concept.AUXILLARY_IMAGE, tf);
        addIfNotNull(imagePopupTypes, ArchitectonicAuxiliary.Concept.VIEWER_IMAGE, tf);
        activeConfig.setEditImageTypePopup(imagePopupTypes);

        I_IntList relCharacteristic = tf.newIntList();

        relCharacteristic.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()));
        relCharacteristic.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids()));
        relCharacteristic.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC.getUids()));
        relCharacteristic.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC.getUids()));
        relCharacteristic.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC.getUids()));
        activeConfig.setEditRelCharacteristicPopup(relCharacteristic);

        I_IntList relRefinabilty = tf.newIntList();

        relRefinabilty.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.getUids()));
        relRefinabilty.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()));
        relRefinabilty.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()));
        activeConfig.setEditRelRefinabiltyPopup(relRefinabilty);

        I_IntList relTypes = tf.newIntList();

        relTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));

        try {
            relTypes.add(tf.uuidToNative(SNOMED.Concept.IS_A.getUids()));
            activeConfig.setClassifierIsaType(tf.getConcept(SNOMED.Concept.IS_A.getUids()));
            activeConfig.setClassificationRoot(tf.getConcept(SNOMED.Concept.ROOT.getUids()));
        } catch (NoMappingException e) {
            activeConfig.setClassifierIsaType(
                    tf.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids()));
            activeConfig.setClassificationRoot(tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));

            // nothing else to do...
        }

        activeConfig.setEditRelTypePopup(relTypes);

        I_IntSet roots = tf.newIntSet();

        addIfNotNull(roots, ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT, tf);
        addIfNotNull(roots, SNOMED.Concept.ROOT, tf);
        activeConfig.setRoots(roots);

        I_IntSet allowedStatus = tf.newIntSet();

        allowedStatus.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.ADJUDICATED, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.ADJUDICATED_AND_PROCESSED, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED, tf);
        allowedStatus.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));

        // TODO: Adding status = "Active value" in snomed metadata - TEMP fix
        allowedStatus.add(Ts.get().getNidForUuids(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()));
        allowedStatus.add(Ts.get().getNidForUuids(SnomedMetadataRf2.CONCEPT_NON_CURRENT_RF2.getUuids()));
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_INTERNAL_USE, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_FOR_RELEASE, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.DUAL_REVIEWED, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.DUAL_REVIEWED_AND_PROCESSED, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.DUPLICATE_PENDING_RETIREMENT, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.FLAGGED_FOR_REVIEW, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_DESC_STYLE_ERROR, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_DUPLICATE, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_REL_ERROR, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.CURRENT_TEMP_INTERNAL_USE, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.FLAGGED_FOR_DUAL_REVIEW, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.INTERNAL_USE_ONLY, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.LIMITED, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.PENDING_MOVE, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.PROCESSED, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.RESOLVED_IN_DUAL, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.PROMOTED, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.RESOLVED_IN_DUAL_AND_PROCESSED, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.CONFLICTING, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.CONSTANT, tf);
        addIfNotNull(allowedStatus, ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE, tf);
        activeConfig.setAllowedStatus(allowedStatus);

        I_IntSet destRelTypes = tf.newIntSet();

        try {
            destRelTypes.add(tf.uuidToNative(SNOMED.Concept.IS_A.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }

        destRelTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
        activeConfig.setDestRelTypes(destRelTypes);

        I_IntSet sourceRelTypes = tf.newIntSet();

        activeConfig.setSourceRelTypes(sourceRelTypes);

        I_IntSet descTypes = tf.newIntSet();

        addIfNotNull(descTypes, ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE, tf);
        addIfNotNull(descTypes, ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE, tf);
        addIfNotNull(descTypes, ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE, tf);
        addIfNotNull(descTypes, ArchitectonicAuxiliary.Concept.DESCRIPTION_TYPE, tf);
        addIfNotNull(descTypes, ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE, tf);
        addIfNotNull(descTypes, ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE, tf);
        addIfNotNull(descTypes, ArchitectonicAuxiliary.Concept.XHTML_DEF, tf);
        activeConfig.setDescTypes(descTypes);
        activeConfig.setDefaultDescriptionType(
                tf.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));

        try {
            activeConfig.setDefaultImageType(
                    tf.getConcept(ArchitectonicAuxiliary.Concept.AUXILLARY_IMAGE.getUids()));
        } catch (NoMappingException ex) {
        }

        activeConfig.setDefaultRelationshipCharacteristic(
                tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()));
        activeConfig.setDefaultRelationshipRefinability(
                tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()));
        activeConfig.setDefaultRelationshipType(
                tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));

        try {
            activeConfig.setDefaultRelationshipType(tf.getConcept(SNOMED.Concept.IS_A.getUids()));
        } catch (NoMappingException e) {
            // nothing to do...
        }

        activeConfig.setDefaultStatus(tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));

        I_IntList treeDescPrefList = activeConfig.getTreeDescPreferenceList();

        treeDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        treeDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        treeDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        treeDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

        I_IntList shortLabelDescPrefList = activeConfig.getShortLabelDescPreferenceList();

        shortLabelDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        shortLabelDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        shortLabelDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        shortLabelDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

        I_IntList longLabelDescPrefList = activeConfig.getLongLabelDescPreferenceList();

        longLabelDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        longLabelDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        longLabelDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        longLabelDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));

        I_IntList tableDescPrefList = activeConfig.getTableDescPreferenceList();

        tableDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.getUids()));
        tableDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
        tableDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids()));
        tableDescPrefList.add(
                tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        activeConfig.setDefaultStatus(tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
        activeConfig.setDefaultDescriptionType(
                tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        activeConfig.setSubversionToggleVisible(false);
        activeConfig.setDefaultRelationshipType(
                tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
        activeConfig.setDefaultRelationshipCharacteristic(
                tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()));
        activeConfig.setDefaultRelationshipRefinability(
                tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()));

        try {
            PathBI editPath = tf.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());

            // activeConfig.addEditingPath(editPath);
            PositionBI viewPosition = tf.newPosition(editPath, Long.MAX_VALUE);
            Set<PositionBI> viewSet = new HashSet<PositionBI>();

            viewSet.add(viewPosition);
            activeConfig.setViewPositions(viewSet);
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.ID, true);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.ATTRIBUTES, true);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.DESCRIPTIONS, true);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.SOURCE_RELS, true);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.DEST_RELS, true);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.LINEAGE, true);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.LINEAGE_GRAPH, false);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.IMAGE, true);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.CONFLICT, true);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.STATED_INFERRED, false);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.PREFERENCES, true);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.HISTORY, true);
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.REFSETS, false);
        activeConfig.setPrecedence(Precedence.PATH);

        return activeConfig;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {
            // nothing to do...
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private boolean setupUser(String fullname, String username, String password, String userUuid,
            String adminUsername, String adminPassword)
            throws TaskFailedException {
        try {
            String userDirStr = "profiles" + File.separator + username;
            File userDir = new File(userDirStr);
            File userQueueRoot = new File("queues", username);

            if (userDir.exists() && userQueueRoot.exists()) {
                return false;
            }

            I_ConfigAceFrame creatorConfig = Terms.get().getActiveAceFrameConfig();
            I_ConfigAceFrame newConfig = newProfile(fullname, username, password, adminUsername,
                    adminPassword);

            if (username != null) {
                if (newConfig.getAddressesList().contains(username) == false) {
                    newConfig.getAddressesList().add(username);
                }
            }

            newConfig.getViewPositionSet().clear();
            newConfig.getEditingPathSet().clear();
            newConfig.setClassificationRoleRoot(creatorConfig.getClassificationRoleRoot());
            newConfig.setClassificationRoot(creatorConfig.getClassificationRoot());
            newConfig.setClassifierInputPath(creatorConfig.getClassifierInputPath());
            newConfig.setClassifierIsaType(creatorConfig.getClassifierIsaType());
            newConfig.setClassifierOutputPath(creatorConfig.getClassifierOutputPath());
            userQueueRoot.mkdirs();

            EditOnRootPath rootPathProfile = new EditOnRootPath(creatorConfig);

            // Create new concept for user...
            if (userUuid == null || userUuid.equals("")) {
                createUser(newConfig, rootPathProfile);
            } else {
                setUserConcept(userUuid, newConfig, creatorConfig);
                addWfRelIfDoesNotExist(userUuid, creatorConfig);
            }

            // Create new paths for user...
            if (creatorConfig.getPromotionPathSet().size() > 1) {
                throw new TaskFailedException("This task only supports a single promotion path...\nFound: "
                        + creatorConfig.getPromotionPathSet().size());
            }

            if (creatorConfig.getEditingPathSet().size() > 1) {
                throw new TaskFailedException("This task only supports a single editing path...\nFound: "
                        + creatorConfig.getPromotionPathSet().size());
            }

            if (creatorConfig.getViewPositionSet().size() == 1) {
                PositionBI viewPosition = (PositionBI) creatorConfig.getViewPositionSet().toArray()[0];
                PathBI editPath = (PathBI) creatorConfig.getEditingPathSet().toArray()[0];

                if (viewPosition.getPath().getConceptNid() != editPath.getConceptNid()) {
                    newConfig.getViewPositionSet().clear();
                    newConfig.getViewPositionSet().addAll(creatorConfig.getViewPositionSet());
                }
            }

            newConfig.getPromotionPathSet().addAll(creatorConfig.getPromotionPathSet());

            List<AlertToDataConstraintFailure> errorsAndWarnings = Terms.get().getCommitErrorsAndWarnings();

            if (errorsAndWarnings.size() > 0) {
                AceLog.getAppLog().warning(errorsAndWarnings.toString());
                Terms.get().cancel();

                return false;
            }

            File changeSetRoot = new File(userDir, "changesets");

            changeSetRoot.mkdirs();

            I_ConfigAceDb newDbProfile = newConfig.getDbConfig();

            newDbProfile.setChangeSetRoot(changeSetRoot);
            newDbProfile.setChangeSetWriterFileName(newConfig.getUsername() + "#1#"
                    + UUID.randomUUID().toString() + ".eccs");
            newDbProfile.setUsername(newConfig.getUsername());

            String tempKey = UUID.randomUUID().toString();
            ChangeSetGeneratorBI generator =
                    Ts.get().createDtoChangeSetGenerator(new File(newConfig.getDbConfig().getChangeSetRoot(), newConfig.getDbConfig().getChangeSetWriterFileName()), new File(newConfig.getDbConfig().getChangeSetRoot(), "#0#"
                    + newConfig.getDbConfig().getChangeSetWriterFileName()), ChangeSetGenerationPolicy.MUTABLE_ONLY);
            List<ChangeSetGeneratorBI> extraGeneratorList = new ArrayList<ChangeSetGeneratorBI>();

            extraGeneratorList.add(generator);
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
            newConfig.getTreeDescPreferenceList().addAll(
                    creatorConfig.getTreeDescPreferenceList().getListValues());
            newConfig.getTableDescPreferenceList().clear();
            newConfig.getTableDescPreferenceList().addAll(
                    creatorConfig.getTableDescPreferenceList().getListValues());
            newConfig.getLanguagePreferenceList().clear();
            newConfig.getLanguagePreferenceList().addAll(
                    creatorConfig.getLanguagePreferenceList().getListValues());
            newConfig.setShowViewerImagesInTaxonomy(creatorConfig.getShowViewerImagesInTaxonomy());
            newConfig.getRefsetsToShowInTaxonomy().clear();
            newConfig.getRefsetsToShowInTaxonomy().addAll(
                    creatorConfig.getRefsetsToShowInTaxonomy().getListValues());
            newConfig.setShowPathInfoInTaxonomy(creatorConfig.getShowPathInfoInTaxonomy());
            newConfig.setShowRefsetInfoInTaxonomy(creatorConfig.getShowRefsetInfoInTaxonomy());
            newConfig.getDescTypes().addAll(creatorConfig.getDescTypes().getSetValues());
            newConfig.getPrefFilterTypesForRel().addAll(creatorConfig.getPrefFilterTypesForRel().getSetValues());
            newConfig.setHighlightConflictsInComponentPanel(
                    creatorConfig.getHighlightConflictsInComponentPanel());
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

            newConfig.getEditingPathSet().clear();
            newConfig.getEditingPathSet().addAll(creatorConfig.getEditingPathSet());
            newConfig.getViewPositionSet().clear();
            newConfig.getViewPositionSet().addAll(creatorConfig.getViewPositionSet());

            newConfig.setClassifierInputMode(creatorConfig.getClassifierInputMode());

            // Create inbox
            createInbox(newConfig, newConfig.getUsername() + ".inbox", userQueueRoot,
                    newConfig.getUsername() + ".inbox");

            // Create todo box
            createInbox(newConfig, newConfig.getUsername() + ".todo", userQueueRoot,
                    newConfig.getUsername() + ".inbox");

            // Create outbox box
            createOutbox(newConfig, newConfig.getUsername() + ".outbox", userQueueRoot,
                    newConfig.getUsername() + ".inbox");
            newConfig.getDbConfig().setProfileFile(new File(userDir, username + ".wb"));

            FileOutputStream fos = new FileOutputStream(newConfig.getDbConfig().getProfileFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(newConfig.getDbConfig());
            oos.close();

            return true;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    @Override
    public Collection<Condition> getConditions() {
        return AbstractTask.CONDITIONAL_TEST_CONDITIONS;
    }
}
