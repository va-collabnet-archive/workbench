package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.refset.members.RefsetUtil;

public final class RefsetExportValidatorImpl implements RefsetExportValidator {

    public void validateIsConceptExtension(final int refsetId,  final RefsetUtil refsetUtil)
            throws RefsetExportValidationException {

        try {
            int conceptExtensionId  = refsetUtil.getLocalizedConceptExtensionNid();
            if (refsetId != conceptExtensionId) {
                throw new RefsetExportValidationException("Refset is not a concept extension.");
            }            
        } catch (Exception e) {
            throw new RefsetExportValidationException("An exception was thrown while validating concept extension.", e);
        }
    }

    public void validateIsCurrent(final I_GetConceptData refsetConcept, final RefsetUtil refsetUtil)
            throws RefsetExportValidationException {
        try {
            int currentStatusId = refsetUtil.getLocalizedCurrentConceptNid();
            I_ConceptAttributePart latestAttributePart = refsetUtil.getLastestAttributePart(refsetConcept);
            if (latestAttributePart == null || latestAttributePart.getStatusId() != currentStatusId) {
                throw new RefsetExportValidationException("Refset concept is not current.");
            }
        } catch (Exception e) {
            throw new RefsetExportValidationException("An exception was thrown while validating current concept.", e);
        }
    }
}
