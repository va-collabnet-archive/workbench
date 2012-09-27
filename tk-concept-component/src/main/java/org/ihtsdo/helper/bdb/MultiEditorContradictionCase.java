/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.helper.bdb;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;

// TODO: Auto-generated Javadoc
/**
 * The Class MultiEditorContradictionCase.
 *
 * @author marc
 */
public class MultiEditorContradictionCase {

    /** The concept nid. */
    private int conceptNid; // concept with contradiction
    
    /** The component nids. */
    private HashSet<Integer> componentNids; //components with contradicion
    
    /** The stamps. */
    private Set<Integer> stamps; //sap nids with contradicion
    
    /** The cases. */
    private ArrayList<String> cases; // reported cases
    // DETAILS
    /** The auth time map computed. */
    private HashMap<UUID, String> authTimeMapComputed; // computed from getAllSapNids()
    
    /** The auth time map missing. */
    private HashMap<UUID, String> authTimeMapMissing; // missing from getAllSapNids()
    
    /** The auth time sets list. */
    private ArrayList<HashSet<UUID>> authTimeSetsList; // editor authTimeHash sets
    
    /** The auth time sets truth list. */
    private ArrayList<HashSet<UUID>> authTimeSetsTruthList; // adjudication authTimeHash sets

    /**
     * Instantiates a new multi editor contradiction case.
     *
     * @param cNid the c nid
     * @param cases the cases
     * @param componentNids the component nids
     * @param stamps the stamps
     */
    public MultiEditorContradictionCase(int cNid, ArrayList<String> cases,
            HashSet<Integer> componentNids,
            Set<Integer> stamps) {
        this.conceptNid = cNid;
        this.cases = cases;
        this.componentNids = componentNids;
        this.stamps = stamps;
    }

    /**
     * Gets the concept nid.
     *
     * @return the concept nid
     */
    public int getConceptNid() {
        return conceptNid;
    }
    
    /**
     * Gets the component nids.
     *
     * @return the component nids
     */
    public HashSet<Integer> getComponentNids() {
        return componentNids;
    }
    
    /**
     * Gets the stamps.
     *
     * @return the stamps
     */
    public Set<Integer> getStamps() {
        return stamps;
    }

    /**
     * Gets the auth time map computed.
     *
     * @return the auth time map computed
     */
    public HashMap<UUID, String> getAuthTimeMapComputed() {
        return authTimeMapComputed;
    }

    /**
     * Gets the auth time map missing.
     *
     * @return the auth time map missing
     */
    public HashMap<UUID, String> getAuthTimeMapMissing() {
        return authTimeMapMissing;
    }

    /**
     * Gets the auth time sets list.
     *
     * @return the auth time sets list
     */
    public ArrayList<HashSet<UUID>> getAuthTimeSetsList() {
        return authTimeSetsList;
    }

    /**
     * Gets the auth time sets truth list.
     *
     * @return the auth time sets truth list
     */
    public ArrayList<HashSet<UUID>> getAuthTimeSetsTruthList() {
        return authTimeSetsTruthList;
    }

    /**
     * Sets the auth time map computed.
     *
     * @param authTimeMapComputed the auth time map computed
     */
    public void setAuthTimeMapComputed(HashMap<UUID, String> authTimeMapComputed) {
        this.authTimeMapComputed = authTimeMapComputed;
    }

    /**
     * Sets the auth time map missing.
     *
     * @param authTimeMapMissing the auth time map missing
     */
    public void setAuthTimeMapMissing(HashMap<UUID, String> authTimeMapMissing) {
        this.authTimeMapMissing = authTimeMapMissing;
    }

    /**
     * Sets the auth time sets list.
     *
     * @param authTimeSetsList the new auth time sets list
     */
    public void setAuthTimeSetsList(ArrayList<HashSet<UUID>> authTimeSetsList) {
        this.authTimeSetsList = authTimeSetsList;
    }

    /**
     * Sets the auth time sets truth list.
     *
     * @param authTimeSetsTruthList the new auth time sets truth list
     */
    public void setAuthTimeSetsTruthList(ArrayList<HashSet<UUID>> authTimeSetsTruthList) {
        this.authTimeSetsTruthList = authTimeSetsTruthList;
    }

    /**
     * Gets the cases.
     *
     * @return the cases
     */
    public ArrayList<String> getCases() {
        return cases;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
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

    /**
     * To string long.
     *
     * @return the string
     */
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
