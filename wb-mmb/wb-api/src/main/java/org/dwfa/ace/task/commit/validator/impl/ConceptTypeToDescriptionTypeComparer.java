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

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.commit.validator.ConceptDataComparerStrategy;

/**
 * {@code ConceptTypeToDescriptionTypeComparer} is an implementation of
 * {@link ConceptDataComparerStrategy} that provides the funcionality to match
 * whether a {@link I_DescripionPart} type is of the required Concept Type. <br>
 * This is an implementation of the Strategy Pattern to allow interchanging of
 * {@link ConceptDataComparerStrategy} objects to be used for different types of
 * comparisons. <br>
 * 
 * @author Matthew Edwards
 */
public final class ConceptTypeToDescriptionTypeComparer implements ConceptDataComparerStrategy {

    /**
     * This Implementation of {@link ConceptDataComparerStrategy} compares the
     * {@link I_GetConceptData} to the {@link I_DescriptionPart} by checking to
     * see if the int value from the {@link I_DescriptionPart#getTypeId()}
     * method is equal (==) to the int value from
     * {@link I_GetConceptData#getConceptId()}.
     * 
     * @see ConceptDataComparerStrategy#isPartRequiredConceptType(org.dwfa.ace.api.I_GetConceptData,
     *      org.dwfa.ace.api.I_DescriptionPart)
     */
    public boolean isPartRequiredConceptType(I_GetConceptData requiredConceptType, I_DescriptionPart part) {
        return part.getTypeId() == requiredConceptType.getConceptId();
    }
}
