/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.dwfa.ace.reporting;

import java.util.ArrayList;
import java.util.UUID;
import org.dwfa.ace.api.I_ConfigAceFrame;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.spec.ConceptSpec;

/**
 * 
 * @goal change-report
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class ChangeReport extends ChangeReportBase {
    private int parentConceptNid;

    public ChangeReport(String v1, String v2, String path1_uuid, String path2_uuid,
                boolean added_concepts, boolean deleted_concepts, boolean added_concepts_refex,boolean deleted_concepts_refex,
                boolean changed_concept_status, boolean changed_concept_author, boolean changed_description_author,
                boolean changed_rel_author, boolean changed_refex_author, String author1, String author2, boolean changed_defined,
                boolean added_descriptions, boolean deleted_descriptions, boolean changed_description_status,
                boolean changed_description_term, boolean changed_description_type, boolean changed_description_language,
                boolean changed_description_case, boolean added_relationships, boolean deleted_relationships,
                boolean changed_relationship_status, boolean changed_relationship_characteristic,
                boolean changed_relationship_refinability, boolean changed_relationship_type,
                boolean changed_relationship_group, I_ConfigAceFrame config, int parentConceptNid) {
        super(v1, v2, path1_uuid, path2_uuid,
                added_concepts, deleted_concepts, added_concepts_refex, deleted_concepts_refex,
                changed_concept_status, changed_concept_author, changed_description_author,
                changed_rel_author, changed_refex_author, author1, author2, changed_defined,
                added_descriptions, deleted_descriptions, changed_description_status,
                changed_description_term, changed_description_type, changed_description_language,
                changed_description_case, added_relationships, deleted_relationships,
                changed_relationship_status, changed_relationship_characteristic, changed_relationship_refinability,
                changed_relationship_type, changed_relationship_group, config, parentConceptNid);
        this.parentConceptNid = parentConceptNid;
    }

    @Override
    protected void processConcepts() throws Exception {
        I_TermFactory tf = Terms.get();
        AceLog.getAppLog().info("Getting concepts in DFS order.");
        //ArrayList<Integer> all_concepts = getAllConcepts();
        ArrayList<Integer> all_concepts = getAllConceptsForParent(parentConceptNid);
        AceLog.getAppLog().info("Processing: " + all_concepts.size());
        long beg = System.currentTimeMillis();
        int i = 0;
        for (int id : all_concepts) {
            I_GetConceptData c = tf.getConcept(id);
            i++;
            processConcept(c, i, beg);
        }
    }
}
