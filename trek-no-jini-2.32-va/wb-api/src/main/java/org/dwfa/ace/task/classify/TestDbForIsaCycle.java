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
package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 *
 * @author marc
 */
@BeanList(specs = {
    @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN)})
public class TestDbForIsaCycle extends AbstractTask {

    private String uuidRoleRoot = "6155818b-09ed-388e-82ce-caa143423e99";
    private String reportCycles = "CycleReport";
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
    int cEditPathNid = Integer.MIN_VALUE; // :TODO: move to logging
    PathBI cEditPathBI = null;
    List<PositionBI> cEditPathListPositionBI = null; // Edit (Stated) Path I_Positions
    // OUTPUT PATHS
    int cViewPathNid; // :TODO: move to logging
    PathBI cViewPathBI; // Used for write back value
    List<PositionBI> cViewPathListPositionBI; // Classifier (Inferred) Path I_Positions
    // MASTER DATA SETS
    List<SnoCon> cycleSnoCons; // "Edit Path" Concepts
    // USER INTERFACE
    private static final Logger logger = Logger.getLogger(TestDbForIsaCycle.class.getName());
    private I_TermFactory tf = null;
    private I_ConfigAceFrame config = null;

    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            logger.info("\r\n::: [TestDbForIsaCycle] execute() -- begin");
            tf = Terms.get();
            setupCoreNids();
            config = tf.getActiveAceFrameConfig();

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
            logger.log(Level.INFO, "\r\n::: [TestDbForIsaCycle] STATED (Edit) PATH DATA : {0}",
                    pcEdit.getStats(startTime));

            if (cycleSnoCons.size() > 0) {
//                if (reportCycles != null) {
//                    SnoCon. dumpToFile(cycleSnoCons,
//                            "target" + File.separator + reportCycles + "_FAIL.txt", 5);
//                }
                StringBuilder sb = new StringBuilder();
                sb.append("CYCLES DETECTED ... ");
                sb.append(cycleSnoCons.size());
                for (int i = 0; i < cycleSnoCons.size() && i < 6; i++) {
                    SnoCon sc = cycleSnoCons.get(i);
                    I_GetConceptData c1 = tf.getConcept(sc.id);
                    sb.append("\r\n");
                    sb.append(c1.getPrimUuid());
                    sb.append("\t");
                    sb.append(c1.getInitialText());
                }
                logger.log(Level.INFO, sb.toString());
                showCycleDialog(sb.toString());
            } else {
                logger.info("\r\n::: [TestDbForIsaCycle] NO CYCLES DETECTED");
                showCycleDialog("NO CYCLES DETECTED");
            }

        } catch (TerminologyException ex) {
            Logger.getLogger(TestDbForIsaCycle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestDbForIsaCycle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(TestDbForIsaCycle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TestDbForIsaCycle.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Condition.CONTINUE;
    }

    private void showCycleDialog(String message) {
        JFrame parentFrame = null;
        for (JFrame frame : OpenFrames.getFrames()) {
            if (frame.isActive()) {
                parentFrame = frame;
                break;
            }
        }
        JOptionPane.showMessageDialog(parentFrame, message);

    }

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // nothing to do.
    }

    @Override
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    private void setupCoreNids() throws TaskFailedException {
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
            throw new TaskFailedException("setupCoreNids", e);
        } catch (IOException e) {
            logger.info(e.toString());
            throw new TaskFailedException("setupCoreNids", e);
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
