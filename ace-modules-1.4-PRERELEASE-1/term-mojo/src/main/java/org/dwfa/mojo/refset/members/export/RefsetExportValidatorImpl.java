/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.mojo.refset.members.export;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;

public final class RefsetExportValidatorImpl implements RefsetExportValidator {

    public void validateIsConceptExtension(final int refsetId, final RefsetUtil refsetUtil)
            throws RefsetExportValidationException {

        try {
            int conceptExtensionId = refsetUtil.getLocalizedConceptExtensionNid();
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
