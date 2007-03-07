package org.dwfa.tapi;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface I_ConceptualizeLocally extends I_Conceptualize,
		I_ManifestLocally {
	
	public boolean isPrimitive() throws IOException, TerminologyException;

	public I_DescribeConceptLocally getDescription(List<I_ConceptualizeLocally> typePriorityList) throws IOException, TerminologyException;
	
	public Collection<I_DescribeConceptLocally> getDescriptions() throws IOException, TerminologyException;

	public Collection<I_RelateConceptsLocally> getSourceRels() throws IOException, TerminologyException;
	public Collection<I_RelateConceptsLocally> getSourceRels(
			Collection<I_ConceptualizeLocally> types) throws IOException, TerminologyException;

	public Collection<I_RelateConceptsLocally> getDestRels() throws IOException, TerminologyException;

	public Collection<I_RelateConceptsLocally> getDestRels(
			Collection<I_ConceptualizeLocally> types) throws IOException, TerminologyException;

	public Collection<I_ConceptualizeLocally> getDestRelConcepts()
			throws IOException, TerminologyException;

	public Collection<I_ConceptualizeLocally> getDestRelConcepts(
			Collection<I_ConceptualizeLocally> types) throws IOException, TerminologyException;

	public Collection<I_ConceptualizeLocally> getSrcRelConcepts()
			throws IOException, TerminologyException;

	public Collection<I_ConceptualizeLocally> getSrcRelConcepts(
			Collection<I_ConceptualizeLocally> types) throws IOException, TerminologyException;

	public I_ConceptualizeUniversally universalize() throws IOException, TerminologyException;

}
