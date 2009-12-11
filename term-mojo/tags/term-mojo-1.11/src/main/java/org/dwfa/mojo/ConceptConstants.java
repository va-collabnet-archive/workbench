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
package org.dwfa.mojo;

import org.dwfa.tapi.spec.ConceptSpec;

/**
 * Concept constants file - used to statically reference required concepts from the terminology
 * 
 * @author Dion McMurtrie
 */
public class ConceptConstants {

    public static final ConceptSpec GENERATES_REL =
            new ConceptSpec("generates", "ca77a82c-ffa8-4dc1-8d31-6b137b4607ca");
    public static final ConceptSpec CREATES_MEMBERSHIP_TYPE =
            new ConceptSpec("creates membership type",
                "adccc8cc-cfe0-4947-b2b2-2c493034ac3a");
    public static final ConceptSpec SNOMED_IS_A =
            new ConceptSpec("Is a (attribute)",
                "c93a30b9-ba77-3adb-a9b8-4589c9f8fb25");

}
