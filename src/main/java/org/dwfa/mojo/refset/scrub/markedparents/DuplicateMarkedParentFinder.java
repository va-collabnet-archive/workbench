package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.RefsetUtilities;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.refset.scrub.ConceptExtFinder;
import org.dwfa.mojo.refset.scrub.util.CandidateWriter;
import org.dwfa.mojo.refset.scrub.util.TerminologyFactoryUtil;

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

    private DuplicateMarketParentSifter duplicateMarketParentSifter;

    private MarkedParentProcessor markedParentProcessor;

    public DuplicateMarkedParentFinder() throws Exception {
		refsetHelper = new RefsetHelper(new TerminologyFactoryUtil().getTermFactory());

        duplicateMarketParentSifter = new DuplicateMarketParentSifter();
    }

    /**
	 * Finds members which are "marked parents" and which are current.
	 */
	public Iterator<I_ThinExtByRefVersioned> iterator() {
		try {
            injectValidTypeIds();
            markedParentProcessor = new MarkedParentProcessor(candidateWriter, validTypeIds);
            candidateWriter = new CandidateWriter(reportFile, termFactory);

            processRefsets();
            
            List<I_ThinExtByRefVersioned> siftedResults = duplicateMarketParentSifter.sift(markedParentProcessor);
            System.out.println("Found " + siftedResults.size() + " candidate extensions.");
            return siftedResults.iterator();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
            candidateWriter.close();
		}
	}

    private void processRefsets() throws Exception {
        for (Integer refsetId : refsetHelper.getSpecificationRefsets()) {
            int memberRefsetId = refsetHelper.getMemberSetConcept(refsetId).getConceptId();
            I_GetConceptData memberSet = refsetHelper.getConcept(memberRefsetId);
            String memberRefsetName = memberSet.getInitialText();
            System.out.println("\nProcessing spec refset: " + memberRefsetName);

            markedParentProcessor.process(memberRefsetName, termFactory.getRefsetExtensionMembers(memberRefsetId));
        }
    }

    private void injectValidTypeIds() throws Exception {
        validTypeIds = new ArrayList<Integer>();
        for (ConceptDescriptor conceptDesc : validTypeConcepts) {
            validTypeIds.add(conceptDesc.getVerifiedConcept().getId().getNativeId());
        }
    }

//
    /**
	 * Utilises the {@link RefsetUtilities} class by injecting the db
	 */
	private class RefsetHelper extends RefsetUtilities {
        
		public RefsetHelper(final I_TermFactory termFactory) {
			super.termFactory = termFactory;
		}
	}
}