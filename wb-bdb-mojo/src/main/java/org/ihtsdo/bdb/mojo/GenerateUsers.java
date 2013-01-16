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

import java.awt.Color;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.entry.Entry;

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
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.*;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.queue.QueueServer;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
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
import org.ihtsdo.tk.binding.snomed.*;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * Goal which generated the users for the application.
 *
 * @goal generate-users
 *
 * @phase prepare-package
 */
public class GenerateUsers extends AbstractMojo {

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
     * :WAS: "${project.build.directory}/users/userConfig.txt"
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
    private File userConfigFile;
    private EditorCategoryRefsetSearcher searcher = null;
    private HashMap<String, ConceptVersionBI> modelers = null;
    private Properties configProps = new Properties();
    private String langSortPref;
    private String langPrefOrder;
    private String statedInferredPolicy;
    private ConceptSpec defaultStatus;
    private ConceptSpec defaultDescType;
    private ConceptSpec defaultRelType;
    private ConceptSpec defaultRelChar;
    private ConceptSpec defaultRelRefinability;
    private ConceptSpec module;
    private String visibleRefests;
    private String projectDevelopmentPathFsn;
    private String projectDevelopmentParentPathFsn;
    private String projectDevelopmentParentPathUuid;
    private String projectDevelopmentOriginPathFsn;
    private String projectDevelopmentViewPathFsn;
    private String projectDevelopmentAdjPathFsn;
    private String hasDevPathAsOriginPathFsn;
    private I_ConfigAceFrame userConfig;
    private I_ConfigAceFrame defaultConfig;
    private Boolean create = true;
    private String generateAdjCs;
    private ConceptSpec queueDescriptionType = new ConceptSpec("user inbox", UUID.fromString("f7e96652-a844-31a3-96b4-7ba3b0807233"));
    private Boolean createdUsers = false;
    private ArrayList<ConceptSpec> cBooleanRefsets;
    private ArrayList<ConceptSpec> cConceptRefsets;
    private ArrayList<ConceptSpec> cConIntRefsets;
    private ArrayList<ConceptSpec> cIntegerRefsets;
    private ArrayList<ConceptSpec> cStringRefsets;
    private ArrayList<ConceptSpec> cConConConRefsets;
    private ArrayList<ConceptSpec> dBooleanRefsets;
    private ArrayList<ConceptSpec> dConceptRefsets;
    private ArrayList<ConceptSpec> dConIntRefsets;
    private ArrayList<ConceptSpec> dIntegerRefsets;
    private ArrayList<ConceptSpec> dStringRefsets;
    private ArrayList<ConceptSpec> dConConConRefsets;
    private ArrayList<ConceptSpec> destRelTypesList;
    private ArrayList<ConceptSpec> cConceptRefsetStatus;
    private ArrayList<ConceptSpec> cConceptRefsetConTypes;
    private ArrayList<ConceptSpec> dConceptRefsetStatus;
    private ArrayList<ConceptSpec> dConceptRefsetConTypes;
    private boolean displayRf2 = false;
    private ConceptSpec refsetStatus;
    private ArrayList<ConceptSpec> additionalRoots;
    private boolean makeUserDevPath = false;
    private ConceptChronicleBI parentConcept = null;

    @Override
    public void execute() throws MojoExecutionException {
        executeMojo();

    }

