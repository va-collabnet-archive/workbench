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
package org.kp.epic.edg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.classify.SnoCon;
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

@BeanList(specs = { @Spec(directory = "tasks/kp/edg", type = BeanType.TASK_BEAN) })
public class AutoGenEDGRefset extends AbstractTask implements ActionListener {
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    // CORE CONSTANTS
    private static int isaNid = Integer.MIN_VALUE;

    private static int nidCURRENT = Integer.MIN_VALUE;
    private static int nidRETIRED = Integer.MIN_VALUE;
    private static int workbenchAuxPath = Integer.MIN_VALUE;

    private static int nidVersion = Integer.MAX_VALUE;

    // UUIDs
    private UUID uuidICD9CMGroups;
    private UUID uuidICD9CodeMappings;

    private UUID uuidEDGClinicalItem_2_National;
    private UUID uuidEDCItem_100;
    private UUID uuidEDCItem_110;

    private UUID uuidEDGClinicalItem_100;
    private UUID uuidEDGClinicalItem_200;
    private UUID uuidEDGClinicalItem_2000;
    private UUID uuidEDGClinicalItem_207;
    private UUID uuidEDGClinicalItem_40;
    private UUID uuidEDGClinicalItem_50;
    private UUID uuidEDGClinicalItem_7000;
    private UUID uuidEDGClinicalItem_7010;
    private UUID uuidEDGClinicalItem_80;
    private UUID uuidEDGClinicalItem_91;

    private UUID uuidEDGClinicalDot1;
    private UUID uuidPatientFriendly;

    private UUID uuidTypeBoolean;
    private UUID uuidTypeConcept;
    private UUID uuidTypeString;
    private UUID uuidTypeInt;

    private UUID uuidSnoConClinicalFinding;
    private UUID uuidSnoConEvent;
    private UUID uuidSnoConSituation;

    // NIDs
    private int nidICD9CMGroups;
    private int nidICD9CodeMappings;
    private int nidIDC9Id;

    private int nidEDGClinicalItem_2_National;
    private int nidsEDGClinicalItem_2[];

    private int nidEDCItem_100;
    private int nidEDCItem_110;

    private int nidEDGClinicalItem_100;
    private int nidEDGClinicalItem_200;
    private int nidEDGClinicalItem_2000;
    private int nidEDGClinicalItem_207;
    private int nidEDGClinicalItem_40;
    private int nidEDGClinicalItem_50;
    private int nidEDGClinicalItem_7000;
    private int nidEDGClinicalItem_7010;
    private int nidEDGClinicalItem_80;
    private int nidEDGClinicalItem_91;

    private int nidEDGClinicalDot1;
    private int nidPatientFriendly;

    private int nidTypeBoolean;
    private int nidTypeConcept;
    private int nidTypeString;
    private int nidTypeInt;

    private int nidUnspecifiedUuid;

    private int nidSnoConClinicalFinding;
    private int nidSnoConEvent;
    private int nidSnoConSituation;

    // MASTER DATA SETS
    List<SnoCon> editSnoCons; // "Edit Path" Concepts
    private Icd9CmGroups icd9Groups;
    private Item40CodeGen item40Generator;

    // INPUT PATHS
    int nidEditPath = Integer.MIN_VALUE; // :TODO: move to logging
    I_Path editIPath = null;
    List<I_Position> editPathPos = null; // Edit (Stated) Path I_Positions

    // INTERFACE
    private Logger logger;
    I_TermFactory tf = null;
    I_ConfigAceFrame config = null;
    private boolean continueThisAction = true;
    private boolean queryConceptViaRefset;

    // :LOG: For detailed logging.
    private boolean debug;
    private boolean useLogFile;
    private boolean verboseLogFile;
    BufferedWriter logFile;
    int logCount;

    @Override
    public void actionPerformed(ActionEvent e) {
        continueThisAction = false;
    }

    private class Item40CodeGen {
        // SORT ORDER: '2.', '20', '2A'
        private LinkedList<String> item2000List;
        private int item200Count;

        public Item40CodeGen() {
            super();
            item2000List = new LinkedList<String>();
            item200Count = 200000;
        }

        public void add(String s) {
            try {
                int i = Integer.parseInt(s);
                if (i > 200000) {
                    if (i > item200Count)
                        item200Count = i; // Update maximum.
                } else {
                    item2000List.add(s);
                }
            } catch (NumberFormatException e) {
                item2000List.add(s);
            }
        }

        public void sort() {
            Collections.sort(item2000List);
        }

        public int new200() {
            return ++item200Count;
        }

        /**
         * Given string without a postfix:<br>
         * 1. Find last postfix.
         * 2. Increment postfix.
         * 3. Add last postfix to master list.
         * 
         * @param s
         * @return
         */
        public String new2000(String s) {

            // Find starting insertion point.
            int idx = Collections.binarySearch(item2000List, s + "A");
            if (idx < 0) {
                idx = -idx - 1; // insert_index = -idx - 1
            }

            String currentStr = null;
            int max = item2000List.size();
            if (idx >= max) {
                // WOULD BE LAST ELEMENT IN ARRAY
                currentStr = computePostfixIncr(s);
                item2000List.add(currentStr);

            } else {
                if (s.equals(computeBase(item2000List.get(idx))) == false) {
                    // ICD9 Code not yet on list.
                    currentStr = s;
                } else {
                    int firstLength = s.length() + 1;
                    int prevLength = firstLength;
                    boolean prevZ = false;
                    while (idx < max && s.equals(computeBase(item2000List.get(idx)))) {
                        currentStr = item2000List.get(idx);

                        // Handle A, AA, ..., AZ, B, BA, BB, sort order.
                        if (currentStr.length() == firstLength && prevLength == firstLength
                            && currentStr.endsWith("Z")) {
                            // At end of single letters A, ..., Z
                            idx = idx - 24;
                            break;

                        } else if (currentStr.length() == prevLength - 1 && prevZ == false) {
                            // At double letter boundary, for example: A,
                            // AA, AB, AC, B
                            currentStr = item2000List.get(idx - 1);
                            break;

                        } else if (currentStr.length() == prevLength - 1 && prevZ == true) {
                            // At double letter Z boundary, for example:
                            // ...AY, AZ, B
                            prevLength = currentStr.length();
                            prevZ = currentStr.endsWith("Z");
                            idx++;

                            if (idx < max) {
                                // CASE: .. AY, AZ, B, C ..
                                String nextStr = item2000List.get(idx);
                                String nextStrBase = computeBase(item2000List.get(idx));
                                if (s.equalsIgnoreCase(nextStrBase)) {
                                    if (nextStr.length() == firstLength) {
                                        // take previous "AZ"
                                        currentStr = item2000List.get(idx - 2);
                                        break;
                                    }
                                } else {
                                    // YZ, Z
                                    currentStr = item2000List.get(idx - 2);
                                    break;
                                }
                            }

                        } else {
                            prevLength = currentStr.length();
                            prevZ = currentStr.endsWith("Z");
                            idx++;
                        }
                    }
                }
                currentStr = computePostfixIncr(currentStr);
                if (currentStr == null)
                    return null;
                // ADD TO MASTER LIST
                if (idx < max)
                    item2000List.add(idx, currentStr);
                else
                    item2000List.add(currentStr);
            }
            return currentStr;
        }

        private String computeBase(String s) {
            String base = null;
            char[] dst = new char[2];
            // SPLIT THE BASE_STRING & POSTFIX_STRING
            int length = s.length();
            if (length <= 0) {
                return null;
            } else if (length == 1) {
                if (Character.isDigit(s.charAt(0))) {
                    base = s;
                } else {
                    logger.info("::: ERROR: SINGLE CHAR, NON-DIGIT: CASE NOT HANDLED");
                    return null;
                }
            } else {
                int srcBegin = length - 2;
                int srcEnd = length;
                int dstBegin = 0;
                s.getChars(srcBegin, srcEnd, dst, dstBegin);
                if (Character.isDigit(dst[0]) == false && Character.isDigit(dst[1]) == false) {
                    base = s.substring(0, length - 2);
                } else if (Character.isDigit(dst[0]) == true && Character.isDigit(dst[1]) == false) {
                    base = s.substring(0, length - 1);
                } else {
                    base = s; // no postfix
                }
            }
            return base;
        }

