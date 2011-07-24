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
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_ConfigAceFrame.CLASSIFIER_INPUT_MODE_PREF;
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
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;

import au.csiro.snorocket.core.IFactory_123;
import au.csiro.snorocket.snapi.Snorocket_123;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_Callback;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_EquivalentCallback;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_InternalDataConCallback;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_InternalDataRelCallback;
import au.csiro.snorocket.snapi.I_Snorocket_123.I_InternalDataRoleCallback;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 * 
 * SnorocketTask retrieves concepts and relationship from the stated edit path
 * and load the same to the IHTSDO (Snorocket) classifier.
 * 
 * Classification is run and the resulting inferred relationships is written
 * back to the database.
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
@BeanList(specs = {
    @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN)})
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
    private static int isLIMITED = Integer.MIN_VALUE;
    private static int isRETIRED = Integer.MIN_VALUE;
    private static int isOPTIONAL_REFINABILITY = Integer.MIN_VALUE;
    private static int isNOT_REFINABLE = Integer.MIN_VALUE;
    private static int isMANDATORY_REFINABILITY = Integer.MIN_VALUE;
    private static int isCh_STATED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_DEFINING_CHARACTERISTIC = Integer.MIN_VALUE;
    // :!!!:???: private static int isCh_STATED_AND_INFERRED_RELATIONSHIP = Integer.MIN_VALUE;
    // :!!!:???: private static int isCh_STATED_AND_SUBSUMED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int sourceUnspecifiedNid;
    private static int workbenchAuxPath = Integer.MIN_VALUE;
    private static int snorocketAuthorNid = Integer.MIN_VALUE;
    // NID SETS
    I_IntSet statusSet = null;
    PositionSetReadOnly cViewPosSet = null;
    PositionSetReadOnly cEditPosSet = null;
    I_IntSet allowedRoleTypes = null;
    // INPUT PATHS
    int cEditPathNid = Integer.MIN_VALUE; // :TODO: move to logging
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
    private static Logger logger;
    private I_TermFactory tf = null;
    private I_ConfigAceFrame config = null;
    private Precedence precedence;
    private I_ManageContradiction contradictionMgr;
    private I_ShowActivity gui = null;
    private boolean continueThisAction = true;
    // INTERNAL
    private static boolean debug = false; // :DEBUG:
    private static boolean debugDump = false; // :DEBUG: save to files
    private boolean usesRf2B = false;

    static {
        if (System.getProperties().get("SnorocketDebug") != null
                && System.getProperties().get("SnorocketDebug").toString().toLowerCase().startsWith("t")) {
            debug = true;
        }
        if (System.getProperties().get("SnorocketDebugDump") != null
                && System.getProperties().get("SnorocketDebugDump").toString().toLowerCase().startsWith("t")) {
            debugDump = true;
        }
    }

    static {
        if (System.getProperties().get("SnorocketDebug") != null
                && System.getProperties().get("SnorocketDebug").toString().toLowerCase().startsWith("t")) {
            debug = true;
        }
        if (System.getProperties().get("SnorocketDebugDump") != null
                && System.getProperties().get("SnorocketDebugDump").toString().toLowerCase().startsWith("t")) {
            debugDump = true;
            au.csiro.snorocket.core.Snorocket.DEBUG_DUMP = true;
        }
    }

    @Override
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

        @Override
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

        @Override
        public void equivalent(ArrayList<Integer> equivalentConcepts) {
            SnoQuery.equivCon.add(new SnoConGrp(equivalentConcepts));
            countConSet += 1;
        }
    }

    private class ProcessInternalDataCon implements I_InternalDataConCallback {

        private List<SnoCon> snocons;

        public ProcessInternalDataCon(List<SnoCon> snocons) {
            super();
            this.snocons = snocons;
        }

        @Override
        public void processConData(int cId) {
            snocons.add(new SnoCon(cId, false));
        }
    }

    private class ProcessInternalDataRel implements I_InternalDataRelCallback {

        private List<SnoRel> snorels;

        public ProcessInternalDataRel(List<SnoRel> snorels) {
            super();
            this.snorels = snorels;
        }

        @Override
        public void processRelData(int c1Id, int roleId, int c2Id, int group) {
            snorels.add(new SnoRel(c1Id, c2Id, roleId, group));
        }
    }

    private class ProcessInternalDataRole implements I_InternalDataRoleCallback {

        private List<SnoCon> snocons;

        public ProcessInternalDataRole(List<SnoCon> snocons) {
            super();
            this.snocons = snocons;
        }

        @Override
        public void processRoleData(int cId) {
            snocons.add(new SnoCon(cId, false));
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
    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        logger = worker.getLogger();
        logger.info("\r\n::: [SnorocketTask] evaluate() -- begin");

        try {
            tf = Terms.get();
            config = tf.getActiveAceFrameConfig();
            precedence = config.getPrecedence();
            contradictionMgr = config.getConflictResolutionStrategy();

            SnoQuery.initAll();

            if (setupCoreNids().equals(Condition.STOP)) {
                return Condition.STOP;
            }
            logger.info(toStringNids());

            // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
            if (setupPaths().equals(Condition.STOP)) {
                return Condition.STOP;
            }
            logger.info(toStringPathPos(cEditPathListPositionBI, "Edit Path"));
            logger.info(toStringPathPos(cViewPathListPositionBI, "View Path"));
            // logger.info(toStringFocusSet(tf));

            // ** GUI: 1. LOAD DATA INTO CLASSIFIER **
            continueThisAction = true;
            gui = tf.newActivityPanel(true, config, "Classifier 1/5: load data", true); // in
            // activity
            // viewer
            gui.addRefreshActionListener(this);
            gui.setProgressInfoUpper("Classifier 1/5: load data");
            gui.setIndeterminate(false);
            gui.setMaximum(1500000);
            gui.setValue(0);
            long startTime = System.currentTimeMillis();

            // SETUP ROLE NID ARRAY
            int[] rNidArray = setupRoleNids();
            int nextRIdx = rNidArray.length;
            if (rNidArray.length > 100) {
                String errStr = "Role types exceeds 100. This will cause a memory issue. "
                        + "Please check that role root is set to 'Concept mode attribute'";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            }

            // GET EDIT_PATH CONCEPTS AND RELATIONSHIPS
            cEditSnoCons = new ArrayList<SnoCon>();
            cEditSnoRels = new ArrayList<SnoRel>();

            SnoPathProcessStated pcEdit = null;
            if (config.getClassifierInputMode() == CLASSIFIER_INPUT_MODE_PREF.EDIT_PATH) {
                pcEdit = new SnoPathProcessStated(logger, cEditSnoCons, cEditSnoRels,
                        allowedRoleTypes, statusSet, cEditPosSet, null, config.getPrecedence(),
                        config.getConflictResolutionStrategy());
                tf.iterateConcepts(pcEdit);
                logger.log(Level.INFO, "\r\n::: [SnorocketTask] GET STATED (Edit) PATH DATA : {0}",
                        pcEdit.getStats(startTime));
            } else if (config.getClassifierInputMode() == CLASSIFIER_INPUT_MODE_PREF.VIEW_PATH) {
                pcEdit = new SnoPathProcessStated(logger, cEditSnoCons, cEditSnoRels,
                        allowedRoleTypes, statusSet, cViewPosSet, null, config.getPrecedence(),
                        config.getConflictResolutionStrategy());
                tf.iterateConcepts(pcEdit);
                logger.log(Level.INFO, "\r\n::: [SnorocketTask] GET STATED (View) PATH DATA : {0}",
                        pcEdit.getStats(startTime));
            } else if (config.getClassifierInputMode() == CLASSIFIER_INPUT_MODE_PREF.VIEW_PATH_WITH_EDIT_PRIORITY) {
                pcEdit = new SnoPathProcessStated(logger, cEditSnoCons, cEditSnoRels,
                        allowedRoleTypes, statusSet, cViewPosSet, cEditPosSet, null, config.getPrecedence(),
                        config.getConflictResolutionStrategy());
                tf.iterateConcepts(pcEdit);
                logger.log(Level.INFO, "\r\n::: [SnorocketTask] GET STATED (View w/ edit priority) PATH DATA : {0}", pcEdit.getStats(startTime));
            } else {
                throw new TaskFailedException("(Classifier) Inferred Path case not implemented.");
            }

            // SETUP CONCEPT NID ARRAY
            final int reserved = 2;
            int margin = cEditSnoCons.size() >> 2; // Add 50%
            int[] cNidArray = new int[cEditSnoCons.size() + margin + reserved];
            cNidArray[IFactory_123.TOP_CONCEPT] = IFactory_123.TOP;
            cNidArray[IFactory_123.BOTTOM_CONCEPT] = IFactory_123.BOTTOM;

            Collections.sort(cEditSnoCons);
            if (cEditSnoCons.get(0).id <= Integer.MIN_VALUE + reserved) {
                throw new TaskFailedException("::: SNOROCKET: TOP & BOTTOM nids NOT reserved");
            }
            int nextCIdx = reserved;
            for (SnoCon sc : cEditSnoCons) {
                cNidArray[nextCIdx++] = sc.id;
            }
            // Fill array to make binary search work correctly.
            Arrays.fill(cNidArray, nextCIdx, cNidArray.length, Integer.MAX_VALUE);

            if (debugDump) {
                // FILTER RELATIONSHIPS
                /* Snorocket_123 is self filtering on C2 */
                int last = cEditSnoRels.size();
                for (int idx = last - 1; idx > -1; idx--) {
                    if (Arrays.binarySearch(cNidArray, cEditSnoRels.get(idx).c2Id) < 0) {
                        cEditSnoRels.remove(idx);
                    }
                }

                dumpSnoCon(cEditSnoCons, "SnoConEdit_RawNid.txt", 1);
                dumpSnoRel(cEditSnoRels, "SnoRelEdit_RawNid.txt", 1);

                dumpSnoCon(cEditSnoCons, "SnoConEditData_full.txt", 4);
                dumpSnoRel(cEditSnoRels, "SnoRelEditData_full.txt", 4);

                dumpSnoCon(cEditSnoCons, "SnoConEditData_compare.txt", 5);
                dumpSnoRel(cEditSnoRels, "SnoRelEditData_compare.txt", 5);
            }

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
                if (debugDump && err > 0) {
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
                    logger.log(Level.INFO, "\r\n::: {0}{1}", new Object[]{sb, dumpSnoRelStr(sr)});
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
                logger.info("\r\n::: [SnorocketTask] Logic Added");
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

                logger.info("\r\n::: [SnorocketTask] \"Never-Grouped\" Added");
            }

            logger.log(Level.INFO, "\r\n::: [SnorocketTask] SORTED & ADDED CONs, RELs"
                    + " *** LAPSE TIME = {0} ***", toStringLapseSec(startTime));

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
            gui = tf.newActivityPanel(true, config, "Classifier 2/5: classify data", true); // in
            // activity
            // viewer
            gui.addRefreshActionListener(this);
            gui.setProgressInfoUpper("Classifier 2/5: classify data");
            gui.setProgressInfoLower("... can take 4 to 6 minutes ...");
            gui.setIndeterminate(true);

            if (debugDump) {
                ArrayList<SnoCon> dataCon = new ArrayList<SnoCon>();
                ArrayList<SnoRel> dataRel = new ArrayList<SnoRel>();
                ArrayList<SnoCon> dataRole = new ArrayList<SnoCon>();

                ProcessInternalDataCon dConProc = new ProcessInternalDataCon(dataCon);
                ProcessInternalDataRel dRelProc = new ProcessInternalDataRel(dataRel);
                ProcessInternalDataRole dRoleProc = new ProcessInternalDataRole(dataRole);

                rocket_123.getInternalDataCon(dConProc);
                rocket_123.getInternalDataRel(dRelProc);
                rocket_123.getInternalDataRole(dRoleProc);

                dumpSnoCon(dataCon, "Rocket123InputIDataCon_compare.txt", 5);
                dumpSnoRel(dataRel, "Rocket123InputIDataRelIdx_compare.txt", 5);
                dumpSnoCon(dataRole, "Rocket123InputIDataRole_compare.txt", 5);

                ArrayList<SnoRel> dataSnoRelNid = new ArrayList<SnoRel>();

                int maxCon = dataCon.size();
                int maxRole = dataRole.size();
                for (SnoRel srd : dataRel) {
                    int iC1 = Integer.MIN_VALUE;
                    int iRole = Integer.MIN_VALUE;
                    int iC2 = Integer.MIN_VALUE;
                    int iG = Integer.MIN_VALUE;

                    if (srd.c1Id < maxCon) {
                        iC1 = dataCon.get(srd.c1Id).id;
                    }
                    if (srd.typeId < maxRole) {
                        iRole = dataRole.get(srd.typeId).id;
                    }
                    if (srd.c2Id < maxCon) {
                        iC2 = dataCon.get(srd.c2Id).id;
                    }
                    iG = srd.group;
                    dataSnoRelNid.add(new SnoRel(iC1, iC2, iRole, iG));
                }
                dumpSnoRel(dataSnoRelNid, "Rocket123InputIDataRelNid_compare.txt", 5);

                dataCon = null;
                dataRel = null;
                dataSnoRelNid = null;
            }

            // RUN CLASSIFIER
            startTime = System.currentTimeMillis();
            logger.info("::: Starting Classifier... ");
            rocket_123.classify();
            logger.log(Level.INFO, "::: Time to classify (ms): {0}",
                    (System.currentTimeMillis() - startTime));

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
            gui = tf.newActivityPanel(true, config, "Classifier */*: retrieve equivalent concepts",
                    true);
            gui.addRefreshActionListener(this);
            gui.setProgressInfoUpper("Classifier */*: retrieve equivalent concepts");
            gui.setIndeterminate(true);
            // gui.setMaximum(1000000);
            // gui.setValue(0);

            // GET CLASSIFER EQUIVALENTS
            worker.getLogger().info("::: GET EQUIVALENT CONCEPTS...");
            startTime = System.currentTimeMillis();
            ProcessEquiv pe = new ProcessEquiv();
            rocket_123.getEquivalents(pe);
            logger.log(Level.INFO, "\r\n::: [SnorocketTask] ProcessEquiv() count={0} time= {1}",
                    new Object[]{pe.countConSet, toStringLapseSec(startTime)});

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
            gui = tf.newActivityPanel(true, config, "Classifier 3/5: retrieve solution set", true); // in
            // activity
            // viewer
            gui.addRefreshActionListener(this);
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
            logger.log(Level.INFO, "\r\n::: [SnorocketTask] GET CLASSIFIER RESULTS count={0} time= {1}",
                    new Object[]{pr.countRel, toStringLapseSec(startTime)});

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
            System.gc();

            if (debugDump) {
                dumpSnoRel(cRocketSnoRels, "SnoRelInferData_full.txt", 4);
                dumpSnoRel(cRocketSnoRels, "SnoRelInferData_compare.txt", 5);
            }

            // ** GUI: 4 GET CLASSIFIER PATH DATA **
            String tmpS = "Classifier 4/5: get previously inferred & compare";
            gui = tf.newActivityPanel(true, config, tmpS, true); // in
            // activity
            // viewer
            gui.addRefreshActionListener(this);
            gui.setProgressInfoUpper(tmpS);
            gui.setIndeterminate(false);
            gui.setMaximum(1000000);
            gui.setValue(0);

            // GET CLASSIFIER_PATH RELS
            startTime = System.currentTimeMillis();
            cClassSnoRels = new ArrayList<SnoRel>();
            SnoPathProcessInferred pcClass = null;
            pcClass = new SnoPathProcessInferred(logger, cClassSnoRels, allowedRoleTypes,
                    statusSet, cEditPosSet, cViewPosSet, gui, precedence, contradictionMgr);
            tf.iterateConcepts(pcClass);
            logger.log(Level.INFO, "\r\n::: [TestSnoPathInferred] GET INFERRED (View) PATH DATA : {0}",
                    pcClass.getStats(startTime));

//            if (config.getClassifierInputMode() == CLASSIFIER_INPUT_MODE_PREF.EDIT_PATH) {
//                pcClass = new SnoPathProcessInferred(logger, cClassSnoRels, allowedRoleTypes,
//                        statusSet, cEditPosSet, cEditPosSet, gui, precedence, contradictionMgr);
//                tf.iterateConcepts(pcClass);
//                logger.info("\r\n::: [TestSnoPathInferred] GET INFERRED (Edit) PATH DATA : "
//                        + pcClass.getStats(startTime));
//            } else if (config.getClassifierInputMode() == CLASSIFIER_INPUT_MODE_PREF.VIEW_PATH) {
//                pcClass = new SnoPathProcessInferred(logger, cClassSnoRels, allowedRoleTypes,
//                        statusSet, cEditPosSet, cViewPosSet, gui, precedence, contradictionMgr);
//                tf.iterateConcepts(pcClass);
//                logger.info("\r\n::: [TestSnoPathInferred] GET INFERRED (View) PATH DATA : "
//                        + pcClass.getStats(startTime));
//            } else if (config.getClassifierInputMode() == CLASSIFIER_INPUT_MODE_PREF.VIEW_PATH_WITH_EDIT_PRIORITY) {
//                pcClass = new SnoPathProcessInferred(logger, cClassSnoRels, allowedRoleTypes,
//                        statusSet, cEditPosSet, cViewPosSet, gui, precedence, contradictionMgr);
//                tf.iterateConcepts(pcClass);
//                logger.info("\r\n::: [TestSnoPathInferred] GET INFERRED (View w/ edit priority) PATH DATA : "
//                        + pcClass.getStats(startTime));
//            } else {
//                throw new TaskFailedException("(Classifier) Inferred Path case not implemented.");
//            }

            // FILTER RELATIONSHIPS
            int last = cClassSnoRels.size();
            for (int idx = last - 1; idx > -1; idx--) {
                if (Arrays.binarySearch(cNidArray, cClassSnoRels.get(idx).c2Id) < 0) {
                    cClassSnoRels.remove(idx);
                }
            }

            if (debugDump) {
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
            gui = tf.newActivityPanel(true, config, "Classifier 5/5: write back updates"
                    + " to classifier path", true);
            gui.addRefreshActionListener(this);
            gui.setProgressInfoUpper("Classifier 5/5: write back updates" + " to classifier path");
            gui.setIndeterminate(true);

            // WRITEBACK RESULTS
            startTime = System.currentTimeMillis();
            logger.info(compareAndWriteBack(cClassSnoRels, cRocketSnoRels, cViewPathNid));

            // Commit
            tf.commit(ChangeSetPolicy.OFF, ChangeSetWriterThreading.SINGLE_THREAD);

            logger.log(Level.INFO, "\r\n::: *** WRITEBACK *** LAPSED TIME =\t{0} ***",
                    toStringLapseSec(startTime));

            if (debugDump) {

                ArrayList<SnoRel> sqrl = SnoQuery.getIsaAdded();
                dumpSnoRel(SnoQuery.getIsaAdded(), "SnoRelIsaAdd_full.txt", 4);
                dumpSnoRel(SnoQuery.getIsaAdded(), "SnoRelIsaAdd_compare.txt", 5);

                sqrl = SnoQuery.getIsaDropped();
                dumpSnoRel(sqrl, "SnoRelIsaDrop_full.txt", 4);
                dumpSnoRel(sqrl, "SnoRelIsaDrop_compare.txt", 5);

                sqrl = SnoQuery.getRoleAdded();
                dumpSnoRel(sqrl, "SnoRelRoleAdd_full.txt", 4);
                dumpSnoRel(sqrl, "SnoRelRoleAdd_compare.txt", 5);

                sqrl = SnoQuery.getRoleDropped();
                dumpSnoRel(sqrl, "SnoRelRoleDrop_full.txt", 4);
                dumpSnoRel(sqrl, "SnoRelRoleDrop_compare.txt", 5);

                SnoConGrpList sqcgl = SnoQuery.getEquiv();
                dumpSnoConGrpList(sqcgl, "SnoConEquiv_compare.txt");
            }

            // ** GUI: 5 COMPLETE **
            gui.setProgressInfoLower("writeback completed, lapsed time = "
                    + toStringLapseSec(startTime));
            gui.complete(); // PHASE 5. DONE

        } catch (TerminologyException e) {
            logger.info("\r\n::: TerminologyException");
            logger.log(Level.INFO, e.toString());
            throw new TaskFailedException("::: TerminologyException", e);
        } catch (IOException e) {
            logger.info("\r\n::: IOException");
            logger.log(Level.INFO, e.toString());
            throw new TaskFailedException("::: IOException", e);
        } catch (Exception e) {
            logger.info("\r\n::: Exception");
            logger.log(Level.INFO, e.toString());
            throw new TaskFailedException("::: Exception", e);
        }

        cClassSnoRels = null; // :MEMORY:
        cRocketSnoRels = null; // :MEMORY:

        SnoQuery.setDirty(true);
        config.fireCommit();

        return Condition.CONTINUE;
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
        // Actual write back approximately 16,380 per minute
        // Write back dropped to approximately 1,511 per minute
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

        logger.log(Level.INFO, "\r\n::: [SnorocketTask]"
                + "\r\n::: snorelA.size() = \t{0}\r\n::: snorelB.size() = \t{1}",
                new Object[]{snorelA.size(), snorelB.size()});

        // BY SORT ORDER, LOWER NUMBER ADVANCES FIRST
        while (!done_A && !done_B) {
            if (++countConSeen % 25000 == 0) {
                logger.log(Level.INFO, "::: [SnorocketTask] compareAndWriteBack @ #\t{0}", countConSeen);
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
        s.append("\r\n::: [SnorocketTask] compareAndWriteBack()");
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
            SnoQuery.isaDropped.add(rel_A);
        } else {
            SnoQuery.roleDropped.add(rel_A);
        }

        try {
            I_RelVersioned rBean = tf.getRelationship(rel_A.relNid);
            if (rBean != null) {
                List<? extends I_RelTuple> rvList = rBean.getSpecifiedVersions(statusSet,
                        cViewPosSet, precedence, contradictionMgr);

                if (rvList.size() == 1) {
                    // CREATE RELATIONSHIP PART W/ TermFactory
                    rvList.get(0).makeAnalog(isRETIRED, snorocketAuthorNid, writeToNid, versionTime);
                    // :!!!:TODO: move addUncommittedNoChecks() to more efficient
                    // location.
                    // more optimal to only call once per concept.
                    I_GetConceptData thisC1 = tf.getConcept(rel_A.c1Id);
                    tf.addUncommittedNoChecks(thisC1);

                } else if (rvList.isEmpty()) {
                    logger.log(Level.INFO, "::: [SnorocketTask] ERROR: writeBackRetired() "
                            + "empty version list" + "\trelNid=\t{0}\tc1=\t{1}\t{2}",
                            new Object[]{rel_A.relNid, rel_A.c1Id, tf.getConcept(rel_A.c1Id).toLongString()});
                } else {
                    logger.log(Level.INFO, "::: [SnorocketTask] ERROR: writeBackRetired() "
                            + "multiple last versions" + "\trelNid=\t{0}\tc1=\t{1}\t{2}",
                            new Object[]{rel_A.relNid, rel_A.c1Id, tf.getConcept(rel_A.c1Id).toLongString()});
                }
            } else {
                logger.log(Level.INFO, "::: [SnorocketTask] ERROR: writeBackRetired() "
                        + "tf.getRelationship({0}) == null", rel_A.relNid);
            }

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void writeBackCurrent(SnoRel rel_B, int writeToNid, long versionTime)
            throws TerminologyException, IOException {
        if (rel_B.typeId == isaNid) {
            SnoQuery.isaAdded.add(rel_B);
        } else {
            SnoQuery.roleAdded.add(rel_B);
        }

        I_GetConceptData thisC1 = tf.getConcept(rel_B.c1Id);
        // @@@ WRITEBACK NEW ISAs --> ALL NEW RELATIONS
        // CREATE RELATIONSHIP PART W/ TermFactory-->VobdEnv
        tf.newRelationshipNoCheck(UUID.randomUUID(), thisC1, rel_B.typeId, rel_B.c2Id,
                isCh_DEFINING_CHARACTERISTIC, isOPTIONAL_REFINABILITY, rel_B.group, isCURRENT,
                snorocketAuthorNid, writeToNid, versionTime);

        // :!!!:TODO: [SnorocketTask] move addUncommittedNoChecks() to more efficient location.
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

    private Condition setupCoreNids() {
        tf = Terms.get();

        // SETUP CORE NATIVES IDs
        try {
            // SETUP CORE NATIVES IDs
            // :TODO: isaNid & rootNid should come from preferences config
            isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
            rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());

            if (config.getClassifierIsaType() != null) {
                int checkIsaNid = tf.uuidToNative(config.getClassifierIsaType().getUids());
                if (checkIsaNid != isaNid) {
                    logger.severe("\r\n::: SEVERE ERROR isaNid MISMACTH ****");
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
                    logger.severe("\r\n::: SEVERE ERROR rootNid MISMACTH ***");
                }
            } else {
                String errStr = "Classifier Root not set! Found: " + config.getEditingPathSet();
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            }

            // :PHASE_2:
            if (config.getClassificationRoleRoot() != null) {
                rootRoleNid = tf.uuidToNative(config.getClassificationRoleRoot().getUids());
            } else {
                String errStr = "Classifier Role Root not set! Found: "
                        + config.getEditingPathSet();
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            }

            isCURRENT = SnomedMetadataRfx.getSTATUS_CURRENT_NID();
            isLIMITED = SnomedMetadataRfx.getSTATUS_LIMITED_NID();
            isRETIRED = SnomedMetadataRfx.getSTATUS_RETIRED_NID();
            isOPTIONAL_REFINABILITY = SnomedMetadataRfx.getREL_OPTIONAL_REFINABILITY_NID();
            isNOT_REFINABLE = SnomedMetadataRfx.getREL_NOT_REFINABLE_NID();
            isMANDATORY_REFINABILITY = SnomedMetadataRfx.getREL_MANDATORY_REFINABILITY_NID();
            isCh_STATED_RELATIONSHIP = SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID();
            isCh_DEFINING_CHARACTERISTIC = SnomedMetadataRfx.getREL_CH_DEFINING_CHARACTERISTIC_NID();

            sourceUnspecifiedNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());

            snorocketAuthorNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.USER.SNOROCKET.getUids());

        } catch (Exception ex) {
            Logger.getLogger(SnorocketTask.class.getName()).log(Level.SEVERE, null, ex);
            return Condition.STOP;
        }
        statusSet = tf.newIntSet();
        statusSet.add(isCURRENT);
        statusSet.add(isLIMITED);
        return Condition.CONTINUE;
    }

    private Condition setupPaths() {
        // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
        try {
            // Setup to exclude Workbench Auxiliary on path
            UUID wAuxUuid = UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66");
            I_GetConceptData wAuxCb = tf.getConcept(wAuxUuid);
            workbenchAuxPath = wAuxCb.getConceptNid();

            // GET ALL EDIT_PATH ORIGINS
            if (config.getEditingPathSet() == null) {
                String errStr = "(Classification error) Edit path is not set.";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            } else if (config.getEditingPathSet().size() != 1) {
                String errStr = "(Classification error) Profile must have exactly one edit path. Found: "
                        + config.getEditingPathSet();
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            }
            cEditPathBI = config.getEditingPathSet().iterator().next();
            cEditPathNid = cEditPathBI.getConceptNid();
            cEditPosSet = new PositionSetReadOnly(tf.newPosition(cEditPathBI, Long.MAX_VALUE));

            cEditPathListPositionBI = new ArrayList<PositionBI>();
            cEditPathListPositionBI.add(tf.newPosition(cEditPathBI, Long.MAX_VALUE));
            getPathOrigins(cEditPathListPositionBI, cEditPathBI);

            // GET ALL CLASSIFER_PATH ORIGINS
            if (config.getViewPositionSet() == null) {
                String errStr = "(Classification error) View path is not set.";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            } else if (config.getViewPositionSet().size() != 1) {
                String errStr = "(Classification error) Profile must have exactly one view path. Found: "
                        + config.getViewPositionSet();
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            }
            cViewPathBI = config.getViewPositionSet().iterator().next().getPath();
            cViewPathNid = cViewPathBI.getConceptNid();
            cViewPosSet = new PositionSetReadOnly(tf.newPosition(cViewPathBI, Long.MAX_VALUE));

            cViewPathListPositionBI = new ArrayList<PositionBI>();
            cViewPathListPositionBI.add(tf.newPosition(cViewPathBI, Long.MAX_VALUE));
            getPathOrigins(cViewPathListPositionBI, cViewPathBI);

        } catch (TerminologyException e) {
            logger.log(Level.INFO, e.toString());
        } catch (IOException e) {
            logger.log(Level.INFO, e.toString());
        }

        return Condition.CONTINUE;
    }

    /**
     * 
     * @return Classifier input and output paths as a string.
     */
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
        s.append(((float) lapseTime / 1000) / 60).append(" (minutes)");
        return s.toString();
    }

    private String toStringLapseSec(long startTime) {
        StringBuilder s = new StringBuilder();
        long stopTime = System.currentTimeMillis();
        long lapseTime = stopTime - startTime;
        s.append((float) lapseTime / 1000).append(" (seconds)");
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
            logger.log(Level.INFO, e.toString());
        } catch (TerminologyException e) {
            logger.log(Level.INFO, e.toString());
        }

        return sb.toString();
    }

    private String toStringNids() {
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [SnorocketTask]");
        s.append("\r\n:::\t").append(isaNid).append("\t : isaNid");
        s.append("\r\n:::\t").append(rootNid).append("\t : rootNid");
        s.append("\r\n:::\t").append(isCURRENT).append("\t : isCURRENT");
        StringBuilder append = s.append("\r\n:::\t").append(isRETIRED).append("\t : isRETIRED");
        s.append("\r\n:::\t").append(isOPTIONAL_REFINABILITY).append("\t : isOPTIONAL_REFINABILITY");
        s.append("\r\n:::\t").append(isNOT_REFINABLE).append("\t : isNOT_REFINABLE");
        s.append("\r\n:::\t").append(isMANDATORY_REFINABILITY).append("\t : isMANDATORY_REFINABILITY");

        s.append("\r\n:::\t").append(isCh_STATED_RELATIONSHIP).append("\t : isCh_STATED_RELATIONSHIP");
        s.append("\r\n:::\t").append(isCh_DEFINING_CHARACTERISTIC).append("\t : isCh_DEFINING_CHARACTERISTIC");
        // :!!!:???: s.append("\r\n:::\t").append(isCh_STATED_AND_INFERRED_RELATIONSHIP);
        // :!!!:???: s.append("\t : isCh_STATED_AND_INFERRED_RELATIONSHIP");
        // :!!!:???: s.append("\r\n:::\t").append(isCh_STATED_AND_SUBSUMED_RELATIONSHIP);
        // :!!!:???: s.append("\t : isCh_STATED_AND_SUBSUMED_RELATIONSHIP");
        s.append("\r\n");
        return s.toString();
    }

    @SuppressWarnings("unused")
    private String toStringFocusSet(I_TermFactory tf) {
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [SnorocketTask] FOCUS SET");
        // LOG SPECIFIC RELATIONS SET
        // VIEW *ALL* CASE1 RELS, BASED ON C1
        int focusCase1OutNid[] = {-2147481934, -2147458073, -2147481931, -2147255612, -2144896203,
            -2147481929};
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
                        s.append("\r\n::: ... \t").append(iR.toString()).append("\t");
                        s.append(iA.toString()).append("\t");
                        s.append(iB.toString()).append("\t");
                        s.append(i1.toString()).append("\t");
                        s.append(i2.toString()).append("\t");
                        s.append(i3.toString()).append("\t");
                        s.append(i4.toString()).append("\t");
                        s.append(i5.toString()).append("\t");
                        s.append(i6.toString()).append("\t");
                        s.append(i7.toString()).append("\t");
                        s.append(x.toString());
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
                    s.append("\r\n::: ... \tRelId:\t").append(iR.toString());
                    s.append("\tCId1:\t").append(iA.toString());
                    s.append("\tCId2:\t").append(iB.toString());
                    s.append("\r\n::: UUIDs:\t").append(rv.getUniversal());
                    s.append("\r\n:::");
                }
            }
        } catch (TerminologyException e) {
            logger.log(Level.INFO, e.toString());
        } catch (IOException e) {
            logger.log(Level.INFO, e.toString());
        }
        return s.toString();
    } // toStringFocusSet()

    @SuppressWarnings("unused")
    private String toStringSnoRel(List<SnoRel> list, int start, int count, String comment) {
        StringBuilder s = new StringBuilder(250 + count * 110);
        s.append("\r\n::: [SnorocketTask] SnoRel Listing -- ").append(comment);
        if (list.size() > 0 && start >= 0 && ((start + count) < list.size())) {
            s.append("\r\n::: \t").append(list.get(0).toStringHdr());
            for (int i = start; i < start + count; i++) {
                SnoRel sr = list.get(i);
                s.append("\r\n::: \t").append(sr.toString());
            }
        } else {
            s.append("\r\n::: *** RANGE ERROR ***");
            s.append("\r\n::: ");
        }

        return s.toString();
    }

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // nothing to do.
    }

    @Override
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

    // GET ROLES
    private ArrayList<SnoRel> getRoles() { // SORT BY [ROLE-C1-GROUP-C2]
        ArrayList<SnoRel> results = new ArrayList<SnoRel>();
        boolean countRolesVerbose = true;

        Comparator<SnoRel> comp = new Comparator<SnoRel>() {

            @Override
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
                    sb.append("::: ");
                    sb.append(SnoTable.toStringIsaAncestry(sr.typeId, cEditPathListPositionBI, true));
                    sb.append("\r\n");
                }
            }
            lastRole = sr.typeId;
        }
        logger.log(Level.INFO, "\r\n::: [SnorocketTask] COUNTED ROLES == {0}\r\n{1}",
                new Object[]{roleCount, sb});
        return results;
    }

    // dumps role-types to console
    private void dumpRoles() { // SORT BY [ROLE-C1-GROUP-C2]
        boolean countRolesFlag = true;
        boolean countRolesVerboseFlag = true;

        Comparator<SnoRel> comp = new Comparator<SnoRel>() {

            @Override
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
                        sb.append("::: ");
                        sb.append(SnoTable.toStringIsaAncestry(sr.typeId, cEditPathListPositionBI, true));
                        sb.append("\r\n");
                    }
                }
                lastRole = sr.typeId;
            }
            logger.log(Level.INFO, "\r\n::: [SnorocketTask] COUNTED ROLES == {0}\r\n{1}",
                    new Object[]{countRoles, sb});
        }
    }

    // dump concepts to file
    private void dumpSnoCon(List<SnoCon> scl, String fName, int format) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fName));
            if (format == 1) { // RAW NIDS
                for (SnoCon sc : scl) {
                    // bw.write(sc.id + "\t" + sc.isDefined + "\r\n");
                    bw.write(sc.id + "\r\n");
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
            if (format == 5) { // "COMPARE" UUIDs only
                for (SnoCon sc : scl) {
                    try {
                        if (sc.id == Integer.MAX_VALUE) {
                            bw.write(sc.id + "_INTMAX\r\n");
                            continue;
                        }

                        I_GetConceptData c = tf.getConcept(sc.id);
                        UUID uuid = null;
                        if (c != null && c.getUids().iterator().hasNext()) {
                            uuid = c.getUids().iterator().next();
                            bw.write(uuid + "\r\n");
                        } else {
                            bw.write(sc.id + "_INTNoNext\r\n");
                        }
                    } catch (Exception e) {
                        bw.write(sc.id + "_INTExcept\r\n");
                    }
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
            // due to tf.getConcept()
            logger.log(Level.INFO, e.toString());
        } catch (IOException e) {
            // due to new FileWriter
            logger.log(Level.INFO, e.toString());
        }
    }

    // dump equivalent concepts to file
    private void dumpSnoConGrpList(SnoConGrpList scgl, String fName) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fName));
            // "COMPARE" UUIDs, //NIDs, Initial Text
            int setNumber = 1;
            for (SnoConGrp scg : scgl) {
                for (SnoCon sc : scg) {
                    I_GetConceptData c = tf.getConcept(sc.id);
                    bw.write(c.getUids().get(0).toString() + "\tset=\t" + setNumber + "\t");
                    bw.write(c.getInitialText() + "\r\n");
                }
                setNumber++;
            }

            bw.flush();
            bw.close();
        } catch (TerminologyException e) {
            // due to tf.getConcept()
            logger.log(Level.INFO, e.toString());
        } catch (IOException e) {
            // due to new FileWriter
            logger.log(Level.INFO, e.toString());
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
            sb.append(c1.getUids().iterator().next()).append("\t");
            sb.append(t.getUids().iterator().next()).append("\t");
            sb.append(c2.getUids().iterator().next()).append("\t");
            sb.append(g).append("\t<br>\t");
            sb.append(sr.c1Id).append("\t").append(sr.typeId).append("\t");
            sb.append(sr.c2Id).append("\t");
            sb.append(sr.group).append("\t<br>\t");
            sb.append("|").append(c1.getInitialText()).append("\t|");
            sb.append(t.getInitialText()).append("\t|");
            sb.append(c2.getInitialText()).append("\t");
            sb.append(g).append("\r\n");

            sb.append("\tc2 status: ** ");
            I_ConceptAttributeVersioned<?> ca = c2.getConceptAttributes();
            for (I_ConceptAttributePart mp : ca.getMutableParts()) {
                sb.append(toStringCNid(mp.getStatusId())).append(" ** ");
            }

            return sb.toString();
        } catch (TerminologyException e) {
            logger.log(Level.INFO, e.toString());
        } catch (IOException e) {
            logger.log(Level.INFO, e.toString());
        }
        return sb.toString();
    }

    private void dumpSnoRel(List<SnoRel> srl, String fName, int format) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fName));
            if (format == 1) { // RAW NIDs
                for (SnoRel sr : srl) {
                    bw.write(sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id + "\t" + sr.group
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
            if (format == 4) { // "FULL": rNID, UUIDs, NIDs, **_index, Initial
                // Text
                int index = 0;
                for (SnoRel sr : srl) {
                    I_GetConceptData c1 = tf.getConcept(sr.c1Id);
                    I_GetConceptData t = tf.getConcept(sr.typeId);
                    I_GetConceptData c2 = tf.getConcept(sr.c2Id);
                    int g = sr.group;
                    bw.write(sr.relNid + "\t" + c1.getUids().iterator().next() + "\t"
                            + t.getUids().iterator().next() + "\t" + c2.getUids().iterator().next()
                            + "\t" + g + "\t");
                    bw.write(sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id + "\t" + sr.group + "\t");
                    bw.write("**_" + index + "\t|");
                    bw.write(c1.getInitialText() + "\t|" + t.getInitialText() + "\t|"
                            + c2.getInitialText() + "\t" + g + "\r\n");
                    index += 1;
                }
            }
            if (format == 5) { // "COMPARE": UUIDs only
                for (SnoRel sr : srl) {
                    StringBuilder sb = new StringBuilder();

                    if (sr.c1Id == Integer.MAX_VALUE) {
                        sb.append("_INTMAX\t");
                    } else {
                        try {
                            I_GetConceptData c1 = tf.getConcept(sr.c1Id);
                            if (c1.getUids().iterator().hasNext()) {
                                sb.append(c1.getUids().iterator().next()).append("\t");
                            } else {
                                sb.append(sr.c1Id).append("_INTNoNext\t");
                            }
                        } catch (Exception e) {
                            sb.append(sr.c1Id).append("_INTExcept\t");
                        }
                    }

                    if (sr.typeId == Integer.MAX_VALUE) {
                        sb.append("_INTMAX\t");
                    } else {
                        try {
                            I_GetConceptData t = tf.getConcept(sr.typeId);
                            if (t.getUids().iterator().hasNext()) {
                                sb.append(t.getUids().iterator().next()).append("\t");
                            } else {
                                sb.append(sr.typeId).append("_INTNoNext\t");
                            }
                        } catch (Exception e) {
                            sb.append(sr.typeId).append("_INTExcept\t");
                        }
                    }

                    if (sr.c2Id == Integer.MAX_VALUE) {
                        sb.append("_INTMAX\t");
                    } else {
                        try {
                            I_GetConceptData c2 = tf.getConcept(sr.c2Id);
                            if (c2.getUids().iterator().hasNext()) {
                                sb.append(c2.getUids().iterator().next()).append("\t");
                            } else {
                                sb.append(sr.c2Id).append("_INTNoNext\t");
                            }
                        } catch (Exception e) {
                            sb.append(sr.c2Id).append("_INTExcept\t");
                        }
                    }

                    sb.append(sr.group);
                    sb.append("\r\n");

                    bw.write(sb.toString());
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
                    bw.write("\t" + sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id + "\t");
                    // CHARACTERISTICTYPE + REFINABILITY + RELATIONSHIPGROUP
                    bw.write("0\t" + "0\t" + sr.group + "\r\n");
                    index += 1;
                }
            }
            bw.flush();
            bw.close();
        } catch (TerminologyException e) {
            // due to tf.getConcept()
            logger.log(Level.INFO, e.toString());
        } catch (IOException e) {
            // due to new FileWriter
            logger.log(Level.INFO, e.toString());
        }
    }
}
