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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.tapi.TerminologyException;

/**
 * SnoTrace gathers SnoRel History in time order an provides the results in a
 * text for for output.
 * 
 * 
 * @author Marc E. Campbell
 * 
 */

public class SnoTrace {
    private I_TermFactory tf = LocalVersionedTerminology.get();;

    // Finds unique Native IDs
    Set<Integer> typeSet = new HashSet<Integer>();
    Set<Integer> cid2Set = new HashSet<Integer>();

    //
    private int max = 100;
    private int cur = 0;
    private SnoRel traceSnoRel[];
    private String traceComment[];
    private String prefixStr = "## ";
    private String name;

    public SnoTrace(int max, String name) {
        // tf = LocalVersionedTerminology.get();
        this.max = max;
        this.name = name;
        traceSnoRel = new SnoRel[max];
        traceComment = new String[max];
    }

    public SnoTrace(int max, String s, I_GetConceptData bean) {
        this.max = max;
        try {
            this.name = s + " for \"" + bean.getInitialText() + "\"";
        } catch (IOException e) {
            this.name = s;
            e.printStackTrace();
        }
        traceSnoRel = new SnoRel[max];
        traceComment = new String[max];
    }

    void update(List<SnoRel> rtlist) {
        update(rtlist, "");
    }

    void update(List<SnoRel> rtlist, String comment) {
        for (SnoRel sr : rtlist)
            update(sr, comment);
    }

    public void update(SnoRel sr, String comment) {
        if (cur < max) {
            typeSet.add(new Integer(sr.typeId)); // role type
            cid2Set.add(new Integer(sr.group)); // role value

            traceSnoRel[cur] = sr;
            traceComment[cur] = prefixStr + comment;

            cur++;
        }
    }

    void update(SnoGrpList sgl) {
        update(sgl, "");
    }

    void update(SnoGrpList sgl, String comment) {
        for (SnoGrp sg : sgl) {
            for (SnoRel sr : sg) {
                if (cur < max) {
                    typeSet.add(new Integer(sr.typeId)); // role_type
                    cid2Set.add(new Integer(sr.c2Id)); // role_value

                    traceSnoRel[cur] = sr;
                    traceComment[cur] = prefixStr + comment;

                    cur++;
                }
            }
        }
    }

    void clear() {
        typeSet.clear();
        cid2Set.clear();

        for (int i = 0; i < max; i++) {
            traceSnoRel[i] = null;
            traceComment[i] = null;
        }
        cur = 0;
    }

    // Native ID, UUID, initialText
    private String toStringNid(int nid) {
        try {
            I_GetConceptData a = tf.getConcept(nid);
            a.getUids().iterator().next().toString();
            String s = nid + "\t" + a.getUids().iterator().next().toString() + "\t" + a.getInitialText();
            return s;
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Most verbose. Includes "everything" (except the nid native id!)
    // Everything: UUIDs, attrib., descr., source rels, images[], uncommitted[]
    private String toStringNidUAB(int nid) {
        try {
            I_GetConceptData a = tf.getConcept(nid);
            UniversalAceBean au = a.getUniversalAceBean();
            return au.toString();
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Several UUIDs related to the immediate concept.
    private String toStringNidUAI(int nid) {
        try {
            I_IdVersioned idv = tf.getId(nid);
            UniversalAceIdentification uai = idv.getUniversal();
            return uai.toString();
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void toStringNidSet() {
        // Initial Text
        StringBuilder s = new StringBuilder();
        s.append("\r\n::: TRACE ROLE_TYPE NIDS");
        for (Integer i : typeSet)
            s.append("\r\n::: \t" + toStringNid(i.intValue()));
        AceLog.getAppLog().log(Level.INFO, s.toString());

        s = new StringBuilder();
        s.append("\r\n::: TRACE ROLE_VALUE NIDS");
        for (Integer i : cid2Set)
            s.append("\r\n::: \t" + toStringNid(i.intValue()));
        AceLog.getAppLog().log(Level.INFO, s.toString());

        if (false) {
            // Universal Bean
            s = new StringBuilder();
            s.append("\r\n::: TRACE ROLE_TYPE UBEAN");
            for (Integer i : typeSet)
                s.append("\r\n::: \r\n" + toStringNidUAB(i.intValue()));
            AceLog.getAppLog().log(Level.INFO, s.toString());

            s = new StringBuilder();
            s.append("\r\n::: TRACE ROLE_VALUE UBEAN");
            for (Integer i : cid2Set)
                s.append("\r\n::: \r\n" + toStringNidUAB(i.intValue()));
            AceLog.getAppLog().log(Level.INFO, s.toString());
        }

        if (false) {
            // Universal ID
            s = new StringBuilder();
            s.append("\r\n::: TRACE ROLE_TYPE UID");
            for (Integer i : typeSet)
                s.append("\r\n::: \r\n" + toStringNidUAI(i.intValue()));
            AceLog.getAppLog().log(Level.INFO, s.toString());

            s = new StringBuilder();
            s.append("\r\n::: TRACE ROLE_VALUE UID");
            for (Integer i : cid2Set)
                s.append("\r\n::: \r\n" + toStringNidUAI(i.intValue()));
            AceLog.getAppLog().log(Level.INFO, s.toString());
        }
    }

    // initialText type:c2id
    private StringBuilder toStringInital(int i, int padTo) {
        StringBuilder s = new StringBuilder(padTo);
        try {
            I_GetConceptData a = tf.getConcept(traceSnoRel[i].typeId);
            I_GetConceptData b = tf.getConcept(traceSnoRel[i].c2Id);
            s.append(a.getInitialText() + ": " + b.getInitialText());
            while (s.length() < padTo)
                s.append(' ');
            return s;
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    // initialText type:c2id
    private int findStringInitalMax() {
        int padTo = 0;
        int a;
        int b;
        try {
            for (int i = 0; i < (cur < max ? cur : max); i++) {
                a = tf.getConcept(traceSnoRel[i].typeId).getInitialText().length();
                b = tf.getConcept(traceSnoRel[i].c2Id).getInitialText().length();
                if ((a + b + 3) > padTo)
                    padTo = (a + b + 3);
            }
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return padTo;
    }

    /**
     * TypeNID, C2NID, GRP, type: cid2, ## comment
     */

    public String toString() {
        StringBuilder s = new StringBuilder(max * 132);
        s.append("\r\n::: TRACE (" + cur + ") -- " + name);
        s.append("\r\n::: \tC1         \tType       \tC2         \tGroup");

        int padTo = findStringInitalMax();

        for (int i = 0; i < (cur < max ? cur : max); i++) {
            s.append("\r\n::: ");
            s.append("\t" + traceSnoRel[i].c1Id);
            s.append("\t" + traceSnoRel[i].typeId);
            s.append("\t" + traceSnoRel[i].c2Id);
            s.append("\t" + traceSnoRel[i].group);
            s.append("\t" + toStringInital(i, padTo));
            s.append("\t" + traceComment[i]);
        }

        return s.toString();
    }

    public void print() {
        AceLog.getAppLog().log(Level.INFO, toString());
    }

}
