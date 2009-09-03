package org.dwfa.ace.task.status;

import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;

/**
 * @deprecated Use {@link TupleListUtil}
 */
@Deprecated
public class SetStatusUtil {
    
	public static void setStatusOfRelInfo(I_GetConceptData status, List<I_RelTuple> reltuples) {
		for (I_RelTuple rt : reltuples) {
			rt.setStatusId(status.getConceptId());
		}
	}
	
	public static void setStatusOfConceptInfo(I_GetConceptData status, List<I_ConceptAttributeTuple> contuples) {
		for (I_ConceptAttributeTuple cat : contuples) {
			cat.setStatusId(status.getConceptId());
		}
	}

	public static void setStatusOfDescriptionInfo(I_GetConceptData status, List<I_DescriptionTuple> desctuples) {
		for (I_DescriptionTuple dt : desctuples) {
			dt.setStatusId(status.getConceptId());
		}
	}
}
