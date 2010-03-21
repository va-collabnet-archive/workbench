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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_WriteDirectToDb;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.task.classify.I_SnorocketFactory.I_Callback;
import org.dwfa.ace.task.profile.NewDefaultProfile;
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

/**
 * This class is an experiment to improve load, classify, and write performance
 * over what is provided in FasterLoad, which loads quickly, but has horrible
 * write performance.
 * <p>
 * 
 * <ol>
 * <li>Simplify the test to determine if a concept is part of the classification
 * hierarchy. The <code>isParentOfOrEqualTo</code> method is to slow. We will
 * instead check based on the following assumptions.
 * <ul>
 * <li>The is-a relationship is unique to the classification. For example, the
 * SNOMED is-a has a different concept id than the ace-auxiliary is-a
 * relationship. So every concept (except the concept root) will have at least
 * one is-a relationship of the proper type.
 * <li>There is a single root concept, and that root is part of the set of
 * included concept
 * <li>Assume that the versions are linear, independent of path, and therefore
 * the status with the latest date on an allowable path is the latest status.
 * <li>Assume that relationships to retired concepts will have a special status
 * so that retired concepts are not encountered by following current
 * relationships
 * 
 * <ul>
 * </ol>
 * These assumptions should allow determination of included concepts in linear
 * time - O(n), with a relatively small constant since they can be performed
 * with a simple integer comparison on the concept type.
 * 
 * @author kec
 * 
 */

