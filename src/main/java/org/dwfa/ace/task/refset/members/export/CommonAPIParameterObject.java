package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.dwfa.ace.task.util.Logger;

public interface CommonAPIParameterObject {
    
    RefsetUtil getRefsetUtil();

    I_TermFactory getTermFactory();

    Logger getLogger();
}
