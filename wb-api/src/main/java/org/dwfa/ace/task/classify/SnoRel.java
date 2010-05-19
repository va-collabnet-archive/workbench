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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;


public class SnoRel implements Comparable<Object> {
    public int relNid;
    public int c1Id; // from I_RelVersioned
    public int c2Id; // from I_RelVersioned
    public int typeId; // from I_RelPart
    public int group; // from I_RelPart

    // SnoRel form a versioned "new" database perspective
    public SnoRel(int c1Id, int c2Id, int roleTypeId, int group, int relNid) {
        this.c1Id = c1Id;
        this.c2Id = c2Id;
        this.typeId = roleTypeId;
        this.group = group;
        this.relNid = relNid;
    }

    // SnoRel from a SnoRocket perspective
    public SnoRel(int c1Id, int c2Id, int roleTypeId, int group) {
        this.c1Id = c1Id;
        this.c2Id = c2Id;
        this.typeId = roleTypeId;
        this.group = group;
        this.relNid = Integer.MAX_VALUE;
    }

    public int getRelId() {
        return relNid;
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
        return new String(relNid + "\t" + c1Id + "\t" + c2Id + "\t" + typeId + "\t" + group);
    }

    public String toStringHdr() {
        return "relId     \t" + "c1Id      \t" + "c2Id      \t" + "typeId    \t" + "group";
    }

    public static void dumpToFile(List<SnoRel> srl, String fName, int format) {

        try {
            I_TermFactory tf = Terms.get();

            BufferedWriter bw = new BufferedWriter(new FileWriter(fName));
            if (format == 1) { // RAW NIDs
                for (SnoRel sr : srl) {
                    bw
                            .write(sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id + "\t" + sr.group
                                    + "\r\n");
                }
            }
            if (format == 2) { // UUIDs
                for (SnoRel sr : srl) {
                    I_GetConceptData c1 = tf.getConcept(sr.c1Id);
                    I_GetConceptData t = tf.getConcept(sr.typeId);
                    I_GetConceptData c2 = tf.getConcept(sr.c2Id);
                    int g = sr.group;
                    bw.write(c1.getUids().iterator().next() + "\t" + t.getUids().iterator().next()
                            + "\t" + c2.getUids().iterator().next() + "\t" + g + "\r\n");
                }
            }
            if (format == 3) { // Initial Text
                for (SnoRel sr : srl) {
                    I_GetConceptData c1 = tf.getConcept(sr.c1Id);
                    I_GetConceptData t = tf.getConcept(sr.typeId);
                    I_GetConceptData c2 = tf.getConcept(sr.c2Id);
                    int g = sr.group;
                    bw.write(c1.getInitialText() + "\t" + t.getInitialText() + "\t"
                            + c2.getInitialText() + "\t" + g + "\r\n");
                }
            }
            if (format == 4) { // "FULL": rNID, UUIDs, NIDs, **_index, Initial
                // Text
                int index = 0;
                for (SnoRel sr : srl) {
                    I_GetConceptData c1 = tf.getConcept(sr.c1Id);
                    I_GetConceptData t = tf.getConcept(sr.typeId);
                    I_GetConceptData c2 = tf.getConcept(sr.c2Id);
                    int g = sr.group;
                    bw.write(sr.relNid + "\t" + c1.getUids().iterator().next() + "\t"
                            + t.getUids().iterator().next() + "\t" + c2.getUids().iterator().next()
                            + "\t" + g + "\t");
                    bw.write(sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id + "\t" + sr.group + "\t");
                    bw.write("**_" + index + "\t|");
                    bw.write(c1.getInitialText() + "\t|" + t.getInitialText() + "\t|"
                            + c2.getInitialText() + "\t" + g + "\r\n");
                    index += 1;
                }
            }
            if (format == 5) { // "COMPARE": UUIDs, Initial Text
                Comparator<SnoRel> compDump = new Comparator<SnoRel>() {
                    public int compare(SnoRel o1, SnoRel o2) {
                        int thisMore = 1;
                        int thisLess = -1;
                        if (o1.c2Id > o2.c2Id) {
                            return thisMore;
                        } else if (o1.c2Id < o2.c2Id) {
                            return thisLess;
                        } else {
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
                                    return 0; // this == received
                                }
                            }
                        }
                    } // compare()
                };
                Collections.sort(srl, compDump);

                int index = 0;
                for (SnoRel sr : srl) {
                    I_GetConceptData c1 = tf.getConcept(sr.c1Id);
                    I_GetConceptData t = tf.getConcept(sr.typeId);
                    I_GetConceptData c2 = tf.getConcept(sr.c2Id);
                    bw.write(c1.getUids().iterator().next() + "\t" + t.getUids().iterator().next()
                            + "\t" + c2.getUids().iterator().next() + "\t");
                    bw.write(c1.getInitialText() + "\t|" + t.getInitialText() + "\t|"
                            + c2.getInitialText() + "\r\n");
                    index += 1;
                }
            }
            if (format == 6) { // Distribution Form
                int index = 0;
                bw.write("RELATIONSHIPID\t" + "CONCEPTID1\t" + "RELATIONSHIPTYPE\t"
                        + "CONCEPTID2\t" + "CHARACTERISTICTYPE\t" + "REFINABILITY\t"
                        + "RELATIONSHIPGROUP\r\n");
                for (SnoRel sr : srl) {
                    // RELATIONSHIPID + CONCEPTID1 + RELATIONSHIPTYPE +
                    // CONCEPTID2
                    bw.write("#" + index + "\t" + sr.c1Id + "\t" + sr.typeId + "\t" + sr.c2Id
                            + "\t");
                    // CHARACTERISTICTYPE + REFINABILITY + RELATIONSHIPGROUP
                    bw.write("NA\t" + "NA\t" + sr.group + "\r\n");
                    index += 1;
                }
            }
            bw.flush();
            bw.close();
        } catch (TerminologyException e) {
            // can be caused by tf.getConcept()
            e.printStackTrace();
        } catch (IOException e) {
            // can be caused by new FileWriter
            e.printStackTrace();
        }
    }

} // class SnoRel

