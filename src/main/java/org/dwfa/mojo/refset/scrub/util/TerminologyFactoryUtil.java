package org.dwfa.mojo.refset.scrub.util;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * Utility class to handle Terminology-specific functionality.
 */
public final class TerminologyFactoryUtil {

    public int getNativeConceptId(final ArchitectonicAuxiliary.Concept concept) throws Exception {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        if (termFactory == null) {
            throw new RuntimeException("The LocalVersionedTerminology is not available. Please check the database.");
        }

        return termFactory.uuidToNative(concept.getUids().iterator().next());
    }

    public I_TermFactory getTermFactory() {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        if (termFactory == null) {
            throw new RuntimeException("The LocalVersionedTerminology is not available. Please check the database.");
        }

        return termFactory;
    }

}
