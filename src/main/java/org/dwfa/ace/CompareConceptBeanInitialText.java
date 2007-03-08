package org.dwfa.ace;

import java.util.Comparator;

import org.dwfa.vodb.types.I_GetConceptData;

import com.sleepycat.je.DatabaseException;

public class CompareConceptBeanInitialText implements Comparator<I_GetConceptData> {

	public int compare(I_GetConceptData cb1, I_GetConceptData cb2) {
		try {
			return cb1.getInitialText().compareTo(cb2.getInitialText());
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

}
