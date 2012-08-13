/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.helper.bdb;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;

/**
 *
 * @author marc
 */
public class MultiEditorContradictionCase {

    private int conceptNid; // concept with contradiction
    private HashSet<Integer> componentNids; //components with contradicion
    private Set<Integer> stamps; //sap nids with contradicion
    private ArrayList<String> cases; // reported cases
    // DETAILS
    private HashMap<UUID, String> authTimeMapComputed; // computed from getAllSapNids()
    private HashMap<UUID, String> authTimeMapMissing; // missing from getAllSapNids()
    private ArrayList<HashSet<UUID>> authTimeSetsList; // editor authTimeHash sets
    private ArrayList<HashSet<UUID>> authTimeSetsTruthList; // adjudication authTimeHash sets

    public MultiEditorContradictionCase(int cNid, ArrayList<String> cases,
            HashSet<Integer> componentNids,
            Set<Integer> stamps) {
        this.conceptNid = cNid;
        this.cases = cases;
        this.componentNids = componentNids;
        this.stamps = stamps;
    }

    public int getConceptNid() {
        return conceptNid;
    }
    
    public HashSet<Integer> getComponentNids() {
        return componentNids;
    }
    
    public Set<Integer> getStamps() {
        return stamps;
    }

    public HashMap<UUID, String> getAuthTimeMapComputed() {
        return authTimeMapComputed;
    }

    public HashMap<UUID, String> getAuthTimeMapMissing() {
        return authTimeMapMissing;
    }

    public ArrayList<HashSet<UUID>> getAuthTimeSetsList() {
        return authTimeSetsList;
    }

    public ArrayList<HashSet<UUID>> getAuthTimeSetsTruthList() {
        return authTimeSetsTruthList;
    }

    public void setAuthTimeMapComputed(HashMap<UUID, String> authTimeMapComputed) {
        this.authTimeMapComputed = authTimeMapComputed;
    }

    public void setAuthTimeMapMissing(HashMap<UUID, String> authTimeMapMissing) {
        this.authTimeMapMissing = authTimeMapMissing;
    }

    public void setAuthTimeSetsList(ArrayList<HashSet<UUID>> authTimeSetsList) {
        this.authTimeSetsList = authTimeSetsList;
    }

    public void setAuthTimeSetsTruthList(ArrayList<HashSet<UUID>> authTimeSetsTruthList) {
        this.authTimeSetsTruthList = authTimeSetsTruthList;
    }

    public ArrayList<String> getCases() {
        return cases;
    }

    @Override
    public String toString() {
        TerminologyStoreDI ts = Ts.get();
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(ts.getConcept(conceptNid).toUserString());
            return sb.toString();
        } catch (IOException ex) {
            Logger.getLogger(MultiEditorContradictionCase.class.getName()).log(Level.SEVERE, null, ex);
            return sb.toString();
        }
    }

    public String toStringLong() {
        TerminologyStoreDI ts = Ts.get();
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("\r\nCASE DETAIL\r\nCONCEPT: ");
            sb.append(ts.getConcept(conceptNid).getPrimUuid().toString());
            sb.append("   ");
            sb.append(ts.getConcept(conceptNid).toUserString());

            sb.append("\r\nRECOMPUTED AuthorTimeHash values");
            ArrayList<String> values = new ArrayList<>();
            for (String s : authTimeMapComputed.values()) {
                values.add(s);
            }
            Collections.sort(values, String.CASE_INSENSITIVE_ORDER);
            for (String s : values) {
                sb.append("\r\n   ");
                sb.append(s);
            }

            sb.append("\r\nUNKNOWN AuthorTimeHash values:");
            for (String s : authTimeMapMissing.values()) {
                sb.append("\r\n   ");
                sb.append(s);
            }

            sb.append("\r\nEDITOR SETS -- CommitRecords:");
            int setCounter = 0;
            for (HashSet<UUID> hs : authTimeSetsList) {
                sb.append("\r\n   AuthorTime HashSet #");
                sb.append(setCounter++);
                for (UUID uuid : hs) {
                    sb.append("\r\n      Value: ");
                    sb.append(uuid.toString());
                }
            }

            sb.append("\r\nADJUDICATION SETS -- AdjudicationRecords:");
            setCounter = 0;
            for (HashSet<UUID> hs : authTimeSetsTruthList) {
                sb.append("\r\n   AuthorTime Adjudiction HashSet #");
                sb.append(setCounter++);
                for (UUID uuid : hs) {
                    sb.append("\r\n      Value: ");
                    sb.append(uuid.toString());
                }
            }

            return sb.toString();
        } catch (IOException ex) {
            Logger.getLogger(MultiEditorContradictionCase.class.getName()).log(Level.SEVERE, null, ex);
            return sb.toString();
        }
    }
}
