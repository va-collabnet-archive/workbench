package org.dwfa.mojo.compare.operators;

import java.util.List;

import org.dwfa.mojo.compare.CompareOperator;
import org.dwfa.mojo.compare.Match;

public class ExactlyX implements CompareOperator {
	
	public int x = 2;
	
	public boolean compare(List<Match> matches) {		
		return matches.size()==x;
	}

}
