/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.project.view.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.api.Precedence;
import org.junit.Ignore;
import org.junit.Test;

/**
 * The Class TestTerminologyProjectDAOForWorkSetsCRUD.
 */
public class TestWorklistAndWorkflow extends TestCase {

    /**
     * The vodb directory.
     */
    File vodbDirectory;
    /**
     * The read only.
     */
    boolean readOnly = false;
    /**
     * The cache size.
     */
    Long cacheSize = Long.getLong("600000000");
    /**
     * The db setup config.
     */
    DatabaseSetupConfig dbSetupConfig;
    /**
     * The config.
     */
    I_ConfigAceFrame config;
    /**
     * The tf.
     */
    I_TermFactory tf;
    /**
     * The new work set concept.
     */
    I_GetConceptData newWorkSetConcept;
    /**
     * The allowed statuses with retired.
     */
    I_IntSet allowedStatusesWithRetired;
    /**
     * The project.
     */
    I_TerminologyProject project;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
//        System.out.println("Deleting test fixture");
//        deleteDirectory(new File("berkeley-db"));
//        System.out.println("Creating test fixture");
////        copyDirectory(new File("/Users/alo/Desktop/berkeley-db"), new File("berkeley-db"));
//        vodbDirectory = new File("berkeley-db");
//        dbSetupConfig = new DatabaseSetupConfig();
//        System.out.println("Opening database");
//        Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
//        tf = (I_ImplementTermFactory) Terms.get();
//        config = getTestConfig();
//        tf.setActiveAceFrameConfig(config);
//
//        I_GetConceptData projectConcept = null;
//        try {
//            projectConcept = tf.getConcept(new UUID[]{UUID.fromString("3efb77c9-1369-3728-a001-faa7ac668efd")});
//        } catch (TerminologyException e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (IOException e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        }
//        project = TerminologyProjectDAO.getTranslationProject(projectConcept, config);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test new workflow.
     */
    @Ignore("Ignored becuase setup has user specific class paths...") @Test
    public void testCreateNewProject() {
//        try {
//            I_GetConceptData partitionConcept = tf.getConcept(UUID.fromString("d88283f4-53c2-4b8b-ae37-428264feaeba"));
//            Partition partition = TerminologyProjectDAO.getPartition(partitionConcept, config);
//            WorkflowDefinition wfDef = getWfDefinition();
//            WorkList workList = TerminologyProjectDAO.generateWorkListFromPartition(partition,
//                    wfDef, getWorkflowMembers(wfDef), "Worklist test 1", config,
//                    Terms.get().newActivityPanel(true, config, "<html>Generating Worklist from partition", true));
//            Terms.get().commit();
//            WorkListMember member = workList.getWorkListMembers().iterator().next();
//
//            WorkflowInterpreter wfInt = WorkflowInterpreter.createWorkflowInterpreter(wfDef);
//
//            System.out.println(wfInt.getNextDestination(member.getWfInstance(), workList));
//            //workList.getPromotionRefset(config).setPromotionStatus(member.getId(), statusConceptId)
//
//
//        } catch (TerminologyException e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (IOException e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        } catch (Exception e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        }
    }

    /**
     * Waiting.
     *
     * @param n the n
     */
    public static void waiting(int n) {

        long t0, t1;
        t0 = System.currentTimeMillis();
        do {
            t1 = System.currentTimeMillis();
        } while ((t1 - t0) < (n * 1000));
    }

    /**
     * Gets the test config.
     *
     * @return the test config
     */
    private I_ConfigAceFrame getTestConfig() {
        I_ConfigAceFrame config = null;
        try {
            config = tf.newAceFrameConfig();
            config.addViewPosition(tf.newPosition(
                    tf.getPath(new UUID[]{UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}),
                    Long.MAX_VALUE));
            config.addEditingPath(tf.getPath(new UUID[]{UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}));
            config.addPromotionPath(tf.getPath(new UUID[]{UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}));

            config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
            config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
            config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
            config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
            config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
            config.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

            //			BdbTermFactory tfb = (BdbTermFactory) tf;
            //			I_ConfigAceDb newDbProfile = tfb.newAceDbConfig();
            //			newDbProfile.setUsername("username");
            //			newDbProfile.setUserConcept(tf.getConcept(UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c")));
            //			newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
            //			newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
            //			newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
            //			newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
            //			config.setDbConfig(newDbProfile);

            config.setPrecedence(Precedence.TIME);

        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }

        return config;
    }

