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

import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;

public class SnoRel implements Comparable<Object> {
    public I_RelVersioned relVers; // relId, c1Id, c2Id
    public I_RelPart relPart; // group, type, path, version, refin., character.

    public int relNid;
    public int c1Id; // from I_RelVersioned
    public int c2Id; // from I_RelVersioned
    public int typeId; // from I_RelPart
    public int group; // from I_RelPart

    // SnoRel form a versioned "old" database perspective
    public SnoRel(I_RelVersioned relVers, I_RelPart relPart) {
        this.c1Id = relVers.getC1Id();
        this.c2Id = relVers.getC2Id();
        this.typeId = relPart.getTypeId();
        this.group = relPart.getGroup();
        this.relVers = relVers;
        this.relPart = relPart;
        this.relNid = Integer.MAX_VALUE;
    }

    // SnoRel form a versioned "new" database perspective
    public SnoRel(int c1Id, int c2Id, int roleTypeId, int group, int relNid) {
        this.c1Id = c1Id;
        this.c2Id = c2Id;
        this.typeId = roleTypeId;
        this.group = group;
        this.relVers = null;
        this.relPart = null;
        this.relNid = relNid;
    }

    // SnoRel from a SnoRocket perspective
    public SnoRel(int c1Id, int c2Id, int roleTypeId, int group) {
        this.c1Id = c1Id;
        this.c2Id = c2Id;
        this.typeId = roleTypeId;
        this.group = group;
        this.relVers = null;
        this.relPart = null;
        this.relNid = Integer.MAX_VALUE;
    }

    public int getRelId() {
        return relVers.getRelId();
    }

    public int getPathId() {
        return relPart.getPathId();
    }

    public int getCharacteristicId() {
        return relPart.getCharacteristicId();
    }

    public int getRefinabilityId() {
        return relPart.getRefinabilityId();
    }

    public void setNid(int nid) {
        this.relNid = nid;
    }

    // default sort order [c1-group-type-c2]
    public int compareTo(Object o) {
        SnoRel other = (SnoRel) o;
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
        StringBuilder s = new StringBuilder();
            s.append(relNid + "\t" + c1Id + "\t" + c2Id + "\t" + typeId + "\t" + group );

        return s.toString();
    }

    public String toStringHdr() {
        return "relId     \t" + "c1Id      \t" + "c2Id      \t" + "typeId    \t" + "group";
    }

} // class SnoRel

