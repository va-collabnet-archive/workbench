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
package org.dwfa.ace.task.profile.cap;

import java.awt.Color;
import java.beans.PropertyVetoException;
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

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
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
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.queue.QueueServer;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

@BeanList(specs = { @Spec(directory = "tasks/ide/profile/cap", type = BeanType.TASK_BEAN) })
public class CreateCapUserPathAndQueuesBasedOnCreatorProfile extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String creatorProfilePropName = ProcessAttachmentKeys.COMMIT_PROFILE.getAttachmentKey();
    private String newProfilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String errorsAndWarningsPropName = ProcessAttachmentKeys.ERRORS_AND_WARNINGS.getAttachmentKey();
    private String parentConceptForUserPropName = ProcessAttachmentKeys.PARENT_CONCEPT_FOR_USER.getAttachmentKey();
    private String parentConceptForPathPropName = ProcessAttachmentKeys.PARENT_CONCEPT_FOR_PATH.getAttachmentKey();
    private String addToPathOriginPropName = ProcessAttachmentKeys.ADD_TO_PATH_ORIGIN.getAttachmentKey();
    private String pathsForViewPropName = ProcessAttachmentKeys.PATHS_FOR_VIEW.getAttachmentKey();
    private String pathsForOriginPropName = ProcessAttachmentKeys.PATHS_FOR_ORIGIN.getAttachmentKey();
    private String releaseDatePropName = ProcessAttachmentKeys.RELEASE_DATE.getAttachmentKey();
	private I_GetConceptData parentConceptForPath;


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(newProfilePropName);
        out.writeObject(creatorProfilePropName);
        out.writeObject(errorsAndWarningsPropName);
        out.writeObject(parentConceptForUserPropName);
        out.writeObject(parentConceptForPathPropName);
        out.writeObject(addToPathOriginPropName);
        out.writeObject(pathsForViewPropName);
        out.writeObject(pathsForOriginPropName);
        out.writeObject(releaseDatePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            newProfilePropName = (String) in.readObject();
            creatorProfilePropName = (String) in.readObject();
            errorsAndWarningsPropName = (String) in.readObject();
            parentConceptForUserPropName = (String) in.readObject();
            parentConceptForPathPropName = (String) in.readObject();
            addToPathOriginPropName = (String) in.readObject();
            pathsForViewPropName = (String) in.readObject();
            pathsForOriginPropName = (String) in.readObject();
            releaseDatePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public String getNewProfilePropName() {
        return newProfilePropName;
    }

    public void setNewProfilePropName(String profilePropName) {
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
            String s = (String)process.getProperty(parentConceptForPathPropName);
            parentConceptForPath = Terms.get().getConcept(Integer.parseInt(s));

            // Only one path for 3 PathsProps thus far
            s = (String)process.getProperty(addToPathOriginPropName);
            PathBI addToPathOrigin = Terms.get().getPath(Integer.parseInt(s));

            s = (String)process.getProperty(pathsForViewPropName);
            PathBI pathsForView= Terms.get().getPath(Integer.parseInt(s));

            s = (String)process.getProperty(pathsForOriginPropName);
            
            PathBI pathsForOrigin = null;
            try {
            	pathsForOrigin = Terms.get().getPath(Integer.parseInt(s));
            } catch (Exception ee) {
            	System.out.println("Unable to identify paths for origin from selection");
            }
            
            String releaseDate = (String) process.getProperty(releaseDatePropName);

            // Set edit path to be Workbench Aux (via Architectonic branch)
            I_ConfigAceFrame creatorConfig = (I_ConfigAceFrame) process.getProperty(creatorProfilePropName);
            PathBI editPath = Terms.get().getPath(Concept.ARCHITECTONIC_BRANCH.getUids());
            creatorConfig.getEditingPathSet().clear();
            creatorConfig.addEditingPath(editPath);
//            creatorConfig.getViewPositionSet().clear();
//            creatorConfig.getViewPositionSet().add(Terms.get().newPosition(editPath, Long.MAX_VALUE));

            
            newConfig.getViewPositionSet().clear();
            newConfig.getEditingPathSet().clear();
            newConfig.setClassificationRoleRoot(creatorConfig.getClassificationRoleRoot());
            newConfig.setClassificationRoot(creatorConfig.getClassificationRoot());
            newConfig.setClassifierInputPath(creatorConfig.getClassifierInputPath());
            newConfig.setClassifierIsaType(creatorConfig.getClassifierIsaType());
            newConfig.setClassifierOutputPath(creatorConfig.getClassifierOutputPath());
            
            String releaseDateProfileName= releaseDate + "_" + newConfig.getUsername();
            String userDirStr = "profiles" + File.separator + releaseDateProfileName;
            
            File userDir = new File(userDirStr);
            File userQueueRoot = new File("queues", releaseDateProfileName);
            userQueueRoot.mkdirs();

            // Create new concept for user if not already in existence...
            I_GetConceptData userConcept = userConceptExists(newConfig.getDbConfig().getFullName(), newConfig.getUsername());
           	
            if (userConcept == null) {            	
                s = (String)process.getProperty(parentConceptForUserPropName);
                I_GetConceptData parentConceptForUser = Terms.get().getConcept(Integer.parseInt(s));
            	userConcept = createUser(newConfig, creatorConfig, parentConceptForUser);
            } 
           	
            newConfig.getDbConfig().setUserConcept(userConcept);

            //This is creating issue (since modelers are already active , still it goes make user as active
            
            if (userWithoutActiveModeler(userConcept, newConfig)) {
             	setNewUserAsActiveModeler(userConcept, creatorConfig);
            }
            // Create new paths for user...
            createDevPath(newConfig, creatorConfig, releaseDate, pathsForView, pathsForOrigin, addToPathOrigin);

/*            
			createClassifierPath(newConfig, promotePathProfile);
            if (creatorConfig.getPromotionPathSet().size() > 1) {
                throw new TaskFailedException("This task only supports a single promotion path...\nFound: "
                    + creatorConfig.getPromotionPathSet().size());
            	
            }
*/            
            if (creatorConfig.getEditingPathSet().size() > 1) {
                throw new TaskFailedException("This task only supports a single editing path...\nFound: "
                    + creatorConfig.getPromotionPathSet().size());
            }

/*            newConfig.getPromotionPathSet().addAll(creatorConfig.getPromotionPathSet());
*/ 
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
            
            newDbProfile.setChangeSetWriterFileName(releaseDateProfileName + "#1#" + 
            		UUID.randomUUID().toString() + ".eccs");
            newDbProfile.setUsername(releaseDateProfileName);

            
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
            newConfig.getDbConfig().setClassifierChangesChangeSetPolicy(
                creatorConfig.getDbConfig().getClassifierChangesChangeSetPolicy());
            newConfig.getDbConfig().setRefsetChangesChangeSetPolicy(
                creatorConfig.getDbConfig().getRefsetChangesChangeSetPolicy());
            newConfig.getDbConfig().setUserChangesChangeSetPolicy(
                creatorConfig.getDbConfig().getUserChangesChangeSetPolicy());
            newConfig.getDbConfig().setChangeSetWriterThreading(
                creatorConfig.getDbConfig().getChangeSetWriterThreading());
//
// Will fire prop change unnecessarily
//            Precedence c = creatorConfig.getPrecedence();
//             newConfig.setPrecedence(c);
//            newConfig.setConflictResolutionStrategy(creatorConfig.getConflictResolutionStrategy());
            
            // clear the user's path color
            if (creatorConfig.getDbConfig().getUserPath() != null) {
                Color userColor = newConfig.getPathColorMap().remove(
                    creatorConfig.getDbConfig().getUserPath().getConceptNid());
                newConfig.setColorForPath(newConfig.getDbConfig().getUserPath().getConceptNid(), userColor);
            }

            /*
           // Create inbox
            createInbox(newConfig, releaseDateProfileName + ".inbox", userQueueRoot, releaseDateProfileName
                + ".inbox");
            // Create todo box
            createInbox(newConfig, releaseDateProfileName + ".todo", userQueueRoot, releaseDateProfileName + ".inbox");
            // Create outbox box
            createOutbox(newConfig, releaseDateProfileName + ".outbox", userQueueRoot, releaseDateProfileName
                + ".inbox");
             */
            
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Failed to create cap user with exception: " + e.getLocalizedMessage(), e);

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

    private boolean userWithoutActiveModeler(I_GetConceptData userConcept, I_ConfigAceFrame newConfig) throws TerminologyException, IOException, ContradictionException {
    	return !WorkflowHelper.isActiveModeler(userConcept.getVersion(newConfig.getViewCoordinate()));
	}

	private void setNewUserAsActiveModeler(I_GetConceptData userConcept, I_ConfigAceFrame creatorConfig) throws TerminologyException, IOException, PropertyVetoException {
        I_TermFactory tf = Terms.get();
        
    	I_GetConceptData workflowEditorStatusRelType = tf.getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS.getUids());
    	I_GetConceptData activeModelerDestinationConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getUids());
		I_GetConceptData activeStatus = tf.getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids());

		PathBI editPath = creatorConfig.getEditingPathSet().iterator().next();
		
		// Retire bad pathed ActMod and also check to see if need new one
        boolean hasActModRel = false;

        NidSetBI desiredRelType = new NidSet();
        desiredRelType.add(workflowEditorStatusRelType.getConceptNid());
        
        List<? extends I_RelTuple> rels = userConcept.getSourceRelTuples(creatorConfig.getAllowedStatus(), 
        																 desiredRelType,
					        											 creatorConfig.getViewPositionSetReadOnly(), 
					        											 creatorConfig.getPrecedence(), 
					        											 creatorConfig.getConflictResolutionStrategy());
        
        for (I_RelTuple rel : rels) {
			if (rel.getC2Id() == activeModelerDestinationConcept.getConceptNid()) {
				if (rel.getPathNid() == editPath.getConceptNid()) {
					hasActModRel = true;
				} else {
					retireRel(rel, creatorConfig);
				}
			}
        }
        
        if (!hasActModRel) {
	        // Set user as active editor (for Workflow)
	        tf.newRelationship(UUID.randomUUID(), userConcept, workflowEditorStatusRelType , activeModelerDestinationConcept , tf
	                .getConcept(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()), tf
	                .getConcept(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids()), 
	                activeStatus, 0, creatorConfig);
        }
        
        tf.addUncommitted(userConcept);
	}

	private I_GetConceptData userConceptExists(String fsn, String preferred) {
		I_GetConceptData parentUserCon = null;
		Set<String> existingUserStrings = new HashSet<String>();

		try {
			int fsnNid = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid();
			int prefNid = ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid();
			int rf2FsnNid = Terms.get().uuidToNative(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()[0]);
			int rf2PrefNid = Terms.get().uuidToNative(SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0]);
			
			
			parentUserCon = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid());

    		for (I_GetConceptData user : WorkflowHelper.getChildren(parentUserCon)) {
    			Collection<? extends I_DescriptionVersioned> descs = user.getDescriptions();
    			
    			for (I_DescriptionVersioned desc : descs) {
    				if (desc.getTypeNid() == fsnNid || desc.getTypeNid() == rf2FsnNid ||
    					desc.getTypeNid() == prefNid || desc.getTypeNid() == rf2PrefNid) {
	    				existingUserStrings.add(desc.getText().toLowerCase());
    				}
    			}
    		}
    	
	    	if (existingUserStrings.contains(fsn.toLowerCase()) && existingUserStrings.contains(preferred.toLowerCase())) {
	    		// Return Concept
	    		for (I_GetConceptData user : WorkflowHelper.getChildren(parentUserCon)) {
	    			if (WorkflowHelper.getFsn(user).equalsIgnoreCase(fsn) &&
	    				WorkflowHelper.getPrefTerm(user).equalsIgnoreCase(preferred)) {
	    				return user;
	    			}
	    		}
	    	} 
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Cannot access user concepts to add their fsn/pref terms to a storage collection");
		}
		
		return null;
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
            AceLog.getAppLog().log(Level.WARNING, "Failed to create cap queue with exception: " + e.getLocalizedMessage(), e);
        }
    }

    private I_GetConceptData createUser(I_ConfigAceFrame newConfig, I_ConfigAceFrame creatorConfig, I_GetConceptData parentConcept) throws TaskFailedException,
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
            .getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()), creatorConfig);
        tf.newDescription(UUID.randomUUID(), userConcept, "en", newConfig.getUsername(), Terms.get().getConcept(
        		SnomedMetadataRf2.PREFERRED_RF2.getUuids()), creatorConfig);
