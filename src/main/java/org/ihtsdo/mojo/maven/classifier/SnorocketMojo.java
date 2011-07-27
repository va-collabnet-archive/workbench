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

import au.csiro.snorocket.core.IFactory_123;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_Callback;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_EquivalentCallback;
import au.csiro.snorocket.snapi.Snorocket_123;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.classify.SnoCon;
import org.dwfa.ace.task.classify.SnoConGrp;
import org.dwfa.ace.task.classify.SnoConGrpList;
import org.dwfa.ace.task.classify.SnoConSer;
import org.dwfa.ace.task.classify.SnoDL;
import org.dwfa.ace.task.classify.SnoDLSet;
import org.dwfa.ace.task.classify.SnoGrp;
import org.dwfa.ace.task.classify.SnoGrpList;
import org.dwfa.ace.task.classify.SnoPathProcessInferred;
import org.dwfa.ace.task.classify.SnoPathProcessStated;
import org.dwfa.ace.task.classify.SnoQuery;
import org.dwfa.ace.task.classify.SnoRel;
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
 *
 */
/**
 * 
 * @goal run-snorocket
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class SnorocketMojo extends AbstractMojo {

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
     * Enable Database Writeback true/false
     *
     * @parameter default-value="false"
     */
    private boolean enableDbWriteback;
    /**
     * Report Changes File Name<br>
     * No report file generated if not provided.
     *
     * @parameter
     */
    private String reportChanges;
    /**
     * Report Equivalences File Name<br>
     * No report file generated if not provided.
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
    List<SnoRel> cEditSnoRels; // "Edit Path" Concepts
    List<SnoCon> cEditSnoCons; // "Edit Path" Relationships
    List<SnoRel> cClassSnoRels; // "Classifier Path" Relationships
    List<SnoRel> cRocketSnoRels; // "Snorocket Results Set" Relationships
    // USER INTERFACE
    private Log logger;
    private I_TermFactory tf = null;
    private I_ConfigAceFrame config;
    private Precedence precedence;
    private I_ManageContradiction contradictionMgr;

    private class ProcessResults implements I_Callback {

        private List<SnoRel> snorels;
        private int countRel = 0; // STATISTICS COUNTER

        public ProcessResults(List<SnoRel> snorels) {
            this.snorels = snorels;
            this.countRel = 0;
        }

        @Override
        public void addRelationship(int conceptId1, int roleId, int conceptId2, int group) {
            countRel++;
            SnoRel relationship = new SnoRel(conceptId1, conceptId2, roleId, group);
            snorels.add(relationship);
            if (countRel % 25000 == 0) {
                // ** GUI: ProcessResults
                logger.info("rels processed " + countRel);
            }
        }
    }

    private class ProcessEquiv implements I_EquivalentCallback {

        private int countConSet = 0; // STATISTICS COUNTER

        public ProcessEquiv() {
            SnoQuery.clearEquiv();
        }

        @Override
        public void equivalent(ArrayList<Integer> equivalentConcepts) {
            SnoQuery.getEquiv().add(new SnoConGrp(equivalentConcepts));
            countConSet += 1;
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        logger = getLog();
        logger.info("\r\n::: [SnorocketMojo] execute() -- begin");
        SnoQuery.initAll();

        try {
            Bdb.setup(berkeleyDir.getAbsolutePath());
            tf = Terms.get();
            config = getMojoDbConfig();
            tf.setActiveAceFrameConfig(config);
            precedence = config.getPrecedence();
            contradictionMgr = config.getConflictResolutionStrategy();

            setupCoreNids();

            // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
            setupPaths();

            logger.info(toStringPathPos(cEditPathListPositionBI, "Edit Path"));
            logger.info(toStringPathPos(cViewPathListPositionBI, "View Path"));
            // logger.info(toStringFocusSet(tf));

            long startTime = System.currentTimeMillis();

            // SETUP ROLE NID ARRAY
            int[] rNidArray = setupRoleNids();
            int nextRIdx = rNidArray.length;
            if (rNidArray.length > 100) {
                String errStr = "Role types exceeds 100. This will cause a memory issue. "
                        + "Please check that role root is set to 'Concept mode attribute'";
                logger.error(errStr);
                throw new MojoFailureException(errStr);
            }

            // GET EDIT_PATH CONCEPTS AND RELATIONSHIPS
            cEditSnoCons = new ArrayList<SnoCon>();
            cEditSnoRels = new ArrayList<SnoRel>();

            SnoPathProcessStated pcEdit = null;
            pcEdit = new SnoPathProcessStated(null, cEditSnoCons, cEditSnoRels,
                    allowedRoleTypes, statusSet, cEditPosSet, null, config.getPrecedence(),
                    config.getConflictResolutionStrategy());
            tf.iterateConcepts(pcEdit);
            logger.info("\r\n::: [SnorocketMojo] GET STATED (Edit) PATH DATA : "
                    + pcEdit.getStats(startTime));

            // SETUP CONCEPT NID ARRAY
            final int reserved = 2;
            int margin = cEditSnoCons.size() >> 2; // Add 50%
            int[] cNidArray = new int[cEditSnoCons.size() + margin + reserved];
            cNidArray[IFactory_123.TOP_CONCEPT] = IFactory_123.TOP;
            cNidArray[IFactory_123.BOTTOM_CONCEPT] = IFactory_123.BOTTOM;

            Collections.sort(cEditSnoCons);
            if (cEditSnoCons.get(0).id <= Integer.MIN_VALUE + reserved) {
                throw new MojoFailureException("::: SNOROCKET: TOP & BOTTOM nids NOT reserved");
            }
            int nextCIdx = reserved;
            for (SnoCon sc : cEditSnoCons) {
                cNidArray[nextCIdx++] = sc.id;
            }
            // Fill array to make binary search work correctly.
            Arrays.fill(cNidArray, nextCIdx, cNidArray.length, Integer.MAX_VALUE);

            // SETUP CLASSIFIER
            Snorocket_123 rocket_123 = new Snorocket_123(cNidArray, nextCIdx, rNidArray, nextRIdx,
                    rootNid);

            // SnomedMetadata :: ISA
            rocket_123.setIsaNid(isaNid);

            // SnomedMetadata :: ROLE_ROOTS
            rocket_123.setRoleRoot(isaNid, true); // @@@
            rocket_123.setRoleRoot(rootRoleNid, false);

            // SET DEFINED CONCEPTS
            for (int i = 0; i < cEditSnoCons.size(); i++) {
                if (cEditSnoCons.get(i).isDefined) {
                    rocket_123.setConceptIdxAsDefined(i + reserved);
                }
            }

            // ADD RELATIONSHIPS
            Collections.sort(cEditSnoRels);
            for (SnoRel sr : cEditSnoRels) {
                int err = rocket_123.addRelationship(sr.c1Id, sr.typeId, sr.c2Id, sr.group);
                if (err > 0) {
                    StringBuilder sb = new StringBuilder();
                    if ((err & 1) == 1) {
                        sb.append(" --UNDEFINED_C1-- ");
                    }
                    if ((err & 2) == 2) {
                        sb.append(" --UNDEFINED_ROLE-- ");
                    }
                    if ((err & 4) == 4) {
                        sb.append(" --UNDEFINED_C2-- ");
                    }
                    logger.info("\r\n::: " + sb /* :!!!: + dumpSnoRelStr(sr) */);
                }
            }

            /* ****************
             * // SnomedMetadata :: RIGHT_IDENTITIES // direct-substance o
             * has-active-ingredient -> direct-substance // SNOMED IDs
             * {"363701004", "127489000"}
             *
             * // SnomedId "363701004" // direct-substance //
             * 49ee3912-abb7-325c-88ba-a98824b4c47d int nidDirectSubstance =
             * tf.getId(
             * UUID.fromString("49ee3912-abb7-325c-88ba-a98824b4c47d"))
             * .getNid();
             *
             * // SnomedId "127489000" // has-active-ingredient //
             * 65bf3b7f-c854-36b5-81c3-4915461020a8 int nidHasActiveIngredient =
             * tf.getId(
             * UUID.fromString("65bf3b7f-c854-36b5-81c3-4915461020a8"))
             * .getNid();
             *
             * int lhsData[] = new int[2]; lhsData[0] = nidDirectSubstance;
             * lhsData[1] = nidHasActiveIngredient;
             * rocket_123.addRoleComposition(lhsData, nidDirectSubstance);
             * ***************
             */

            // ROLE_COMPOSITION List
            ArrayList<SnoDL> dll = SnoDLSet.getDLList();
            if (dll != null) {
                for (SnoDL sdl : dll) {
                    rocket_123.addRoleComposition(sdl.getLhsNids(), sdl.getRhsNid());
                }
                logger.info("\r\n::: [SnorocketMojo] Logic Added");
            }

            // NEVER_GROUPED
            // SnomedId "123005000" part-of
            // UUID ngUUid = UUID.fromString("b4c3f6f9-6937-30fd-8412-d0c77f8a7f73");
            // rocket_123.setRoleNeverGrouped(tf.getId(ngUUid).getNid());

            // SnomedId "272741003" laterality
            // ngUUid = UUID.fromString("26ca4590-bbe5-327c-a40a-ba56dc86996b");
            // rocket_123.setRoleNeverGrouped(tf.getId(ngUUid).getNid());

            // SnomedId "127489000" has-active-ingredient
            // ngUUid = UUID.fromString("65bf3b7f-c854-36b5-81c3-4915461020a8");
            // rocket_123.setRoleNeverGrouped(tf.getId(ngUUid).getNid());

            // SnomedId "411116001" // has-dose-form
            // ngUUid = UUID.fromString("072e7737-e22e-36b5-89d2-4815f0529c63");
            // rocket_123.setRoleNeverGrouped(tf.getId(ngUUid).getNid());

            // NEVER_GROUPED List
            ArrayList<SnoConSer> ngl = SnoDLSet.getNeverGroup();
            if (ngl != null) {
                for (SnoConSer scs : ngl) {
                    rocket_123.setRoleNeverGrouped(scs.id);
                }

                logger.info("\r\n::: [SnorocketMojo] \"Never-Grouped\" Added");
            }

            logger.info("\r\n::: [SnorocketMojo] SORTED & ADDED CONs, RELs *** LAPSE TIME = "
                    + toStringLapseSec(startTime) + " ***");

            cEditSnoCons = null; // :MEMORY:
            cEditSnoRels = null; // :MEMORY:
            pcEdit = null; // :MEMORY:
            System.gc();

            // RUN CLASSIFIER
            startTime = System.currentTimeMillis();
            logger.info("::: Starting Classifier... ");
            rocket_123.classify();
            logger.info("::: Time to classify (ms): " + (System.currentTimeMillis() - startTime));

            // GET CLASSIFER EQUIVALENTS
            logger.info("::: GET EQUIVALENT CONCEPTS...");
            startTime = System.currentTimeMillis();
            ProcessEquiv pe = new ProcessEquiv();
            rocket_123.getEquivalents(pe);
            logger.info("\r\n::: [SnorocketMojo] ProcessEquiv() count=" + pe.countConSet
                    + " time= " + toStringLapseSec(startTime));

            // GET CLASSIFER RESULTS
            cRocketSnoRels = new ArrayList<SnoRel>();
            logger.info("::: GET CLASSIFIER RESULTS...");
            startTime = System.currentTimeMillis();
            ProcessResults pr = new ProcessResults(cRocketSnoRels);
            rocket_123.getDistributionFormRelationships(pr);
            logger.info("\r\n::: [SnorocketMojo] GET CLASSIFIER RESULTS count=" + pr.countRel
                    + " time= " + toStringLapseSec(startTime));

            pr = null; // :MEMORY:
            rocket_123 = null; // :MEMORY:
            System.gc();
            System.gc();

            // GET CLASSIFIER_PATH RELS
            startTime = System.currentTimeMillis();
            cClassSnoRels = new ArrayList<SnoRel>();
            SnoPathProcessInferred pcClass = null;
            pcClass = new SnoPathProcessInferred(null, cClassSnoRels, allowedRoleTypes,
                    statusSet, cEditPosSet, cViewPosSet, null, precedence, contradictionMgr);
            tf.iterateConcepts(pcClass);
            logger.info("\r\n::: [SnorocketMojo] GET INFERRED (View) PATH DATA : "
                    + pcClass.getStats(startTime));

            // FILTER RELATIONSHIPS
            int last = cClassSnoRels.size();
            for (int idx = last - 1; idx > -1; idx--) {
                if (Arrays.binarySearch(cNidArray, cClassSnoRels.get(idx).c2Id) < 0) {
                    cClassSnoRels.remove(idx);
                }
            }

            pcClass = null; // :MEMORY:

            // WRITEBACK RESULTS
            startTime = System.currentTimeMillis();
            logger.info(compareAndWriteBack(cClassSnoRels, cRocketSnoRels, cViewPathNid));

            // Commit
            // :!!!: tf.commit(ChangeSetPolicy.OFF, ChangeSetWriterThreading.SINGLE_THREAD);

            logger.info("\r\n::: *** WRITEBACK *** LAPSED TIME =\t" + toStringLapseSec(startTime) + "\t ***");

        } catch (MojoFailureException e) {
            logger.info("\r\n::: TerminologyException");
            logger.info(e.toString());
            throw new MojoFailureException("::: TerminologyException", e);
        } catch (IOException e) {
            logger.info("\r\n::: IOException");
            logger.info(e.toString());
            throw new MojoFailureException("::: IOException", e);
        } catch (Exception e) {
            logger.info("\r\n::: Exception");
            logger.info(e.toString());
            throw new MojoFailureException("::: Exception", e);
        }

        if (SnoQuery.getIsaAdded().size() > 0
                || SnoQuery.getIsaDropped().size() > 0
                || SnoQuery.getRoleAdded().size() > 0
                || SnoQuery.getRoleDropped().size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("\r\n::: [SnorocketMojo] ISA ADD  = ").append(SnoQuery.getIsaAdded().size());
            sb.append("\r\n::: [SnorocketMojo] ISA DROP = ").append(SnoQuery.getIsaDropped().size());
            sb.append("\r\n::: [SnorocketMojo] ROLE ADD = ").append(SnoQuery.getRoleAdded().size());
            sb.append("\r\n::: [SnorocketMojo] ROLE DROP = ").append(SnoQuery.getRoleDropped().size());
            sb.append(sb.toString());
            if (reportChanges != null) {
                SnoRel.dumpToFile(SnoQuery.getIsaAdded(),
                        "target" + File.separator + reportChanges + "_ISA_ADD.txt", 2);
                SnoRel.dumpToFile(SnoQuery.getIsaDropped(),
                        "target" + File.separator + reportChanges + "_ISA_DROP.txt", 2);
                SnoRel.dumpToFile(SnoQuery.getRoleAdded(),
                        "target" + File.separator + reportChanges + "_ROLE_ADD.txt", 2);
                SnoRel.dumpToFile(SnoQuery.getRoleDropped(),
                        "target" + File.separator + reportChanges + "_ROLE_DROP.txt", 2);
            }
        } else {
            logger.info("\r\n::: [SnorocketMojo] NO CLASSIFICATION CHANGES");
            if (reportChanges != null) {
                SnoRel.dumpToFile(SnoQuery.getIsaAdded(),
                        "target" + File.separator + reportChanges + "_NO_CHANGES.txt", 2);
            }
        }

        if (SnoQuery.getEquiv().size() > 0) {
            logger.info("\r\n::: [SnorocketMojo] EQUIVALENCES DETECTED = " + SnoQuery.getEquiv().size());
            if (reportEquivalences != null) {
                SnoConGrpList.dumpSnoConGrpList(SnoQuery.getEquiv(),
                        "target" + File.separator + reportEquivalences + "_FAIL.txt");
            }
        } else {
            logger.info("\r\n::: [SnorocketMojo] NO EQUIVALENCES DETECTED");
            if (reportEquivalences != null) {
                SnoConGrpList.dumpSnoConGrpList(SnoQuery.getEquiv(),
                        "target" + File.separator + reportEquivalences + "_PASS.txt");
            }
        }

        cClassSnoRels = null; // :MEMORY:
        cRocketSnoRels = null; // :MEMORY:
    }

    private String compareAndWriteBack(List<SnoRel> snorelA, List<SnoRel> snorelB, int classPathNid)
            throws TerminologyException, IOException {
        // Actual write back approximately 16,380 per minute
        // Write back dropped to approximately 1,511 per minute
        long vTime = System.currentTimeMillis();

        // STATISTICS COUNTERS
        int countConSeen = 0;
        int countSame = 0;
        int countSameISA = 0;
        int countA_Diff = 0;
        int countA_DiffISA = 0;
        int countA_Total = 0;
        int countB_Diff = 0;
        int countB_DiffISA = 0;
        int countB_Total = 0;

        // SETUP CLASSIFIER QUERY
        SnoQuery.clearDiff();

        long startTime = System.currentTimeMillis();
        Collections.sort(snorelA);
        Collections.sort(snorelB);

        // Typically, A is the Classifier Path (for previously inferred)
        // Typically, B is the SnoRocket Results Set (for newly inferred)
        Iterator<SnoRel> itA = snorelA.iterator();
        Iterator<SnoRel> itB = snorelB.iterator();
        SnoRel rel_A = itA.next();
        SnoRel rel_B = itB.next();
        boolean done_A = false;
        boolean done_B = false;

        logger.info("\r\n::: [SnorocketMojo]"
                + "\r\n::: snorelA.size() = \t" + snorelA.size()
                + "\r\n::: snorelB.size() = \t" + snorelB.size());

        // BY SORT ORDER, LOWER NUMBER ADVANCES FIRST
        while (!done_A && !done_B) {
            if (++countConSeen % 25000 == 0) {
                logger.info("::: [SnorocketMojo] compareAndWriteBack @ #\t" + countConSeen);
            }

            if (rel_A.c1Id == rel_B.c1Id) {
                // COMPLETELY PROCESS ALL C1 FOR BOTH IN & OUT
                // PROCESS C1 WITH GROUP == 0
                int thisC1 = rel_A.c1Id;

                // PROCESS WHILE BOTH HAVE GROUP 0
                while (rel_A.c1Id == thisC1 && rel_B.c1Id == thisC1 && rel_A.group == 0
                        && rel_B.group == 0 && !done_A && !done_B) {

                    // PROGESS GROUP ZERO
                    switch (compareSnoRel(rel_A, rel_B)) {
                        case 1: // SAME
                            // GATHER STATISTICS
                            countSame++;
                            countA_Total++;
                            countB_Total++;
                            if (rel_A.typeId == isaNid) {
                                countSameISA++;
                            }
                            // NOTHING TO WRITE IN THIS CASE
                            if (itA.hasNext()) {
                                rel_A = itA.next();
                            } else {
                                done_A = true;
                            }
                            if (itB.hasNext()) {
                                rel_B = itB.next();
                            } else {
                                done_B = true;
                            }
                            break;

                        case 2: // REL_A > REL_B -- B has extra stuff
                            // WRITEBACK REL_B (Classifier Results) AS CURRENT
                            countB_Diff++;
                            countB_Total++;
                            if (rel_B.typeId == isaNid) {
                                countB_DiffISA++;
                            }
                            writeBackCurrent(rel_B, classPathNid, vTime);

                            if (itB.hasNext()) {
                                rel_B = itB.next();
                            } else {
                                done_B = true;
                            }
                            break;

                        case 3: // REL_A < REL_B -- A has extra stuff
                            // WRITEBACK REL_A (Classifier Input) AS RETIRED
                            // GATHER STATISTICS
                            countA_Diff++;
                            countA_Total++;
                            if (rel_A.typeId == isaNid) {
                                countA_DiffISA++;
                            }
                            writeBackRetired(rel_A, classPathNid, vTime);

                            if (itA.hasNext()) {
                                rel_A = itA.next();
                            } else {
                                done_A = true;
                            }
                            break;
                    } // switch
                }

                // REMAINDER LIST_A GROUP 0 FOR C1
                while (rel_A.c1Id == thisC1 && rel_A.group == 0 && !done_A) {
                    countA_Diff++;
                    countA_Total++;
                    if (rel_A.typeId == isaNid) {
                        countA_DiffISA++;
                    }
                    writeBackRetired(rel_A, classPathNid, vTime);
                    if (itA.hasNext()) {
                        rel_A = itA.next();
                    } else {
                        done_A = true;
                    }
                    break;
                }

                // REMAINDER LIST_B GROUP 0 FOR C1
                while (rel_B.c1Id == thisC1 && rel_B.group == 0 && !done_B) {
                    countB_Diff++;
                    countB_Total++;
                    if (rel_B.typeId == isaNid) {
                        countB_DiffISA++;
                    }
                    writeBackCurrent(rel_B, classPathNid, vTime);
                    if (itB.hasNext()) {
                        rel_B = itB.next();
                    } else {
                        done_B = true;
                    }
                    break;
                }

                // ** SEGMENT GROUPS **
                SnoGrpList groupList_A = new SnoGrpList();
                SnoGrpList groupList_B = new SnoGrpList();
                SnoGrp groupA = null;
                SnoGrp groupB = null;

                // SEGMENT GROUPS IN LIST_A
                int prevGroup = Integer.MIN_VALUE;
                while (rel_A.c1Id == thisC1 && !done_A) {
                    if (rel_A.group != prevGroup) {
                        groupA = new SnoGrp();
                        groupList_A.add(groupA);
                    }

                    groupA.add(rel_A);

                    prevGroup = rel_A.group;
                    if (itA.hasNext()) {
                        rel_A = itA.next();
                    } else {
                        done_A = true;
                    }
                }
                // SEGMENT GROUPS IN LIST_B
                prevGroup = Integer.MIN_VALUE;
                while (rel_B.c1Id == thisC1 && !done_B) {
                    if (rel_B.group != prevGroup) {
                        groupB = new SnoGrp();
                        groupList_B.add(groupB);
                    }

                    groupB.add(rel_B);

                    prevGroup = rel_B.group;
                    if (itB.hasNext()) {
                        rel_B = itB.next();
                    } else {
                        done_B = true;
                    }
                }

                // FIND GROUPS IN GROUPLIST_A WITHOUT AN EQUAL IN GROUPLIST_B
                // WRITE THESE GROUPED RELS AS "RETIRED"
                SnoGrpList groupList_NotEqual;
                if (groupList_A.size() > 0) {
                    groupList_NotEqual = groupList_A.whichNotEqual(groupList_B);
                    for (SnoGrp sg : groupList_NotEqual) {
                        for (SnoRel sr_A : sg) {
                            writeBackRetired(sr_A, classPathNid, vTime);
                        }
                    }
                    countA_Total += groupList_A.countRels();
                    countA_Diff += groupList_NotEqual.countRels();
                }

                // FIND GROUPS IN GROUPLIST_B WITHOUT AN EQUAL IN GROUPLIST_A
                // WRITE THESE GROUPED RELS AS "NEW, CURRENT"
                if (groupList_B.size() > 0) {
                    groupList_NotEqual = groupList_B.whichNotEqual(groupList_A);
                    for (SnoGrp sg : groupList_NotEqual) {
                        for (SnoRel sr_B : sg) {
                            writeBackCurrent(sr_B, classPathNid, vTime);
                        }
                    }
                    countB_Total += groupList_A.countRels();
                    countB_Diff += groupList_NotEqual.countRels();
                }
            } else if (rel_A.c1Id > rel_B.c1Id) {
                // CASE 2: LIST_B HAS CONCEPT NOT IN LIST_A
                // COMPLETELY *ADD* ALL THIS C1 FOR REL_B AS NEW, CURRENT
                int thisC1 = rel_B.c1Id;
                while (rel_B.c1Id == thisC1) {
                    countB_Diff++;
                    countB_Total++;
                    if (rel_B.typeId == isaNid) {
                        countB_DiffISA++;
                    }
                    writeBackCurrent(rel_B, classPathNid, vTime);
                    if (itB.hasNext()) {
                        rel_B = itB.next();
                    } else {
                        done_B = true;
                        break;
                    }
                }

            } else {
                // CASE 3: LIST_A HAS CONCEPT NOT IN LIST_B
                // COMPLETELY *RETIRE* ALL THIS C1 FOR REL_A
                int thisC1 = rel_A.c1Id;
                while (rel_A.c1Id == thisC1) {
                    countA_Diff++;
                    countA_Total++;
                    if (rel_A.typeId == isaNid) {
                        countA_DiffISA++;
                    }
                    writeBackRetired(rel_A, classPathNid, vTime);
                    if (itA.hasNext()) {
                        rel_A = itA.next();
                    } else {
                        done_A = true;
                        break;
                    }
                }
            }
        }

        // AT THIS POINT, THE PREVIOUS C1 HAS BE PROCESSED COMPLETELY
        // AND, EITHER REL_A OR REL_B HAS BEEN COMPLETELY PROCESSED
        // AND, ANY REMAINDER IS ONLY ON REL_LIST_A OR ONLY ON REL_LIST_B
        // AND, THAT REMAINDER HAS A "STANDALONE" C1 VALUE
        // THEREFORE THAT REMAINDER WRITEBACK COMPLETELY
        // AS "NEW CURRENT" OR "OLD RETIRED"
        //
        // LASTLY, IF .NOT.DONE_A THEN THE NEXT REL_A IN ALREADY IN PLACE
        while (!done_A) {
            countA_Diff++;
            countA_Total++;
            if (rel_A.typeId == isaNid) {
                countA_DiffISA++;
            }
            // COMPLETELY UPDATE ALL REMAINING REL_A AS RETIRED
            writeBackRetired(rel_A, classPathNid, vTime);
            if (itA.hasNext()) {
                rel_A = itA.next();
            } else {
                done_A = true;
                break;
            }
        }

        while (!done_B) {
            countB_Diff++;
            countB_Total++;
            if (rel_B.typeId == isaNid) {
                countB_DiffISA++;
            }
            // COMPLETELY UPDATE ALL REMAINING REL_B AS NEW, CURRENT
            writeBackCurrent(rel_B, classPathNid, vTime);
            if (itB.hasNext()) {
                rel_B = itB.next();
            } else {
                done_B = true;
                break;
            }
        }

        // CHECKPOINT DATABASE

        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [SnorocketMojo] compareAndWriteBack()");
        long lapseTime = System.currentTimeMillis() - startTime;
        s.append("\r\n::: [Time] Sort/Compare Input & Output: \t").append(lapseTime);
        s.append("\t(mS)\t").append(((float) lapseTime / 1000) / 60).append("\t(min)");
        s.append("\r\n");
        s.append("\r\n::: ");
        s.append("\r\n::: countSame:     \t").append(countSame);
        s.append("\r\n::: countSameISA:  \t").append(countSameISA);
        s.append("\r\n::: A == Classifier Output Path");
        s.append("\r\n::: countA_Diff:   \t").append(countA_Diff);
        s.append("\r\n::: countA_DiffISA:\t").append(countA_DiffISA);
        s.append("\r\n::: countA_Total:  \t").append(countA_Total);
        s.append("\r\n::: B == Classifier Solution Set");
        s.append("\r\n::: countB_Diff:   \t").append(countB_Diff);
        s.append("\r\n::: countB_DiffISA:\t").append(countB_DiffISA);
        s.append("\r\n::: countB_Total:  \t").append(countB_Total);
        s.append("\r\n::: ");

        return s.toString();
    }

    private void writeBackRetired(SnoRel rel_A, int writeToNid, long versionTime)
            throws IOException {
        if (rel_A.typeId == isaNid) {
            SnoQuery.getIsaDropped().add(rel_A);
        } else {
            SnoQuery.getRoleDropped().add(rel_A);
        }

        if (enableDbWriteback == false) {
            return;
        }

        try {
            I_RelVersioned rBean = tf.getRelationship(rel_A.relNid);
            if (rBean != null) {
                List<? extends I_RelTuple> rvList = rBean.getSpecifiedVersions(statusSet,
                        cViewPosSet, precedence, contradictionMgr);

                if (rvList.size() == 1) {
                    // CREATE RELATIONSHIP PART W/ TermFactory
                    rvList.get(0).makeAnalog(isRETIRED, snorocketAuthorNid, writeToNid, versionTime);
                    I_GetConceptData thisC1 = tf.getConcept(rel_A.c1Id);
                    tf.addUncommittedNoChecks(thisC1);

                } else if (rvList.isEmpty()) {
                    logger.info("::: [SnorocketMojo] ERROR: writeBackRetired() "
                            + "empty version list" + "\trelNid=\t" + rel_A.relNid
                            + "\tc1=\t" + rel_A.c1Id
                            + "\t" + tf.getConcept(rel_A.c1Id).toLongString());
                } else {
                    logger.info("::: [SnorocketMojo] ERROR: writeBackRetired() "
                            + "multiple last versions"
                            + "\trelNid=\t" + rel_A.relNid
                            + "\tc1=\t" + rel_A.c1Id
                            + "\t" + tf.getConcept(rel_A.c1Id).toLongString());
                }
            } else {
                logger.info("::: [SnorocketMojo] ERROR: writeBackRetired() "
                        + "tf.getRelationship(" + rel_A.relNid + ") == null");
            }

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void writeBackCurrent(SnoRel rel_B, int writeToNid, long versionTime)
            throws TerminologyException, IOException {
        if (rel_B.typeId == isaNid) {
            SnoQuery.getIsaAdded().add(rel_B);
        } else {
            SnoQuery.getRoleAdded().add(rel_B);
        }

        if (enableDbWriteback == false) {
            return;
        }

        I_GetConceptData thisC1 = tf.getConcept(rel_B.c1Id);
        // @@@ WRITEBACK NEW ISAs --> ALL NEW RELATIONS
        // CREATE RELATIONSHIP PART W/ TermFactory-->VobdEnv
        tf.newRelationshipNoCheck(UUID.randomUUID(), thisC1, rel_B.typeId, rel_B.c2Id,
                isCh_DEFINING_CHARACTERISTIC, isOPTIONAL_REFINABILITY, rel_B.group, isCURRENT,
                snorocketAuthorNid, writeToNid, versionTime);

        // :!!!:TODO: [SnorocketMojo] move addUncommittedNoChecks() to more efficient location.
        // more optimal to only call once per concept.
        tf.addUncommittedNoChecks(thisC1);
    }

    private int compareSnoRel(SnoRel inR, SnoRel outR) {
        if ((inR.c1Id == outR.c1Id) && (inR.group == outR.group) && (inR.typeId == outR.typeId)
                && (inR.c2Id == outR.c2Id)) {
            return 1; // SAME
        } else if (inR.c1Id > outR.c1Id) {
            return 2; // ADDED
        } else if ((inR.c1Id == outR.c1Id) && (inR.group > outR.group)) {
            return 2; // ADDED
        } else if ((inR.c1Id == outR.c1Id) && (inR.group == outR.group)
                && (inR.typeId > outR.typeId)) {
            return 2; // ADDED
        } else if ((inR.c1Id == outR.c1Id) && (inR.group == outR.group)
                && (inR.typeId == outR.typeId) && (inR.c2Id > outR.c2Id)) {
            return 2; // ADDED
        } else {
            return 3; // DROPPED
        }
    } // compareSnoRel

    private I_ConfigAceFrame getMojoDbConfig()
            throws TerminologyException, IOException, ParseException {
        I_ConfigAceFrame tmpConfig = null;
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

        // :!!!: config.setClassificationRoot(null);
        // :!!!: config.setClassificationRoleRoot(null);
        // :!!!: config.setClassifierIsaType(null);
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

            snorocketAuthorNid =
                    tf.uuidToNative(ArchitectonicAuxiliary.Concept.USER.SNOROCKET.getUids());

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

    private int[] setupRoleNids() throws TerminologyException, IOException {
        int countRelDuplVersion = 0;
        LinkedHashSet<Integer> resultSet = new LinkedHashSet<Integer>();

        I_GetConceptData rootConcept = tf.getConcept(rootRoleNid);
        Collection<? extends I_RelVersioned> thisLevel = rootConcept.getDestRels();
        while (thisLevel.size() > 0) {
            ArrayList<I_RelVersioned> nextLevel = new ArrayList<I_RelVersioned>();
            for (I_RelVersioned<?> rv : thisLevel) {
                I_RelPart rPart1 = null;
                for (PositionBI pos : cEditPathListPositionBI) { // PATHS_IN_PRIORITY_ORDER
                    for (I_RelPart rPart : rv.getMutableParts()) {
                        if (pos.getPath().getConceptNid() == rPart.getPathId()) {
                            if (rPart1 == null) {
                                rPart1 = rPart; // ... KEEP FIRST_INSTANCE
                            } else if (rPart1.getVersion() < rPart.getVersion()) {
                                rPart1 = rPart; // ... KEEP MORE_RECENT PART
                            } else if (rPart1.getVersion() == rPart.getVersion()) {
                                countRelDuplVersion++;
                                if (rPart.getStatusId() == isCURRENT) {
                                    rPart1 = rPart; // KEEP CURRENT PART
                                }
                            }
                        }
                    }
                    if (rPart1 != null) {
                        break; // IF FOUND ON THIS PATH, STOP SEARCHING
                    }
                }

                if ((rPart1 != null) && (rPart1.getStatusId() == isCURRENT)
                        && (rPart1.getTypeId() == isaNid)) {
                    // KEEP C1 AS RESULT
                    resultSet.add(rv.getC1Id());

                    // GET C1 DESTINATION RELS FOR NEXT LEVEL
                    I_GetConceptData c1 = tf.getConcept(rv.getC1Id());
                    Collection<? extends I_RelVersioned> c1Rels = c1.getDestRels();
                    for (I_RelVersioned nextRel : c1Rels) {
                        nextLevel.add(nextRel);
                    }
                }
            } // for thisLevel

            thisLevel = nextLevel;
        } // while thisLevel

        // IS-A is outside the active SNOMED role root
        resultSet.add(isaNid);

        // prepare the role nid array.
        int[] resultInt = new int[resultSet.size()];
        int i = 0;
        for (Integer rNid : resultSet) {
            resultInt[i] = rNid;
            i++;
        }
        Arrays.sort(resultInt);

        //
        StringBuilder sb = new StringBuilder("::: ALLOWED ROLES = " + resultInt.length
                + "\t **\r\n");
        for (int cNid : resultInt) {
            sb.append(":::   \t").append(toStringCNid(cNid)).append("\r\n");
        }
        logger.info(sb.toString());

        allowedRoleTypes = tf.newIntSet();
        allowedRoleTypes.addAll(resultInt);
        return resultInt;
    }

    private String toStringLapseSec(long startTime) {
        StringBuilder s = new StringBuilder();
        long stopTime = System.currentTimeMillis();
        long lapseTime = stopTime - startTime;
        s.append((float) lapseTime / 1000).append(" (seconds)");
        return s.toString();
    }

    private String toStringPathPos(List<PositionBI> pathPos, String pStr) {
        // BUILD STRING
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [SnorocketMojo] PATH ID -- ").append(pStr).append("\r\n");
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
