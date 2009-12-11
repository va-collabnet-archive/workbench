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
 * Concept constants file - used to statically reference required concepts from
 * the terminology
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
    public static final ConceptSpec RELATIONSHIP_REFINABILITY_EXTENSION =
            new ConceptSpec("Relationship refinability reference set",
                "a69353f3-fcb2-488f-ad00-84152672ef49");
    public static final ConceptSpec MODIFIER_SOME =
            new ConceptSpec("Some (core meta-data concept)",
                "082b9bd6-0e3d-4f8d-8d22-85e956feeaef");
    public static final ConceptSpec DEFINITION_TYPE_EXTENSION =
            new ConceptSpec("Definition status reference set",
                "6e88ee62-3fa0-495f-945f-809723a78c4b");
    public static final ConceptSpec UNIVERSALLY_UNIQUE_IDENTIFIER =
            new ConceptSpec(
                "Universally Unique Identifier (foundation meta-data concept)",
                "689572c2-103c-4892-aaad-c30ffa6b4b1c");
    public static final ConceptSpec SNOMED_ID_MAP_EXTENSION =
            new ConceptSpec(
                "SNOMED RT identifier map (foundation meta-data concept)",
                "ef010cf1-cf06-4c8a-9684-a040e61b319d");
    public static final ConceptSpec CTV3_ID_MAP_EXTENSION =
            new ConceptSpec("CTV3 map (foundation meta-data concept)",
                "6f1e56b5-c127-4f0b-97fa-cb72c76ad58a");
    public static final ConceptSpec REFSET_DESCRIPTOR_TYPE =
            new ConceptSpec(
                "Reference set descriptor (foundation meta-data concept)",
                "38bc0215-859d-4d78-9a84-ab76a932b780");
    public static final ConceptSpec CONCEPT_INACTIVATION_INDICATOR =
            new ConceptSpec(
                "Concept inactivation indicator (foundation meta-data concept)",
                "f8834d2f-4e2d-4793-a9e0-5190391ad277");
    public static final ConceptSpec DESCRIPTION_INACTIVATION_INDICATOR =
            new ConceptSpec(
                "Description inactivation indicator (foundation meta-data concept)",
                "69eb6cad-441a-456e-93da-78520ba68a29");
    public static final ConceptSpec RELATIONSHIP_INACTIVATION_INDICATOR =
            new ConceptSpec(
                "Relationship inactivation indicator (foundation meta-data concept)",
                "28857a70-98f3-4698-8f10-42bf0a24402a");

}
