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
import java.util.logging.Level;
import java.util.logging.Logger;
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
     * Report Equivalences File Name<br> No report file generated if not provided.
     *
     * @parameter
     */
    private String reportEquivalences;
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
    private I_ManageContradiction contradictionMgr;

    private HashSet<Integer> debugWatchNidSet() {
        HashSet<Integer> iSet = new HashSet<Integer>();
        //Combined core needle and aspiration biopsy
        //c486681e-3641-3a47-b614-09ab864f47c5
        //69902009
        UUID debugUuidIcd1111 = UUID.fromString("c486681e-3641-3a47-b614-09ab864f47c5");
        //Foobar Hazelnut Foobar (substance)
        //56d78966-fdc2-3b77-bf1c-304a18488a56
        //256353000
        UUID debugUuidIcd2222 = UUID.fromString("56d78966-fdc2-3b77-bf1c-304a18488a56");
        //Biopsy of lesion of lung (procedure)
        //29999d84-6d45-4dd6-83e8-31eb09a59780
        UUID debugUuidIcd3333 = UUID.fromString("29999d84-6d45-4dd6-83e8-31eb09a59780");
        //Aspiration biopsy of lesion of lung
        //0dd44b38-9048-3a79-9a05-eaf3430781c0
        //173191000
        UUID debugUuidIcd4444 = UUID.fromString("0dd44b38-9048-3a79-9a05-eaf3430781c0");
        //Combined core needle and aspiration biopsy
        //c486681e-3641-3a47-b614-09ab864f47c5
        //69902009
        UUID debugUuidIcd5555 = UUID.fromString("c486681e-3641-3a47-b614-09ab864f47c5");
        //Aspiration of bronchus (procedure)
        //e57412a1-802a-46e1-9a18-3f9f0e914d61
        UUID debugUuidIcd6666 = UUID.fromString("e57412a1-802a-46e1-9a18-3f9f0e914d61");
        //Aspiration of bronchus with lavage (procedure)
        //86123773-4f24-31c8-8d64-6a2e7b940f92
        //397396006
        UUID debugUuidIcd7777 = UUID.fromString("86123773-4f24-31c8-8d64-6a2e7b940f92");
        try {
            iSet.add(tf.getConcept(debugUuidIcd1111).getNid());
            iSet.add(tf.getConcept(debugUuidIcd2222).getNid());
            iSet.add(tf.getConcept(debugUuidIcd3333).getNid());
            iSet.add(tf.getConcept(debugUuidIcd4444).getNid());
            iSet.add(tf.getConcept(debugUuidIcd5555).getNid());
            iSet.add(tf.getConcept(debugUuidIcd6666).getNid());
            iSet.add(tf.getConcept(debugUuidIcd7777).getNid());
        } catch (TerminologyException ex) {
            Logger.getLogger(MultiEditorContradictionDetectorMojo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MultiEditorContradictionDetectorMojo.class.getName()).log(Level.SEVERE, null, ex);
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
            ViewCoordinate vc = config.getViewCoordinate();

            List<MultiEditorContradictionCase> cases = new ArrayList<MultiEditorContradictionCase>();
            MultiEditorContradictionDetector mecd;
            mecd = new MultiEditorContradictionDetector(commitRecRefsetNid, vc,
                    cases, debugWatchNidSet());
            // Ts.get().iterateConceptDataInSequence(mecd);
            Ts.get().iterateConceptDataInParallel(mecd);

            if (cases.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("\r\n::: [MultiEditorContradictionDetectionMojo] FOUND CONTRADICTIONS\r\n");
                for (MultiEditorContradictionCase contradictionCase : cases) {
                    I_GetConceptData concept = tf.getConcept(contradictionCase.getcNid());
                    sb.append("::: CONTRADICTING COMMIT_RECORDS: ");
                    sb.append(concept.getPrimUuid().toString());
                    sb.append(" ");
                    sb.append(concept.toUserString());
                    sb.append("\r\n");
                }
                logger.info(sb.toString());
            } else {
                logger.info("::: [MultiEditorContradictionDetectionMojo] NO CONTRADICTIONS FOUND.");
            }

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
