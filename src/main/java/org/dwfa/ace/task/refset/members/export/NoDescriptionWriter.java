package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_GetConceptData;

public interface NoDescriptionWriter extends ExportWriter {

    void write(I_GetConceptData concept) throws Exception;
}
