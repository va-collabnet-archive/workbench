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
import java.util.Iterator;
import java.util.List;

public class SnoGrpList extends ArrayList<SnoGrp> {
    private static final long serialVersionUID = 1L;

    public SnoGrpList() {
        super();
    }

    /**
     * Construct a ROLE_GROUP_LIST from <code>List&lt;SnoRel&gt;</code><br>
     * <br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
     * provide overall computational efficiency.</font>
     * 
     */
    public SnoGrpList(List<SnoRel> rels) {
        super();
        // First Group
        SnoGrp group = new SnoGrp();
        this.add(group);

        // First SnoRel in First Group
        Iterator<SnoRel> it = rels.iterator();
        SnoRel snoRelA = it.next();
        group.add(snoRelA);

        while (it.hasNext()) {
            SnoRel snoRelB = it.next();
            if (snoRelB.group == snoRelA.group) {
                group.add(snoRelB); // ADD TO SAME GROUP
            } else {
                group = new SnoGrp(); // CREATE NEW GROUP
                this.add(group); // ADD GROUP TO GROUP LIST
                group.add(snoRelB);
            }
            snoRelA = snoRelB;
        }
    }

    public int countRels() {
        int returnCount = 0;
        for (SnoGrp sg : this)
            returnCount += sg.size();
        return returnCount;
    }

    /**
     * Which group(s) in THIS ROLE_GROUP_LIST are NON-REDUNTANT?<br>
     * <br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
     * provide overall computational efficiency.</font>
     * 
     * @return SnoGrpList
     */
    public SnoGrpList whichNonRedundant() {
        int max = this.size();
        if (max <= 1)
            return this; // trivial case.

        // Check in the reverse direction.
        SnoGrpList sgPass1 = new SnoGrpList();
        for (int ai = max - 1; ai > 0; ai--) {
            SnoGrp groupA = this.get(ai);
            boolean keep = true;
            for (int bi = ai - 1; bi >= 0; bi--) {
                SnoGrp groupB = this.get(bi);
                if (groupA.subsumes(groupB)) {
                    keep = false;
                    break;
                }
            }
            if (keep)
                sgPass1.add(groupA); // creates reverse order
        }
        sgPass1.add(this.get(0));

        // Repeat in reverse order.
        // KEY: Duplicates will have been reduced to singleton in the first pass
        SnoGrpList sgPass2 = new SnoGrpList();
        max = sgPass1.size();
        for (int ai = max - 1; ai > 0; ai--) {
            SnoGrp groupA = sgPass1.get(ai);
            boolean keep = true;
            for (int bi = ai - 1; bi >= 0; bi--) {
                SnoGrp groupB = sgPass1.get(bi);
                if (groupA.subsumes(groupB)) {
                    keep = false;
                    break;
                }
            }
            if (keep)
                sgPass2.add(groupA); // undoes reverse order
        }
        if (max >= 1)
            sgPass2.add(sgPass1.get(0)); // was fully tested on pass1

        return sgPass2;
    }

    /**
     * Which groups in this DIFFERENTIATE from all groups in groupListB?<br>
     * <br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
     * provide overall computational efficiency.</font>
     * 
     * @return SnoGrpList
     */
    public SnoGrpList whichDifferentiateFrom(SnoGrpList groupListB) {
        if (this.size() == 0)
            return this; // trivial case.

        SnoGrpList sg = new SnoGrpList();
        for (SnoGrp groupA : this) {
            boolean keep = true;
            for (SnoGrp groupB : groupListB) {
                if (groupA.subsumes(groupB)) {
                    keep = false;
                    break;
                }
            }
            if (keep)
                sg.add(groupA);
        }

        return sg;
    }

    /**
     * Which groups in this do not have ANY equal group in groupListB?<br>
     * <br>
     * <font color=#990099> IMPLEMENTATION NOTE: roleGroups MUST be pre-sorted
     * in C1-Group-Type-C2 order for this routine. Pre-sorting is used to
     * provide overall computational efficiency.</font>
     * 
     * @param <code>SnoGrpList groupListB</code>
     * @return <code>SnoGrpList</code>
     */
    public SnoGrpList whichNotEqual(SnoGrpList groupListB) {
        SnoGrpList sg = new SnoGrpList();
        for (SnoGrp groupA : this) {
            boolean foundEqual = false;
            for (SnoGrp groupB : groupListB) {
                if (groupA.equals(groupB)) {
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

}
