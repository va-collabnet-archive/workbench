/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.sct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.classify.SnoCon;
import org.dwfa.ace.task.classify.SnoPathProcessStated;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.ace.task.classify.SnoRelLong;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.intsdo.junit.bdb.BdbTestRunner;
import org.intsdo.junit.bdb.BdbTestRunnerConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for tracker id: artf221341
 * @author kec
 */
@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig()
public class RegressionRetrieveStatedTest {

    public RegressionRetrieveStatedTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void retrieveStatedData() throws TerminologyException, IOException, Exception {
        System.out.println(" ::: BEGIN RegressionRetrievalStated.retrieveStatedData()");
        long startTime = System.currentTimeMillis();
        tf = Terms.get();
        precedence = Precedence.PATH;
        contradictionMgr = new IdentifyAllConflictStrategy();

        cEditSnoCons = new ArrayList<SnoCon>();
        cEditSnoRels = new ArrayList<SnoRel>();

        setupCoreNids();
        setupPaths();
        System.out.println(toStringPathPos(cEditPathListPositionBI, "Primary Input Path"));
        setupRoleNids();

        SnoPathProcessStated pcEdit = null;
        pcEdit = new SnoPathProcessStated(null, cEditSnoCons, cEditSnoRels,
                allowedRoleTypes, statusSet, cEditPosSet, null, precedence,
                contradictionMgr);

        // ADD UUID TO WATCH FOR EXCEPTION
        // Corneal gluing (procedure)
        pcEdit.addWatchConcept(UUID.fromString("86603ae8-ef03-3d34-93b5-0241c555adb7"));

        tf.iterateConcepts(pcEdit);
        System.out.println("\r\n::: [TestSnoPathProcessStated] GET STATED (Edit) PATH DATA : "
                + pcEdit.getStats(startTime));

        // UNCOMMENT TO SAVE ALL RETRIEVED DATA TO A FILE
        // dumpSnoRelSctIds(cEditSnoRels, "RegressionRetrievalStated_sctIds_t" + startTime + ".txt");

        cEditSnoRels = null;
        System.out.println(" ::: END RegressionRetrievalStated.retrieveStatedData()");
    }

    private void setupPaths() throws Exception {
        // a stable relationship: 'Is a' - 'Is a' - 'Linkage concept'
        RelationshipChronicleBI rel =
                (RelationshipChronicleBI) Ts.get().getComponent(UUID.fromString("e2ee7bba-9219-52e5-8a74-9cd73b427d7e"));

        PathBI snomedPath = Ts.get().getPath(rel.getPrimordialVersion().getPathNid());
        // PositionBI latestOnSnomedPath = new Position(Long.MAX_VALUE, snomedPath);

        // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
        // Setup to exclude Workbench Auxiliary on path
        UUID wAuxUuid = UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66");
        I_GetConceptData wAuxCb = tf.getConcept(wAuxUuid);
        workbenchAuxPath = wAuxCb.getConceptNid();

        cEditPathBI = snomedPath;
        cEditPathNid = cEditPathBI.getConceptNid();
        cEditPosSet = new PositionSetReadOnly(tf.newPosition(cEditPathBI, Integer.MAX_VALUE));

        cEditPathListPositionBI = new ArrayList<PositionBI>();
        cEditPathListPositionBI.add(tf.newPosition(cEditPathBI, Integer.MAX_VALUE));
        getPathOrigins(cEditPathListPositionBI, cEditPathBI);

        cViewPathBI = snomedPath;
        cViewPathNid = cViewPathBI.getConceptNid();
        cViewPosSet = new PositionSetReadOnly(tf.newPosition(cViewPathBI, Integer.MAX_VALUE));

        cViewPathListPositionBI = new ArrayList<PositionBI>();
        cViewPathListPositionBI.add(tf.newPosition(cViewPathBI, Integer.MAX_VALUE));
        getPathOrigins(cViewPathListPositionBI, cViewPathBI);
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

    private void setupCoreNids() throws TerminologyException, IOException {
        // SETUP CORE NATIVES IDs
        isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
        // rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());

        // :!!!: rootRoleNid = tf.uuidToNative(config.getClassificationRoleRoot().getUids());

        // 0 CURRENT, 1 RETIRED
        isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
        isLIMITED = tf.uuidToNative(ArchitectonicAuxiliary.Concept.LIMITED.getUids());
        isRETIRED = tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
        isOPTIONAL_REFINABILITY = tf.uuidToNative(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
        isNOT_REFINABLE = tf.uuidToNative(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
        isMANDATORY_REFINABILITY = tf.uuidToNative(ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.getUids());
        isCh_STATED_RELATIONSHIP = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
        isCh_DEFINING_CHARACTERISTIC = tf.uuidToNative(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
        isCh_STATED_AND_INFERRED_RELATIONSHIP = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP.getUids());
        isCh_STATED_AND_SUBSUMED_RELATIONSHIP = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_SUBSUMED_RELATIONSHIP.getUids());
        sourceUnspecifiedNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());

        snorocketAuthorNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.USER.SNOROCKET.getUids());

        uuidSourceSnomedLong = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next();
        snomedLongAuthorityNid = tf.uuidToNative(uuidSourceSnomedLong);

        statusSet = tf.newIntSet();
        statusSet.add(isCURRENT);
        statusSet.add(isLIMITED);
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
            sb.append(":::   \t" + toStringCNid(cNid) + "\r\n");
        }
        System.out.println(sb.toString());

