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

import org.apache.lucene.document.Document;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HoldRefsetPreferences;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.I_RefsetDefaultsBoolean;
import org.dwfa.ace.refset.I_RefsetDefaultsConInt;
import org.dwfa.ace.refset.I_RefsetDefaultsConcept;
import org.dwfa.ace.refset.I_RefsetDefaultsInteger;
import org.dwfa.ace.refset.I_RefsetDefaultsString;
import org.dwfa.ace.refset.I_RefsetDefaultsTemplate;
import org.dwfa.ace.refset.I_RefsetsDefaultsConConCon;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.PathCB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.Taxonomies;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.ValidationException;
import org.ihtsdo.ttk.preferences.EnumBasedPreferences;
import org.ihtsdo.ttk.queue.QueueList;
import org.ihtsdo.ttk.queue.QueuePreferences;
import org.ihtsdo.ttk.queue.QueueType;
import org.ihtsdo.ttk.queue.QueueType.Types;
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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import org.ihtsdo.ttk.queue.QueueAddress;

/**
 * Goal which generated the users for the application.
 *
 * @goal generate-users-extended
 *
 * @phase prepare-package
 */
public class GenerateUsersExtended extends AbstractMojo {
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
   private Boolean            createdUsers    = false;
   private boolean            displayRf2      = false;
   private boolean            makeUserDevPath = false;
   private ConceptChronicleBI parentConcept   = null;

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
    * @parameter expression="${project.build.directory}/users/userPermissionRefset.txt"
    */
   private File wfPermissionsFile;

   /**
    *
    * @parameter expression="${project.build.directory}/users/userPermissionRels.txt"
    */
   private File relPermissionsFile;

   /**
    *
    * @parameter expression="${project.build.directory}/users"
    */
   private File defaultUserConfig;

   /**
    *
    * @parameter expression="${project.build.directory}/wb-bundle/config"
    */
   private File configDir;

   /**
    * The name of the parent concept for new users
    *
    * @parameter
    */
   private String userParentConceptName;

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
   private String                 version;
   private File                   userConfigFile;
   private String                 langSortPref = "rf2";
   private String                 langPrefOrder;
   private String                 statedInferredPolicy = "is";
   private ConceptSpec            defaultStatus;
   private ConceptSpec            defaultDescType;
   private ConceptSpec            defaultRelType;
   private ConceptSpec            defaultRelChar;
   private ConceptSpec            defaultRelRefinability;
   private ConceptSpec            module;
   private String                 visibleRefests;
   private String                 projectDevelopmentPathFsn = "Workbench Auxiliary";
   private String                 projectDevelopmentParentPathFsn;
   private String                 projectDevelopmentParentPathUuid;
   private String                 projectDevelopmentOriginPathFsn;
   private String                 projectDevelopmentViewPathFsn = "Workbench Auxiliary";
   private String                 projectDevelopmentAdjPathFsn;
   private String                 hasDevPathAsOriginPathFsn;
   private I_ConfigAceFrame       userConfig;
   private I_ConfigAceFrame       defaultConfig;
   private String                 generateAdjCs = "false";
   private ArrayList<ConceptSpec> cBooleanRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> cConceptRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> cConIntRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> cIntegerRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> cStringRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> cConConConRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> dBooleanRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> dConceptRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> dConIntRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> dIntegerRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> dStringRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> dConConConRefsets = new ArrayList<>();
   private ArrayList<ConceptSpec> destRelTypesList = new ArrayList<>();
   private ArrayList<ConceptSpec> cConceptRefsetStatus = new ArrayList<>();
   private ArrayList<ConceptSpec> cConceptRefsetConTypes = new ArrayList<>();
   private ArrayList<ConceptSpec> dConceptRefsetStatus = new ArrayList<>();
   private ArrayList<ConceptSpec> dConceptRefsetConTypes = new ArrayList<>();
   private ConceptSpec            refsetStatus;
   private ArrayList<ConceptSpec> additionalRoots = new ArrayList<>();
   private HashMap<UUID, Color> pathColors = new HashMap<>();
   private String pathColorProp;

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
         tf.addUncommittedNoChecks(user);
      }
   }

   private void addUserToAddressList(String username) {
      if (username != null) {
         if (userConfig.getAddressesList().contains(username) == false) {
            userConfig.getAddressesList().add(username);
         }
      }
   }

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

   private void createDefaultUserParentConcept()
           throws IOException, ValidationException {
      UUID userParentConceptUUID =
         UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");
      ConceptSpec userParent = new ConceptSpec("user", userParentConceptUUID);

      parentConcept =
         Ts.get().getConcept(userParent.getLenient().getPrimUuid());
   }

   private ConceptChronicleBI createUser()
           throws TerminologyException, IOException,
                  UnsupportedEncodingException, NoSuchAlgorithmException,
                  MojoExecutionException, InvalidCAB, ContradictionException {
      I_ConfigAceDb dbConfig = userConfig.getDbConfig();

      AceLog.getAppLog().info("Create new path for user: "
                              + dbConfig.getFullName());

      if ((dbConfig.getFullName() == null)
              || (dbConfig.getFullName().length() == 0)) {
         JOptionPane.showMessageDialog(
             userConfig.getWorkflowPanel().getTopLevelAncestor(),
             "Full name cannot be empty.");

         throw new MojoExecutionException("Full name of user cannot be empty.");
      }

      I_TermFactory tf = Terms.get();

      // Needs a concept record...
      ConceptCB userConceptBp = new ConceptCB(dbConfig.getFullName(),
                                   userConfig.getUsername(), LANG_CODE.EN,
                                   TermAux.IS_A.getLenient().getPrimUuid(),
                                   parentConcept.getPrimUuid());
      UUID userUuid = userConceptBp.getComponentUuid();

      if (Ts.get().hasUuid(userUuid)) {
         setUserConcept(userUuid.toString());
         addWfRelIfDoesNotExist(userUuid.toString());

         return Ts.get().getConcept(userUuid);
      } else {

         // Needs a description record...
         DescriptionCAB inboxDescBp =
            new DescriptionCAB(userConceptBp.getComponentUuid(),
                               queueDescriptionType.getLenient().getPrimUuid(),
                               LANG_CODE.EN,
                               userConfig.getUsername() + ".inbox", false);

         // add workflow relationship
         RelationshipCAB wfRelBp =
            new RelationshipCAB(userConceptBp.getComponentUuid(),
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

         userConceptBp.addDescriptionCAB(inboxDescBp);
         userConceptBp.setRelationshipCAB(wfRelBp);

         ConceptChronicleBI userConcept = builder.construct(userConceptBp);

         dbConfig.setUserConcept((I_GetConceptData) userConcept);
         Ts.get().addUncommitted(userConcept);

         return userConcept;
      }
   }

   private void createUserParentConcept(boolean getUuidFromFactory)
           throws NoSuchAlgorithmException, UnsupportedEncodingException,
                  IOException, InvalidCAB, ContradictionException,
                  ValidationException {

      // Default parent concept blueprint.
      ConceptCB parentConceptBp = new ConceptCB(userParentConceptName,
                                     userParentConceptName, LANG_CODE.EN_US,
                                     TermAux.IS_A.getLenient().getPrimUuid(),
                                     TermAux.USER.getLenient().getPrimUuid());

      // Choose a strategy for creating the parent concept UUID.
      // Legacy code used Type5UuidFactory, but KP changes
      // merged in subsequently use ConceptCB#getComponentUuid().
      // They are both type 5 UUIDs but have different namespaces.
      UUID parentConceptUuid = null;

      if (getUuidFromFactory) {
         parentConceptUuid =
            Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                 userParentConceptName);
      } else {
         parentConceptUuid = parentConceptBp.getComponentUuid();
      }

      // Get parentConcept, or create one if it doesn't exist.
      if (Ts.get().hasUuid(parentConceptUuid)) {
         parentConcept = Ts.get().getConcept(parentConceptUuid);
      } else {
         TerminologyBuilderBI builder =
            Ts.get().getTerminologyBuilder(
                Ts.get().getMetadataEditCoordinate(),
                Ts.get().getMetadataViewCoordinate());

         parentConcept = builder.construct(parentConceptBp);
         Ts.get().addUncommitted(parentConcept);
         Ts.get().commit();
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
         String firstLine = userReader.readLine();

         if (firstLine != null) {
            getLog().info(
                "****************\n Creating new users \n****************\n");

            String userLine = userReader.readLine();

            if (userLine != null) {
               while (userLine != null) {
                  userConfig = null;

                  String[] parts = userLine.split("\t");

                  if (parts.length == 6) {
                     setupUser(parts[0], parts[1], parts[2], parts[3],
                               parts[4], parts[5], "");
                  }

                  if (parts.length == 7) {
                     setupUser(parts[0], parts[1], parts[2], parts[3],
                               parts[4], parts[5], parts[6]);
                  }

                  userLine = userReader.readLine();
               }

               // add users to wf permissions refset
               I_TermFactory tf = Terms.get();

//               if (createdUsers) {
                  defaultConfig = newProfile(null, null, null, null, null);

                  Set<PathBI> editingPathSet =
                     defaultConfig.getEditingPathSet();

                  editingPathSet.clear();
                  editingPathSet.add(
                      Ts.get().getPath(
                         TermAux.WB_AUX_PATH.getLenient().getNid()));
                  tf.setActiveAceFrameConfig(defaultConfig);

                  ViewCoordinate             vc     =
                     defaultConfig.getViewCoordinate();
                  EditorCategoryRefsetWriter writer =
                     new EditorCategoryRefsetWriter();
                  BufferedReader wfReader =
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
                        columns[0] = (String) columns[0].subSequence(
                           "User permission (".length(), columns[0].length());

                        // remove ")"
                        columns[2] = columns[2].trim();
                        columns[2] =
                           columns[2].substring(0, columns[2].length() - 1);

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
                              writer.retireEditorCategory(
                                  modelers.get(columns[0]), columns[1],
                                  oldCategory);
                           } else {
                              addingRequired = false;
                           }
                        }

                        if (addingRequired) {
                           if (modelers.get(columns[0]) == null) {
                              getLog().info("null found, adding: "
                                            + columns[0]);
                           }

                           writer.setEditor(modelers.get(columns[0]));
                           writer.setSemanticArea(columns[1]);
                           writer.setCategory(newCategory);
                           writer.addMember(true);
                        }
                     }

                     wfLine = wfReader.readLine();
                  }

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

                           addRelPermission(parts[0], parts[1], parts[2],
                                            parts[3], parts[4]);
                           relPermissionLine = br.readLine();
                        }
                     } catch (Exception ex) {
                        throw new TaskFailedException(ex);
                     }
                  } else {
                     getLog().warn("No relPermissionsFile: "
                                   + relPermissionsFile.getAbsolutePath());
                  }
