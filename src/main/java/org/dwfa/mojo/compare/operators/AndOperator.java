package org.dwfa.mojo.compare.operators;

import java.util.List;

import org.dwfa.mojo.compare.CompareOperator;
import org.dwfa.mojo.compare.Match;

public class AndOperator implements CompareOperator {

	public List<CompareOperator> operators;
	
	public boolean compare(List<Match> matches) {		
		if (!(operators==null)) {
			boolean result = true;
			for (CompareOperator co : operators) {
				result = result && co.compare(matches);
			}
			return result;
		}
		else {
			return false;			
		}
	}

}
