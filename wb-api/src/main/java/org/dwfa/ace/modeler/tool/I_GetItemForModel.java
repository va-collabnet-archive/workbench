package org.dwfa.ace.modeler.tool;

import org.dwfa.ace.api.I_GetConceptData;

public interface I_GetItemForModel {
	Object getItemFromConcept(I_GetConceptData concept)throws Exception;
}
