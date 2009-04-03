package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.refset.scrub.ConceptExtFinder;
import org.dwfa.mojo.refset.scrub.ConceptExtHandler;

import java.util.Iterator;

/**
 * This scrubber removes duplicate marked parents.
 */
public class DuplicateMarkedParentScrubber implements ConceptExtHandler {

	/**
	 * Specifies the valid concept extension concept values (refset membership types)
	 * @parameter
	 */
	protected ConceptDescriptor[] validTypeConcepts;

	protected I_TermFactory termFactory;

	private int retiredStatusId;

	public DuplicateMarkedParentScrubber() throws Exception {
		termFactory = LocalVersionedTerminology.get();
		retiredStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());
	}

	public void process(final ConceptExtFinder finder) {
		try {
			Iterator<I_ThinExtByRefVersioned> iterator = finder.iterator();
			while  (iterator.hasNext()) {
				processExtension(iterator.next());
			}
			termFactory.commit();
		} catch (Exception e) {
			throw new RuntimeException("Unable to complete the scrub.", e);
		}
	}

	private void processExtension(final I_ThinExtByRefVersioned member) throws Exception {
        //There should be one only version for each member.
        //TODO: clean this up to handle a variable number of versions.
        I_ThinExtByRefPartConcept newPart = (I_ThinExtByRefPartConcept) member.getVersions().get(0).duplicatePart();
        newPart.setStatus(retiredStatusId);
        newPart.setVersion(Integer.MAX_VALUE);
        member.addVersion(newPart);
        termFactory.addUncommitted(member);
	}
}