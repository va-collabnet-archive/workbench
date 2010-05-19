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
package org.dwfa.ace.task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.util.id.Type3UuidFactory;

/**
 * A MatchReviewItem is an input term and a list of description and concept
 * matches. <br>
 * There are three lists. The description string, the concept string, and the
 * concept id.
 * 
 * @author Eric Mays (EKM)
 * 
 */
public class MatchReviewItem {

    private String term;

    private List<String> descriptions;

    private List<String> concepts;

    private List<Long> concept_ids;

    public String getTerm() {
        return term;
    }

    public enum AttachmentKeys {
        UUID_LIST_LIST, HTML_DETAIL, TERM;

        public String getAttachmentKey() {
            return "A: " + this.name();
        }
    }

    /*
     * Create from a string
     */
    public void createFromString(String str) throws Exception {
        BufferedReader br = new BufferedReader(new StringReader(str));
        this.createFromReader(br);
    }

    /*
     * Create from a file
     */
    public void createFromFile(String file_name) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file_name));
        this.createFromReader(br);
    }

    /*
     * The string a file methods call this once they've constructed the reader
     */
    protected void createFromReader(BufferedReader br) throws Exception {
        descriptions = new ArrayList<String>();
        concepts = new ArrayList<String>();
        concept_ids = new ArrayList<Long>();
        String line;
        int i = 0;
        while ((line = br.readLine()) != null) {
            i++;
            // The first line is the input term
            if (i == 1) {
                term = line;
                continue;
            }
            // The remaining lines are description and SCTID pairs. Tab
            // seperated.
            String[] fields = line.split("\t");
            descriptions.add(fields[0]);
            concepts.add(fields[1]);
            concept_ids.add(Long.parseLong(fields[2]));
        }
    }

    /*
     * Get the concepts in the match review as UUIDs
     * 
     * @return A list of list of UUIDs
     */
    public List<List<UUID>> getUuidListList() {
        List<List<UUID>> uuid_list_list = new ArrayList<List<UUID>>();
        for (long concept_id : this.concept_ids) {
            List<UUID> uuids = new ArrayList<UUID>();
            UUID uuid = Type3UuidFactory.fromSNOMED(concept_id);
            uuids.add(uuid);
            uuid_list_list.add(uuids);
        }
        return uuid_list_list;
    }

    /*
     * Construct the HTML for this item. Will be displayed in the queue message
     * and signpost. Contains the inout term followed by a table of matching
     * descriptions and concepts
     * 
     * @return the HTML for the item
     */
    public String getHtml() {
        String html = "";
        html += "<html>";
        html += "<h3>" + this.term + "</h3><table border=\"1\">";
        html += "<tr><th>" + "Description" + "<th>" + "Concept" + "</tr>";
        for (int i = 0; i < this.descriptions.size(); i++) {
            html += "<tr><td>" + descriptions.get(i) + "<td>" + concepts.get(i) + "</tr>";
        }
        html += "</table>";
        return html;
    }

}
