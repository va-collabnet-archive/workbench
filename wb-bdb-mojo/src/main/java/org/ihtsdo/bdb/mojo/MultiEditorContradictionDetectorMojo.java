/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.dwfa.ace.api.*;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.helper.bdb.MultiEditorContradictionCase;
import org.ihtsdo.helper.bdb.MultiEditorContradictionDetector;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 *
 * @goal find-contradictions
 */
public class MultiEditorContradictionDetectorMojo extends AbstractMojo {

    /**
     * Directory of the berkeley database.
     *
     * @parameter expression="${project.build.directory}/generated-resources/berkeley-db"
     */
    private File berkeleyDir;
    /**
     * <uuidClassRoot>ee9ac5d2-a07c-3981-a57a-f7f26baf38d8</uuidClassRoot>
     * <uuidClassIsa>c93a30b9-ba77-3adb-a9b8-4589c9f8fb25</uuidClassIsa>
     * <uuidClassUserId>7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9</uuidClassUserId>
     */
    /**
     * The uuid for role root.
     *
     * @parameter expression="6155818b-09ed-388e-82ce-caa143423e99" @required
     */
    private String uuidRoleRoot;
    /**
     * The uuid for the tested path.
     *
     * @parameter @required
     */
    private String uuidEditPath;
    /**
     * The time for the test in yyyy.mm.dd hh:mm:ss format
     *
     * @parameter
     */
    private String dateTimeStr;
    /**
     * UUIDs to watch.
     *
     * @parameter
     */
    private String[] watchUuidList;
    /**
     * Report Contradictions File Name<br> No report file generated if not provided.
     *
     * @parameter
     */
    private String reportName;
    // CORE CONSTANTS
    private static int isaNid = Integer.MAX_VALUE;
    private static int rootNid = Integer.MAX_VALUE;
    private static int rootRoleNid = Integer.MAX_VALUE;
    private static int isCURRENT = Integer.MAX_VALUE;
    private static int isLIMITED = Integer.MAX_VALUE;
    private static int isRETIRED = Integer.MAX_VALUE;
    private static int workbenchAuxPath = Integer.MAX_VALUE;
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
    // USER INTERFACE
    private Log logger;
    private I_TermFactory tf = null;
    private I_ConfigAceFrame config;
    private Precedence precedence;
    private ContradictionManagerBI contradictionMgr;

    private HashSet<Integer> debugWatchNidSet() {
        HashSet<Integer> iSet = new HashSet<Integer>();

        if (watchUuidList != null) {
            for (String uuidString : watchUuidList) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    iSet.add(tf.getConcept(uuid).getNid());
                } catch (Exception ex) {
                    logger.error("BAD UUID: " + uuidString, ex);
                }

            }
        }

        return iSet;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        logger = getLog();
        logger.info("\r\n::: [MultiEditorContradictionDetectionMojo] execute() -- begin");

        tf = Terms.get();
        if (tf == null) {
            Bdb.setup(berkeleyDir.getAbsolutePath());
        }
        tf = Terms.get();
        config = getMojoDbConfig();
        try {
            tf.setActiveAceFrameConfig(config);
            precedence = config.getPrecedence();
            contradictionMgr = config.getConflictResolutionStrategy();

            setupCoreNids();

            // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
            setupPaths();

            logger.info(toStringPathPos(cEditPathListPositionBI, "Edit Path"));
            logger.info(toStringPathPos(cViewPathListPositionBI, "View Path"));

            long startTime = System.currentTimeMillis();
            int commitRecRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.COMMIT_RECORD.getUids());
            int adjudicationRecRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.ADJUDICATION_RECORD.getUids());
            ViewCoordinate vc = config.getViewCoordinate();

            List<MultiEditorContradictionCase> cases;
            MultiEditorContradictionDetector mecd;
            StringBuilder sb;
            
            // *** TEST CASE 1 ***
