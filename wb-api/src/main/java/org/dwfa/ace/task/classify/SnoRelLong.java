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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class SnoRelLong implements Comparable<Object> {

    public long c1Id; // SCTID from I_RelVersioned
    public long c2Id; // SCTID from I_RelVersioned
    public long typeId; // SCTID from I_RelPart
    public int group; // from I_RelPart

    // SnoRel from a SnoRocket perspective
    public SnoRelLong(long c1Id, long c2Id, long roleTypeId, int group) {
        this.c1Id = c1Id;
        this.c2Id = c2Id;
        this.typeId = roleTypeId;
        this.group = group;
    }

    // default sort order [c1-group-type-c2]
    @Override
    public int compareTo(Object o) {
        SnoRelLong other = (SnoRelLong) o;
        int thisMore = 1;
        int thisLess = -1;
        if (this.c1Id > other.c1Id) {
            return thisMore;
        } else if (this.c1Id < other.c1Id) {
            return thisLess;
        } else {
            if (this.group > other.group) {
                return thisMore;
            } else if (this.group < other.group) {
                return thisLess;
            } else {
                if (this.typeId > other.typeId) {
                    return thisMore;
                } else if (this.typeId < other.typeId) {
                    return thisLess;
                } else {
                    if (this.c2Id > other.c2Id) {
                        return thisMore;
                    } else if (this.c2Id < other.c2Id) {
                        return thisLess;
                    } else {
                        return 0; // this == received
                    }
                }
            }
        }
    } // SnoRel.compareTo()

    public String toString() {
        return new String(c1Id + "\t" + c2Id + "\t" + typeId + "\t" + group);
    }

    public String toStringHdr() {
        return "c1Id      \tc2Id      \ttypeId    \tgroup";
    }

    public static void dumpToFile(List<SnoRelLong> srl, String fName) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fName));
        // SCT IDs from Distribution Form
        bw.write("CONCEPTID1\t" + "RELATIONSHIPTYPE\t" + "CONCEPTID2\t" + "RELATIONSHIPGROUP\r\n");
        for (SnoRelLong sr : srl) {
            bw.write(sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id + "\t" + sr.group + "\r\n");
        }
        bw.flush();
        bw.close();
    }
} // class SnoRel

