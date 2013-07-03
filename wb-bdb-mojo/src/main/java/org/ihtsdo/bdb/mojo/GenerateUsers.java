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



package org.ihtsdo.bdb.mojo;

//~--- non-JDK imports --------------------------------------------------------

import static org.dwfa.bpa.util.AppInfoProperties.ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.VERSION;

import org.apache.lucene.document.Document;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.AppInfoProperties;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.tk.binding.snomed.*;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.ttk.preferences.EnumBasedPreferences;
import org.ihtsdo.ttk.queue.QueueList;
import org.ihtsdo.ttk.queue.QueuePreferences;
import org.ihtsdo.ttk.queue.QueueType;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import org.ihtsdo.ttk.queue.QueueAddress;

/**
 * Goal which generated the users for the application.
 *
 * @goal generate-users
 *
 * @phase prepare-package
 */
public class GenerateUsers extends AbstractMojo {
   private static final String               QUEUE_ROOT           = "queues";
   private static final String               PROFILE_ROOT         = "profiles";
   private EditorCategoryRefsetSearcher      searcher             = null;
   private HashMap<String, ConceptVersionBI> modelers             = null;
   private Properties                        configProps          =
      new Properties();
   private Boolean                           create               = true;
   private ConceptSpec                       queueDescriptionType =
      new ConceptSpec("user inbox",
                      UUID.fromString("f7e96652-a844-31a3-96b4-7ba3b0807233"));

   /**
    * Berkeley directory.
    *
    * @parameter expression="${project.build.directory}/wb-bundle/berkeley-db"
    */
   private File berkeleyDir;

   /**
    * @parameter expression="${project.build.directory}/wb-bundle"
    */
   private File wbBundleDir;

   /**
    *
    * @parameter expression="${project.build.directory}/users/users.txt"
    */
   private File usersFile;

   /**
    *
    * @parameter
    * expression="${project.build.directory}/users/userPermissionRefset.txt"
    */
   private File wfPermissionsFile;

   /**
    *
    * @parameter expression="${project.build.directory}/users/userPermissionRels.txt"
    */
   private File relPermissionsFile;

   /**
    *
    * @parameter expression="${project.build.directory}/users/userConfig.txt"
    */
   private File defaultUserConfig;

   /**
    *
    * @parameter expression="${project.build.directory}/wb-bundle/config"
    */
   private File configDir;

   /**
    * The groupId to use when constructing a user preference appPrefix.
    *
    * @parameter expression="${project.groupId}"
    */
   private String groupId;

   /**
    * The artifactId to use when constructing a user preference appPrefix.
    *
    * @parameter expression="${project.artifactId}"
    */
   private String artifactId;

   /**
    * The version to use when constructing a user preference appPrefix.
    *
    * @parameter expression="${project.version}"
    */
   private String           version;
   private String           langSortPref;
   private String           langPrefOrder;
   private String           statedInferredPolicy;
   private ConceptSpec      defaultStatus;
   private ConceptSpec      defaultDescType;
   private ConceptSpec      defaultRelType;
   private ConceptSpec      defaultRelChar;
   private ConceptSpec      defaultRelRefinability;
   private ConceptSpec      module;
   private String           visibleRefests;
   private String           projectDevelopmentPathFsn;
   private String           projectDevelopmentViewPathFsn;
   private String           projectDevelopmentAdjPathFsn;
   private I_ConfigAceFrame userConfig;
   private I_ConfigAceFrame wfConfig;
   private String           generateAdjCs;

