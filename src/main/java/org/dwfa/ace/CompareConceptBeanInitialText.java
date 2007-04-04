package org.dwfa.ace;

import java.io.IOException;
import java.util.Comparator;

import org.dwfa.ace.api.I_GetConceptData;

public class CompareConceptBeanInitialText implements Comparator<I_GetConceptData> {

	public int compare(I_GetConceptData cb1, I_GetConceptData cb2) {
		try {
			return cb1.getInitialText().compareTo(cb2.getInitialText());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
