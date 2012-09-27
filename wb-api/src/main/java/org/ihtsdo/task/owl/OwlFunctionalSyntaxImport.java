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
package org.ihtsdo.task.owl;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.dwfa.ace.api.*;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.classify.SnoCon;
import org.dwfa.ace.task.classify.SnoGrp;
import org.dwfa.ace.task.classify.SnoGrpList;
import org.dwfa.ace.task.classify.SnoGrpNumType;
import org.dwfa.ace.task.classify.SnoGrpUuidList;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.ace.task.classify.SnorocketExTask;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.helper.descriptionlogic.DescriptionLogic;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 * some comment
 * @author marc
 */
@BeanList(specs = {
    @Spec(directory = "tasks/owl", type = BeanType.TASK_BEAN)})
public class OwlFunctionalSyntaxImport extends AbstractTask implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private TerminologyStoreDI ts;

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
    /**
     * Line terminator is deliberately set to CR-LF which is DOS style
     */
    private static final String LINE_TERMINATOR = "\n"; // \n == $0A
    private static final String TAB_CHARACTER = "\t";
    private static final String FILE_SEPARATOR = File.separator;
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
    private static int isCh_INFERRED_CHARACTERISTIC = Integer.MIN_VALUE;
    private static int workbenchAuxPath = Integer.MIN_VALUE;
    private static int condorAuthorNid = Integer.MIN_VALUE;
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
    List<SnoRel> cCondorSnoRels; // "Snorocket Results Set" Relationships
    // USER INTERFACE
    private static Logger logger;
    private I_TermFactory tf = null;
    private I_ConfigAceFrame config = null;
    private Precedence precedence;
    private ContradictionManagerBI contradictionMgr;
    private I_ShowActivity gui = null;
    private boolean continueThisAction = true;
    // INTERNAL
    private SnoGrpNumType.SNOGRP_NUMBER_APPROACH groupNumApproach = SnoGrpNumType.SNOGRP_NUMBER_APPROACH.RF1;

    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        Ts.get().suspendChangeNotifications();

        try {
            logger = worker.getLogger();
            logger.info("\r\n::: [CondorRunTask] evaluate() -- begin");
            try {
                ts = Ts.get();
                tf = Terms.get();
                config = tf.getActiveAceFrameConfig();
                precedence = config.getPrecedence();
                contradictionMgr = config.getConflictResolutionStrategy();

                // Show in Activity Viewer window
                I_ShowActivity gui1 = tf.newActivityPanel(true, config, "Import OWL Task", false);
                gui1.addRefreshActionListener(this);
                gui1.setProgressInfoUpper("Import OWL Task");
                gui1.setIndeterminate(true);
                gui1.setProgressInfoLower("... in process ...");
                long startTime1 = System.currentTimeMillis();

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

                // PARSE RESULTS
                cCondorSnoRels = parseOwlfFile();
                if (cCondorSnoRels.isEmpty()) {
                    Logger.getLogger(OwlFunctionalSyntaxImport.class.getName()).log(Level.INFO, "OwlFunctionalSyntaxImport compareAndWriteBack() not executed");
                    return Condition.CONTINUE;
                }

                // GET EXISTING CONDOR CLASSIFIER RELATIONSHIPS
                cClassSnoRels = new ArrayList<SnoRel>();
                SnoPathProcessCondorInferred pcClass;

                gui = tf.newActivityPanel(true, config, "Import OWL: get existing inferred rels", false);
                gui.addRefreshActionListener(this);
                gui.setProgressInfoUpper("Import OWL: get existing inferred rels");
                gui.setIndeterminate(false);
                gui.setMaximum(1500000);
                gui.setValue(0);
                long startTime = System.currentTimeMillis();

                pcClass = new SnoPathProcessCondorInferred(logger, cClassSnoRels, allowedRoleTypes,
                        statusSet, cViewPosSet, gui, precedence, contradictionMgr);
                tf.iterateConcepts(pcClass);
                gui.setProgressInfoLower("complete, time = " + toStringLapseSec(startTime));
                gui.complete();
                logger.info(pcClass.getStats(startTime));

                gui.setProgressInfoLower("complete, time = " + toStringLapseSec(startTime));
                gui.complete();

                // WRITEBACK RESULTS
                // show in Activity Viewer window
                gui = tf.newActivityPanel(true, config, "Import OWL: writeback results", false);
                gui.addRefreshActionListener(this);
                gui.setProgressInfoUpper("Import OWL: writeback results");
                gui.setProgressInfoLower("... can take few minutes ...");
                gui.setIndeterminate(true);
                startTime = System.currentTimeMillis();
                logger.info(compareAndWriteBack(cClassSnoRels, cCondorSnoRels, cViewPathNid));

                // Commit
                tf.commit(ChangeSetPolicy.OFF, ChangeSetWriterThreading.SINGLE_THREAD);

                gui.setProgressInfoLower("complete, time = " + toStringLapseSec(startTime));
                gui.complete();

                gui1.setProgressInfoLower("complete, time = " + toStringLapseSec(startTime1));
                gui1.complete();
            } catch (Exception ex) {
                Logger.getLogger(OwlFunctionalSyntaxImport.class.getName()).log(Level.SEVERE, null, ex);
                throw new TaskFailedException();
            }

            return Condition.CONTINUE;
        } finally {
            Ts.get().resumeChangeNotifications();
        }
    }

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    @Override
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * Set approach for generating role group numbers.
     *
     * @return
     */
    public void setGroupNumApproach(SnoGrpNumType.SNOGRP_NUMBER_APPROACH groupNumApproach) {
        this.groupNumApproach = groupNumApproach;
    }

    private File openFileDialog(String prompt) {
        File theFile = null;
        FileDialog dialog = new FileDialog(new Frame(), prompt, FileDialog.LOAD);
        //
        // dialog.setDirectory(AceConfig.config.getProfileFile().getParentFile().getAbsolutePath());
//        dialog.setFilenameFilter(new FilenameFilter() {
//
//            @Override
//            public boolean accept(File dir, String name) {
//                return name.endsWith(".owl");
//            }
//        });
        // Display dialog and wait for response
        dialog.setVisible(true);
        // Check response
        if (dialog.getFile() != null) {
            theFile = new File(dialog.getDirectory(), dialog.getFile());
        }
        // Cleanup
        dialog.dispose();
        return theFile;
    }

    private ArrayList<SnoRel> parseOwlfFile() {
        ArrayList<SnoRel> owlRels = new ArrayList<SnoRel>();
        BufferedReader r = null;
        String lineIn;
        try {
            String fNameIn = "condor_out.owl"; // FILE_SEPARATOR + "name.ext"

//            File fIn = openFileDialog("Find condor_out.owl");
//            fNameIn = fIn.getAbsolutePath();

            r = new BufferedReader(new InputStreamReader(new FileInputStream(fNameIn), "UTF-8"));

            while (r.ready()) {
                // SubClassOf(:NID_n10000006 :NID_n29857009)
                // SubClassOf(:NID_n10000006%20:NIS_n29857009)%0A  ... space ... linefeed
                lineIn = r.readLine();

                if (lineIn.toLowerCase().contains("owl:nothing")
                        || lineIn.toLowerCase().contains("equivalentclasses")) {
                    Logger.getLogger(OwlFunctionalSyntaxImport.class.getName()).log(Level.INFO, "OwlFuntionalSyntaxImport found owl:Nothing or equivalent.");
                    JOptionPane pane = new JOptionPane(
                            "Encounter owl:Nothing or equivalent!\nContinue or stop data import?");
                    Object[] options = new String[]{"Continue", "STOP"};
                    pane.setOptions(options);
                    JDialog dialog = pane.createDialog(new JFrame(), "Dialog");
                    dialog.setVisible(true);
                    Object obj = pane.getValue();
                    int result = -1;
                    for (int k = 0; k < options.length; k++) {
                        if (options[k].equals(obj)) {
                            result = k;
                        }
                    }

                    switch (result) {
                        case 0: // Continue
                            Logger.getLogger(OwlFunctionalSyntaxImport.class.getName()).log(Level.INFO, "OwlFuntionalSyntaxImport user continued import.");
                            break;
                        case 1: // Exit
                            Logger.getLogger(OwlFunctionalSyntaxImport.class.getName()).log(Level.INFO, "OwlFuntionalSyntaxImport user canceled import.");
                            owlRels.clear();
                            return owlRels;
                        default:
                            Logger.getLogger(OwlFunctionalSyntaxImport.class.getName()).log(Level.INFO, "OwlFuntionalSyntaxImport missing user response.");
                    }
                }

                if (lineIn.contains("SubClassOf") && lineIn.contains("NID_")) {
                    lineIn = lineIn.replace("SubClassOf(:NID_n", "-");
                    lineIn = lineIn.replace(":NID_n", "-");
                    lineIn = lineIn.replace(")", "");
                    String[] line = lineIn.split(" ");

                    if (line.length == 2) {
                        int nidC1 = Integer.parseInt(line[0]);
                        int nidC2 = Integer.parseInt(line[1]);

                        owlRels.add(new SnoRel(nidC1, nidC2, isaNid, 0)); // no group

                    }

                }

            }

        } catch (IOException ex) {
            Logger.getLogger(OwlFunctionalSyntaxImport.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                r.close();
            } catch (IOException ex) {
                Logger.getLogger(OwlFunctionalSyntaxImport.class.getName()).log(Level.SEVERE, null, ex);
            }

            // :!!!: throw TaskFailedException

        }

        return owlRels;
    }

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
        int countAB_RoleGroupNumbChange = 0;

        long startTime = System.currentTimeMillis();
        Collections.sort(snorelA);
        Collections.sort(snorelB);

        // Typically, A is the Classifier Path (for previously inferred)
        // Typically, B is the SnoRocket Results Set (for newly inferred)
        Iterator<SnoRel> itA = snorelA.iterator();
        Iterator<SnoRel> itB = snorelB.iterator();

        if (itA.hasNext() == false) {
            logger.log(Level.INFO, "::: [OWLF Import] initial writeback detected.");
            for (int i = 0; i < snorelB.size(); i++) {
                SnoRel srb = snorelB.get(i);
                writeBackCurrent(srb, classPathNid, vTime);
                if (i % 25000 == 0 && logger != null) {
                    logger.log(Level.INFO, "::: [OWLF Import] Concepts viewed:\t{0}", i);
                }
            }
            return "compareAndWriteBack total new ConDOR added Is-a rels: " + snorelB.size();
        }

        SnoRel rel_A = itA.next();
        SnoRel rel_B = itB.next();
        boolean done_A = false;
        boolean done_B = false;

        logger.log(Level.INFO, "\r\n::: [OWLF Import]"
                + "\r\n::: snorelA.size() = \t{0}\r\n::: snorelB.size() = \t{1}",
                new Object[]{snorelA.size(), snorelB.size()});

        // BY SORT ORDER, LOWER NUMBER ADVANCES FIRST
        while (!done_A && !done_B) {
            if (++countConSeen % 25000 == 0) {
                logger.log(Level.INFO, "::: [OWLF Import] compareAndWriteBack @ #\t{0}", countConSeen);
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
                        break;
                    }
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
                        break;
                    }
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

                // DETERMINE ROLE GROUP NUMBERS IN USE
                HashSet<Integer> groupNumbersInUse = new HashSet<Integer>();
                for (SnoGrp sg : groupList_A) {
                    if (sg.size() > 0) {
                        groupNumbersInUse.add(Integer.valueOf(sg.get(0).group));
                    }
                }

                // FIND GROUPS IN GROUPLIST_A WITHOUT AN EQUAL IN GROUPLIST_B
                // WRITE THESE GROUPED RELS AS "RETIRED"
                SnoGrpList groupList_NotEqual_A;
                if (groupList_A.size() > 0) {
                    groupList_NotEqual_A = groupList_A.whichNotEqual(groupList_B);
                    for (SnoGrp sg : groupList_NotEqual_A) {
                        for (SnoRel sr_A : sg) {
                            writeBackRetired(sr_A, classPathNid, vTime);
                        }
                    }
                    countA_Total += groupList_A.countRels();
                    countA_Diff += groupList_NotEqual_A.countRels();

                    // KEEP ONLY THE LOGICALLY SAME GROUPS
                    if (groupList_NotEqual_A.size() > 0) {
                        groupList_A.removeAll(groupList_NotEqual_A);
                    }
                }

                // COMPUTED ROLE GROUP NUMBER REPLACES :SIMPLE_ACTIVE_RETIRED_NONOVERLAP:
                if (groupList_B.isEmpty() == false) {
                    if (groupNumApproach == SnoGrpNumType.SNOGRP_NUMBER_APPROACH.RF1) {
                        groupList_B = calcNewRoleGroupNumbersRf1(groupList_A, groupList_B);
                    } else {
                        calcNewRoleGroupNumbersRf2Computed(groupList_B, groupNumbersInUse);
                    }
                }

                // FIND GROUPS IN GROUPLIST_B WITHOUT AN EQUAL IN GROUPLIST_A
                // WRITE THESE GROUPED RELS AS "NEW, CURRENT"
                SnoGrpList groupList_NotEqual_B;
                if (groupList_B.size() > 0) {
                    groupList_NotEqual_B = groupList_B.whichNotEqual(groupList_A);
                    for (SnoGrp sg : groupList_NotEqual_B) {
                        for (SnoRel sr_B : sg) {
                            writeBackCurrent(sr_B, classPathNid, vTime);
                        }
                    }
                    countB_Total += groupList_B.countRels();
                    countB_Diff += groupList_NotEqual_B.countRels();

                    // KEEP ONLY THE LOGICALLY SAME GROUPS
                    if (groupList_NotEqual_B.size() > 0) {
                        groupList_B.removeAll(groupList_NotEqual_B);
                    }
                }

                // CHECK FOR NEW ROLE GROUP NUMBERS ASSIGNED TO EXISTING LOGICAL ROLES
                if (groupList_A.size() > 0 && groupList_B.size() > 0) {
                    if (groupList_A.size() != groupList_B.size()) {
                        logger.log(Level.SEVERE, "ERROR: AB group list size not equal");
                    }

                    for (SnoGrp sgA : groupList_A) {
                        SnoGrp sgB = sgA.findLogicalEquivalent(groupList_B);
                        if (sgB != null) {
                            if (sgA.get(0).group != sgB.get(0).group) {
                                for (SnoRel snoRel : sgA) {
                                    writeBackModifiedGroup(snoRel, sgB.get(0).group, classPathNid, vTime);
                                    countAB_RoleGroupNumbChange++;
                                }
                            }
                        } else {
                            logger.log(Level.SEVERE, "ERROR: AB logical equivalent group not found");
                        }

                    }
                }

            } else if (rel_A.c1Id > rel_B.c1Id) {
                // CASE 2: LIST_B HAS CONCEPT NOT IN LIST_A
                // COMPLETELY *ADD* ALL THIS C1 FOR REL_B AS NEW, CURRENT
                int thisC1 = rel_B.c1Id;
                while (rel_B.c1Id == thisC1 && rel_B.group == 0) {
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

                // SEGMENT GROUPS IN LIST_B
                SnoGrpList groupList_B = new SnoGrpList();
                SnoGrp groupB = null;
                int prevGroup = Integer.MIN_VALUE;
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

                // COMPUTED ROLE GROUP NUMBER REPLACES & WRITE CURRENT
                if (groupList_B.isEmpty() == false) {
                    if (groupNumApproach == SnoGrpNumType.SNOGRP_NUMBER_APPROACH.RF1) {
                        groupList_B = calcNewRoleGroupNumbersRf1(null, groupList_B);
                    } else {
                        calcNewRoleGroupNumbersRf2Computed(groupList_B, null);
                    }
                    for (SnoGrp sg : groupList_B) {
                        for (SnoRel sr : sg) {
                            writeBackCurrent(sr, classPathNid, vTime);
                            countB_Diff++;
                            countB_Total++;
                        }
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
            }
        }

        while (!done_B) {
            int thisC1 = rel_B.c1Id;
            while (rel_B.c1Id == thisC1 && rel_B.group == 0) {
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

            // SEGMENT GROUPS IN LIST_B
            SnoGrpList groupList_B = new SnoGrpList();
            SnoGrp groupB = null;
            int prevGroup = Integer.MIN_VALUE;
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

            // COMPUTED ROLE GROUP NUMBER REPLACES & WRITE CURRENT
            if (groupList_B.isEmpty() == false) {
                if (groupNumApproach == SnoGrpNumType.SNOGRP_NUMBER_APPROACH.RF1) {
                    groupList_B = calcNewRoleGroupNumbersRf1(null, groupList_B);
                } else {
                    calcNewRoleGroupNumbersRf2Computed(groupList_B, null);
                }
                for (SnoGrp sg : groupList_B) {
                    for (SnoRel sr : sg) {
                        writeBackCurrent(sr, classPathNid, vTime);
                        countB_Diff++;
                        countB_Total++;
                    }
                }
            }
        }

        // CHECKPOINT DATABASE

        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [OWLF Import] compareAndWriteBack()");
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
        s.append("\r\n::: AB group number change:  \t").append(countAB_RoleGroupNumbChange);
        s.append("\r\n::: ");

        return s.toString();
    }

    private void writeBackModifiedGroup(SnoRel rel_A, int group, int writeToNid, long versionTime)
            throws IOException {

        try {
            I_RelVersioned rBean = tf.getRelationship(rel_A.relNid);
            if (rBean != null) {
                List<? extends I_RelTuple> rvList = rBean.getSpecifiedVersions(statusSet,
                        cViewPosSet, precedence, contradictionMgr);

                if (rvList.size() == 1) {
                    // CREATE RELATIONSHIP PART W/ TermFactory (RelationshipRevision)
                    I_RelPart analog = (I_RelPart) rvList.get(0).makeAnalog(isCURRENT,
                            versionTime,
                            condorAuthorNid,
                            config.getEditCoordinate().getModuleNid(),
                            writeToNid);
                    analog.setGroup(group);

                    I_GetConceptData thisC1 = tf.getConcept(rel_A.c1Id);

                    ts.writeDirect(thisC1);

                } else if (rvList.isEmpty()) {
                    StringBuilder sb = new StringBuilder("::: [SnorocketExTask] WARNING: writeBackModified() ");
                    sb.append("empty version list\trelNid=\t");
                    sb.append(Integer.toString(rel_A.relNid));
                    sb.append("\tc1=\t");
                    sb.append(Integer.toString(rel_A.c1Id));
                    sb.append("\t");
                    sb.append(tf.getConcept(rel_A.c1Id).toUserString());
                    logger.log(Level.INFO, sb.toString());
                } else {
                    StringBuilder sb = new StringBuilder("::: [SnorocketExTask] WARNING: writeBackModified() ");
                    sb.append("multiple last versions\trelNid=\t");
                    sb.append(Integer.toString(rel_A.relNid));
                    sb.append("\tc1=\t");
                    sb.append(Integer.toString(rel_A.c1Id));
                    sb.append("\t");
                    sb.append(tf.getConcept(rel_A.c1Id).toUserString());
                    logger.log(Level.INFO, sb.toString());
                }
            } else {
                logger.log(Level.INFO, "::: [SnorocketExTask] ERROR: writeBackModified() "
                        + "tf.getRelationship({0}) == null", rel_A.relNid);
            }

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void writeBackRetired(SnoRel rel_A, int writeToNid, long versionTime)
            throws IOException {

        try {
            I_RelVersioned rBean = tf.getRelationship(rel_A.relNid);
            if (rBean != null) {
                List<? extends I_RelTuple> rvList = rBean.getSpecifiedVersions(statusSet,
                        cViewPosSet, precedence, contradictionMgr);

                if (rvList.size() == 1) {

                    // CREATE RELATIONSHIP PART W/ TermFactory
                    rvList.get(0).makeAnalog(isRETIRED,
                            versionTime,
                            condorAuthorNid,
                            config.getEditCoordinate().getModuleNid(),
                            writeToNid);
                    // :!!!:TODO: move addUncommittedNoChecks() to more efficient location.
                    // more optimal to only call once per concept.
                    I_GetConceptData thisC1 = tf.getConcept(rel_A.c1Id);
                    ts.writeDirect(thisC1);

                } else if (rvList.isEmpty()) {
                    logger.log(Level.INFO, "::: [OWLF Import] ERROR: writeBackRetired() "
                            + "empty version list" + "\trelNid=\t{0}\tc1=\t{1}\t{2}",
                            new Object[]{rel_A.relNid, rel_A.c1Id, tf.getConcept(rel_A.c1Id).toLongString()});
                } else {
                    logger.log(Level.INFO, "::: [OWLF Import] ERROR: writeBackRetired() "
                            + "multiple last versions" + "\trelNid=\t{0}\tc1=\t{1}\t{2}",
                            new Object[]{rel_A.relNid, rel_A.c1Id, tf.getConcept(rel_A.c1Id).toLongString()});
                }
            } else {
                logger.log(Level.INFO, "::: [OWLF Import] ERROR: writeBackRetired() "
                        + "tf.getRelationship({0}) == null", rel_A.relNid);
            }

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void writeBackCurrent(SnoRel rel_B, int writeToNid, long versionTime)
            throws TerminologyException, IOException {

        I_GetConceptData thisC1 = tf.getConcept(rel_B.c1Id);
        // @@@ WRITEBACK NEW ISAs --> ALL NEW RELATIONS
        // CREATE RELATIONSHIP PART W/ TermFactory-->VobdEnv
        tf.newRelationshipNoCheck(UUID.randomUUID(), thisC1, rel_B.typeId, rel_B.c2Id,
                isCh_INFERRED_CHARACTERISTIC, isOPTIONAL_REFINABILITY, rel_B.group, isCURRENT,
                condorAuthorNid, writeToNid, versionTime);

        // :!!!:TODO: [SnorocketTask] move addUncommittedNoChecks() to more efficient location.
        // more optimal to only call once per concept.
        ts.writeDirect(thisC1);
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
            if (SnomedMetadataRfx.getReleaseFormat() == 1) {
                isCh_INFERRED_CHARACTERISTIC = SnomedMetadataRfx.getREL_CH_DEFINING_CHARACTERISTIC_NID();
            } else {
                isCh_INFERRED_CHARACTERISTIC = SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID();
            }

            condorAuthorNid = DescriptionLogic.CONDOR_REASONER.getLenient().getNid();

        } catch (Exception ex) {
            Logger.getLogger(CondorRun.class.getName()).log(Level.SEVERE, null, ex);
            return Condition.STOP;
        }
        statusSet = tf.newIntSet();
        statusSet.add(isCURRENT);
        statusSet.add(isLIMITED);
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
            logger.log(Level.INFO, e.toString());
        } catch (TerminologyException e) {
            logger.log(Level.INFO, e.toString());
        }

        return sb.toString();
    }

    private String toStringNids() {
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [CondorRunTask]");
        s.append("\r\n:::\t").append(isaNid).append("\t : isaNid");
        s.append("\r\n:::\t").append(rootNid).append("\t : rootNid");
        s.append("\r\n:::\t").append(isCURRENT).append("\t : isCURRENT");
        StringBuilder append = s.append("\r\n:::\t").append(isRETIRED).append("\t : isRETIRED");
        s.append("\r\n:::\t").append(isOPTIONAL_REFINABILITY).append("\t : isOPTIONAL_REFINABILITY");
        s.append("\r\n:::\t").append(isNOT_REFINABLE).append("\t : isNOT_REFINABLE");
        s.append("\r\n:::\t").append(isMANDATORY_REFINABILITY).append("\t : isMANDATORY_REFINABILITY");

        s.append("\r\n:::\t").append(isCh_STATED_RELATIONSHIP).append("\t : isCh_STATED_RELATIONSHIP");
        s.append("\r\n:::\t").append(isCh_INFERRED_CHARACTERISTIC).append("\t : defining/inferred");
        // :!!!:???: s.append("\r\n:::\t").append(isCh_STATED_AND_INFERRED_RELATIONSHIP);
        // :!!!:???: s.append("\t : isCh_STATED_AND_INFERRED_RELATIONSHIP");
        // :!!!:???: s.append("\r\n:::\t").append(isCh_STATED_AND_SUBSUMED_RELATIONSHIP);
        // :!!!:???: s.append("\t : isCh_STATED_AND_SUBSUMED_RELATIONSHIP");
        s.append("\r\n");
        return s.toString();
    }

    private String toStringPathPos(List<PositionBI> pathPos, String pStr) {
        // BUILD STRING
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [OWLF Import] PATH ID -- ").append(pStr).append("\r\n");
        for (PositionBI position : pathPos) {
            s.append("::: .. PATH:\t").append(toStringCNid(position.getPath().getConceptNid())).append("\r\n");
        }
        s.append(":::");
        return s.toString();
    }

    void calcNewRoleGroupNumbersRf2Computed(SnoGrpList groupList, HashSet<Integer> inUse) {
        try {
            // Create uuid based role group ids
            SnoGrpUuidList sgul = new SnoGrpUuidList(groupList);
            assert groupList.size() == sgul.size();

            // Calculate role group numbers
            sgul.calcNewRoleGroupNumbers(inUse);

            // Transfer role group computed from UUIDs to int SnoRelGrp
            for (int i = 0; i < sgul.size(); i++) {
                int group = sgul.get(i).get(0).group;
                SnoGrp g = groupList.get(i);
                for (SnoRel snoRel : g) {
                    snoRel.group = group;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SnorocketExTask.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    SnoGrpList calcNewRoleGroupNumbersRf1(SnoGrpList logicallyEqual_A, SnoGrpList all_B) {

        SnoGrpList groupList_NotEqual_B = all_B;

        // Determine inferred role group numbers in use.
        HashSet<Integer> groupNumbersInUse = new HashSet<Integer>();
        if (logicallyEqual_A != null) {
            for (SnoGrp sg : logicallyEqual_A) {
                if (sg.size() > 0) {
                    groupNumbersInUse.add(Integer.valueOf(sg.get(0).group));
                }
            }
            if (all_B.size() > 0) {
                groupList_NotEqual_B = all_B.whichNotEqual(logicallyEqual_A);
            }
        }

        Integer groupNumber = 1;
        for (SnoGrp sg : groupList_NotEqual_B) {
            // find next free group number
            while (groupNumbersInUse.contains(groupNumber)) {
                groupNumber++;
            }
            groupNumbersInUse.add(groupNumber);
            for (SnoRel sr_B : sg) {
                // :SIMPLE_ACTIVE_RETIRED_NONOVERLAP:
                sr_B.group = groupNumber.intValue();
            }
        }

        return groupList_NotEqual_B;
    }

    /**
     * actionPerformed sets an internal flag to stop from processing
     *
     * @param arg0
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        continueThisAction = false;
    }

    private String toStringLapseSec(long startTime) {
        StringBuilder s = new StringBuilder();
        long stopTime = System.currentTimeMillis();
        long lapseTime = stopTime - startTime;
        s.append((float) lapseTime / 1000).append(" (seconds)");
        return s.toString();
    }

    private ArrayList<SnoRel> removeDuplciates(ArrayList<SnoRel> cAllIInferredSnoRels) {
        // Count duplicates
        int count = 0;
        for (int idxFrom = 0; idxFrom < cAllIInferredSnoRels.size() - 1; idxFrom++) {
            SnoRel srA = cAllIInferredSnoRels.get(idxFrom);
            SnoRel srB = cAllIInferredSnoRels.get(idxFrom + 1);
            if (srA.c1Id == srB.c1Id && srA.c2Id == srB.c2Id && srA.typeId == srB.typeId) {
                count++;
            }
        }

        ArrayList<SnoRel> aList = new ArrayList<SnoRel>(cAllIInferredSnoRels.size() - count);
        count = 0;
        int idxFrom = 0;
        for (; idxFrom < cAllIInferredSnoRels.size() - 1; idxFrom++) {
            SnoRel srA = cAllIInferredSnoRels.get(idxFrom);
            SnoRel srB = cAllIInferredSnoRels.get(idxFrom + 1);
            if (srA.c1Id == srB.c1Id && srA.c2Id == srB.c2Id && srA.typeId == srB.typeId) {
                count++;
            } else {
                aList.add(srA);
            }

        }
        aList.add(cAllIInferredSnoRels.get(idxFrom));

        return aList;
    }
}
