package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.dwfa.ace.task.util.Logger;

public class CommonAPIParameterObjectImpl implements CommonAPIParameterObject {
    
    private final RefsetUtil refsetUtil;
    private final I_TermFactory termFactory;
    private final Logger logger;

    public CommonAPIParameterObjectImpl(final RefsetUtil refsetUtil, final I_TermFactory termFactory,
                                        final Logger logger) {
        this.refsetUtil = refsetUtil;
        this.termFactory = termFactory;
        this.logger = logger;
    }

    public RefsetUtil getRefsetUtil() {
        return refsetUtil;
    }

    public I_TermFactory getTermFactory() {
        return termFactory;
    }

    public Logger getLogger() {
        return logger;
    }
}
