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
package org.dwfa.ace.task.commit.validator;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_GetConceptData;

/**
 * {@code ConceptDataComparerStrategy} represents an interface to allow
 * implementations to provide the funcionality to
 * check whether a {@link I_DescripionPart} type is of the required Concept
 * Type. <br>
 * 
 * 
 * @author Matthew Edwards
 */
public interface ConceptDataComparerStrategy {

    /**
     * Method to check whether a I_DescriptionPart type is of the required
     * Concept Type.
     * Implementing subclasses will determine how to compare the required
     * concept to the description part.
     * 
     * @param requiredConceptType the {@code I_GetConceptData} that is required.
     * @param part the {@code I_DescriptionPart} to check
     * @return true/false whether the I_Description part type is the same as the
     *         {@code I_GetConceptData}
     */
    boolean isPartRequiredConceptType(I_GetConceptData requiredConceptType, I_DescriptionPart part);
}
