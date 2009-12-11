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
package org.dwfa.ace.refset;

import org.dwfa.tapi.spec.ConceptSpec;

/**
 * Concept constants file - used to statically reference required concepts from the terminology
 * 
 * @author Dion McMurtrie
 */
public class ConceptConstants {

    public static final ConceptSpec AU_CT_EDIT_PATH =
            new ConceptSpec("au-ct edit path",
                "c65b08ce-8512-52fa-be06-0844bd7310d6");
    public static final ConceptSpec GENERATES_REL =
            new ConceptSpec("generates", "ca77a82c-ffa8-4dc1-8d31-6b137b4607ca");
    public static final ConceptSpec CREATES_MEMBERSHIP_TYPE =
            new ConceptSpec("creates membership type",
                "adccc8cc-cfe0-4947-b2b2-2c493034ac3a");
    public static final ConceptSpec SNOMED_IS_A =
            new ConceptSpec("Is a (attribute)",
                "c93a30b9-ba77-3adb-a9b8-4589c9f8fb25");
    public static final ConceptSpec PARENT_MARKER =
            new ConceptSpec("marked parent member",
                "125f3d04-de17-490e-afec-1431c2a39e29");
    public static final ConceptSpec REFSET_TYPE_REL =
            new ConceptSpec("refset type rel",
                "f353db14-3b0d-3001-9f27-7d8b00f51a1c");
    public static final ConceptSpec REFSET_PURPOSE_REL =
            new ConceptSpec("refest purpose rel",
                "f60922c9-cb3d-3099-8960-1097d2c5afdc");
}
