/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.mojo.refset.scrub;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.RefsetUtilities;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.refset.scrub.util.CandidateWriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Finds concept extensions that match a particular criteria.<br>
 * This implementation will sweep through the member refsets looking for: <li>
 * Specification extensions <li>Retirement path conflicts (extensions that are
 * not properly retired on all paths)
 */
public class MemberSpecFinder implements ConceptExtFinder {

    /**
     * The name of the file to generate containing a list of all the qualifing
     * concept extensions
     * that are found.
     * 
     * @parameter
     */
    public String reportFile;

    /**
     * Specifies the valid extension concept values that are permitted.
     * Extensions not of this type
     * will be returned by the {@link #iterator()}
     * 
     * @parameter
     */
    public ConceptDescriptor[] validTypeConcepts;

    protected I_TermFactory termFactory;

    protected RefsetHelper refsetHelper;

    private List<Integer> validTypeIds;

    private CandidateWriter candidateWriter;

    public MemberSpecFinder() throws Exception {
        termFactory = LocalVersionedTerminology.get();
        if (termFactory == null) {
            throw new RuntimeException("The LocalVersionedTerminology is not available. Please check the database.");
        }
        refsetHelper = new RefsetHelper(termFactory);
    }

    /**
     * Find any concept extension that has a current version part which does NOT
     * have an
     * valid concept value/type.
     */
    public Iterator<I_ThinExtByRefVersioned> iterator() {
        try {
            candidateWriter = new CandidateWriter(reportFile, termFactory);
            ArrayList<I_ThinExtByRefVersioned> candidates = new ArrayList<I_ThinExtByRefVersioned>();

            for (Integer refsetId : refsetHelper.getSpecificationRefsets()) {

                int memberRefsetId = refsetHelper.getMemberSetConcept(refsetId).getConceptId();
                I_GetConceptData memberSet = refsetHelper.getConcept(memberRefsetId);
                String memberRefsetName = memberSet.getInitialText();
                System.out.println("\nProcessing spec refset: " + memberRefsetName);

                final int CURRENT_STATUS_ID = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()
                    .iterator()
                    .next());

                List<I_ThinExtByRefVersioned> refsetMembers = termFactory.getRefsetExtensionMembers(memberRefsetId);
                for (I_ThinExtByRefVersioned member : refsetMembers) {
                    List<? extends I_ThinExtByRefPart> versions = member.getVersions();
                    for (I_ThinExtByRefPart version : versions) {
                        if (version.getStatus() == CURRENT_STATUS_ID) {
                            if (version instanceof I_ThinExtByRefPartConcept) {
                                int inclusionType = ((I_ThinExtByRefPartConcept) version).getConceptId();
                                if (!isValidType(inclusionType)) {
                                    candidates.add(member);
                                    candidateWriter.logCandidate(memberRefsetName, member);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Found " + candidates.size() + " candidate extensions.");
            return candidates.iterator();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            candidateWriter.close();
        }
    }

    private boolean isValidType(int inclusionType) throws Exception {
        if (validTypeIds == null) {
            validTypeIds = new ArrayList<Integer>();
            for (ConceptDescriptor conceptDesc : validTypeConcepts) {
                validTypeIds.add(conceptDesc.getVerifiedConcept().getId().getNativeId());
            }
        }
        return validTypeIds.contains(Integer.valueOf(inclusionType));
    }

    /**
     * Utilises the {@link RefsetUtilities} class by injecting the db
     */
    private class RefsetHelper extends RefsetUtilities {
        public RefsetHelper(I_TermFactory termFactory) {
            super.termFactory = termFactory;
        }
    }

    private class PartDescription {
        String typeDesc;
        String statusDesc;
        String pathDesc;
    }
}
