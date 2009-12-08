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
package org.dwfa.ace.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.ace.task.search.IsKindOf;
import org.dwfa.ace.task.search.RelSubsumptionMatch;
import org.dwfa.jini.TermEntry;
import org.dwfa.vodb.types.ConceptBean;

public class SimilarConceptQuery {

    public static QueryBean make(I_GetConceptData concept, I_ConfigAceFrame config) throws IOException {

        List<I_TestSearchResults> extraCriterion = new ArrayList<I_TestSearchResults>();
        // Get text, all unique words...
        TreeSet<String> uniqueWords = new TreeSet<String>();
        TreeSet<String> commonWords = new TreeSet<String>();
        for (I_DescriptionVersioned dv : concept.getDescriptions()) {
            TreeSet<String> uniqueDescriptionWords = new TreeSet<String>();
            for (I_DescriptionPart part : dv.getVersions()) {
                String[] parts = part.getText().toLowerCase().split("\\s+");
                for (String word : parts) {
                    if (word.length() > 2 && (word.toLowerCase().equals("the") == false)) {
                        uniqueWords.add(word);
                        uniqueDescriptionWords.add(word);
                    }
                }
            }
            if (commonWords.size() == 0) {
                commonWords.addAll(uniqueDescriptionWords);
            } else {
                commonWords.retainAll(uniqueDescriptionWords);
            }
        }
        StringBuffer queryBuff = new StringBuffer();
        for (String word : commonWords) {
            queryBuff.append("+");
            queryBuff.append(word);
            queryBuff.append(" ");
        }
        uniqueWords.removeAll(commonWords);
        for (String word : uniqueWords) {
            queryBuff.append(word);
            queryBuff.append(" ");
        }

        for (I_RelTuple rel : concept.getSourceRelTuples(config.getAllowedStatus(), null, config.getViewPositionSet(),
            true)) {
            // Get "is-a" relationships for is-child-of queries...
            if (config.getSourceRelTypes().contains(rel.getTypeId())) {
                IsKindOf ico = new IsKindOf();
                ico.setParentTerm(new TermEntry(ConceptBean.get(rel.getC2Id()).getUids()));
                extraCriterion.add(ico);
            } else {
                // Other rels for rel-type queries...
                RelSubsumptionMatch rsm = new RelSubsumptionMatch();
                rsm.setRelRestrictionTerm(new TermEntry(ConceptBean.get(rel.getC2Id()).getUids()));
                rsm.setRelTypeTerm(new TermEntry(ConceptBean.get(rel.getTypeId()).getUids()));
                extraCriterion.add(rsm);
            }
        }
        return new QueryBean(queryBuff.toString(), extraCriterion);
    }
}
