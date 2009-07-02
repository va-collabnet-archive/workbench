package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;

import java.io.IOException;
import java.util.UUID;
import java.util.Collection;
import java.util.List;

public interface RefsetUtil {

	I_ConceptAttributePart getLastestAttributePart(I_GetConceptData refsetConcept) throws IOException;

	I_IntSet createIntSet(I_TermFactory termFactory, Collection<UUID> uuid) throws Exception;

	I_ThinExtByRefPart getLatestVersion(I_ThinExtByRefVersioned ext, I_TermFactory termFactory) throws TerminologyException, IOException;

	String getSnomedId(int nid, I_TermFactory termFactory) throws Exception;

	<T> T assertExactlyOne(Collection<T> collection);

	I_ThinExtByRefPart getLatestVersionIfCurrent(I_ThinExtByRefVersioned ext, I_TermFactory termFactory) throws Exception;

	int getLocalizedParentMarkerNid();

	int getLocalizedConceptExtensionNid() throws Exception;

	int getLocalizedCurrentConceptNid() throws Exception;

	List<I_DescriptionTuple> getDescriptionTuples(final I_GetConceptData concept, I_IntSet allowedStatuses, I_IntSet allowedTypes) throws Exception;

	I_IntSet createCurrentStatus(I_TermFactory termFactory) throws Exception;

	I_IntSet createFullySpecifiedName(I_TermFactory termFactory) throws Exception;

	I_IntSet createPreferredTerm(I_TermFactory termFactory) throws Exception;

	List<I_DescriptionTuple> getFSNDescriptionsForConceptHavingCurrentStatus(I_TermFactory termFactory, int conceptId) throws Exception;

	List<I_DescriptionTuple> getPTDescriptionsForConceptHavingCurrentStatus(I_TermFactory termFactory, int conceptId) throws Exception;
}
