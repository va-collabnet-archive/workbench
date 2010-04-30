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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;

public class SnoGrp extends ArrayList<SnoRel> {
    private static final long serialVersionUID = 1L;
    public static SnoAB gSnoAB = null;
    private static final boolean debug = false; // :DEBUG:

    public SnoGrp(List<SnoRel> relList, boolean needsToBeSorted) {
        super();
        // set doSort = true if list not pre-sorted to C1-Group-Type-C2 order
        if (needsToBeSorted)
            Collections.sort(relList);
        this.addAll(relList);
        // :TODO:NYI: remove or flag duplicate role-value pairs
        // :TODO:NYI: remove or flag redundant role-value type subsumption
        if (gSnoAB == null)
            gSnoAB = new SnoAB();
    }

    public SnoGrp(SnoRel o) {
        super();
        this.add(o); // 
    }

    public SnoGrp() {
        super();
        if (gSnoAB == null)
            gSnoAB = new SnoAB();
    }

    /**
     * Which role-values in this differentiate from those in B?<br>
     * <br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
     * provide overall computational efficiency.</font>
     * 
     * @param SnoGrp
     * @return
     */
    public SnoGrp whichRoleValAreNonRedundant() {
        StringBuilder s = new StringBuilder("\r\n::: SnoGrp X.whichRoleValAreNonRedundant()");
        if (this.size() == 0) {
            if (debug) {
                s.append("\r\n::: ### EMPTY SET ###");
                AceLog.getAppLog().log(Level.INFO, s.toString());
            }

            return this; // empty set A, effectively "subsumes" everything
        }

        if (debug) {
            for (SnoRel sr : this)
                s.append("\r\n::: THIS:tA,vA\t" + sr.typeId + "\t" + sr.c2Id);
        }

        // Step 1. Remove duplicates.
        // The duplicate removable method requires a pre-sorted group.
        int yA = 0; // current A index
        int yB = 0; // current B index
        SnoGrp sgPass1 = new SnoGrp();
        sgPass1.add(this.get(0));
        while (yA < this.size() - 1) {
            SnoRel rvA = this.get(yA);
            SnoRel rvB = this.get(yA + 1);
            if (rvA.c2Id != rvB.c2Id || rvA.typeId != rvB.typeId) {
                sgPass1.add(rvB); // keeps same order
            } else {
                if (debug) {
                    s.append("\r\n::: DUPL:tA,vA\t" + rvB.typeId + "\t" + rvB.c2Id + "\tDUPLICATE REMOVED - PASS 1");
                }
            }
            yA++;
        }

        // Step 2. Find differentiations
        // Check in the reverse direction.
        int max = sgPass1.size();
        SnoGrp sgPass2 = new SnoGrp();
        for (yA = max - 1; yA > 0; yA--) {
            SnoRel relA = sgPass1.get(yA);
            boolean keep = true;
            for (yB = max - 2; yB >= 0; yB--) {
                // Check differentiation
                SnoRel relB = sgPass1.get(yB);
                if (relA.typeId == relB.typeId)
                    if (gSnoAB.aSubsumesB(relA.c2Id, relB.c2Id)) {
                        keep = false;
                        break;
                    }
                if (relA.typeId != relB.typeId)
                    if (gSnoAB.aSubsumesB(relA.typeId, relB.typeId)) {
                        keep = false;
                        break;
                    }
            }
            if (keep) {
                sgPass2.add(relA); // creates reverse order
            } else {
                if (debug) {
                    s.append("\r\n::: DROP:tA,vA\t" + relA.typeId + "\t" + relA.c2Id + "\tREMOVED - PASS 2");
                }
            }
        }
        sgPass2.add(sgPass1.get(0));

        // Now check again in reverse order direction.
        // Final order will be correct.
        // Duplicates will have been eliminated in the first pass.
        SnoGrp sgPass3 = new SnoGrp();
        max = sgPass2.size();
        for (int ai = max - 1; ai > 0; ai--) {
            SnoRel relA = sgPass2.get(ai);
            boolean keep = true;
            for (int bi = max - 2; bi >= 0; bi--) {
                SnoRel relB = sgPass2.get(bi);
                if (relA.typeId == relB.typeId)
                    if (gSnoAB.aSubsumesB(relA.c2Id, relB.c2Id)) {
                        keep = false;
                        break;
                    }
                if (relA.typeId != relB.typeId)
                    if (gSnoAB.aSubsumesB(relA.typeId, relB.typeId)) {
                        keep = false;
                        break;
                    }
            }
            if (keep) {
                sgPass3.add(relA); // reverses order, to correct order
            } else {
                if (debug) {
                    s.append("\r\n::: DROP:tA,vA\t" + relA.typeId + "\t" + relA.c2Id + "\tREMOVED - PASS 3");
                }
            }
        }
        if (max >= 1)
            sgPass3.add(sgPass2.get(0)); // was fully tested on pass2

        if (debug) {
            for (SnoRel sr : sgPass3)
                s.append("\r\n::: OUT :tA,vA\t" + sr.typeId + "\t" + sr.c2Id);
            if (sgPass3.size() == this.size()) {
                s.append("\r\n::: ### NO NET CHANGE ###");
            } else {
                s.append("\r\n::: ### REMOVED " + (this.size() - sgPass3.size()) + " ###");
            }
            s.append("\r\n::: ### RETURNED == " + sgPass3.size() + " AS NON_REDUNDANT ###");
            AceLog.getAppLog().log(Level.INFO, s.toString());
        }
        return sgPass3;
    }

