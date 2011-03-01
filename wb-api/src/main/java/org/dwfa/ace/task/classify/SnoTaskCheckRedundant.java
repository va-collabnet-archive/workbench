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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
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

/**
 * :NYI: NOT UPDATED FOR SNOROCKET AS A 'USER'
 * 
 * @author marc
 *
 */

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class SnoTaskCheckRedundant extends AbstractTask implements ActionListener {
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

    // CORE NID CONSTANTS
    private static int isaNid = Integer.MIN_VALUE;
    private static int rootNid = Integer.MIN_VALUE;
    private static int isCURRENT = Integer.MIN_VALUE;
    private static int isRETIRED = Integer.MIN_VALUE;
    private static int isOPTIONAL_REFINABILITY = Integer.MIN_VALUE;
    private static int isNOT_REFINABLE = Integer.MIN_VALUE;
    private static int isMANDATORY_REFINABILITY = Integer.MIN_VALUE;
    private static int isCh_STATED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_DEFINING_CHARACTERISTIC = Integer.MIN_VALUE;
    private static int isCh_STATED_AND_INFERRED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_STATED_AND_SUBSUMED_RELATIONSHIP = Integer.MIN_VALUE;
    // INPUT PATHS
    int cEditPathNid = Integer.MIN_VALUE; // :TODO: move to logging
    PathBI cEditIPath = null;
    List<PositionBI> cEditPathPos = null; // Edit (Stated) Path I_Positions

    // OUTPUT PATHS
    int cClassPathNid; // :TODO: move to logging
    PathBI cClassIPath; // Used for write back value
    List<PositionBI> cClassPathPos; // Classifier (Inferred) Path I_Positions

    // MASTER DATA SETS
    // List<SnoRel> cEditSnoRels; // "Edit Path" Concepts
    // List<SnoRel> cClassSnoRels; // "Classifier Path" Relationships

    // USER INTERFACE
    private Logger logger = null;
    I_TermFactory tf = null;
    I_ConfigAceFrame config = null;
    I_ShowActivity gui = null;
    private boolean continueThisAction = true;

    public void actionPerformed(ActionEvent e) {
        continueThisAction = false; // User requested to stop process
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        List<SnoRel> cEditSnoRels;
        List<SnoRel> cClassSnoRels; // "Classifier Path" Relationships

        logger = worker.getLogger();
        logger.info("\r\n::: [SnoTaskCheckRedunant] evaluate() -- begin");
        tf = Terms.get();

        if (setupCoreNids().equals(Condition.STOP))
            return Condition.STOP;
        logger.info(toStringNids());

        if (setupPaths().equals(Condition.STOP))
            return Condition.STOP;
        logger.info(toStringPathPos(cEditPathPos, "Edit Path"));
        logger.info(toStringPathPos(cClassPathPos, "Classifier Path"));

        try {
            // **** GUI:START: 1. GET STATED PATH SNORELS ****
            continueThisAction = true;
            String guiStr = "Compare Stated & Inferred 1/3: Get Edit Path";
            gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig(), guiStr, true); // in
            // activity
            // viewer
            gui.addRefreshActionListener(this);
            gui.setProgressInfoUpper(guiStr);
            gui.setIndeterminate(false);
            gui.setMaximum(1000000);
            gui.setValue(0);

            // GET EDIT_PATH RELS
            cEditSnoRels = new ArrayList<SnoRel>();
            long startTime = System.currentTimeMillis();
            SnoPathProcess pcEdit;
            pcEdit = new SnoPathProcess(logger, null, cEditSnoRels, null, cEditPathPos, gui,false);
            tf.iterateConcepts(pcEdit);
            logger.info("\r\n::: [SnorocketTaskExp] GET STATED PATH DATA" + pcEdit.getStats(startTime));

            // ** GUI:DONE: 1. GET STATED PATH SNORELS **
            if (continueThisAction) {
                gui.setProgressInfoLower("edit path rels = " + pcEdit.countRelAdded + ", lapsed time = "
                    + toStringLapseSec(startTime));
                gui.complete(); // PHASE 1. DONE
            } else {
                gui.setProgressInfoLower("comparison stopped by user");
                gui.complete(); // PHASE 1. DONE
                return Condition.CONTINUE;
            }

            // **** GUI:START: 2. GET CLASSIFIER PATH DATA ****
            guiStr = "Compare Stated & Inferred 2/3: Get Classifier Path";
            gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig(), guiStr, true); // in
            // activity
            // viewer
            gui.addRefreshActionListener(this);
            gui.setProgressInfoUpper(guiStr);
            gui.setIndeterminate(false);
            gui.setMaximum(1000000);
            gui.setValue(0);

            // GET CLASSIFIER_PATH RELS
            cClassSnoRels = new ArrayList<SnoRel>();
            startTime = System.currentTimeMillis();
            SnoPathProcess pcClass = new SnoPathProcess(logger, null, cClassSnoRels, null, cClassPathPos, gui, true);
            tf.iterateConcepts(pcClass);
            logger.info("\r\n::: [SnorocketTaskExp] GET INFERRED PATH DATA" + pcClass.getStats(startTime));

            // ** GUI:DONE: 2 -- done
            if (continueThisAction) {
                gui.setProgressInfoLower("classifier path rels = " + pcClass.countRelAdded + ", lapsed time = "
                    + toStringLapseSec(startTime));
                gui.complete(); // 3 GET CLASSIFIER RESULTS -- done
                pcClass = null; // :MEMORY:
            } else {
                gui.setProgressInfoLower("comparison stopped by user");
                gui.complete(); // PHASE 1. DONE
                return Condition.CONTINUE;
            }

            // **** GUI: 3. COMPARE RESULTS ****
            guiStr = "Compare Stated & Inferred 3/3: Check Redundant Roles";
            gui = tf.newActivityPanel(true, tf.getActiveAceFrameConfig(), guiStr, true); // in
            // activity
            // viewer
            gui.addRefreshActionListener(this);
            gui.setProgressInfoUpper(guiStr);
            gui.setIndeterminate(true);
            // COMPARE RESULTS. Sort is performed in the compare routine.
            startTime = System.currentTimeMillis();
            logger.info(checkRedundantRoles(cClassSnoRels));
            // 3 GET CLASSIFIER RESULTS -- done
            gui.setProgressInfoLower("lapsed time = " + toStringLapseSec(startTime));
            gui.complete();

        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Condition.CONTINUE;
    }

    /**
     * 
     * USE CASE: <b>A</b> = Stated (Edit Path), <b>B</b> = Inferred (Classifier
     * Path)<br>
     * <br>
     * 
     * <font color=#990099> IMPLEMENTATION NOTE: <code>snorelA</code> and
     * <code>snorelB</code> MUST be pre-sorted in C1-Group-Type-C2 order for
     * this routine. Pre-sorting is used to provide overall computational
     * efficiency.</font>
     * 
     * @param <code>List&lt;SnoRel&gt; snorelA</code>
     * @param <code>List&lt;SnoRel&gt; snorelB</code>
     * @return
     */
    private String checkRedundantRoles(List<SnoRel> snorelA) {
        // STATISTICS COUNTERS
        int countSame = 0;
        int countSameISA = 0;
        int countA_Diff = 0;
        int countA_DiffISA = 0;
        int countA_Total = 0;
        int countB_Diff = 0;
        int countB_DiffISA = 0;
        int countB_Total = 0;

        // HISTORY
        // What is the MAX number of rels to capture in history?
        int historySize = 100;
        // Which NativeID (Nid) is the first to capture?
        // Integer.MIN_VALUE for capturing any from the beginning.
        int historyStartC1Nid = Integer.MIN_VALUE;
        List<SnoRel> histListA = new ArrayList<SnoRel>();
        Set<Integer> histC1Set = new HashSet<Integer>();
        Set<Integer> histTypeSet = new HashSet<Integer>();
        Set<Integer> histC2Set = new HashSet<Integer>();

        long startTime = System.currentTimeMillis();
        Collections.sort(snorelA);

        for (SnoRel sr : snorelA) {
            // !!! EDIT ME HERE

            // !!! GET IMMEDIATE ROLE

            // !!! CREATE ROLE GROUPS

            // !!! CHECK FOR REDUNDANT

            if ((histListA.size() < historySize) && (sr.c1Id >= historyStartC1Nid)) {
                histListA.add(sr);
                updateSets(sr, histC1Set, histTypeSet, histC2Set);
            }
        }

        StringBuffer s = new StringBuffer();
        s.append("\r\n::: [SnoTaskCheckRedunant] compareResults()");
        long lapseTime = System.currentTimeMillis() - startTime;
        s.append("\r\n::: [Time] Check for redundant roles: \t" + lapseTime + "\t(mS)\t"
            + (((float) lapseTime / 1000) / 60) + "\t(min)");
        s.append("\r\n");

        s.append("\r\n::: CONCEPT IDS");
        for (Integer nInt : histC1Set)
            s.append("\r\n::: \t" + toStringNid(nInt.intValue()));
        s.append("\r\n::: ROLE_TYPE IDS");
        for (Integer nInt : histTypeSet)
            s.append("\r\n::: \t" + toStringNid(nInt.intValue()));
        s.append("\r\n::: ROLE_VALUE IDS");
        for (Integer nInt : histC2Set)
            s.append("\r\n::: \t" + toStringNid(nInt.intValue()));

        s.append("\r\n::: RELS WITH REDUNDANT ROLES");
        if (histListA.size() > 0) {
            s.append("\r\n::: \t" + histListA.get(0).toStringHdr());
            for (SnoRel sr : histListA) {
                s.append("\r\n::: \t" + sr.toString());
            }
        } else {
            s.append("\r\n::: none found on path");
        }

        return s.toString();
    }

    private void updateSets(SnoRel rel, Set<Integer> cid1Set, Set<Integer> typeSet, Set<Integer> cid2Set) {
        cid1Set.add(new Integer(rel.c1Id));
        typeSet.add(new Integer(rel.typeId));
        cid2Set.add(new Integer(rel.c2Id));

    }

    private Condition setupCoreNids() {
        // SETUP CORE NATIVES IDs
        try {
            config = tf.getActiveAceFrameConfig();

            // SETUP CORE NATIVES IDs
            // :TODO: isaNid & rootNid should come from preferences config
            isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
            rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());
            if (config.getClassifierIsaType() != null) {
                int checkIsaNid = tf.uuidToNative(config.getClassifierIsaType().getUids());
                if (checkIsaNid != isaNid) {
                    logger.severe("\r\n::: SERVERE ERROR isaNid MISMACTH ****");
                }
            } else {
                String errStr = "Profile must have only one edit path. Found: "
                    + tf.getActiveAceFrameConfig().getEditingPathSet();
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new TaskFailedException(errStr));
                return Condition.STOP;
            }

            if (config.getClassificationRoot() != null) {
                int checkRootNid = tf.uuidToNative(config.getClassificationRoot().getUids());
                if (checkRootNid != rootNid) {
                    logger.severe("\r\n::: SERVERE ERROR rootNid MISMACTH ***");
                }
            } else {
                String errStr = "Profile must have only one edit path. Found: "
                    + tf.getActiveAceFrameConfig().getEditingPathSet();
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new TaskFailedException(errStr));
                return Condition.STOP;
            }

            isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()); // 0
            // CURRENT,
            // 1
            // RETIRED
            isRETIRED = tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
            isOPTIONAL_REFINABILITY = tf.uuidToNative(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            isNOT_REFINABLE = tf.uuidToNative(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
            isMANDATORY_REFINABILITY = tf.uuidToNative(ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.getUids());
            isCh_STATED_RELATIONSHIP = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            isCh_DEFINING_CHARACTERISTIC = tf.uuidToNative(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
            isCh_STATED_AND_INFERRED_RELATIONSHIP = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP.getUids());
            isCh_STATED_AND_SUBSUMED_RELATIONSHIP = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_SUBSUMED_RELATIONSHIP.getUids());
            tf.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Condition.CONTINUE;
    }

    private Condition setupPaths() {
        // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
        try {
            if (config.getEditingPathSet().size() != 1) {
                String errStr = "Profile must have only one edit path. Found: "
                    + tf.getActiveAceFrameConfig().getEditingPathSet();
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new TaskFailedException(errStr));
                return Condition.STOP;
            }

            // GET ALL EDIT_PATH ORIGINS
            I_GetConceptData cEditPathObj = config.getClassifierInputPath();
            if (cEditPathObj == null) {
                String errStr = "Classifier Input (Edit) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return Condition.STOP;
            }

            cEditPathNid = cEditPathObj.getConceptNid();
            cEditIPath = tf.getPath(cEditPathObj.getUids());
            cEditPathPos = new ArrayList<PositionBI>();
            cEditPathPos.add(tf.newPosition(cEditIPath, Integer.MAX_VALUE));
            addPathOrigins(cEditPathPos, cEditIPath);

            // GET ALL CLASSIFER_PATH ORIGINS
            I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
            if (cClassPathObj == null) {
                String errStr = "Classifier Output (Inferred) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return Condition.STOP;
            }
            cClassPathNid = cClassPathObj.getConceptNid();
            cClassIPath = tf.getPath(cClassPathObj.getUids());
            cClassPathPos = new ArrayList<PositionBI>();
            cClassPathPos.add(tf.newPosition(cClassIPath, Integer.MAX_VALUE));
            addPathOrigins(cClassPathPos, cClassIPath);

        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Condition.CONTINUE;
    }

    private void addPathOrigins(List<PositionBI> origins, PathBI p) {
        origins.addAll(p.getOrigins());
        for (PositionBI o : p.getOrigins()) {
            addPathOrigins(origins, o.getPath());
        }
    }

    @SuppressWarnings("unused")
    private String toStringLapseMin(long startTime) {
        StringBuilder s = new StringBuilder();
        long stopTime = System.currentTimeMillis();
        long lapseTime = stopTime - startTime;
        s.append((((float) lapseTime / 1000) / 60) + "\t(min)");
        return s.toString();
    }

    private String toStringLapseSec(long startTime) {
        StringBuilder s = new StringBuilder();
        long stopTime = System.currentTimeMillis();
        long lapseTime = stopTime - startTime;
        s.append((lapseTime / 1000) + "\t(sec)");
        return s.toString();
    }

    private String toStringNid(int nid) {
        try {
            I_GetConceptData a = tf.getConcept(nid);
            a.getUids().iterator().next().toString();
            String s = nid + "\t" + a.getUids().iterator().next().toString() + "\t" + a.getInitialText();
            return s;
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String toStringNids() {
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [SnoTaskCheckRedunant]");
        s.append("\r\n:::\t" + isaNid + "\t : isaNid");
        s.append("\r\n:::\t" + rootNid + "\t : rootNid");
        s.append("\r\n:::\t" + isCURRENT + "\t : isCURRENT");
        s.append("\r\n:::\t" + isRETIRED + "\t : isRETIRED");
        s.append("\r\n:::\t" + isOPTIONAL_REFINABILITY + "\t : isOPTIONAL_REFINABILITY");
        s.append("\r\n:::\t" + isNOT_REFINABLE + "\t : isNOT_REFINABLE");
        s.append("\r\n:::\t" + isMANDATORY_REFINABILITY + "\t : isMANDATORY_REFINABILITY");

        s.append("\r\n:::\t" + isCh_STATED_RELATIONSHIP + "\t : isCh_STATED_RELATIONSHIP");
        s.append("\r\n:::\t" + isCh_DEFINING_CHARACTERISTIC + "\t : isCh_DEFINING_CHARACTERISTIC");
        s.append("\r\n:::\t" + isCh_STATED_AND_INFERRED_RELATIONSHIP + "\t : isCh_STATED_AND_INFERRED_RELATIONSHIP");
        s.append("\r\n:::\t" + isCh_STATED_AND_SUBSUMED_RELATIONSHIP + "\t : isCh_STATED_AND_SUBSUMED_RELATIONSHIP");
        s.append("\r\n");
        return s.toString();
    }

    private String toStringPathPos(List<PositionBI> pathPos, String pStr) {
        // BUILD STRING
        StringBuffer s = new StringBuffer();
        s.append("\r\n::: [SnorocketTaskExp] PATH ID -- " + pStr);
        for (PositionBI position : pathPos) {
		    s.append("\r\n::: ... PathID:\t" + position.getPath().getConceptNid() + "\tVersion:\t"
		        + position.getVersion() + "\tUUIDs:\t" + position.getPath().getUUIDs());
		}
        s.append("\r\n:::");
        return s.toString();
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // nothing to do
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

}
