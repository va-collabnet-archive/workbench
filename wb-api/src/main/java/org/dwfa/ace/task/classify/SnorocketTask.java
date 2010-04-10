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
package org.dwfa.ace.task.classify;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
//import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec; //import org.ihtsdo.db.bdb.Bdb;

import au.csiro.snorocket.core.IFactory_123;
import au.csiro.snorocket.snapi.Snorocket_123;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_Callback;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_EquivalentCallback;

/**
 * 
 * SnorocketTask retrieves concepts and relationship from the stated edit path
 * and load the same to the IHTSDO (Snorocket) classifier.
 * 
 * Classification is run and the resulting inferred relationships is written
 * back
 * to the database.
 * 
 * <ul>
 * <li>The is-a relationship is unique to the classification. For example, the
 * SNOMED is-a has a different concept id than the ace-auxiliary is-a
 * relationship. So every concept (except the concept root) will have at least
 * one is-a relationship of the proper type.
 * <li>There is a single root concept, and that root is part of the set of
 * included concept
 * <li>Assumes that the versions are linear, independent of path, and therefore
 * the status with the latest date on an allowable path is the latest status.
 * <li>Only current concepts and relationships are to be used and that all other
 * statuses will can be filtered out.
 * 
 * <ul>
 * </ol>
 * <p>
 * <p>
 * <ul>
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class SnorocketTask extends AbstractTask implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    // CORE CONSTANTS
    private static int isaNid = Integer.MIN_VALUE;
    private static int rootNid = Integer.MIN_VALUE;
    private static int rootRoleNid = Integer.MIN_VALUE;
    private static int isCURRENT = Integer.MIN_VALUE;
    private static int isRETIRED = Integer.MIN_VALUE;
    private static int isOPTIONAL_REFINABILITY = Integer.MIN_VALUE;
    private static int isNOT_REFINABLE = Integer.MIN_VALUE;
    private static int isMANDATORY_REFINABILITY = Integer.MIN_VALUE;
    private static int isCh_STATED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_DEFINING_CHARACTERISTIC = Integer.MIN_VALUE;
    private static int isCh_STATED_AND_INFERRED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_STATED_AND_SUBSUMED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int sourceUnspecifiedNid;
    private static int workbenchAuxPath = Integer.MIN_VALUE;

    // NID SETS
    I_IntSet statusSet = null;
    PositionSetReadOnly cClassPosSet = null;
    PositionSetReadOnly cEditPosSet = null;
    I_IntSet allowedRoleTypes = null;

    // INPUT PATHS
    int cEditPathNid = Integer.MIN_VALUE; // :TODO: move to logging
    I_Path cEditIPath = null;
    List<I_Position> cEditPathPos = null; // Edit (Stated) Path I_Positions

    // OUTPUT PATHS
    int cClassPathNid; // :TODO: move to logging
    I_Path cClassIPath; // Used for write back value
    List<I_Position> cClassPathPos; // Classifier (Inferred) Path I_Positions

    // MASTER DATA SETS
    List<SnoRel> cEditSnoRels; // "Edit Path" Concepts
    List<SnoCon> cEditSnoCons; // "Edit Path" Relationships
    List<SnoRel> cClassSnoRels; // "Classifier Path" Relationships
    List<SnoRel> cRocketSnoRels; // "Snorocket Results Set" Relationships

    // USER INTERFACE
    private Logger logger;
    I_TermFactory tf = null;
    I_ConfigAceFrame config = null;
    I_ShowActivity gui = null;
    private boolean continueThisAction = true;

    // INTERNAL
    private static boolean debug = false; // :DEBUG:
    private static boolean debugDump = false; // :DEBUG: save to files

    public void actionPerformed(ActionEvent arg0) {
        continueThisAction = false;
    }

    /**
     * <b><font color=blue>ProcessResults</font></b><br>
     * Retrieves results from classifier.<br>
     * <br>
     * <b>First Classification Run</b><br>
     * <i>The Classifier may create a new part for an existing relationship.<br>
     * </i> PROCESS:<br>
     * <code>Search for existing concept to get relationship with same origin/dest/type.<br>
     * CASE (search == empty)<br>
     * create new origin/dest/type "inferred" relationship w/ status "current"<br>
     * add part to the new relationship on the TO PATH<br>
     * CASE (search == success)<br>
     * * add part to the existing "stated" relationship on the TO PATH</code>
     * <p>
     * <b>Subsequent Classification Runs</b><br>
     * Case A. NEW: Create new relationship with status CURRENT. see "new" above
     * <br>
     * Case B. SAME: Do nothing.<br>
     * Case C. ABSENT: WAS in previous, NOT in current. Set status to RETIRED.
     * 
     */
    private class ProcessResults implements I_Callback {
        private List<SnoRel> snorels;
        private int countRel = 0; // STATISTICS COUNTER

        public ProcessResults(List<SnoRel> snorels) {
            this.snorels = snorels;
            this.countRel = 0;
        }

        public void addRelationship(int conceptId1, int roleId, int conceptId2, int group) {
            countRel++;
            SnoRel relationship = new SnoRel(conceptId1, conceptId2, roleId, group);
            snorels.add(relationship);
            if (countRel % 25000 == 0) {
                // ** GUI: ProcessResults
                gui.setValue(countRel);
                gui.setProgressInfoLower("rels processed " + countRel);
            }
        }

    }

    private class ProcessEquiv implements I_EquivalentCallback {
        private int countConSet = 0; // STATISTICS COUNTER

        public ProcessEquiv() {
            SnoQuery.clearEquiv();
        }

        public void equivalent(ArrayList<Integer> equivalentConcepts) {
            SnoQuery.equivCon.add(new SnoConGrp(equivalentConcepts));
            countConSet += 1;
        }

    }

    /**
     * POSSIBLE APPROACHES FOR RECIEVING DATA FROM NEW DATABASE<br>
     * 1. Get List<SnoCon>, use List<SnoCon> as post processing filter.<br>
     * 2. Get List<SnoCon>, use List<SnoCon> as filter in second pass.<br>
     * 3. Get List<SnoCon>, use List<SnoCon> in individually get Rels in loop.<br>
     * 4. Check relationship C2 while processing path.<br>
     * 5. Modify processConcepts internal.<br>
     */

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        logger = worker.getLogger();
        logger.info("\r\n::: [SnorocketTask] evaluate() -- begin");

        tf = Terms.get();
        SnoQuery.initAll();

        if (setupCoreNids().equals(Condition.STOP))
            return Condition.STOP;
        logger.info(toStringNids());

        // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
        if (setupPaths().equals(Condition.STOP))
            return Condition.STOP;
        logger.info(toStringPathPos(cEditPathPos, "Edit Path"));
        logger.info(toStringPathPos(cClassPathPos, "Classifier Path"));
        // logger.info(toStringFocusSet(tf));

        try {
            // ** GUI: 1. LOAD DATA INTO CLASSIFIER **
            continueThisAction = true;
            gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig()); // in
            // activity
            // viewer
            gui.addActionListener(this);
            gui.setProgressInfoUpper("Classifier 1/5: load data");
            gui.setIndeterminate(false);
            gui.setMaximum(1500000);
            gui.setValue(0);
            long startTime = System.currentTimeMillis();

            // SETUP ROLE NID ARRAY
            int[] rNidArray = setupRoleNids();
            int nextRIdx = rNidArray.length;

            // GET EDIT_PATH CONCEPTS AND RELATIONSHIPS
            cEditSnoCons = new ArrayList<SnoCon>();
            cEditSnoRels = new ArrayList<SnoRel>();
            SnoPathProcessConcepts pcEdit = new SnoPathProcessConcepts(logger, cEditSnoCons,
                    cEditSnoRels, allowedRoleTypes, statusSet, cEditPosSet, gui, false);
            tf.iterateConcepts(pcEdit); // :!!!:
            // Bdb.getConceptDb().iterateConceptDataInSequence(pcEdit);
            logger
                    .info("\r\n::: [SnorocketTask] GET STATED PATH DATA"
                            + pcEdit.getStats(startTime));

            // SETUP CONCEPT NID ARRAY
            final int reserved = 2;
            int margin = cEditSnoCons.size() >> 2; // Add 25%
            int[] cNidArray = new int[cEditSnoCons.size() + margin + reserved];
            cNidArray[IFactory_123.TOP_CONCEPT] = IFactory_123.TOP;
            cNidArray[IFactory_123.BOTTOM_CONCEPT] = IFactory_123.BOTTOM;

            Collections.sort(cEditSnoCons);
            if (cEditSnoCons.get(0).id <= Integer.MIN_VALUE + reserved)
                throw new TaskFailedException("::: SNOROCKET: TOP & BOTTOM nids NOT reserved");
            int nextCIdx = reserved;
            for (SnoCon sc : cEditSnoCons)
                cNidArray[nextCIdx++] = sc.id;
            // Fill array to make binary search work correctly.
            Arrays.fill(cNidArray, nextCIdx, cNidArray.length, Integer.MAX_VALUE);

            // FILTER RELATIONSHIPS
            /* Snorocket_123 is self filtering on C2
            int last = cEditSnoRels.size();
            for (int idx = last - 1; idx > -1; idx--)
                if (Arrays.binarySearch(cNidArray, cEditSnoRels.get(idx).c2Id) < 0)
                    cEditSnoRels.remove(idx);
            */

            if (debugDump) {
                dumpSnoCon(cEditSnoCons, "SnoConEditData_full.txt", 4);
                dumpSnoRel(cEditSnoRels, "SnoRelEditData_full.txt", 4);
                Collections.sort(cEditSnoCons);
                dumpSnoCon(cEditSnoCons, "SnoConEditData_compare.txt", 5);
                Collections.sort(cEditSnoRels);
                dumpSnoRel(cEditSnoRels, "SnoRelEditData_compare.txt", 5);
            }

            // SETUP CLASSIFIER
            Snorocket_123 rocket_123 = new Snorocket_123(cNidArray, nextCIdx, rNidArray, nextRIdx,
                    rootNid);
            // SET ISA
            rocket_123.setIsaNid(isaNid);
            rocket_123.setRoleRoot(isaNid, true); // @@@
            rocket_123.setRoleRoot(rootRoleNid, false);

            // SET DEFINED CONCEPTS
            for (int i = 0; i < cEditSnoCons.size(); i++)
                if (cEditSnoCons.get(i).isDefined)
                    rocket_123.setConceptIdxAsDefined(i + reserved);

            // ADD RELATIONSHIPS
            Collections.sort(cEditSnoRels);
            for (SnoRel sr : cEditSnoRels) {
                int err = rocket_123.addRelationship(sr.c1Id, sr.typeId, sr.c2Id, sr.group);
                if (debugDump && err > 0) {
                    StringBuilder sb = new StringBuilder();
                    if ((err & 1) == 1)
                        sb.append(" --UNDEFINED_C1-- ");
                    if ((err & 2) == 2)
                        sb.append(" --UNDEFINED_ROLE-- ");
                    if ((err & 4) == 4)
                        sb.append(" --UNDEFINED_C2-- ");
                    logger.info("\r\n::: " + sb + dumpSnoRelStr(sr));
                }
            }

            // 
            ArrayList<SnoDL> dll = SnoDLSet.getDLList();
            if (dll != null) {
                for (SnoDL sdl : dll) {
                    rocket_123.addRoleComposition(sdl.getLhsNids(), sdl.getRhsNid());
                }
                logger.info("\r\n::: [SnorocketTask] Logic Added");
            }
            // 
            ArrayList<SnoConSer> ngl = SnoDLSet.getNeverGroup();
            if (ngl != null) {
                for (SnoConSer scs : ngl)
                    rocket_123.setRoleNeverGrouped(scs.id);

                logger.info("\r\n::: [SnorocketTask] \"Never-Grouped\" Added");
            }

            logger.info("\r\n::: [SnorocketTask] SORTED & ADDED CONs, RELs" + " *** LAPSE TIME = "
                    + toStringLapseSec(startTime) + " ***");

            // ** GUI: 1. LOAD DATA -- done **
            if (continueThisAction) {
                gui.setProgressInfoLower("edit path rels = " + pcEdit.countRelAdded
                        + ", lapsed time = " + toStringLapseSec(startTime));
                gui.complete(); // PHASE 1. DONE
            } else {
                gui.setProgressInfoLower("classification stopped by user");
                gui.complete(); // PHASE 1. DONE
                return Condition.CONTINUE;
            }
            
            cEditSnoCons = null; // :MEMORY:
            cEditSnoRels = null; // :MEMORY:
            pcEdit = null; // :MEMORY:
            System.gc();

            // ** GUI: 2 RUN CLASSIFIER **
            gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig()); // in
            // activity
            // viewer
            gui.addActionListener(this);
            gui.setProgressInfoUpper("Classifier 2/5: classify data");
            gui.setProgressInfoLower("... can take 4 to 6 minutes ...");
            gui.setIndeterminate(true);

            // RUN CLASSIFIER
            startTime = System.currentTimeMillis();
            logger.info("::: Starting Classifier... ");
            rocket_123.classify();
            logger.info("::: Time to classify (ms): " + (System.currentTimeMillis() - startTime));

            // ** GUI: PHASE 2. -- done
            if (continueThisAction) {
                gui.setProgressInfoLower("classification complete, time = "
                        + toStringLapseSec(startTime));
                gui.complete();
            } else {
                gui.setProgressInfoLower("classification stopped by user");
                gui.complete(); // PHASE 1. DONE
                return Condition.CONTINUE;
            }

            // ** GUI: * GET CLASSIFIER EQUIVALENTS **
            // Show in activity viewer
            gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig());
            gui.addActionListener(this);
            gui.setProgressInfoUpper("Classifier */*: retrieve equivalent concepts");
            gui.setIndeterminate(true);
            // gui.setMaximum(1000000);
            // gui.setValue(0);

            // GET CLASSIFER EQUIVALENTS
            worker.getLogger().info("::: GET EQUIVALENT CONCEPTS...");
            startTime = System.currentTimeMillis();
            ProcessEquiv pe = new ProcessEquiv();
            rocket_123.getEquivalents(pe);
            logger.info("\r\n::: [SnorocketTask] ProcessEquiv() count=" + pe.countConSet
                    + " time= " + toStringLapseSec(startTime));

            // ** GUI: * -- done
            if (continueThisAction) {
                gui.setProgressInfoLower("solution set rels = " + pe.countConSet
                        + ", lapsed time = " + toStringLapseSec(startTime));
                gui.complete(); // GET CONCEPT EQUIVALENTS -- done
                pe = null; // :MEMORY:
            } else {
                gui.setProgressInfoLower("get evquivalents stopped by user");
                gui.complete(); // PHASE 1. DONE
                return Condition.CONTINUE;
            }

            // ** GUI: 3 GET CLASSIFIER RESULTS **
            gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig()); // in
            // activity
            // viewer
            gui.addActionListener(this);
            gui.setProgressInfoUpper("Classifier 3/5: retrieve solution set");
            gui.setIndeterminate(false);
            gui.setMaximum(1000000);
            gui.setValue(0);

            // GET CLASSIFER RESULTS
            cRocketSnoRels = new ArrayList<SnoRel>();
            worker.getLogger().info("::: GET CLASSIFIER RESULTS...");
            startTime = System.currentTimeMillis();
            ProcessResults pr = new ProcessResults(cRocketSnoRels);
            rocket_123.getDistributionFormRelationships(pr);
            logger.info("\r\n::: [SnorocketTask] GET CLASSIFIER RESULTS count=" + pr.countRel
                    + " time= " + toStringLapseSec(startTime));

            // ** GUI: 3 -- done
            if (continueThisAction) {
                gui.setProgressInfoLower("solution set rels = " + pr.countRel + ", lapsed time = "
                        + toStringLapseSec(startTime));
                gui.complete(); // 3 GET CLASSIFIER RESULTS -- done
                pr = null; // :MEMORY:
                rocket_123 = null; // :MEMORY:
                System.gc();
            } else {
                gui.setProgressInfoLower("classification stopped by user");
                gui.complete(); // PHASE 1. DONE
                rocket_123 = null; // :MEMORY:
                return Condition.CONTINUE;
            }

            if (debugDump) {
                dumpSnoRel(cRocketSnoRels, "SnoRelInferData_full.txt", 4);
                Collections.sort(cRocketSnoRels);
                dumpSnoRel(cRocketSnoRels, "SnoRelInferData_compare.txt", 5);
            }

            // ** GUI: 4 GET CLASSIFIER PATH DATA **
            gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig()); // in
            // activity
            // viewer
            gui.addActionListener(this);
            String tmpS = "Classifier 4/5: get previously inferred & compare";
            gui.setProgressInfoUpper(tmpS);
            gui.setIndeterminate(false);
            gui.setMaximum(1000000);
            gui.setValue(0);

            // GET CLASSIFIER_PATH RELS
            cClassSnoRels = new ArrayList<SnoRel>();
            startTime = System.currentTimeMillis();
            SnoPathProcessConcepts pcClass = new SnoPathProcessConcepts(logger, null,
                    cClassSnoRels, allowedRoleTypes, statusSet, cClassPosSet, gui, true);
            tf.iterateConcepts(pcClass);
            logger.info("\r\n::: [SnorocketTask] GET INFERRED PATH DATA"
                    + pcClass.getStats(startTime));

            // FILTER RELATIONSHIPS
            int last = cClassSnoRels.size();
            for (int idx = last - 1; idx > -1; idx--)
                if (Arrays.binarySearch(cNidArray, cClassSnoRels.get(idx).c2Id) < 0)
                    cClassSnoRels.remove(idx);

            if (debugDump) {
                Collections.sort(cClassSnoRels);
                dumpSnoRel(cClassSnoRels, "SnoRelCPathData_full.txt", 4);
                dumpSnoRel(cClassSnoRels, "SnoRelCPathData_compare.txt", 5);
            }

            // ** GUI: 4 -- done
            if (continueThisAction) {
                gui.setProgressInfoLower("classifier path prior rels = " + pcClass.countRelAdded
                        + ", lapsed time = " + toStringLapseSec(startTime));
                gui.complete(); // -- done
            } else {
                gui.setProgressInfoLower("classification stopped by user");
                gui.complete(); // PHASE 1. DONE
                return Condition.CONTINUE;
            }
            pcClass = null; // :MEMORY:

            // ** GUI: 5 WRITE BACK RESULTS **
            gui.complete(); // PHASE 5. DONE
            gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig()); // in
            // activity
            // viewer
            gui.addActionListener(this);
            gui.setProgressInfoUpper("Classifier 5/5: write back updates" + " to classifier path");
            gui.setIndeterminate(true);

            // WRITEBACK RESULTS
            startTime = System.currentTimeMillis();
            logger.info(compareAndWriteBack(cClassSnoRels, cRocketSnoRels, cClassPathNid));
            logger.info("\r\n::: *** WRITEBACK *** LAPSED TIME =\t" + toStringLapseSec(startTime)
                    + " ***");

            if (debugDump) {
                ArrayList<SnoRel> sqrl = SnoQuery.getIsaAdded();
                Collections.sort(sqrl);
                dumpSnoRel(SnoQuery.getIsaAdded(), "SnoRelIsaAdd_full.txt", 4);
                dumpSnoRel(SnoQuery.getIsaAdded(), "SnoRelIsaAdd_compare.txt", 5);

                sqrl = SnoQuery.getIsaDropped();
                Collections.sort(sqrl);
                dumpSnoRel(sqrl, "SnoRelIsaDrop_full.txt", 4);
                dumpSnoRel(sqrl, "SnoRelIsaDrop_compare.txt", 5);

                sqrl = SnoQuery.getRoleAdded();
                Collections.sort(sqrl);
                dumpSnoRel(sqrl, "SnoRelRoleAdd_full.txt", 4);
                dumpSnoRel(sqrl, "SnoRelRoleAdd_compare.txt", 5);

                sqrl = SnoQuery.getRoleDropped();
                Collections.sort(sqrl);
                dumpSnoRel(sqrl, "SnoRelRoleDrop_full.txt", 4);
                dumpSnoRel(sqrl, "SnoRelRoleDrop_compare.txt", 5);
            }

            // ** GUI: 5 COMPLETE **
            gui.setProgressInfoLower("writeback completed, lapsed time = "
                    + toStringLapseSec(startTime));
            gui.complete(); // PHASE 5. DONE

        } catch (TerminologyException e) {
            logger.info("\r\n::: TerminologyException");
            e.printStackTrace();
            throw new TaskFailedException("::: TerminologyException", e);
        } catch (IOException e) {
            logger.info("\r\n::: IOException");
            e.printStackTrace();
            throw new TaskFailedException("::: IOException", e);
        } catch (Exception e) {
            logger.info("\r\n::: Exception");
            e.printStackTrace();
            throw new TaskFailedException("::: Exception", e);
        }

        cClassSnoRels = null; // :MEMORY:
        cRocketSnoRels = null; // :MEMORY:
        return Condition.CONTINUE;
    }

    private void getPathOrigins(List<I_Position> origins, I_Path p) {
        List<I_Position> thisLevel = new ArrayList<I_Position>();

        for (I_Position o : p.getOrigins()) {
            origins.add(o);
            thisLevel.add(o);
        }

        // do a breadth first traversal of path origins.
        while (thisLevel.size() > 0) {
            List<I_Position> nextLevel = new ArrayList<I_Position>();
            for (I_Position p1 : thisLevel) {
                for (I_Position p2 : p1.getPath().getOrigins())
                    if ((origins.contains(p2) == false)
                            && (p2.getPath().getConceptId() != workbenchAuxPath)) {
                        origins.add(p2);
                        nextLevel.add(p2);
                    }
            }

            thisLevel = nextLevel;
        }
    }

    /**
     * 
     * USE CASE: <b>A</b> = ClassPath (previously inferred), <b>B</b> = Rocket
     * (newly inferred)<br>
     * <br>
     * Differing previously inferred <b>A</b> are retired. Differing new
     * inferred <b>B</b> are created as CURRENT, NEW VERSION. <br>
     * <BR>
     * <font color=#990099> IMPLEMENTATION NOTE: <code>snorelA</code> and
     * <code>snorelB</code> MUST be pre-sorted in C1-Group-Type-C2 order for
     * this routine. Pre-sorting is used to provide overall computational
     * efficiency.</font>
     * 
     * @param <code>List&lt;SnoRel&gt; snorelA // previously inferred</code>
     * @param <code>List&lt;SnoRel&gt; snorelB // currently inferred</code>
     * @param <code><b>int</b> classPathNid // classifier path native id </code>
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private String compareAndWriteBack(List<SnoRel> snorelA, List<SnoRel> snorelB, int classPathNid)
            throws TerminologyException, IOException {
        long vTime;
        vTime = System.currentTimeMillis();

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

        logger.info("\r\n::: [SnorocketTask]" + "\r\n::: snorelA.size() = \t" + snorelA.size()
                + "\r\n::: snorelB.size() = \t" + snorelB.size());

        // BY SORT ORDER, LOWER NUMBER ADVANCES FIRST
        while (!done_A && !done_B) {
            if (++countConSeen % 25000 == 0) {
                logger.info("::: [SnorocketTask] compareAndWriteBack @ #\t" + countConSeen);
            }
            // Actual write back approximately 16,380 per minute

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
                        if (rel_A.typeId == isaNid)
                            countSameISA++;
                        // NOTHING TO WRITE IN THIS CASE
                        if (itA.hasNext())
                            rel_A = itA.next();
                        else
                            done_A = true;
                        if (itB.hasNext())
                            rel_B = itB.next();
                        else
                            done_B = true;
                        break;

                    case 2: // REL_A > REL_B -- B has extra stuff
                        // WRITEBACK REL_B (Classifier Results) AS CURRENT
                        countB_Diff++;
                        countB_Total++;
                        if (rel_B.typeId == isaNid)
                            countB_DiffISA++;
                        writeBackCurrent(rel_B, classPathNid, vTime);

                        if (itB.hasNext())
                            rel_B = itB.next();
                        else
                            done_B = true;
                        break;

                    case 3: // REL_A < REL_B -- A has extra stuff
                        // WRITEBACK REL_A (Classifier Input) AS RETIRED
                        // GATHER STATISTICS
                        countA_Diff++;
                        countA_Total++;
                        if (rel_A.typeId == isaNid)
                            countA_DiffISA++;
                        writeBackRetired(rel_A, classPathNid, vTime);

                        if (itA.hasNext())
                            rel_A = itA.next();
                        else
                            done_A = true;
                        break;
                    } // switch
                }

                // REMAINDER LIST_A GROUP 0 FOR C1
                while (rel_A.c1Id == thisC1 && rel_A.group == 0 && !done_A) {
                    countA_Diff++;
                    countA_Total++;
                    if (rel_A.typeId == isaNid)
                        countA_DiffISA++;
                    writeBackRetired(rel_A, classPathNid, vTime);
                    if (itA.hasNext())
                        rel_A = itA.next();
                    else
                        done_A = true;
                    break;
                }

                // REMAINDER LIST_B GROUP 0 FOR C1
                while (rel_B.c1Id == thisC1 && rel_B.group == 0 && !done_B) {
                    countB_Diff++;
                    countB_Total++;
                    if (rel_B.typeId == isaNid)
                        countB_DiffISA++;
                    writeBackCurrent(rel_B, classPathNid, vTime);
                    if (itB.hasNext())
                        rel_B = itB.next();
                    else
                        done_B = true;
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
                    if (itA.hasNext())
                        rel_A = itA.next();
                    else
                        done_A = true;
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
                    if (itB.hasNext())
                        rel_B = itB.next();
                    else
                        done_B = true;
                }

                // FIND GROUPS IN GROUPLIST_A WITHOUT AN EQUAL IN GROUPLIST_B
                // WRITE THESE GROUPED RELS AS "RETIRED"
                SnoGrpList groupList_NotEqual;
                if (groupList_A.size() > 0) {
                    groupList_NotEqual = groupList_A.whichNotEqual(groupList_B);
                    for (SnoGrp sg : groupList_NotEqual)
                        for (SnoRel sr_A : sg)
                            writeBackRetired(sr_A, classPathNid, vTime);
                    countA_Total += groupList_A.countRels();
                    countA_Diff += groupList_NotEqual.countRels();
                }

                // FIND GROUPS IN GROUPLIST_B WITHOUT AN EQUAL IN GROUPLIST_A
                // WRITE THESE GROUPED RELS AS "NEW, CURRENT"
                if (groupList_B.size() > 0) {
                    groupList_NotEqual = groupList_B.whichNotEqual(groupList_A);
                    for (SnoGrp sg : groupList_NotEqual)
                        for (SnoRel sr_B : sg)
                            writeBackCurrent(sr_B, classPathNid, vTime);
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
                    if (rel_B.typeId == isaNid)
                        countB_DiffISA++;
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
                    if (rel_A.typeId == isaNid)
                        countA_DiffISA++;
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
            if (rel_A.typeId == isaNid)
                countA_DiffISA++;
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
            if (rel_B.typeId == isaNid)
                countB_DiffISA++;
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
        s.append("\r\n::: [SnorocketTask] compareAndWriteBack()");
        long lapseTime = System.currentTimeMillis() - startTime;
        s.append("\r\n::: [Time] Sort/Compare Input & Output: \t" + lapseTime + "\t(mS)\t"
                + (((float) lapseTime / 1000) / 60) + "\t(min)");
        s.append("\r\n");
        s.append("\r\n::: ");
        s.append("\r\n::: countSame:     \t" + countSame);
        s.append("\r\n::: countSameISA:  \t" + countSameISA);
        s.append("\r\n::: countA_Diff:   \t" + countA_Diff);
        s.append("\r\n::: countA_DiffISA:\t" + countA_DiffISA);
        s.append("\r\n::: countA_Total:  \t" + countA_Total);
        s.append("\r\n::: countB_Diff:   \t" + countB_Diff);
        s.append("\r\n::: countB_DiffISA:\t" + countB_DiffISA);
        s.append("\r\n::: countB_Total:  \t" + countB_Total);
        s.append("\r\n::: ");

        return s.toString();
    }

    private void writeBackRetired(SnoRel rel_A, int writeToNid, long versionTime)
            throws IOException {
        if (rel_A.typeId == isaNid)
            SnoQuery.isaDropped.add(rel_A);
        else
            SnoQuery.roleDropped.add(rel_A);

        try {
            I_RelVersioned rBean = tf.getRelationship(rel_A.relNid);
            List<? extends I_RelPart> rvList = rBean.getVersions(true);

            if (rvList.size() != 1)
                logger.info("::: [SnorocketTask] ERROR: writeBackRetired() multiple last versions");
            else {

                // CREATE RELATIONSHIP PART W/ TermFactory
                I_RelPart nextRelPart = tf.newRelPart(); // I_RelPart
                nextRelPart.setTypeId(rel_A.typeId); // from classifier
                nextRelPart.setGroup(rel_A.group); // from classifier
                I_RelPart lastPart = rvList.get(0);
                nextRelPart.setCharacteristicId(lastPart.getCharacteristicId());
                nextRelPart.setRefinabilityId(lastPart.getRefinabilityId());
                nextRelPart.setStatusId(isRETIRED);
                nextRelPart.setTime(versionTime);
                nextRelPart.setPathId(writeToNid); // via preferences

                rBean.addVersionNoRedundancyCheck(nextRelPart);

                // Commit
                tf.commit(ChangeSetPolicy.OFF, ChangeSetWriterThreading.SINGLE_THREAD);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void writeBackCurrent(SnoRel rel_B, int writeToNid, 
            long versionTime) throws TerminologyException, IOException {
        if (rel_B.typeId == isaNid)
            SnoQuery.isaAdded.add(rel_B);
        else
            SnoQuery.roleAdded.add(rel_B);
               
        // @@@ WRITEBACK NEW ISAs --> ALL NEW RELATIONS
        // CREATE RELATIONSHIP PART W/ TermFactory-->VobdEnv
        tf.newRelationshipNoCheck(UUID.randomUUID(),
                tf.getConcept(rel_B.c1Id), 
                rel_B.typeId, 
                rel_B.c2Id, 
                isCh_DEFINING_CHARACTERISTIC, 
                isOPTIONAL_REFINABILITY, 
                isCURRENT, 
                rel_B.group, 
                writeToNid, 
                versionTime);
        
        // Commit
        try {
            tf.commit(ChangeSetPolicy.OFF, ChangeSetWriterThreading.SINGLE_THREAD);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
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

    private Condition setupCoreNids() {
        I_TermFactory tf = Terms.get();

        // SETUP CORE NATIVES IDs
        try {
            config = tf.getActiveAceFrameConfig();

            // SETUP CORE NATIVES IDs
            // :TODO: isaNid & rootNid should come from preferences config
            isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
            rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());
            // :???: Should ROOT_ROLE be considered for addition to
            // SNOMED.Concept

            if (config.getClassifierIsaType() != null) {
                int checkIsaNid = tf.uuidToNative(config.getClassifierIsaType().getUids());
                if (checkIsaNid != isaNid) {
                    logger.severe("\r\n::: SERVERE ERROR isaNid MISMACTH ****");
                }
            } else {
                String errStr = "Classification 'Is a' not set in Classifier Preferences!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            }

            if (config.getClassificationRoot() != null) {
                int checkRootNid = tf.uuidToNative(config.getClassificationRoot().getUids());
                if (checkRootNid != rootNid) {
                    logger.severe("\r\n::: SERVERE ERROR rootNid MISMACTH ***");
                }
            } else {
                String errStr = "Classifier Root not set! Found: "
                        + tf.getActiveAceFrameConfig().getEditingPathSet();
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            }

            // :PHASE_2:
            if (config.getClassificationRoleRoot() != null) {
                rootRoleNid = tf.uuidToNative(config.getClassificationRoleRoot().getUids());
            } else {
                String errStr = "Classifier Role Root not set! Found: "
                        + tf.getActiveAceFrameConfig().getEditingPathSet();
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            }

            // 0 CURRENT, 1 RETIRED
            isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            isRETIRED = tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
            isOPTIONAL_REFINABILITY = tf
                    .uuidToNative(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            isNOT_REFINABLE = tf.uuidToNative(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
                    .getUids());
            isMANDATORY_REFINABILITY = tf
                    .uuidToNative(ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.getUids());
            isCh_STATED_RELATIONSHIP = tf
                    .uuidToNative(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            isCh_DEFINING_CHARACTERISTIC = tf
                    .uuidToNative(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
            isCh_STATED_AND_INFERRED_RELATIONSHIP = tf
                    .uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP
                            .getUids());
            isCh_STATED_AND_SUBSUMED_RELATIONSHIP = tf
                    .uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_SUBSUMED_RELATIONSHIP
                            .getUids());
            sourceUnspecifiedNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
                    .getUids());
        } catch (TerminologyException e) {
            e.printStackTrace();
            return Condition.STOP;
        } catch (IOException e) {
            e.printStackTrace();
            return Condition.STOP;
        }
        statusSet = tf.newIntSet();
        statusSet.add(isCURRENT);
        return Condition.CONTINUE;
    }

    private Condition setupPaths() {
        // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
        try {
            if (config.getEditingPathSet().size() != 1) {
                String errStr = "Profile must have only one edit path. Found: "
                        + tf.getActiveAceFrameConfig().getEditingPathSet();
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            }

            // GET ALL EDIT_PATH ORIGINS
            I_GetConceptData cEditPathObj = config.getClassifierInputPath();
            if (cEditPathObj == null) {
                String errStr = "Classifier Input (Edit) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return Condition.STOP;
            }

            // Setup to exclude Workbench Auxiliary on path
            UUID wAuxUuid = UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66");
            I_GetConceptData wAuxCb = tf.getConcept(wAuxUuid);
            workbenchAuxPath = wAuxCb.getConceptId();

            cEditPathNid = cEditPathObj.getConceptId();
            cEditIPath = tf.getPath(cEditPathObj.getUids());
            cEditPosSet = new PositionSetReadOnly(tf.newPosition(cEditIPath, Integer.MAX_VALUE));
            // cEditPosSet = new PositionSetReadOnly(cEditIPath.getOrigins().get(0));

            cEditPathPos = new ArrayList<I_Position>();
            cEditPathPos.add(tf.newPosition(cEditIPath, Integer.MAX_VALUE));
            getPathOrigins(cEditPathPos, cEditIPath);

            // GET ALL CLASSIFER_PATH ORIGINS
            I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
            if (cClassPathObj == null) {
                String errStr = "Classifier Output (Inferred) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return Condition.STOP;
            }
            cClassPathNid = cClassPathObj.getConceptId();
            cClassIPath = tf.getPath(cClassPathObj.getUids());
            cClassPosSet = new PositionSetReadOnly(tf.newPosition(cClassIPath, Integer.MAX_VALUE));
            // cClassPosSet = new PositionSetReadOnly(cClassIPath.getOrigins().get(0));

            cClassPathPos = new ArrayList<I_Position>();
            cClassPathPos.add(tf.newPosition(cClassIPath, Integer.MAX_VALUE));
            getPathOrigins(cClassPathPos, cClassIPath);

        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Condition.CONTINUE;
    }

    /**
     * 
     * @return Classifier input and output paths as a string.
     */
    private String toStringPathPos(List<I_Position> pathPos, String pStr) {
        // BUILD STRING
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [SnorocketTask] PATH ID -- " + pStr + "\r\n");
        for (I_Position position : pathPos) {
            s.append("::: ... PATH:\t" + toStringCNid(position.getPath().getConceptId()) + "\r\n");
        }
        s.append(":::");
        return s.toString();
    }

    /**
     * Given the <code>startTime</code>, computes <code>lapsedTime</code> by use
     * the time of calling <code>toStringTime()</code> as the
     * <code>stopTime</code>.
     * 
     * @param <code>long startTime</code> // in milliseconds
     * @param label
     * @return
     */
    @SuppressWarnings("unused")
    private String toStringLapseMin(long startTime) {
        StringBuilder s = new StringBuilder();
        long stopTime = System.currentTimeMillis();
        long lapseTime = stopTime - startTime;
        s.append((((float) lapseTime / 1000) / 60) + " (minutes)");
        return s.toString();
    }

    private String toStringLapseSec(long startTime) {
        StringBuilder s = new StringBuilder();
        long stopTime = System.currentTimeMillis();
        long lapseTime = stopTime - startTime;
        s.append(((float) lapseTime / 1000) + " (seconds)");
        return s.toString();
    }

    private String toStringCNid(int cNid) {
        StringBuilder sb = new StringBuilder();
        try {
            I_GetConceptData c = tf.getConcept(cNid);
            sb.append(c.getUids().iterator().next() + "\t");
            sb.append(cNid + "\t");
            sb.append(c.getInitialText());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return sb.toString();
    }

    private String toStringNids() {
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [SnorocketTask]");
        s.append("\r\n:::\t" + isaNid + "\t : isaNid");
        s.append("\r\n:::\t" + rootNid + "\t : rootNid");
        s.append("\r\n:::\t" + isCURRENT + "\t : isCURRENT");
        s.append("\r\n:::\t" + isRETIRED + "\t : isRETIRED");
        s.append("\r\n:::\t" + isOPTIONAL_REFINABILITY + "\t : isOPTIONAL_REFINABILITY");
        s.append("\r\n:::\t" + isNOT_REFINABLE + "\t : isNOT_REFINABLE");
        s.append("\r\n:::\t" + isMANDATORY_REFINABILITY + "\t : isMANDATORY_REFINABILITY");

        s.append("\r\n:::\t" + isCh_STATED_RELATIONSHIP + "\t : isCh_STATED_RELATIONSHIP");
        s.append("\r\n:::\t" + isCh_DEFINING_CHARACTERISTIC + "\t : isCh_DEFINING_CHARACTERISTIC");
        s.append("\r\n:::\t" + isCh_STATED_AND_INFERRED_RELATIONSHIP
                + "\t : isCh_STATED_AND_INFERRED_RELATIONSHIP");
        s.append("\r\n:::\t" + isCh_STATED_AND_SUBSUMED_RELATIONSHIP
                + "\t : isCh_STATED_AND_SUBSUMED_RELATIONSHIP");
        s.append("\r\n");
        return s.toString();
    }

    @SuppressWarnings("unused")
    private String toStringFocusSet(I_TermFactory tf) {
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [SnorocketTask] FOCUS SET");
        // LOG SPECIFIC RELATIONS SET
        // VIEW *ALL* CASE1 RELS, BASED ON C1
        int focusCase1OutNid[] = { -2147481934, -2147458073, -2147481931, -2147255612, -2144896203,
                -2147481929 };
        s.append("\r\n::: ALL CASE1 RELS, BASED ON C1, NO FILTERS");
        s.append("\r\n::: ****" + "\tRelId     " + "\tCId1      " + "\tCId2      " + "\tType      "
                + "\tGroup" + "\tStatus    " + "\tRefin.    " + "\tChar.     " + "\tPathID    "
                + "\tVersion   ");
        Integer x = 0;
        try {
            for (int c1 : focusCase1OutNid) {
                I_GetConceptData relSource;
                relSource = tf.getConcept(c1);
                Collection<? extends I_RelVersioned> lrv = relSource.getSourceRels();
                for (I_RelVersioned rv : lrv) {

                    Integer iR = rv.getRelId();
                    Integer iA = rv.getC1Id();
                    Integer iB = rv.getC2Id();
                    List<? extends I_RelPart> parts = rv.getMutableParts();
                    for (I_RelPart p : parts) {
                        x++;
                        Integer i1 = p.getTypeId();
                        Integer i2 = p.getGroup();
                        Integer i3 = p.getStatusId();
                        Integer i4 = p.getRefinabilityId();
                        Integer i5 = p.getCharacteristicId();
                        Integer i6 = p.getPathId();
                        Integer i7 = p.getVersion();
                        s.append("\r\n::: ... \t" + iR.toString() + "\t" + iA.toString() + "\t"
                                + iB.toString() + "\t" + i1.toString() + "\t" + i2.toString()
                                + "\t" + i3.toString() + "\t" + i4.toString() + "\t"
                                + i5.toString() + "\t" + i6.toString() + "\t" + i7.toString()
                                + "\t" + x.toString());
                    }
                }
            }
            s.append("\r\n:::");
            for (int c1Nid : focusCase1OutNid) {
                I_GetConceptData sourceRel = tf.getConcept(c1Nid);
                Collection<? extends I_RelVersioned> lsr = sourceRel.getSourceRels();
                for (I_RelVersioned rv : lsr) {

                    Integer iR = rv.getRelId();
                    Integer iA = rv.getC1Id();
                    Integer iB = rv.getC2Id();
                    s.append("\r\n::: ... \tRelId:\t" + iR.toString() + "\tCId1:\t" + iA.toString()
                            + "\tCId2:\t" + iB.toString());
                    s.append("\r\n::: UUIDs:\t" + rv.getUniversal());
                    s.append("\r\n:::");
                }
            }
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s.toString();
    } // toStringFocusSet()

    @SuppressWarnings("unused")
    private String toStringSnoRel(List<SnoRel> list, int start, int count, String comment) {
        StringBuilder s = new StringBuilder(250 + count * 110);
        s.append("\r\n::: [SnorocketTask] SnoRel Listing -- " + comment);
        if (list.size() > 0 && start >= 0 && ((start + count) < list.size())) {
            s.append("\r\n::: \t" + list.get(0).toStringHdr());
            for (int i = start; i < start + count; i++) {
                SnoRel sr = list.get(i);
                s.append("\r\n::: \t" + sr.toString());
            }
        } else {
            s.append("\r\n::: *** RANGE ERROR ***");
            s.append("\r\n::: ");
        }

        return s.toString();
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // nothing to do.

    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    // 
    private int[] setupRoleNids() throws TerminologyException, IOException {
        int countRelDuplVersion = 0;
        LinkedHashSet<Integer> resultSet = new LinkedHashSet<Integer>();

        I_GetConceptData rootConcept = tf.getConcept(rootRoleNid);
        Collection<? extends I_RelVersioned> thisLevel = rootConcept.getDestRels();
        while (thisLevel.size() > 0) {
            ArrayList<I_RelVersioned> nextLevel = new ArrayList<I_RelVersioned>();
            for (I_RelVersioned rv : thisLevel) {
                I_RelPart rPart1 = null;
                for (I_Position pos : cEditPathPos) { // PATHS_IN_PRIORITY_ORDER
                    for (I_RelPart rPart : rv.getMutableParts()) {
                        if (pos.getPath().getConceptId() == rPart.getPathId()) {
                            if (rPart1 == null) {
                                rPart1 = rPart; // ... KEEP FIRST_INSTANCE
                            } else if (rPart1.getVersion() < rPart.getVersion()) {
                                rPart1 = rPart; // ... KEEP MORE_RECENT PART
                            } else if (rPart1.getVersion() == rPart.getVersion()) {
                                countRelDuplVersion++;
                                if (rPart.getStatusId() == isCURRENT)
                                    rPart1 = rPart; // KEEP CURRENT PART
                            }
                        }
                    }
                    if (rPart1 != null)
                        break; // IF FOUND ON THIS PATH, STOP SEARCHING
                }

                if ((rPart1 != null) && (rPart1.getStatusId() == isCURRENT)
                        && (rPart1.getTypeId() == isaNid)) {
                    // KEEP C1 AS RESULT
                    resultSet.add(rv.getC1Id());

                    // GET C1 DESTINATION RELS FOR NEXT LEVEL
                    I_GetConceptData c1 = tf.getConcept(rv.getC1Id());
                    Collection<? extends I_RelVersioned> c1Rels = c1.getDestRels();
                    for (I_RelVersioned nextRel : c1Rels)
                        nextLevel.add(nextRel);
                }
            } // for thisLevel

            thisLevel = nextLevel;
        } // while thisLevel

        // IS-A is outside the active SNOMED role root
        resultSet.add(isaNid);

        // 
        StringBuilder sb = new StringBuilder("::: ALLOWED ROLES = " + resultSet.size() + "\r\n");
        for (Integer cNid : resultSet) {
            sb.append(":::    " + toStringCNid(cNid) + "\r\n");
        }
        logger.info(sb.toString());

        // prepare the role nid array.
        int[] resultInt = new int[resultSet.size()];
        int i = 0;
        for (Integer rNid : resultSet) {
            resultInt[i] = rNid;
            i++;
        }
        Arrays.sort(resultInt);

        allowedRoleTypes = tf.newIntSet();
        allowedRoleTypes.addAll(resultInt);
        return resultInt;
    }

    // GET ROLES
    private ArrayList<SnoRel> getRoles() { // SORT BY [ROLE-C1-GROUP-C2]
        ArrayList<SnoRel> results = new ArrayList<SnoRel>();
        boolean countRolesVerbose = true;

        Comparator<SnoRel> comp = new Comparator<SnoRel>() {
            public int compare(SnoRel o1, SnoRel o2) {
                int thisMore = 1;
                int thisLess = -1;
                if (o1.typeId > o2.typeId) {
                    return thisMore;
                } else if (o1.typeId < o2.typeId) {
                    return thisLess;
                } else {
                    if (o1.c1Id > o2.c1Id) {
                        return thisMore;
                    } else if (o1.c1Id < o2.c1Id) {
                        return thisLess;
                    } else {

                        if (o1.group > o2.group) {
                            return thisMore;
                        } else if (o1.group < o2.group) {
                            return thisLess;
                        } else {

                            if (o1.c2Id > o2.c2Id) {
                                return thisMore;
                            } else if (o1.c2Id < o2.c2Id) {
                                return thisLess;
                            } else {
                                return 0; // this == received
                            }
                        }
                    }
                }
            } // compare()
        };

        Collections.sort(cEditSnoRels, comp);

        int roleCount = 0;
        int lastRole = Integer.MIN_VALUE;
        StringBuilder sb = new StringBuilder(4096);
        for (SnoRel sr : cEditSnoRels) {
            if (sr.typeId != lastRole) {
                roleCount += 1;
                results.add(sr);
                if (countRolesVerbose) {
                    sb.append("::: " + SnoTable.toStringIsaAncestry(sr.typeId, cEditPathPos)
                            + "\r\n");
                }
            }
            lastRole = sr.typeId;
        }
        logger.info("\r\n::: [SnorocketTask] COUNTED ROLES == " + roleCount + "\r\n" + sb);
        return results;
    }

    // dumps role-types to console
    private void dumpRoles() { // SORT BY [ROLE-C1-GROUP-C2]
        boolean countRolesFlag = true;
        boolean countRolesVerboseFlag = true;

        Comparator<SnoRel> comp = new Comparator<SnoRel>() {
            public int compare(SnoRel o1, SnoRel o2) {
                int thisMore = 1;
                int thisLess = -1;
                if (o1.typeId > o2.typeId) {
                    return thisMore;
                } else if (o1.typeId < o2.typeId) {
                    return thisLess;
                } else {
                    if (o1.c1Id > o2.c1Id) {
                        return thisMore;
                    } else if (o1.c1Id < o2.c1Id) {
                        return thisLess;
                    } else {

                        if (o1.group > o2.group) {
                            return thisMore;
                        } else if (o1.group < o2.group) {
                            return thisLess;
                        } else {

                            if (o1.c2Id > o2.c2Id) {
                                return thisMore;
                            } else if (o1.c2Id < o2.c2Id) {
                                return thisLess;
                            } else {
                                return 0; // this == received
                            }
                        }
                    }
                }
            } // compare()
        };

        Collections.sort(cEditSnoRels, comp);

        if (countRolesFlag) {
            int countRoles = 0;
            int lastRole = Integer.MIN_VALUE;
            StringBuilder sb = new StringBuilder(4096);
            for (SnoRel sr : cEditSnoRels) {
                if (sr.typeId != lastRole) {
                    countRoles += 1;
                    if (countRolesVerboseFlag) {
                        sb.append("::: " + SnoTable.toStringIsaAncestry(sr.typeId, cEditPathPos)
                                + "\r\n");
                    }
                }
                lastRole = sr.typeId;
            }
            logger.info("\r\n::: [SnorocketTask] COUNTED ROLES == " + countRoles + "\r\n" + sb);
        }
    }

    // dump concepts to file
    private void dumpSnoCon(List<SnoCon> scl, String fName, int format) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fName));
            if (format == 1) { // RAW NIDS
                for (SnoCon sc : scl) {
                    bw.write(sc.id + "\t" + sc.isDefined + "\r\n");
                }
            }
            if (format == 2) { // RAW UUIDs
                for (SnoCon sc : scl) {
                    I_GetConceptData c = tf.getConcept(sc.id);
                    boolean d = sc.isDefined;
                    bw.write(c.getUids().iterator().next() + "\t" + d + "\r\n");
                }
            }
            if (format == 3) { // Initial Text
                for (SnoCon sc : scl) {
                    I_GetConceptData c = tf.getConcept(sc.id);
                    boolean d = sc.isDefined;
                    bw.write(c.getInitialText() + "\t" + d + "\r\n");
                }
            }
            if (format == 4) { // "FULL" UUIDs, NIDs, **_index, Initial Text
                int index = 0;
                for (SnoCon sc : scl) {
                    I_GetConceptData c = tf.getConcept(sc.id);
                    boolean d = sc.isDefined;
                    bw.write(c.getUids().iterator().next() + "\t" + d + "\t");
                    bw.write(sc.id + "\t");
                    bw.write("**_" + index + "\t");
                    bw.write(c.getInitialText() + "\r\n");
                    index += 1;
                }
            }
            if (format == 5) { // "COMPARE" UUIDs, //NIDs, Initial Text
                int index = 0;
                for (SnoCon sc : scl) {
                    I_GetConceptData c = tf.getConcept(sc.id);
                    boolean d = sc.isDefined;
                    bw.write(c.getUids().iterator().next() + "\t" + d + "\t");
                    // bw.write(sc.id + "\t");
                    bw.write(c.getInitialText() + "\r\n");
                    index += 1;
                }
            }
            if (format == 6) { // Distribution Form
                int index = 0;
                bw.write("CONCEPTID\t" + "CONCEPTSTATUS\t" + "FULLYSPECIFIEDNAME\t" + "CTV3ID\t"
                        + "SNOMEDID\t" + "ISPRIMITIVE");
                for (SnoCon sc : scl) {
                    I_GetConceptData c = tf.getConcept(sc.id);
                    boolean d = sc.isDefined;
                    // CONCEPTID CONCEPTSTATUS FULLYSPECIFIEDNAME
                    bw.write(sc.id + "\t" + "0\t" + c.getInitialText() + "0\t");
                    // CTV3ID SNOMEDID
                    bw.write("NA\t" + "NA\t");
                    // ISPRIMITIVE
                    bw.write((d ? "0" : "1") + "\r\n");
                    index += 1;
                }
            }
            if (format == 7) { // NIDs to UUIDs
                for (SnoCon sc : scl) {
                    I_GetConceptData c = tf.getConcept(sc.id);
                    bw.write(sc.id + "\t" + c.getUids().iterator().next() + "\r\n");
                }
            }
            if (format == 8) { // UUIDs to NIDs
                for (SnoCon sc : scl) {
                    I_GetConceptData c = tf.getConcept(sc.id);
                    bw.write(c.getUids().iterator().next() + "\t" + sc.id + "\r\n");
                }
            }
            bw.flush();
            bw.close();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            // due to tf.getConcept()
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // due to new FileWriter
            e.printStackTrace();
        }
    }

    // dumps relationships to a file
    private String dumpSnoRelStr(SnoRel sr) {
        StringBuilder sb = new StringBuilder();
        try {
            I_GetConceptData c1 = tf.getConcept(sr.c1Id);
            I_GetConceptData t = tf.getConcept(sr.typeId);
            I_GetConceptData c2 = tf.getConcept(sr.c2Id);
            int g = sr.group;
            sb.append(c1.getUids().iterator().next() + "\t" + t.getUids().iterator().next() + "\t"
                    + c2.getUids().iterator().next() + "\t" + g + "\t<br>\t");
            sb.append(sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id + "\t" + sr.group + "\t<br>\t");
            sb.append("|" + c1.getInitialText() + "\t|" + t.getInitialText() + "\t|"
                    + c2.getInitialText() + "\t" + g + "\r\n");

            sb.append("\tc2 status: ** ");
            for (I_ConceptAttributePart mp : c2.getConceptAttributes().getMutableParts())
                sb.append(toStringCNid(mp.getStatusId()) + " ** ");

            return sb.toString();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void dumpSnoRel(List<SnoRel> srl, String fName, int format) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fName));
            if (format == 1) { // RAW NIDs
                for (SnoRel sr : srl) {
                    bw
                            .write(sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id + "\t" + sr.group
                                    + "\r\n");
                }
            }
            if (format == 2) { // UUIDs
                for (SnoRel sr : srl) {
                    I_GetConceptData c1 = tf.getConcept(sr.c1Id);
                    I_GetConceptData t = tf.getConcept(sr.typeId);
                    I_GetConceptData c2 = tf.getConcept(sr.c2Id);
                    int g = sr.group;
                    bw.write(c1.getUids().iterator().next() + "\t" + t.getUids().iterator().next()
                            + "\t" + c2.getUids().iterator().next() + "\t" + g + "\r\n");
                }
            }
            if (format == 3) { // Initial Text
                for (SnoRel sr : srl) {
                    I_GetConceptData c1 = tf.getConcept(sr.c1Id);
                    I_GetConceptData t = tf.getConcept(sr.typeId);
                    I_GetConceptData c2 = tf.getConcept(sr.c2Id);
                    int g = sr.group;
                    bw.write(c1.getInitialText() + "\t" + t.getInitialText() + "\t"
                            + c2.getInitialText() + "\t" + g + "\r\n");
                }
            }
            if (format == 4) { // "FULL": UUIDs, NIDs, **_index, Initial Text
                int index = 0;
                for (SnoRel sr : srl) {
                    I_GetConceptData c1 = tf.getConcept(sr.c1Id);
                    I_GetConceptData t = tf.getConcept(sr.typeId);
                    I_GetConceptData c2 = tf.getConcept(sr.c2Id);
                    int g = sr.group;
                    bw.write(c1.getUids().iterator().next() + "\t" + t.getUids().iterator().next()
                            + "\t" + c2.getUids().iterator().next() + "\t" + g + "\t");
                    bw.write(sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id + "\t" + sr.group + "\t");
                    bw.write("**_" + index + "\t|");
                    bw.write(c1.getInitialText() + "\t|" + t.getInitialText() + "\t|"
                            + c2.getInitialText() + "\t" + g + "\r\n");
                    index += 1;
                }
            }
            if (format == 5) { // "COMPARE": UUIDs, //NIDs, Initial Text
                int index = 0;
                for (SnoRel sr : srl) {
                    I_GetConceptData c1 = tf.getConcept(sr.c1Id);
                    I_GetConceptData t = tf.getConcept(sr.typeId);
                    I_GetConceptData c2 = tf.getConcept(sr.c2Id);
                    int g = sr.group;
                    bw.write(c1.getUids().iterator().next() + "\t" + t.getUids().iterator().next()
                            + "\t" + c2.getUids().iterator().next() + "\t" + g + "\t");
                    //bw.write(sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id + "\t" + sr.group + "\t");
                    bw.write(c1.getInitialText() + "\t|" + t.getInitialText() + "\t|"
                            + c2.getInitialText() + "\r\n");
                    index += 1;
                }
            }
            if (format == 6) { // Distribution Form
                int index = 0;
                bw.write("RELATIONSHIPID\t" + "CONCEPTID1\t" + "RELATIONSHIPTYPE\t"
                        + "CONCEPTID2\t" + "CHARACTERISTICTYPE\t" + "REFINABILITY\t"
                        + "RELATIONSHIPGROUP\r\n");
                for (SnoRel sr : srl) {
                    // RELATIONSHIPID + CONCEPTID1 + RELATIONSHIPTYPE +
                    // CONCEPTID2
                    bw.write("#" + index + "\t" + sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id
                            + "\t");
                    // CHARACTERISTICTYPE + REFINABILITY + RELATIONSHIPGROUP
                    bw.write("NA\t" + "NA\t" + sr.group + "\r\n");
                    index += 1;
                }
            }
            bw.flush();
            bw.close();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            // due to tf.getConcept()
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // due to new FileWriter
            e.printStackTrace();
        }
    }

}
