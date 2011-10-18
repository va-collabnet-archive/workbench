package org.ihtsdo.project.panel.dnd;

import org.dwfa.ace.api.I_GetConceptData;

public interface I_GetItemForModel {
	Object getItemFromConcept(I_GetConceptData concept)throws Exception;
}
