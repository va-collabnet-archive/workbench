package org.ihtsdo.cs;

import java.io.IOException;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.etypes.EConcept;

public interface I_ComputeEConceptForChangeSet {

	public EConcept getEConcept(Concept c) throws IOException;

}