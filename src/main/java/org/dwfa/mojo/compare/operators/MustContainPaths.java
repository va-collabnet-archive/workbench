package org.dwfa.mojo.compare.operators;

import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.compare.CompareOperator;
import org.dwfa.mojo.compare.Match;

public class MustContainPaths implements CompareOperator {
	
	private ConceptDescriptor[] paths;
	
	public boolean compare(List<Match> matches) {

		int numberOfMatches = 0;
		for (Match m : matches) {
			try {
				boolean matched = false;
				for (int i = 0; i < paths.length && !matched; i++) {
					ConceptDescriptor cd = paths[i];
					I_GetConceptData concept = cd.getVerifiedConcept();														
					if (m.getPath1().getPath().getConceptId()==concept.getConceptId() || 
						m.getPath2().getPath().getConceptId()==concept.getConceptId()) {
						// The match occurred on a required path
						matched = true;
						numberOfMatches++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return numberOfMatches==paths.length;
	}

}
