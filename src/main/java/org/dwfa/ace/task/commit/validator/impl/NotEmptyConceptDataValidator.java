/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.ace.task.commit.validator.impl;

import org.dwfa.ace.task.commit.validator.GetConceptDataValidationStrategy;
import java.util.List;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.commit.validator.ValidationException;

/**
 * {@code NotEmptyConceptDataValidator} is an implementation of {@link GetConceptDataValidationStrategy}
 * that verifys a that the text of an {@link I_DescriptionPart} is not null or has a length greater than zero.
 * <br>
 * This is an implementation of the Strategy Pattern to allow interchanging of {@link GetConceptDataValidationStrategy}
 * objects to be used for different types of validation.
 * <br>
 * If an {@code I_GetConceptData} object has no descriptions, the validate method will pass through and not return any
 * errors as there was not description parts to check.
 *
 * @author Matthew Edwards
 */
public class NotEmptyConceptDataValidator implements GetConceptDataValidationStrategy {

    private final I_GetConceptData requiredConcept;
    private final I_GetConceptData conceptToValidate;
    private final List<I_DescriptionVersioned> descriptions;

    public NotEmptyConceptDataValidator(final I_GetConceptData requiredType,
            final List<I_DescriptionVersioned> descriptions, final I_GetConceptData conceptToValidate)
            throws Exception {
        this.requiredConcept = requiredType;
        this.descriptions = descriptions;
        this.conceptToValidate = conceptToValidate;
    }

    /**
     * @see GetConceptDataValidationStrategy#validate()
     * @see NotEmptyConceptDataValidator
     */
    @Override
    public void validate() throws ValidationException {
        for (I_DescriptionVersioned description : descriptions) {
            for (I_DescriptionPart part : description.getVersions()) {
                if (isPartRequiredConceptType(requiredConcept, part) && !isHasValue(part)) {
                    throw new ValidationException(String.format("Concept %1$s has empty %2$s",
                            conceptToValidate.getConceptId(), requiredConcept.toString()));
                }
            }
        }
    }

    /**
     * Utility Method to check that an instance of {@link I_DescriptionPart} has a text value returned by the
     * {@link I_DescriptionPart#getText()} method that is not null and has a length greater than zero.
     * @param part I_DescriptionPart the {@code I_DescriptionPart} to check.
     * @return true - if the {@link I_DescriptionPart#getText()} method returns a value that is not null and has a
     * length greater than zero.<br><br>
     * false -if the {@link I_DescriptionPart#getText()} method returns a value that not
     * null or has a length of zero.
     */
    private boolean isHasValue(I_DescriptionPart part) {
        boolean isNotEmpty = true;

        if (part.getText() != null) {
            isNotEmpty = part.getText().length() > 0;
        } else {
            isNotEmpty = false;
        }

        return isNotEmpty;
    }

    /**
     * Utility Method to check whether a I_DescriptionPart type is of the required Concept Type.
     * @param requiredConceptType the {@code I_GetConceptData} that is required.
     * @param part the {@code I_DescriptionPart} to check
     * @return true/false whether the I_Description part type is the same as the {@code I_GetConceptData}
     */
    private boolean isPartRequiredConceptType(I_GetConceptData requiredConceptType, I_DescriptionPart part) {
        return part.getTypeId() == requiredConceptType.getConceptId();
    }
}
