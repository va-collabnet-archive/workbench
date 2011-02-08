package org.dwfa.ace.task.classify;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
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

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class TestSnoPathProcessInferred extends AbstractTask {
    private static final long serialVersionUID = 1L;

    // USER INTERFACE
    private Logger logger;
    private I_TermFactory tf;
    private I_ConfigAceFrame config = null;

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
    private static int isCh_STATED_AND_INFERRED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_STATED_AND_SUBSUMED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int sourceUnspecifiedNid;
    private static int workbenchAuxPath = Integer.MIN_VALUE;

    private static int snorocketAuthorNid = Integer.MIN_VALUE;

    private UUID uuidSourceSnomedLong = null;
    private static int snomedLongAuthorityNid = Integer.MIN_VALUE;

    // NID SETS
    I_IntSet statusSet = null;
    PositionSetReadOnly cClassPosSet = null;
    PositionSetReadOnly cEditPosSet = null;
    I_IntSet allowedRoleTypes = null;

    // INPUT PATHS
    int cEditPathNid = Integer.MIN_VALUE; // :TODO: move to logging
    PathBI cEditIPath = null;
    List<PositionBI> cEditPathPos = null; // Edit (Stated) Path I_Positions

    // OUTPUT PATHS
    int cClassPathNid; // :TODO: move to logging
    PathBI cClassIPath; // Used for write back value
    List<PositionBI> cClassPathPos; // Classifier (Inferred) Path I_Positions

    // MASTER DATA SETS
    List<SnoRel> cEditSnoRels; // "Edit Path" Concepts
    List<SnoCon> cEditSnoCons; // "Edit Path" Relationships
    List<SnoRel> cClassSnoRels; // "Classifier Path" Relationships
    List<SnoRel> cRocketSnoRels; // "Snorocket Results Set" Relationships

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // nothing to do
    }

    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        logger = worker.getLogger();
        logger.info("\r\n::: [TestSnoPathInferred] evaluate() -- begin");

        try {
            long startTime = System.currentTimeMillis();
            tf = Terms.get();
            config = tf.getActiveAceFrameConfig();
            cClassSnoRels = new ArrayList<SnoRel>();

            setupCoreNids();
            setupPaths();
            logger.info(toStringPathPos(cClassPathPos, "Classifier Path"));
            setupRoleNids();

            SnoPathProcessInferred pcClass = new SnoPathProcessInferred(logger,
                    cClassSnoRels, allowedRoleTypes, statusSet, cEditPosSet,
                    cClassPosSet, null, config
                            .getPrecedence(), config.getConflictResolutionStrategy());
            tf.iterateConcepts(pcClass);
            logger.info("\r\n::: [TestSnoPathInferred] GET INFERRED PATH DATA : "
                    + pcClass.getStats(startTime));

            dumpSnoRel(cEditSnoRels, "TestSnoPathInferred_sctIds_t" + startTime + ".txt", 6);

        } catch (Exception e) {
            e.printStackTrace();
        }

        cEditSnoRels = null;
        System.gc();

        logger.info("\r\n::: [TestSnoPathInferred] evaluate() -- completed");
        return Condition.CONTINUE;
    }

    @Override
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    private Condition setupPaths() {
        // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
        try {
            if (config.getEditingPathSet().size() != 1) {
                String errStr = "Profile must have only one edit path. Found: "
                        + config.getEditingPathSet();
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
            workbenchAuxPath = wAuxCb.getConceptNid();

            cEditPathNid = cEditPathObj.getConceptNid();
            cEditIPath = tf.getPath(cEditPathObj.getUids());
            cEditPosSet = new PositionSetReadOnly(tf.newPosition(cEditIPath, Integer.MAX_VALUE));
            // cEditPosSet = new
            // PositionSetReadOnly(cEditIPath.getOrigins().get(0));

            cEditPathPos = new ArrayList<PositionBI>();
            cEditPathPos.add(tf.newPosition(cEditIPath, Integer.MAX_VALUE));
            getPathOrigins(cEditPathPos, cEditIPath);

            // GET ALL CLASSIFER_PATH ORIGINS
            I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
            if (cClassPathObj == null) {
                String errStr = "Classifier Output (Inferred) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return Condition.STOP;
            }
            cClassPathNid = cClassPathObj.getConceptNid();
            cClassIPath = tf.getPath(cClassPathObj.getUids());
            cClassPosSet = new PositionSetReadOnly(tf.newPosition(cClassIPath, Integer.MAX_VALUE));
            // cClassPosSet = new
            // PositionSetReadOnly(cClassIPath.getOrigins().get(0));

            cClassPathPos = new ArrayList<PositionBI>();
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
                for (PositionBI p2 : p1.getPath().getOrigins())
                    if ((origins.contains(p2) == false)
                            && (p2.getPath().getConceptNid() != workbenchAuxPath)) {
                        origins.add(p2);
                        nextLevel.add(p2);
                    }
            }

            thisLevel = nextLevel;
        }
    }

    private Condition setupCoreNids() {
        I_TermFactory tf = Terms.get();

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
            isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            isLIMITED = tf.uuidToNative(ArchitectonicAuxiliary.Concept.LIMITED.getUids());
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

            snorocketAuthorNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.USER.SNOROCKET
                    .getUids());

            uuidSourceSnomedLong = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids()
                    .iterator().next();
            snomedLongAuthorityNid = tf.uuidToNative(uuidSourceSnomedLong);

        } catch (TerminologyException e) {
            e.printStackTrace();
            return Condition.STOP;
        } catch (IOException e) {
            e.printStackTrace();
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
                for (PositionBI pos : cEditPathPos) { // PATHS_IN_PRIORITY_ORDER
                    for (I_RelPart rPart : rv.getMutableParts()) {
                        if (pos.getPath().getConceptNid() == rPart.getPathId()) {
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
            sb.append(":::   \t" + toStringCNid(cNid) + "\r\n");
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

    private String toStringPathPos(List<PositionBI> pathPos, String pStr) {
        // BUILD STRING
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [SnorocketTask] PATH ID -- " + pStr + "\r\n");
        for (PositionBI position : pathPos) {
            s.append("::: ... PATH:\t"
                    + toStringCNid(position.getPath().getConceptNid()) + "\r\n");
        }
        s.append(":::");
        return s.toString();
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
                            if (c1.getUids().iterator().hasNext())
                                sb.append(c1.getUids().iterator().next() + "\t");
                            else
                                sb.append(sr.c1Id + "_INTNoNext\t");
                        } catch (Exception e) {
                            sb.append(sr.c1Id + "_INTExcept\t");
                        }
                    }

                    if (sr.typeId == Integer.MAX_VALUE) {
                        sb.append("_INTMAX\t");
                    } else {
                        try {
                            I_GetConceptData t = tf.getConcept(sr.typeId);
                            if (t.getUids().iterator().hasNext())
                                sb.append(t.getUids().iterator().next() + "\t");
                            else
                                sb.append(sr.typeId + "_INTNoNext\t");
                        } catch (Exception e) {
                            sb.append(sr.typeId + "_INTExcept\t");
                        }
                    }

                    if (sr.c2Id == Integer.MAX_VALUE) {
                        sb.append("_INTMAX\t");
                    } else {
                        try {
                            I_GetConceptData c2 = tf.getConcept(sr.c2Id);
                            if (c2.getUids().iterator().hasNext())
                                sb.append(c2.getUids().iterator().next() + "\t");
                            else
                                sb.append(sr.c2Id + "_INTNoNext\t");
                        } catch (Exception e) {
                            sb.append(sr.c2Id + "_INTExcept\t");
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

                long c1 = Long.MAX_VALUE, role = Long.MAX_VALUE, c2 = Long.MAX_VALUE;
                for (SnoRel sr : srl) {
                    index += 1;

                    //c1
                    I_GetConceptData cb = tf.getConcept(sr.c1Id);
                    if (cb != null) {
                        List<? extends I_IdVersion> idv1 = cb.getIdentifier().getIdVersions();
                        c1 = Long.MAX_VALUE;
                        for (I_IdVersion id : idv1)
                            if (id.getAuthorityNid() == snomedLongAuthorityNid) {
                                c1 = (Long) id.getDenotation();
                                break;
                            }
                    } else
                        logger.info("\r\n::: [TestSnoPathInferred] error c1Id = " + sr.c1Id);

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
                        } else
                            logger.info("\r\n::: [TestSnoPathInferred] no identifier error c2Id = "
                                    + sr.c2Id + " " + cb2.getPrimUuid() + " "
                                    + cb2.getInitialText() + " where... c1Id " + sr.c1Id + " "
                                    + cb.getPrimUuid() + " " + cb.getInitialText());
                    } else
                        logger.info("\r\n::: [TestSnoPathInferred] error c2Id = " + sr.c2Id);

                    // role
                    cb = tf.getConcept(sr.typeId);
                    if (cb != null) {
                        I_Identify idfr = cb.getIdentifier();
                        if (idfr != null) {
                            List<? extends I_IdVersion> idv3 = idfr.getIdVersions();
                            role = Long.MAX_VALUE;
                            for (I_IdVersion id : idv3)
                                if (id.getAuthorityNid() == snomedLongAuthorityNid) {
                                    role = (Long) id.getDenotation();
                                    break;
                                }
                        } else
                            logger
                                    .info("\r\n::: [TestSnoPathInferred] no identifier error typeId = "
                                            + sr.typeId
                                            + " "
                                            + cb.getPrimUuid()
                                            + " "
                                            + cb.getInitialText());
                    } else
                        logger.info("\r\n::: [TestSnoPathInferred] error typeId = " + sr.typeId);

                    // RELATIONSHIPID + CONCEPTID1 + RELATIONSHIPTYPE +
                    // CONCEPTID2
                    bw.write("\t" + c1 + "\t" + role + "\t" + c2 + "\t");
                    // CHARACTERISTICTYPE + REFINABILITY + RELATIONSHIPGROUP
                    bw.write("0\t" + "0\t" + sr.group + "\r\n");
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