        allowedRoleTypes = tf.newIntSet();
        allowedRoleTypes.addAll(resultInt);
        return resultInt;
    }

    private String toStringCNid(int cNid) throws TerminologyException, IOException {
        StringBuilder sb = new StringBuilder();
        I_GetConceptData c = tf.getConcept(cNid);
        sb.append(c.getUids().iterator().next() + "\t");
        sb.append(cNid + "\t");
        sb.append(c.getInitialText());
        return sb.toString();
    }

    private String toStringPathPos(List<PositionBI> pathPos, String pStr) throws TerminologyException, IOException {
        // BUILD STRING
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: PATH ID -- " + pStr + "\r\n");
        for (PositionBI position : pathPos) {
            s.append("::: ... PATH:\t"
                    + toStringCNid(position.getPath().getConceptNid()) + "\r\n");
        }
        s.append(":::");
        return s.toString();
    }

    private void dumpSnoRelSctIds(List<SnoRel> srl, String fName) throws TerminologyException, IOException {
        List<SnoRelLong> srlSctId = new ArrayList<SnoRelLong>();

        // Convert native id to sct id
        long c1 = Long.MAX_VALUE, role = Long.MAX_VALUE, c2 = Long.MAX_VALUE;
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
                System.out.println("\r\n::: error c1Id = " + sr.c1Id);
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
                    System.out.println("\r\n::: no identifier error c2Id = "
                            + sr.c2Id + " " + cb2.getPrimUuid() + " "
                            + cb2.getInitialText() + " where... c1Id " + sr.c1Id + " "
                            + cb.getPrimUuid() + " " + cb.getInitialText());
                }
            } else {
                System.out.println("\r\n::: error c2Id = " + sr.c2Id);
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
                    System.out.println("\r\n::: no identifier error typeId = "
                            + sr.typeId
                            + " "
                            + cb.getPrimUuid()
                            + " "
                            + cb.getInitialText());
                }
            } else {
                System.out.println("\r\n::: error typeId = " + sr.typeId);
            }

            // CONCEPTID1 + RELATIONSHIPTYPE + CONCEPTID2 + RELATIONSHIPGROUP
            srlSctId.add(new SnoRelLong(c1, c2, role, sr.group));
        }

        Collections.sort(srlSctId);
        SnoRelLong.dumpToFile(srlSctId, fName);
    }
    // ENVIRONMENT
    private I_TermFactory tf;
    // private I_ConfigAceFrame config = null;
    private Precedence precedence;
    private I_ManageContradiction contradictionMgr;
    // CORE CONSTANTS
    private static int isaNid = Integer.MIN_VALUE;
    //private static int rootNid = Integer.MIN_VALUE;
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
}
