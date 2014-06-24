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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.*;
import org.dwfa.ace.api.I_ConfigAceFrame.CLASSIFIER_INPUT_MODE_PREF;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.classify.*;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.helper.descriptionlogic.DescriptionLogic;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.binding.snomed.TermAux;

/**
 *
 * @author marc
 */
@BeanList(specs = {
    @Spec(directory = "tasks/owl", type = BeanType.TASK_BEAN)})
public class OwlFunctionalSyntaxExport extends AbstractTask implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private boolean continueThisAction;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
    }
    // USER INTERFACE
    private static final Logger logger = Logger.getLogger("OWL");
    private I_TermFactory tf;
    private I_ConfigAceFrame config = null;
    private ViewCoordinate vc = null;
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
    private static int workbenchAuxPath = Integer.MIN_VALUE;
    private static int condorAuthorNid = Integer.MIN_VALUE;
    private UUID uuidSourceSnomedLong = null;
    private static int snomedLongAuthorityNid = Integer.MIN_VALUE;
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
    private int[] roleNidArray;

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do
    }

    @Override
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        logger.info("\r\n::: [OwlFunctionalSyntaxExport] evaluate() -- begin");

        try {
            tf = Terms.get();
            config = tf.getActiveAceFrameConfig();
            vc = config.getViewCoordinate();
            cEditSnoCons = new ArrayList<SnoCon>();
            cEditSnoRels = new ArrayList<SnoRel>();

            // show in Activity Viewer window
            config = Terms.get().getActiveAceFrameConfig();
            I_ShowActivity gui1 = tf.newActivityPanel(true, config, "Export OWL Task", false);
            gui1.addRefreshActionListener(this);
            gui1.setProgressInfoUpper("Export OWL Task");
            gui1.setIndeterminate(true);
            gui1.setProgressInfoLower("... in process ...");
            long startTime1 = System.currentTimeMillis();

            setupCoreNids();
            setupPaths();
            logger.info(toStringPathPos(cEditPathListPositionBI, "Primary Input Path"));
            roleNidArray = setupRoleNids();

            I_ShowActivity gui;
            gui = tf.newActivityPanel(true, config, "Export OWL: get stated rels", false);
            gui.addRefreshActionListener(this);
            gui.setProgressInfoUpper("Export OWL: read stated rels from database");
            gui.setIndeterminate(false);
            gui.setMaximum(1500000);
            gui.setValue(0);
            long startTime = System.currentTimeMillis();
            SnoPathProcessCondorStated pcEdit;
            if (config.getClassifierInputMode() == CLASSIFIER_INPUT_MODE_PREF.EDIT_PATH) {
                pcEdit = new SnoPathProcessCondorStated(logger, cEditSnoCons, cEditSnoRels,
                        allowedRoleTypes, statusSet, cEditPosSet, gui, config.getPrecedence(),
                        config.getConflictResolutionStrategy());
                tf.iterateConcepts(pcEdit);
                logger.log(Level.INFO, "\r\n::: [OWLF] GET STATED (Edit) PATH DATA : {0}",
                        pcEdit.getStats(startTime1));
            } else if (config.getClassifierInputMode() == CLASSIFIER_INPUT_MODE_PREF.VIEW_PATH) {
                pcEdit = new SnoPathProcessCondorStated(logger, cEditSnoCons, cEditSnoRels,
                        allowedRoleTypes, statusSet, cViewPosSet, gui, config.getPrecedence(),
                        config.getConflictResolutionStrategy());
                tf.iterateConcepts(pcEdit);
                logger.log(Level.INFO, "\r\n::: [OWLF] GET STATED (View) PATH DATA : {0}",
                        pcEdit.getStats(startTime1));
            } else if (config.getClassifierInputMode()
                    == CLASSIFIER_INPUT_MODE_PREF.VIEW_PATH_WITH_EDIT_PRIORITY) {
                pcEdit = new SnoPathProcessCondorStated(logger, cEditSnoCons, cEditSnoRels,
                        allowedRoleTypes, statusSet, cViewPosSet, cEditPosSet, gui,
                        config.getPrecedence(), config.getConflictResolutionStrategy());
                tf.iterateConcepts(pcEdit);
                logger.log(Level.INFO, "\r\n::: [OWLF] GET STATED (View w/ edit priority) PATH DATA : {0}",
                        pcEdit.getStats(startTime1));
            } else {
                throw new TaskFailedException("config.getClassifierInputMode() case not implemented.");
            }
            gui.setProgressInfoLower(cEditSnoRels.size() 
                    + " rels processed, time = " + toStringLapseSec(startTime));
            gui.complete();
            gui1.setProgressInfoLower("... writing stated data ...");

            writeOwl(cEditSnoCons, cEditSnoRels, "condor_in.owl");
            // writeOwl(cEditSnoCons, cEditSnoRels, "OWLF_sctIds_t" + startTime + ".txt");

            gui1.setProgressInfoLower("complete, time = " + toStringLapseSec(startTime1));
            gui1.complete();

        } catch (TerminologyException ex) {
            Logger.getLogger(OwlFunctionalSyntaxExport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OwlFunctionalSyntaxExport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(OwlFunctionalSyntaxExport.class.getName()).log(Level.SEVERE, null, ex);
        }
        cEditSnoRels = null;
        System.gc();

        logger.info("\r\n::: [OwlFunctionalSyntaxExport] evaluate() -- completed");
        return Condition.CONTINUE;
    }

    private Condition setupPaths() {
        try {
            // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
            // Setup to exclude Workbench Auxiliary on path
            UUID wAuxUuid = UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66");
            I_GetConceptData wAuxCb = tf.getConcept(wAuxUuid);
            workbenchAuxPath = wAuxCb.getConceptNid();

            // GET ALL EDIT_PATH ORIGINS
            if (config.getEditingPathSet() == null) {
                String errStr = "(Edit Path error) Edit path is not set.";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
                return Condition.STOP;
            } else if (config.getEditingPathSet().size() != 1) {
                String errStr = "(Edit Path error) Profile must have exactly one edit path. Found: "
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

        } catch (TerminologyException ex) {
            Logger.getLogger(OwlFunctionalSyntaxExport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OwlFunctionalSyntaxExport.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    private Condition setupCoreNids() {
        // SETUP CORE NATIVES IDs
        try {
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

            // 0 CURRENT, 1 RETIRED
            isCURRENT = SnomedMetadataRfx.getSTATUS_CURRENT_NID();
            isLIMITED = SnomedMetadataRfx.getSTATUS_LIMITED_NID();
            isRETIRED = SnomedMetadataRfx.getSTATUS_RETIRED_NID();
            isOPTIONAL_REFINABILITY = SnomedMetadataRfx.getREL_OPTIONAL_REFINABILITY_NID();
            isNOT_REFINABLE = SnomedMetadataRfx.getREL_NOT_REFINABLE_NID();
            isMANDATORY_REFINABILITY = SnomedMetadataRfx.getREL_MANDATORY_REFINABILITY_NID();
            isCh_STATED_RELATIONSHIP = SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID();
            isCh_DEFINING_CHARACTERISTIC = SnomedMetadataRfx.getREL_CH_DEFINING_CHARACTERISTIC_NID();

            condorAuthorNid = DescriptionLogic.CONDOR_REASONER.getLenient().getNid();

            uuidSourceSnomedLong = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next();
            snomedLongAuthorityNid = tf.uuidToNative(uuidSourceSnomedLong);

        } catch (Exception ex) {
            Logger.getLogger(OwlFunctionalSyntaxExport.class.getName()).log(Level.SEVERE, null, ex);
            return Condition.STOP;
        }
        statusSet = tf.newIntSet();
        statusSet.add(isCURRENT);
        statusSet.add(isLIMITED);
        return Condition.CONTINUE;
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
                        if (pos.getPath().getConceptNid() == rPart.getPathNid()) {
                            if (rPart1 == null) {
                                rPart1 = rPart; // ... KEEP FIRST_INSTANCE
                            } else if (rPart1.getTime() < rPart.getTime()) {
                                rPart1 = rPart; // ... KEEP MORE_RECENT PART
                            } else if (rPart1.getTime() == rPart.getTime()) {
                                countRelDuplVersion++;
                                if (rPart.getStatusNid() == isCURRENT) {
                                    rPart1 = rPart; // KEEP CURRENT PART
                                }
                            }
                        }
                    }
                    if (rPart1 != null) {
                        break; // IF FOUND ON THIS PATH, STOP SEARCHING
                    }
                }

                if ((rPart1 != null) && (rPart1.getStatusNid() == isCURRENT)
                        && (rPart1.getTypeNid() == isaNid)) {
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
            sb.append(":::   \t");
            sb.append(toStringCNid(cNid));
            sb.append("\r\n");
        }
        logger.info(sb.toString());

        allowedRoleTypes = tf.newIntSet();
        allowedRoleTypes.addAll(resultInt);
        return resultInt;
    }

    private String toStringCNid(int cNid) {
        StringBuilder sb = new StringBuilder();
        try {
            I_GetConceptData c = tf.getConcept(cNid);
            sb.append(c.getUids().iterator().next());
            sb.append("\t");
            sb.append(cNid);
            sb.append("\t");
            sb.append(c.getInitialText());

        } catch (TerminologyException ex) {
            Logger.getLogger(OwlFunctionalSyntaxExport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OwlFunctionalSyntaxExport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

    private String toStringPathPos(List<PositionBI> pathPos, String pStr) {
        // BUILD STRING
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [SnorocketTask] PATH ID -- ");
        s.append(pStr);
        s.append("\r\n");
        for (PositionBI position : pathPos) {
            s.append("::: ... PATH:\t");
            s.append(toStringCNid(position.getPath().getConceptNid()));
            s.append("\r\n");
        }
        s.append(":::");
        return s.toString();
    }

    private void writeOwl(List<SnoCon> scl, List<SnoRel> srl, String fName)
            throws TerminologyException, IOException, InvalidCAB, ContradictionException {
        logger.info("\r\n::: [OwlFunctionalSyntaxExport] writeOwl() -- begin");

        // SORT CONCEPTS & RELATIONSHIPS
        Collections.sort(scl); // cNid
        Collections.sort(srl); // c1Nid, group, typeId, c2Nid
        List<SnoRel> c1Parents = new ArrayList<SnoRel>();
        List<SnoRel> c1Roles = new ArrayList<SnoRel>();

        BufferedWriter bw = new BufferedWriter(new FileWriter(fName));

        // WRITE HEADER
        bw.append("Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)\n");
        bw.append("Prefix(owl:=<http://www.w3.org/2002/07/owl#>)\n");
        bw.append("Prefix(:=<http://www.ihtsdo.org/>)\n");
        bw.append("Prefix(xml:=<http://www.w3.org/XML/1998/namespace>)\n");
        bw.append("Prefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)\n");
        bw.append("Prefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)\n");

        // BEGIN ONTOLOGY
        bw.append("\n\nOntology(<http://www.ihtsdo.org/wb.nid.owl>\n");
        bw.append("Annotation(rdfs:comment \"IHTSDO Workbench native id export.\")\n");
        bw.append("Declaration(ObjectProperty(:RoleGroup))\n");
        bw.append("AnnotationAssertion(rdfs:label :RoleGroup \"RoleGroup\")\n");

        // WRITE ROLE DEFINITIONS
        HashSet<Integer> roleHashSet = new HashSet<Integer>();
        for (int roleNid : roleNidArray) {
            roleHashSet.add(roleNid);
        }
        // for (int roleNid : roleNidArray) {
        int relIdx = 0;
        for (SnoCon sc : scl) {
            int roleNid = sc.id;
            if (roleNid == isaNid || roleHashSet.contains(roleNid) == false) {
                continue;
            }
            String rNidStr = Integer.toString(roleNid).replace("-", "n");
            String rText = tf.getConcept(roleNid).getInitialText().replace("\"", "\'");
            int countRightBracket = countOccurrences(rText, '(');
            int countLeftBracket = countOccurrences(rText, ')');
            if (countRightBracket != countLeftBracket) {
                rText = rText.replace("(", "%28");
                rText = rText.replace(")", "%29");
            }
            bw.append("Declaration(ObjectProperty(:NID_" + rNidStr + "))\n");
            bw.append("AnnotationAssertion(rdfs:label :NID_" + rNidStr + " \"" + rText + "\")\n");

            // Partition parent
            c1Parents.clear();
            while (relIdx < srl.size() && srl.get(relIdx).c1Id <= sc.id) {
                if (srl.get(relIdx).c1Id == sc.id) {
                    if (srl.get(relIdx).typeId == isaNid) {
                        c1Parents.add(srl.get(relIdx));
                    } else {
                        logger.log(Level.INFO, "\r\n::: attribute role not expected {0}", roleNid);
                    }
                }
                relIdx++;
            }

            // Role Root for classification
            // "Concept model attribute (attribute)"
            // 410662002
            // 6155818b-09ed-388e-82ce-caa143423e99
            if (c1Parents.size() == 1) {
                int c2Nid = c1Parents.get(0).c2Id;
                if (c2Nid != rootRoleNid) {
                    String c2NidStr = Integer.toString(c2Nid).replace("-", "n");
                    if (roleNid == rootRoleNid) {
                        bw.append("SubClassOf(:NID_" + rNidStr + " :NID_" + c2NidStr + ")\n");
                    } else {
                        bw.append("SubObjectPropertyOf(:NID_" + rNidStr + " :NID_" + c2NidStr + ")\n");
                    }
                }
            } else {
                logger.log(Level.INFO, "\r\n::: only one parent expected {0}", roleNid);
            }

            // :NYI: right identity
            // bw.append("SubObjectPropertyOf(ObjectPropertyChain(:NID_" + rnid
            //        + " :NID_" + NYI +") :NID_" + rnid + ")\n");
        }

        // WRITE CONCEPT DEFINITIONS
        relIdx = 0;
        for (SnoCon sc : scl) {
            if (roleHashSet.contains(sc.id) == true && sc.id != isaNid) {
                continue;
            }
            String c1NidStr = Integer.toString(sc.id).replace("-", "n");
            String c1Text = tf.getConcept(sc.id).getInitialText().replace("\"", "\'");
            int countRightBracket = countOccurrences(c1Text, '(');
            int countLeftBracket = countOccurrences(c1Text, ')');
            if (countRightBracket != countLeftBracket) {
                c1Text = c1Text.replace("(", "%28");
                c1Text = c1Text.replace(")", "%29");
            }

            bw.append("Declaration(Class(:NID_" + c1NidStr + "))\n");
            bw.append("AnnotationAssertion(rdfs:label :NID_" + c1NidStr + " \"" + c1Text + "\")\n");

            // Partition parent and role-value lists
            c1Parents.clear();
            c1Roles.clear();
            while (relIdx < srl.size() && srl.get(relIdx).c1Id <= sc.id) {
                if (srl.get(relIdx).c1Id == sc.id) {
                    if (srl.get(relIdx).typeId == isaNid) {
                        c1Parents.add(srl.get(relIdx));
                    } else {
                        c1Roles.add(srl.get(relIdx));
                    }
                }
                relIdx++;
            }

            //
            if (c1Parents.size() == 0L && c1Roles.size() == 0L) {
                // nothing to do
            } else if (c1Parents.size() == 1 && c1Roles.size() == 0L) {
                String c2NidStr = Integer.toString(c1Parents.get(0).c2Id).replace("-", "n");
                bw.append("SubClassOf(:NID_" + c1NidStr + " :NID_" + c2NidStr + ")\n");
            } else {
                if (sc.isDefined) {
                    bw.append("EquivalentClasses(:NID_" + c1NidStr + " ObjectIntersectionOf(");
                } else {
                    bw.append("SubClassOf(:NID_" + c1NidStr + " ObjectIntersectionOf(");
                }

                // ADD PARENTS
                for (SnoRel snoRel : c1Parents) {
                    String c2NidStr = Integer.toString(snoRel.c2Id).replace("-", "n");
                    bw.append(":NID_" + c2NidStr + " ");
                }
                bw.append("\n");

                //
                HashSet<Integer> neverGroupedSet;
                neverGroupedSet = new HashSet<Integer>();


                // ADD ROLE-VALUES
                if (c1Roles.size() > 0L) {
                    // SEGMENT ROLE GROUPS.  GROUP 0 IS ROLE GROUP OF ONE MEMBER.
                    SnoGrpList groupList = new SnoGrpList();
                    SnoGrp group = null;
                    int prevGroup = 0;
                    for (SnoRel snoRel : c1Roles) {
                        if (snoRel.group == 0) {
                            group = new SnoGrp();
                            groupList.add(group);
                        } else if (snoRel.group != prevGroup) {
                            group = new SnoGrp();
                            groupList.add(group);
                        }

                        group.add(snoRel);
                        prevGroup = snoRel.group;
                    }

                    // WRITE ROLE GROUPS
                    for (SnoGrp rg : groupList) {
                        if (rg.size() > 1) {
                            bw.append("       ObjectSomeValuesFrom(:RoleGroup ObjectIntersectionOf(\n");
                            for (SnoRel r : rg) {
                                String typeNid = Integer.toString(r.typeId).replace("-", "n");
                                String c2Nid = Integer.toString(r.c2Id).replace("-", "n");
                                if (DescriptionLogic.isNegatedRel(r.relNid, vc)) {
                                    bw.append("                        ObjectSomeValuesFrom(:NID_" + typeNid
                                            + " ObjectComplementOf(:NID_" + c2Nid + "))\n");
                                } else {
                                    bw.append("                        ObjectSomeValuesFrom(:NID_" + typeNid
                                            + " :NID_" + c2Nid + ")\n");
                                }
                            }
                            bw.append("))");

                        } else if (rg.size() == 1) { //  # No need for intersectionOf or looping.
                            String typeNid = Integer.toString(rg.get(0).typeId).replace("-", "n");
                            String c2Nid = Integer.toString(rg.get(0).c2Id).replace("-", "n");
                            if (neverGroupedSet.contains(rg.get(0).typeId)) {
                                if (DescriptionLogic.isNegatedRel(rg.get(0).relNid, vc)) {
                                    bw.append("       ObjectSomeValuesFrom(:NID_" + typeNid
                                            + " ObjectComplementOf(:NID_" + c2Nid + "))\n");
                                } else {
                                    bw.append("       ObjectSomeValuesFrom(:NID_" + typeNid
                                            + " :NID_" + c2Nid + ")\n");
                                }
                            } else {
                                if (DescriptionLogic.isNegatedRel(rg.get(0).relNid, vc)) {
                                    bw.append("       ObjectSomeValuesFrom(:RoleGroup ObjectSomeValuesFrom(:NID_"
                                            + typeNid + " ObjectComplementOf(:NID_" + c2Nid + ")))\n");
                                } else {
                                    bw.append("       ObjectSomeValuesFrom(:RoleGroup ObjectSomeValuesFrom(:NID_"
                                            + typeNid + " :NID_" + c2Nid + "))\n");
                                }
                            }
                        }
                    }

                    bw.append("))\n");
                } else {
                    bw.append("))\n");
                }
            }
        }

        // ADD DISJOINT CLASSES
        I_GetConceptData cb = tf.getConcept(DescriptionLogic.getDisjointSetsRefsetNid());
        I_IntSet auxIsaRoleType = tf.newIntSet();
        auxIsaRoleType.add(tf.uuidToNative(TermAux.IS_A.getUuids()));
        I_IntSet currentStatusIntSet = tf.newIntSet();
        currentStatusIntSet.add(SnomedMetadataRfx.getSTATUS_CURRENT_NID());

        // APPROACH: getDestRelOrigins
        Set<? extends I_GetConceptData> disjointSets;
        disjointSets = cb.getDestRelOrigins(currentStatusIntSet, auxIsaRoleType,
                config.getViewPositionSetReadOnly(), config.getPrecedence(),
                config.getConflictResolutionStrategy());

        for (I_GetConceptData disjointCollection : disjointSets) {
            Collection<? extends RefexChronicleBI<?>> members = disjointCollection.getRefsetMembers();

            bw.append("DisjointClasses(");
            for (RefexChronicleBI rcbi : members) {
                bw.append(" :NID_" + Integer.toString(
                        rcbi.getReferencedComponentNid()).replace("-", "n"));
            }
            bw.append(" )\n");
        }

        // APPROACH: getDestRelTuples
//        List<? extends I_RelTuple> disjointTupleList = cb.getDestRelTuples(auxIsaRoleType,
//                config.getPrecedence(), config.getConflictResolutionStrategy());
//        for (I_RelTuple relTuple : disjointTupleList) {
//            I_GetConceptData disjointCollection = tf.getConcept(relTuple.getC1Id());
//            Collection<? extends RefexChronicleBI<?>> members = disjointCollection.getRefsetMembers();
//
//            bw.append("DisjointClasses(");
//            for (RefexChronicleBI rcbi : members) {
//                bw.append(" :NID_" + rcbi.getReferencedComponentNid());
//            }
//            bw.append(" )\n");
//        }

        // ADD UNION SETS
        // :!!!:SNOOWL:
        //Declaration(Class(:NID_nUION))
        //AnnotationAssertion(rdfs:label :NID_nUION "Union of This or That (disorder)")
        //EquivalentClasses( :NID_nUION ObjectUnionOf( :NID_n2146864209 :NID_n2146618658))

        cb = tf.getConcept(DescriptionLogic.getUnionSetsRefsetNid());
        auxIsaRoleType = tf.newIntSet();
        auxIsaRoleType.add(tf.uuidToNative(TermAux.IS_A.getUuids()));
        currentStatusIntSet = tf.newIntSet();
        currentStatusIntSet.add(SnomedMetadataRfx.getSTATUS_CURRENT_NID());

        int parentMemberTypeNid = tf.getConcept(
                RefsetAuxiliary.Concept.MARKED_PARENT.getPrimoridalUid()).getConceptNid();
        int normalMemberNid = tf.getConcept(
                RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids()).getConceptNid();

        // APPROACH: getDestRelOrigins
        Set<? extends I_GetConceptData> unionsSets;
        unionsSets = cb.getDestRelOrigins(currentStatusIntSet, auxIsaRoleType,
                config.getViewPositionSetReadOnly(), config.getPrecedence(),
                config.getConflictResolutionStrategy());

        for (I_GetConceptData unionCollection : unionsSets) {
            Collection<? extends RefexVersionBI<?>> members;
            members = unionCollection.getRefsetMembersActive(vc);

            // find the parent
            RefexNidVersionBI union = null;
            for (RefexVersionBI<?> rvbi : members) {
                if (RefexNidVersionBI.class.isAssignableFrom(rvbi.getClass())
                        && parentMemberTypeNid == ((RefexNidVersionBI) rvbi).getNid1()) {
                    union = (RefexNidVersionBI) rvbi;
                    break;
                }
            }
            String unionStr = Integer.toString(union.getReferencedComponentNid()).replace("-", "n");
            bw.append("Declaration(Class(:NID_" + unionStr + "))\n");
            String unionText = unionCollection.getInitialText().replace("\"", "\'");
            bw.append("AnnotationAssertion(rdfs:label :NID_" + unionStr
                    + " \"" + unionText + "\")\n");
            bw.append("EquivalentClasses( :NID_" + unionStr);

            // add normal members
            bw.append(" ObjectUnionOf(");
            for (RefexVersionBI<?> rvbi : members) {
                if (RefexNidVersionBI.class.isAssignableFrom(rvbi.getClass())
                        && normalMemberNid == ((RefexNidVersionBI) rvbi).getNid1()) {
                    bw.append(" :NID_" + Integer.toString(
                            rvbi.getReferencedComponentNid()).replace("-", "n"));
                }
            }
            bw.append(" ))\n"); // END UNION SET
        }

        // END ONTOLOGY
        bw.append(")\n");

        bw.flush();
        bw.close();
    }

    private void convertNidToSctidRel(List<SnoRel> srl) {
        List<SnoRelLong> srlSctId = new ArrayList<SnoRelLong>();

        // Convert native id to sct id
        long c1 = Long.MAX_VALUE, role = Long.MAX_VALUE, c2 = Long.MAX_VALUE;
        try {
            for (SnoRel sr : srl) {
                //c1
                I_GetConceptData cb = tf.getConcept(sr.c1Id);
                if (cb != null) {
                    List<? extends I_IdVersion> idv1 = cb.getIdentifier().getIdVersions();
                    c1 = Long.MAX_VALUE;
                    for (I_IdVersion id : idv1) {
                        if (id.getAuthorityNid() == snomedLongAuthorityNid) {
                            c1 = (Long) id.getDenotation();
                            break;
                        }
                    }
                } else {
                    logger.log(Level.INFO, "\r\n::: [OwlFunctionalSyntaxExport] error c1Id = {0}", sr.c1Id);
                }

                // c2
                I_GetConceptData cb2 = tf.getConcept(sr.c2Id);
                if (cb2 != null) {
                    I_Identify idfr2 = cb2.getIdentifier();
                    if (idfr2 != null) {
                        List<? extends I_IdVersion> idv2 = idfr2.getIdVersions();
                        c2 = Long.MAX_VALUE;
                        for (I_IdVersion id : idv2) {
                            if (id.getAuthorityNid() == snomedLongAuthorityNid) {
                                c2 = (Long) id.getDenotation();
                                break;
                            }
                        }
                    } else {
                        logger.log(Level.INFO,
                                "\r\n::: [OWLF] no identifier warning c2Id = {0} {1} {2} where... c1Id {3} {4} {5}",
                                new Object[]{sr.c2Id, cb2.getPrimUuid(), cb2.getInitialText(), sr.c1Id,
                                    cb.getPrimUuid(), cb.getInitialText()});
                    }
                } else {
                    logger.log(Level.INFO, "\r\n::: [OWLF] error c2Id = {0}", sr.c2Id);
                }

                // role
                cb = tf.getConcept(sr.typeId);
                if (cb != null) {
                    I_Identify idfr = cb.getIdentifier();
                    if (idfr != null) {
                        List<? extends I_IdVersion> idv3 = idfr.getIdVersions();
                        role = Long.MAX_VALUE;
                        for (I_IdVersion id : idv3) {
                            if (id.getAuthorityNid() == snomedLongAuthorityNid) {
                                role = (Long) id.getDenotation();
                                break;
                            }
                        }
                    } else {
                        logger.log(Level.INFO, "\r\n::: [OWLF] no identifier warning typeId = {0} {1} {2}",
                                new Object[]{sr.typeId, cb.getPrimUuid(), cb.getInitialText()});
                    }
                } else {
                    logger.log(Level.INFO, "\r\n::: [OWLF] error typeId = {0}", sr.typeId);
                }

                // CONCEPTID1 + RELATIONSHIPTYPE + CONCEPTID2 + RELATIONSHIPGROUP
                srlSctId.add(new SnoRelLong(c1, c2, role, sr.group));
            }
        } catch (TerminologyException ex) {
            Logger.getLogger(OwlFunctionalSyntaxExport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OwlFunctionalSyntaxExport.class.getName()).log(Level.SEVERE, null, ex);
        }

        Collections.sort(srlSctId);
    }

    public static int countOccurrences(String haystack, char needle) {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }
        return count;
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
}