/* !!! OBSOLETE MockSnorocket */

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class LoadClassifyWrite extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private static volatile int conceptCount = 0;
    private static volatile int classifyConceptCount = 0;
    private static volatile int relCount = 0;
    private static volatile int relsOnPathCount = 0;
    private static volatile int activeRelCount = 0;
    private static volatile int statedRelCount = 0;

    private static int multipleRelEntriesForVersion = 0;
    private static int multipleRelEntriesForVersion2 = 0;
    private static int multipleAttrEntriesForVersion = 0;

    private static int isaId = Integer.MIN_VALUE;

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

    private static class MockSnorocketFactory implements I_SnorocketFactory {

        private int conceptCount = 0;
        private int isACount = 0;
        private int relationshipCount = 0;

        synchronized public void addConcept(int conceptId, boolean fullyDefined) {
            conceptCount++;
        }

        public void setIsa(int id) {
            isACount++;
        }

        synchronized public void addRelationship(int c1, int rel, int c2, int group) {
            relationshipCount++;
        }

        public void classify() {
            System.err.println("*** " + conceptCount + "\t" + relationshipCount);
        }

        public void getResults(I_Callback callback) {
        }

        public I_SnorocketFactory createExtension() {
            // TODO Auto-generated method stub
            return null;
        }

        public InputStream getStream() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public void getEquivConcepts(I_EquivalentCallback callback) {
            // TODO Auto-generated method stub

        }

        public void addRoleComposition(int[] lhsIds, int rhsId) {
            // TODO Auto-generated method stub

        }

        public void addRoleRoot(int id, boolean inclusive) {
            // TODO Auto-generated method stub

        }

        public void addRoleNeverGrouped(int id) {
            // TODO Auto-generated method stub

        }

    }

    private class ProcessConcepts implements I_ProcessConcepts {

        final ClassifierUtil util;

        public ProcessConcepts(I_Work worker, I_SnorocketFactory rocket) throws TerminologyException, IOException {
            util = new ClassifierUtil(worker.getLogger(), rocket);
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
            conceptCount++;
            util.processConcept(concept, false); // DO NOT INCLUDE UNCOMMITTED
        }

    }

    /**
     * <b><font color=blue>ProcessResults</font></b><br>
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

    private class ProcessResults implements I_SnorocketFactory.I_Callback {
        private final Logger logger;
        private final I_TermFactory tf = LocalVersionedTerminology.get();
        private final I_WriteDirectToDb di;

        // STATISTICS
        int countReturnedRel = 0; // Size of result set from classifier
        int countNewRel = 0;
        int countWrittenRel = 0; // ALL written relationships
        int countCase1 = 0;
        int countCase1NotCurrent = 0;
        int countCase1Retired = 0;
        int countCase1NotDefining = 0;
        int countCase1NotOptRefinable = 0;
        int countCase2 = 0;
        int countCase3 = 0;
        int countCase4 = 0;
        String strCase1;

        // *** WAS ***
        // public I_GetConceptData relCharacteristic;
        // public I_GetConceptData relRefinability;
        // public I_GetConceptData relStatus;
        // final private I_ConfigAceFrame newConfig;

        // *** IS ***
        private final int isCURRENT;
        private final int isRETIRED;
        private final int isDEFINING;
        private final int isOptREFINABLE;
        private final int versionTime;
        private final int sourceUnspecifiedNid;

        private final I_ConfigAceFrame frameConfig;
        private final I_GetConceptData cInputPathObj;
        private final int cInputPathNid;
        private final I_Path cInputIPath;
        private final List<I_Position> cInputOrigins;
        private final I_GetConceptData cOutputPathObj;
        private final int cOutputPathNid;
        private final I_Path cOutputIPath;
        private final List<I_Position> cOutputOrigins;

        public void ProcessResultsWAS() throws Exception {
            // *** WAS ***
            // di = tf.getDirectInterface();

            // relCharacteristic = tf
            // .getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
            // .getUids());
            // relRefinability = tf
            // .getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
            // .getUids());
            // relStatus = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT
            // .getUids());

            // newConfig = NewDefaultProfile.newProfile("", "", "", "");
            // newConfig.getEditingPathSet().clear();
            // newConfig.addEditingPath(inferredPath); !!! @@@
        }

        public void addRelationshipWAS(int cId1, int roleId, int cId2, int group) throws Exception {
            final I_GetConceptData relSource = tf.getConcept(cId1);
            final I_GetConceptData relType = tf.getConcept(roleId);
            final I_GetConceptData relDest = tf.getConcept(cId2);
            final UUID newRelUid = UUID.randomUUID();
            // tf.newRelationship(newRelUid, relSource, relType, relDest,
            // relCharacteristic, relRefinability, relStatus, group,
            // newConfig); // @@@ NOT THREAD SAFE ?
        }

        public ProcessResults(Logger l) throws Exception {
            logger = l;
            di = tf.getDirectInterface();

            // int s = ArchitectonicAuxiliary.Concept.CURRENT.ordinal(); //???
            // 0 CURRENT, 1 RETIRED
            isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            isRETIRED = tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
            isDEFINING = tf.uuidToNative(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
            // NOT_REFINABLE | OPTIONAL_REFINABILITY | MANDATORY_REFINABILITY
            isOptREFINABLE = tf.uuidToNative(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            // Integer.MIN_VALUE | Integer.MAX_VALUE @@@ When used???
            versionTime = tf.convertToThinVersion(System.currentTimeMillis());

            // GET INPUT & OUTPUT PATHS FOR CLASSIFIER RESULTS
            frameConfig = tf.getActiveAceFrameConfig();

            cInputPathObj = frameConfig.getClassifierInputPath(); // I_GetConceptData
            cInputPathNid = cInputPathObj.getConceptId();
            cInputIPath = tf.getPath(cInputPathObj.getUids());

            cOutputPathObj = frameConfig.getClassifierOutputPath(); // I_GetConceptData
            cOutputPathNid = cOutputPathObj.getConceptId();
            cOutputIPath = tf.getPath(cOutputPathObj.getUids());

            sourceUnspecifiedNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());

            // @@@ most recent version on path or origin(s) of path
            // GET ALL ORIGIN (ANCESTOR) PATHS
            cInputOrigins = new ArrayList<I_Position>();
            cInputOrigins.add(tf.newPosition(cInputIPath, Integer.MAX_VALUE));
            addPathOrigins(cInputOrigins, cInputIPath);
            cOutputOrigins = new ArrayList<I_Position>();
            cOutputOrigins.add(tf.newPosition(cOutputIPath, Integer.MAX_VALUE));
            addPathOrigins(cOutputOrigins, cOutputIPath);

            String s = new String("\r\n::: INPUT Path ID\t" + cInputIPath.getConceptId());
            for (I_Position position : cInputOrigins) {
                s = s.concat("\r\n::: INPUT Origin Path ID\t" + position.getPath().getConceptId());
            }

            s = s.concat("\r\n::: OUTPUT Path ID\t" + cOutputIPath.getConceptId());
            for (I_Position position : cOutputOrigins) {
                s = s.concat("\r\n::: OUTPUT Origin Path ID\t" + position.getPath().getConceptId());
            }
            s = s.concat("\r\n::: VERSION TIME\t" + versionTime);
            s = s.concat("\r\n");
            s = s.concat("\r\n isCURRENT:\t" + isCURRENT);
            s = s.concat("\r\n isRETIRED:\t" + isRETIRED);
            s = s.concat("\r\n isDEFINING:\t" + isDEFINING);
            s = s.concat("\r\n isOptREFINABLE:\t" + isOptREFINABLE);
            s = s.concat("\r\n");
            logger.info(s);

            strCase1 = new String("\r\n:::\tSTATUS\tREFIN.\tCHAR.\tVTIME");
        }

        private void addPathOrigins(final List<I_Position> origins, final I_Path p) {
            origins.addAll(p.getOrigins());
            for (I_Position o : p.getOrigins()) {
                addPathOrigins(origins, o.getPath());
            }
        }

        public void addRelationship(int cId1, int roleId, int cId2, int group) {
            if (++countReturnedRel % 100000 == 0) {
                logger.info("#" + countReturnedRel + "# " + cId1 + " " + roleId + " " + cId2);
            }
            try {

                // SEARCH FOR EXISTING RELATIONSHIP
                I_RelVersioned case1Rel = null; // immutable,on path, recent
                I_RelPart case1RelPart = null; // immutable, on path, recent
                I_RelVersioned case2Rel = null; // immutable,not on path_version
                I_RelPart case2RelPart = null; // immutable, not on path_version
                I_RelVersioned case3Rel = null; // CId1, CId2 only

                // CHECK I_RelVersioned: CID1, CID2
                final I_GetConceptData relSource = tf.getConcept(cId1);
                List<I_RelVersioned> lrv = relSource.getSourceRels();
                for (I_RelVersioned rv : lrv) {
                    if (cId2 == rv.getC2Id()) {
                        case3Rel = rv;

                        // CHECK IMMUTABLE: CID1, CID2, ROLE, GROUP MATCH
                        List<I_RelPart> parts = rv.getVersions();
                        for (I_RelPart part : parts) {
                            if (part.getTypeId() == roleId && part.getGroup() == group) {
                                case2Rel = rv;
                                case2RelPart = part;

                                // CHECK: ON-PATH, VERSION IN-SCOPE
                                for (I_Position loopPos : cInputOrigins) {
                                    if (part.getPathId() == loopPos.getPath().getConceptId()
                                        && part.getVersion() <= loopPos.getVersion()) {

                                        // KEEP MOST RECENT
                                        if (case1RelPart == null) {
                                            case1Rel = rv;
                                            case1RelPart = part;
                                        } else if (part.getVersion() > case1RelPart.getVersion()) {
                                            case1Rel = rv;
                                            case1RelPart = part;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // GENERATE STATS
                if (case1RelPart != null) {
                    countCase1++;

                    if (case1RelPart.getStatusId() != isCURRENT) {
                        countCase1NotCurrent++;
                        if (countCase1 < 100) {
                            // STATUS, REFIN., CHAR., VTIME
                            Integer i1 = case1RelPart.getStatusId();
                            Integer i2 = case1RelPart.getRefinabilityId();
                            Integer i3 = case1RelPart.getCharacteristicId();
                            Integer i4 = case1RelPart.getVersion();

                            strCase1 = strCase1.concat("\r\n:::\t" + i1.toString() + "\t" + i2.toString() + "\t"
                                + i3.toString() + "\t" + i4.toString());
                        } else if (countCase1 == 100) {
                            logger.info(strCase1);
                        }
                        if (case1RelPart.getStatusId() == isRETIRED) {
                            countCase1Retired++;
                        }
                    }
                    if (case1RelPart.getCharacteristicId() != isDEFINING) {
                        countCase1NotDefining++;
                    }
                    if (case1RelPart.getRefinabilityId() != isOptREFINABLE) {
                        countCase1NotOptRefinable++;
                    }

                } else if (case2RelPart != null) {
                    countCase2++;
                } else if (case3Rel != null) {
                    countCase3++;
                } else {
                    countCase4++;
                }

                if (case1Rel != null && case1RelPart != null) {
                    // IMMUTABLE, ON PATH, MOST RECENT
                    if (case1RelPart.getStatusId() == isCURRENT) {
                        // ACTION = DO_NOTHING

                        // CHECK: REFINABILITY & CHARACTERISTIC

                    } else {
                        // ACTION = CREATE UPDATED PART
                    }

                } else if (case2RelPart != null) {
                    // IMMUTABLE, NOT ON PATH OR NOT IN VERSION TIME SCOPE
                    // EMPTY SET
                } else if (case3Rel != null && false) { // !!! DISABLED WRITES
                    // REL WITH SAME CID1, CID2
                    // "NEWLY INFERRED PART" -- generate new id and write id to

                    // GENERATE NEW REL ID -- AND WRITE TO DB
                    // @@@ Should this case create a new REL ID ???
                    Collection<UUID> rUids = new ArrayList<UUID>();
                    rUids.add(UUID.randomUUID());
                    // (Collection<UUID>, int, I_Path, int)
                    int newRelNid = di.uuidToNativeDirectWithGeneration(rUids, sourceUnspecifiedNid, cOutputIPath,
                        versionTime);

                    // CREATE RELATIONSHIP OBJECT -- IN MEMORY
                    // (int relNid, int conceptNid, int relDestinationNid)
                    I_RelVersioned newRel = di.newRelationshipBypassCommit(newRelNid, cId1, cId2);

                    // CREATE RELATIONSHIP PART W/ TermFactory-->VobdEnv
                    I_RelPart newRelPart = tf.newRelPart(); // I_RelPart
                    newRelPart.setTypeId(roleId); // from classifier
                    newRelPart.setGroup(group); // from classifier
                    newRelPart.setCharacteristicId(isDEFINING); // fixed
                    newRelPart.setRefinabilityId(isOptREFINABLE); // fixed
                    newRelPart.setStatusId(isCURRENT); // CURRENT | RETIRED
                    newRelPart.setVersion(versionTime);
                    newRelPart.setPathId(cOutputPathNid); // via preferences

                    newRel.addVersionNoRedundancyCheck(newRelPart);
                    di.writeRel(newRel); // WRITE TO DB
                    if (++countWrittenRel % 1000 == 0) {
                        logger.info("...WRITTEN REL... " + countWrittenRel);
                    }
                } else {
                    // EMPTY SET: NO CID1, CID2 PAIRS FOUND
                }

            } catch (TerminologyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } // addRelationship
    } // class ProcessResults

    public static void logMemory(String tag, I_Work worker) {
        boolean log_memory_p = true;
        if (!log_memory_p)
            return;
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        System.out.println(">>>" + "@" + st[3].getClassName() + "." + st[3].getMethodName() + ":"
            + st[3].getLineNumber() + ">>>" + tag);
        Runtime rt = Runtime.getRuntime();
        // EKM - comment this in to meter memory
        // rt.gc();
        System.out.println(">>>" + "Used memory @ " + tag + ": "
            + ((rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)));
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            logMemory("LCW evaluate start", worker);
            worker.getLogger().info("LCW start evaluate()");

            conceptCount = 0;
            classifyConceptCount = 0;
            relCount = 0;
            relsOnPathCount = 0;
            activeRelCount = 0;
            statedRelCount = 0;

            long startTime = System.currentTimeMillis();

            // :EDIT:MEC: from FasterLoad
            final I_SnorocketFactory rocket = (I_SnorocketFactory) Class.forName(
                "au.csiro.snorocket.ace.SnorocketFactory"
            // "org.dwfa.ace.task.classify.FasterLoad$MockSnorocketFactory"
            )
                .newInstance();

            // I_SnorocketFactory rocket = (I_SnorocketFactory) process
            // .readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());
            // new MockSnorocketFactory();

            final I_TermFactory tf = LocalVersionedTerminology.get();

            if (tf.getActiveAceFrameConfig().getEditingPathSet().size() != 1) {
                throw new TaskFailedException("Profile must have only one edit path. Found: "
                    + tf.getActiveAceFrameConfig().getEditingPathSet());
            }

            isaId = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
            rocket.setIsa(isaId);

            ProcessConcepts pc = new ProcessConcepts(worker, rocket);
            tf.iterateConcepts(pc);

            worker.getLogger().info("LCW time load (ms): " + (System.currentTimeMillis() - startTime));
            String pcStr = new String("\r\n Count Add Concepts:\t" + pc.util.countAddConcept);
            pcStr = pcStr.concat("\r\n Count Add Rel:\t" + pc.util.countAddRel);
            pcStr = pcStr.concat("\r\n Count Rel Status-Not-Current:\t" + pc.util.countStatusNotCurrent);
            pcStr = pcStr.concat("\r\n");
            worker.getLogger().info(pcStr);
            // logMemory("LCW load", worker);

            // logMemory("LCW pre classify", worker);
            startTime = System.currentTimeMillis();
            worker.getLogger().info("Starting classify... ");
            rocket.classify();
            worker.getLogger().info("LCW time classify (ms): " + (System.currentTimeMillis() - startTime));
            // logMemory("LCW post classify", worker);

            // :MEC:EDIT:BEGIN: @@@
            worker.getLogger().info(":MEC: GET CLASSIFIER RESULTS...");

            long startPRTime = System.currentTimeMillis();
            ProcessResults pr = new ProcessResults(worker.getLogger());
            rocket.getResults(pr);
            worker.getLogger().info("LCW time ProcessResults (ms): " + (System.currentTimeMillis() - startPRTime));

            // !!!worker.getLogger().info(":MEC: CHECKPOINT DATABASE...");
            // !!!tf.getDirectInterface().sync(); // checkpoint db changes

            String s = new String("\r\n:STAT: " + pc.util.countAddConcept + " CLASSIFIER INPUT CONCEPT COUNT\r\n");
            s = s.concat(":STAT: " + pc.util.countAddRel + " CLASSIFIER INPUT REL COUNT\r\n\r\n");
            s = s.concat(":STAT:CASE1:\t" + pr.countCase1 + "\tTOTAL\r\n");
            s = s.concat(":STAT:CASE2:\t" + pr.countCase2 + "\tTOTAL\r\n");
            s = s.concat(":STAT:CASE3:\t" + pr.countCase3 + "\tTOTAL\r\n");
            s = s.concat(":STAT:CASE4:\t" + pr.countCase4 + "\tTOTAL\r\n");
            int caseCnt = pr.countCase1 + pr.countCase2 + pr.countCase3 + pr.countCase4;
            s = s.concat(":STAT: \t" + caseCnt + " \tCASE TOTAL COUNT\r\n");
            s = s.concat(":STAT: \t" + pr.countReturnedRel + " \tCLASSIFIER OUTPUT REL COUNT\r\n");

            s = s.concat("\r\n:STAT:CASE1: \t" + pr.countCase1NotCurrent + " \tNOT CURRENT\r\n");
            s = s.concat(":STAT:CASE1: \t" + pr.countCase1NotCurrent + " \tNOT DEFINING\r\n");
            s = s.concat(":STAT:CASE1: \t" + pr.countCase1NotCurrent + " \tNOT OPT. REFINABLE\r\n");

            worker.getLogger().info(s);

            worker.getLogger().info(":STAT: " + pr.countWrittenRel + " relations written to database");
            // :MEC:EDIT:END: @@@

            process.writeAttachment(ProcessKey.SNOROCKET.getAttachmentKey(), rocket);
            logMemory("LCW evaluate end", worker);

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1) throws TaskFailedException {
        // nothing to do...
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
