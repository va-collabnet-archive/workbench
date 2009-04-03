package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.RefsetUtilities;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.refset.scrub.ConceptExtFinder;
import org.dwfa.mojo.refset.scrub.util.CandidateWriter;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Finds duplicate marked parents.
 */
public final class DuplicateMarkedParentFinder implements ConceptExtFinder {

	/**
	 * The name of the file to generate containing a list of all the qualifing concept extensions
	 * that are found.
	 * @parameter
	 */
	public String reportFile;

	/**
	 * Specifies the valid extension concept values that are permitted. Extensions not of this type
	 * will be returned by the {@link #iterator()}
	 * @parameter
	 */
	public ConceptDescriptor[] validTypeConcepts;


	private I_TermFactory termFactory;

	private RefsetHelper refsetHelper;

    private CandidateWriter candidateWriter;

	private List<Integer> validTypeIds;

    private int currentStatusId;
    private int retiredStatusId;


    public DuplicateMarkedParentFinder() throws Exception {
		termFactory = LocalVersionedTerminology.get();
		if (termFactory == null) {
			throw new RuntimeException("The LocalVersionedTerminology is not available. Please check the database.");
		}

        retiredStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());
        currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());
		refsetHelper = new RefsetHelper(termFactory);
	}

    /**
	 * Find any concept extension that has a current version part which does NOT have an
	 * valid concept value/type.
	 */
	public Iterator<I_ThinExtByRefVersioned> iterator() {
		try {
            injectValidTypeIds();
			candidateWriter = new CandidateWriter(reportFile, termFactory);
            DuplicateFinder duplicateFinder = new DuplicateFinder(currentStatusId);

            for (Integer refsetId : refsetHelper.getSpecificationRefsets()) {
				int memberRefsetId = refsetHelper.getMemberSetConcept(refsetId).getConceptId();
				I_GetConceptData memberSet = refsetHelper.getConcept(memberRefsetId);
				String memberRefsetName = memberSet.getInitialText();
				System.out.println("\nProcessing spec refset: " + memberRefsetName);

				List<I_ThinExtByRefVersioned> refsetMembers = termFactory.getRefsetExtensionMembers(memberRefsetId);
                processRefsetMembers(duplicateFinder, memberRefsetName, refsetMembers);
            }
            List<I_ThinExtByRefVersioned> results = new DuplicateMarkerParentSorter().sort(duplicateFinder.getDuplicates());
            System.out.println("Found " + results.size() + " candidate extensions.");
			return results.iterator();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
            candidateWriter.close();
		}
	}

    private void processRefsetMembers(final DuplicateFinder duplicateFinder, final String memberRefsetName,
                                      final List<I_ThinExtByRefVersioned> refsetMembers) throws Exception {

        for (I_ThinExtByRefVersioned member : refsetMembers) {
            List<? extends I_ThinExtByRefPart> versions = member.getVersions();
            for (I_ThinExtByRefPart version : versions) {
                if (version instanceof I_ThinExtByRefPartConcept && isCurrentOrRetired(version)) {
                    int inclusionType = ((I_ThinExtByRefPartConcept) version).getConceptId();
                    if (isMarkedParent(inclusionType)) {
                        duplicateFinder.put(member);
                        candidateWriter.logCandidate(memberRefsetName, member);
                    }
                }
            }
        }
    }

    private void injectValidTypeIds() throws Exception {
        validTypeIds = new ArrayList<Integer>();
        for (ConceptDescriptor conceptDesc : validTypeConcepts) {
            validTypeIds.add(conceptDesc.getVerifiedConcept().getId().getNativeId());
        }
    }

    private boolean isCurrentOrRetired(final I_ThinExtByRefPart version) {
        return version.getStatus() == currentStatusId || version.getStatus() == retiredStatusId;
    }

    private boolean isMarkedParent(final int inclusionType) throws Exception {
		return validTypeIds.contains(Integer.valueOf(inclusionType));
	}

    /**
	 * Utilises the {@link org.dwfa.ace.refset.RefsetUtilities} class by injecting the db
	 */
	private class RefsetHelper extends RefsetUtilities {
		public RefsetHelper(I_TermFactory termFactory) {
			super.termFactory = termFactory;
		}
	}
}