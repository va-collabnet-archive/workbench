package org.dwfa.mojo.refset.spec;

import java.io.File;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.mojo.ConceptDescriptor;

public class RefsetInclusionSpec {

    public File exportFile;
    public ConceptDescriptor refsetConcept;

    public boolean test(I_GetConceptData testConcept) throws Exception {
        if (refsetConcept.getVerifiedConcept().equals(testConcept)) {
            return true;
        } else {
            return false;
        }
    }
}
