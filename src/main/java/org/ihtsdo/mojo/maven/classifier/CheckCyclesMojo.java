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
package org.ihtsdo.mojo.maven.classifier;

import org.apache.maven.plugin.logging.Log;
import org.dwfa.ace.task.classify.SnoPathProcessStatedCycleCheck;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.classify.SnoCon;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 *
 * @author marc
 */
/**
 *
 * @goal run-cycle-check
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class CheckCyclesMojo extends AbstractMojo {

    /**
     * Directory of the berkeley database.
     *
     * @parameter expression="${project.build.directory}/generated-resources/berkeley-db"
     * @required
     */
    private File berkeleyDir;
    /**
    <uuidClassRoot>ee9ac5d2-a07c-3981-a57a-f7f26baf38d8</uuidClassRoot>
    <uuidClassIsa>c93a30b9-ba77-3adb-a9b8-4589c9f8fb25</uuidClassIsa>
    <uuidClassUserId>7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9</uuidClassUserId>
     */
    /**
     * The uuid for role root.
     *
     * @parameter expression="6155818b-09ed-388e-82ce-caa143423e99"
     * @required
     */
    private String uuidRoleRoot;
    /**
     * The uuid for the tested path.
     *
     * @parameter
     * @required
     */
    private String uuidEditPath;
    /**
     * The time for the test in yyyy.mm.dd hh:mm:ss format
     *
     * @parameter
     */
    private String dateTimeStr;
    /**
     * Report Cycles File Name<br>
     * No report file generated if not provided.
     *
     * @parameter
     */
    private String reportCycles;
    // CORE CONSTANTS
    private static int isaNid = Integer.MAX_VALUE;
    private static int rootNid = Integer.MAX_VALUE;
    private static int rootRoleNid = Integer.MAX_VALUE;
    private static int isCURRENT = Integer.MAX_VALUE;
    private static int isLIMITED = Integer.MAX_VALUE;
    private static int isRETIRED = Integer.MAX_VALUE;
    private static int isOPTIONAL_REFINABILITY = Integer.MAX_VALUE;
    private static int isNOT_REFINABLE = Integer.MAX_VALUE;
    private static int isMANDATORY_REFINABILITY = Integer.MAX_VALUE;
    private static int isCh_STATED_RELATIONSHIP = Integer.MAX_VALUE;
    private static int isCh_DEFINING_CHARACTERISTIC = Integer.MAX_VALUE;
    private static int workbenchAuxPath = Integer.MAX_VALUE;
    private static int snorocketAuthorNid = Integer.MAX_VALUE;
    // NID SETS
    I_IntSet statusSet = null;
    PositionSetReadOnly cViewPosSet = null;
    PositionSetReadOnly cEditPosSet = null;
    I_IntSet allowedRoleTypes = null;
    // INPUT PATHS
    int cEditPathNid = Integer.MAX_VALUE; // :TODO: move to logging
    PathBI cEditPathBI = null;
    List<PositionBI> cEditPathListPositionBI = null; // Edit (Stated) Path I_Positions
    // OUTPUT PATHS
    int cViewPathNid; // :TODO: move to logging
    PathBI cViewPathBI; // Used for write back value
    List<PositionBI> cViewPathListPositionBI; // Classifier (Inferred) Path I_Positions
    // MASTER DATA SETS
    List<SnoCon> cycleSnoCons; // "Edit Path" Concepts
    // USER INTERFACE
    private Log logger;
    private I_TermFactory tf = null;
    private I_ConfigAceFrame config;
    private Precedence precedence;
    private I_ManageContradiction contradictionMgr;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            logger = getLog();
            logger.info("\r\n::: [CheckCyclesMojo] execute() -- begin");

            Bdb.setup(berkeleyDir.getAbsolutePath());
            tf = Terms.get();
            setupCoreNids();
            config = getMojoDbConfig();
            tf.setActiveAceFrameConfig(config);
            precedence = config.getPrecedence();
            contradictionMgr = config.getConflictResolutionStrategy();

            // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
            setupPaths();

            logger.info(toStringPathPos(cEditPathListPositionBI, "Edit Path"));
            logger.info(toStringPathPos(cViewPathListPositionBI, "View Path"));
            // logger.info(toStringFocusSet(tf));

            long startTime = System.currentTimeMillis();

            // PROCESS EDIT_PATH
            cycleSnoCons = new ArrayList<SnoCon>(); // List of relationships with cycles
            SnoPathProcessStatedCycleCheck pcEdit = null;
            pcEdit = new SnoPathProcessStatedCycleCheck(
                    cycleSnoCons,
                    allowedRoleTypes,
                    statusSet,
                    cEditPosSet,
                    config.getPrecedence(),
                    config.getConflictResolutionStrategy());
            tf.iterateConcepts(pcEdit);
            logger.info("\r\n::: [CheckCyclesMojo] STATED (Edit) PATH DATA : "
                    + pcEdit.getStats(startTime));

            if (cycleSnoCons.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("CYCLES DETECTED ... ");
                sb.append(cycleSnoCons.size());
                sb.append("\r\n");
                for (int i = 0; i < cycleSnoCons.size(); i++) {
                    SnoCon sc = cycleSnoCons.get(i);
                    I_GetConceptData c1 = tf.getConcept(sc.id);
                    sb.append(c1.getPrimUuid());
                    sb.append("\t");
                    sb.append(c1.getInitialText());
                    sb.append("\r\n");
                }
                logger.info("\r\n::: [CheckCyclesMojo] CYCLES DETECTED = " + cycleSnoCons.size());

//                if (reportCycles != null) {
//                    SnoRel.dumpToFile(cycleSnoCons,
//                            "target" + File.separator + reportCycles + "_FAIL.txt", 5);
//                }
            } else {
                logger.info("\r\n::: [CheckCyclesMojo] NO CYCLES DETECTED");
//                if (reportCycles != null) {
//                    SnoRel.dumpToFile(cycleSnoCons,
//                            "target" + File.separator + reportCycles + "_PASS.txt", 5);
//                }
            }

        } catch (TerminologyException ex) {
            Logger.getLogger(CheckCyclesMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CheckCyclesMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(CheckCyclesMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CheckCyclesMojo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private I_ConfigAceFrame getMojoDbConfig()
            throws TerminologyException, IOException, ParseException {
        I_ConfigAceFrame tmpConfig = null;
        tmpConfig = tf.newAceFrameConfig();
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
        tmpConfig.addViewPosition(tf.newPosition(tf.getPath(new UUID[]{UUID.fromString(uuidEditPath)}),
                df.parse(dateTimeStr).getTime()));
        // Addes inferred promotion template to catch the context relationships [ testing
        //tmpConfig.addViewPosition(tf.newPosition(tf.getPath(new UUID[] { UUID.fromString("cb0f6c0d-ebf3-5d84-9e12-d09a937cbffd") }), Integer.MAX_VALUE));
        //tmpConfig.addEditingPath(tf.getPath(new UUID[] { UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2") }));
        PathBI editPath = tf.getPath(new UUID[]{UUID.fromString(uuidEditPath)});
        tmpConfig.addEditingPath(editPath);
        tmpConfig.getDescTypes().add(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID());
        tmpConfig.getDescTypes().add(SnomedMetadataRfx.getDES_SYNONYM_PREFERRED_NAME_NID());
        tmpConfig.getDestRelTypes().add(
                ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
        tmpConfig.getDestRelTypes().add(
                ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.localize().getNid());
        tmpConfig.getDestRelTypes().add(
                Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
        tmpConfig.setDefaultStatus(tf.getConcept(SnomedMetadataRfx.getSTATUS_CURRENT_NID()));
        tmpConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_CURRENT_NID());

        tmpConfig.setClassifierIsaType(tf.getConcept(SNOMED.Concept.IS_A.getPrimoridalUid()));

        tmpConfig.setClassificationRoot(tf.getConcept(SNOMED.Concept.ROOT.getPrimoridalUid()));
        tmpConfig.setClassificationRoleRoot(tf.getConcept(UUID.fromString(uuidRoleRoot)));
        // :!!!: config.setClassifierInputPath(null);
        // :!!!: config.setClassifierOutputPath(null);

        // I_ConfigAceDb newDbProfile = tf.newAceDbConfig();
        // newDbProfile.setUsername("username");
        // newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
        // newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
        // newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
        // newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
        // tmpConfig.setDbConfig(newDbProfile);

        tmpConfig.setPrecedence(Precedence.TIME);

        return tmpConfig;
    }

    private void setupCoreNids() throws MojoFailureException {
        tf = Terms.get();

        // SETUP CORE NATIVES IDs
        try {
            // SETUP CORE NATIVES IDs
            isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
            rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());

            rootRoleNid = tf.uuidToNative(UUID.fromString(uuidRoleRoot));

            // 0 CURRENT, 1 RETIRED
            isCURRENT = SnomedMetadataRfx.getSTATUS_CURRENT_NID();
            isLIMITED = SnomedMetadataRfx.getSTATUS_LIMITED_NID();
            isRETIRED = SnomedMetadataRfx.getSTATUS_RETIRED_NID();
            isOPTIONAL_REFINABILITY = SnomedMetadataRfx.getREL_OPTIONAL_REFINABILITY_NID();
            isNOT_REFINABLE = SnomedMetadataRfx.getREL_NOT_REFINABLE_NID();
            isMANDATORY_REFINABILITY = SnomedMetadataRfx.getREL_MANDATORY_REFINABILITY_NID();
            isCh_STATED_RELATIONSHIP = SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID();
            isCh_DEFINING_CHARACTERISTIC = SnomedMetadataRfx.getREL_CH_DEFINING_CHARACTERISTIC_NID();

            snorocketAuthorNid = tf.uuidToNative(
                    ArchitectonicAuxiliary.Concept.USER.SNOROCKET.getUids());

        } catch (TerminologyException e) {
            logger.info(e.toString());
            throw new MojoFailureException("setupCoreNids", e);
        } catch (IOException e) {
            logger.info(e.toString());
            throw new MojoFailureException("setupCoreNids", e);
        }
        statusSet = tf.newIntSet();
        statusSet.add(isCURRENT);
        statusSet.add(isLIMITED);
        return;
    }

    private void setupPaths() {
        // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
        try {
            // Setup to exclude Workbench Auxiliary on path
            UUID wAuxUuid = UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66");
            I_GetConceptData wAuxCb = tf.getConcept(wAuxUuid);
            workbenchAuxPath = wAuxCb.getConceptNid();

            // GET ALL EDIT_PATH ORIGINS
            cEditPathBI = config.getEditingPathSet().iterator().next();
            cEditPathNid = cEditPathBI.getConceptNid();
            cEditPosSet = new PositionSetReadOnly(tf.newPosition(cEditPathBI, Long.MAX_VALUE));

            cEditPathListPositionBI = new ArrayList<PositionBI>();
            cEditPathListPositionBI.add(tf.newPosition(cEditPathBI, Long.MAX_VALUE));
            getPathOrigins(cEditPathListPositionBI, cEditPathBI);

            // GET ALL CLASSIFER_PATH ORIGINS
            cViewPathBI = config.getViewPositionSet().iterator().next().getPath();
            cViewPathNid = cViewPathBI.getConceptNid();
            cViewPosSet = new PositionSetReadOnly(tf.newPosition(cViewPathBI, Long.MAX_VALUE));

            cViewPathListPositionBI = new ArrayList<PositionBI>();
            cViewPathListPositionBI.add(tf.newPosition(cViewPathBI, Long.MAX_VALUE));
            getPathOrigins(cViewPathListPositionBI, cViewPathBI);

        } catch (TerminologyException e) {
            logger.info(e.toString());
        } catch (IOException e) {
            logger.info(e.toString());
        }
    }

    private void getPathOrigins(List<PositionBI> origins, PathBI p) {
        List<PositionBI> thisLevel = new ArrayList<PositionBI>();

        for (PositionBI o : p.getOrigins()) {
            origins.add(o);
            thisLevel.add(o);
        }

        // do a breadth first traversal of path origins.
        while (thisLevel.size() > 0) {
            List<PositionBI> nextLevel = new ArrayList<PositionBI>();
            for (PositionBI p1 : thisLevel) {
                for (PositionBI p2 : p1.getPath().getOrigins()) {
                    if ((origins.contains(p2) == false)
                            && (p2.getPath().getConceptNid() != workbenchAuxPath)) {
                        origins.add(p2);
                        nextLevel.add(p2);
                    }
                }
            }

            thisLevel = nextLevel;
        }
    }

    private String toStringPathPos(List<PositionBI> pathPos, String pStr) {
        // BUILD STRING
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [SnorocketTask] PATH ID -- ").append(pStr).append("\r\n");
        for (PositionBI position : pathPos) {
            s.append("::: .. PATH:\t").append(toStringCNid(position.getPath().getConceptNid())).append("\r\n");
        }
        s.append(":::");
        return s.toString();
    }

    private String toStringCNid(int cNid) {
        StringBuilder sb = new StringBuilder();
        try {
            I_GetConceptData c = tf.getConcept(cNid);
            sb.append(c.getUids().iterator().next()).append("\t");
            sb.append(cNid).append("\t");
            sb.append(c.getInitialText());
        } catch (IOException e) {
            logger.info(e.toString());
        } catch (TerminologyException e) {
            logger.info(e.toString());
        }

        return sb.toString();
    }
}