//               }

               Terms.get().commit();
               getLog().info("Starting close.");
               Bdb.close();
               getLog().info("db closed");
            }
         }
      } catch (Exception ex) {
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      } catch (Throwable ex) {
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      }
   }

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

   private void makeUserDevPaths(String username)
           throws NoSuchAlgorithmException, UnsupportedEncodingException,
                  IOException, InvalidCAB, ContradictionException,
                  ValidationException {
      if (!username.equals("username")) {
         UUID editParentPathUuid = null;

         if (!projectDevelopmentParentPathUuid.equals("")) {
            editParentPathUuid =
               UUID.fromString(projectDevelopmentParentPathUuid);
         } else {
            editParentPathUuid =
               Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                    projectDevelopmentParentPathFsn);
         }

         UUID editOriginPathUuid =
            Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                 projectDevelopmentOriginPathFsn);
         UUID originFromDevPathUuid =
            Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                 hasDevPathAsOriginPathFsn);
         PathBI    editPath      = null;
         ConceptCB newEditPathBp = new ConceptCB(username + " dev path",
                                      username + " dev path", LANG_CODE.EN,
                                      TermAux.IS_A.getLenient().getPrimUuid(),
                                      editParentPathUuid);
         UUID userDevPathUuid = newEditPathBp.getComponentUuid();

         if (Ts.get().hasUuid(userDevPathUuid)) {
            editPath =
               Ts.get().getPath(Ts.get().getNidForUuids(userDevPathUuid));
         } else {
            RefexCAB pathRefexBp =
               new RefexCAB(TK_REFEX_TYPE.CID,
                            TermAux.PATH.getLenient().getConceptNid(),
                            RefsetAux.PATH_REFSET.getLenient().getNid());

            pathRefexBp.put(RefexCAB.RefexProperty.UUID1,
                            newEditPathBp.getComponentUuid());
            pathRefexBp.setMemberUuid(UUID.randomUUID());

            RefexCAB pathOriginRefexBp =
               new RefexCAB(TK_REFEX_TYPE.CID_INT,
                            newEditPathBp.getComponentUuid(),
                            RefsetAux.PATH_ORIGIN_REFEST.getLenient().getNid(),
                            null, null);

            pathOriginRefexBp.put(RefexCAB.RefexProperty.UUID1,
                                  editOriginPathUuid);
            pathOriginRefexBp.put(RefexCAB.RefexProperty.INTEGER1,
                                  Integer.MAX_VALUE);
            pathRefexBp.setMemberUuid(UUID.randomUUID());

            RefexCAB pathOriginRefexOtherBp =
               new RefexCAB(TK_REFEX_TYPE.CID_INT, originFromDevPathUuid,
                            RefsetAux.PATH_ORIGIN_REFEST.getLenient().getNid(),
                            null, null);

            pathOriginRefexOtherBp.put(RefexCAB.RefexProperty.UUID1,
                                       newEditPathBp.getComponentUuid());
            pathOriginRefexOtherBp.put(RefexCAB.RefexProperty.INTEGER1,
                                       Integer.MAX_VALUE);
            pathOriginRefexOtherBp.setMemberUuid(UUID.randomUUID());

            TerminologyBuilderBI builder =
               Ts.get().getTerminologyBuilder(
                   Ts.get().getMetadataEditCoordinate(),
                   Ts.get().getMetadataViewCoordinate());
            PathCB pathBp = new PathCB(newEditPathBp, pathRefexBp,
                                       pathOriginRefexBp,
                                       pathOriginRefexOtherBp,
                                       Ts.get().getConcept(editOriginPathUuid));

            editPath = builder.construct(pathBp);
         }

         userConfig.addEditingPath(editPath);
          for (Entry<UUID, Color> entry : pathColors.entrySet()) {
              userConfig.setColorForPath(Ts.get().getNidForUuids(entry.getKey()),
                      entry.getValue());
          }
      }
   }

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
      I_IntList statusPopupTypes = tf.newIntList();

      statusPopupTypes.add(SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid());
      statusPopupTypes.add(
          SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getNid());
      statusPopupTypes.add(
          SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
      statusPopupTypes.add(
          SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
      activeConfig.setEditStatusTypePopup(statusPopupTypes);

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

      if (destRelTypesList != null) {
         for (ConceptSpec relTypeSpec : destRelTypesList) {
            destRelTypes.add(relTypeSpec.getLenient().getConceptNid());
         }
      }

       activeConfig.setDestRelTypes(destRelTypes);
       // set up editing defaults
       activeConfig.setDefaultImageType(
               tf.getConcept(TermAux.AUX_IMAGE.getLenient().getUUIDs()));
       if (defaultDescType != null) {
           activeConfig.setDefaultDescriptionType(
                   tf.getConcept(defaultDescType.getLenient().getUUIDs()));
       }
       if (defaultRelChar != null) {
           activeConfig.setDefaultRelationshipCharacteristic(
                   tf.getConcept(defaultRelChar.getLenient().getUUIDs()));
       }
       if (defaultRelRefinability != null) {
           activeConfig.setDefaultRelationshipRefinability(
                   tf.getConcept(defaultRelRefinability.getLenient().getUUIDs()));
       }
       if (defaultRelType != null) {
           activeConfig.setDefaultRelationshipType(
                   tf.getConcept(defaultRelType.getLenient().getUUIDs()));
       }
       if (defaultStatus != null) {
           activeConfig.setDefaultStatus(
                   tf.getConcept(defaultStatus.getLenient().getUUIDs()));
       }

      // set up refset defaults for editing
      I_HoldRefsetPreferences attribRefsetPref =
         activeConfig.getRefsetPreferencesForToggle(TOGGLES.ATTRIBUTES);
      I_RefsetDefaultsTemplate templatePreferences =
         attribRefsetPref.getTemplatePreferences();
      I_RefsetDefaultsBoolean booleanPreferences =
         attribRefsetPref.getBooleanPreferences();
      I_IntList booleanPopupIds = booleanPreferences.getRefsetPopupIds();

      booleanPopupIds.clear();

      if (!cBooleanRefsets.isEmpty()) {
         booleanPreferences.setDefaultRefset(
             (I_GetConceptData) cBooleanRefsets.get(0).getLenient());
         booleanPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cBooleanRefsets) {
            booleanPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsConcept conceptPreferences =
         attribRefsetPref.getConceptPreferences();
      I_IntList conceptPopupIds = conceptPreferences.getRefsetPopupIds();

      conceptPopupIds.clear();

      if (!cConceptRefsets.isEmpty()) {
         conceptPreferences.setDefaultRefset(
             (I_GetConceptData) cConceptRefsets.get(0).getLenient());
         conceptPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cConceptRefsets) {
            conceptPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_IntList conceptStatusPopupIds = conceptPreferences.getStatusPopupIds();

      conceptStatusPopupIds.clear();

      for (ConceptSpec spec : cConceptRefsetStatus) {
         conceptStatusPopupIds.add(spec.getLenient().getNid());
      }

      I_IntList conceptConceptPopupIds =
         conceptPreferences.getConceptPopupIds();

      conceptConceptPopupIds.clear();

      for (ConceptSpec spec : cConceptRefsetConTypes) {
         conceptConceptPopupIds.add(spec.getLenient().getNid());
      }

      I_RefsetDefaultsInteger integerPreferences =
         attribRefsetPref.getIntegerPreferences();
      I_IntList integerPopupIds = integerPreferences.getRefsetPopupIds();

      integerPopupIds.clear();

      if (!cIntegerRefsets.isEmpty()) {
         integerPreferences.setDefaultRefset(
             (I_GetConceptData) cIntegerRefsets.get(0).getLenient());
         integerPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cIntegerRefsets) {
            integerPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsConInt conIntPreferences =
         attribRefsetPref.getConIntPreferences();
      I_IntList conIntPopupIds = conIntPreferences.getRefsetPopupIds();

      conIntPopupIds.clear();

      if (!cConIntRefsets.isEmpty()) {
         conIntPreferences.setDefaultRefset(
             (I_GetConceptData) cConIntRefsets.get(0).getLenient());
         conIntPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cConIntRefsets) {
            conIntPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsString stringPreferences =
         attribRefsetPref.getStringPreferences();
      I_IntList stringPopupIds = stringPreferences.getRefsetPopupIds();

      stringPopupIds.clear();

      if (!cStringRefsets.isEmpty()) {
         stringPreferences.setDefaultRefset(
             (I_GetConceptData) cStringRefsets.get(0).getLenient());
         stringPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cStringRefsets) {
            stringPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetsDefaultsConConCon conConConPreferences =
         attribRefsetPref.getCidCidCidPreferences();
      I_IntList conConConPopupIds = conConConPreferences.getRefsetPopupIds();

      conConConPopupIds.clear();

      if (!cConConConRefsets.isEmpty()) {
         conConConPreferences.setDefaultRefset(
             (I_GetConceptData) cConConConRefsets.get(0).getLenient());
         conConConPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cConConConRefsets) {
            conConConPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_HoldRefsetPreferences descRefsetPref =
         activeConfig.getRefsetPreferencesForToggle(TOGGLES.DESCRIPTIONS);
      I_RefsetDefaultsBoolean booleanPreferencesDesc =
         descRefsetPref.getBooleanPreferences();
      I_IntList booleanPopupIdsDesc =
         booleanPreferencesDesc.getRefsetPopupIds();

      booleanPopupIdsDesc.clear();

      if (!dBooleanRefsets.isEmpty()) {
         booleanPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dBooleanRefsets.get(0).getLenient());
         booleanPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dBooleanRefsets) {
            booleanPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsConcept conceptPreferencesDesc =
         descRefsetPref.getConceptPreferences();
      I_IntList conceptPopupIdsDesc =
         conceptPreferencesDesc.getRefsetPopupIds();

      conceptPopupIdsDesc.clear();

      if (!dConceptRefsets.isEmpty()) {
         conceptPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dConceptRefsets.get(0).getLenient());
         conceptPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dConceptRefsets) {
            conceptPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

      I_IntList descStatusPopupIds = conceptPreferencesDesc.getStatusPopupIds();

      descStatusPopupIds.clear();

      for (ConceptSpec spec : dConceptRefsetStatus) {
         descStatusPopupIds.add(spec.getLenient().getNid());
      }

      I_IntList descConceptPopupIds =
         conceptPreferencesDesc.getConceptPopupIds();

      descConceptPopupIds.clear();

      for (ConceptSpec spec : dConceptRefsetConTypes) {
         descConceptPopupIds.add(spec.getLenient().getNid());
      }

      I_RefsetDefaultsInteger integerPreferencesDesc =
         descRefsetPref.getIntegerPreferences();
      I_IntList integerPopupIdsDesc =
         integerPreferencesDesc.getRefsetPopupIds();

      integerPopupIdsDesc.clear();

      if (!dIntegerRefsets.isEmpty()) {
         integerPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dIntegerRefsets.get(0).getLenient());
         integerPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dIntegerRefsets) {
            integerPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsConInt conIntPreferencesDesc =
         descRefsetPref.getConIntPreferences();
      I_IntList conIntPopupIdsDesc = conIntPreferencesDesc.getRefsetPopupIds();

      conIntPopupIdsDesc.clear();

      if (!dConIntRefsets.isEmpty()) {
         conIntPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dConIntRefsets.get(0).getLenient());
         conIntPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dConIntRefsets) {
            conIntPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsString stringPreferencesDesc =
         descRefsetPref.getStringPreferences();
      I_IntList stringPopupIdsDesc = stringPreferencesDesc.getRefsetPopupIds();

      stringPopupIdsDesc.clear();

      if (!dStringRefsets.isEmpty()) {
         stringPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dStringRefsets.get(0).getLenient());
         stringPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dStringRefsets) {
            stringPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetsDefaultsConConCon conConConPreferencesDesc =
         descRefsetPref.getCidCidCidPreferences();
      I_IntList conConConPopupIdsDesc =
         conConConPreferencesDesc.getRefsetPopupIds();

      conConConPopupIdsDesc.clear();

      if (!dConConConRefsets.isEmpty()) {
         conConConPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dConConConRefsets.get(0).getLenient());
         conConConPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dConConConRefsets) {
            conConConPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

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
          PathBI viewPath =
                  tf.getPath(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                  this.projectDevelopmentViewPathFsn));
          PositionBI viewPosition = tf.newPosition(viewPath, Long.MAX_VALUE);
          Set<PositionBI> viewSet = new HashSet<PositionBI>();

          viewSet.add(viewPosition);
          activeConfig.setViewPositions(viewSet);
      PathBI editPath = null;

      if (!makeUserDevPath) {
         editPath = tf.getPath(
            Type5UuidFactory.get(
               Type5UuidFactory.PATH_ID_FROM_FS_DESC,
               this.projectDevelopmentPathFsn));
         activeConfig.addEditingPath(editPath);
      }

      if (this.projectDevelopmentAdjPathFsn != null) {
         PathBI adjPath = tf.getPath(
                              Type5UuidFactory.get(
                                 Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                 this.projectDevelopmentAdjPathFsn));

         activeConfig.addPromotionPath(adjPath);
      }

      for(Entry<UUID,Color> entry : pathColors.entrySet()){
          activeConfig.setColorForPath(Ts.get().getNidForUuids(entry.getKey()),
                                         entry.getValue());
      }
              
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
       if (langPrefOrder != null) {
           String[] langPrefConcepts = langPrefOrder.split(";");

           for (String langPref : langPrefConcepts) {
               ConceptSpec lang = getConceptSpecFromPrefs(langPref.trim());

               languagePreferenceList.add(lang.getLenient().getNid());
           }
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
       if (visibleRefests != null) {
           String[] refsets = visibleRefests.split(",");

           for (String r : refsets) {
               String toggle =
                       r.substring(0, r.indexOf(".")).trim();
               String refset =
                       r.substring(r.indexOf(".") + 1).trim();
               I_HostConceptPlugins.TOGGLES t = null;
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
       }

      return activeConfig;
   }

   private boolean processUser(String username, String userUuid, File userDir,
                               File userProfile, boolean getUuidFromFactory)
           throws ValidationException, IOException, TerminologyException,
                  NoSuchAlgorithmException, UnsupportedEncodingException,
                  InvalidCAB, ContradictionException, MojoExecutionException,
                  Exception {
      setActiveFrameConfig(userProfile);

      // setup changesets
      String tempKey = setupChangeSetsAndAddChangeSetGenerator(userDir);

      // make user paths?
      if (makeUserDevPath) {
         makeUserDevPaths(username);
      }

      // Create user parent concept.
      if ((userParentConceptName != null) && (parentConcept == null)) {
         createUserParentConcept(getUuidFromFactory);
      } else if (parentConcept == null) {
         createDefaultUserParentConcept();
      }

      addUserToAddressList(username);

      // Create new concept for user...
      if ((userUuid == null) || userUuid.equals("")) {
         createUser();
      } else {
         setUserConcept(userUuid);
         addWfRelIfDoesNotExist(userUuid);
      }

      List<AlertToDataConstraintFailure> errorsAndWarnings =
         Terms.get().getCommitErrorsAndWarnings();

      if (errorsAndWarnings.size() > 0) {
         AceLog.getAppLog().warning(errorsAndWarnings.toString());
         Terms.get().cancel();

         return false;
      }

      try {
         Terms.get().commit();
      } catch (Exception e) {
         throw new MojoExecutionException(e.getLocalizedMessage(), e);
      } finally {
         Ts.get().removeChangeSetGenerator(tempKey);
      }

      setQueuePreferences(userDir);

      return true;
   }

   private void readUserConfigFile() throws MojoExecutionException {
      BufferedReader configReader = null;

      try {
         configReader = new BufferedReader(new FileReader(userConfigFile));
         configProps.load(configReader);
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
         projectDevelopmentParentPathFsn =
            configProps.getProperty("projectDevelopmentParentPathFsn");
         projectDevelopmentParentPathUuid =
            configProps.getProperty("projectDevelopmentParentPathUuid");
         projectDevelopmentOriginPathFsn =
            configProps.getProperty("projectDevelopmentOriginPathFsn");
         hasDevPathAsOriginPathFsn =
            configProps.getProperty("hasDevPathAsOriginPathFsn");
         cBooleanRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("concept.booleanRefsets"));
         cConceptRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("concept.conceptRefsets"));
         cIntegerRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("concept.integerRefsets"));
         cConIntRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("concept.conIntRefsets"));
         cStringRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("concept.stringRefsets"));
         cConConConRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("concept.conConConRefsets"));
         dBooleanRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("desc.booleanRefsets"));
         dConceptRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("desc.conceptRefsets"));
         dConIntRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("desc.conIntRefsets"));
         dIntegerRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("desc.integerRefsets"));
         dStringRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("desc.stringRefsets"));
         dConConConRefsets = getConceptSpecListFromPrefs(
            configProps.getProperty("desc.conConConRefsets"));
         destRelTypesList = getConceptSpecListFromPrefs(
            configProps.getProperty("parentRelationshipTypes"));
         cConceptRefsetStatus = getConceptSpecListFromPrefs(
            configProps.getProperty("concept.conceptRefsetStatus"));
         cConceptRefsetConTypes = getConceptSpecListFromPrefs(
            configProps.getProperty("concept.conceptConTypes"));
         dConceptRefsetStatus = getConceptSpecListFromPrefs(
            configProps.getProperty("desc.conceptRefsetStatus"));
         dConceptRefsetConTypes = getConceptSpecListFromPrefs(
            configProps.getProperty("desc.conceptConTypes"));

         if ("true".equals(configProps.getProperty("displayRf2"))) {
            displayRf2 = true;
         }

         refsetStatus =
            getConceptSpecFromPrefs(configProps.getProperty("refsetStatus"));
         additionalRoots = getConceptSpecListFromPrefs(
            configProps.getProperty("additionalRoots"));

         if ("true".equals(configProps.getProperty("makeUserDevPath"))) {
            makeUserDevPath = true;
         }

         module        =
            getConceptSpecFromPrefs(configProps.getProperty("module"));
         generateAdjCs = configProps.getProperty("generateAdjCs");
         pathColorProp = configProps.getProperty("pathColors");
         processPathColors();
      } catch (FileNotFoundException ex) {
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      } catch (IOException ex) {
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      } catch (NoSuchAlgorithmException ex) {
           throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
       } finally {
         try {
            configReader.close();
         } catch (IOException ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
         }
      }
   }
   
   private void processPathColors() throws MojoExecutionException, NoSuchAlgorithmException, UnsupportedEncodingException, ValidationException, IOException{
       String[] pathColorProps = pathColorProp.split(";");
       for(String pathColor : pathColorProps){
           String[] parts = pathColor.split(",");
           String fsn = parts[0].trim();
           UUID pathUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, fsn);
           if(fsn.equals(TermAux.SNOMED_CORE_PATH.getDescription())){
               pathUuid = TermAux.SNOMED_CORE_PATH.getLenient().getPrimUuid();
           }
           if(fsn.equals(TermAux.WB_AUX_PATH.getDescription())){
               pathUuid = TermAux.WB_AUX_PATH.getLenient().getPrimUuid();
           }
           int r = Integer.parseInt(parts[1].replace("Color(", "").trim());
           int g = Integer.parseInt(parts[2].trim());
           int b = Integer.parseInt(parts[3].replace(")", "").trim());
           pathColors.put(pathUuid, new Color(r,g,b));
       }
   }

   private String setupChangeSetsAndAddChangeSetGenerator(File userDir) {
      File changeSetRoot = new File(userDir, "changesets");

      getLog().info("** Changeset root: " + changeSetRoot.getAbsolutePath());
      changeSetRoot.mkdirs();

      I_ConfigAceDb dbConfig              = userConfig.getDbConfig();
      File          absoluteChangeSetRoot = new File(wbBundleDir,
                                               PROFILE_ROOT + File.separator
                                               + "user-creation-changesets");

      dbConfig.setChangeSetRoot(changeSetRoot);
      getLog().info("** Changeset root from db config: "
                    + dbConfig.getChangeSetRoot().getAbsolutePath());
      getLog().info("** absoluteChangeSetRoot: "
                    + absoluteChangeSetRoot.getAbsolutePath());
      dbConfig.setChangeSetWriterFileName(userConfig.getUsername() + "#1#"
              + UUID.randomUUID().toString() + ".eccs");
      dbConfig.setUsername(userConfig.getUsername());

      String tempKey       = UUID.randomUUID().toString();
      File   changeSetFile = new File(absoluteChangeSetRoot,
                                      dbConfig.getChangeSetWriterFileName());
      File changeSetTempFile =
         new File(absoluteChangeSetRoot,
                  "#0#" + dbConfig.getChangeSetWriterFileName());
      ChangeSetGeneratorBI generator =
         Ts.get().createDtoChangeSetGenerator(changeSetFile, changeSetTempFile,
            ChangeSetGenerationPolicy.MUTABLE_ONLY);

      Ts.get().addChangeSetGenerator(tempKey, generator);

      return tempKey;
   }

   private boolean setupUser(String fullname, String username, String password,
                             String userUuid, String adminUsername,
                             String adminPassword, String userConfigList)
           throws MojoExecutionException {
      try {
         File userDir = new File(wbBundleDir,
                                 PROFILE_ROOT + File.separator + username);
         File userProfile = new File(userDir, username + ".wb");

         create = !userProfile.exists();

         if (create) {
            createdUsers = true;

            if (userConfigList.equals("")) {
               userConfigFile = new File(defaultUserConfig + File.separator
                                         + "userConfig.txt");
               readUserConfigFile();
               userConfig = newProfile(fullname, username, password,
                                       adminUsername, adminPassword);
            } else {
               String[] userConfigs = userConfigList.split(",");

               for (int i = userConfigs.length - 1; i >= 0; i--) {
                  userConfigFile = new File(defaultUserConfig + File.separator
                                            + userConfigs[i].trim());
                  readUserConfigFile();

                  if (userConfig == null) {    // create new
                     userConfig = newProfile(fullname, username, password,
                                             adminUsername, adminPassword);

                     // When we have multiple configs, do not use factory to create initial UUID.
                     boolean getUuidFromFactory = false;

                     if (!processUser(username, userUuid, userDir, userProfile,
                                      getUuidFromFactory)) {
                        return false;
                     }
                  } else {
                     updateConfig();
                  }

                  writeConfig();
               }

               return true;
            }

            // When we have a single config, use factory to create initial UUID.
            boolean getUuidFromFactory = true;

            if (!processUser(username, userUuid, userDir, userProfile,
                             getUuidFromFactory)) {
               return false;
            }

            writeConfig();
         }

         return true;
      } catch (Exception e) {
         throw new MojoExecutionException(e.getLocalizedMessage(), e);
      }
   }

   public I_ConfigAceFrame updateConfig()
           throws MojoExecutionException, TerminologyException, IOException,
                  NoSuchAlgorithmException, InvalidCAB, ContradictionException {
      I_ImplementTermFactory tf = (I_ImplementTermFactory) Terms.get();

      // status popup values
      I_IntList statusPopupTypes = tf.newIntList();

      statusPopupTypes.add(SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid());
      statusPopupTypes.add(
          SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getNid());
      statusPopupTypes.add(
          SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
      statusPopupTypes.add(
          SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
      userConfig.setEditStatusTypePopup(statusPopupTypes);

      // set up classifier
      userConfig.setClassificationRoot(
          tf.getConcept(Taxonomies.SNOMED.getLenient().getUUIDs()));
      userConfig
         .setClassificationRoleRoot(
             tf.getConcept(
                (new ConceptSpec(
                    "Concept model attribute (attribute)",
                    UUID.fromString("6155818b-09ed-388e-82ce-caa143423e99")))
                       .getLenient().getUUIDs()));
      userConfig.setClassifierInputMode(
          I_ConfigAceFrame.CLASSIFIER_INPUT_MODE_PREF.EDIT_PATH);
      userConfig.setClassifierIsaType(
          (I_GetConceptData) Snomed.IS_A.getLenient());

      // set up taxonomy view roots
      I_IntSet roots = tf.newIntSet();

      roots.add(Taxonomies.REFSET_AUX.getLenient().getNid());
      roots.add(Taxonomies.WB_AUX.getLenient().getNid());
      roots.add(Taxonomies.SNOMED.getLenient().getNid());

      if (additionalRoots != null) {
         if (!additionalRoots.isEmpty()) {
            for (ConceptSpec root : additionalRoots) {
               roots.add(root.getLenient().getNid());
            }
         }
      }

      userConfig.setRoots(roots);

      // set up allowed statuses
      I_IntSet allowedStatus = tf.newIntSet();

      allowedStatus.add(SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid());
      allowedStatus.add(
          SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
      userConfig.setAllowedStatus(allowedStatus);

      // set up parent relationship rel types (view->taxonomy)
      I_IntSet destRelTypes = tf.newIntSet();

      destRelTypes.add(Snomed.IS_A.getLenient().getNid());
      destRelTypes.add(TermAux.IS_A.getLenient().getNid());

      if (destRelTypesList != null) {
         for (ConceptSpec relTypeSpec : destRelTypesList) {
            destRelTypes.add(relTypeSpec.getLenient().getConceptNid());
         }
      }

      userConfig.setDestRelTypes(destRelTypes);

      // set up editing defaults
      userConfig.setDefaultImageType(
          tf.getConcept(TermAux.AUX_IMAGE.getLenient().getUUIDs()));
      userConfig.setDefaultDescriptionType(
          tf.getConcept(defaultDescType.getLenient().getUUIDs()));
      userConfig.setDefaultRelationshipCharacteristic(
          tf.getConcept(defaultRelChar.getLenient().getUUIDs()));
      userConfig.setDefaultRelationshipRefinability(
          tf.getConcept(defaultRelRefinability.getLenient().getUUIDs()));
      userConfig.setDefaultRelationshipType(
          tf.getConcept(defaultRelType.getLenient().getUUIDs()));
      userConfig.setDefaultStatus(
          tf.getConcept(defaultStatus.getLenient().getUUIDs()));

      // set up refset defaults for editing
      I_HoldRefsetPreferences attribRefsetPref =
         userConfig.getRefsetPreferencesForToggle(TOGGLES.ATTRIBUTES);
      I_RefsetDefaultsTemplate templatePreferences =
         attribRefsetPref.getTemplatePreferences();
      I_RefsetDefaultsBoolean booleanPreferences =
         attribRefsetPref.getBooleanPreferences();
      I_IntList booleanPopupIds = booleanPreferences.getRefsetPopupIds();

      if (!cBooleanRefsets.isEmpty()) {
         booleanPreferences.setDefaultRefset(
             (I_GetConceptData) cBooleanRefsets.get(0).getLenient());
         booleanPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cBooleanRefsets) {
            booleanPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsConcept conceptPreferences =
         attribRefsetPref.getConceptPreferences();
      I_IntList conceptPopupIds = conceptPreferences.getRefsetPopupIds();

      if (!cConceptRefsets.isEmpty()) {
         conceptPreferences.setDefaultRefset(
             (I_GetConceptData) cConceptRefsets.get(0).getLenient());
         conceptPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cConceptRefsets) {
            conceptPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_IntList conceptStatusPopupIds = conceptPreferences.getStatusPopupIds();

      for (ConceptSpec spec : cConceptRefsetStatus) {
         conceptStatusPopupIds.add(spec.getLenient().getNid());
      }

      I_IntList conceptConceptPopupIds =
         conceptPreferences.getConceptPopupIds();

      for (ConceptSpec spec : cConceptRefsetConTypes) {
         conceptConceptPopupIds.add(spec.getLenient().getNid());
      }

      I_RefsetDefaultsInteger integerPreferences =
         attribRefsetPref.getIntegerPreferences();
      I_IntList integerPopupIds = integerPreferences.getRefsetPopupIds();

      if (!cIntegerRefsets.isEmpty()) {
         integerPreferences.setDefaultRefset(
             (I_GetConceptData) cIntegerRefsets.get(0).getLenient());
         integerPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cIntegerRefsets) {
            integerPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsConInt conIntPreferences =
         attribRefsetPref.getConIntPreferences();
      I_IntList conIntPopupIds = conIntPreferences.getRefsetPopupIds();

      if (!cConIntRefsets.isEmpty()) {
         conIntPreferences.setDefaultRefset(
             (I_GetConceptData) cConIntRefsets.get(0).getLenient());
         conIntPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cConIntRefsets) {
            conIntPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsString stringPreferences =
         attribRefsetPref.getStringPreferences();
      I_IntList stringPopupIds = stringPreferences.getRefsetPopupIds();

      if (!cStringRefsets.isEmpty()) {
         stringPreferences.setDefaultRefset(
             (I_GetConceptData) cStringRefsets.get(0).getLenient());
         stringPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cStringRefsets) {
            stringPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetsDefaultsConConCon conConConPreferences =
         attribRefsetPref.getCidCidCidPreferences();
      I_IntList conConConPopupIds = conConConPreferences.getRefsetPopupIds();

      if (!cConConConRefsets.isEmpty()) {
         conConConPreferences.setDefaultRefset(
             (I_GetConceptData) cConConConRefsets.get(0).getLenient());
         conConConPreferences.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : cConConConRefsets) {
            conConConPopupIds.add(spec.getLenient().getConceptNid());
         }
      }

      I_HoldRefsetPreferences descRefsetPref =
         userConfig.getRefsetPreferencesForToggle(TOGGLES.DESCRIPTIONS);
      I_RefsetDefaultsBoolean booleanPreferencesDesc =
         descRefsetPref.getBooleanPreferences();
      I_IntList booleanPopupIdsDesc =
         booleanPreferencesDesc.getRefsetPopupIds();

      if (!dBooleanRefsets.isEmpty()) {
         booleanPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dBooleanRefsets.get(0).getLenient());
         booleanPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dBooleanRefsets) {
            booleanPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsConcept conceptPreferencesDesc =
         descRefsetPref.getConceptPreferences();
      I_IntList conceptPopupIdsDesc =
         conceptPreferencesDesc.getRefsetPopupIds();

      if (!dConceptRefsets.isEmpty()) {
         conceptPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dConceptRefsets.get(0).getLenient());
         conceptPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dConceptRefsets) {
            conceptPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

      I_IntList descStatusPopupIds = conceptPreferencesDesc.getStatusPopupIds();

      for (ConceptSpec spec : dConceptRefsetStatus) {
         descStatusPopupIds.add(spec.getLenient().getNid());
      }

      I_IntList descConceptPopupIds =
         conceptPreferencesDesc.getConceptPopupIds();

      for (ConceptSpec spec : dConceptRefsetConTypes) {
         descConceptPopupIds.add(spec.getLenient().getNid());
      }

      I_RefsetDefaultsInteger integerPreferencesDesc =
         descRefsetPref.getIntegerPreferences();
      I_IntList integerPopupIdsDesc =
         integerPreferencesDesc.getRefsetPopupIds();

      if (!dIntegerRefsets.isEmpty()) {
         integerPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dIntegerRefsets.get(0).getLenient());
         integerPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dIntegerRefsets) {
            integerPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsConInt conIntPreferencesDesc =
         descRefsetPref.getConIntPreferences();
      I_IntList conIntPopupIdsDesc = conIntPreferencesDesc.getRefsetPopupIds();

      if (!dConIntRefsets.isEmpty()) {
         conIntPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dConIntRefsets.get(0).getLenient());
         conIntPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dConIntRefsets) {
            conIntPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetDefaultsString stringPreferencesDesc =
         descRefsetPref.getStringPreferences();
      I_IntList stringPopupIdsDesc = stringPreferencesDesc.getRefsetPopupIds();

      if (!dStringRefsets.isEmpty()) {
         stringPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dStringRefsets.get(0).getLenient());
         stringPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dStringRefsets) {
            stringPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

      I_RefsetsDefaultsConConCon conConConPreferencesDesc =
         descRefsetPref.getCidCidCidPreferences();
      I_IntList conConConPopupIdsDesc =
         conConConPreferencesDesc.getRefsetPopupIds();

      if (!dConConConRefsets.isEmpty()) {
         conConConPreferencesDesc.setDefaultRefset(
             (I_GetConceptData) dConConConRefsets.get(0).getLenient());
         conConConPreferencesDesc.setDefaultStatusForRefset(
             (I_GetConceptData) refsetStatus.getLenient());

         for (ConceptSpec spec : dConConConRefsets) {
            conConConPopupIdsDesc.add(spec.getLenient().getConceptNid());
         }
      }

      // set up label display prefs
      I_IntList treeDescPrefList       = userConfig.getTreeDescPreferenceList();
      I_IntList shortLabelDescPrefList =
         userConfig.getShortLabelDescPreferenceList();
      I_IntList longLabelDescPrefList =
         userConfig.getLongLabelDescPreferenceList();
      I_IntList tableDescPrefList = userConfig.getTableDescPreferenceList();

      if (displayRf2) {
         treeDescPrefList.add(
             SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
         treeDescPrefList.add(
             SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
         treeDescPrefList
            .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1
               .getLenient().getNid());
         treeDescPrefList
            .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
               .getNid());
         shortLabelDescPrefList.add(
             SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
         shortLabelDescPrefList.add(
             SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
         shortLabelDescPrefList
            .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1
               .getLenient().getNid());
         shortLabelDescPrefList
            .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
               .getNid());
         longLabelDescPrefList.add(
             SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
         longLabelDescPrefList.add(
             SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
         longLabelDescPrefList
            .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
               .getNid());
         longLabelDescPrefList
            .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1
               .getLenient().getNid());
         tableDescPrefList.add(
             SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
         tableDescPrefList.add(
             SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
         tableDescPrefList
            .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1
               .getLenient().getNid());
         tableDescPrefList
            .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
               .getNid());
      } else {
         treeDescPrefList
            .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1
               .getLenient().getNid());
         treeDescPrefList
            .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
               .getNid());
         shortLabelDescPrefList
            .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1
               .getLenient().getNid());
         shortLabelDescPrefList
            .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
               .getNid());
         longLabelDescPrefList
            .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
               .getNid());
         longLabelDescPrefList
            .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1
               .getLenient().getNid());
         tableDescPrefList
            .add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1
               .getLenient().getNid());
         tableDescPrefList
            .add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient()
               .getNid());
      }

      // set up paths
      PathBI editPath = null;

      // can only make user dev path during profil creation, and not during config update.
      if (!makeUserDevPath) {
         editPath = tf.getPath(
            Type5UuidFactory.get(
               Type5UuidFactory.PATH_ID_FROM_FS_DESC,
               this.projectDevelopmentPathFsn));
         userConfig.addEditingPath(editPath);
      }

      PathBI viewPath =
         tf.getPath(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                         this.projectDevelopmentViewPathFsn));
      PositionBI      viewPosition = tf.newPosition(viewPath, Long.MAX_VALUE);
      Set<PositionBI> viewSet      = new HashSet<PositionBI>();

      viewSet.add(viewPosition);
      userConfig.setViewPositions(viewSet);

      if (this.projectDevelopmentAdjPathFsn != null) {
         PathBI adjPath = tf.getPath(
                              Type5UuidFactory.get(
                                 Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                 this.projectDevelopmentAdjPathFsn));

         userConfig.addPromotionPath(adjPath);
      }

      for(Entry<UUID,Color> entry : pathColors.entrySet()){
          userConfig.setColorForPath(Ts.get().getNidForUuids(entry.getKey()),
                                         entry.getValue());
      }

      // set up toggles
      userConfig.setSubversionToggleVisible(false);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.ID, true);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.ATTRIBUTES, true);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.DESCRIPTIONS, true);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.SOURCE_RELS, true);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.DEST_RELS, true);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.LINEAGE, true);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.LINEAGE_GRAPH, false);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.IMAGE, true);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.CONFLICT, true);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.STATED_INFERRED, false);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.PREFERENCES, true);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.HISTORY, true);
      userConfig.setTogglesInComponentPanelVisible(
          I_HostConceptPlugins.TOGGLES.REFSETS, true);
      userConfig.setPrecedence(Precedence.PATH);

      // set up vewing preferences

      /*
       * langSortPref preference options are: rf2 refex (rf2), language refex
       * (lr), type before language (tl), language before type (lt)
       */
      if (langSortPref.equals("rf2")) {
         userConfig.setLanguageSortPref(
             I_ConfigAceFrame.LANGUAGE_SORT_PREF.RF2_LANG_REFEX);
      } else if (langSortPref.equals("lr")) {
         userConfig.setLanguageSortPref(
             I_ConfigAceFrame.LANGUAGE_SORT_PREF.LANG_REFEX);
      } else if (langSortPref.equals("tl")) {
         userConfig.setLanguageSortPref(
             I_ConfigAceFrame.LANGUAGE_SORT_PREF.TYPE_B4_LANG);
      } else if (langSortPref.equals("lt")) {
         userConfig.setLanguageSortPref(
             I_ConfigAceFrame.LANGUAGE_SORT_PREF.LANG_B4_TYPE);
      } else {
         throw new MojoExecutionException("Can't handle value:  "
                                          + langSortPref
                                          + " for preference: langSortPref");
      }

      I_IntList languagePreferenceList = userConfig.getLanguagePreferenceList();

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
         userConfig.setRelAssertionType(RelAssertionType.STATED);
      } else if (statedInferredPolicy.equals("i")) {
         userConfig.setRelAssertionType(RelAssertionType.INFERRED);
      } else if (statedInferredPolicy.equals("is")) {
         userConfig.setRelAssertionType(RelAssertionType.INFERRED_THEN_STATED);
      } else {
         throw new MojoExecutionException(
             "Can't handle value:  " + statedInferredPolicy
             + " for preference: statedInferredPolicy");
      }

      userConfig.setShowViewerImagesInTaxonomy(true);

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

         userConfig.setRefsetInToggleVisible(refsetType, t, true);
      }

      return userConfig;
   }

   private void writeConfig() throws FileNotFoundException, IOException {
      I_ConfigAceDb dbConfig = userConfig.getDbConfig();

      getLog().info("** Before write: " + dbConfig.getUserConcept());

      File profileFile = dbConfig.getProfileFile();

      getLog().info("** User Profile File: " + profileFile.getAbsolutePath());

      FileOutputStream   fos = new FileOutputStream(profileFile);
      ObjectOutputStream oos = new ObjectOutputStream(fos);

      oos.writeObject(dbConfig);
      oos.close();
   }

   private ConceptSpec getConceptSpecFromPrefs(String configString) {
      if ((configString != null) &&!configString.equals("")) {
         String prefTerm = configString.substring(configString.indexOf("(")
                              + 1, configString.indexOf(","));
         String uuidString = configString.substring(configString.indexOf(",")
                                + 1, configString.lastIndexOf(")"));

         return new ConceptSpec(prefTerm.trim(),
                                UUID.fromString(uuidString.trim()));
      } else {
         return null;
      }
   }

   private ArrayList<ConceptSpec> getConceptSpecListFromPrefs(
           String configString) {
      ArrayList<ConceptSpec> conceptSpecList = new ArrayList<ConceptSpec>();

      if ((configString != null) &&!configString.equals("")) {
         String[] conceptSpecs = configString.split(";");

         if (conceptSpecs.length > 0) {
            for (String spec : conceptSpecs) {
               conceptSpecList.add(getConceptSpecFromPrefs(spec));
            }
         }
      }

      return conceptSpecList;
   }

   private void setActiveFrameConfig(File userProfile)
           throws ValidationException, IOException, TerminologyException {
      userConfig.getDbConfig().setProfileFile(userProfile);

      UUID moduleUuid = module.getLenient().getPrimUuid();

      userConfig.setModuleNid(Ts.get().getNidForUuids(moduleUuid));
      Terms.get().setActiveAceFrameConfig(userConfig);
   }

   private void setInboxQueuePreferences(QueueList queueList, String inboxName,
           File userQueueRoot)
           throws IOException, Exception {
      String           id             = UUID.randomUUID().toString();
      File             queueDirectory = new File(userQueueRoot, inboxName);
      QueueType        queueType      = new QueueType(Types.INBOX);
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
      QueueType        queueType      = new QueueType(Types.OUTBOX);
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
