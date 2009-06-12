package org.dwfa.ace.refset;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;

import java.io.IOException;
import java.util.UUID;
import java.util.Collection;

public interface RefsetUtil {

    I_ConceptAttributePart getLastestAttributePart(I_GetConceptData refsetConcept) throws IOException;

    I_IntSet createIntSet(I_TermFactory termFactory, Collection<UUID> uuid) throws Exception;

    I_ThinExtByRefPart getLatestVersionIfCurrent(I_ThinExtByRefVersioned ext,
        I_TermFactory termFactory) throws TerminologyException, IOException;

    String getSnomedId(int nid, I_TermFactory termFactory) throws Exception;

    <T> T assertExactlyOne(Collection<T> collection);
}
