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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_ConfigAceFrame.CLASSIFIER_INPUT_MODE_PREF;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

public class SnoTable {

    private final static int stateFlag = 0x00000001;
    private final static int inferFlag = 0x00000002;
    private final static int dnfFlag = 0x00000010;
    private final static int anfFlag = 0x00000020;
    private final static int scfFlag = 0x00000040;
    private final static int lcfFlag = 0x00000080;
    private final static int totalColFlags = 6; // !! 7 SF, S, I, DN, AN, SC, LC
    private final static int totalCol = 8;
    // private final static int isaProxFlag = 0x00010000;
    // private final static int isaProxPrimFlag = 0x00020000;
    // private final static int roleProxFlag = 0x00100000;
    // private final static int roleDiffFromRootFlag = 0x00200000;
    // private final static int roleDiffFromProxFlag = 0x00400000;
    // private final static int roleDiffFromProxPrimFlag = 0x00800000;
    // INFERRED GROUP LISTS
    SnoGrpList isaProxSnoGrpList = null; // USE: Distribution, Authoring Normal
    SnoGrpList isaProxPrimSnoGrpList = null; // USE: Short, Long Canonical
    SnoGrpList roleDiffFromRootList = null;
    SnoGrpList roleDiffFromProxList = null;
    SnoGrpList roleDiffFromProxPrimList = null;
    // STATED GROUP LISTS
    SnoGrpList isaStatedSnoGrpList = null; // USE: Stated
    SnoGrpList roleStatedSnoGrpList = null; // USE: Stated
    // ROWS OF DATA IN TABLE
    String cBeanStr;
    boolean cBeanDef;
    ArrayList<SnoTableRow> snoTableRows = null;
    // ** WORKBENCH PARTICULARS **
    static private I_TermFactory tf;
    // ** CORE CONSTANTS **
    private static int isaNid = Integer.MIN_VALUE;
    private static int isCURRENT = Integer.MIN_VALUE;
    private static int rootNid = Integer.MIN_VALUE;
    private static int roleRootNid = Integer.MIN_VALUE;
    // STATED & INFERRED PATHS
    static List<PositionBI> cEditPath = null; // Classifier Stated Path
    static List<PositionBI> cViewPath = null; // Classifier Inferred Path
    private static int snorocketAuthorNid;
    private static final boolean USER_STATED = true;
    private static final boolean USER_SNOROCKET = true;
    // ** STATISTICS **
    // :NYI: need reset statistics routine
    // private int countFindIsaProxDuplPart = 0;
    private int countFindRoleProxDuplPart = 0;
    // private int countFindSelfDuplPart = 0;
    private int countIsCDefinedDuplPart = 0;
    // private int countFindIsaProxDuplPartGE2 = 0;
    private int countFindRoleProxDuplPartGE2 = 0;
    // private int countFindSelfDuplPartGE2 = 0;
    private int countIsCDefinedDuplPartGE2 = 0;
    // Setup Strings
    String xStr = new String(String.valueOf('\u2022')); // &bull; U+2022 (8226)
    String bStr = new String(" ");
    String errStr = new String("*");
    String typeFont = "<font face='Dialog' size='3' color='#000066'>";
    String valueFont = "<font face='Dialog' size='3' color='#006600'>";
    // INTERNAL
    private boolean debug = false; // :DEBUG:

    @SuppressWarnings("serial")
    private class SnoTableRow extends SnoGrp {

        int flags;
        int rowHeight;

        public SnoTableRow(int flag) {
            flags = flag;
        }

        public SnoTableRow(SnoGrp sg, int flag) {
            for (SnoRel rel : sg) {
                this.add(rel);
            }
            flags = flag;
            rowHeight = 18;
        }

        void setFlag(int flag) {
            flags = flags | flag;
        }
    }