    void executeMojo() throws MojoExecutionException {

        try {
            getLog().info("****************\n Creating new users \n****************\n");
            Bdb.selectJeProperties(berkeleyDir,
                    berkeleyDir);

            Bdb.setup(berkeleyDir.getAbsolutePath());

            //get config properties

            /*
             * LIST OF CONFIG PROPERTIES: langSortPref, langPrefOrder,
             * statedInferredPolicy, defaultStatus, defaultDescType,
             * defaultRelType, defaultRelChar, defaultRelRefinability,
             * visibleRefests, editPath, viewPath
             */
//            BufferedReader configReader = new BufferedReader(new FileReader(defaultUserConfig  + File.separator + "userConfig.txt"));
//            configProps.load(configReader);
//            langSortPref = configProps.getProperty("langSortPref");
//            langPrefOrder = configProps.getProperty("langPrefOrder");
//            statedInferredPolicy = configProps.getProperty("statedInferredPolicy");
//            defaultStatus = getConceptSpecFromPrefs(configProps.getProperty("defaultStatus"));
//            defaultDescType = getConceptSpecFromPrefs(configProps.getProperty("defaultDescType"));
//            defaultRelChar = getConceptSpecFromPrefs(configProps.getProperty("defaultRelChar"));
//            defaultRelType = getConceptSpecFromPrefs(configProps.getProperty("defaultRelType"));
//            defaultRelRefinability = getConceptSpecFromPrefs(configProps.getProperty("defaultRelRefinability"));
//            visibleRefests = configProps.getProperty("visibleRefests");
//            projectDevelopmentPathFsn = configProps.getProperty("projectDevelopmentPathFsn");
//            projectDevelopmentViewPathFsn = configProps.getProperty("projectDevelopmentViewPathFsn");
//            projectDevelopmentAdjPathFsn = configProps.getProperty("projectDevelopmentAdjPathFsn");
//            module = getConceptSpecFromPrefs(configProps.getProperty("module"));
//            generateAdjCs = configProps.getProperty("generateAdjCs");

            //create user based on profile config
            BufferedReader userReader = new BufferedReader(new FileReader(usersFile));
            String firstLine = userReader.readLine();
            if (firstLine != null) {
                getLog().info("****************\n Creating new users \n****************\n");
                String userLine = userReader.readLine();
                if (userLine != null) {
                    while (userLine != null) {
                        userConfig = null;
                        String[] parts = userLine.split("\t");
                        if (parts.length == 6) {
                            setupUser(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], "");
                        }
                        if (parts.length == 7) {
                            setupUser(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
                        }
                        userLine = userReader.readLine();
                    }

                    //add users to wf permissions refset
                    I_TermFactory tf = Terms.get();
                    if (createdUsers) {
                        defaultConfig = newProfile(null, null, null, null, null);
                        Set<PathBI> editingPathSet = defaultConfig.getEditingPathSet();
                        editingPathSet.clear();
                        editingPathSet.add(Ts.get().getPath(TermAux.WB_AUX_PATH.getLenient().getNid()));
                        tf.setActiveAceFrameConfig(defaultConfig);
                        ViewCoordinate vc = defaultConfig.getViewCoordinate();
                        EditorCategoryRefsetWriter writer = new EditorCategoryRefsetWriter();

                        BufferedReader wfReader = new BufferedReader(new FileReader(wfPermissionsFile));

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
                                //Get rid of "User permission"
                                columns[0] = (String) columns[0].subSequence("User permission (".length(), columns[0].length());
                                //remove ")"
                                columns[2] = columns[2].trim();
                                columns[2] = columns[2].substring(0, columns[2].length() - 1);

                                int i = 0;
                                for (String c : columns) {
                                    columns[i++] = c.split("=")[1].trim();
                                }
                                ConceptVersionBI newCategory = WorkflowHelper.lookupEditorCategory(columns[2], vc);
                                ConceptVersionBI oldCategory = identifyExistingEditorCategory(columns, vc);
                                boolean addingRequired = true;

                                if (oldCategory != null) {
                                    if (!oldCategory.equals(newCategory)) {
                                        writer.retireEditorCategory(modelers.get(columns[0]), columns[1], oldCategory);
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
                        getLog().info("Starting rels permissions creation");
                        if (relPermissionsFile.exists()) {
                            try {
                                FileReader fr = new FileReader(relPermissionsFile);
                                BufferedReader br = new BufferedReader(fr);

                                br.readLine();

                                String relPermissionLine = br.readLine();
                                getLog().info("Looking at lines...");
                                while (relPermissionLine != null) {
                                    String[] parts = relPermissionLine.split("\t");

                                    addRelPermission(parts[0], parts[1], parts[2], parts[3], parts[4]);
                                    relPermissionLine = br.readLine();
                                }
                            } catch (Exception ex) {
                                throw new TaskFailedException(ex);
                            }
                        } else {
                            getLog().warn("No relPermissionsFile: " + relPermissionsFile.getAbsolutePath());
                        }
                    }

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

    private void readUserConfigFile() throws MojoExecutionException {
        BufferedReader configReader = null;
        try {
            configReader = new BufferedReader(new FileReader(userConfigFile));
            configProps.load(configReader);
            langSortPref = configProps.getProperty("langSortPref");
            langPrefOrder = configProps.getProperty("langPrefOrder");
            statedInferredPolicy = configProps.getProperty("statedInferredPolicy");
            defaultStatus = getConceptSpecFromPrefs(configProps.getProperty("defaultStatus"));
            defaultDescType = getConceptSpecFromPrefs(configProps.getProperty("defaultDescType"));
            defaultRelChar = getConceptSpecFromPrefs(configProps.getProperty("defaultRelChar"));
            defaultRelType = getConceptSpecFromPrefs(configProps.getProperty("defaultRelType"));
            defaultRelRefinability = getConceptSpecFromPrefs(configProps.getProperty("defaultRelRefinability"));
            visibleRefests = configProps.getProperty("visibleRefests");
            projectDevelopmentPathFsn = configProps.getProperty("projectDevelopmentPathFsn");
            projectDevelopmentViewPathFsn = configProps.getProperty("projectDevelopmentViewPathFsn");
            projectDevelopmentAdjPathFsn = configProps.getProperty("projectDevelopmentAdjPathFsn");
            projectDevelopmentParentPathFsn = configProps.getProperty("projectDevelopmentParentPathFsn");
            projectDevelopmentParentPathUuid = configProps.getProperty("projectDevelopmentParentPathUuid");
            projectDevelopmentOriginPathFsn = configProps.getProperty("projectDevelopmentOriginPathFsn");
            hasDevPathAsOriginPathFsn = configProps.getProperty("hasDevPathAsOriginPathFsn");
            cBooleanRefsets = getConceptSpecListFromPrefs(configProps.getProperty("concept.booleanRefsets"));
            cConceptRefsets = getConceptSpecListFromPrefs(configProps.getProperty("concept.conceptRefsets"));
            cIntegerRefsets = getConceptSpecListFromPrefs(configProps.getProperty("concept.integerRefsets"));
            cConIntRefsets = getConceptSpecListFromPrefs(configProps.getProperty("concept.conIntRefsets"));
            cStringRefsets = getConceptSpecListFromPrefs(configProps.getProperty("concept.stringRefsets"));
            cConConConRefsets = getConceptSpecListFromPrefs(configProps.getProperty("concept.conConConRefsets"));
            dBooleanRefsets = getConceptSpecListFromPrefs(configProps.getProperty("desc.booleanRefsets"));
            dConceptRefsets = getConceptSpecListFromPrefs(configProps.getProperty("desc.conceptRefsets"));
            dConIntRefsets = getConceptSpecListFromPrefs(configProps.getProperty("desc.conIntRefsets"));
            dIntegerRefsets = getConceptSpecListFromPrefs(configProps.getProperty("desc.integerRefsets"));
            dStringRefsets = getConceptSpecListFromPrefs(configProps.getProperty("desc.stringRefsets"));
            dConConConRefsets = getConceptSpecListFromPrefs(configProps.getProperty("desc.conConConRefsets"));
            destRelTypesList = getConceptSpecListFromPrefs(configProps.getProperty("parentRelationshipTypes"));
            cConceptRefsetStatus = getConceptSpecListFromPrefs(configProps.getProperty("concept.conceptRefsetStatus"));
            cConceptRefsetConTypes = getConceptSpecListFromPrefs(configProps.getProperty("concept.conceptConTypes"));
            dConceptRefsetStatus = getConceptSpecListFromPrefs(configProps.getProperty("desc.conceptRefsetStatus"));
            dConceptRefsetConTypes = getConceptSpecListFromPrefs(configProps.getProperty("desc.conceptConTypes"));
            if ("true".equals(configProps.getProperty("displayRf2"))) {
                displayRf2 = true;
            }
            refsetStatus = getConceptSpecFromPrefs(configProps.getProperty("refsetStatus"));
            additionalRoots = getConceptSpecListFromPrefs(configProps.getProperty("additionalRoots"));
            if ("true".equals(configProps.getProperty("makeUserDevPath"))) {
                makeUserDevPath = true;
            }
            module = getConceptSpecFromPrefs(configProps.getProperty("module"));
            generateAdjCs = configProps.getProperty("generateAdjCs");

        } catch (FileNotFoundException ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        } catch (IOException ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        } finally {
            try {
                configReader.close();
            } catch (IOException ex) {
                throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
            }
        }
    }

    private void addRelPermission(String userName, String typeUid, String typeName,
            String targetUid, String targetName) throws Exception {
        I_TermFactory tf = Terms.get();
        I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

        I_GetConceptData user = null;
        SearchResult results = tf.doLuceneSearch(userName);
        for (int i = 0; i < results.topDocs.scoreDocs.length; i++) {
            Document doc = results.searcher.doc(results.topDocs.scoreDocs[i].doc);
            int cnid = Integer.parseInt(doc.get("cnid"));
            int dnid = Integer.parseInt(doc.get("dnid"));
            //getLog().info(doc);
            I_DescriptionVersioned<?> foundDescription = tf.getDescription(dnid);
            if (foundDescription.getTuples(
                    config.getConflictResolutionStrategy()).iterator().next().getText().equals(userName)) {
                user = tf.getConcept(cnid);
                break;
            }
        }
        if (user == null) {
            //throw new Exception("User unknown");
            //skip line
            getLog().warn("User not found:" + userName + " for rel permission");
        } else {
            getLog().info("Creating permission for user " + user.toString() + " if not current");
            RelationshipCAB relCab = new RelationshipCAB(user.getPrimUuid(), UUID.fromString(typeUid),
                    UUID.fromString(targetUid), 0, TkRelationshipType.STATED_ROLE);
            Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate()).constructIfNotCurrent(relCab);
            //			old way
            //			tf.newRelationship(UUID.randomUUID(), user, 
            //					tf.getConcept(UUID.fromString(typeUid)), 
            //					tf.getConcept(UUID.fromString(targetUid)), 
            //					tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), 
            //					tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
            //					tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
            //					0, config);
            tf.addUncommittedNoChecks(user);
        }
    }

    private boolean setupUser_trek145_Original(String fullname, String username, String password, String userUuid,
            String adminUsername, String adminPassword)
            throws MojoExecutionException {
        try {
            File userDir = new File(wbBundleDir, "profiles" + File.separator + username);
            File userProfile = new File(userDir, username + ".wb");
            create = !userProfile.exists();
            if (create) {
                File userQueueRoot = new File(wbBundleDir, "queues" + File.separator + username);

                userConfig = newProfile(fullname, username, password, adminUsername,
                        adminPassword);
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
                if (userUuid == null || userUuid.equals("")) {
                    createUser();
                } else {
                    ConceptChronicleBI concept = Ts.get().getConcept(UUID.fromString(userUuid));
                    setUserConcept(userUuid);
                    addWfRelIfDoesNotExist(userUuid);
                }


                List<AlertToDataConstraintFailure> errorsAndWarnings = Terms.get().getCommitErrorsAndWarnings();

                if (errorsAndWarnings.size() > 0) {
                    AceLog.getAppLog().warning(errorsAndWarnings.toString());
                    Terms.get().cancel();

                    return false;
                }

                File changeSetRoot = new File(userDir, "changesets");
                getLog().info("** Changeset root: " + changeSetRoot.getAbsolutePath());
                changeSetRoot.mkdirs();

                I_ConfigAceDb newDbProfile = userConfig.getDbConfig();
                File absoluteChangeSetRoot = new File(wbBundleDir, "profiles/user-creation-changesets");

                newDbProfile.setChangeSetRoot(changeSetRoot);
                getLog().info("** Changeset root from db config: " + newDbProfile.getChangeSetRoot().getAbsolutePath());
                getLog().info("** absoluteChangeSetRoot: " + absoluteChangeSetRoot.getAbsolutePath());
                newDbProfile.setChangeSetWriterFileName(userConfig.getUsername() + "#1#"
                        + UUID.randomUUID().toString() + ".eccs");
                newDbProfile.setUsername(userConfig.getUsername());

                String tempKey = UUID.randomUUID().toString();
                ChangeSetGeneratorBI generator =
                        Ts.get().createDtoChangeSetGenerator(new File(absoluteChangeSetRoot, newDbProfile.getChangeSetWriterFileName()), new File(absoluteChangeSetRoot, "#0#"
                        + newDbProfile.getChangeSetWriterFileName()), ChangeSetGenerationPolicy.MUTABLE_ONLY);
                List<ChangeSetGeneratorBI> extraGeneratorList = new ArrayList<>();

                extraGeneratorList.add(generator);
                Ts.get().addChangeSetGenerator(tempKey, generator);
                try {
                    Terms.get().commit();
                } catch (Exception e) {
                    throw new MojoExecutionException(e.getLocalizedMessage(), e);
                } finally {
                    Ts.get().removeChangeSetGenerator(tempKey);
                }


                // Create inbox
                createInbox(userConfig, userConfig.getUsername() + ".inbox", userQueueRoot,
                        userConfig.getUsername() + ".inbox");

                // Create todo box
                createInbox(userConfig, userConfig.getUsername() + ".todo", userQueueRoot,
                        userConfig.getUsername() + ".inbox");

                // Create outbox box
                createOutbox(userConfig, userConfig.getUsername() + ".outbox", userQueueRoot,
                        userConfig.getUsername() + ".inbox");

                getLog().info("** Before write: " + userConfig.getDbConfig().getUserConcept());
                File test = userConfig.getDbConfig().getProfileFile();
                getLog().info("** User Profile File: " + test.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(userConfig.getDbConfig().getProfileFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                oos.writeObject(userConfig.getDbConfig());
                oos.close();
            }

            return true;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private boolean setupUser(String fullname, String username, String password, String userUuid,
            String adminUsername, String adminPassword, String userConfigList)
            throws MojoExecutionException {
        try {
            File userDir = new File(wbBundleDir, "profiles" + File.separator + username);
            File userProfile = new File(userDir, username + ".wb");
            create = !userProfile.exists();
            if (create) {
                createdUsers = true;
                if (userConfigList.equals("")) {
                    userConfigFile = new File(defaultUserConfig + File.separator + "userConfig.txt");
                    readUserConfigFile();
                } else {
                    String[] userConfigs = userConfigList.split(",");
                    for (int i = userConfigs.length - 1; i >= 0; i--) {
                        userConfigFile = new File(defaultUserConfig + File.separator + userConfigs[i].trim());
                        readUserConfigFile();
                        if (userConfig == null) { //create new
                            File userQueueRoot = new File(wbBundleDir, "queues" + File.separator + username);

                            userConfig = newProfile(fullname, username, password, adminUsername,
                                    adminPassword);
                            userConfig.getDbConfig().setProfileFile(userProfile);
                            UUID moduleUuid = module.getLenient().getPrimUuid();
                            userConfig.setModuleNid(Ts.get().getNidForUuids(moduleUuid));
                            Terms.get().setActiveAceFrameConfig(userConfig);

                            //setup changesets
                            File changeSetRoot = new File(userDir, "changesets");
                            getLog().info("** Changeset root: " + changeSetRoot.getAbsolutePath());
                            changeSetRoot.mkdirs();

                            I_ConfigAceDb newDbProfile = userConfig.getDbConfig();
                            File absoluteChangeSetRoot = new File(wbBundleDir, "profiles/user-creation-changesets");

                            newDbProfile.setChangeSetRoot(changeSetRoot);
                            getLog().info("** Changeset root from db config: " + newDbProfile.getChangeSetRoot().getAbsolutePath());
                            getLog().info("** absoluteChangeSetRoot: " + absoluteChangeSetRoot.getAbsolutePath());
                            newDbProfile.setChangeSetWriterFileName(userConfig.getUsername() + "#1#"
                                    + UUID.randomUUID().toString() + ".eccs");
                            newDbProfile.setUsername(userConfig.getUsername());

                            String tempKey = UUID.randomUUID().toString();
                            ChangeSetGeneratorBI generator =
                                    Ts.get().createDtoChangeSetGenerator(new File(absoluteChangeSetRoot, newDbProfile.getChangeSetWriterFileName()), new File(absoluteChangeSetRoot, "#0#"
                                    + newDbProfile.getChangeSetWriterFileName()), ChangeSetGenerationPolicy.MUTABLE_ONLY);
                            List<ChangeSetGeneratorBI> extraGeneratorList = new ArrayList<>();

                            extraGeneratorList.add(generator);
                            Ts.get().addChangeSetGenerator(tempKey, generator);


                            //make user paths
                            if (makeUserDevPath) {
                                if (!username.equals("username")) {
                                    UUID editParentPathUuid = null;
                                    if (!projectDevelopmentParentPathUuid.equals("")) {
                                        editParentPathUuid = UUID.fromString(projectDevelopmentParentPathUuid);
                                    } else {
                                        editParentPathUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                                projectDevelopmentParentPathFsn);
                                    }
                                    UUID editOriginPathUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                            projectDevelopmentOriginPathFsn);
                                    UUID originFromDevPathUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                                            hasDevPathAsOriginPathFsn);
                                    PathBI editPath = null;
                                    ConceptCB newEditPathBp = new ConceptCB(username + " dev path",
                                            username + " dev path",
                                            LANG_CODE.EN,
                                            TermAux.IS_A.getLenient().getPrimUuid(),
                                            editParentPathUuid);
                                    UUID userDevPathUuid = newEditPathBp.getComponentUuid();
                                    if (Ts.get().hasUuid(userDevPathUuid)) {
                                        editPath = Ts.get().getPath(Ts.get().getNidForUuids(userDevPathUuid));
                                    } else {
                                        RefexCAB pathRefexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                                                TermAux.PATH.getLenient().getConceptNid(),
                                                RefsetAux.PATH_REFSET.getLenient().getNid());
                                        pathRefexBp.put(RefexCAB.RefexProperty.UUID1, newEditPathBp.getComponentUuid());
                                        pathRefexBp.setMemberUuid(UUID.randomUUID());

                                        RefexCAB pathOriginRefexBp = new RefexCAB(TK_REFEX_TYPE.CID_INT,
                                                newEditPathBp.getComponentUuid(),
                                                RefsetAux.PATH_ORIGIN_REFEST.getLenient().getNid(), null, null);
                                        pathOriginRefexBp.put(RefexCAB.RefexProperty.UUID1, editOriginPathUuid);
                                        pathOriginRefexBp.put(RefexCAB.RefexProperty.INTEGER1, Integer.MAX_VALUE);
                                        pathRefexBp.setMemberUuid(UUID.randomUUID());

                                        RefexCAB pathOriginRefexOtherBp = new RefexCAB(TK_REFEX_TYPE.CID_INT,
                                                originFromDevPathUuid,
                                                RefsetAux.PATH_ORIGIN_REFEST.getLenient().getNid(), null, null);
                                        pathOriginRefexOtherBp.put(RefexCAB.RefexProperty.UUID1, newEditPathBp.getComponentUuid());
                                        pathOriginRefexOtherBp.put(RefexCAB.RefexProperty.INTEGER1, Integer.MAX_VALUE);
                                        pathOriginRefexOtherBp.setMemberUuid(UUID.randomUUID());
                                        TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(Ts.get().getMetadataEditCoordinate(),
                                                Ts.get().getMetadataViewCoordinate());
                                        PathCB pathBp = new PathCB(newEditPathBp,
                                                pathRefexBp,
                                                pathOriginRefexBp,
                                                pathOriginRefexOtherBp,
                                                Ts.get().getConcept(editOriginPathUuid));
                                        editPath = builder.construct(pathBp);
                                    }

                                    userConfig.addEditingPath(editPath);
                                    userConfig.setColorForPath(editPath.getConceptNid(), new Color(128, 128, 128));
                                }
                            }

                            if (username != null) {
                                if (userConfig.getAddressesList().contains(username) == false) {
                                    userConfig.getAddressesList().add(username);
                                }
                            }

                            userQueueRoot.mkdirs();

                            if (userParentConceptName != null && parentConcept == null) {
                                ConceptCB parentConceptBp = new ConceptCB(
                                        userParentConceptName,
                                        userParentConceptName,
                                        LANG_CODE.EN_US,
                                        TermAux.IS_A.getLenient().getPrimUuid(),
                                        TermAux.USER.getLenient().getPrimUuid());
                                UUID parentConceptUuid = parentConceptBp.getComponentUuid();
                                if (Ts.get().hasUuid(parentConceptUuid)) {
                                    parentConcept = Ts.get().getConcept(parentConceptUuid);
                                } else {
                                    TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(
                                            Ts.get().getMetadataEditCoordinate(),
                                            Ts.get().getMetadataViewCoordinate());
                                    parentConcept = builder.construct(parentConceptBp);
                                    Ts.get().addUncommitted(parentConcept);
                                    Ts.get().commit();
                                }
                            } else if (parentConcept == null) {
                                ConceptSpec userParent = new ConceptSpec("user",
                                        UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));
                                parentConcept = Ts.get().getConcept(userParent.getLenient().getPrimUuid());
                            }

                            // Create new concept for user...
                            if (userUuid == null || userUuid.equals("")) {
                                createUser();
                            } else {
                                setUserConcept(userUuid);
                                addWfRelIfDoesNotExist(userUuid);
                            }


                            List<AlertToDataConstraintFailure> errorsAndWarnings = Terms.get().getCommitErrorsAndWarnings();

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


                            // Create inbox
                            createInbox(userConfig, userConfig.getUsername() + ".inbox", userQueueRoot,
                                    userConfig.getUsername() + ".inbox");

                            // Create todo box
                            createInbox(userConfig, userConfig.getUsername() + ".todo", userQueueRoot,
                                    userConfig.getUsername() + ".inbox");

                            // Create outbox box
                            createOutbox(userConfig, userConfig.getUsername() + ".outbox", userQueueRoot,
                                    userConfig.getUsername() + ".inbox");

                            getLog().info("** Before write: " + userConfig.getDbConfig().getUserConcept());
                            File test = userConfig.getDbConfig().getProfileFile();
                            getLog().info("** User Profile File: " + test.getAbsolutePath());
                            FileOutputStream fos = new FileOutputStream(userConfig.getDbConfig().getProfileFile());
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            I_ConfigAceDb dbConfig = userConfig.getDbConfig();
                            oos.writeObject(userConfig.getDbConfig());
                            oos.close();
                        } else {
                            updateConfig();
                            getLog().info("** Before write: " + userConfig.getDbConfig().getUserConcept());
                            File test = userConfig.getDbConfig().getProfileFile();
                            getLog().info("** User Profile File: " + test.getAbsolutePath());
                            FileOutputStream fos = new FileOutputStream(userConfig.getDbConfig().getProfileFile());
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            I_ConfigAceDb dbConfig = userConfig.getDbConfig();
                            oos.writeObject(userConfig.getDbConfig());
                            oos.close();
                        }
                    }
                    return true;
                }

                File userQueueRoot = new File(wbBundleDir, "queues" + File.separator + username);

                userConfig = newProfile(fullname, username, password, adminUsername,
                        adminPassword);
                userConfig.getDbConfig().setProfileFile(userProfile);
                UUID moduleUuid = module.getLenient().getPrimUuid();
                userConfig.setModuleNid(Ts.get().getNidForUuids(moduleUuid));
                Terms.get().setActiveAceFrameConfig(userConfig);

                File changeSetRoot = new File(userDir, "changesets");
                getLog().info("** Changeset root: " + changeSetRoot.getAbsolutePath());
                changeSetRoot.mkdirs();

                I_ConfigAceDb newDbProfile = userConfig.getDbConfig();
                File absoluteChangeSetRoot = new File(wbBundleDir, "profiles/user-creation-changesets");

                newDbProfile.setChangeSetRoot(changeSetRoot);
                getLog().info("** Changeset root from db config: " + newDbProfile.getChangeSetRoot().getAbsolutePath());
                getLog().info("** absoluteChangeSetRoot: " + absoluteChangeSetRoot.getAbsolutePath());
                newDbProfile.setChangeSetWriterFileName(userConfig.getUsername() + "#1#"
                        + UUID.randomUUID().toString() + ".eccs");
                newDbProfile.setUsername(userConfig.getUsername());

                String tempKey = UUID.randomUUID().toString();
                ChangeSetGeneratorBI generator =
                        Ts.get().createDtoChangeSetGenerator(new File(absoluteChangeSetRoot, newDbProfile.getChangeSetWriterFileName()), new File(absoluteChangeSetRoot, "#0#"
                        + newDbProfile.getChangeSetWriterFileName()), ChangeSetGenerationPolicy.MUTABLE_ONLY);
                List<ChangeSetGeneratorBI> extraGeneratorList = new ArrayList<ChangeSetGeneratorBI>();

                extraGeneratorList.add(generator);
                Ts.get().addChangeSetGenerator(tempKey, generator);

                if (userParentConceptName != null && parentConcept == null) {
                    UUID parentConceptUuid = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, userParentConceptName);
                    if (Ts.get().hasUuid(parentConceptUuid)) {
                        parentConcept = Ts.get().getConcept(parentConceptUuid);
                    } else {
                        ConceptCB parentConceptBp = new ConceptCB(
                                userParentConceptName,
                                userParentConceptName,
                                LANG_CODE.EN_US,
                                TermAux.IS_A.getLenient().getPrimUuid(),
                                TermAux.USER.getLenient().getPrimUuid());
                        TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(
                                Ts.get().getMetadataEditCoordinate(),
                                Ts.get().getMetadataViewCoordinate());
                        parentConcept = builder.construct(parentConceptBp);
                        Ts.get().addUncommitted(parentConcept);
                        Ts.get().commit();
                    }
                } else if (parentConcept == null) {
                    ConceptSpec userParent = new ConceptSpec("user",
                            UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));
                    parentConcept = Ts.get().getConcept(userParent.getLenient().getPrimUuid());
                }

                if (username != null) {
                    if (userConfig.getAddressesList().contains(username) == false) {
                        userConfig.getAddressesList().add(username);
                    }
                }

                userQueueRoot.mkdirs();

                // Create new concept for user...
                if (userUuid == null || userUuid.equals("")) {
                    createUser();
                } else {
                    setUserConcept(userUuid);
                    addWfRelIfDoesNotExist(userUuid);
                }


                List<AlertToDataConstraintFailure> errorsAndWarnings = Terms.get().getCommitErrorsAndWarnings();

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


                // Create inbox
                createInbox(userConfig, userConfig.getUsername() + ".inbox", userQueueRoot,
                        userConfig.getUsername() + ".inbox");

                // Create todo box
                createInbox(userConfig, userConfig.getUsername() + ".todo", userQueueRoot,
                        userConfig.getUsername() + ".inbox");

                // Create outbox box
                createOutbox(userConfig, userConfig.getUsername() + ".outbox", userQueueRoot,
                        userConfig.getUsername() + ".inbox");

                getLog().info("** Before write: " + userConfig.getDbConfig().getUserConcept());
                File test = userConfig.getDbConfig().getProfileFile();
                getLog().info("** User Profile File: " + test.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(userConfig.getDbConfig().getProfileFile());
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
        if (configString != null && !configString.equals("")) {
            String prefTerm = configString.substring(configString.indexOf("(") + 1, configString.indexOf(","));
            String uuidString = configString.substring(configString.indexOf(",") + 1, configString.lastIndexOf(")"));

            return new ConceptSpec(prefTerm.trim(), UUID.fromString(uuidString.trim()));
        } else {
            return null;
        }

    }

    private ArrayList<ConceptSpec> getConceptSpecListFromPrefs(String configString) {
        ArrayList<ConceptSpec> conceptSpecList = new ArrayList<ConceptSpec>();
        if (configString != null && !configString.equals("")) {
            String[] conceptSpecs = configString.split(";");
            if (conceptSpecs.length > 0) {
                for (String spec : conceptSpecs) {
                    conceptSpecList.add(getConceptSpecFromPrefs(spec));
                }
            }
        }

        return conceptSpecList;
    }

    private ConceptVersionBI identifyExistingEditorCategory(String[] columns, ViewCoordinate vc) {
        try {
            return searcher.searchForCategoryByModelerAndTag(modelers.get(columns[0]), columns[1], vc);
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Failed to identify existing categories for mod: "
                    + columns[0] + " and semTag: " + columns[1], e);
        }

        return null;
    }

    private void createInbox(I_ConfigAceFrame config, String inboxName, File userQueueRoot,
            String nodeInboxAddress) throws IOException, ConfigurationException, Exception {
        config.getQueueAddressesToShow().add(inboxName);
        createQueue(config, "inbox", inboxName, userQueueRoot, nodeInboxAddress);
    }

    private void createOutbox(I_ConfigAceFrame config, String outboxName, File userQueueRoot,
            String nodeInboxAddress) throws IOException, ConfigurationException, Exception {
        config.getQueueAddressesToShow().add(outboxName);
        createQueue(config, "outbox", outboxName, userQueueRoot, nodeInboxAddress);
    }

    private void createQueue(I_ConfigAceFrame config, String queueType, String queueName, File userQueueRoot,
            String nodeInboxAddress) throws IOException, ConfigurationException, Exception {

        if (userQueueRoot.exists() == false) {
            userQueueRoot.mkdirs();
        }

        File queueDirectory = new File(userQueueRoot, queueName);

        queueDirectory.mkdirs();

        Map<String, String> substutionMap = new TreeMap<String, String>();

        substutionMap.put("**queueName**", queueDirectory.getName());
        substutionMap.put("**inboxAddress**", queueDirectory.getName());
        substutionMap.put("**directory**", FileIO.getPathRelativeToDir(queueDirectory, wbBundleDir).replace('\\', '/'));
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

        File queueConfigTemplate = new File(configDir, fileName);
        String configTemplateString = FileIO.readerToString(new FileReader(queueConfigTemplate));

        for (String key : substutionMap.keySet()) {
            configTemplateString = configTemplateString.replace(key, substutionMap.get(key));
        }

        File newQueueConfig = new File(queueDirectory, "queue.config");
        FileWriter fw = new FileWriter(newQueueConfig);

        fw.write(configTemplateString);
        fw.close();
        config.getDbConfig().getQueues().add(FileIO.getPathRelativeToDir(newQueueConfig, wbBundleDir));

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
    }

    private ConceptChronicleBI createUser()
            throws TerminologyException, IOException, UnsupportedEncodingException,
            NoSuchAlgorithmException, MojoExecutionException, InvalidCAB, ContradictionException {
        AceLog.getAppLog().info("Create new path for user: " + userConfig.getDbConfig().getFullName());

        if ((userConfig.getDbConfig().getFullName() == null)
                || (userConfig.getDbConfig().getFullName().length() == 0)) {
            JOptionPane.showMessageDialog(userConfig.getWorkflowPanel().getTopLevelAncestor(),
                    "Full name cannot be empty.");

            throw new MojoExecutionException("Full name of user cannot be empty.");
        }

        I_TermFactory tf = Terms.get();

        // Needs a concept record...
        if (Ts.get().hasUuid(UUID.fromString("d8ebf7ea-afd7-5db8-aff0-d3028253f6bf"))) {
            System.out.println("FOUND IT");
        }
        ConceptCB userConceptBp = new ConceptCB(userConfig.getDbConfig().getFullName(),
                userConfig.getUsername(),
                LANG_CODE.EN,
                TermAux.IS_A.getLenient().getPrimUuid(),
                parentConcept.getPrimUuid());
        if (Ts.get().hasUuid(UUID.fromString("d8ebf7ea-afd7-5db8-aff0-d3028253f6bf"))) {
            System.out.println("FOUND IT");
        }
        UUID userUuid = userConceptBp.getComponentUuid();
        if (Ts.get().hasUuid(UUID.fromString("d8ebf7ea-afd7-5db8-aff0-d3028253f6bf"))) {
            System.out.println("FOUND IT");
        }
        if (Ts.get().hasUuid(userUuid)) {
            setUserConcept(userUuid.toString());
            addWfRelIfDoesNotExist(userUuid.toString());
            return Ts.get().getConcept(userUuid);
        } else {
//                        userConceptBp.setComponentUuid(userUuid);
            // Needs a description record...
            DescriptionCAB inboxDescBp = new DescriptionCAB(userConceptBp.getComponentUuid(),
                    queueDescriptionType.getLenient().getPrimUuid(),
                    LANG_CODE.EN,
                    userConfig.getUsername() + ".inbox",
                    false);

            //add workflow relationship
            RelationshipCAB wfRelBp = new RelationshipCAB(userConceptBp.getComponentUuid(),
                    ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS.getPrimoridalUid(),
                    ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getPrimoridalUid(),
                    0,
                    TkRelationshipType.STATED_ROLE);
            ViewCoordinate vc = userConfig.getViewCoordinate();
            EditCoordinate oldEc = userConfig.getEditCoordinate();
            EditCoordinate ec = new EditCoordinate(oldEc.getAuthorNid(),
                    oldEc.getModuleNid(),
                    TermAux.WB_AUX_PATH.getLenient().getNid());
            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(ec, vc);
            userConceptBp.addDescriptionCAB(inboxDescBp);
            userConceptBp.setRelationshipCAB(wfRelBp);
            ConceptChronicleBI userConcept = builder.construct(userConceptBp);
            userConfig.getDbConfig().setUserConcept((I_GetConceptData) userConcept);
            Ts.get().addUncommitted(userConcept);

            return userConcept;
        }
    }

    private void addWfRelIfDoesNotExist(String userUuidString) throws TerminologyException, IOException, InvalidCAB, ContradictionException {
        I_GetConceptData userConcept = Terms.get().getConcept(UUID.fromString(userUuidString));
        int wfNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getUids());
        boolean found = false;
        for (I_RelVersioned rel : userConcept.getSourceRels()) {
            if (rel.getTargetNid() == wfNid) {
                found = true;
            }
        }
        if (!found) {
            RelationshipCAB wfRelBp = new RelationshipCAB(userConcept.getPrimUuid(),
                    ArchitectonicAuxiliary.Concept.WORKFLOW_EDITOR_STATUS.getPrimoridalUid(),
                    ArchitectonicAuxiliary.Concept.WORKFLOW_ACTIVE_MODELER.getPrimoridalUid(),
                    0,
                    TkRelationshipType.STATED_ROLE);
            ViewCoordinate vc = userConfig.getViewCoordinate();
            EditCoordinate oldEc = userConfig.getEditCoordinate();
            EditCoordinate ec = new EditCoordinate(oldEc.getAuthorNid(),
                    oldEc.getModuleNid(),
                    TermAux.WB_AUX_PATH.getLenient().getNid());
            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(ec, vc);
            builder.construct(wfRelBp);

        }
        Ts.get().addUncommitted(userConcept);
    }

    private void setUserConcept(String userUuidString) throws TerminologyException, IOException, InvalidCAB, ContradictionException {
        I_GetConceptData userConcept = Terms.get().getConcept(UUID.fromString(userUuidString));
        I_TermFactory tf = Terms.get();
        DescriptionCAB inboxDescBp = new DescriptionCAB(userConcept.getPrimUuid(),
                queueDescriptionType.getLenient().getPrimUuid(),
                LANG_CODE.EN,
                userConfig.getUsername() + ".inbox",
                false);
        ViewCoordinate vc = userConfig.getViewCoordinate();
        EditCoordinate oldEc = userConfig.getEditCoordinate();
        EditCoordinate ec = new EditCoordinate(oldEc.getAuthorNid(),
                oldEc.getModuleNid(),
                TermAux.WB_AUX_PATH.getLenient().getNid());
        TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(ec, vc);
        builder.construct(inboxDescBp);
        userConfig.getDbConfig().setUserConcept(userConcept);
        Ts.get().addUncommitted(userConcept);
    }

    public I_ConfigAceFrame newProfile(String fullName, String username, String password, String adminUsername,
            String adminPassword) throws MojoExecutionException, TerminologyException, IOException, NoSuchAlgorithmException {

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
        if (generateAdjCs.equalsIgnoreCase("true")) {
            newDbProfile.setAdjudicationWorkListChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
        } else {
            newDbProfile.setAdjudicationWorkListChangeSetPolicy(ChangeSetPolicy.OFF);
        }
        activeConfig.setDbConfig(newDbProfile);
        newDbProfile.getAceFrames().add(activeConfig);

        if (fullName == null || fullName.length() < 2) {
            fullName = "Full Name";
        }

        if (username == null || username.length() < 2) {
            username = "username";
        }
        if (adminUsername == null || adminUsername.length() < 2) {
            adminUsername = "admin";
            adminPassword = "visit.bend";
        }

        activeConfig.getDbConfig().setFullName(fullName);
        activeConfig.setUsername(username);
        activeConfig.setPassword(password);
        activeConfig.setAdminPassword(adminPassword);
        activeConfig.setAdminUsername(adminUsername);

        //status popup values
        I_IntList statusPopupTypes = tf.newIntList();
       statusPopupTypes.add(SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid());
        statusPopupTypes.add(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getNid());
        statusPopupTypes.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
        statusPopupTypes.add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
        activeConfig.setEditStatusTypePopup(statusPopupTypes);

        //set up classifier
        activeConfig.setClassificationRoot(tf.getConcept(Taxonomies.SNOMED.getLenient().getUUIDs()));
        activeConfig.setClassificationRoleRoot(tf.getConcept((new ConceptSpec("Concept model attribute (attribute)",
                UUID.fromString("6155818b-09ed-388e-82ce-caa143423e99"))).getLenient().getUUIDs()));
        activeConfig.setClassifierInputMode(I_ConfigAceFrame.CLASSIFIER_INPUT_MODE_PREF.EDIT_PATH);
        activeConfig.setClassifierIsaType((I_GetConceptData) Snomed.IS_A.getLenient());

        //set up taxonomy view roots
        I_IntSet roots = tf.newIntSet();
        roots.add(Taxonomies.REFSET_AUX.getLenient().getNid());
        roots.add(Taxonomies.WB_AUX.getLenient().getNid());
        roots.add(Taxonomies.SNOMED.getLenient().getNid());
        activeConfig.setRoots(roots);

        //set up allowed statuses
        I_IntSet allowedStatus = tf.newIntSet();
        allowedStatus.add(SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid());
        allowedStatus.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
        activeConfig.setAllowedStatus(allowedStatus);

        //set up parent relationship rel types (view->taxonomy)
        I_IntSet destRelTypes = tf.newIntSet();
        destRelTypes.add(Snomed.IS_A.getLenient().getNid());
        destRelTypes.add(TermAux.IS_A.getLenient().getNid());
        activeConfig.setDestRelTypes(destRelTypes);

        //set up editing defaults
        activeConfig.setDefaultImageType(tf.getConcept(TermAux.AUX_IMAGE.getLenient().getUUIDs()));
        activeConfig.setDefaultDescriptionType(tf.getConcept(defaultDescType.getLenient().getUUIDs()));
        activeConfig.setDefaultRelationshipCharacteristic(tf.getConcept(defaultRelChar.getLenient().getUUIDs()));
        activeConfig.setDefaultRelationshipRefinability(tf.getConcept(defaultRelRefinability.getLenient().getUUIDs()));
        activeConfig.setDefaultRelationshipType(tf.getConcept(defaultRelType.getLenient().getUUIDs()));
        activeConfig.setDefaultStatus(tf.getConcept(defaultStatus.getLenient().getUUIDs()));

        //set up label display prefs
        I_IntList treeDescPrefList = activeConfig.getTreeDescPreferenceList();
        treeDescPrefList.add(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
        treeDescPrefList.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
        treeDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());
        treeDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());

        I_IntList shortLabelDescPrefList = activeConfig.getShortLabelDescPreferenceList();
        shortLabelDescPrefList.add(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
        shortLabelDescPrefList.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
        shortLabelDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());
        shortLabelDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());

        I_IntList longLabelDescPrefList = activeConfig.getLongLabelDescPreferenceList();
        longLabelDescPrefList.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
        longLabelDescPrefList.add(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
        longLabelDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());
        longLabelDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());

        I_IntList tableDescPrefList = activeConfig.getTableDescPreferenceList();
        tableDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());
        tableDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());
        tableDescPrefList.add(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
        tableDescPrefList.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());

        //set up paths
        PathBI viewPath = tf.getPath(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, this.projectDevelopmentViewPathFsn));
        PositionBI viewPosition = tf.newPosition(viewPath, Long.MAX_VALUE);
        Set<PositionBI> viewSet = new HashSet<PositionBI>();
        viewSet.add(viewPosition);
        activeConfig.setViewPositions(viewSet);

        PathBI editPath = null;
        if (!makeUserDevPath) {
            editPath = tf.getPath(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, this.projectDevelopmentPathFsn));
            activeConfig.addEditingPath(editPath);
            if (editPath.getConceptNid() != viewPath.getConceptNid()) {
                activeConfig.setColorForPath(editPath.getConceptNid(), new Color(128, 128, 128));
            }
        }

        if (this.projectDevelopmentAdjPathFsn != null) {
            PathBI adjPath = tf.getPath(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, this.projectDevelopmentAdjPathFsn));
            activeConfig.addPromotionPath(adjPath);
        }

        activeConfig.setColorForPath(viewPath.getConceptNid(), new Color(255, 84, 27));
        activeConfig.setColorForPath(ReferenceConcepts.TERM_AUXILIARY_PATH.getNid(), new Color(25, 178, 63));
        activeConfig.setColorForPath(Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()), new Color(81, 23, 255));

        //set up toggles
        activeConfig.setSubversionToggleVisible(false);
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
        activeConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.REFSETS, true);

        activeConfig.setPrecedence(Precedence.PATH);

        //set up vewing preferences

        /*
         * langSortPref preference options are: rf2 refex (rf2), language refex
         * (lr), type before language (tl), language before type (lt)
         */
        if (langSortPref.equals("rf2")) {
            activeConfig.setLanguageSortPref(I_ConfigAceFrame.LANGUAGE_SORT_PREF.RF2_LANG_REFEX);
        } else if (langSortPref.equals("lr")) {
            activeConfig.setLanguageSortPref(I_ConfigAceFrame.LANGUAGE_SORT_PREF.LANG_REFEX);
        } else if (langSortPref.equals("tl")) {
            activeConfig.setLanguageSortPref(I_ConfigAceFrame.LANGUAGE_SORT_PREF.TYPE_B4_LANG);
        } else if (langSortPref.equals("lt")) {
            activeConfig.setLanguageSortPref(I_ConfigAceFrame.LANGUAGE_SORT_PREF.LANG_B4_TYPE);
        } else {
            throw new MojoExecutionException("Can't handle value:  " + langSortPref
                    + " for preference: langSortPref");
        }
        I_IntList languagePreferenceList = activeConfig.getLanguagePreferenceList();
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
            activeConfig.setRelAssertionType(RelAssertionType.INFERRED_THEN_STATED);
        } else {
            throw new MojoExecutionException("Can't handle value:  " + statedInferredPolicy
                    + " for preference: statedInferredPolicy");
        }

        activeConfig.setShowViewerImagesInTaxonomy(true);

        //set up enabled refsets
		/*
         * Available components: concept, description
         *
         * Available types: boolean,concept, conInt, string, integer, conConCon
         */
        String[] refsets = visibleRefests.split(",");
        for (String r : refsets) {
            String toggle = r.substring(0, r.indexOf(".")).trim();
            String refset = r.substring(r.indexOf(".") + 1).trim();
            I_HostConceptPlugins.TOGGLES t = null;
            I_HostConceptPlugins.REFSET_TYPES refsetType = null;

            if (toggle.equals("concept")) {
                t = I_HostConceptPlugins.TOGGLES.ATTRIBUTES;
            } else if (toggle.equals("desc")) {
                t = I_HostConceptPlugins.TOGGLES.DESCRIPTIONS;
            } else {
                throw new MojoExecutionException("Can't handle value:  " + toggle
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
                throw new MojoExecutionException("Can't handle value:  " + refset
                        + " For preference: visibleRefsets.type");
            }
            activeConfig.setRefsetInToggleVisible(refsetType, t, true);
        }

        return activeConfig;
    }

    public I_ConfigAceFrame updateConfig() throws MojoExecutionException, TerminologyException, IOException, NoSuchAlgorithmException, InvalidCAB, ContradictionException {

        I_ImplementTermFactory tf = (I_ImplementTermFactory) Terms.get();

        //status popup values
        I_IntList statusPopupTypes = tf.newIntList();
        statusPopupTypes.add(SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid());
        statusPopupTypes.add(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getNid());
        statusPopupTypes.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
        statusPopupTypes.add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
        userConfig.setEditStatusTypePopup(statusPopupTypes);

        //set up classifier
        userConfig.setClassificationRoot(tf.getConcept(Taxonomies.SNOMED.getLenient().getUUIDs()));
        userConfig.setClassificationRoleRoot(tf.getConcept((new ConceptSpec("Concept model attribute (attribute)",
                UUID.fromString("6155818b-09ed-388e-82ce-caa143423e99"))).getLenient().getUUIDs()));
        userConfig.setClassifierInputMode(I_ConfigAceFrame.CLASSIFIER_INPUT_MODE_PREF.EDIT_PATH);
        userConfig.setClassifierIsaType((I_GetConceptData) Snomed.IS_A.getLenient());

        //set up taxonomy view roots
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

        //set up parent relationship rel types (view->taxonomy)
        I_IntSet destRelTypes = tf.newIntSet();
        destRelTypes.add(Snomed.IS_A.getLenient().getNid());
        destRelTypes.add(TermAux.IS_A.getLenient().getNid());
        if (destRelTypesList != null) {
            for (ConceptSpec relTypeSpec : destRelTypesList) {
                destRelTypes.add(relTypeSpec.getLenient().getConceptNid());
            }
        }
        userConfig.setDestRelTypes(destRelTypes);

        //set up editing defaults
        userConfig.setDefaultImageType(tf.getConcept(TermAux.AUX_IMAGE.getLenient().getUUIDs()));
        userConfig.setDefaultDescriptionType(tf.getConcept(defaultDescType.getLenient().getUUIDs()));
        userConfig.setDefaultRelationshipCharacteristic(tf.getConcept(defaultRelChar.getLenient().getUUIDs()));
        userConfig.setDefaultRelationshipRefinability(tf.getConcept(defaultRelRefinability.getLenient().getUUIDs()));
        userConfig.setDefaultRelationshipType(tf.getConcept(defaultRelType.getLenient().getUUIDs()));
        userConfig.setDefaultStatus(tf.getConcept(defaultStatus.getLenient().getUUIDs()));

        //set up refset defaults for editing
        I_HoldRefsetPreferences attribRefsetPref = userConfig.getRefsetPreferencesForToggle(TOGGLES.ATTRIBUTES);
        I_RefsetDefaultsTemplate templatePreferences = attribRefsetPref.getTemplatePreferences();

        I_RefsetDefaultsBoolean booleanPreferences = attribRefsetPref.getBooleanPreferences();
        I_IntList booleanPopupIds = booleanPreferences.getRefsetPopupIds();
        if (!cBooleanRefsets.isEmpty()) {
            booleanPreferences.setDefaultRefset((I_GetConceptData) cBooleanRefsets.get(0).getLenient());
            booleanPreferences.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : cBooleanRefsets) {
                booleanPopupIds.add(spec.getLenient().getConceptNid());
            }
        }

        I_RefsetDefaultsConcept conceptPreferences = attribRefsetPref.getConceptPreferences();
        I_IntList conceptPopupIds = conceptPreferences.getRefsetPopupIds();
        if (!cConceptRefsets.isEmpty()) {
            conceptPreferences.setDefaultRefset((I_GetConceptData) cConceptRefsets.get(0).getLenient());
            conceptPreferences.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : cConceptRefsets) {
                conceptPopupIds.add(spec.getLenient().getConceptNid());
            }
        }
        I_IntList conceptStatusPopupIds = conceptPreferences.getStatusPopupIds();
        for (ConceptSpec spec : cConceptRefsetStatus) {
            conceptStatusPopupIds.add(spec.getLenient().getNid());
        }
        I_IntList conceptConceptPopupIds = conceptPreferences.getConceptPopupIds();
        for (ConceptSpec spec : cConceptRefsetConTypes) {
            conceptConceptPopupIds.add(spec.getLenient().getNid());
        }

        I_RefsetDefaultsInteger integerPreferences = attribRefsetPref.getIntegerPreferences();
        I_IntList integerPopupIds = integerPreferences.getRefsetPopupIds();
        if (!cIntegerRefsets.isEmpty()) {
            integerPreferences.setDefaultRefset((I_GetConceptData) cIntegerRefsets.get(0).getLenient());
            integerPreferences.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : cIntegerRefsets) {
                integerPopupIds.add(spec.getLenient().getConceptNid());
            }
        }

        I_RefsetDefaultsConInt conIntPreferences = attribRefsetPref.getConIntPreferences();
        I_IntList conIntPopupIds = conIntPreferences.getRefsetPopupIds();
        if (!cConIntRefsets.isEmpty()) {
            conIntPreferences.setDefaultRefset((I_GetConceptData) cConIntRefsets.get(0).getLenient());
            conIntPreferences.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : cConIntRefsets) {
                conIntPopupIds.add(spec.getLenient().getConceptNid());
            }
        }

        I_RefsetDefaultsString stringPreferences = attribRefsetPref.getStringPreferences();
        I_IntList stringPopupIds = stringPreferences.getRefsetPopupIds();
        if (!cStringRefsets.isEmpty()) {
            stringPreferences.setDefaultRefset((I_GetConceptData) cStringRefsets.get(0).getLenient());
            stringPreferences.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : cStringRefsets) {
                stringPopupIds.add(spec.getLenient().getConceptNid());
            }
        }

        I_RefsetsDefaultsConConCon conConConPreferences = attribRefsetPref.getCidCidCidPreferences();
        I_IntList conConConPopupIds = conConConPreferences.getRefsetPopupIds();
        if (!cConConConRefsets.isEmpty()) {
            conConConPreferences.setDefaultRefset((I_GetConceptData) cConConConRefsets.get(0).getLenient());
            conConConPreferences.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : cConConConRefsets) {
                conConConPopupIds.add(spec.getLenient().getConceptNid());
            }
        }

        I_HoldRefsetPreferences descRefsetPref = userConfig.getRefsetPreferencesForToggle(TOGGLES.DESCRIPTIONS);

        I_RefsetDefaultsBoolean booleanPreferencesDesc = descRefsetPref.getBooleanPreferences();
        I_IntList booleanPopupIdsDesc = booleanPreferencesDesc.getRefsetPopupIds();
        if (!dBooleanRefsets.isEmpty()) {
            booleanPreferencesDesc.setDefaultRefset((I_GetConceptData) dBooleanRefsets.get(0).getLenient());
            booleanPreferencesDesc.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : dBooleanRefsets) {
                booleanPopupIdsDesc.add(spec.getLenient().getConceptNid());
            }
        }

        I_RefsetDefaultsConcept conceptPreferencesDesc = descRefsetPref.getConceptPreferences();
        I_IntList conceptPopupIdsDesc = conceptPreferencesDesc.getRefsetPopupIds();
        if (!dConceptRefsets.isEmpty()) {
            if (conceptPopupIdsDesc.get(0) == Taxonomies.REFSET_AUX.getLenient().getConceptNid()) {
                conceptPopupIdsDesc.remove(0);
            }
            conceptPreferencesDesc.setDefaultRefset((I_GetConceptData) dConceptRefsets.get(0).getLenient());
            conceptPreferencesDesc.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : dConceptRefsets) {
                conceptPopupIdsDesc.add(spec.getLenient().getConceptNid());
            }
        }
        I_IntList descStatusPopupIds = conceptPreferencesDesc.getStatusPopupIds();
        for (ConceptSpec spec : dConceptRefsetStatus) {
            descStatusPopupIds.add(spec.getLenient().getNid());
        }
        I_IntList descConceptPopupIds = conceptPreferencesDesc.getConceptPopupIds();
        for (ConceptSpec spec : dConceptRefsetConTypes) {
            descConceptPopupIds.add(spec.getLenient().getNid());
        }

        I_RefsetDefaultsInteger integerPreferencesDesc = descRefsetPref.getIntegerPreferences();
        I_IntList integerPopupIdsDesc = integerPreferencesDesc.getRefsetPopupIds();
        if (!dIntegerRefsets.isEmpty()) {
            integerPreferencesDesc.setDefaultRefset((I_GetConceptData) dIntegerRefsets.get(0).getLenient());
            integerPreferencesDesc.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : dIntegerRefsets) {
                integerPopupIdsDesc.add(spec.getLenient().getConceptNid());
            }
        }

        I_RefsetDefaultsConInt conIntPreferencesDesc = descRefsetPref.getConIntPreferences();
        I_IntList conIntPopupIdsDesc = conIntPreferencesDesc.getRefsetPopupIds();
        if (!dConIntRefsets.isEmpty()) {
            conIntPreferencesDesc.setDefaultRefset((I_GetConceptData) dConIntRefsets.get(0).getLenient());
            conIntPreferencesDesc.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : dConIntRefsets) {
                conIntPopupIdsDesc.add(spec.getLenient().getConceptNid());
            }
        }

        I_RefsetDefaultsString stringPreferencesDesc = descRefsetPref.getStringPreferences();
        I_IntList stringPopupIdsDesc = stringPreferencesDesc.getRefsetPopupIds();
        if (!dStringRefsets.isEmpty()) {
            stringPreferencesDesc.setDefaultRefset((I_GetConceptData) dStringRefsets.get(0).getLenient());
            stringPreferencesDesc.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : dStringRefsets) {
                stringPopupIdsDesc.add(spec.getLenient().getConceptNid());
            }
        }

        I_RefsetsDefaultsConConCon conConConPreferencesDesc = descRefsetPref.getCidCidCidPreferences();
        I_IntList conConConPopupIdsDesc = conConConPreferencesDesc.getRefsetPopupIds();
        if (!dConConConRefsets.isEmpty()) {
            conConConPreferencesDesc.setDefaultRefset((I_GetConceptData) dConConConRefsets.get(0).getLenient());
            conConConPreferencesDesc.setDefaultStatusForRefset((I_GetConceptData) refsetStatus.getLenient());
            for (ConceptSpec spec : dConConConRefsets) {
                conConConPopupIdsDesc.add(spec.getLenient().getConceptNid());
            }
        }

        //set up label display prefs
        I_IntList treeDescPrefList = userConfig.getTreeDescPreferenceList();
        I_IntList shortLabelDescPrefList = userConfig.getShortLabelDescPreferenceList();
        I_IntList longLabelDescPrefList = userConfig.getLongLabelDescPreferenceList();
        I_IntList tableDescPrefList = userConfig.getTableDescPreferenceList();
        if (displayRf2) {
            treeDescPrefList.add(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
            treeDescPrefList.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
            treeDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());
            treeDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());

            shortLabelDescPrefList.add(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
            shortLabelDescPrefList.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
            shortLabelDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());
            shortLabelDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());

            longLabelDescPrefList.add(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
            longLabelDescPrefList.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
            longLabelDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());
            longLabelDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());

            tableDescPrefList.add(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
            tableDescPrefList.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
            tableDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());
            tableDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());

        } else {
            treeDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());
            treeDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());


            shortLabelDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());
            shortLabelDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());


            longLabelDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());
            longLabelDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());


            tableDescPrefList.add(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid());
            tableDescPrefList.add(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());
        }

        //set up paths
        PathBI editPath = null;
        //can only make user dev path during profil creation, and not during config update.
        if (!makeUserDevPath) {
            editPath = tf.getPath(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, this.projectDevelopmentPathFsn));
            userConfig.addEditingPath(editPath);
        }
        PathBI viewPath = tf.getPath(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, this.projectDevelopmentViewPathFsn));
        PositionBI viewPosition = tf.newPosition(viewPath, Long.MAX_VALUE);
        Set<PositionBI> viewSet = new HashSet<PositionBI>();
        viewSet.add(viewPosition);
        userConfig.setViewPositions(viewSet);

        if (this.projectDevelopmentAdjPathFsn != null) {
            PathBI adjPath = tf.getPath(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, this.projectDevelopmentAdjPathFsn));
            userConfig.addPromotionPath(adjPath);
        }

        userConfig.setColorForPath(viewPath.getConceptNid(), new Color(255, 84, 27));
        userConfig.setColorForPath(ReferenceConcepts.TERM_AUXILIARY_PATH.getNid(), new Color(25, 178, 63));
        userConfig.setColorForPath(Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()), new Color(81, 23, 255));

        //set up toggles
        userConfig.setSubversionToggleVisible(false);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.ID, true);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.ATTRIBUTES, true);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.DESCRIPTIONS, true);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.SOURCE_RELS, true);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.DEST_RELS, true);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.LINEAGE, true);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.LINEAGE_GRAPH, false);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.IMAGE, true);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.CONFLICT, true);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.STATED_INFERRED, false);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.PREFERENCES, true);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.HISTORY, true);
        userConfig.setTogglesInComponentPanelVisible(I_HostConceptPlugins.TOGGLES.REFSETS, true);

        userConfig.setPrecedence(Precedence.PATH);

        //set up vewing preferences

        /*
         * langSortPref preference options are: rf2 refex (rf2), language refex
         * (lr), type before language (tl), language before type (lt)
         */
        if (langSortPref.equals("rf2")) {
            userConfig.setLanguageSortPref(I_ConfigAceFrame.LANGUAGE_SORT_PREF.RF2_LANG_REFEX);
        } else if (langSortPref.equals("lr")) {
            userConfig.setLanguageSortPref(I_ConfigAceFrame.LANGUAGE_SORT_PREF.LANG_REFEX);
        } else if (langSortPref.equals("tl")) {
            userConfig.setLanguageSortPref(I_ConfigAceFrame.LANGUAGE_SORT_PREF.TYPE_B4_LANG);
        } else if (langSortPref.equals("lt")) {
            userConfig.setLanguageSortPref(I_ConfigAceFrame.LANGUAGE_SORT_PREF.LANG_B4_TYPE);
        } else {
            throw new MojoExecutionException("Can't handle value:  " + langSortPref
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
            throw new MojoExecutionException("Can't handle value:  " + statedInferredPolicy
                    + " for preference: statedInferredPolicy");
        }

        userConfig.setShowViewerImagesInTaxonomy(true);

        //set up enabled refsets
		/*
         * Available components: concept, description
         *
         * Available types: boolean,concept, conInt, string, integer, conConCon
         */
        String[] refsets = visibleRefests.split(",");
        for (String r : refsets) {
            String toggle = r.substring(0, r.indexOf(".")).trim();
            String refset = r.substring(r.indexOf(".") + 1).trim();
            I_HostConceptPlugins.TOGGLES t = null;
            I_HostConceptPlugins.REFSET_TYPES refsetType = null;

            if (toggle.equals("concept")) {
                t = I_HostConceptPlugins.TOGGLES.ATTRIBUTES;
            } else if (toggle.equals("desc")) {
                t = I_HostConceptPlugins.TOGGLES.DESCRIPTIONS;
            } else {
                throw new MojoExecutionException("Can't handle value:  " + toggle
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
                throw new MojoExecutionException("Can't handle value:  " + refset
                        + " For preference: visibleRefsets.type");
            }
            userConfig.setRefsetInToggleVisible(refsetType, t, true);
        }

        return userConfig;
    }
}
