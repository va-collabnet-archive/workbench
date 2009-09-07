package org.dwfa.ace.api.ebr;

import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.tapi.TerminologyException;

import java.io.IOException;

public interface I_GetExtensionData {

   I_ThinExtByRefVersioned getExtension() throws IOException;

   UniversalAceExtByRefBean getUniversalAceBean() throws TerminologyException, IOException;

   int getMemberId();

    /**
     * Removes this extension from the uncommitted extension cache.
     * @throws IOException If an exception occurs.
     */
    void removeFromCache() throws IOException;
}
