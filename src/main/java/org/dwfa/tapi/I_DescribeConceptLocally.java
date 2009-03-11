package org.dwfa.tapi;

import java.io.IOException;


public interface I_DescribeConceptLocally extends I_DescribeConcept, I_ManifestLocally {

	public I_ConceptualizeLocally getConcept() throws IOException, TerminologyException;

	public I_ConceptualizeLocally getDescType() throws IOException, TerminologyException;

	public I_ConceptualizeLocally getStatus() throws IOException, TerminologyException;

	public I_DescribeConceptUniversally universalize() throws IOException, TerminologyException;

}