    /**
     * Which role-values in this differentiate from those in B?<br>
     * <br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
     * provide overall computational efficiency.</font>
     * 
     * @param SnoGrp
     * @return
     */
    public SnoGrp whichRoleValDifferFrom(SnoGrp rg_B) {
        StringBuilder s = new StringBuilder("\r\n::: SnoGrp X.whichRoleValDifferFrom(Y)");

        int maxA = this.size();
        if (maxA == 0) {
            if (debug) {
                s.append("\r\n::: ### EMPTY_SET X IN & RETURNED ###");
                AceLog.getAppLog().log(Level.INFO, s.toString());
            }
            return this; // empty set A, effectively "subsumes" everything
        }

        int maxB = rg_B.size();
        if (maxB == 0) {
            if (debug) {
                s.append("\r\n::: ### Y EMPTY, ALL X DIFFERS ###");
                AceLog.getAppLog().log(Level.INFO, s.toString());
            }
            return this; // A can not subsume empty set B.
        }

        if (debug) {
            int az = 0;
            int bz = 0;
            while (az < maxA && bz < maxB) {
                SnoRel rvA = this.get(az++);
                SnoRel rvB = rg_B.get(bz++);
                s.append("\r\n::: IN:tA-vA-tB-vB\t");
                s.append(rvA.typeId + "\t" + rvA.c2Id + "\t");
                s.append(rvB.typeId + "\t" + rvB.c2Id + "\t");
            }
            while (az < maxA) {
                SnoRel rvA = this.get(az++);
                s.append("\r\n::: IN:tA-vA-tB-vB\t");
                s.append(rvA.typeId + "\t" + rvA.c2Id + "\t" + "\t" + "\t");
            }
            while (bz < maxB) {
                SnoRel rvB = rg_B.get(bz++);
                s.append("\r\n::: IN:tA-vA-tB-vB\t");
                s.append("\t" + "\t" + rvB.typeId + "\t" + rvB.c2Id + "\t");
            }
        }

        SnoGrp returnSnoRels = new SnoGrp();

        SnoRel rvA = this.get(0);
        SnoRel rvB = rg_B.get(0);
        int typeA = rvA.typeId;
        int typeB = rvB.typeId;
        int yA = 0; // current A index
        int zA = 0; // next A index
        while (zA < maxA && this.get(zA).typeId == typeA)
            zA++;
        int yB = 0; // current B index
        int zB = 0; // next B index
        while (zB < maxB && rg_B.get(zB).typeId == typeB)
            zB++;

        while (yA < maxA && yB < maxB) {
            // steps through one role-type at a time
            if (typeA == typeB) {
                for (int i = yA; i < zA; i++) {
                    boolean subsumesAtLeastOneRV = false;
                    for (int j = yB; j < zB; j++) {
                        rvA = this.get(i);
                        rvB = rg_B.get(j);
                        if (debug) {
                            s.append("\r\n::: CHECK rvA rvB:");
                            s.append("\t" + rvA.typeId + "\t" + rvA.c2Id);
                            s.append("\t" + rvB.typeId + "\t" + rvB.c2Id);
                            s.append("\t" + i + "\t" + j);
                            s.append("\tTYPE_A == TYPE_B, CHECK VALUE_A");
                        }
                        if (rvA.c2Id == rvB.c2Id || gSnoAB.aSubsumesB(rvA.c2Id, rvB.c2Id)) {
                            subsumesAtLeastOneRV = true;
                            i = zA;
                            j = zB;
                        }
                    }
                    if (!subsumesAtLeastOneRV) {
                        if (debug) {
                            s.append("\r\n::: KEEP! rvA ---:");
                            s.append("\t" + rvA.typeId + "\t" + rvA.c2Id);
                            s.append("\t___________\t___________");
                            s.append("\t" + i + "\t" + yB);
                            s.append("\tVALUE_A DIFFERENTIATES");
                        }
                        returnSnoRels.add(rvA);
                    }
                }

                // get next role-type group for both
                yA = zA;
                yB = zB;
                while (yA < maxA && zA < maxA && this.get(zA).typeId == this.get(yA).typeId)
                    zA++;
                while (yB < maxB && zB < maxB && rg_B.get(zB).typeId == rg_B.get(yB).typeId)
                    zB++;
            } else if (typeA > typeB) {
                if (debug) {
                    s.append("\r\n::: CHECK rvA rvB:");
                    s.append("\t" + typeA + "\t" + "___________");
                    s.append("\t" + typeB + "\t" + "___________");
                    s.append("\t" + yA + "\t" + yB);
                    s.append("\tTYPE_A > TYPE_B, SKIP TO NEXT TYPE_B");
                }
                // get next role-type for B
                yB = zB;
                while (yB < maxB && zB < maxB && rg_B.get(zB).typeId == rg_B.get(yB).typeId)
                    zB++;
            } else {
                if (debug) {
                    s.append("\r\n::: CHECK rvA rvB:");
                    s.append("\t" + typeA + "\t" + "___________");
                    s.append("\t" + typeB + "\t" + "___________");
                    s.append("\t" + yA + "\t" + yB);
                    s.append("\tTYPE_A < TYPE_B, CHECK TYPE_A");
                }
                // if role-type differentiates, then add all this role-type.
                boolean subsumesAtLeastOneType = false;
                rvA = this.get(yA);
                int j = 0;
                while (j < maxB) {
                    rvB = rg_B.get(j);
                    if (gSnoAB.aSubsumesB(rvA.typeId, rvB.typeId)) {
                        subsumesAtLeastOneType = true;
                        break;
                    }
                    // skip to next B role-type to check
                    int tmpType = rvB.typeId;
                    while (j < maxB && rg_B.get(j).typeId == tmpType)
                        j++;
                }
                if (!subsumesAtLeastOneType)
                    for (int i = yA; i < zA; i++) {
                        rvA = this.get(i);
                        returnSnoRels.add(rvA);
                        if (debug) {
                            s.append("\r\n::: KEEP! rvA ---:");
                            s.append("\t" + rvA.typeId + "\t" + rvA.c2Id);
                            s.append("\t___________\t___________");
                            s.append("\t" + i + "\t" + yB);
                            s.append("\tTYPE_A DIFFERENTIATES");
                        }
                    }

                // get next role-type for A
                yA = zA;
                while (yA < maxA && zA < maxA && this.get(zA).typeId == this.get(yA).typeId)
                    zA++;
            }

            if (yA < maxA)
                typeA = this.get(yA).typeId;
            if (yB < maxB)
                typeB = rg_B.get(yB).typeId;

        }

        while (yA < maxA) {
            // Check for existence of 1 differentiating role-type.
            // Will has no equal and will subsume no role-values in B.
            // The type has no equal by virtue of being in this code branch.
            boolean subsumesAtLeastOneType = false;
            rvA = this.get(yA);
            int j = 0;
            while (j < maxB) {
                rvB = rg_B.get(j);
                if (gSnoAB.aSubsumesB(rvA.typeId, rvB.typeId)) {
                    subsumesAtLeastOneType = true;
                    break;
                }
                int tmpType = rvB.typeId;
                while (j < maxB && rg_B.get(j).typeId == tmpType)
                    j++;
            }
            if (!subsumesAtLeastOneType)
                for (int i = yA; i < zA; i++) {
                    rvA = this.get(i);
                    returnSnoRels.add(rvA);
                    if (debug) {
                        s.append("\r\n::: KEEP+ rvA ---:");
                        s.append("\t" + rvA.typeId + "\t" + rvA.c2Id);
                        s.append("\t___________\t___________");
                        s.append("\t" + i + "\t" + yB);
                        s.append("\tTYPE_A DIFFERENTIATES+");
                    }
                }

            // get next role-type for A
            yA = zA;
            while (yA < maxA && zA < maxA && this.get(zA).typeId == this.get(yA).typeId)
                zA++;
        }

        if (debug) {
            for (SnoRel sr : returnSnoRels)
                s.append("\r\n::: OUT :tA,vA\t" + sr.typeId + "\t" + sr.c2Id);
            s.append("\r\n::: ### RETURNED == " + returnSnoRels.size() + " AS DIFFERERNTIATING ###");
            AceLog.getAppLog().log(Level.INFO, s.toString());
        }

        return returnSnoRels;
    }

