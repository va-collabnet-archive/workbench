package org.dwfa.tapi;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


public interface I_StoreLocalFixedTerminology {

	public I_ExtendLocally getExtension(I_ManifestLocally component, I_ConceptualizeLocally extensionType)  throws IOException, TerminologyException;

	public Collection<I_ConceptualizeLocally> getRoots() throws IOException, TerminologyException;
	
	public I_ConceptualizeLocally getConcept(int conceptNid) throws IOException, TerminologyException;
	
	public I_DescribeConceptLocally getDescription(int descriptionNid, int conceptNid) throws IOException, TerminologyException;
	public Collection<I_DescribeConceptLocally> getDescriptionsForConcept(I_ConceptualizeLocally concept) throws IOException, TerminologyException;

	public I_RelateConceptsLocally getRel(int relNid) throws IOException, TerminologyException;
	public Collection<I_RelateConceptsLocally> getSourceRels(I_ConceptualizeLocally source) throws IOException, TerminologyException;
	public Collection<I_RelateConceptsLocally> getDestRels(I_ConceptualizeLocally dest) throws IOException, TerminologyException;
	
	public Collection<I_DescribeConceptLocally> doDescriptionSearch(String[] words) throws IOException, TerminologyException;
	public Collection<I_DescribeConceptLocally> doDescriptionSearch(List<String> words) throws IOException, TerminologyException;

	public Collection<I_ConceptualizeLocally> doConceptSearch(String[] words) throws IOException, TerminologyException;
	public Collection<I_ConceptualizeLocally> doConceptSearch(List<String> words) throws IOException, TerminologyException;

	public int getNid(UUID uid) throws IOException, TerminologyException;
	public int getNid(Collection<UUID> uids) throws IOException, TerminologyException;
	
	public Collection<UUID> getUids(int nid) throws IOException, TerminologyException;

	public Collection<I_ConceptualizeLocally> getExtensionTypes() throws IOException, TerminologyException;
	
}