        /**
         * Increments the string postfix A-Z, AA-ZZ.
         * 
         * @param s
         * @return
         */
        private String computePostfixIncr(String s) {
            String base = null;
            int numPostfixDigits = 0;
            char[] dst = new char[2];

            // SPLIT THE BASE_STRING & POSTFIX_STRING
            int length = s.length();
            if (length <= 0) {
                return null;
            } else if (length == 1) {
                if (Character.isDigit(s.charAt(0))) {
                    base = s;
                    numPostfixDigits = 0;
                } else {
                    logger.info(":::ERROR: SINGLE CHAR, NON-DIGIT: CASE NOT HANDLED");
                    return null;
                }
            } else {
                int srcBegin = length - 2;
                int srcEnd = length;
                int dstBegin = 0;
                s.getChars(srcBegin, srcEnd, dst, dstBegin);
                if (Character.isDigit(dst[0]) == false && Character.isDigit(dst[1]) == false) {
                    numPostfixDigits = 2;
                    base = s.substring(0, length - 2);
                } else if (Character.isDigit(dst[0]) == true && Character.isDigit(dst[1]) == false) {
                    numPostfixDigits = 1;
                    base = s.substring(0, length - 1);
                } else {
                    base = s; // no postfix
                }
            }

            // GET NEXT POSTFIX_STRING VALUE
            String postfix = null;
            switch (numPostfixDigits) {
            case 2:
                dst[1]++;
                if (dst[1] <= 'Z') {
                    postfix = new String(dst);
                } else {
                    dst[1] = 'A';
                    dst[0]++;
                    if (dst[0] <= 'Z') {
                        postfix = new String(dst);
                    } else { // !ERROR -- MAX 'ZZ' reached
                        logger.info("\r\n:::ERROR: MAX ZZ -- CASE NOT HANDLED");
                    }
                }
                break;

            case 1:
                dst[1]++;
                if (dst[1] <= 'Z') {
                    // String(char value[], int offset, int count)
                    postfix = new String(dst, 1, 1);
                } else {
                    postfix = new String("AA");
                }
                break;

            case 0:
                postfix = new String("A");
                break;

            default:
                break;
            }

            logger.info("\r\n:::in: " + s + " out: " + base + postfix + " base: " + base
                + " postfix: " + postfix);
            if (postfix == null)
                return null;
            else
                return base + postfix;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("Item_200_Count: " + item200Count + "\r\n");
            for (String s : item2000List)
                sb.append(s + "\r\n");

            return sb.toString();
        }
    }

    private class Icd9CmGroups {
        private class Icd9CmGroupRange implements Comparable<Icd9CmGroupRange> {
            String min;
            String max;
            String dot1;

            public Icd9CmGroupRange(String min, String max, String dot1) {
                super();
                this.min = min;
                this.max = max;
                this.dot1 = dot1;
            }

            @Override
            public int compareTo(Icd9CmGroupRange other) {
                return this.min.compareToIgnoreCase(other.min);
            }
        }

        ArrayList<Icd9CmGroupRange> groupRanges;

