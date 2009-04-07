package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.refset.scrub.ConceptExtFinder;
import org.dwfa.mojo.refset.scrub.ConceptExtHandler;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This scrubber removes duplicate "marked parents" changing their status to "retired".
 */
public final class DuplicateMarkedParentScrubber implements ConceptExtHandler {

	/**
	 * TODO: REMOVE.
     * This is not used. This has been introduced to get around a maven problem of not allowing implementations
     * without parameters. Remove once this is sorted out.
	 * @parameter
	 */
	public ConceptDescriptor[] validTypeConcepts;

	private final I_TermFactory termFactory;

	private final int retiredStatusId;

	public DuplicateMarkedParentScrubber() throws Exception {
		termFactory = LocalVersionedTerminology.get();
		retiredStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());
	}

	public void process(final ConceptExtFinder finder) {
		try {
            for (Object aFinder : finder) {
                processExtension((I_ThinExtByRefVersioned) aFinder);
            }
			termFactory.commit();
		} catch (Exception e) {
			throw new RuntimeException("Unable to complete the scrub.", e);
		}
	}

	private void processExtension(final I_ThinExtByRefVersioned member) throws Exception {
        //sort by version, smallest to largest.
        SortedSet<I_ThinExtByRefPart> sortedVersions =  new TreeSet<I_ThinExtByRefPart>(new LatestVersionComparator());
        sortedVersions.addAll(member.getVersions());

        //Get the latest version.
        I_ThinExtByRefPartConcept newPart = (I_ThinExtByRefPartConcept) sortedVersions.last().duplicatePart();
        newPart.setStatus(retiredStatusId);
        newPart.setVersion(Integer.MAX_VALUE);
        member.addVersion(newPart);
        termFactory.addUncommitted(member);
	}
}