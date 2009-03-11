package org.dwfa.tapi;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface I_ConceptualizeUniversally extends I_Conceptualize, I_ManifestUniversally {

	public boolean isPrimitive(I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;
	
	public I_DescribeConceptUniversally getDescription(List<I_ConceptualizeUniversally> typePriorityList, I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

	public Collection<I_DescribeConceptUniversally> getDescriptions(I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

	public Collection<I_RelateConceptsUniversally> getSourceRels(I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;
	public Collection<I_RelateConceptsUniversally> getSourceRels(Collection<I_ConceptualizeUniversally> types, 
			I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

	public Collection<I_RelateConceptsUniversally> getDestRels(I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;
	public Collection<I_RelateConceptsUniversally> getDestRels(Collection<I_ConceptualizeUniversally> types, 
			I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

	public Collection<I_ConceptualizeUniversally> getDestRelConcepts(I_StoreUniversalFixedTerminology termStore)
			throws IOException, TerminologyException;

	public Collection<I_ConceptualizeUniversally> getDestRelConcepts(
			Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

	public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(I_StoreUniversalFixedTerminology termStore)
			throws IOException, TerminologyException;

	public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(
			Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) throws IOException, TerminologyException;

	public I_ConceptualizeLocally localize() throws IOException, TerminologyException;

}
