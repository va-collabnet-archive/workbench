/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.helper.bdb;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;


/**
 * The Class MultiEditorContradictionCase represents a contradiction on a single
 * concept and contains methods for accessing details about the contradiction. A
 * contradiction is defined as any simultaneous edits on the same concept by two
 * or more editors which were not visible to the other editors at the time of
 * commit.
 *
 * <p>Each component contains a set of all prior author times which were present
 * on the concept at the time of commit plus the author-time associated with the
 * commit of the component. Each set represents what was "seen" by the editor at
 * the time of commit.
 *
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

    /**
     * Instantiates a new multi editor contradiction case for a concept
     * associated with the given
     * <code>conceptNid</code>.
     *
     * @param conceptNid the nid representing the concept in contradiction
     * @param cases an <code>ArrayList</code> of strings representing the time,
     * author, and computed time-author hash
     * @see MultiEditorContradictionDetector#toStringAuthorTime TODO-javaodc:
     * check that this works
     * @param componentNids the nids associated with the contradicting
     * components of the specified concept
     * @param stamps the stamp nids associated with the contradicting components
     * @see StampBI
     */
    public MultiEditorContradictionCase(int conceptNid, ArrayList<String> cases,
            HashSet<Integer> componentNids,
            Set<Integer> stamps) {
        this.conceptNid = conceptNid;
        this.cases = cases;
        this.componentNids = componentNids;
        this.stamps = stamps;
    }

    /**
     * Gets the nid of the concept represented by this contradiction case.
     *
     * @return the concept nid
     */
    public int getConceptNid() {
        return conceptNid;
    }

    /**
     * Gets the nids of the contradicting components from the concept
     * represented by this contradiction case.
     *
     * @return a set containing the nids of the contradicting components
     */
    public HashSet<Integer> getComponentNids() {
        return componentNids;
    }

    /**
     * Gets the stamp nids associated with the contradicting components for this
     * contradiction case.
     *
     * @return a set containing stamp nids from the contradictions
     * @see StampBI
     */
    public Set<Integer> getStamps() {
        return stamps;
    }

    /**
     * Gets a map of component uuids to a string representing the time, author,
     * and computed author-times. The author-time is a type 5 uuid hash of the
     * author uuid plus time associated with each component.
     *
     * @return a map of component uuids to computed author-time
     * @see
     * MultiEditorContradictionDetector#getComputedAthMap(org.ihtsdo.tk.api.concept.ConceptVersionBI,
     * boolean)
     * @see MultiEditorContradictionDetector#toStringAuthorTime(long,
     * org.ihtsdo.tk.api.concept.ConceptChronicleBI, java.util.UUID)
     */
    public HashMap<UUID, String> getAuthTimeMapComputed() {
        return authTimeMapComputed;
    }

    /**
     * Gets a map of compute author-time hashes which were missing from the
     * actual concept. Each author-time is mapped to a string representing a
     * missing author-time.
     *
     * @see MultiEditorContradictionDetector#getMissingAthMap(java.util.HashMap,
     * java.util.ArrayList, java.util.ArrayList)
     * @see
     * MultiEditorContradictionDetector#toStringAuthorTimeMissing(java.util.UUID)
     *
     * @return the auth time map missing
     */
    public HashMap<UUID, String> getAuthTimeMapMissing() {
        return authTimeMapMissing;
    }

    /**
     * A list of author-time sets. Each component contains a set of author times
     * which were present on the concept at the time of commit. Each set
     * represents what was "seen" by the author at the time of commit.
     *
     * @return a list of author-time sets
     */
    public ArrayList<HashSet<UUID>> getAuthTimeSetsList() {
        return authTimeSetsList;
    }

    /**
     * A list of author-time sets from adjudication. Each component contains a
     * set of author times which were present on the concept at the time of
     * commit in the adjudication window. Each set represents what was "seen" by
     * the author at the time of commit in the adjudication window. These sets
     * are taken as "truth" and override a normal set from commit.
     *
     * @return a list of author-time sets
     */
    public ArrayList<HashSet<UUID>> getAuthTimeSetsTruthList() {
        return authTimeSetsTruthList;
    }

    /**
     * Sets the map of component uuids to computed author-times for this
     * contradiction case.
     *
     * @param authTimeMapComputed the computed author-time map
     * @see MultiEditorContradictionCase#getAuthTimeMapComputed()
     */
    public void setAuthTimeMapComputed(HashMap<UUID, String> authTimeMapComputed) {
        this.authTimeMapComputed = authTimeMapComputed;
    }

    /**
     * Sets the map missing of missing author-times for this contradiction case.
     *
     * @param authTimeMapMissing the missing author-time map
     * @see MultiEditorContradictionCase#getAuthTimeMapMissing()
     */
    public void setAuthTimeMapMissing(HashMap<UUID, String> authTimeMapMissing) {
        this.authTimeMapMissing = authTimeMapMissing;
    }

    /**
     * Sets the list of author-time sets present in the concept associated with
     * this contradiction case.
     *
     * @param authTimeSetsList the list of author-time sets
     * @see MultiEditorContradictionCase#getAuthTimeSetsList()
     */
    public void setAuthTimeSetsList(ArrayList<HashSet<UUID>> authTimeSetsList) {
        this.authTimeSetsList = authTimeSetsList;
    }

    /**
     * Sets the list of author-time sets from adjudication present in the
     * concept associated with this contradiction case.
     *
     * @param authTimeSetsTruthList the new auth time sets truth list
     */
    public void setAuthTimeSetsTruthList(ArrayList<HashSet<UUID>> authTimeSetsTruthList) {
        this.authTimeSetsTruthList = authTimeSetsTruthList;
    }

    /**
     * Gets the strings for each contradiction on the concept which contain the time,
     * author, and computed time-author hash
     * @see MultiEditorContradictionDetector#toStringAuthorTime
     *
     * @return a list of strings representing each contradiction
     * @see MultiEditorContradictionCase#t
     */
    public ArrayList<String> getCases() {
        return cases;
    }

    /**
     * Gets a string representation of the concept associated with this contradiction case.
     * 
     * @return a string representing the concept associated with this contradiction case
     * @see ConceptChronicleBI#toUserString()
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
     * Generates a string representing this contradiction case including the concept,
     * the computed author-times, any unknown author-times, editing commit author-time sets, 
     * and adjudication commit author-time sets.
     *
     * @return a string representing this contradiction case
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
