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
package org.dwfa.ace.task.commit.validator.impl;

import java.util.List;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.commit.validator.ConceptDataComparerStrategy;
import org.dwfa.ace.task.commit.validator.GetConceptDataValidationStrategy;
import org.dwfa.ace.task.commit.validator.ValidationException;

/**
 * {@code NotNumericConceptDataValidator} is an implementation of
 * {@link GetConceptDataValidationStrategy} that verifys a that the text of an
 * {@link I_DescriptionPart} is not a pureley numeric value. <br>
 * This is an implementation of the Strategy Pattern to allow interchanging of
 * {@link GetConceptDataValidationStrategy} objects to be used for different
 * types of validation. <br>
 * If an {@code I_GetConceptData} object has no descriptions, the validate
 * method will pass through and not return any
 * errors as there was not description parts to check.
 * 
 * @author Matthew Edwards
 */
public class NotNumericConceptDataValidator implements GetConceptDataValidationStrategy {

    private final I_GetConceptData requiredConcept;
    private final I_GetConceptData conceptToValidate;
    private final List<I_DescriptionVersioned> descriptions;
    private final ConceptDataComparerStrategy comparer;

    /**
     * Constructs and Instance of NotNumericConceptDataValidator with a default
     * Concept to DescriptionPart comparison
     * algorithm of type {@link ConceptTypeToDescriptionTypeComparer}
     * 
     * @param requiredConcept {@link I_GetConceptData} the concept to check
     * @param descriptions a List of {@link I_DescriptionVersioned} objects to
     *            iterate through to find the
     *            requiredConcept
     * @param conceptToValidate the current concept being validated.
     */
    public NotNumericConceptDataValidator(final I_GetConceptData requiredConcept,
            final List<I_DescriptionVersioned> descriptions, final I_GetConceptData conceptToValidate) {
        this(requiredConcept, conceptToValidate, descriptions, new ConceptTypeToDescriptionTypeComparer());
    }

    /**
     * Constructs and Instance of NotNumericConceptDataValidator.
     * 
     * @param requiredConcept {@link I_GetConceptData} the concept to check
     * @param descriptions a List of {@link I_DescriptionVersioned} objects to
     *            iterate through to find the
     *            requiredConcept
     * @param conceptToValidate the current concept being validated.
     * @param comparer the algorithm used to compare the requiredConcept to the
     *            Description parts of the {@link I_DescriptionVersioned}
     *            objects in the {@code descriptions} List.
     */
    public NotNumericConceptDataValidator(I_GetConceptData requiredConcept, I_GetConceptData conceptToValidate,
            List<I_DescriptionVersioned> descriptions, ConceptDataComparerStrategy comparer) {
        this.requiredConcept = requiredConcept;
        this.conceptToValidate = conceptToValidate;
        this.descriptions = descriptions;
        this.comparer = comparer;
    }

    public void validate() throws ValidationException {
        for (I_DescriptionVersioned description : descriptions) {
            for (I_DescriptionPart part : description.getMutableParts()) {
                if (comparer.isPartRequiredConceptType(requiredConcept, part) && isNumeric(part)) {
                    throw new ValidationException(String.format(" %2$s for concept %1$s has contains only numeric "
                        + "characters", conceptToValidate.getConceptNid(), requiredConcept.toString()));
                }
            }
        }
    }

    /**
     * Utility Method to return true if a Description part contains only Digit
     * Characters
     * 
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
}
