package org.dwfa.tapi;

import java.io.IOException;

public interface I_RelateConceptsLocally extends I_ManifestLocally,
		I_RelateConcepts {
	public I_ConceptualizeLocally getC1() throws IOException, TerminologyException;

	public I_ConceptualizeLocally getC2() throws IOException, TerminologyException;

	public I_ConceptualizeLocally getCharacteristic() throws IOException, TerminologyException;

	public I_ConceptualizeLocally getRefinability() throws IOException, TerminologyException;

	public I_ConceptualizeLocally getRelType() throws IOException, TerminologyException;

	public I_RelateConceptsUniversally universalize() throws IOException, TerminologyException;

}
