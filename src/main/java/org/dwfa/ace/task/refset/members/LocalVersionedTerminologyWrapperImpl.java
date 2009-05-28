package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;

public final class LocalVersionedTerminologyWrapperImpl implements LocalVersionedTerminologyWrapper {

    public I_TermFactory get() {
        return LocalVersionedTerminology.get();
    }
}