    public SnoTable() {
        try {
            updatePrefs(true);
        } catch (Exception ex) {
            Logger.getLogger(SnoTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static boolean prefsNotSet() {
        if (isaNid == Integer.MIN_VALUE || isCURRENT == Integer.MIN_VALUE
                || rootNid == Integer.MIN_VALUE || roleRootNid == Integer.MIN_VALUE
                || cEditPath == null || cViewPath == null) {
            return true;
        } else {
            return false;
        }
    }

    public static String updatePrefs(boolean showDialogs) throws Exception {
        tf = Terms.get();
        I_ConfigAceFrame config;
        config = tf.getActiveAceFrameConfig();
        return updatePrefs(showDialogs, config);
    }

    private static String updatePrefs(boolean showDialogs, I_ConfigAceFrame config) throws Exception {
        // Setup core constants
        if (config.getClassifierIsaType() != null) {
            isaNid = config.getClassifierIsaType().getConceptNid();
        } else {
            String errStr = "'Is a' -- not set in Classifier preferences tab!";
            if (showDialogs) {
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
            } else {
                AceLog.getAppLog().log(Level.SEVERE, errStr, new Exception(errStr));
            }
            return errStr;
        }

        if (config.getClassificationRoleRoot() != null) {
            roleRootNid = config.getClassificationRoleRoot().getConceptNid();
        } else {
            String errStr = "Classifier Role Root -- not set in Classifier preferences tab!";
            if (showDialogs) {
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
            } else {
                AceLog.getAppLog().log(Level.SEVERE, errStr, new Exception(errStr));
            }
            return errStr;
        }

        isCURRENT = SnomedMetadataRfx.getSTATUS_CURRENT_NID();

        if (config.getClassificationRoot() != null) {
            rootNid = tf.uuidToNative(config.getClassificationRoot().getUids());
        } else {
            String errStr = "Classification Root not set!";
            if (showDialogs) {
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
            } else {
                AceLog.getAppLog().log(Level.SEVERE, errStr, new Exception(errStr));
            }
            return errStr;
        }

        // GET ALL EDIT_PATH ORIGINS
        if (config.getEditingPathSet() == null) {
            String errStr = "(SnoTable error) Edit path is not set.";
            AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                    new TaskFailedException(errStr));
            return errStr;
        } else if (config.getEditingPathSet().size() != 1) {
            String errStr = "(SnoTable error) Profile must have exactly one edit path. Found: "
                    + config.getEditingPathSet();
            if (showDialogs) {
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new TaskFailedException(errStr));
            } else {
                AceLog.getAppLog().log(Level.SEVERE, errStr, new Exception(errStr));
            }

            return errStr;
        }
        PathBI cEditPathBI = config.getEditingPathSet().iterator().next();
        cEditPath = new ArrayList<PositionBI>();
        cEditPath.add(tf.newPosition(cEditPathBI, Long.MAX_VALUE));
        addPathOrigins(cEditPath, cEditPathBI);

        // GET ALL CLASSIFER_PATH ORIGINS
        if (config.getViewPositionSet() == null) {
            String errStr = "(SnoTable error) View path is not set.";
            AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                    new TaskFailedException(errStr));
            return errStr;
        } else if (config.getViewPositionSet().size() != 1) {
            String errStr = "(SnoTable error) Profile must have exactly one view path. Found: "
                    + config.getViewPositionSet();
            AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                    new TaskFailedException(errStr));
            return errStr;
        }
        PositionBI cViewPositionBI = config.getViewPositionSet().iterator().next();
        cViewPath = new ArrayList<PositionBI>();
        cViewPath.add(cViewPositionBI);
        addPathOrigins(cViewPath, cViewPositionBI.getPath());

        snorocketAuthorNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.USER.SNOROCKET.getUids());

        return null;
    }

    private static void addPathOrigins(List<PositionBI> origins, PathBI p) {
        origins.addAll(p.getOrigins());
        for (PositionBI o : p.getOrigins()) {
            addPathOrigins(origins, o.getPath());
        }
    }

    // gatherFormData(theCBean) normal form data () (move routines here)
    public boolean gatherFormData(I_GetConceptData cBean) {
        ArrayList<SnoRel> isaProxList = null;
        ArrayList<SnoRel> isaProxPrimList = null;
        ArrayList<SnoRel> isaStatedList = null;

        try {
            if (cEditPath == null || cViewPath == null) {
                updatePrefs(false);
            }
            if (cEditPath == null || cViewPath == null) {
                return false;
            }

            if (tf == null) {
                Terms.get();
            }
            CLASSIFIER_INPUT_MODE_PREF inputMode = tf.getActiveAceFrameConfig().getClassifierInputMode();

            List<PositionBI> statedPath = null;
            List<PositionBI> inferredPath = null;
            if (inputMode == CLASSIFIER_INPUT_MODE_PREF.EDIT_PATH) {
                statedPath = cEditPath;
                inferredPath = cViewPath;
            } else if (inputMode == CLASSIFIER_INPUT_MODE_PREF.VIEW_PATH) {
                statedPath = cViewPath;
                inferredPath = cViewPath;
            } else if (inputMode == CLASSIFIER_INPUT_MODE_PREF.VIEW_PATH_WITH_EDIT_PRIORITY) {
                statedPath = cViewPath;
                inferredPath = cViewPath;
            }

            cBeanStr = cBean.getInitialText(); // Remember concept name
            cBeanDef = isCDefined(cBean, statedPath);

            // GET STATED DATA
            isaStatedList = findIsaProximal(cBean, statedPath, USER_STATED);
            isaStatedSnoGrpList = new SnoGrpList();
            for (SnoRel sr : isaStatedList) {
                isaStatedSnoGrpList.add(new SnoGrp(sr));
            }

            roleStatedSnoGrpList = findRoleGrpProximal(cBean, statedPath, USER_STATED);

            // GET INFERRED DATA
            // USE: Distribution Normal, Authoring Normal
            isaProxList = findIsaProximal(cBean, inferredPath, USER_SNOROCKET);
            isaProxSnoGrpList = new SnoGrpList();
            for (SnoRel sr : isaProxList) {
                isaProxSnoGrpList.add(new SnoGrp(sr));
            }

            // USE: Short Canonical Form, Long Canonical Form
            isaProxPrimList = findIsaProximalPrim(cBean, inferredPath, USER_SNOROCKET);
            isaProxPrimSnoGrpList = new SnoGrpList();
            for (SnoRel sr : isaProxPrimList) {
                isaProxPrimSnoGrpList.add(new SnoGrp(sr));
            }

            // USE: Distribution Normal Form, Long Canonical Form
            roleDiffFromRootList = findRoleGrpDiffFromRoot(cBean, inferredPath, USER_SNOROCKET);

            // USE: Authoring Form
            roleDiffFromProxList = findRoleGrpDiffFromProx(cBean, isaProxList, inferredPath,
                    USER_SNOROCKET);

            // USE: Short Canonical Form
            roleDiffFromProxPrimList = findRoleGrpDiffFromProxPrim(cBean, isaProxPrimList,
                    inferredPath, USER_SNOROCKET);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    } // gatherFormData

    // sort SnoGrpList (use SnoGrpList)
    // merge SnoGrpLists with flags
    // build data array
    private SnoGrpList markRows(SnoGrpList grpListA, ArrayList<SnoTableRow> rowListB, int flag) {
        SnoGrpList sg = new SnoGrpList();
        for (SnoGrp groupA : grpListA) {
            boolean foundEqual = false;
            for (SnoTableRow groupB : rowListB) {
                if (groupA.equals(groupB)) {
                    groupB.flags = groupB.flags | flag;
                    foundEqual = true;
                    break;
                }
            }
            if (!foundEqual) {
                sg.add(groupA);
            }
        }
        return sg;
    }

    public SnoGrpList getStatedIsaProx() {
        return isaStatedSnoGrpList;
    }

    public SnoGrpList getStatedRole() {
        return roleStatedSnoGrpList;
    }

    public SnoGrpList getIsaProx() {
        return isaProxSnoGrpList;
    }

    public SnoGrpList getIsaProxPrim() {
        return isaProxPrimSnoGrpList;
    }

    public SnoGrpList getRoleDiffFromProx() {
        return roleDiffFromProxList;
    }

    public SnoGrpList getRoleDiffFromProxPrim() {
        return roleDiffFromProxPrimList;
    }

    public SnoGrpList getRoleDiffFromRootList() {
        return roleDiffFromRootList;
    }

    public String[][] getStringData(I_GetConceptData cBean) throws TerminologyException,
            IOException {
        // Get the Form data component ArrayLists
        gatherFormData(cBean);
        return getStringData();
    }

    public String[][] getStringData() throws TerminologyException, IOException {
        SnoGrpList diff;
        int flags;

        // Create the Table Rows
        snoTableRows = new ArrayList<SnoTableRow>();

        // IS-A
        flags = stateFlag;
        diff = markRows(isaStatedSnoGrpList, snoTableRows, flags);
        for (SnoGrp sg : diff) {
            snoTableRows.add(new SnoTableRow(sg, flags));
        }

        flags = dnfFlag | anfFlag | inferFlag;
        diff = markRows(isaProxSnoGrpList, snoTableRows, flags);
        for (SnoGrp sg : diff) {
            snoTableRows.add(new SnoTableRow(sg, flags));
        }

        flags = lcfFlag | scfFlag | inferFlag;
        diff = markRows(isaProxPrimSnoGrpList, snoTableRows, flags);
        for (SnoGrp sg : diff) {
            snoTableRows.add(new SnoTableRow(sg, flags));
        }

        // ROLE VALUES: STATED
        flags = stateFlag;
        diff = markRows(roleStatedSnoGrpList, snoTableRows, flags);
        for (SnoGrp sg : diff) {
            snoTableRows.add(new SnoTableRow(sg, flags));
        }

        // ROLE VALUES: Distribution Normal Form, Long Canonical Form
        flags = dnfFlag | lcfFlag | inferFlag;
        diff = markRows(roleDiffFromRootList, snoTableRows, flags);
        for (SnoGrp sg : diff) {
            snoTableRows.add(new SnoTableRow(sg, flags));
        }

        // ROLE VALUES: Authoring Form
        flags = anfFlag | inferFlag;
        diff = markRows(roleDiffFromProxList, snoTableRows, flags);
        for (SnoGrp sg : diff) {
            snoTableRows.add(new SnoTableRow(sg, flags));
        }

        // ROLE VALUES: Short Canonical Form
        flags = scfFlag | inferFlag;
        diff = markRows(roleDiffFromProxPrimList, snoTableRows, flags);
        for (SnoGrp sg : diff) {
            snoTableRows.add(new SnoTableRow(sg, flags));
        }

        int totalRows = snoTableRows.size();
        String tableStrings[][] = new String[totalRows][totalCol];

        for (int i = 0; i < totalRows; i++) {
            SnoTableRow row = snoTableRows.get(i);
            int snoRelCount = row.size();
            if (snoRelCount <= 0) {
                // empty row
                for (int j = 0; j < 7; j++) {
                    tableStrings[i][j] = errStr;
                }
                continue;
            }

            // FLAG STRINGS
            tableStrings[i][0] = ((row.flags & stateFlag) > 0) ? xStr : bStr;
            tableStrings[i][1] = ((row.flags & inferFlag) > 0) ? xStr : bStr;
            tableStrings[i][2] = ((row.flags & dnfFlag) > 0) ? xStr : bStr;
            tableStrings[i][3] = ((row.flags & anfFlag) > 0) ? xStr : bStr;
            tableStrings[i][4] = ((row.flags & scfFlag) > 0) ? xStr : bStr;
            tableStrings[i][5] = ((row.flags & lcfFlag) > 0) ? xStr : bStr;

            StringBuilder str = new StringBuilder("<html>");
            // Each row is a group of 1 or more
            SnoRel sr = row.get(0);
            // TYPE STRING
            I_GetConceptData typeBean = tf.getConcept(sr.typeId);
            str.append(typeFont).append(typeBean.getInitialText());
            // VALUE STRING
            I_GetConceptData valueBean = tf.getConcept(sr.c2Id);
            str.append(": </font>").append(valueFont).append(valueBean.getInitialText());

            // Add addition in group
            for (int j = 1; j < snoRelCount; j++) {
                sr = row.get(j);
                // TYPE STRING
                typeBean = tf.getConcept(sr.typeId);
                str.append("</font><br>").append(typeFont).append(typeBean.getInitialText());
                // VALUE STRING
                valueBean = tf.getConcept(sr.c2Id);
                str.append(": </font>").append(valueFont).append(valueBean.getInitialText());
            }
            tableStrings[i][6] = str.toString();
        }

        if (debug) {
            logTable(tableStrings);
        }

        return tableStrings;
    }

    // show stats
    /**
     * List<SnoRel> --> ArrayList<SnoRel>
     * 
     */
    private static ArrayList<SnoRel> findIsaProximal(I_GetConceptData cBean,
            List<PositionBI> posList, boolean isStatedUser) {
        ArrayList<SnoRel> returnSnoRels = new ArrayList<SnoRel>();
        try {
            Collection<? extends I_RelVersioned> relList = cBean.getSourceRels();
            for (I_RelVersioned<?> rel : relList) { // FOR EACH [C1, C2] PAIR
                // FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
                I_RelPart rp1 = null;
                for (PositionBI pos : posList) { // FOR EACH PATH POSITION
                    // FIND MOST CURRENT
                    int tmpCountDupl = 0;
                    for (I_RelPart rp : rel.getMutableParts()) {
                        if (isStatedUser && rp.getAuthorNid() == snorocketAuthorNid) {
                            continue; // filter stated vs. inferred
                        } else if (!isStatedUser && rp.getAuthorNid() != snorocketAuthorNid) {
                            continue; // filter stated vs. inferred
                        }
                        if (rp.getPathId() == pos.getPath().getConceptNid()) {
                            if (rp1 == null) {
                                rp1 = rp; // ... KEEP FIRST_INSTANCE PART
                            } else if (rp1.getVersion() < rp.getVersion()) {
                                rp1 = rp; // ... KEEP MORE_RECENT PART
                            } else if (rp1.getVersion() == rp.getVersion()) {
                                // DUPLICATE PART SHOULD NEVER HAPPEN
                                tmpCountDupl++;
                            }
                        }
                    }
                    if (rp1 != null) {
                        if (rp1.getStatusId() == isCURRENT && rp1.getTypeId() == isaNid) {
                            returnSnoRels.add(new SnoRel(rel.getC1Id(), rel.getC2Id(), rp1.getTypeId(), rp1.getGroup(), rel.getNid()));
                        }
                        break; // IF FOUND ON THIS PATH, STOP SEARCHING
                    }
                } // FOR EACH PATH POSITION

            } // FOR EACH [C1, C2] PAIR
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return returnSnoRels;
    }

    /**
     * Does starting at C2 ever end up at C1?<br>
     * If yes, then adding C1-ISA-C2 to C1 will create a cycle.<br>
     */
    public static boolean findIsaCycle(int c1, int type, int c2, boolean isStatedUser) throws Exception {

        if (cEditPath == null) {
            updatePrefs(true);
        }
        if (cEditPath == null) {
            return false;
        }
        if (type != isaNid) {
            return false;
        }

        // Does starting at C2 ever end up a C1?
        // If yes, then C1-Type-C2 will create a cycle.
        int startNid = c2;
        int testNid = c1;

        I_GetConceptData cBean = tf.getConcept(startNid);
        List<PositionBI> posList = cEditPath;

        //
        List<I_GetConceptData> isaCBNext = new ArrayList<I_GetConceptData>();

        List<SnoRel> isaSnoRelProx = findIsaProximal(cBean, posList, isStatedUser);
        while (isaSnoRelProx.size() > 0) {
            // TEST LIST FOR PRIMITIVE OR NOT
            for (SnoRel isaSnoRel : isaSnoRelProx) {
                int theNid = isaSnoRel.c2Id;
                I_GetConceptData isaCB = tf.getConcept(theNid);
                if (theNid == rootNid) { // i.e. not primitive
                    // search no more on this branch
                } else if (theNid == testNid) {

                    return true; // circled back on self
                } else {
                    isaCBNext.add(isaCB); // add to next level search
                }
            }

            // GET ALL NEXT LEVEL RELS FOR NON_PRIMITIVE CONCEPTS
            isaSnoRelProx = new ArrayList<SnoRel>();
            for (I_GetConceptData cbNext : isaCBNext) {
                List<SnoRel> nextSnoRelList = findIsaProximal(cbNext, posList, isStatedUser);
                if (nextSnoRelList.size() > 0) {
                    isaSnoRelProx.addAll(nextSnoRelList);
                }
            }

            // RESET NEXT LEVEL SEARCH LIST
            isaCBNext = new ArrayList<I_GetConceptData>(); // new "next" list
        }
        return false;
    }

    public static int testIsRole(int cid, boolean isStatedUser) {
        try {
            if (prefsNotSet()) {
                updatePrefs(true);
            }
            if (prefsNotSet()) {
                return -1; // can not confirm as role
            }
            int[] parents = {isaNid, roleRootNid};
            if (testIsaChildOf(parents, isaNid, cid, isStatedUser)) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Determine test if concept "is-a" child of given concepts.<br>
     * Does starting at C2 ever end up at C1?<br>
     * If yes, then C2 is a child of at least one of the provided C1.<br>
     */
    public static boolean testIsaChildOf(int[] parents, int type, int c1, boolean isStatedUser)
            throws Exception {

        if (cEditPath == null) {
            updatePrefs(true);
        }
        if (cEditPath == null) {
            return false;
        }
        if (type != isaNid) {
            return false;
        }

        // Does starting at C1 ever end up as child of a PARENT[]?
        int startNid = c1;
        int[] testNids = parents;

        I_GetConceptData cBean = tf.getConcept(startNid);
        List<PositionBI> posList = cEditPath;

        //
        List<I_GetConceptData> isaCBNext = new ArrayList<I_GetConceptData>();

        List<SnoRel> isaSnoRelProx = findIsaProximal(cBean, posList, isStatedUser);
        while (isaSnoRelProx.size() > 0) {
            // TEST LIST FOR PRIMITIVE OR NOT
            for (SnoRel isaSnoRel : isaSnoRelProx) {
                int theNid = isaSnoRel.c2Id;
                I_GetConceptData isaCB = tf.getConcept(theNid);
                boolean isChildOf = false;
                for (int nid : testNids) {
                    if (theNid == nid) {
                        isChildOf = true;
                    }
                }
                if (theNid == rootNid) { // i.e. not primitive
                    // search no more on this branch
                } else if (isChildOf) {
                    return true; // is a child of
                } else {
                    isaCBNext.add(isaCB); // add to next level search
                }
            }

            // GET ALL NEXT LEVEL RELS FOR NON_PRIMITIVE CONCEPTS
            isaSnoRelProx = new ArrayList<SnoRel>();
            for (I_GetConceptData cbNext : isaCBNext) {
                List<SnoRel> nextSnoRelList = findIsaProximal(cbNext, posList, isStatedUser);
                if (nextSnoRelList.size() > 0) {
                    isaSnoRelProx.addAll(nextSnoRelList);
                }
            }

            // RESET NEXT LEVEL SEARCH LIST
            isaCBNext = new ArrayList<I_GetConceptData>(); // new "next" list
        }
        return false;
    }

    private ArrayList<SnoRel> findIsaProximalPrim(I_GetConceptData cBean, List<PositionBI> posList,
            boolean isStatedUser) throws TerminologyException, IOException {

        List<I_GetConceptData> isaCBNext = new ArrayList<I_GetConceptData>();
        ArrayList<SnoRel> isaSnoRelFinal = new ArrayList<SnoRel>();

        List<SnoRel> isaSnoRelProx = findIsaProximal(cBean, posList, isStatedUser);
        while (isaSnoRelProx.size() > 0) {
            // TEST LIST FOR PRIMITIVE OR NOT
            for (SnoRel isaSnoRel : isaSnoRelProx) {
                I_GetConceptData isaCB = tf.getConcept(isaSnoRel.c2Id);
                if (isCDefined(isaCB, posList)) { // i.e. not primitive
                    isaCBNext.add(isaCB); // keep looking for primitive
                } else {
                    int z = 0;
                    while ((z < isaSnoRelFinal.size())
                            && isaSnoRelFinal.get(z).c2Id != isaSnoRel.c2Id) {
                        z++;
                    }
                    // IF NOT_REDUNDANT, THEN ADD
                    if (z == isaSnoRelFinal.size()) {
                        isaSnoRelFinal.add(isaSnoRel); // add to return
                        // primitives list
                    }
                }
            }

            // GET ALL NEXT LEVEL RELS FOR NON_PRIMITIVE CONCEPTS
            isaSnoRelProx = new ArrayList<SnoRel>();
            for (I_GetConceptData cbNext : isaCBNext) {
                List<SnoRel> nextSnoRelList = findIsaProximal(cbNext, posList, isStatedUser);
                if (nextSnoRelList.size() > 0) {
                    isaSnoRelProx.addAll(nextSnoRelList);
                }
            }

            // RESET NEXT LEVEL SEARCH LIST
            isaCBNext = new ArrayList<I_GetConceptData>(); // new "next" list
        }

        return isaSnoRelFinal;
    }

    private boolean isCDefined(I_GetConceptData cBean, List<PositionBI> posList) {

        try {
            I_ConceptAttributeVersioned cv = cBean.getConceptAttributes();
            List<? extends I_ConceptAttributePart> cvList = cv.getMutableParts();
            I_ConceptAttributePart cp1 = null;
            for (PositionBI pos : posList) {
                int tmpCountDupl = 0;
                for (I_ConceptAttributePart cp : cvList) {
                    // FIND MOST RECENT
                    if (cp.getPathId() == pos.getPath().getConceptNid()) {
                        if (cp1 == null) {
                            cp1 = cp; // ... KEEP FIRST_INSTANCE, CURRENT PART
                        } else if (cp1.getVersion() < cp.getVersion()) {
                            cp1 = cp; // ... KEEP MORE_RECENT, CURRENT PART
                        } else if (cp1.getVersion() == cp.getVersion()) {
                            // THIS DUPLICATE SHOULD NEVER HAPPEN
                            tmpCountDupl++;
                        }
                    }
                }
                if (cp1 != null) { // IF FOUND ON THIS PATH, STOP SEARCHING
                    // VERIFICATION STATISTICS
                    if (tmpCountDupl > 1) {
                        countIsCDefinedDuplPart++;
                        countIsCDefinedDuplPartGE2++;
                    } else if (tmpCountDupl == 1) {
                        countIsCDefinedDuplPart++;
                    }
                    return cp1.isDefined();
                }
            }
            return false;
        } catch (IOException e) {
            // EXCEPTION --> cBean.getConceptAttributes() FAILED
            e.printStackTrace();
            return false;
        }
    }

    private SnoGrpList findRoleGrpProximal(I_GetConceptData cBean, List<PositionBI> posList, boolean isStatedUser) {
        SnoGrpList returnSnoGrpList;

        // Find individual proximal roles
        List<SnoRel> roleSnoRelProx = findRoleProximal(cBean, posList, isStatedUser);

        // Get the role groups as groups
        returnSnoGrpList = splitGrouped(roleSnoRelProx, isStatedUser);

        // Get group "0" roles as one group
        SnoGrp tmpGroup0 = splitNonGrouped(roleSnoRelProx, isStatedUser);

        // Convert each group "0" role into its own group of one role each.
        // Add each singleton group to beginning in sort order
        for (int i = tmpGroup0.size() - 1; i >= 0; i--) {
            returnSnoGrpList.add(0, new SnoGrp(tmpGroup0.get(i)));
        }

        return returnSnoGrpList;
    }

    private List<SnoRel> findRoleProximal(I_GetConceptData cBean, List<PositionBI> posList, boolean isStatedUser) {
        ArrayList<SnoRel> returnSnoRels = new ArrayList<SnoRel>();

        try {
            Collection<? extends I_RelVersioned> relList = cBean.getSourceRels();
            for (I_RelVersioned<?> rel : relList) { // FOR EACH [C1, C2] PAIR
                // FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
                I_RelPart rp1 = null;
                for (PositionBI pos : posList) { // FOR EACH PATH POSITION
                    // FIND MOST CURRENT
                    int tmpCountDupl = 0;
                    for (I_RelPart rp : rel.getMutableParts()) {
                        if (isStatedUser && rp.getAuthorNid() == snorocketAuthorNid) {
                            continue; // filter stated vs. inferred
                        } else if (!isStatedUser && rp.getAuthorNid() != snorocketAuthorNid) {
                            continue; // filter stated vs. inferred
                        }
                        if (rp.getPathId() == pos.getPath().getConceptNid()) {
                            if (rp1 == null) {
                                rp1 = rp; // ... KEEP FIRST_INSTANCE PART
                            } else if (rp1.getVersion() < rp.getVersion()) {
                                rp1 = rp; // ... KEEP MORE_RECENT PART
                            } else if (rp1.getVersion() == rp.getVersion()) {
                                // DUPLICATE PART SHOULD NEVER HAPPEN
                                tmpCountDupl++;
                            }
                        }
                    }
                    if (rp1 != null) {
                        if (rp1.getStatusId() == isCURRENT && rp1.getTypeId() != isaNid) {
                            returnSnoRels.add(new SnoRel(rel.getC1Id(), rel.getC2Id(), rp1.getTypeId(), rp1.getGroup(), rel.getNid()));
                        }
                        // VERIFICATION STATISTICS
                        if (tmpCountDupl > 1) {
                            countFindRoleProxDuplPart++;
                            countFindRoleProxDuplPartGE2++;
                        } else if (tmpCountDupl == 1) {
                            countFindRoleProxDuplPart++;
                        }
                        break; // IF FOUND ON THIS PATH, STOP SEARCHING
                    }
                } // FOR EACH PATH POSITION

            } // FOR EACH [C1, C2] PAIR
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return returnSnoRels;
    }

    private SnoGrpList splitGrouped(List<SnoRel> relsIn, boolean isStatedUser) {
        SnoGrpList sg = new SnoGrpList();

        // Step 1: Segment
        List<SnoRel> srl = new ArrayList<SnoRel>();
        for (SnoRel r : relsIn) {
            if (r.group != 0) {
                srl.add(r);
            }
        }

        if (srl.isEmpty()) {
            return sg; // this is an empty set.
        }
        Collections.sort(srl);

        int i = 0;
        int max = srl.size();
        int lastGroupId = srl.get(0).group;
        SnoGrp groupList = new SnoGrp();
        while (i < max) {
            SnoRel thisSnoRel = srl.get(i++);
            if (thisSnoRel.group != lastGroupId) {
                sg.add(groupList); // has been pre-sorted
                groupList = new SnoGrp();
            }
            groupList.add(thisSnoRel);
            lastGroupId = thisSnoRel.group;
        }
        if (groupList.size() > 0) {
            sg.add(groupList);
        }

        // Step 2: Get non-Redundant set
        sg = sg.whichNonRedundant(isStatedUser);

        return sg;
    }

    private SnoGrp splitNonGrouped(List<SnoRel> relsIn, boolean isStatedUser) {
        List<SnoRel> relsOut = new ArrayList<SnoRel>();
        for (SnoRel r : relsIn) {
            if (r.group == 0) {
                relsOut.add(r);
            }
        }
        SnoGrp sgOut = new SnoGrp(relsOut, true);
        sgOut = sgOut.whichRoleValAreNonRedundant(isStatedUser);
        return sgOut; // returns as sorted.
    }

    private SnoGrpList findRoleGrpDiffFromRoot(I_GetConceptData cBean, List<PositionBI> posList, boolean isStatedUser)
            throws TerminologyException, IOException {
        SnoGrpList grpListA;
        SnoGrpList grpListB;

        // GET IMMEDIATE PROXIMAL ROLES OF *THIS*CONCEPT*
        grpListA = findRoleGrpProximal(cBean, posList, isStatedUser);

        // GET PROXIMAL ISAs, one next level up at a time
        List<SnoRel> isaSnoRelNext = findIsaProximal(cBean, posList, isStatedUser);
        List<SnoRel> isaSnoRelNextB = new ArrayList<SnoRel>();
        while (isaSnoRelNext.size() > 0) {

            // FOR EACH CURRENT PROXIMAL CONCEPT...
            for (SnoRel isaSnoRel : isaSnoRelNext) {
                // Get I_GetConceptData (aka ConceptBean) from Nid
                // tf.getConcept may throw an exception
                I_GetConceptData isaCB = tf.getConcept(isaSnoRel.c2Id);

                // ... EVALUATE PROXIMAL ROLES & SEPARATE GROUP
                grpListB = findRoleGrpProximal(isaCB, posList, isStatedUser);

                // KEEP DIFFERENTIATED GROUPS
                // keep what continues to differentiate
                grpListA = grpListA.whichDifferentiateFrom(grpListB, isStatedUser);
                // add anything new which also differentiates
                grpListA.addAll(grpListB.whichDifferentiateFrom(grpListA, isStatedUser));

                // ... GET PROXIMAL ISAs
                isaSnoRelNextB.addAll(findIsaProximal(isaCB, posList, isStatedUser));
            }

            // SETUP NEXT LEVEL OF ISAs
            isaSnoRelNext = isaSnoRelNextB;
            isaSnoRelNextB = new ArrayList<SnoRel>();
        }

        // last check for redundant -- check may not be needed
        grpListA = grpListA.whichNonRedundant(isStatedUser);

        return grpListA;
    }

    private SnoGrpList findRoleGrpDiffFromProx(I_GetConceptData cBean, List<SnoRel> isaList,
            List<PositionBI> posList, boolean isStatedUser) throws TerminologyException, IOException {

        // FIND IMMEDIATE ROLES OF *THIS*CONCEPT*
        SnoGrpList grpListA = findRoleGrpProximal(cBean, posList, isStatedUser);
        if (grpListA.size() == 0) // NOTHING TO DIFFERENTIATE
        {
            return grpListA;
        }

        // FIND NON-REDUNDANT ROLE SET OF PROXIMATE ISA
        SnoGrpList grpListB = new SnoGrpList();
        for (SnoRel isaSnoRel : isaList) {
            I_GetConceptData isaCB = tf.getConcept(isaSnoRel.c2Id);
            SnoGrpList tmpGrpList = findRoleGrpDiffFromRoot(isaCB, posList, isStatedUser);

            // IF EMPTY LIST, SKIP TO NEXT
            if (tmpGrpList.size() == 0) {
                break;
            }

            // keep role-groups which continue to differentiate
            grpListB = grpListB.whichDifferentiateFrom(tmpGrpList, isStatedUser);
            // add anything new which also differentiates
            grpListB.addAll(tmpGrpList.whichDifferentiateFrom(grpListB, isStatedUser));
        }

        // KEEP ONLY ROLES DIFFERENTIATED FROM MOST PROXIMATE
        grpListA = grpListA.whichDifferentiateFrom(grpListB, isStatedUser);

        return grpListA;
    }

    private SnoGrpList findRoleGrpDiffFromProxPrim(I_GetConceptData cBean, List<SnoRel> isaList,
            List<PositionBI> posList, boolean isStatedUser) throws TerminologyException, IOException {

        // FIND ALL NON-REDUNDANT INHERITED ROLES OF *THIS*CONCEPT*
        SnoGrpList grpListA = findRoleGrpDiffFromRoot(cBean, posList, isStatedUser);
        if (grpListA.size() == 0) // NOTHING TO DIFFERENTIATE
        {
            return grpListA;
        }

        // FIND ROLE SET OF MOST PROXIMATE PRIMITIVE
        SnoGrpList grpListB = new SnoGrpList();
        for (SnoRel isaSnoRel : isaList) {
            I_GetConceptData isaCB = tf.getConcept(isaSnoRel.c2Id);
            SnoGrpList tmpGrpList = findRoleGrpDiffFromRoot(isaCB, posList, isStatedUser);

            // IF EMPTY LIST, SKIP TO NEXT
            if (tmpGrpList.size() == 0) {
                break;
            }

            // keep role-groups which continue to differentiate
            grpListB = grpListB.whichDifferentiateFrom(tmpGrpList, isStatedUser);
            // add anything new which also differentiates
            grpListB.addAll(tmpGrpList.whichDifferentiateFrom(grpListB, isStatedUser));
        }

        // KEEP ONLY ROLES DIFFERENTIATED FROM MOST PROXIMATE PRIMITIVE
        grpListA = grpListA.whichDifferentiateFrom(grpListB, isStatedUser);

        return grpListA;
    }

    private void logTable(String[][] data) {
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: TABLE -- \"").append(cBeanStr).append("\" ");
        s.append((cBeanDef == true) ? "(( DEFINED ))" : "(( PRIMITIVE ))");

        s.append("\r\n::: \tSF\tI \tDN\tAN\tSC\tLC");

        for (int i = 0; i < data.length; i++) {
            s.append("\r\n::: ");
            for (int j = 0; j < totalColFlags; j++) {
                if (data[i][j].equalsIgnoreCase(xStr)) {
                    s.append("\tX ");
                } else {
                    s.append("\t ");
                }
            }
            String s1 = data[i][totalColFlags].replace(typeFont, "");
            s1 = s1.replace(valueFont, "");
            s1 = s1.replace("<html>", "");
            s1 = s1.replace("</font>", "");
            s1 = s1.replace("<br>", " || ");
            s.append("\t ").append(s1);
        }
        AceLog.getAppLog().log(Level.INFO, s.toString());
    }

    // This routine is a simple version for check role roots.
    // Expansion would be needed to properly format more complex ancestry.
    static public String toStringIsaAncestry(int conceptNid, List<PositionBI> posList, boolean isStatedUser) {
        String barStr = new String(" || ");
        String commaStr = new String(", ");
        String delimStr = commaStr;
        StringBuilder sb = new StringBuilder(512);
        sb.insert(0, String.valueOf(conceptNid));

        if (tf == null) {
            try {
                updatePrefs(true);
            } catch (Exception e1) {
                e1.printStackTrace();
                return null;
            }
        }

        try {
            // tf.getConcept(int) may create an exception
            I_GetConceptData cBean = tf.getConcept(conceptNid);
            List<I_GetConceptData> isaCBNext = new ArrayList<I_GetConceptData>();

            List<SnoRel> isaSnoRelProx = findIsaProximal(cBean, posList, isStatedUser);
            while (isaSnoRelProx.size() > 0) {

                // ADD THIS LEVEL RELS TO STRING
                if (isaSnoRelProx.size() > 1) {
                    delimStr = barStr;
                }
                for (SnoRel isaSnoRel : isaSnoRelProx) {
                    sb.insert(0, String.valueOf(isaSnoRel.c2Id) + delimStr);
                    delimStr = commaStr;

                    if (isaSnoRel.c2Id != rootNid) { // not root
                        I_GetConceptData isaCB = tf.getConcept(isaSnoRel.c2Id);
                        isaCBNext.add(isaCB); // used to get next level
                    }
                }

                // GET ALL NEXT LEVEL RELS FOR NON_PRIMITIVE CONCEPTS
                isaSnoRelProx = new ArrayList<SnoRel>();
                for (I_GetConceptData cbNext : isaCBNext) {
                    List<SnoRel> nextSnoRelList = findIsaProximal(cbNext, posList, isStatedUser);
                    if (nextSnoRelList.size() > 0) {
                        isaSnoRelProx.addAll(nextSnoRelList);
                    }
                }

                // RESET NEXT LEVEL SEARCH LIST
                isaCBNext = new ArrayList<I_GetConceptData>(); // new "next"
                // list
            }

        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    static public String toString(int nid) {
        StringBuilder sb = new StringBuilder();
        try {
            if (tf == null) {
                updatePrefs(false);
            }
            I_GetConceptData cb = tf.getConcept(nid);
            sb.append(String.valueOf(nid)).append("\t").append(cb.getInitialText()).append("\r\n");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }

    static public String toString(SnoRel sr) {
        StringBuilder sb = new StringBuilder();
        try {
            if (tf == null) {
                updatePrefs(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }
}