    // If targetLocation does not exist, it will be created.
    /**
     * Copy directory.
     *
     * @param sourceLocation the source location
     * @param targetLocation the target location
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    /**
     * Delete directory.
     *
     * @param path the path
     * @return true, if successful
     */
    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    /**
     * Gets the wf definition.
     *
     * @return the wf definition
     */
    public static WorkflowDefinition getWfDefinition() {
        WorkflowDefinition wfdf = new WorkflowDefinition();

        List<WfState> states = new ArrayList<WfState>();
        states.add(new WfState("Assigned", UUID.randomUUID()));
        states.add(new WfState("Responded by SME", UUID.randomUUID()));
        states.add(new WfState("Responded by Super SME", UUID.randomUUID()));
        states.add(new WfState("Consulted to SME", UUID.randomUUID()));
        states.add(new WfState("Consulted to Super SME", UUID.randomUUID()));
        states.add(new WfState("Reviewed", UUID.randomUUID()));
        states.add(new WfState("Revision rejected", UUID.randomUUID()));
        states.add(new WfState("Translated", UUID.randomUUID()));
        states.add(new WfState("Translation rejected", UUID.randomUUID()));

        List<WfRole> roles = new ArrayList<WfRole>();
        roles.add(new WfRole("Editorial Board", UUID.randomUUID()));
        roles.add(new WfRole("TSP Translator", UUID.randomUUID()));
        roles.add(new WfRole("SME", UUID.randomUUID()));
        roles.add(new WfRole("Super SME", UUID.randomUUID()));
        roles.add(new WfRole("TPO Reviewer", UUID.randomUUID()));
        roles.add(new WfRole("TSP Reviewer", UUID.randomUUID()));

        Map<String, WfAction> actions = new HashMap<String, WfAction>();
//		actions.put("Approve", new StubAction("Approve"));
//		actions.put("Reject revision with stated reason", new StubAction("Reject revision with stated reason"));
//		actions.put("Consult to Super SME", new StubAction("Consult to Super SME"));
//		actions.put("Translate", new StubAction("Translate"));
//		actions.put("Respond SME consultation", new StubAction("Respond SME consultation"));
//		actions.put("Respond Super SME consultation", new StubAction("Respond Super SME consultation"));
//		actions.put("Reject revision", new StubAction("Reject revision"));
//		actions.put("Escalate", new StubAction("Escalate"));
//		actions.put("Reject translation with stated reason", new StubAction("Reject translation with stated reason"));
//		actions.put("Review", new StubAction("Review"));
//		actions.put("Consult to SME", new StubAction("Consult to SME"));

        wfdf.setName("Workflow Canada 1");
        wfdf.setRoles(roles);
        wfdf.setStates(states);
        wfdf.setActions(actions);
        wfdf.getXlsFileName().add("/Users/alo/Desktop/test-dtable.xls");

        return wfdf;
    }

    /**
     * Gets the workflow members.
     *
     * @param wfDef the wf def
     * @return the workflow members
     */
    private static List<WfMembership> getWorkflowMembers(WorkflowDefinition wfDef) {
        List<WfMembership> members = new ArrayList<WfMembership>();
        int counter = 0;
        for (WfRole loopRole : wfDef.getRoles()) {
            counter++;
            WfUser loopuser = new WfUser("User " + counter, UUID.randomUUID());
            WfPermission loopPermission = new WfPermission(UUID.randomUUID(),
                    loopRole, UUID.randomUUID());
            List<WfPermission> listPerm = new ArrayList<WfPermission>();
            listPerm.add(loopPermission);
            loopuser.setPermissions(listPerm);

            members.add(new WfMembership(UUID.randomUUID(),
                    loopuser, loopRole, true));
        }
        return members;
    }
}
