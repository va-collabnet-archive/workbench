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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Finds duplicate marked parents which should be "retired".
 */
public final class DuplicateMarkedParentFinder implements ConceptExtFinder {

	/**
	 * The name of the file to generate containing a list of all the qualifing concept extensions
	 * that are found.
	 * @parameter
	 */
	public String reportFile;

	/**
	 * Specifies the list of concepts that are processed.
	 * @parameter
	 */
	public ConceptDescriptor[] validTypeConcepts;


	private I_TermFactory termFactory;

	private RefsetHelper refsetHelper;

    private CandidateWriter candidateWriter;

	private List<Integer> validTypeIds;

    private int currentStatusId;

    private int retiredStatusId;

    private DuplicateMarketParentSifter duplicateMarketParentSifter;

    private List<ComponentRefsetKey> normalMemberList;

    private int normalMemberId;

    private static final String NORMAL_MEMBER_UUID = "cc624429-b17d-4ac5-a69e-0b32448aaf3c";

    private static final String NORMAL_MEMBER_DESC = "normal member";


    public DuplicateMarkedParentFinder() throws Exception {
		termFactory = LocalVersionedTerminology.get();
		if (termFactory == null) {
			throw new RuntimeException("The LocalVersionedTerminology is not available. Please check the database.");
		}

        retiredStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());
        currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());
		refsetHelper = new RefsetHelper(termFactory);

        duplicateMarketParentSifter = new DuplicateMarketParentSifter();
        normalMemberList = new ArrayList<ComponentRefsetKey>();
        injectNormalMemberId(new ConceptDescriptor(NORMAL_MEMBER_UUID, NORMAL_MEMBER_DESC));
    }

    /**
	 * Finds members which are "marked parents" and which are current.
	 */
	public Iterator<I_ThinExtByRefVersioned> iterator() {
		try {
            injectValidTypeIds();
			candidateWriter = new CandidateWriter(reportFile, termFactory);
            DuplicateMarkedParentMarker duplicateMarkedParentMarker = new DuplicateMarkedParentMarker(currentStatusId);

            processRefsets(duplicateMarkedParentMarker);
            List<ComponentRefsetMembers> markedParents = duplicateMarkedParentMarker.getDuplicates();
            List<I_ThinExtByRefVersioned> siftedResults = duplicateMarketParentSifter.sift(normalMemberList, markedParents);
            System.out.println("Found " + siftedResults.size() + " candidate extensions.");

            return siftedResults.iterator();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
            candidateWriter.close();
		}
	}

    private void injectNormalMemberId(final ConceptDescriptor conceptDescriptor) throws Exception {
        normalMemberId = conceptDescriptor.getVerifiedConcept().getId().getNativeId();
    }

    private void processRefsets(final DuplicateMarkedParentMarker duplicateMarkedParentMarker) throws Exception {
        for (Integer refsetId : refsetHelper.getSpecificationRefsets()) {
            int memberRefsetId = refsetHelper.getMemberSetConcept(refsetId).getConceptId();
            I_GetConceptData memberSet = refsetHelper.getConcept(memberRefsetId);
            String memberRefsetName = memberSet.getInitialText();
            System.out.println("\nProcessing spec refset: " + memberRefsetName);

            List<I_ThinExtByRefVersioned> refsetMembers = termFactory.getRefsetExtensionMembers(memberRefsetId);
            processMarkedParentsInRefsetMembers(duplicateMarkedParentMarker, memberRefsetName, refsetMembers);
        }
    }

    private void processMarkedParentsInRefsetMembers(final DuplicateMarkedParentMarker duplicateMarkedParentMarker, final String memberRefsetName,
                                      final List<I_ThinExtByRefVersioned> refsetMembers) throws Exception {

        for (I_ThinExtByRefVersioned member : refsetMembers) {
            List<? extends I_ThinExtByRefPart> versions = member.getVersions();
            for (I_ThinExtByRefPart version : versions) {
                if (version instanceof I_ThinExtByRefPartConcept && isCurrentOrRetired(version)) {
                    int inclusionType = ((I_ThinExtByRefPartConcept) version).getConceptId();
                    if (isMarkedParent(inclusionType)) {
                        duplicateMarkedParentMarker.put(member);
                    }

                    if (isNormalMember(inclusionType)) {
                        normalMemberList.add(new ComponentRefsetKey(member));
                    }

                    candidateWriter.logCandidate(memberRefsetName, member);
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

    private boolean isNormalMember(final int inclusionType) throws Exception {
        return  normalMemberId == inclusionType;
    }

    /**
	 * Utilises the {@link RefsetUtilities} class by injecting the db
	 */
	private class RefsetHelper extends RefsetUtilities {
        
		public RefsetHelper(final I_TermFactory termFactory) {
			super.termFactory = termFactory;
		}
	}
}