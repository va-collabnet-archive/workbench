package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
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

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class TestCPath_New extends AbstractTask {
    private static final long serialVersionUID = 1L;

    // USER INTERFACE
    private Logger logger;
    private I_TermFactory tf;
    private I_ConfigAceFrame config = null;

    // INTERNAL DATA STRUCTURES
    private ArrayList<SnoRel> cClassSnoRels;
    private PositionSetReadOnly cClassPosSet;
    private int isaNid;
    private int rootNid;
    private int rootRoleNid;
    private int isCURRENT;
    private I_IntSet statusSet;
    private I_IntSet allowedRoleTypes;
    private ArrayList<I_Position> cClassPathPos;
    private int workbenchAuxPath;
    
    // :DEBUG:
    private static boolean debugDump; 

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // nothing to do
    }

    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        debugDump = true;
        logger = worker.getLogger();
        logger.info("\r\n::: [TestCPath_New] evaluate() -- begin");

        tf = Terms.get();

        setupCoreNids();
        setupPaths();

        cClassSnoRels = new ArrayList<SnoRel>();
        // cClassSnoRels = null;
        logger.info("\r\n::: [TestCPath_New] cClassSnoRels = null;");

        try {
            setupRoleNids();
            SnoPathProcessConcepts pcClass = new SnoPathProcessConcepts(logger, null,
                    cClassSnoRels, allowedRoleTypes, statusSet, cClassPosSet, null, false, config
                            .getPrecedence(), config.getConflictResolutionStrategy());
            tf.iterateConcepts(pcClass);

            int countNull = 0;
            int countTotal = 0;
            StringBuilder sb = new StringBuilder(
                    "\r\n::: [TestCPath_New] tf.getRelationship(...) == null; ... if any exist ...");
            for (SnoRel sr : cClassSnoRels) {
                try {
                    I_RelVersioned rBean = tf.getRelationship(sr.relNid);
                    if (rBean == null) {
                        if (countNull < 20) {
                            sb.append("\r\n::: relNid=\t" + sr.relNid + " "
                                    + Terms.get().nativeToUuid(sr.relNid));
                            addToOutput("c1Nid", sb, sr.c1Id, tf.getConcept(sr.c1Id));
                            addToOutput("rtNid", sb, sr.typeId, tf.getConcept(sr.typeId));
                            addToOutput("c2Nid", sb, sr.c2Id, tf.getConcept(sr.c2Id));
                        }
                        countNull++;
                    }
                    countTotal++;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (TerminologyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            sb.append("\r\n::: ... completed limited snapshot ...");
            logger.info(sb.toString() + "\r\n::: [TestCPath_New] countNull=" + countNull
                    + " countTotal=" + countTotal);

            Comparator<SnoRel> compDump = new Comparator<SnoRel>() {
                public int compare(SnoRel o1, SnoRel o2) {
                    int thisMore = 1;
                    int thisLess = -1;
                    if (o1.c1Id > o2.c1Id) {
                        return thisMore;
                    } else if (o1.c1Id < o2.c1Id) {
                        return thisLess;
                    } else {
                        if (o1.typeId > o2.typeId) {
                            return thisMore;
                        } else if (o1.typeId < o2.typeId) {
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
                } // compare()
            };

            // 
            if (debugDump == true) {
                SnoRel.dumpToFile(cClassSnoRels, "SnoRelInferPathRead_New_full.txt", 4);                
                Collections.sort(cClassSnoRels, compDump);
                SnoRel.dumpToFile(cClassSnoRels, "SnoRelInferPathRead_New_compare.txt", 5);
            }

            cClassSnoRels = null;
            System.gc();

            logger.info("\r\n::: [TestCPath_New] evaluate() -- completed");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Condition.CONTINUE;
    }

    private void addToOutput(String label, StringBuilder sb, int nid, I_GetConceptData src)
            throws IOException {
        if (src != null)
            sb.append("\t" + label + "=\t" + nid + "\t" + src.getUids().get(0) + "\t"
                    + src.getInitialText());
        else
            sb.append("\t" + label + "=\t" + nid);
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
                        + tf.getActiveAceFrameConfig().getEditingPathSet();
                logger.info(errStr);
                return Condition.STOP;
            }

            // GET ALL EDIT_PATH ORIGINS
            I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
            if (cClassPathObj == null) {
                String errStr = "Classifier Stored Output (Inferred) Path -- not set in Classifier preferences tab!";
                logger.info(errStr);
                return Condition.STOP;
            }

            I_Path cClassIPath = tf.getPath(cClassPathObj.getUids());
            cClassPosSet = new PositionSetReadOnly(tf.newPosition(cClassIPath, Integer.MAX_VALUE));
            // cClassPosSet = new
            // PositionSetReadOnly(cClassIPath.getOrigins().get(0));

            // Setup to exclude Workbench Auxiliary on path
            UUID wAuxUuid = UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66");
            I_GetConceptData wAuxCb = tf.getConcept(wAuxUuid);
            workbenchAuxPath = wAuxCb.getConceptId();

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

    private Condition setupCoreNids() {
        I_TermFactory tf = Terms.get();

        // SETUP CORE NATIVES IDs
        try {
            config = tf.getActiveAceFrameConfig();

            // SETUP CORE NATIVES IDs
            // :TODO: isaNid & rootNid should come from preferences config
            isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
            rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());

            if (config.getClassificationRoot() != null) {
                int checkRootNid = tf.uuidToNative(config.getClassificationRoot().getUids());
                if (checkRootNid != rootNid) {
                    logger.severe("\r\n::: SERVERE ERROR rootNid MISMACTH ***");
                }
            } else {
                String errStr = "Classifier Root not set! Found: "
                        + tf.getActiveAceFrameConfig().getEditingPathSet();
                logger.info(errStr);
                return Condition.STOP;
            }

            // :PHASE_2:
            if (config.getClassificationRoleRoot() != null) {
                rootRoleNid = tf.uuidToNative(config.getClassificationRoleRoot().getUids());
            } else {
                String errStr = "Classifier Role Root not set! Found: "
                        + tf.getActiveAceFrameConfig().getEditingPathSet();
                logger.info(errStr);
                return Condition.STOP;
            }

            // 0 CURRENT, 1 RETIRED
            isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
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

    private void setupRoleNids() throws TerminologyException, IOException {
        int countRelDuplVersion = 0;
        LinkedHashSet<Integer> resultSet = new LinkedHashSet<Integer>();

        I_GetConceptData rootConcept = tf.getConcept(rootRoleNid);
        Collection<? extends I_RelVersioned> thisLevel = rootConcept.getDestRels();
        while (thisLevel.size() > 0) {
            ArrayList<I_RelVersioned> nextLevel = new ArrayList<I_RelVersioned>();
            for (I_RelVersioned rv : thisLevel) {
                I_RelPart rPart1 = null;
                for (I_Position pos : cClassPathPos) { // PATHS_IN_PRIORITY_ORDER
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

        logger.info("::: ALLOWED ROLES = " + resultSet.size());

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
    }

}