        public Icd9CmGroups() {
            super();
            this.groupRanges = new ArrayList<Icd9CmGroupRange>();
            try {
                setupICD9CMGroups();
            } catch (TerminologyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public String lookupDot1(String icd9Code) {
            String result = null;
            boolean found = false;
            // :!!!:@@@: should range check more numeric based?

            for (Icd9CmGroupRange gr : groupRanges) {
                boolean testMin = false;
                boolean testMax = false;
                if (gr.min.compareToIgnoreCase(icd9Code) <= 0)
                    testMin = true;
                if (gr.max.compareToIgnoreCase(icd9Code) >= 0)
                    testMax = true;

                if (testMin && testMax) {
                    result = gr.dot1;
                    if (found)
                        logger.info("\r\n::: [AutoGenEDGRefset] DUPLICATE lookupDot1(" + icd9Code
                            + ") = " + gr.dot1);

                    found = true;
                }

            }
            if (!found)
                result = new String("66");

            return result; // !!!
        }

        private String findId_Dot1(I_GetConceptData concept) throws Exception {
            String ret = null;
            int lastVersion = Integer.MIN_VALUE;

            List<? extends I_IdPart> idList = concept.getIdentifier().getMutableIdParts();
            for (I_IdPart part : idList) {
                if (part.getAuthorityNid() == nidEDGClinicalDot1) {
                    if (part.getVersion() >= lastVersion) {
                        ret = part.getDenotation().toString();
                        lastVersion = part.getVersion();
                    }
                }
            }
            return ret;
        }

        private String findLastPart(I_ThinExtByRefVersioned ext) {
            I_ThinExtByRefPart lastPart = null;
            int lastVersion = Integer.MIN_VALUE;

            // Get all version parts of each extension
            List<? extends I_ThinExtByRefPart> vList = ext.getMutableParts();
            for (I_ThinExtByRefPart v : vList) {
                if (v.getVersion() > lastVersion) {
                    lastPart = v;
                    lastVersion = v.getVersion();
                }
            }

            if (lastPart == null)
                return null;

            try {
                I_ThinExtByRefPartString castPart = (I_ThinExtByRefPartString) lastPart;
                return castPart.getStringValue();
            } catch (Exception e) {
                return null;
            }
        }

        private void setupICD9CMGroups() throws TerminologyException, ParseException, IOException {
            List<I_GetConceptData> childList = new ArrayList<I_GetConceptData>();
            StringBuilder sb = new StringBuilder();

            // Get the children Concepts for ICD9-CM Groups Vol 1
            int icd9GroupParentId = tf.uuidToNative(uuidICD9CMGroups);
            I_GetConceptData parent = tf.getConcept(icd9GroupParentId);
            List<? extends I_RelVersioned> childRelList = parent.getDestRels();
            for (I_RelVersioned childRel : childRelList) {

                // :NYI: does not check for status of most current version
                // :@@@: treat all child refsets as current for KP Pilot
                // :@@@: does not search below the first level children
                int childNid = childRel.getC1Id();
                I_GetConceptData childCB = tf.getConcept(childNid);
                childList.add(childCB);

                sb.append("\r\n::: " + childCB.getInitialText());
            }
            logger.info("\r\n::: [AutoGenEDGRefset] ICD9CMGroups" + sb.toString());

            for (I_GetConceptData cb : childList) {
                // "id source" "EDC Dot1" !!!
                String dot1 = null;
                try {
                    dot1 = findId_Dot1(cb);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // Get all concept extensions.
                List<? extends I_ThinExtByRefVersioned> extList = cb.getExtensions();
                String partMin = null;
                String partMax = null;
                for (I_ThinExtByRefVersioned ext : extList) {
                    if (ext.getRefsetId() == nidEDCItem_100)
                        partMin = findLastPart(ext);
                    else if (ext.getRefsetId() == nidEDCItem_110)
                        partMax = findLastPart(ext);
                }

                if (partMin != null && partMax != null && dot1 != null) {
                    groupRanges.add(new Icd9CmGroupRange(partMin, partMax, dot1));
                    logger.info("\r\n::: [AutoGenEDGRefset] ICD9 Range: " + cb.getInitialText()
                        + ", " + partMin + ", " + partMax + ", " + dot1);
                }
            }

            // Sort the ranges collection
            Collections.sort(groupRanges);

            // Log results
            sb = new StringBuilder("\r\n::: [AutoGenEDGRefset] Dot1 Ranges");
            for (Icd9CmGroupRange gr : groupRanges)
                sb.append("\r\n::: \t" + gr.min + "\t" + gr.max + "\tdot1:\t" + gr.dot1);
            sb.append("\r\n");

            return;
        }
    }

    private class FoundMember {
        I_ThinExtByRefVersioned ext = null;
        I_ThinExtByRefPart extPart = null;

        public FoundMember(I_ThinExtByRefVersioned extResult, I_ThinExtByRefPart extPartResult) {
            super();
            this.ext = extResult;
            this.extPart = extPartResult;
        }

        public I_ThinExtByRefVersioned getExt() {
            return ext;
        }

        public void setExt(I_ThinExtByRefVersioned extResult) {
            this.ext = extResult;
        }

        public I_ThinExtByRefPart getExtPart() {
            return extPart;
        }

        public void setExtPart(I_ThinExtByRefPart extPartResult) {
            this.extPart = extPartResult;
        }
    }

    private class FoundDescExtPair {
        I_DescriptionVersioned desc;
        I_ThinExtByRefVersioned ext;

        public FoundDescExtPair(I_DescriptionVersioned desc, I_ThinExtByRefVersioned ext) {
            super();
            this.desc = desc;
            this.ext = ext;
        }

        public I_DescriptionVersioned getDesc() {
            return desc;
        }

        public void setDesc(I_DescriptionVersioned desc) {
            this.desc = desc;
        }

        public I_ThinExtByRefVersioned getExt() {
            return ext;
        }

        public void setExt(I_ThinExtByRefVersioned ext) {
            this.ext = ext;
        }
    }

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // nothing to do.
    }

    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        debug = true;
        useLogFile = true;
        verboseLogFile = true;
        logCount = 0;

        tf = LocalVersionedTerminology.get();
        logger = worker.getLogger();
        logger.info("\r\n::: [AutoGenEDGRefset] evaluate() -- begin");

        if (useLogFile) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
                String dateStr = formatter.format(new Date());
                FileWriter fw = new FileWriter("AutogenEDGReport-" + dateStr + ".txt");
                logFile = new BufferedWriter(fw);
            } catch (IOException e) {
                useLogFile = false;
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        setupUUIDs();
        if (setupCoreNids().equals(Condition.STOP))
            return Condition.STOP;
        logger.info(toStringNids());

        // GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
        if (setupPaths().equals(Condition.STOP))
            return Condition.STOP;
        logger.info(toStringPathPos(editPathPos, "Edit Path"));

        // 1. Setup ICD9-CM Groups: min, max, dot1
        icd9Groups = new Icd9CmGroups();

        // 2. GET CONCEPTS with Type 2 Refset Members
        queryConceptViaRefset = false;
        if (queryConceptViaRefset) {
            try {
                editSnoCons = findConceptsFromRefSet();
            } catch (TerminologyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            editSnoCons = new ArrayList<SnoCon>();
            long startTime = System.currentTimeMillis();
            try {
                AutoGenPathProcess pc = new AutoGenPathProcess(logger, editSnoCons, null,
                    editPathPos, nidsEDGClinicalItem_2);
                tf.iterateConcepts(pc);
                logger.info("\r\n::: [AutoGenEDGRefset] GET EDIT PATH DATA"
                    + pc.toStringStats(startTime));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // 3. DETERMINE ITEM40 Codes in use.
        item40Generator = new Item40CodeGen();
        doEDGClinicalItem_40_Setup(editSnoCons);
        logger.info("\r\n::: [AutoGenEDGRefset] INITIAL ITEM 40 \r\n" + item40Generator.toString());

        // 4. PERFORM DATA CHECKS ON CONCEPTS !!!
        checkData(editSnoCons);

        // 5. AUTO GENERATE REFSET MEMBERS
        createEDGClinicalItems(editSnoCons);

        if (useLogFile) {
            try {
                logFile.flush();
                logFile.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return Condition.CONTINUE;
    }

    @Override
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * CHECK: at least one IDC9 must be present<br>
     * AFFECTS: 80, 200, 2000, 40, 91<br>
     * <br>
     * CHECK: "Patient Friendly Display Name" is missing<br>
     * AFFECTS: 7010, 7000<br>
     * <br>
     * CHECK: 7000 is missing when 7010 is present<br>
     * AFFECTS: 7000<br>
     * <br>
     * CHECK: either EDC_Item_100 or EDC_Item_120 missing<br>
     * AFFECTS: 80<br>
     * 
     * @param conceptList
     */
    private void checkData(List<SnoCon> conceptList) {
        // AutoGenPathProcess()

    }

    private void createEDGClinicalItems(List<SnoCon> scl) {
        for (SnoCon concept : scl) {
            /**** Handle ICD9 Codes ****/
            // Get the ICD9 Code Mapping values for this concept.
            ArrayList<String> icd9CodeValues = findICD9Values(concept.id);

            // Process the Type 2 descriptions for this process
            try {
                String friendlyName = findDescrFriendly(concept.id);

                if (useLogFile) {
                    if (icd9CodeValues.size() < 1) {
                        logFile.write((logCount++) + "\t-WARN-\tICD9 MAP NO!\t"
                            + toLogStr(concept.id) + "\r\n");
                    } else if (verboseLogFile) {
                        logFile.write((logCount++) + "\t--OK--\tICD9 MAP [" + icd9CodeValues.size()
                            + "]\t" + toLogStr(concept.id) + "\r\n");
                    }
                    if (friendlyName == null) {
                        logFile.write((logCount++) + "\t-WARN-\tP.F.Name NO!\t"
                            + toLogStr(concept.id) + "\r\n");
                    } else if (verboseLogFile) {
                        logFile.write((logCount++) + "\t--OK--\tP.F.Name YES\t"
                            + toLogStr(concept.id) + "\r\n");
                    }
                }

                List<I_DescriptionVersioned> descList = findDescription_Type2(concept.id);
                for (I_DescriptionVersioned desc : descList) {
                    int dNid = desc.getNid();

                    // Description restated
                    doEDGClinicalItem_100(desc);

                    // Defaults to "Clinically active"
                    doEDGClinicalItem_207(dNid);

                    // Patient Friendly Display Name
                    doEDGClinicalItem_7010(desc, friendlyName);

                    // Flag ICD9 Code presence (i.e. Item 200 or 2000 present)
                    doEDGClinicalItem_91(dNid, icd9CodeValues);

                    // Handles Items 200, 2000
                    doEDGClinicalItem_200x(dNid, icd9CodeValues);

                    // Fill in EDC Dot1 which contains ICD9 Code
                    doEDGClinicalItem_80(desc, icd9CodeValues);

                    // Create unique Item 200/2000 instance
                    // Clinical Item 40 is nested in Item 200/2000 method
                }
            } catch (TerminologyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    /************************
     * EDGClinicalItem_100<br>
     * TYPE: String<br>
     * USE: description restated<br>
     * 
     * @param concept
     */
    private void doEDGClinicalItem_100(I_DescriptionVersioned desc) {
        try {
            I_DescriptionPart part = lastVersionDesc(desc);

            // Assure that description text is current.
            FoundMember oldMember = memberFind(desc.getNid(), nidEDGClinicalItem_100);
            if (oldMember == null && part != null)
                memberCreate_String(nidEDGClinicalItem_100, desc.getNid(), part.getText());
            else if (oldMember != null && part != null)
                memberUpdate_String(oldMember.getExt(), oldMember.getExtPart(), part.getText());
            else if (oldMember != null && part == null)
                ; // DO NOTHING

        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /************************
     * EDGClinicalItem_200<br>
     * TYPE: String<br>
     * USE: for multiple ICD-9 codes<br>
     * NOTE: to be no longer used by 2013 or sooner<br>
     * <br>
     * <code>
     * IF (.MULTIPLE. ICD9_Code_Mapping .EXIST.)<br>
     * THEN<br>
     * &nbsp;&nbsp;CREATE EDGClinicalItem_200 refset_member<br>
     * &nbsp;&nbsp;CATENATE all ICD9_Code_Mapping external ICD9_ID with '/'<br>
     * &nbsp;&nbsp;SET refset_member String to resulting catenation<br></code> <br>
     * EDGClinicalItem_2000<br>
     * TYPE: String<br>
     * USE: for single ICD-9 code<br>
     * NOTE: same value as EDG_Billing_Item_40.<br>
     * links clinical record to billing record.<br>
     * <br>
     * <code>
     * IF (.ONLY.ONE. ICD9_Code_Mapping .EXISTS.)<br>
     * THEN<br>
     * &nbsp;&nbsp;CREATE EDGClinicalItem_200 refset_member<br>
     * &nbsp;&nbsp;SET refset_member String to ICD9_Code_Mapping external ICD9_ID<br></code>
     * 
     * @param nidConcept
     * @param icd9Codes
     */
    private void doEDGClinicalItem_200x(int nidConcept, List<String> icd9Codes) {
        try {
            // Check for existing Item 200 or Item 2000 RefSet member.
            List<I_ThinExtByRefVersioned> extList = tf.getAllExtensionsForComponent(nidConcept);

            I_ThinExtByRefVersioned ext200 = null;
            I_ThinExtByRefPart ext200part = null;
            I_ThinExtByRefVersioned ext2000 = null;
            I_ThinExtByRefPart ext2000part = null;
            for (I_ThinExtByRefVersioned ext : extList) {
                if (ext.getRefsetId() == nidEDGClinicalItem_200) {
                    if (ext200 == null) {
                        ext200 = ext;
                        List<? extends I_ThinExtByRefPart> partList = ext.getMutableParts();
                        int lastVersion = Integer.MIN_VALUE;
                        for (I_ThinExtByRefPart part : partList) {
                            if (part.getVersion() > lastVersion) {
                                lastVersion = part.getVersion();
                                ext200part = part;
                            }
                        }
                    } else
                        logger.info("\r\n::: !!! DUPLICATE ITEM 200 ERROR");
                } else if (ext.getRefsetId() == nidEDGClinicalItem_2000) {
                    if (ext2000 == null) {
                        ext2000 = ext;
                        List<? extends I_ThinExtByRefPart> partList = ext.getMutableParts();
                        int lastVersion = Integer.MIN_VALUE;
                        for (I_ThinExtByRefPart part : partList) {
                            if (part.getVersion() > lastVersion) {
                                lastVersion = part.getVersion();
                                ext2000part = part;
                            }
                        }
                    } else
                        logger.info("\r\n::: !!! DUPLICATE ITEM 2000 ERROR");
                }
            }

            // Create and/or Update extensions based on existing extensions.
            int totalCodes = icd9Codes.size();
            String icd9CodeStr = null;
            if (totalCodes > 1) {
                Collections.sort(icd9Codes);
                StringBuilder sb = new StringBuilder(icd9Codes.get(0));
                for (int i = 1; i < totalCodes; i++)
                    sb.append("/" + icd9Codes.get(i));
                icd9CodeStr = sb.toString();
            } else if (totalCodes == 1) {
                icd9CodeStr = icd9Codes.get(0);
            } else if (totalCodes == 0)
                icd9CodeStr = "";

            boolean item200 = true;
            boolean item2000 = false;
            boolean updated = false;
            if (totalCodes == 1) {
                if (ext200part == null && ext2000part == null) {
                    // ADD NEW ITEM 2000 REFSET MEMBER
                    memberCreate_String(nidEDGClinicalItem_2000, nidConcept, icd9CodeStr);
                    doEDGClinicalItem_40(nidConcept, icd9CodeStr, item2000);
                } else if (ext200part != null && ext2000part == null) {
                    // RETIRE ITEM 200.
                    memberRetire_String(ext200, ext200part);
                    // CREATE NEW ITEM 2000 MEMBER
                    memberCreate_String(nidEDGClinicalItem_2000, nidConcept, icd9CodeStr);
                    // CHANGE Item 40 to be base on Item 2000
                    doEDGClinicalItem_40(nidConcept, icd9CodeStr, item2000);
                } else if (ext200part == null && ext2000part != null) {
                    // IF CHANGED, UPDATE ITEM 2000 MEMBER
                    updated = memberUpdate_String(ext2000, ext2000part, icd9CodeStr);
                    if (updated)
                        doEDGClinicalItem_40(nidConcept, icd9CodeStr, item2000);
                } else if (ext200part != null && ext2000part != null) {
                    // ERROR
                    // RETIRE ITEM 200.
                    memberRetire_String(ext200, ext200part);
                    // IF CHANGED, UPDATE ITEM 2000 MEMBER
                    updated = memberUpdate_String(ext2000, ext2000part, icd9CodeStr);
                    if (updated)
                        doEDGClinicalItem_40(nidConcept, icd9CodeStr, item2000);
                }
            } else if (totalCodes > 1) {
                if (ext200part == null && ext2000part == null) {
                    // ADD NEW ITEM 200 REFSET MEMBER
                    memberCreate_String(nidEDGClinicalItem_200, nidConcept, icd9CodeStr);
                    doEDGClinicalItem_40(nidConcept, icd9CodeStr, item200);
                } else if (ext200part == null && ext2000part != null) {
                    // CREATE NEW ITEM 200 MEMBER
                    memberCreate_String(nidEDGClinicalItem_200, nidConcept, icd9CodeStr);
                    // RETIRE ITEM 2000
                    memberRetire_String(ext2000, ext2000part);
                    // CHANGE Item 40 to be base on Item 200
                    doEDGClinicalItem_40(nidConcept, icd9CodeStr, item200);
                } else if (ext200part != null && ext2000part == null) {
                    // IF CHANGED, UPDATE ITEM 200 MEMBER
                    updated = memberUpdate_String(ext200, ext200part, icd9CodeStr);
                    if (updated)
                        doEDGClinicalItem_40(nidConcept, icd9CodeStr, item200);
                } else if (ext200part != null && ext2000part != null) {
                    // ERROR
                    // RETIRE ITEM 2000.
                    memberRetire_String(ext2000, ext2000part);
                    // IF CHANGED, UPDATE ITEM 200 MEMBER
                    updated = memberUpdate_String(ext200, ext200part, icd9CodeStr);
                    if (updated)
                        doEDGClinicalItem_40(nidConcept, icd9CodeStr, item2000);
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return;
    }

    /************************
     * EDGClinicalItem_207<br>
     * TYPE: integer<br>
     * USE: sets to "Clinically Active" as default<br>
     * NOTE: 1 == "Clinically Inactive, 2 == "Clinically Active"<br>
     * <br>
     * <code>
     * IF (EDGClinicalItem_207 .IS.NOT.PRESENT)<br>
     * THEN<br>
     * &nbsp;&nbsp;CREATE EDGClinicalItem_207 refset_member<br>
     * &nbsp;&nbsp;SET refset_member value to 2<br>
     * ELSE<br>
     * &nbsp;&nbsp;DO_NOTHING<br></code>
     * 
     * @param nidConcept
     */
    private void doEDGClinicalItem_207(int nidConcept) {
        try {
            FoundMember oldMember = memberFind(nidConcept, nidEDGClinicalItem_207);
            if (oldMember == null)
                memberCreate_Int(nidEDGClinicalItem_207, nidConcept, 2);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * EDGClinicalItem_40<br>
     * TYPE: String<br>
     * USE: "unified" item 200 & item 2000 identifiers<br>
     * <b>RESTRICTION:
     * <ul>
     * <li>CALL ONLY IF item 200/2000 has changed or add, so a new item 40 is
     * required!</li>
     * <li>MUST BE COMPUTED IN BATCH to avoid concurrency issues of possibly
     * generating conflicting values by different users
     * </ul>
     * <br>
     * </b> <code>
     * FOR ALL (EDGClinicalItem_2_National .AND. EDGClinicalItem_2_Regional)<br>
     * IF (EDGClinicalItem_200 .PRESENT.)<br>
     * THEN<br>
     * &nbsp;&nbsp;GET MAX 200000_Range_Number<br>
     * &nbsp;&nbsp;CREATE EDGClinicalItem_40 refset_member<br>
     * &nbsp;&nbsp;SET refset_member MAX 200000_Range_Number plus 1<br>
     * <br>
     * IF (EDGClinicalItem_2000 .PRESENT.)<br>
     * THEN<br>
     * &nbsp;&nbsp;GET EDGClinicalItem_2000 ICD9 Code value<br>
     * &nbsp;&nbsp;CATENATE next alpha character from A-Z,AA-AB..ZY, ZZ range)<br></code>
     * <br>
     * 
     * @param concept
     */
    private void doEDGClinicalItem_40(int nidConcept, String code, boolean item200) {
        String tmpStr = null;
        if (item200)
            // Create NEW EDGClinical Item 40 based on Item 200
            tmpStr = Integer.toString(item40Generator.new200());
        else
            // Create NEW EDGClinical Item 40 based on Item 2000
            tmpStr = item40Generator.new2000(code);

        if (tmpStr == null)
            return;

        try {
            FoundMember oldMember = memberFind(nidConcept, nidEDGClinicalItem_40);
            if (oldMember == null) {
                memberCreate_String(nidEDGClinicalItem_40, nidConcept, tmpStr);
            } else {
                memberUpdate_String(oldMember.getExt(), oldMember.getExtPart(), tmpStr);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void doEDGClinicalItem_40_Setup(List<SnoCon> scl) {
        for (SnoCon sc : scl) {

            List<I_DescriptionVersioned> descList;
            try {
                descList = findDescription_Type2(sc.id);
            } catch (TerminologyException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                continue;
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                continue;
            }

            for (I_DescriptionVersioned desc : descList) {
                int dNid = desc.getNid();

                // Check for existing Item 200 or Item 2000 RefSet member.
                List<I_ThinExtByRefVersioned> extList = null;
                try {
                    extList = tf.getAllExtensionsForComponent(dNid);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    continue;
                }
                I_ThinExtByRefVersioned ext200 = null;
                I_ThinExtByRefPart ext200part = null;
                I_ThinExtByRefVersioned ext2000 = null;
                I_ThinExtByRefPart ext2000part = null;
                I_ThinExtByRefVersioned ext40 = null;
                I_ThinExtByRefPart ext40part = null;
                for (I_ThinExtByRefVersioned ext : extList) {
                    if (ext.getRefsetId() == nidEDGClinicalItem_200) {
                        if (ext200 == null) {
                            ext200 = ext;
                            List<? extends I_ThinExtByRefPart> partList = ext.getMutableParts();
                            int lastVersion = Integer.MIN_VALUE;
                            for (I_ThinExtByRefPart part : partList) {
                                if (part.getVersion() > lastVersion) {
                                    lastVersion = part.getVersion();
                                    ext200part = part;
                                }
                            }
                        } else {
                            logger.info("\r\n::: !!! DUPLICATE ITEM 200 ERROR");
                        }
                    } else if (ext.getRefsetId() == nidEDGClinicalItem_2000) {
                        if (ext2000 == null) {
                            ext2000 = ext;
                            List<? extends I_ThinExtByRefPart> partList = ext.getMutableParts();
                            int lastVersion = Integer.MIN_VALUE;
                            for (I_ThinExtByRefPart part : partList) {
                                if (part.getVersion() > lastVersion) {
                                    lastVersion = part.getVersion();
                                    ext2000part = part;
                                }
                            }
                        } else {
                            logger.info("\r\n::: !!! DUPLICATE ITEM 2000 ERROR");
                        }
                    } else if (ext.getRefsetId() == nidEDGClinicalItem_40) {
                        if (ext40 == null) {
                            ext40 = ext;
                            List<? extends I_ThinExtByRefPart> partList = ext.getMutableParts();
                            int lastVersion = Integer.MIN_VALUE;
                            for (I_ThinExtByRefPart part : partList) {
                                if (part.getVersion() > lastVersion) {
                                    lastVersion = part.getVersion();
                                    ext40part = part;
                                }
                            }
                        } else {
                            logger.info("\r\n::: !!! DUPLICATE ITEM 40 ERROR");
                        }
                    }

                }

                if (ext200part != null) {
                    I_ThinExtByRefPartString tmp = (I_ThinExtByRefPartString) ext40part;
                    item40Generator.add(tmp.getStringValue());

                }
                if (ext2000part != null) {
                    I_ThinExtByRefPartString tmp = (I_ThinExtByRefPartString) ext40part;
                    item40Generator.add(tmp.getStringValue());

                }

            }
        }
        item40Generator.sort();
    }

    /**************************
     * EDGClinicalItem_7010<br>
     * TYPE: String<br>
     * USE: Patient_Friendly_Display_Name text<br>
     * <br>
     * <code>
     * IF (Patient_Friendly_Display_Name .DOES.NOT.EXIST.)<br>
     * THEN<br>
     * &nbsp;&nbsp;DO_NOTHING<br>
     * <br>
     * IF (.ONLY.ONE. Patient_Friendly_Display_Name .EXISTS.)<br>
     * THEN<br>
     * &nbsp;&nbsp;CREATE EDGClinicalItem_7010 refset_member<br>
     * &nbsp;&nbsp;SET refset_member string value to contain same description text<br>
     * <br>
     * IF (.MULTIPLE. Patient_Friendly_Display_Name .EXIST.)<br>
     * THEN<br>
     * &nbsp;&nbsp;SELECT one<br>
     * &nbsp;&nbsp;CREATE one EDGClinicalItem_7010 refset_member<br>
     * &nbsp;&nbsp;SET refset_member string value to contain selected description text<br>
     * </code>
     * 
     * @param nid
     */
    private void doEDGClinicalItem_7010(I_DescriptionVersioned desc, String name) {
        // NOTE: name is null if no patient friendly name exists.
        try {
            FoundMember oldMember = memberFind(desc.getNid(), nidEDGClinicalItem_7010);
            if (name == null && oldMember == null)
                ; // DO NOTHING
            else if (name != null && oldMember == null)
                memberCreate_String(nidEDGClinicalItem_7010, desc.getNid(), name);
            else if (name != null && oldMember != null)
                memberUpdate_String(oldMember.getExt(), oldMember.getExtPart(), name);
            else
                // name == null && oldMember != null
                ; // memberRetire_String(oldMember.getExt(),
            // oldMember.getExtPart());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /************************
     * doEDGClinicalItem_80()<br>
     * TYPE: Concept<br>
     * USE: EDC Dot1, Diagnostic Group Concept ID, Section E<br>
     * NOTE: EDC Dot1 '66' meaning "Other diagnosis"<br>
     * <br>
     * <code>
     * IF (EDCItem_2000 .EXISTS.)<br>
     * THEN<br>
     * &nbsp;&nbsp;GET ICD9 Code<br>
     * &nbsp;&nbsp;FIND ICD9_CM_Vol_1_Group<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE (ICD_9 >= EDCItem_110) .AND. (EDCItem_110 <= EDCItem_110)<br>
     * &nbsp;&nbsp;CREATE EDGClinicalItem_80 refset_member<br>
     * &nbsp;&nbsp;SET refset_member value of ICD9_CM_Vol_1_Group Dot1<br>
     * <br>
     * ELSE IF (EDCItem_2000 .EXISTS.) .AND. (EDCItem_110 .EXISTS.))<br>
     * THEN<br>
     * &nbsp;&nbsp;GET First ICD9 Code<br>
     * &nbsp;&nbsp;FIND ICD9_CM_Vol_1_Group<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;WHERE (ICD_9 >= EDCItem_110) .AND. (EDCItem_110 <= EDCItem_110)<br>
     * &nbsp;&nbsp;CREATE EDGClinicalItem_80 refset_member<br>
     * &nbsp;&nbsp;SET refset_member value of ICD9_CM_Vol_1_Group Dot1<br>
     * <br>
     * ELSE<br>
     * &nbsp;&nbsp;CREATE EDGClinicalItem_80 refset_member<br>
     * &nbsp;&nbsp;SET refset_member value to 66<br></code>
     * 
     * @param concept
     * @param icd9CodeValues
     */
    private void doEDGClinicalItem_80(I_DescriptionVersioned desc, ArrayList<String> icd9CodeValues) {
        if (icd9CodeValues.size() < 1)
            return;

        String dot1 = icd9Groups.lookupDot1(icd9CodeValues.get(0));
        try {
            FoundMember oldMember = memberFind(desc.getNid(), nidEDGClinicalItem_80);
            if (oldMember == null)
                memberCreate_String(nidEDGClinicalItem_80, desc.getNid(), dot1);
            else
                memberUpdate_String(oldMember.getExt(), oldMember.getExtPart(), dot1);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /***********************
     * <b>EDG Clinical Item 91</b><br>
     * TYPE: integer<br>
     * USE: flag presence of EDGClinicalItem_2000 or EDGClinicalItem_200<br>
     * NOTE: EDGClinicalItem_91 refset will be provided<br>
     *<br>
     * <code>
     * IF ((EDGClinicalItem_2000 .NOT.PRESENT.) <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;.AND. (EDGClinicalItem_200 .NOT.PRESENT.))<br>
     * THEN <br>
     * &nbsp;&nbsp;CREATE EDGClinicalItem_91 refset_member <br>
     * &nbsp;&nbsp;SET refset_member value to 1 <br>
     * ELSE <br>
     * &nbsp;&nbsp;CREATE EDGClinicalItem_91 refset_member <br>
     * &nbsp;&nbsp;SET refset_member value to 0 <br></code>
     * 
     * @param nid
     */
    private void doEDGClinicalItem_91(int nid, ArrayList<String> icd9CodeValues) {
        try {
            // Determine current value
            int value;
            if (icd9CodeValues.size() > 0)
                value = 0;
            else
                value = 1; // 1 IFF both Item200 & Item2000 not present

            // Check for existing Item 91 RefSet member.
            FoundMember oldMember = memberFind(nid, nidEDGClinicalItem_91);
            if (oldMember == null)
                memberCreate_Int(nidEDGClinicalItem_91, nid, value);
            else
                memberUpdate_Int(oldMember.getExt(), oldMember.getExtPart(), value);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return;
    }

    private void memberCreate_Int(int nidRefSet, int nidConcept, int value)
            throws TerminologyException, IOException {
        // ADD NEW ITEM ___ REFSET MEMBER
        // NEW EXTENSION
        // UUID uid -- random
        // int source -- nid for unspecifiedUuid
        // I_Path idPath
        // int version :@@@:???: MAX_INTEGER absorbed into UUID
        int memberId = tf.uuidToNativeWithGeneration(UUID.randomUUID(), nidUnspecifiedUuid,
            editIPath, nidVersion);
        // (refSetId, memberId, componentId, typeId)
        I_ThinExtByRefVersioned newExt = tf.newExtension(nidRefSet, memberId, nidConcept,
            nidTypeString);
        tf.addUncommitted(newExt);

        // NEW PART
        I_ThinExtByRefPartInteger newExtPart = tf.newExtensionPart(I_ThinExtByRefPartInteger.class);

        // PART SETTERS
        newExtPart.setPathId(nidEditPath);
        newExtPart.setStatusId(nidCURRENT);
        newExtPart.setVersion(nidVersion);

        // Set extension part value
        newExtPart.setIntValue(value);
        // Add version to extension
        newExt.addVersion(newExtPart);

        tf.addUncommitted(newExt);

        if (useLogFile && verboseLogFile)
            writeExtLog((logCount++) + "\tCREATE\t" + value + toLogStr(newExt) + "\r\n");
    }

    private void memberCreate_String(int nidRefSet, int nidConcept, String value)
            throws TerminologyException, IOException {
        // ADD NEW ITEM ___ REFSET MEMBER
        // NEW EXTENSION
        // UUID uid -- random
        // int source -- nid for unspecifiedUuid
        // I_Path idPath
        // int version :@@@:???: MAX_INTEGER absorbed into UUID
        int memberId = tf.uuidToNativeWithGeneration(UUID.randomUUID(), nidUnspecifiedUuid,
            editIPath, nidVersion);
        // (refSetId, memberId, componentId, typeId)
        I_ThinExtByRefVersioned newExt = tf.newExtension(nidRefSet, memberId, nidConcept,
            nidTypeString);
        tf.addUncommitted(newExt);

        // NEW PART
        I_ThinExtByRefPartString newExtPart = tf.newExtensionPart(I_ThinExtByRefPartString.class);

        // PART SETTERS
        newExtPart.setPathId(nidEditPath);
        newExtPart.setStatusId(nidCURRENT);
        newExtPart.setVersion(nidVersion);

        // Set extension part value
        newExtPart.setStringValue(value);
        // Add version to extension
        newExt.addVersion(newExtPart);

        tf.addUncommitted(newExt);

        if (useLogFile && verboseLogFile)
            writeExtLog((logCount++) + "\tCREATE\t" + value + toLogStr(newExt) + "\r\n");
    }

    private FoundMember memberFind(int nidConcept, int nidClinicalItem_Num) throws IOException {
        List<I_ThinExtByRefVersioned> extList = tf.getAllExtensionsForComponent(nidConcept);

        // 
        I_ThinExtByRefVersioned extResult = null;
        I_ThinExtByRefPart extPartResult = null;
        for (I_ThinExtByRefVersioned ext : extList) {
            if (ext.getRefsetId() == nidClinicalItem_Num) {
                if (extResult == null) {
                    extResult = ext;
                    List<? extends I_ThinExtByRefPart> partList = ext.getMutableParts();
                    int lastVersion = Integer.MIN_VALUE;
                    for (I_ThinExtByRefPart part : partList) {
                        if (part.getVersion() > lastVersion) {
                            lastVersion = part.getVersion();
                            extPartResult = part;
                        }
                    }
                } else
                    logger.info("\r\n::: !!! DUPLICATE ITEM ERROR");
            }
        }

        FoundMember result = null;
        if (extResult != null && extPartResult != null)
            result = new FoundMember(extResult, extPartResult);

        return result;
    }

    // 
    private void memberRetire_String(I_ThinExtByRefVersioned ext, I_ThinExtByRefPart extPart) {
        I_ThinExtByRefPart dupl = extPart.duplicate();
        dupl.setStatusId(nidRETIRED);
        dupl.setVersion(nidVersion);
        ext.addVersion(dupl);
        tf.addUncommitted(ext);

        if (useLogFile && verboseLogFile)
            writeExtLog((logCount++) + "\tRETIRE\t" + "*****" + toLogStr(ext) + "\r\n");
    }

    // 
    private void memberUpdate_Int(I_ThinExtByRefVersioned ext, I_ThinExtByRefPart extPart, int value) {
        I_ThinExtByRefPartInteger extPartStr = (I_ThinExtByRefPartInteger) extPart;

        if (extPartStr.getIntValue() != value) {
            I_ThinExtByRefPart dupl = extPart.duplicate();
            // dupl.setStatusId(:!!!:@@@:); default status of update???
            dupl.setVersion(nidVersion);
            I_ThinExtByRefPartInteger duplInt = (I_ThinExtByRefPartInteger) dupl;
            duplInt.setIntValue(value);
            ext.addVersion(dupl);
            tf.addUncommitted(ext);

            if (useLogFile && verboseLogFile)
                writeExtLog((logCount++) + "\tUPDATE\t" + value + toLogStr(ext) + "\r\n");
        }

    }

    // 
    private boolean memberUpdate_String(I_ThinExtByRefVersioned ext, I_ThinExtByRefPart extPart,
            String str) {
        I_ThinExtByRefPartString extPartStr = (I_ThinExtByRefPartString) extPart;

        if (!extPartStr.getStringValue().equalsIgnoreCase(str)) {
            I_ThinExtByRefPart dupl = extPart.duplicate();
            // dupl.setStatusId(:!!!:@@@:); default status of update???
            dupl.setVersion(nidVersion);
            I_ThinExtByRefPartString duplStr = (I_ThinExtByRefPartString) dupl;
            duplStr.setStringValue(str);
            ext.addVersion(dupl);
            tf.addUncommitted(ext);
            if (useLogFile && verboseLogFile)
                writeExtLog((logCount++) + "\tUPDATE\t" + str + toLogStr(ext) + "\r\n");
            return true;
        }
        return false;
    }

    private List<FoundDescExtPair> findDescExtPair_Type2(int cNid) throws TerminologyException,
            IOException {
        // Create list of descriptions which have type 2 extensions
        List<FoundDescExtPair> resultList = new ArrayList<FoundDescExtPair>();

        I_GetConceptData concept = tf.getConcept(cNid);
        List<? extends I_DescriptionVersioned> descList = concept.getDescriptions();
        for (I_DescriptionVersioned desc : descList) {
            List<I_ThinExtByRefVersioned> extList = tf.getAllExtensionsForComponent(desc.getNid());
            // check each member for presence of Clinical Type 2 extension
            for (I_ThinExtByRefVersioned ext : extList) {
                int refSetNid = ext.getRefsetId();
                int len = nidsEDGClinicalItem_2.length;
                for (int i = 0; i < len; i++)
                    if (refSetNid == nidsEDGClinicalItem_2[i])
                        resultList.add(new FoundDescExtPair(desc, ext));
            }
        }
        return resultList;
    }

    private List<I_DescriptionVersioned> findDescription_Type2(int cNid)
            throws TerminologyException, IOException {
        // Create list of descriptions which have type 2 extensions
        List<I_DescriptionVersioned> resultList = new ArrayList<I_DescriptionVersioned>();

        I_GetConceptData concept = tf.getConcept(cNid);
        List<? extends I_DescriptionVersioned> descList = concept.getDescriptions();
        for (I_DescriptionVersioned desc : descList) {
            List<I_ThinExtByRefVersioned> extList = tf.getAllExtensionsForComponent(desc.getNid());
            // check each member for presence of Clinical Type 2 extension
            for (I_ThinExtByRefVersioned ext : extList) {
                int refSetNid = ext.getRefsetId();
                int len = nidsEDGClinicalItem_2.length;
                boolean found = false;
                for (int i = 0; i < len; i++)
                    if (refSetNid == nidsEDGClinicalItem_2[i])
                        found = true;
                if (found)
                    resultList.add(desc);
            }
        }
        return resultList;
    }

    private String findDescrFriendly(int cNid) throws TerminologyException, IOException {
        String result = null;
        // Find first Patient Friendly Display String
        I_GetConceptData concept = tf.getConcept(cNid);
        List<? extends I_DescriptionVersioned> descList = concept.getDescriptions();
        for (I_DescriptionVersioned desc : descList) {
            I_DescriptionPart part = lastVersionDesc(desc);
            if (part != null && part.getTypeId() == nidPatientFriendly)
                if (result == null)
                    result = part.getText();
                else
                    logger.info("\r\n::: ERROR Multiple Patient Friendly Names: "
                        + concept.getInitialText() + " .AND. " + result);
        }
        return result;
    }

    // :@@@: can not find
    private List<SnoCon> findConceptsFromRefSet() throws TerminologyException, ParseException,
            IOException {
        // :TODO: REFSET -> MEMBER -> DESCRIPTION -> CONCEPT needs to be
        // revisited
        List<SnoCon> result = new ArrayList<SnoCon>();
        List<I_GetConceptData> refsetList = findRefSets_Type2();
        for (I_GetConceptData refset : refsetList) {
            // GET MEMBERS
            List<I_ThinExtByRefVersioned> memberList;
            memberList = tf.getRefsetExtensionMembers(refset.getConceptId());
            for (I_ThinExtByRefVersioned member : memberList) {
                // DETERMINE IF THIS IS A
                int descNid = member.getComponentId();
                // :TODO:MEC:NOTE: revisit getDescription() API when newer DB
                // structure in place
                I_DescriptionVersioned desc = tf.getDescription(descNid, descNid);

                // :MEC:NOTE: get concept which encloses description
                int conNid = desc.getConceptId();
                I_GetConceptData conBean = tf.getConcept(conNid);
                // :TODO: check if current part is active, on path, then get if
                // idDefined.

                result.add(new SnoCon(conNid, false)); // is defined field will
                // be ignored in this
                // class
            }
        }
        return result;
    }

    /**
     * Find all refset extension IDC9 Code Mappings for given SNOMED concept.<br>
     * <br>
     * LEVELS:
     * <ol>
     * <li>For a given SNOMED ID, find each ID9 Mapping concept extension.
     * <li>For each ICD9 Mapping extension, find the most recent versioned part.
     * <li>For the recent ICD9 Mapping versioned part, get the ICD9 Code native
     * id (NID).
     * <li>From the ICD9 Code NID, retrieve the IDC9 Code I_IdVersioned.
     * <li>...
     * </ol>
     * 
     * @param nid
     * @return
     * @throws Exception
     */
    private ArrayList<String> findICD9Values(int snoNid) {
        ArrayList<String> result = new ArrayList<String>();

        try {
            // Find all ICD9 Mapping extensions to Snomed concept
            List<I_ThinExtByRefVersioned> snoExtList = tf.getAllExtensionsForComponent(snoNid);
            for (I_ThinExtByRefVersioned ext : snoExtList) {
                if (ext.getRefsetId() == nidICD9CodeMappings) {

                    // FIND MOST RECENT VERSION OF THIS ICD9 MAPPING
                    int lastVersion = Integer.MIN_VALUE;
                    I_ThinExtByRefPart lastPart = null;
                    List<? extends I_ThinExtByRefPart> partsList = ext.getMutableParts();
                    for (I_ThinExtByRefPart part : partsList) {
                        if (isOnAPath(part.getPathId(), part.getVersion())) {
                            if (part.getVersion() > lastVersion) {
                                lastVersion = part.getVersion();
                                lastPart = part;
                            }
                        }
                    }
                    if (lastPart != null) {
                        I_ThinExtByRefPartConcept conPart = (I_ThinExtByRefPartConcept) lastPart;
                        int icd9CodeNid = conPart.getC1id();
                        // Get Identifiers for the ICD9 Code Concept
                        I_GetConceptData icd9CodeCB = tf.getConcept(icd9CodeNid);

                        I_Identify icd9CodeId = icd9CodeCB.getIdentifier();
                        for (I_IdPart idPart : icd9CodeId.getMutableIdParts()) {
                            if (idPart.getAuthorityNid() == nidIDC9Id) {
                                result.add((String) idPart.getDenotation());
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    private List<I_GetConceptData> findRefSets_Type2() throws TerminologyException, ParseException,
            IOException {
        List<I_GetConceptData> childRefsets = new ArrayList<I_GetConceptData>();
        StringBuilder sb = new StringBuilder();

        // Get the children refsets of EDGClinicalItem_2_National refset
        int refsetId = tf.uuidToNative(uuidEDGClinicalItem_2_National);
        I_GetConceptData parent = tf.getConcept(refsetId);
        List<? extends I_RelVersioned> childRelList = parent.getDestRels();
        for (I_RelVersioned childRel : childRelList) {

            // :NYI: does not check for latest status of most current version
            // :@@@: treat all child refsets as current for KP Pilot
            int childNid = childRel.getC1Id();
            I_GetConceptData childCB = tf.getConcept(childNid);
            childRefsets.add(childCB);

            sb.append("\r\n::: " + childCB.getInitialText());
        }
        logger.info("\r\n::: [AutoGenEDGRefset] Type 2 RefSets" + sb.toString());
        return childRefsets;
    }

    private boolean isOnAPath(int pNid, int vNid) {
        // :@@@: for Workbench Auxiliary Path
        if (pNid == workbenchAuxPath)
            return true;
        
        for (I_Position pos : editPathPos) 
            if ((pos.getPath().getConceptId() == pNid) && (vNid <= pos.getVersion()))
                return true;

        return false;
    }

    private I_DescriptionPart lastVersionDesc(I_DescriptionVersioned desc) {
        // FIND MOST RECENT VERSION OF THIS ICD9 MAPPING
        int lastVersion = Integer.MIN_VALUE;
        I_DescriptionPart lastPart = null;
        List<? extends I_DescriptionPart> partsList = desc.getMutableParts();
        for (I_DescriptionPart part : partsList) {
            if (isOnAPath(part.getPathId(), part.getVersion())) {
                if (part.getVersion() > lastVersion) {
                    lastVersion = part.getVersion();
                    lastPart = part;
                }
            }
        }
        return lastPart;
    }

    private Condition setupCoreNids() {
        I_TermFactory tf = LocalVersionedTerminology.get();

        // SETUP CORE NATIVES IDs
        try {
            config = tf.getActiveAceFrameConfig();

            // SETUP CORE NATIVES IDs
            isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());

            if (config.getClassifierIsaType() != null) {
                int checkIsaNid = tf.uuidToNative(config.getClassifierIsaType().getUids());
                if (checkIsaNid != isaNid) {
                    logger.severe("\r\n::: SERVERE ERROR isaNid MISMATCH ****");
                }
            } else {
                String errStr = "Classifier Is-a not set! Found: "
                    + tf.getActiveAceFrameConfig().getEditingPathSet();
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                    new TaskFailedException(errStr));
                return Condition.STOP;
            }

            // 0 CURRENT, 1 RETIRED
            nidCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            nidRETIRED = tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
            nidVersion = Integer.MAX_VALUE;

            nidUnspecifiedUuid = ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
                .getNid();

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

            nidEditPath = cEditPathObj.getConceptId();
            editIPath = tf.getPath(cEditPathObj.getUids());
            editPathPos = new ArrayList<I_Position>();
            editPathPos.add(tf.newPosition(editIPath, Integer.MAX_VALUE));
            setupPathOrigins(editPathPos, editIPath);

        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Condition.CONTINUE;
    }

    private void setupPathOrigins(List<I_Position> origins, I_Path p) {
        for (I_Position o : p.getOrigins()) {
            if (o.getPath().getConceptId() != workbenchAuxPath)
                origins.add(o);
        }

        for (I_Position o : p.getOrigins()) {
            setupPathOrigins(origins, o.getPath()); // recursive
        }
    }

    private void setupUUIDs() {
        uuidICD9CMGroups = UUID.fromString("66b583e9-6b6b-58be-8349-30a8b7e8f79e");
        uuidICD9CodeMappings = UUID.fromString("30d00210-3bf5-559c-8969-b74b5f85c07e");

        uuidEDGClinicalItem_2_National = UUID.fromString("d1b595e1-5f8c-5c0e-90b4-81445018c76a");

        uuidEDCItem_100 = UUID.fromString("a37ed683-2994-5c25-af18-a97dd9014a71");
        uuidEDCItem_110 = UUID.fromString("05a66107-c857-57e4-8df7-25719919dac6");

        uuidEDGClinicalItem_100 = UUID.fromString("457aca8f-f790-5a1a-82a0-1ab7c7eb21ba");
        uuidEDGClinicalItem_200 = UUID.fromString("0a8ef58f-51bf-5947-9dce-a3e6d803973b");
        uuidEDGClinicalItem_2000 = UUID.fromString("a57ed0aa-7a59-58e2-9199-dd6b4295507c");
        uuidEDGClinicalItem_207 = UUID.fromString("c9a04c71-ba94-5119-935c-8c959674e227");
        uuidEDGClinicalItem_40 = UUID.fromString("eb2cf3af-7737-58aa-be20-037579dd83c7");
        uuidEDGClinicalItem_50 = UUID.fromString("472f9db9-4376-43c7-9d26-492900255f38");
        uuidEDGClinicalItem_7000 = UUID.fromString("bdbfe72e-11a7-5f80-a2ac-5d265f661f4a");
        uuidEDGClinicalItem_7010 = UUID.fromString("4a972ec9-ce8a-53dd-85f6-6890b1fb9e68");
        uuidEDGClinicalItem_80 = UUID.fromString("46919845-e9cd-5b69-a03d-3bbdfd02eec6");
        uuidEDGClinicalItem_91 = UUID.fromString("d6b995df-78a9-4521-921d-699baa74d0f9");

        uuidEDGClinicalDot1 = UUID.fromString("b7c3242d-aabc-56f8-9c5f-78a1e8a7cb1b");
        uuidPatientFriendly = UUID.fromString("df600a87-6877-4d82-b27e-6c32c0427845");

        uuidTypeBoolean = UUID.fromString("893b86c9-1f2d-395c-a184-f10358c37856");
        uuidTypeConcept = UUID.fromString("d815700e-dd66-3f91-8f05-99c60b995eb4");
        uuidTypeString = UUID.fromString("4a5d2768-e2ae-3bc1-be2d-8d733cd4abdb");
        uuidTypeInt = UUID.fromString("bf91e36c-ff77-35cf-ad92-890518d0f5f2");

        uuidSnoConClinicalFinding = UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c");
        uuidSnoConEvent = UUID.fromString("c7243365-510d-3e5f-82b3-7286b27d7698");
        uuidSnoConSituation = UUID.fromString("27d03723-07c3-3de9-828b-76aa05a23438");

        try {
            nidICD9CMGroups = tf.getConcept(uuidICD9CMGroups).getConceptId();
            nidICD9CodeMappings = tf.getConcept(uuidICD9CodeMappings).getConceptId();
            nidIDC9Id = tf.uuidToNative(ArchitectonicAuxiliary.Concept.ICD_9.getUids());

            nidEDGClinicalItem_2_National = tf.getConcept(uuidEDGClinicalItem_2_National)
                .getConceptId();

            nidEDCItem_100 = tf.getConcept(uuidEDCItem_100).getConceptId();
            nidEDCItem_110 = tf.getConcept(uuidEDCItem_110).getConceptId();

            nidEDGClinicalItem_100 = tf.getConcept(uuidEDGClinicalItem_100).getConceptId();
            nidEDGClinicalItem_200 = tf.getConcept(uuidEDGClinicalItem_200).getConceptId();
            nidEDGClinicalItem_2000 = tf.getConcept(uuidEDGClinicalItem_2000).getConceptId();
            nidEDGClinicalItem_207 = tf.getConcept(uuidEDGClinicalItem_207).getConceptId();
            nidEDGClinicalItem_40 = tf.getConcept(uuidEDGClinicalItem_40).getConceptId();
            nidEDGClinicalItem_50 = tf.getConcept(uuidEDGClinicalItem_50).getConceptId();
            nidEDGClinicalItem_7000 = tf.getConcept(uuidEDGClinicalItem_7000).getConceptId();
            nidEDGClinicalItem_7010 = tf.getConcept(uuidEDGClinicalItem_7010).getConceptId();
            nidEDGClinicalItem_80 = tf.getConcept(uuidEDGClinicalItem_80).getConceptId();
            nidEDGClinicalItem_91 = tf.getConcept(uuidEDGClinicalItem_91).getConceptId();

            nidEDGClinicalDot1 = tf.getConcept(uuidEDGClinicalDot1).getConceptId();
            nidPatientFriendly = tf.getConcept(uuidPatientFriendly).getConceptId();

            nidTypeBoolean = tf.getConcept(uuidTypeBoolean).getConceptId();
            nidTypeConcept = tf.getConcept(uuidTypeConcept).getConceptId();
            nidTypeString = tf.getConcept(uuidTypeString).getConceptId();
            nidTypeInt = tf.getConcept(uuidTypeInt).getConceptId();

            List<I_GetConceptData> type2CBList = findRefSets_Type2();
            int size = type2CBList.size();
            nidsEDGClinicalItem_2 = new int[size];
            for (int i = 0; i < size; i++)
                nidsEDGClinicalItem_2[i] = type2CBList.get(i).getNid();

            nidSnoConClinicalFinding = tf.getConcept(uuidSnoConClinicalFinding).getConceptId();
            nidSnoConEvent = tf.getConcept(uuidSnoConEvent).getConceptId();
            nidSnoConSituation = tf.getConcept(uuidSnoConSituation).getConceptId();

        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String toLogStr(int cNid) {
        String pad = "                     ";
        StringBuilder s = new StringBuilder();
        try {
            I_GetConceptData concept = tf.getConcept(cNid);
            s.append(concept.getInitialText() + "\t");
            s.append(concept.getUids().iterator().next());
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return s.toString();
    }

    private String toLogStr(I_ThinExtByRefVersioned ext) {
        StringBuilder s = new StringBuilder();

        int rfNid = ext.getRefsetId();
        if (rfNid == nidEDGClinicalItem_100)
            s.append("\t|EDGClinicalItem_100 \t");
        else if (rfNid == nidEDGClinicalItem_200)
            s.append("\t|EDGClinicalItem_200 \t");
        else if (rfNid == nidEDGClinicalItem_2000)
            s.append("\t|EDGClinicalItem_2000\t");
        else if (rfNid == nidEDGClinicalItem_207)
            s.append("\t|EDGClinicalItem_207 \t");
        else if (rfNid == nidEDGClinicalItem_40)
            s.append("\t|EDGClinicalItem_40  \t");
        else if (rfNid == nidEDGClinicalItem_50)
            s.append("\t|EDGClinicalItem_50  \t");
        else if (rfNid == nidEDGClinicalItem_7000)
            s.append("\t|EDGClinicalItem_7000\t");
        else if (rfNid == nidEDGClinicalItem_7010)
            s.append("\t|EDGClinicalItem_7010\t");
        else if (rfNid == nidEDGClinicalItem_80)
            s.append("\t|EDGClinicalItem_80  \t");
        else if (rfNid == nidEDGClinicalItem_91)
            s.append("\t|EDGClinicalItem_91  \t");
        else
            s.append("\t|unknown\t");

        try {
            I_GetConceptData concept = tf.getConcept(ext.getComponentId());
            s.append(concept.getUids().iterator().next());
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return s.toString();
    }

    private void writeExtLog(String s) {
        try {
            logFile.write(s);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String toStringNids() {
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: [AutoGenEDGRefset]");
        s.append("\r\n:::\t" + isaNid + "\t : isaNid");
        s.append("\r\n:::\t" + nidCURRENT + "\t : nidCURRENT");
        s.append("\r\n:::\t" + nidRETIRED + "\t : nidRETIRED");
        s.append("\r\n");
        return s.toString();
    }

    /**
     * @return Classifier input and output paths as a string.
     */
    private String toStringPathPos(List<I_Position> pathPos, String pStr) {
        // BUILD STRING
        StringBuffer s = new StringBuffer();
        s.append("\r\n::: [AutoGenEDGRefset] PATH ID -- " + pStr);
        try {
            for (I_Position position : pathPos) {
                s.append("\r\n::: PATH NAME:\t"
                    + tf.getConcept(position.getPath().getConceptId()).getInitialText()
                    + "\r\n       PathID:\t" + position.getPath().getConceptId() + "\tUUID:\t"
                    + position.getPath().getUniversal().getPathId().iterator().next());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TerminologyException e) {
            e.printStackTrace();
        }
        s.append("\r\n:::");
        return s.toString();
    }

}
