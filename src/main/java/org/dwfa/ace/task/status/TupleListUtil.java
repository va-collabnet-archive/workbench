package org.dwfa.ace.task.status;

import java.util.List;

import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_GetConceptData;

/**
 * Utility functions to set an attribute on multiple tuples at once
 */
public class TupleListUtil {

	public static void setStatus(I_GetConceptData status, List<? extends I_AmTuple> tuples) {
		for (I_AmTuple t : tuples) {
			t.setStatusId(status.getConceptId());
		}
	}
	
	public static void setVersion(int version, List<? extends I_AmTuple> tuples) {
        for (I_AmTuple t : tuples) {
            t.setVersion(version);
        }
	}
	
	public static void setPath(int pathId, List<? extends I_AmTuple> tuples) {
        for (I_AmTuple t : tuples) {
            t.setPathId(pathId);
        }
	}
}
