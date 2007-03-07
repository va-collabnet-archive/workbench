package org.dwfa.tapi;

import java.io.IOException;


public interface I_RelateConceptsUniversally extends I_ManifestUniversally,
		I_RelateConcepts {
	public I_ConceptualizeUniversally getC1() throws IOException, TerminologyException;

	public I_ConceptualizeUniversally getC2() throws IOException, TerminologyException;

	public I_ConceptualizeUniversally getCharacteristic() throws IOException, TerminologyException;

	public I_ConceptualizeUniversally getRefinability() throws IOException, TerminologyException;

	public I_ConceptualizeUniversally getRelType() throws IOException, TerminologyException;

	public I_RelateConceptsLocally localize() throws IOException, TerminologyException;

}
