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
package org.ihtsdo.mojo.mojo.refset.scrub.markedparents;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.mojo.mojo.ConceptDescriptor;
import org.ihtsdo.mojo.mojo.refset.scrub.ConceptExtFinder;
import org.ihtsdo.mojo.mojo.refset.scrub.ConceptExtHandler;

/**
 * This scrubber removes duplicate "marked parents" changing their status to
 * "retired".
 */
public final class DuplicateMarkedParentScrubber implements ConceptExtHandler {

    /**
     * TODO: REMOVE.
     * This is not used. This has been introduced to get around a maven problem
     * of not allowing implementations
     * without parameters. Remove once this is sorted out.
     * 
     * @parameter
     */
    private ConceptDescriptor[] validTypeConcepts;

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
        // sort by version, smallest to largest.
        SortedSet<I_ThinExtByRefPart> sortedVersions = new TreeSet<I_ThinExtByRefPart>(new LatestVersionComparator());
        sortedVersions.addAll(member.getMutableParts());

        // Get the latest version.
        I_ThinExtByRefPartConcept oldPart = (I_ThinExtByRefPartConcept) sortedVersions.last();
        I_ThinExtByRefPartConcept newPart = (I_ThinExtByRefPartConcept) oldPart.makeAnalog(retiredStatusId, oldPart.getPathId(), Long.MAX_VALUE);
         member.addVersion(newPart);
        termFactory.addUncommitted(member);
    }
}
