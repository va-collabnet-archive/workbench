package org.dwfa.tapi;

import java.io.IOException;


public interface I_DescribeConceptUniversally extends I_DescribeConcept, I_ManifestUniversally {

	public I_ConceptualizeUniversally getConcept() throws IOException, TerminologyException;

	public I_ConceptualizeUniversally getDescType() throws IOException, TerminologyException;

	public I_ConceptualizeUniversally getStatus() throws IOException, TerminologyException;

	public I_DescribeConceptLocally localize() throws IOException, TerminologyException;

}