    public SnoGrp addAllWithSort(SnoGrp roleGroupB) {

        this.addAll(roleGroupB);
        // SORT BY [ROLE-C2-GROUP-C2]
        Comparator<SnoRel> comp = new Comparator<SnoRel>() {
            public int compare(SnoRel o1, SnoRel o2) {
                int thisMore = 1;
                int thisLess = -1;
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
                        return 0; // EQUAL
                    }
                }
            } // compare()
        };
        Collections.sort(this, comp);

        return this;
    }

    public SnoGrp sortByType() {
        // SORT BY [ROLE-C2-GROUP-C2]
        Comparator<SnoRel> comp = new Comparator<SnoRel>() {
            public int compare(SnoRel o1, SnoRel o2) {
                int thisMore = 1;
                int thisLess = -1;
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
                        return 0; // EQUAL
                    }
                }
            } // compare()
        };
        Collections.sort(this, comp);
        return this;
    }

    /**
     * Does roleGroupA Role-Value match roleGroupB Role-Values?<br>
     * <br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
     * provide overall computational efficiency.</font>
     * 
     * @param roleGroupB
     * @return true iff RoleValuse match
     */
    public boolean equals(SnoGrp roleGroupB) {
        int sizeA = this.size();
        if (sizeA != roleGroupB.size())
            return false; // trivial case, does not have same number of elements

        if (sizeA == 0)
            return true; // trivial case, both empty

        int i = 0;
        boolean isSame = true;
        while (i < sizeA) {
            if (this.get(i).typeId != roleGroupB.get(i).typeId || this.get(i).c2Id != roleGroupB.get(i).c2Id) {
                isSame = false;
                break;
            }
            i++;
        }

        return isSame;
    }

    /**
     * <code>if A.subsumes(B) then A is redundant.<br><br>
     * if Z.subsumes(Y) then Z does not differentiate.<br>
     * <br>
     * Hence, a return value of <font color=#660033><b>true</b>
     * </font> essentially means "DON'T KEEP THIS ROLE_GROUP"
     * <br><br>
     * For ROLE_GROUP_A to not subsume ROLE_GROUP_B, ROLE_GROUP_A must have a 
     * least one ROLE_TYPE or one ROLE_VALUE_PAIR that is not equal to and does 
     * not subsume any of the existing ROLE_TYPEs or ROLE_VALUE_PAIRs in 
     * ROLE_GROUP_B. <br><br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     *  in C1-Group-Type-C2 order for this routine.
	 * Pre-sorting is used to provide overall computational efficiency.</font>
     * 
     * @param relListB
     * @return
     */
    public boolean subsumes(List<SnoRel> rg_B) {
        int maxA = this.size();
        if (maxA == 0)
            return true; // empty set A, effectively "subsumes" everything

        int maxB = rg_B.size();
        if (maxB == 0)
            return false; // A can not subsume empty set B.

        // search for existence of one distinguishing roleTypes or roleValuePair
        StringBuilder s = new StringBuilder("\r\n::: SnoGrp X.subsumes(Y)");
        if (debug) {
            int az = 0;
            int bz = 0;
            while (az < maxA && bz < maxB) {
                SnoRel rvA = this.get(az++);
                SnoRel rvB = rg_B.get(bz++);
                s.append("\r\n::: IN:tA,vA,tB,vB\t");
                s.append(rvA.typeId + "\t" + rvA.c2Id + "\t");
                s.append(rvB.typeId + "\t" + rvB.c2Id + "\t");
            }
            while (az < maxA) {
                SnoRel rvA = this.get(az++);
                s.append("\r\n::: IN:tA,vA,tB,vB\t");
                s.append(rvA.typeId + "\t" + rvA.c2Id + "\t");
                s.append("___________" + "\t" + "___________" + "\t");
            }
            while (bz < maxB) {
                SnoRel rvB = rg_B.get(bz++);
                s.append("\r\n::: IN:tA,vA,tB,vB\t");
                s.append("___________" + "\t" + "___________" + "\t");
                s.append(rvB.typeId + "\t" + rvB.c2Id + "\t");
            }
            s.append("\r\n::: ");
        }

        SnoRel rvA = this.get(0);
        SnoRel rvB = rg_B.get(0);
        int typeA = rvA.typeId;
        int typeB = rvB.typeId;
        int yA = 0; // current A index
        int zA = 0; // next A index
        while (zA < maxA && this.get(zA).typeId == typeA)
            zA++;
        int yB = 0; // current B index
        int zB = 0; // next B index
        while (zB < maxB && rg_B.get(zB).typeId == typeB)
            zB++;

        while (yA < maxA && yB < maxB) {

            if (typeA == typeB) {
                // Check for existence of 1 differentiating role-value.
                // Will have no equal and will subsume no role-values in B.
                boolean subsumesAtLeastOneRV = false;
                for (int i = yA; i < zA; i++)
                    for (int j = yB; j < zB; j++) {
                        rvA = this.get(i);
                        rvB = rg_B.get(j);
                        if (debug) {
                            s.append("\r\n::: CHECK rvA rvB:");
                            s.append("\t" + rvA.typeId + "\t" + rvA.c2Id);
                            s.append("\t" + rvB.typeId + "\t" + rvB.c2Id);
                            s.append("\t" + i + "\t" + j);
                            s.append("\tTYPE_A == TYPE_B, CHECK VALUE_A");
                        }
                        if (rvA.c2Id == rvB.c2Id || gSnoAB.aSubsumesB(rvA.c2Id, rvB.c2Id)) {
                            subsumesAtLeastOneRV = true;
                            i = zA;
                            j = zB;
                        }
                    }
                if (!subsumesAtLeastOneRV) {
                    if (debug) {
                        s.append("\r\n::: \t*** DIFFERENT BY ROLE_VALUE ***\t");
                        s.append("\r\n::: \t");
                        AceLog.getAppLog().log(Level.INFO, s.toString());
                    }
                    return false;
                }

                // get next role-type for both
                yA = zA;
                while (zA < maxA && this.get(zA).typeId == this.get(yA).typeId)
                    zA++;
                yB = zB;
                while (zB < maxB && rg_B.get(zB).typeId == rg_B.get(yB).typeId)
                    zB++;
            } else if (typeA > typeB) {
                // get next role-type for B
                if (debug) {
                    s.append("\r\n::: CHECK rvA rvB:");
                    s.append("\t" + typeA + "\t" + "___________");
                    s.append("\t" + typeB + "\t" + "___________");
                    s.append("\t" + yA + "\t" + yB);
                    s.append("\tTYPE_A > TYPE_B, SKIP TO NEXT TYPE_B");
                }
                yB = zB;
                while (zB < maxB && rg_B.get(zB).typeId == rg_B.get(yB).typeId)
                    zB++;
            } else {
                if (debug) {
                    s.append("\r\n::: CHECK rvA rvB:");
                    s.append("\t" + typeA + "\t" + "___________");
                    s.append("\t" + typeB + "\t" + "___________");
                    s.append("\t" + yA + "\t" + yB);
                    s.append("\tTYPE_A < TYPE_B, CHECK TYPE_A");
                }
                // Check for existence of 1 differentiating role-type.
                // Will has no equal and will subsume no role-values in B.
                // The type has no equal by virtue of being in this code branch.
                boolean subsumesAtLeastOneType = false;
                rvA = this.get(yA);
                int j = 0;
                while (j < maxB) {
                    rvB = rg_B.get(j);
                    if (gSnoAB.aSubsumesB(rvA.typeId, rvB.typeId)) {
                        subsumesAtLeastOneType = true;
                        break;
                    }
                    j++;
                }
                if (!subsumesAtLeastOneType) {
                    if (debug) {
                        s.append("\r\n::: \t*** DIFFERENT BY ROLE_TYPE ***\t");
                        s.append("\r\n::: \t");
                        AceLog.getAppLog().log(Level.INFO, s.toString());
                    }
                    return false;
                }
                // get next role-type for A
                yA = zA;
                while (zA < maxA && this.get(zA).typeId == this.get(yA).typeId)
                    zA++;
            }

            if (yA < maxA)
                typeA = this.get(yA).typeId;
            if (yB < maxB)
                typeB = rg_B.get(yB).typeId;

        }

        while (yA < maxA) {
            // Check for existence of 1 differentiating role-type.
            // Will has no equal and will subsume no role-values in B.
            // The type has no equal by virtue of being in this code branch.
            boolean subsumesAtLeastOneType = false;
            rvA = this.get(yA);
            int j = 0;
            while (j < maxB) {
                rvB = rg_B.get(j);
                if (gSnoAB.aSubsumesB(rvA.typeId, rvB.typeId)) {
                    subsumesAtLeastOneType = true;
                    break;
                }
                j++;
            }
            if (!subsumesAtLeastOneType) {
                if (debug) {
                    s.append("\r\n::: \t *** DIFFERENTIATION BY TYPE ***\t");
                    s.append("\r\n::: \t");
                    AceLog.getAppLog().log(Level.INFO, s.toString());
                }
                return false;
            }

            // get next role-type for A
            yA = zA;
            while (zA < maxA && this.get(zA).typeId == this.get(yA).typeId)
                zA++;
        }

        if (debug) {
            s.append("\r\n::: \t *** X SUBSUMES *** NOT DIFFERENTIATED ***\t");
            s.append("\r\n::: \t");
            AceLog.getAppLog().log(Level.INFO, s.toString());
        }

        // At this point, NO differentiating role-type or role-value exists.
        return true;
    }
} // class SnoGrp
