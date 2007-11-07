package org.dwfa.mojo.compare;

import java.util.List;

public interface CompareOperator {

	public boolean compare(List<Match> matches);
}
