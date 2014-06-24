package org.ihtsdo.concept;


public interface I_BindConceptComponents {

	public Concept getEnclosingConcept();

	public void setupBinder(Concept enclosingConcept);

}