   private void addRelPermission(String userName, String typeUid,
                                 String typeName, String targetUid,
                                 String targetName)
           throws Exception {
      I_TermFactory    tf      = Terms.get();
      I_ConfigAceFrame config  = tf.getActiveAceFrameConfig();
      I_GetConceptData user    = null;
      SearchResult     results = tf.doLuceneSearch(userName);

      for (int i = 0; i < results.topDocs.scoreDocs.length; i++) {
         Document doc  = results.searcher.doc(results.topDocs.scoreDocs[i].doc);
         int      cnid = Integer.parseInt(doc.get("cnid"));
         int      dnid = Integer.parseInt(doc.get("dnid"));

         // getLog().info(doc);
         I_DescriptionVersioned<?> foundDescription = tf.getDescription(dnid);

         if (foundDescription.getTuples(config.getConflictResolutionStrategy())
                 .iterator().next().getText().equals(userName)) {
            user = tf.getConcept(cnid);

            break;
         }
      }

      if (user == null) {

         // throw new Exception("User unknown");
         // skip line
         getLog().warn("User not found:" + userName + " for rel permission");
      } else {
         getLog().info("Creating permission for user " + user.toString()
                       + " if not current");

         RelationshipCAB relCab = new RelationshipCAB(user.getPrimUuid(),
                                     UUID.fromString(typeUid),
                                     UUID.fromString(targetUid), 0,
                                     TkRelationshipType.STATED_ROLE);

         Ts.get().getTerminologyBuilder(
             config.getEditCoordinate(),
             config.getViewCoordinate()).constructIfNotCurrent(relCab);

         // old way
         // tf.newRelationship(UUID.randomUUID(), user,
         // tf.getConcept(UUID.fromString(typeUid)),
         // tf.getConcept(UUID.fromString(targetUid)),
         // tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
         // tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
         // tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()),
         // 0, config);
         tf.addUncommittedNoChecks(user);
      }
   }
/*
   private void addWfRelIfDoesNotExist(String userUuidString)
           throws TerminologyException, IOException, InvalidCAB,
                  ContradictionException {
      I_GetConceptData userConcept =
         Terms.get().getConcept(UUID.fromString(userUuidString));
      int wfNid =
         Terms.get().uuidToNative(
             ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getUids());
      boolean found = false;

      for (I_RelVersioned rel : userConcept.getSourceRels()) {
         if (rel.getTargetNid() == wfNid) {
            found = true;
         }
      }

      if (!found) {
         RelationshipCAB wfRelBp =
            new RelationshipCAB(userConcept.getPrimUuid(),
                                ArchitectonicAuxiliary.Concept
                                   .WORKFLOW_EDITOR_STATUS
                                   .getPrimoridalUid(), ArchitectonicAuxiliary
                                   .Concept.WORKFLOW_ACTIVE_MODELER
                                   .getPrimoridalUid(), 0,
                                      TkRelationshipType.STATED_ROLE);
         ViewCoordinate vc    = userConfig.getViewCoordinate();
         EditCoordinate oldEc = userConfig.getEditCoordinate();
         EditCoordinate ec    = new EditCoordinate(oldEc.getAuthorNid(),
                                   oldEc.getModuleNid(),
                                   TermAux.WB_AUX_PATH.getLenient().getNid());
         TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(ec, vc);

         builder.construct(wfRelBp);
      }

      Ts.get().addUncommitted(userConcept);
   }
*/
   private ConceptChronicleBI createUser()
           throws TerminologyException, IOException,
                  UnsupportedEncodingException, NoSuchAlgorithmException,
                  MojoExecutionException, InvalidCAB, ContradictionException {
//      AceLog.getAppLog().info("Create new path for user: "
//                              + userConfig.getDbConfig().getFullName());

      if ((userConfig.getDbConfig().getFullName() == null)
              || (userConfig.getDbConfig().getFullName().length() == 0)) {
         JOptionPane.showMessageDialog(
             userConfig.getWorkflowPanel().getTopLevelAncestor(),
             "Full name cannot be empty.");

         throw new MojoExecutionException("Full name of user cannot be empty.");
      }

      I_TermFactory tf = Terms.get();

      // Needs a concept record...
      UUID userUuid =
         Type5UuidFactory.get(Type5UuidFactory.USER_FULLNAME_NAMESPACE,
                              userConfig.getDbConfig().getFullName());
      ConceptCB userConceptBp;

      if (Ts.get().hasUuid(userUuid)) {
         setUserConcept(userUuid.toString());
         // addWfRelIfDoesNotExist(userUuid.toString());

         return Ts.get().getConcept(userUuid);
      } else {
         ConceptSpec userParent =
            new ConceptSpec(
                "user",
                UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));

         userConceptBp = new ConceptCB(userConfig.getDbConfig().getFullName(),
                                       userConfig.getUsername(), LANG_CODE.EN,
                                       TermAux.IS_A.getLenient().getPrimUuid(),
                                       userParent.getLenient().getPrimUuid());

         // Needs a description record...
         DescriptionCAB inboxDescBp =
            new DescriptionCAB(userConceptBp.getComponentUuid(),
                               queueDescriptionType.getLenient().getPrimUuid(),
                               LANG_CODE.EN,
                               userConfig.getUsername() + ".inbox", false);

         // add workflow relationship
//         RelationshipCAB wfRelBp =
//            new RelationshipCAB(userConceptBp.getComponentUuid(),
//                                ArchitectonicAuxiliary.Concept
//                                   .WORKFLOW_EDITOR_STATUS
//                                   .getPrimoridalUid(), ArchitectonicAuxiliary
//                                   .Concept.WORKFLOW_ACTIVE_MODELER
//                                   .getPrimoridalUid(), 0,
//                                      TkRelationshipType.STATED_ROLE);
         ViewCoordinate vc    = userConfig.getViewCoordinate();
         EditCoordinate oldEc = userConfig.getEditCoordinate();
         EditCoordinate ec    = new EditCoordinate(oldEc.getAuthorNid(),
                                   oldEc.getModuleNid(),
                                   TermAux.WB_AUX_PATH.getLenient().getNid());
         TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(ec, vc);

         userConceptBp.addDescriptionCAB(inboxDescBp);
         //userConceptBp.setRelationshipCAB(wfRelBp);

         ConceptChronicleBI userConcept = builder.construct(userConceptBp);

         userConfig.getDbConfig().setUserConcept(
             (I_GetConceptData) userConcept);
         Ts.get().addUncommitted(userConcept);

         return userConcept;
      }
   }

   @Override
   public void execute() throws MojoExecutionException {
      executeMojo();
   }

   void executeMojo() throws MojoExecutionException {
      try {
         getLog().info(
             "****************\n Creating new users \n****************\n");
         Bdb.selectJeProperties(berkeleyDir, berkeleyDir);
         Bdb.setup(berkeleyDir.getAbsolutePath());

         // create user based on profile config
         BufferedReader userReader =
            new BufferedReader(new FileReader(usersFile));

         userReader.readLine();

         String userLine = userReader.readLine();

         while (userLine != null) {
            String[] parts = userLine.split("\t");

            if (parts.length == 6) {
               readConfigFile("");
               setupUser(parts[0], parts[1], parts[2], parts[3], parts[4],
                         parts[5]);
            }

            if (parts.length == 7) {
               readConfigFile(parts[6]);
               setupUser(parts[0], parts[1], parts[2], parts[3], parts[4],
                         parts[5]);
            }

            userLine = userReader.readLine();
         }

         // add users to wf permissions refset
/*
          I_TermFactory tf = Terms.get();
 

         wfConfig = newProfile(null, null, null, null, null);

         Set<PathBI> editingPathSet = wfConfig.getEditingPathSet();

         editingPathSet.clear();
         editingPathSet.add(
             Ts.get().getPath(TermAux.WB_AUX_PATH.getLenient().getNid()));
         tf.setActiveAceFrameConfig(wfConfig);

         ViewCoordinate             vc       = wfConfig.getViewCoordinate();
         EditorCategoryRefsetWriter writer   = new EditorCategoryRefsetWriter();
         BufferedReader             wfReader =
            new BufferedReader(new FileReader(wfPermissionsFile));

         WorkflowHelper.updateModelers(vc);
         modelers = WorkflowHelper.getModelers();
         searcher = new EditorCategoryRefsetSearcher();
         wfReader.readLine();

         String wfLine = wfReader.readLine();

NEXT_WHILE:
         while (wfLine != null) {
            if (wfLine.trim().length() == 0) {
               wfLine = wfReader.readLine();

               continue NEXT_WHILE;
            }

            String[] columns = wfLine.split(",");

            if (columns.length >= 3) {

               // Get rid of "User permission"
               columns[0] =
                  (String) columns[0].subSequence("User permission (".length(),
                                                  columns[0].length());

               // remove ")"
               columns[2] = columns[2].trim();
               columns[2] = columns[2].substring(0, columns[2].length() - 1);

               int i = 0;

               for (String c : columns) {
                  columns[i++] = c.split("=")[1].trim();
               }

               ConceptVersionBI newCategory =
                  WorkflowHelper.lookupEditorCategory(columns[2], vc);
               ConceptVersionBI oldCategory =
                  identifyExistingEditorCategory(columns, vc);
               boolean addingRequired = true;

               if (oldCategory != null) {
                  if (!oldCategory.equals(newCategory)) {
                     writer.retireEditorCategory(modelers.get(columns[0]),
                                                 columns[1], oldCategory);
                  } else {
                     addingRequired = false;
                  }
               }

               if (addingRequired) {
                  if (modelers.get(columns[0]) == null) {
                     getLog().info("null found, adding: " + columns[0]);
                  }

                  writer.setEditor(modelers.get(columns[0]));
                  writer.setSemanticArea(columns[1]);
                  writer.setCategory(newCategory);
                  writer.addMember(true);
               }
            }

            wfLine = wfReader.readLine();
         }
*/
         getLog().info("Starting rels permissions creation");

         if (relPermissionsFile.exists()) {
            try {
               FileReader     fr = new FileReader(relPermissionsFile);
               BufferedReader br = new BufferedReader(fr);

               br.readLine();

               String relPermissionLine = br.readLine();

               getLog().info("Looking at lines...");

               while (relPermissionLine != null) {
                  String[] parts = relPermissionLine.split("\t");

                  addRelPermission(parts[0], parts[1], parts[2], parts[3],
                                   parts[4]);
                  relPermissionLine = br.readLine();
               }
            } catch (Exception ex) {
               throw new TaskFailedException(ex);
            }
         } else {
            getLog().warn("No relPermissionsFile: "
                          + relPermissionsFile.getAbsolutePath());
         }

         Terms.get().commit();
         getLog().info("Starting close.");
         Bdb.close();
         getLog().info("db closed");

         // Finally, export the groupId, artifactId, version to a properties file.
         // This will be queried when the workbench launches to load user pref's.
         exportAppInfoProperties();
      } catch (Exception ex) {
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      } catch (Throwable ex) {
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      }
   }

   private void exportAppInfoProperties()
           throws FileNotFoundException, IOException {
      Properties appInfoProperties = new Properties();

      // Set workbench build properties.
      appInfoProperties.setProperty(GROUP_ID, groupId);
      appInfoProperties.setProperty(ARTIFACT_ID, artifactId);
      appInfoProperties.setProperty(VERSION, version);

      // Write out to file.
      File   profileRoot           = new File(wbBundleDir, PROFILE_ROOT);
      File   appInfoPropertiesFile = new File(profileRoot,
                                        "appinfo.properties");
      String comment               = "App Info";

      appInfoProperties.storeToXML(new FileOutputStream(appInfoPropertiesFile),
                                   comment);
   }

   /*
   private ConceptVersionBI identifyExistingEditorCategory(String[] columns,
           ViewCoordinate vc) {
      try {
         return searcher.searchForCategoryByModelerAndTag(
             modelers.get(columns[0]), columns[1], vc);
      } catch (Exception e) {
         AceLog.getAppLog().log(
             Level.WARNING,
             "Failed to identify existing categories for mod: " + columns[0]
             + " and semTag: " + columns[1], e);
      }

      return null;
   }
*/
   public I_ConfigAceFrame newProfile(String fullName, String username,
                                      String password, String adminUsername,
                                      String adminPassword)
           throws MojoExecutionException, TerminologyException, IOException,
                  NoSuchAlgorithmException {
      I_ImplementTermFactory tf           =
         (I_ImplementTermFactory) Terms.get();
      I_ConfigAceFrame       activeConfig = tf.newAceFrameConfig();

      for (I_HostConceptPlugins.HOST_ENUM h :
              I_HostConceptPlugins.HOST_ENUM.values()) {
         for (I_PluginToConceptPanel plugin :
                 activeConfig.getDefaultConceptPanelPluginsForEditor()) {
            activeConfig.addConceptPanelPlugins(h, plugin.getId(), plugin);
         }
      }

      I_ConfigAceDb newDbProfile = tf.newAceDbConfig();

      newDbProfile.setUsername(username);
      newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
      newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
      newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
      newDbProfile.setChangeSetWriterThreading(
          ChangeSetWriterThreading.SINGLE_THREAD);

      if (generateAdjCs.equalsIgnoreCase("true")) {
         newDbProfile.setAdjudicationWorkListChangeSetPolicy(
             ChangeSetPolicy.INCREMENTAL);
      } else {
         newDbProfile.setAdjudicationWorkListChangeSetPolicy(
             ChangeSetPolicy.OFF);
      }

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

      // status popup values
      /*
      I_IntList statusPopupTypes = tf.newIntList();

      statusPopupTypes.add(SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid());
      statusPopupTypes.add(
          SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getNid());
      statusPopupTypes.add(
          SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
      statusPopupTypes.add(
          SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
      activeConfig.setEditStatusTypePopup(statusPopupTypes);
       */
      
      // set up classifier
      activeConfig.setClassificationRoot(
          tf.getConcept(Taxonomies.SNOMED.getLenient().getUUIDs()));
      activeConfig
         .setClassificationRoleRoot(
             tf.getConcept(
                (new ConceptSpec(
                    "Concept model attribute (attribute)",
                    UUID.fromString("6155818b-09ed-388e-82ce-caa143423e99")))
                       .getLenient().getUUIDs()));
      activeConfig.setClassifierInputMode(
          I_ConfigAceFrame.CLASSIFIER_INPUT_MODE_PREF.EDIT_PATH);
      activeConfig.setClassifierIsaType(
          (I_GetConceptData) Snomed.IS_A.getLenient());

      // set up taxonomy view roots
      I_IntSet roots = tf.newIntSet();

      roots.add(Taxonomies.REFSET_AUX.getLenient().getNid());
      roots.add(Taxonomies.WB_AUX.getLenient().getNid());
      roots.add(Taxonomies.SNOMED.getLenient().getNid());
      activeConfig.setRoots(roots);

      // set up allowed statuses
      I_IntSet allowedStatus = tf.newIntSet();

      allowedStatus.add(SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid());
      allowedStatus.add(
          SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
      activeConfig.setAllowedStatus(allowedStatus);

      // set up parent relationship rel types (view->taxonomy)
      I_IntSet destRelTypes = tf.newIntSet();

      destRelTypes.add(Snomed.IS_A.getLenient().getNid());
      destRelTypes.add(TermAux.IS_A.getLenient().getNid());
      activeConfig.setDestRelTypes(destRelTypes);

      // set up editing defaults
      activeConfig.setDefaultImageType(
          tf.getConcept(TermAux.AUX_IMAGE.getLenient().getUUIDs()));
      activeConfig.setDefaultDescriptionType(
          tf.getConcept(defaultDescType.getLenient().getUUIDs()));
      activeConfig.setDefaultRelationshipCharacteristic(
          tf.getConcept(defaultRelChar.getLenient().getUUIDs()));
      activeConfig.setDefaultRelationshipRefinability(
          tf.getConcept(defaultRelRefinability.getLenient().getUUIDs()));
      activeConfig.setDefaultRelationshipType(
          tf.getConcept(defaultRelType.getLenient().getUUIDs()));
      activeConfig.setDefaultStatus(
          tf.getConcept(defaultStatus.getLenient().getUUIDs()));

      // set up label display prefs
      I_IntList treeDescPrefList = activeConfig.getTreeDescPreferenceList();

      treeDescPrefList.add(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
      treeDescPrefList.add(
          SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
      treeDescPrefList
         .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient()
            .getNid());
      treeDescPrefList
         .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
            .getNid());

      I_IntList shortLabelDescPrefList =
         activeConfig.getShortLabelDescPreferenceList();

      shortLabelDescPrefList.add(
          SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
      shortLabelDescPrefList.add(
          SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
      shortLabelDescPrefList
         .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient()
            .getNid());
      shortLabelDescPrefList
         .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
            .getNid());

      I_IntList longLabelDescPrefList =
         activeConfig.getLongLabelDescPreferenceList();

      longLabelDescPrefList.add(
          SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
      longLabelDescPrefList.add(
          SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
      longLabelDescPrefList
         .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
            .getNid());
      longLabelDescPrefList
         .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient()
            .getNid());

      I_IntList tableDescPrefList = activeConfig.getTableDescPreferenceList();

      tableDescPrefList
         .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient()
            .getNid());
      tableDescPrefList
         .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
            .getNid());
      tableDescPrefList.add(
          SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
      tableDescPrefList.add(
          SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());

      // set up paths
      PathBI editPath =
         tf.getPath(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                         this.projectDevelopmentPathFsn));

      activeConfig.addEditingPath(editPath);

      PathBI viewPath =
         tf.getPath(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                         this.projectDevelopmentViewPathFsn));
      PositionBI      viewPosition = tf.newPosition(viewPath, Long.MAX_VALUE);
      Set<PositionBI> viewSet      = new HashSet<PositionBI>();

      viewSet.add(viewPosition);
      activeConfig.setViewPositions(viewSet);

      if (this.projectDevelopmentAdjPathFsn != null) {
         PathBI adjPath = tf.getPath(
                              Type5UuidFactory.get(
                                 Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                 this.projectDevelopmentAdjPathFsn));

         activeConfig.addPromotionPath(adjPath);
      }

      activeConfig.setColorForPath(viewPath.getConceptNid(),
                                   new Color(255, 84, 27));
      activeConfig.setColorForPath(
          ReferenceConcepts.TERM_AUXILIARY_PATH.getNid(),
          new Color(25, 178, 63));
      activeConfig.setColorForPath(
          Ts.get().getNidForUuids(
             ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()), new Color(
             81, 23, 255));

      // set up toggles
      activeConfig.setSubversionToggleVisible(false);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.ID, true);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.ATTRIBUTES, true);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.DESCRIPTIONS, true);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.SOURCE_RELS, true);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.DEST_RELS, true);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.LINEAGE, true);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.LINEAGE_GRAPH, false);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.IMAGE, true);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.CONFLICT, true);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.STATED_INFERRED, false);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.PREFERENCES, true);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.HISTORY, true);
      activeConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.REFSETS, true);
      activeConfig.setPrecedence(Precedence.PATH);

      // set up vewing preferences

      /*
       * langSortPref preference options are: rf2 refex (rf2), language refex
       * (lr), type before language (tl), language before type (lt)
       */
      if (langSortPref.equals("rf2")) {
         activeConfig.setLanguageSortPref(
             I_ConfigAceFrame.LANGUAGE_SORT_PREF.RF2_LANG_REFEX);
      } else if (langSortPref.equals("lr")) {
         activeConfig.setLanguageSortPref(
             I_ConfigAceFrame.LANGUAGE_SORT_PREF.LANG_REFEX);
      } else if (langSortPref.equals("tl")) {
         activeConfig.setLanguageSortPref(
             I_ConfigAceFrame.LANGUAGE_SORT_PREF.TYPE_B4_LANG);
      } else if (langSortPref.equals("lt")) {
         activeConfig.setLanguageSortPref(
             I_ConfigAceFrame.LANGUAGE_SORT_PREF.LANG_B4_TYPE);
      } else {
         throw new MojoExecutionException("Can't handle value:  "
                                          + langSortPref
                                          + " for preference: langSortPref");
      }

      I_IntList languagePreferenceList =
         activeConfig.getLanguagePreferenceList();

      languagePreferenceList.clear();

      String[] langPrefConcepts = langPrefOrder.split(";");

      for (String langPref : langPrefConcepts) {
         ConceptSpec lang = getConceptSpecFromPrefs(langPref.trim());

         languagePreferenceList.add(lang.getLenient().getNid());
      }

      /*
       * statedInferredPolicy preference options: stated (s), inferred (i),
       * inferred then stated (is)
       */
      if (statedInferredPolicy.equals("s")) {
         activeConfig.setRelAssertionType(RelAssertionType.STATED);
      } else if (statedInferredPolicy.equals("i")) {
         activeConfig.setRelAssertionType(RelAssertionType.INFERRED);
      } else if (statedInferredPolicy.equals("is")) {
         activeConfig.setRelAssertionType(
             RelAssertionType.INFERRED_THEN_STATED);
      } else {
         throw new MojoExecutionException(
             "Can't handle value:  " + statedInferredPolicy
             + " for preference: statedInferredPolicy");
      }

      activeConfig.setShowViewerImagesInTaxonomy(true);

      // set up enabled refsets

      /*
       * Available components: concept, description
       *
       * Available types: boolean,concept, conInt, string, integer, conConCon
       */
      String[] refsets = visibleRefests.split(",");

      for (String r : refsets) {
         String                            toggle     =
            r.substring(0, r.indexOf(".")).trim();
         String                            refset     =
            r.substring(r.indexOf(".") + 1).trim();
         I_HostConceptPlugins.TOGGLES      t          = null;
         I_HostConceptPlugins.REFSET_TYPES refsetType = null;

         if (toggle.equals("concept")) {
            t = I_HostConceptPlugins.TOGGLES.ATTRIBUTES;
         } else if (toggle.equals("desc")) {
            t = I_HostConceptPlugins.TOGGLES.DESCRIPTIONS;
         } else {
            throw new MojoExecutionException(
                "Can't handle value:  " + toggle
                + " For preference: visibleRefsets.component");
         }

         if (refset.equals("boolean")) {
            refsetType = I_HostConceptPlugins.REFSET_TYPES.BOOLEAN;
         } else if (refset.equals("concept")) {
            refsetType = I_HostConceptPlugins.REFSET_TYPES.CONCEPT;
         } else if (refset.equals("conInt")) {
            refsetType = I_HostConceptPlugins.REFSET_TYPES.CON_INT;
         } else if (refset.equals("string")) {
            refsetType = I_HostConceptPlugins.REFSET_TYPES.STRING;
         } else if (refset.equals("integer")) {
            refsetType = I_HostConceptPlugins.REFSET_TYPES.INTEGER;
         } else if (refset.equals("conConCon")) {
            refsetType = I_HostConceptPlugins.REFSET_TYPES.CID_CID_CID;
         } else {
            throw new MojoExecutionException(
                "Can't handle value:  " + refset
                + " For preference: visibleRefsets.type");
         }

         activeConfig.setRefsetInToggleVisible(refsetType, t, true);
      }

      return activeConfig;
   }

   private void readConfigFile(String userConfig)
           throws FileNotFoundException, IOException {
      if (userConfig == "") {
         BufferedReader configReader =
            new BufferedReader(new FileReader(defaultUserConfig));

         configProps.load(configReader);
      } else {
         String userConfigFile = defaultUserConfig.getPath();

         userConfigFile = userConfigFile.replace("userConfig.txt", userConfig);

         File           configFile   = new File(userConfigFile);
         BufferedReader configReader =
            new BufferedReader(new FileReader(configFile));

         configProps.load(configReader);
      }

      langSortPref         = configProps.getProperty("langSortPref");
      langPrefOrder        = configProps.getProperty("langPrefOrder");
      statedInferredPolicy = configProps.getProperty("statedInferredPolicy");
      defaultStatus        =
         getConceptSpecFromPrefs(configProps.getProperty("defaultStatus"));
      defaultDescType =
         getConceptSpecFromPrefs(configProps.getProperty("defaultDescType"));
      defaultRelChar =
         getConceptSpecFromPrefs(configProps.getProperty("defaultRelChar"));
      defaultRelType =
         getConceptSpecFromPrefs(configProps.getProperty("defaultRelType"));
      defaultRelRefinability = getConceptSpecFromPrefs(
         configProps.getProperty("defaultRelRefinability"));
      visibleRefests            = configProps.getProperty("visibleRefests");
      projectDevelopmentPathFsn =
         configProps.getProperty("projectDevelopmentPathFsn");
      projectDevelopmentViewPathFsn =
         configProps.getProperty("projectDevelopmentViewPathFsn");
      projectDevelopmentAdjPathFsn =
         configProps.getProperty("projectDevelopmentAdjPathFsn");
      module        =
         getConceptSpecFromPrefs(configProps.getProperty("module"));
      generateAdjCs = configProps.getProperty("generateAdjCs");
   }

   private boolean setupUser(String fullname, String username, String password,
                             String userUuid, String adminUsername,
                             String adminPassword)
           throws MojoExecutionException {
      try {
         File userDir = new File(wbBundleDir,
                                 "profiles" + File.separator + username);
         File userProfile = new File(userDir, username + ".wb");

         create = !userProfile.exists();

         if (create) {
            File userQueueRoot = new File(wbBundleDir,
                                          "queues" + File.separator + username);

            userConfig = newProfile(fullname, username, password,
                                    adminUsername, adminPassword);
            userConfig.getDbConfig().setProfileFile(userProfile);

            UUID moduleUuid = module.getLenient().getPrimUuid();

            userConfig.setModuleNid(Ts.get().getNidForUuids(moduleUuid));
            Terms.get().setActiveAceFrameConfig(userConfig);

            if (username != null) {
               if (userConfig.getAddressesList().contains(username) == false) {
                  userConfig.getAddressesList().add(username);
               }
            }

            userQueueRoot.mkdirs();

            // Create new concept for user...
            if ((userUuid == null) || userUuid.equals("")) {
               createUser();
            } else {
               ConceptChronicleBI concept =
                  Ts.get().getConcept(UUID.fromString(userUuid));

               setUserConcept(userUuid);
               //addWfRelIfDoesNotExist(userUuid);
            }

            List<AlertToDataConstraintFailure> errorsAndWarnings =
               Terms.get().getCommitErrorsAndWarnings();

            if (errorsAndWarnings.size() > 0) {
               AceLog.getAppLog().warning(errorsAndWarnings.toString());
               Terms.get().cancel();

               return false;
            }

            File changeSetRoot = new File(userDir, "changesets");

            getLog().info("** Changeset root: "
                          + changeSetRoot.getAbsolutePath());
            changeSetRoot.mkdirs();

            I_ConfigAceDb newDbProfile          = userConfig.getDbConfig();
            File          absoluteChangeSetRoot =
               new File(wbBundleDir, "profiles/user-creation-changesets");

            newDbProfile.setChangeSetRoot(changeSetRoot);
            getLog().info("** Changeset root from db config: "
                          + newDbProfile.getChangeSetRoot().getAbsolutePath());
            getLog().info("** absoluteChangeSetRoot: "
                          + absoluteChangeSetRoot.getAbsolutePath());
            newDbProfile.setChangeSetWriterFileName(userConfig.getUsername()
                    + "#1#" + UUID.randomUUID().toString() + ".eccs");
            newDbProfile.setUsername(userConfig.getUsername());

            String               tempKey   = UUID.randomUUID().toString();
            ChangeSetGeneratorBI generator =
               Ts.get()
                  .createDtoChangeSetGenerator(
                     new File(
                        absoluteChangeSetRoot,
                        newDbProfile.getChangeSetWriterFileName()), new File(
                           absoluteChangeSetRoot,
                           "#0#"
                           + newDbProfile
                              .getChangeSetWriterFileName()), ChangeSetGenerationPolicy
                                 .MUTABLE_ONLY);
            List<ChangeSetGeneratorBI> extraGeneratorList =
               new ArrayList<ChangeSetGeneratorBI>();

            extraGeneratorList.add(generator);
            Ts.get().addChangeSetGenerator(tempKey, generator);

            try {
               Terms.get().commit();
            } catch (Exception e) {
               throw new MojoExecutionException(e.getLocalizedMessage(), e);
            } finally {
               Ts.get().removeChangeSetGenerator(tempKey);
            }

            setQueuePreferences(userDir);
            getLog().info("** Before write: "
                          + userConfig.getDbConfig().getUserConcept());

            File test = userConfig.getDbConfig().getProfileFile();

            getLog().info("** User Profile File: " + test.getAbsolutePath());

            FileOutputStream fos =
               new FileOutputStream(userConfig.getDbConfig().getProfileFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(userConfig.getDbConfig());
            oos.close();
         }

         return true;
      } catch (Exception e) {
         throw new MojoExecutionException(e.getLocalizedMessage(), e);
      }
   }

   private ConceptSpec getConceptSpecFromPrefs(String configString) {
      String prefTerm = configString.substring(configString.indexOf("(") + 1,
                           configString.indexOf(","));
      String uuidString = configString.substring(configString.indexOf(",") + 1,
                             configString.lastIndexOf(")"));

      return new ConceptSpec(prefTerm.trim(),
                             UUID.fromString(uuidString.trim()));
   }

   private void setInboxQueuePreferences(QueueList queueList, String inboxName,
           File userQueueRoot)
           throws IOException, Exception {
      String           id             = UUID.randomUUID().toString();
      File             queueDirectory = new File(userQueueRoot, inboxName);
      QueueType        queueType      = new QueueType(QueueType.Types.INBOX);
      QueuePreferences queuePrefs     = new QueuePreferences(inboxName, id,
                                           queueDirectory, Boolean.FALSE,
                                           queueType);
      queuePrefs.getServiceItemProperties().add(new QueueAddress(inboxName));

      queueList.getQueuePreferences().add(queuePrefs);
   }

   private void setOutboxQueuePreferences(QueueList queueList,
           String outboxName, File userQueueRoot)
           throws IOException, Exception {
      String           id             = UUID.randomUUID().toString();
      File             queueDirectory = new File(userQueueRoot, outboxName);
      QueueType        queueType      = new QueueType(QueueType.Types.OUTBOX);
      QueuePreferences queuePrefs     = new QueuePreferences(outboxName, id,
                                           queueDirectory, Boolean.FALSE,
                                           queueType);
      queuePrefs.getServiceItemProperties().add(new QueueAddress(outboxName));

      queueList.getQueuePreferences().add(queuePrefs);
   }

   private void setQueuePreferences(File userDir)
           throws IOException, Exception {
      String userName = userConfig.getUsername();

      // Create queue root directory (at WB build time).
      File queueRoot     = new File(wbBundleDir, QUEUE_ROOT);
      File userQueueRoot = new File(queueRoot, userName);

      getLog().info("** userQueueRoot: " + userQueueRoot);
      userQueueRoot.mkdirs();

      // Specify individual queues (to be created at WB run time).
      QueueList queueList = new QueueList();

      // Set inbox prefs.
      String inboxName = userName + ".inbox";

      new File(userQueueRoot, inboxName).mkdirs();
      setInboxQueuePreferences(queueList, inboxName, userQueueRoot);

      // Set todo box prefs.
      String todoBoxName = userName + ".todo";

      new File(userQueueRoot, todoBoxName).mkdirs();
      setInboxQueuePreferences(queueList, todoBoxName, userQueueRoot);

      // Set outbox prefs.
      String outboxName = userName + ".outbox";

      new File(userQueueRoot, outboxName).mkdirs();
      setOutboxQueuePreferences(queueList, outboxName, userQueueRoot);
      getLog().info("** queueList: " + queueList);

      // Export queue list to user preferences.
      String appPrefix = EnumBasedPreferences.getDefaultAppPrefix(groupId,
                            artifactId, version, userName);
      EnumBasedPreferences prefs = new EnumBasedPreferences(appPrefix);

      queueList.exportFields(prefs);

      // Write user preferences to file.
      File userPrefsFile = new File(userDir, "Preferences.xml");

      getLog().info("** userPrefsFile: " + userPrefsFile);
      prefs.exportSubtree(new FileOutputStream(userPrefsFile));

      // Java Preference API will also store these preferences on
      // the build server.  Clean them up here.
      prefs.removeNode();
      prefs.flush();
   }

   private void setUserConcept(String userUuidString)
           throws TerminologyException, IOException, InvalidCAB,
                  ContradictionException {
      I_GetConceptData userConcept =
         Terms.get().getConcept(UUID.fromString(userUuidString));
      I_TermFactory  tf          = Terms.get();
      DescriptionCAB inboxDescBp =
         new DescriptionCAB(userConcept.getPrimUuid(),
                            queueDescriptionType.getLenient().getPrimUuid(),
                            LANG_CODE.EN, userConfig.getUsername() + ".inbox",
                            false);
      ViewCoordinate vc    = userConfig.getViewCoordinate();
      EditCoordinate oldEc = userConfig.getEditCoordinate();
      EditCoordinate ec    = new EditCoordinate(oldEc.getAuthorNid(),
                                oldEc.getModuleNid(),
                                TermAux.WB_AUX_PATH.getLenient().getNid());
      TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(ec, vc);

      builder.construct(inboxDescBp);
      userConfig.getDbConfig().setUserConcept(userConcept);
      Ts.get().addUncommitted(userConcept);
   }
}
