package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class SnoTable {

    private final static int stateFlag = 0x00000001;
    private final static int inferFlag = 0x00000002;
    private final static int dnfFlag = 0x00000010;
    private final static int anfFlag = 0x00000020;
    private final static int scfFlag = 0x00000040;
    private final static int lcfFlag = 0x00000080;
    private final static int totalColFlags = 6;
    private final static int totalCol = 8;

    //private final static int isaProxFlag = 0x00010000;
    //private final static int isaProxPrimFlag = 0x00020000;
    //private final static int roleProxFlag = 0x00100000;
    //private final static int roleDiffFromRootFlag = 0x00200000;
    //private final static int roleDiffFromProxFlag = 0x00400000;
    //private final static int roleDiffFromProxPrimFlag = 0x00800000;

    // // !!! which forms use
    SnoGrpList isaProxSnoGrpList = null; // USE: Distribution, Authoring Normal
    SnoGrpList isaProxPrimSnoGrpList = null; // USE: Short, Long Canonical

    SnoGrpList roleDiffFromRootList = null;
    SnoGrpList roleDiffFromProxList = null;
    SnoGrpList roleDiffFromProxPrimList = null;

    //
    SnoGrpList isaStatedSnoGrpList = null; // USE: Stated
    SnoGrpList roleStatedSnoGrpList = null; // USE: Stated

    ArrayList<SnoTableRow> snoTableRows = null;
    
    // ** WORKBENCH PARTICULARS **
    private I_TermFactory tf;

    // ** CORE CONSTANTS **
    private static int isaNid;
    private static int isCURRENT = Integer.MIN_VALUE;

    // STATED & INFERRED PATHS
    List<I_Position> cStatedPath = null; // Classifier Stated Path
    List<I_Position> cInferredPath = null; // Classifier Inferred Path

    // ** STATISTICS **
    // !!! :TODO: ??? need reset statistics routine
    private int countFindIsaProxDuplPart = 0;
    private int countFindRoleProxDuplPart = 0;
    private int countFindSelfDuplPart = 0;
    private int countIsCDefinedDuplPart = 0;
    private int countFindIsaProxDuplPartGE2 = 0;
    private int countFindRoleProxDuplPartGE2 = 0;
    private int countFindSelfDuplPartGE2 = 0;
    private int countIsCDefinedDuplPartGE2 = 0;

    // ** :DEBUG: **
    private boolean debug = true;

    @SuppressWarnings("serial")
    private class SnoTableRow extends SnoGrp {
        int flags;
        int rowHeight;

        public SnoTableRow(int flag) {
            flags = flag;
        }

        public SnoTableRow(SnoGrp sg, int flag) {
            for (SnoRel rel : sg)
                this.add(rel);
            flags = flag;
            rowHeight = 18;
        }

        void setFlag(int flag) {
            flags = flags | flag;
        }

    }

    public SnoTable() {
        tf = LocalVersionedTerminology.get();
        try {
            I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

            // Setup core constants
            isaNid = config.getClassifierIsaType().getConceptId();
            isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT
                    .getUids());

            // GET ALL EDIT_PATH ORIGINS
            I_GetConceptData cEditPathObj = config.getClassifierInputPath();
            if (cEditPathObj == null) {
                String errStr = "Classifier Input (Edit) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new Exception(errStr));
                return;
            }

            I_Path cEditIPath = tf.getPath(cEditPathObj.getUids());
            cStatedPath = new ArrayList<I_Position>();
            cStatedPath.add(tf.newPosition(cEditIPath, Integer.MAX_VALUE));
            addPathOrigins(cStatedPath, cEditIPath);

            // GET ALL CLASSIFER_PATH ORIGINS
            I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
            if (cClassPathObj == null) {
                String errStr = "Classifier Output (Inferred) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr,
                        new Exception(errStr));
                return;
            }
            I_Path cClassIPath = tf.getPath(cClassPathObj.getUids());
            cInferredPath = new ArrayList<I_Position>();
            cInferredPath.add(tf.newPosition(cClassIPath, Integer.MAX_VALUE));
            addPathOrigins(cInferredPath, cClassIPath);

        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void addPathOrigins(List<I_Position> origins, I_Path p) {
        origins.addAll(p.getOrigins());
        for (I_Position o : p.getOrigins()) {
            addPathOrigins(origins, o.getPath());
        }
    }

    // gatherFormData(theCBean) normal form data () (move routines here)
    public void gatherFormData(I_GetConceptData cBean) {
        ArrayList<SnoRel> isaProxList = null;
        ArrayList<SnoRel> isaProxPrimList = null;
        ArrayList<SnoRel> isaStatedList = null;
        try {
            
            // GET STATED DATA
            isaStatedList = findIsaProximal(cBean, cStatedPath);
            isaStatedSnoGrpList = new SnoGrpList();
            for (SnoRel sr : isaStatedList)
                isaStatedSnoGrpList.add(new SnoGrp(sr));
            
            List<SnoRel> roleProx = findRoleProximal(cBean, cStatedPath);
            roleStatedSnoGrpList = splitGrouped(roleProx);
            SnoGrp rv0 = splitNonGrouped(roleProx);
            // Add un-grouped to beginning in sort order
            for (int i = rv0.size() - 1; i >= 0; i--) {
                roleStatedSnoGrpList.add(0, new SnoGrp(rv0.get(i)));
            }
            
            // GET INFERRED DATA
            // USE: Distribution Normal, Authoring Normal
            isaProxList = findIsaProximal(cBean, cInferredPath);
            isaProxSnoGrpList = new SnoGrpList();
            for (SnoRel sr : isaProxList)
                isaProxSnoGrpList.add(new SnoGrp(sr));

            // USE: Short Canonical Form, Long Canonical Form
            isaProxPrimList = findIsaProximalPrim(cBean, cInferredPath);
            isaProxPrimSnoGrpList = new SnoGrpList();
            for (SnoRel sr : isaProxPrimList)
                isaProxPrimSnoGrpList.add(new SnoGrp(sr));

            // USE: Distribution Normal Form, Long Canonical Form
            roleDiffFromRootList = findRoleDiffFromRoot(cBean, cInferredPath);

            // USE: Authoring Form
            roleDiffFromProxList = findRoleDiffFromProx(cBean, isaProxList,
                    cInferredPath);

            // USE: Short Canonical Form
            roleDiffFromProxPrimList = findRoleDiffFromProxPrim(cBean,
                    isaProxPrimList, cInferredPath);

        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } // gatherFormData

    // sort SnoGrpList (use SnoGrpList)

    // merge SnoGrpLists with flags

    // build data array
    public SnoGrpList markRows(SnoGrpList grpListA,
            ArrayList<SnoTableRow> rowListB, int flag) {
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

    public String[][] getStringData(I_GetConceptData cBean)
            throws TerminologyException, IOException {
        SnoGrpList diff;
        int flags;
        // Get the Form data component ArrayLists
        gatherFormData(cBean);

        // Create the Table Rows
        snoTableRows = new ArrayList<SnoTableRow>();

        // IS-A
        flags = stateFlag;
        diff = markRows(isaStatedSnoGrpList, snoTableRows, flags);
        for (SnoGrp sg : diff)
            snoTableRows.add(new SnoTableRow(sg, flags));
        
        flags = dnfFlag | anfFlag | inferFlag;
        diff = markRows(isaProxSnoGrpList, snoTableRows, flags);
        for (SnoGrp sg : diff)
            snoTableRows.add(new SnoTableRow(sg, flags));

        flags = lcfFlag | scfFlag | inferFlag;
        diff = markRows(isaProxPrimSnoGrpList, snoTableRows, flags);
        for (SnoGrp sg : diff)
            snoTableRows.add(new SnoTableRow(sg, flags));

        // ROLE VALUES: STATED
        flags = stateFlag;
        diff = markRows(roleStatedSnoGrpList, snoTableRows, flags);
        for (SnoGrp sg : diff)
            snoTableRows.add(new SnoTableRow(sg, flags));        
        
        // ROLE VALUES: Distribution Normal Form, Long Canonical Form
        flags = dnfFlag | lcfFlag | inferFlag;
        diff = markRows(roleDiffFromRootList, snoTableRows, flags);
        for (SnoGrp sg : diff)
            snoTableRows.add(new SnoTableRow(sg, flags));

        // ROLE VALUES: Authoring Form
        flags = anfFlag | inferFlag;
        diff = markRows(roleDiffFromProxList, snoTableRows, flags);
        for (SnoGrp sg : diff)
            snoTableRows.add(new SnoTableRow(sg, flags));

        // ROLE VALUES: Short Canonical Form
        flags = scfFlag | inferFlag;
        diff = markRows(roleDiffFromProxPrimList, snoTableRows, flags);
        for (SnoGrp sg : diff)
            snoTableRows.add(new SnoTableRow(sg, flags));

        // Setup Strings
        String xStr = new String("•");
        String bStr = new String(" ");
        String errStr = new String("*");
        String typeFont = "<font face='Dialog' size='3' color='blue'>";
        String valueFont = "<font face='Dialog' size='3' color='green'>";

        int totalRows = snoTableRows.size();
        String tableStrings[][] = new String[totalRows][totalCol];

        for (int i = 0; i < totalRows; i++) {
            SnoTableRow row = snoTableRows.get(i);
            int snoRelCount = row.size();
            if (snoRelCount <= 0) {
                // empty row
                for (int j = 0; j < 7; j++)
                    tableStrings[i][j] = errStr;
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
            str.append(typeFont + typeBean.getInitialText());
            // VALUE STRING
            I_GetConceptData valueBean = tf.getConcept(sr.c2Id);
            str.append(": </font>" + valueFont + valueBean.getInitialText());

            // Add addition in group
            for (int j = 1; j < snoRelCount; j++) {
                sr = row.get(j);
                // TYPE STRING
                typeBean = tf.getConcept(sr.typeId);
                str
                        .append("</font><br>" + typeFont
                                + typeBean.getInitialText());
                // VALUE STRING
                valueBean = tf.getConcept(sr.c2Id);
                str
                        .append(": </font>" + valueFont
                                + valueBean.getInitialText());
            }
            tableStrings[i][6] = str.toString();
        }

        return tableStrings;
    }

    // show stats

    /**
     * List<SnoRel> --> ArrayList<SnoRel>
     * 
     */

    private ArrayList<SnoRel> findIsaProximal(I_GetConceptData cBean,
            List<I_Position> posList) {
        ArrayList<SnoRel> returnSnoRels = new ArrayList<SnoRel>();
        try {
            List<I_RelVersioned> relList = cBean.getSourceRels();
            for (I_RelVersioned rel : relList) { // FOR EACH [C1, C2] PAIR
                // FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
                I_RelPart rp1 = null;
                for (I_Position pos : posList) { // FOR EACH PATH POSITION
                    // FIND MOST CURRENT
                    int tmpCountDupl = 0;
                    for (I_RelPart rp : rel.getVersions()) {
                        if (rp.getPathId() == pos.getPath().getConceptId()) {
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
                        if (rp1.getStatusId() == isCURRENT
                                && rp1.getTypeId() == isaNid) {
                            returnSnoRels.add(new SnoRel(rel, rp1, -1));
                        }
                        break; // IF FOUND ON THIS PATH, STOP SEARCHING
                    }
                } // FOR EACH PATH POSITION

            } // FOR EACH [C1, C2] PAIR
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return returnSnoRels;
    }

    private ArrayList<SnoRel> findIsaProximalPrim(I_GetConceptData cBean,
            List<I_Position> posList) throws TerminologyException, IOException {

        List<I_GetConceptData> isaCBNext = new ArrayList<I_GetConceptData>();
        ArrayList<SnoRel> isaSnoRelFinal = new ArrayList<SnoRel>();

        List<SnoRel> isaSnoRelProx = findIsaProximal(cBean, posList);
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
                List<SnoRel> nextTuples = findIsaProximal(cbNext, posList);
                if (nextTuples.size() > 0)
                    isaSnoRelProx.addAll(nextTuples);
            }

            // RESET NEXT LEVEL SEARCH LIST
            isaCBNext = new ArrayList<I_GetConceptData>(); // new "next" list
        }

        return isaSnoRelFinal;
    }

    private boolean isCDefined(I_GetConceptData cBean, List<I_Position> posList) {

        try {
            I_ConceptAttributeVersioned cv = cBean.getConceptAttributes();
            List<I_ConceptAttributePart> cvList = cv.getVersions();
            I_ConceptAttributePart cp1 = null;
            for (I_Position pos : posList) {
                int tmpCountDupl = 0;
                for (I_ConceptAttributePart cp : cvList) {
                    // FIND MOST RECENT
                    if (cp.getPathId() == pos.getPath().getConceptId()) {
                        if (cp1 == null) {
                            cp1 = cp; // ... KEEP FIRST_INSTANCE, CURRENT PART
                        } else if (cp1.getVersion() < cp.getVersion()) {
                            cp1 = cp; // ... KEEP MORE_RECENT, CURRENT PART
                        } else if (cp1.getVersion() == cp.getVersion()) {
                            // !!! THIS DUPLICATE SHOULD NEVER HAPPEN
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
            // TODO Auto-generated catch block
            // EXCEPTION --> cBean.getConceptAttributes() FAILED
            e.printStackTrace();
            return false;
        }
    }

    private List<SnoRel> findRoleProximal(I_GetConceptData cBean,
            List<I_Position> posList) {
        ArrayList<SnoRel> returnSnoRels = new ArrayList<SnoRel>();

        try {
            List<I_RelVersioned> relList = cBean.getSourceRels();
            for (I_RelVersioned rel : relList) { // FOR EACH [C1, C2] PAIR
                // FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
                I_RelPart rp1 = null;
                for (I_Position pos : posList) { // FOR EACH PATH POSITION
                    // FIND MOST CURRENT
                    int tmpCountDupl = 0;
                    for (I_RelPart rp : rel.getVersions()) {
                        if (rp.getPathId() == pos.getPath().getConceptId()) {
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
                        if (rp1.getStatusId() == isCURRENT
                                && rp1.getTypeId() != isaNid) {
                            returnSnoRels.add(new SnoRel(rel, rp1, -1));
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
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return returnSnoRels;
    }

    private SnoGrpList findRoleDiffFromRoot(I_GetConceptData cBean,
            List<I_Position> posList) throws TerminologyException, IOException {
        SnoGrpList rgl_A;
        SnoGrpList rgl_B;
        SnoGrp rv_A;
        SnoGrp rv_B;

        // GET IMMEDIATE PROXIMAL ROLES & SEPARATE GROUPS
        List<SnoRel> roleSnoRelProx = findRoleProximal(cBean, posList);
        rgl_A = splitGrouped(roleSnoRelProx);
        rv_A = splitNonGrouped(roleSnoRelProx);

        // GET PROXIMAL ISAs, one next level up at a time
        List<SnoRel> isaSnoRelNext = findIsaProximal(cBean, posList);
        List<SnoRel> isaSnoRelNextB = new ArrayList<SnoRel>();
        while (isaSnoRelNext.size() > 0) {

            // FOR EACH CURRENT PROXIMAL CONCEPT...
            for (SnoRel isaSnoRel : isaSnoRelNext) {
                // Get I_GetConceptData (aka ConceptBean) from Nid
                // tf.getConcept may throw an exception
                I_GetConceptData isaCB = tf.getConcept(isaSnoRel.c2Id);

                // ... EVALUATE PROXIMAL ROLES & SEPARATE GROUP
                roleSnoRelProx = findRoleProximal(isaCB, posList);
                rgl_B = splitGrouped(roleSnoRelProx);
                rv_B = splitNonGrouped(roleSnoRelProx);

                // KEEP DIFFERENTIATED, STAND ALONE, ROLE-VALUE PAIRS
                rv_A = rv_A.whichRoleValDifferFrom(rv_B);
                // setup rv_A for the next iteration
                // add anything new which also differentiates
                rv_A.addAllWithSort(rv_B.whichRoleValDifferFrom(rv_A));

                // KEEP DIFFERENTIATED GROUPS
                // keep what continues to differentiate
                rgl_A = rgl_A.whichDifferentiateFrom(rgl_B);
                // add anything new which also differentiates
                rgl_A.addAll(rgl_B.whichDifferentiateFrom(rgl_A));

                // ... GET PROXIMAL ISAs
                isaSnoRelNextB.addAll(findIsaProximal(isaCB, posList));
            }

            // SETUP NEXT LEVEL OF ISAs
            isaSnoRelNext = isaSnoRelNextB;
            isaSnoRelNextB = new ArrayList<SnoRel>();
        }

        // last check for redundant -- check may not be needed
        rv_A = rv_A.whichRoleValAreNonRedundant();
        rgl_A = rgl_A.whichNonRedundant();

        // Remove any un-grouped which do not differentiate from other groups
        SnoGrpList grpList0 = new SnoGrpList();
        for (SnoRel a : rv_A)
            grpList0.add(new SnoGrp(a));
        grpList0 = grpList0.whichDifferentiateFrom(rgl_A);

        // Add un-grouped to beginning in sort order
        for (int i = grpList0.size() - 1; i >= 0; i--) {
            rgl_A.add(0, grpList0.get(i));
        }

        return rgl_A;
    }

    private SnoGrpList findRoleDiffFromProx(I_GetConceptData cBean,
            List<SnoRel> isaList, List<I_Position> posList)
            throws TerminologyException, IOException {

        // FIND IMMEDIATE ROLES OF *THIS*CONCEPT*
        List<SnoRel> roleSnoRelSetA = findRoleProximal(cBean, posList);
        SnoGrpList grpListA = splitGrouped(roleSnoRelSetA);
        SnoGrp unGrpA = splitNonGrouped(roleSnoRelSetA);

        // FIND NON-REDUNDANT ROLE SET OF PROXIMATE ISA
        SnoGrpList grpListB = new SnoGrpList();
        SnoGrp unGrpB = new SnoGrp();
        for (SnoRel isaSnoRel : isaList) {
            I_GetConceptData isaCB = tf.getConcept(isaSnoRel.c2Id);
            SnoGrpList tmpGrpList = findRoleDiffFromRoot(isaCB, posList);

            // separate un-grouped
            SnoGrp tmpUnGrp;
            if (tmpGrpList.size() > 0)
                tmpUnGrp = tmpGrpList.remove(0);
            else
                break;

            // KEEP DIFFERENTIATED, STAND ALONE, ROLE-VALUE PAIRS
            unGrpB = unGrpB.whichRoleValDifferFrom(tmpUnGrp);
            // setup rv_A for the next iteration
            // add anything new which also differentiates
            unGrpB.addAllWithSort(tmpUnGrp.whichRoleValDifferFrom(unGrpB));

            // keep role-groups which continue to differentiate
            grpListB = grpListB.whichDifferentiateFrom(tmpGrpList);
            // add anything new which also differentiates
            grpListB.addAll(tmpGrpList.whichDifferentiateFrom(grpListB));
        }

        // KEEP ONLY ROLES DIFFERENTIATED FROM MOST PROXIMATE
        unGrpA = unGrpA.whichRoleValDifferFrom(unGrpB);
        grpListA.whichDifferentiateFrom(grpListB);

        // Remove any un-grouped which do not differentiate from other groups
        SnoGrpList grpList0 = new SnoGrpList();
        for (SnoRel a : unGrpA)
            grpList0.add(new SnoGrp(a));
        grpList0 = grpList0.whichDifferentiateFrom(grpListA);

        // Add un-grouped to beginning in sort order
        for (int i = grpList0.size() - 1; i >= 0; i--) {
            grpListA.add(0, grpList0.get(i));
        }

        return grpListA;
    }

    private SnoGrpList findRoleDiffFromProxPrim(I_GetConceptData cBean,
            List<SnoRel> isaList, List<I_Position> posList)
            throws TerminologyException, IOException {

        // FIND ALL NON-REDUNDANT INHERITED ROLES OF *THIS*CONCEPT*
        SnoGrpList grpListA = findRoleDiffFromRoot(cBean, posList);
        // separate un-grouped
        SnoGrp unGrpA;
        if (grpListA.size() > 0)
            unGrpA = grpListA.remove(0);
        else {
            return grpListA;
        }

        // FIND ROLE SET OF MOST PROXIMATE PRIMITIVE
        SnoGrpList grpListB = new SnoGrpList();
        SnoGrp unGrpB = new SnoGrp();
        for (SnoRel isaSnoRel : isaList) {
            I_GetConceptData isaCB = tf.getConcept(isaSnoRel.c2Id);
            SnoGrpList tmpGrpList = findRoleDiffFromRoot(isaCB, posList);

            // separate un-grouped
            SnoGrp tmpUnGrp;
            if (tmpGrpList.size() > 0)
                tmpUnGrp = tmpGrpList.remove(0);
            else
                break;

            // KEEP DIFFERENTIATED, STAND ALONE, ROLE-VALUE PAIRS
            unGrpB = unGrpB.whichRoleValDifferFrom(tmpUnGrp);
            // setup rv_A for the next iteration
            // add anything new which also differentiates
            unGrpB.addAllWithSort(tmpUnGrp.whichRoleValDifferFrom(unGrpB));

            // keep role-groups which continue to differentiate
            grpListB = grpListB.whichDifferentiateFrom(tmpGrpList);
            // add anything new which also differentiates
            grpListB.addAll(tmpGrpList.whichDifferentiateFrom(grpListB));
        }

        // KEEP ONLY ROLES DIFFERENTIATED FROM MOST PROXIMATE PRIMITIVE
        unGrpA = unGrpA.whichRoleValDifferFrom(unGrpB);
        grpListA.whichDifferentiateFrom(grpListB);

        // Remove any un-grouped which do not differentiate from other groups
        SnoGrpList grpList0 = new SnoGrpList();
        for (SnoRel a : unGrpA)
            grpList0.add(new SnoGrp(a));
        grpList0 = grpList0.whichDifferentiateFrom(grpListA);

        // Add un-grouped to beginning in sort order
        for (int i = grpList0.size() - 1; i >= 0; i--) {
            grpListA.add(0, grpList0.get(i));
        }

        return grpListA;
    }

    private SnoGrp splitNonGrouped(List<SnoRel> relsIn) {
        List<SnoRel> relsOut = new ArrayList<SnoRel>();
        for (SnoRel r : relsIn)
            if (r.group == 0)
                relsOut.add(r);
        SnoGrp sgOut = new SnoGrp(relsOut, true);
        sgOut = sgOut.whichRoleValAreNonRedundant();
        return sgOut; // returns as sorted.
    }

    private SnoGrpList splitGrouped(List<SnoRel> relsIn) {
        SnoGrpList sg = new SnoGrpList();

        // Step 1: Segment
        List<SnoRel> srl = new ArrayList<SnoRel>();
        for (SnoRel r : relsIn)
            if (r.group != 0)
                srl.add(r);

        if (srl.size() == 0)
            return sg; // this is an empty set.

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
        if (groupList.size() > 0)
            sg.add(groupList);

        // Step 2: Get non-Redundant set
        sg = sg.whichNonRedundant();

        return sg;
    }

}