//            cases = new ArrayList<MultiEditorContradictionCase>();
//            mecd = new MultiEditorContradictionDetector(commitRecRefsetNid,
//                    adjudicationRecRefsetNid,
//                    vc, // ViewCoordinates
//                    cases, // Resulting Cases
//                    debugWatchNidSet(),
//                    false, // ignoreReadOnlySap ... AuthorTime from refset for given concept
//                    false); // ignoreNonVisibleAth ... AuthorTime from concept
//            Ts.get().iterateConceptDataInSequence(mecd);
//            // Ts.get().iterateConceptDataInParallel(mecd);
//
//            // REPORT COMPONENTS WITH MISSING COMMIT RECORDS
//            sb = new StringBuilder();
//            sb.append("\r\n**** COMPONENTS MISSING COMMITRECORDS ****");
//            sb.append("\r\n[MultiEditorContradictionDetectionMojo] MISSING COMMITRECORDS LIST\r\n");
//            sb.append(mecd.toStringMissingCommitRecords());
//            sb.append("\r\n");
//            logger.info(sb.toString());


            // REPORT CASE 1 RESULTS
//            if (cases.size() > 0) {
//                // CONTRADICTIONS
//                sb = new StringBuilder();
//                sb.append("\r\n***** CONTRADICTION CASE 1: IGNORE NOTHING *****");
//                sb.append("\r\n[MultiEditorContradictionDetectionMojo] FOUND CONTRADICTIONS\r\n");
//                for (MultiEditorContradictionCase contradictionCase : cases) {
//                    sb.append(contradictionCase.toStringLong());
//                    sb.append("\r\n");
//                }
//                logger.info(sb.toString());
//
//            } else {
//                sb = new StringBuilder();
//                sb.append("\r\n***** CASE 1: IGNORE NOTHING *****");
//                sb.append("\r\n[MultiEditorContradictionDetectionMojo] NO CONTRADICTIONS FOUND");
//                logger.info(sb.toString());
//            }

            // *** TEST CASE 2 ***
