package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.refset.members.RefsetUtil;

public interface RefsetExportValidator {

    void validateIsConceptExtension(int refsetId, RefsetUtil refsetUtil)
            throws RefsetExportValidationException;

    void validateIsCurrent(I_GetConceptData refsetConcept, RefsetUtil refsetUtil)
            throws RefsetExportValidationException;
}