//        tf.newDescription(UUID.randomUUID(), userConcept, "en", newConfig.getUsername() + ".inbox", Terms.get()
//            .getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids()), creatorConfig);

        // Needs a relationship record...
        tf.newRelationship(UUID.randomUUID(), userConcept, tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
            .getUids()), parentConcept, tf
            .getConcept(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()), tf
            .getConcept(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids()), tf
            .getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()), 0, creatorConfig);
            
        newConfig.getDbConfig().setUserConcept(userConcept);

        tf.addUncommitted(userConcept);
        
        return userConcept;
    }

    private void createDevPath(I_ConfigAceFrame newConfig, I_ConfigAceFrame creatorConfig, String releaseDate, PathBI pathsForView, PathBI pathsForOrigin, PathBI addToPathOrigin) throws Exception {
    	Set<PositionBI> inputSet = new HashSet<PositionBI>();
    	inputSet.add(Terms.get().newPosition(pathsForOrigin, Long.MAX_VALUE));
    	
    	String suffix = " author path";
        String prefName = releaseDate + " " + newConfig.getUsername() + suffix;
        String fsnName = releaseDate + " " + newConfig.getDbConfig().getFullName() + suffix;
        PathBI devPath = createNewPath(newConfig, creatorConfig, inputSet, fsnName, prefName);
        newConfig.addEditingPath(devPath);
        I_GetConceptData devPathConcept = Terms.get().getConcept(devPath.getConceptNid());
        newConfig.setClassifierInputPath(devPathConcept);
        newConfig.getViewPositionSet().add(Terms.get().newPosition(pathsForView, Long.MAX_VALUE));
        newConfig.getDbConfig().setUserPath(devPathConcept);

        ((I_Path)addToPathOrigin).addOrigin(Terms.get().newPosition(devPath, Long.MAX_VALUE),
        		Terms.get().getActiveAceFrameConfig());
    }


    private PathBI createNewPath(I_ConfigAceFrame config, I_ConfigAceFrame commitConfig, 
    		Collection<? extends PositionBI> positionSet,
            String fsnPathName, String prefPathName) throws TaskFailedException, TerminologyException, IOException {
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
        Terms.get().newDescription(UUID.randomUUID(), pathConcept, "en", prefPathName,
        		Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()), 
        		commitConfig);
        Terms.get().newDescription(UUID.randomUUID(), pathConcept, "en", prefPathName,
            Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getUuids()), commitConfig);

        // Needs a relationship record...
        Terms.get().newRelationship(UUID.randomUUID(), pathConcept,
        		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), parentConceptForPath,
        		Terms.get().getConcept(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()),
        		Terms.get().getConcept(SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids()),
        		Terms.get().getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()), 0, commitConfig);

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

    public String getParentConceptForPathPropName() {
        return parentConceptForUserPropName;
    }

    public void setParentConceptForPathPropName(String prop) {
        this.parentConceptForUserPropName = prop;
    }

    public String getParentConceptForUserPropName() {
        return parentConceptForUserPropName;
    }

    public void setParentConceptForUserPropName(String prop) {
        this.parentConceptForUserPropName = prop;
    }

    public String getAddToPathOriginPropName() {
        return addToPathOriginPropName;
    }

    public void setAddToPathOriginPropName(String prop) {
        this.addToPathOriginPropName = prop;
    }

    public String getPathsForViewPropName() {
        return pathsForViewPropName;
    }

    public void setPathsForViewPropName(String prop) {
        this.pathsForViewPropName = prop;
    }

    public String getPathsForOriginPropName() {
        return pathsForOriginPropName;
    }

    public void setPathsForOriginPropName(String prop) {
        this.pathsForOriginPropName = prop;
    }

    public String getReleaseDatePropName() {
        return releaseDatePropName;
    }

    public void setReleaseDatePropName(String prop) {
        this.releaseDatePropName = prop;
    }

    private void retireRel(I_RelTuple rel, I_ConfigAceFrame config) throws TerminologyException, PropertyVetoException {
        try {
            I_RelVersioned relVersioned = rel.getFixedPart();

            for (PathBI editPath : config.getEditingPathSet()) {
                I_RelPart oldPart = relVersioned.getLastTuple().getMutablePart();
                I_RelPart newPart =(I_RelPart) oldPart.makeAnalog(SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
									                              config.getDbConfig().getUserConcept().getNid(),
									                              editPath.getConceptNid(),
									                              Long.MAX_VALUE);
                newPart.setPathNid(oldPart.getPathNid());
                
                if (oldPart.getStatusNid() != ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid() &&
                	oldPart.getStatusNid() != SnomedMetadataRfx.getSTATUS_RETIRED_NID()) {
                    relVersioned.addVersion(newPart);
                }
            }

            I_GetConceptData concept = Terms.get().getConceptForNid(relVersioned.getNid());
            Terms.get().addUncommitted(concept);
        } catch (IOException e1) {
            AceLog.getAppLog().log(Level.WARNING, "Failed to retire existing workflow user rel with exception: " + e1.getLocalizedMessage(), e1);
        }

    }

}