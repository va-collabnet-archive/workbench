package org.ihtsdo.concept;

import org.ihtsdo.db.util.GCValueComponentMap;

public interface I_BindConceptComponents {

	public Concept getEnclosingConcept();

	public void setupBinder(Concept enclosingConcept,
			GCValueComponentMap componentMap);

}