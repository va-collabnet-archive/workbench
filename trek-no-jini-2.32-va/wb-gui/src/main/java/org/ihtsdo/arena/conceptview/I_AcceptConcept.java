package org.ihtsdo.arena.conceptview;

import org.dwfa.ace.api.I_GetConceptData;

public interface I_AcceptConcept {
	public void sendConcept(I_GetConceptData c);
}
