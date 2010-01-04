package org.ihtsdo.db.bdb.concept;

public interface I_BindConceptComponents {

	public void setupBinder(Concept enclosingConcept);

	public Concept getEnclosingConcept();

}