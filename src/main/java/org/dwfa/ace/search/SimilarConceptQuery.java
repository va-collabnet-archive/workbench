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
import org.dwfa.ace.task.search.IsChildOf;
import org.dwfa.ace.task.search.RelSubsumptionMatch;
import org.dwfa.jini.TermEntry;
import org.dwfa.vodb.types.ConceptBean;

public class SimilarConceptQuery {
	
	public static QueryBean make(I_GetConceptData concept, I_ConfigAceFrame config) throws IOException {
		
		List<I_TestSearchResults> extraCriterion = new ArrayList<I_TestSearchResults>();
		// Get text, all unique words...
		TreeSet<String> uniqueWords = new TreeSet<String>();
		for (I_DescriptionVersioned dv: concept.getDescriptions()) {
			for (I_DescriptionPart part: dv.getVersions()) {
				String[] parts = part.getText().toLowerCase().split("\\s+");
				for (String word: parts) {
					uniqueWords.add(word);
				}
			}			
		}
		StringBuffer queryBuff = new StringBuffer();
		for (String word: uniqueWords) {
			queryBuff.append("+");
			queryBuff.append(word);
			queryBuff.append(" ");
		}
		
		for (I_RelTuple rel: concept.getSourceRelTuples(config.getAllowedStatus(), null, config.getViewPositionSet(), true)) {
			// Get "is-a" relationships for is-child-of queries...
			if (config.getSourceRelTypes().contains(rel.getTypeId())) {
				IsChildOf ico = new IsChildOf();
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
