/*
 *  Copyright 2009 matt.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.dwfa.ace.task.commit.validator.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.commit.validator.GetConceptDataValidationStrategy;
import org.dwfa.ace.task.commit.validator.ValidationException;

/**
 *
 * @author matt
 */
public class NotNumericConceptDataValidator implements GetConceptDataValidationStrategy {

    private final I_GetConceptData requiredConcept;
    private final I_GetConceptData conceptToValidate;
    private final List<I_DescriptionVersioned> descriptions;
    private static final String NOT_DIGIT_REGEX_PATTERN = "\\D";

    public NotNumericConceptDataValidator(final I_GetConceptData requiredType,
            final List<I_DescriptionVersioned> descriptions, final I_GetConceptData conceptToValidate) {
        this.requiredConcept = requiredType;
        this.descriptions = descriptions;
        this.conceptToValidate = conceptToValidate;
    }

    public void validate() throws ValidationException {
        for (I_DescriptionVersioned description : descriptions) {
            for (I_DescriptionPart part : description.getVersions()) {
                if (isPartRequiredConceptType(requiredConcept, part) && isNumeric(part)) {
                    throw new ValidationException(String.format(" %2$s for concept %1$s has contains only numeric " +
                            "characters", conceptToValidate.getConceptId(), requiredConcept.toString()));
                }
            }
        }
    }

    /**
     * Utility Method to return true if a Description part contains only Digit Characters
     * @param part
     * @return
     */
    private boolean isNumeric(I_DescriptionPart part) {
        if (part.getText() == null) {
            return false;
        }
        try {
            Long.parseLong(part.getText());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }

    }

    /**
     * Utility Method to check whether a I_DescriptionPart type is of the required Concept Type.
     * @param requiredConceptType the {@code I_GetConceptData} that is required.
     * @param part the {@code I_DescriptionPart} to check
     * @return true/false whether the I_Description part type is the same as the {@code I_GetConceptData}
     */
    public static boolean isPartRequiredConceptType(I_GetConceptData requiredConceptType, I_DescriptionPart part) {
        return part.getTypeId() == requiredConceptType.getConceptId();
    }
}
