package org.dwfa.mojo.compare.operators;

import java.util.List;

import org.dwfa.mojo.compare.CompareOperator;
import org.dwfa.mojo.compare.Match;

public class OrOperator implements CompareOperator {

	public List<CompareOperator> operators;
	
	public boolean compare(List<Match> matches) {		
		if (!(operators==null)) {
			for (CompareOperator co : operators) {
				if (co.compare(matches)) {
					return true;
				}
			}
			return false;
		}
		else {
			return false;			
		}
	}

}
