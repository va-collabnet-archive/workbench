package org.dwfa.mojo.compare.operators;

import java.util.HashSet;
import java.util.List;

import org.dwfa.mojo.compare.CompareOperator;
import org.dwfa.mojo.compare.Match;

public class XOrMore implements CompareOperator {

	public int x = 2;
	
	public boolean compare(List<Match> matches) {
		HashSet<Integer> uniqueset = new HashSet<Integer>();
		for (Match m : matches) {
			uniqueset.add(m.getPath1().getPath().getConceptId());
			uniqueset.add(m.getPath2().getPath().getConceptId());
		}
		
		return uniqueset.size()>=x;
	}

}