//            cases = new ArrayList<MultiEditorContradictionCase>();
//            mecd = new MultiEditorContradictionDetector(commitRecRefsetNid,
//                    adjudicationRecRefsetNid,
//                    vc, // ViewCoordinates
//                    cases, // Resulting Cases
//                    debugWatchNidSet(),
//                    true, // ignoreReadOnlySap ... AuthorTime from refset for given concept
//                    false); // ignoreNonVisibleAth ... AuthorTime from concept
//            Ts.get().iterateConceptDataInSequence(mecd);
//            // Ts.get().iterateConceptDataInParallel(mecd);
//
//            // report results
//            if (cases.size() > 0) {
//                sb = new StringBuilder();
//                sb.append("\r\n***** CASE 2: DON'T COMPUTE READONLY *****");
//                sb.append("\r\n[MultiEditorContradictionDetectionMojo] FOUND CONTRADICTIONS\r\n");
//                for (MultiEditorContradictionCase contradictionCase : cases) {
//                    sb.append(contradictionCase.toStringLong());
//                    sb.append("\r\n");
//                }
//                logger.info(sb.toString());
//            } else {
//                sb = new StringBuilder();
//                sb.append("\r\n***** CASE 2: DON'T COMPUTE READONLY *****");
//                sb.append("\r\n[MultiEditorContradictionDetectionMojo] NO CONTRADICTIONS FOUND");
//                logger.info(sb.toString());
//            }

            // *** TEST CASE 3 ***
            cases = new ArrayList<MultiEditorContradictionCase>();
            mecd = new MultiEditorContradictionDetector(commitRecRefsetNid,
                    adjudicationRecRefsetNid,
                    vc, // ViewCoordinates
                    cases, // Resulting Cases
                    debugWatchNidSet(),
                    true, // ignoreReadOnlySap ... AuthorTime from refset for given concept
                    true); // ignoreNonVisibleAth ... AuthorTime from concept
            Ts.get().iterateConceptDataInSequence(mecd);
            // Ts.get().iterateConceptDataInParallel(mecd);

            // report results
            if (cases.size() > 0) {
                sb = new StringBuilder();
                sb.append("\r\n***** CASE 3: DON'T COMPUTE READONLY + IGNORE NONCOMPUTED *****");
                sb.append("\r\n[MultiEditorContradictionDetectionMojo] FOUND CONTRADICTIONS\r\n");
                for (MultiEditorContradictionCase contradictionCase : cases) {
                    sb.append(contradictionCase.toStringLong());
                    sb.append("\r\n");
                }
                logger.info(sb.toString());
            } else {
                sb = new StringBuilder();
                sb.append("\r\n***** CASE 3: DON'T COMPUTE READONLY + IGNORE NONCOMPUTED *****");
                sb.append("\r\n[MultiEditorContradictionDetectionMojo] NO CONTRADICTIONS FOUND");
                logger.info(sb.toString());
            }

            // WATCH RESULTS -- NOT CONTRADICTIONS
            List<MultiEditorContradictionCase> watchCaseList = mecd.getWatchCaseList();
            sb = new StringBuilder();
            sb.append("\r\n**** NON-CONTRADICTING WATCH CASE 1: IGNORE NOTHING ****");
            sb.append("\r\n[MultiEditorContradictionDetectionMojo] WATCH");
            if (watchCaseList != null) {
                sb.append("\r\n-- WATCH LIST --");
                for (String uuidStr : watchUuidList) {
                    sb.append("\r\n    ");
                    sb.append(uuidStr);
                }

                for (MultiEditorContradictionCase watchCase : watchCaseList) {
                    sb.append("\r\n-- NON-CONTRADICTING WATCH CASE --");
                    sb.append(watchCase.toStringLong());
                    sb.append("\r\n");
                }
            }
            logger.info(sb.toString());

        } catch (Exception ex) {
            logger.error("\r\n::: [MultiEditorContradictionDetectionMojo] TerminologyException", ex);
            throw new MojoFailureException("Exception", ex);
        }

        logger.info("\r\n::: [MultiEditorContradictionDetectionMojo] execute() -- end");
    }

    private I_ConfigAceFrame getMojoDbConfig()
            throws MojoFailureException {
        try {
            I_ConfigAceFrame tmpConfig;
            tmpConfig = tf.newAceFrameConfig();
            DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
            tmpConfig.addViewPosition(tf.newPosition(tf.getPath(new UUID[]{UUID.fromString(uuidEditPath)}), df.parse(dateTimeStr).getTime()));
            // Addes inferred promotion template to catch the context relationships [ testing
            //tmpConfig.addViewPosition(tf.newPosition(tf.getPath(new UUID[] { UUID.fromString("cb0f6c0d-ebf3-5d84-9e12-d09a937cbffd") }), Integer.MAX_VALUE));
            //tmpConfig.addEditingPath(tf.getPath(new UUID[] { UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2") }));
            PathBI editPath = tf.getPath(new UUID[]{UUID.fromString(uuidEditPath)});
            tmpConfig.addEditingPath(editPath);
            tmpConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
            tmpConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
            tmpConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
            tmpConfig.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
            tmpConfig.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.localize().getNid());
            tmpConfig.getDestRelTypes().add(Terms.get().uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
            tmpConfig.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
            tmpConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            tmpConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());

            tmpConfig.setClassifierIsaType(tf.getConcept(SNOMED.Concept.IS_A.getPrimoridalUid()));

            // I_ConfigAceDb newDbProfile = tf.newAceDbConfig();
            // newDbProfile.setUsername("username");
            // newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
            // newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
            // newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
            // newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
            // tmpConfig.setDbConfig(newDbProfile);

            tmpConfig.setPrecedence(Precedence.TIME);

            return tmpConfig;
        } catch (ParseException ex) {
            logger.error("\r\n::: [MultiEditorContradictionDetectionMojo] ParseException", ex);
            throw new MojoFailureException("IOException", ex);
        } catch (IOException ex) {
            logger.error("\r\n::: [MultiEditorContradictionDetectionMojo] IOException", ex);
            throw new MojoFailureException("IOException", ex);
        } catch (TerminologyException ex) {
            logger.error("\r\n::: [MultiEditorContradictionDetectionMojo] TerminologyException", ex);
            throw new MojoFailureException("TerminologyException", ex);
        }
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
        s.append("\r\n::: [MultiEditorContradictionDetectionMojo] PATH ID -- ").append(pStr).append("\r\n");
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
            if (!c.isCanceled()) {
                if (c.getUids().iterator().hasNext()) {
                    sb.append(c.getUids().iterator().next());
                    sb.append("\t");
                } else {
                    sb.append("NO_UUID\t");
                }
                sb.append(cNid).append("\t");
                sb.append(c.getInitialText());
            } else {
                sb.append("CANCELED\t");
                sb.append(cNid).append("\t");
                sb.append(c.getInitialText());
            }
        } catch (IOException e) {
            logger.info(e.toString());
        } catch (TerminologyException e) {
            logger.info(e.toString());
        }

        return sb.toString();
    }
}
