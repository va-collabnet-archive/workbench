/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.binding.snomed;

import java.util.UUID;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Class SnomedMetadataRf1 contains
 * <code>ConceptSpec</code> representations of SNOMED Metadata which conform to
 * the Release Format 1.
 *
 */
public class SnomedMetadataRf1 {

    /**
     * Represents the SNOMED RF1 concept: fully specified name.
     */
    // SCT ID: none.
    // SCT Enum: DescriptionType 3
    public static ConceptSpec FULLY_SPECIFIED_DESCRIPTION_TYPE =
            new ConceptSpec("RF1 fully specified name (description type)",
            UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66"));
    // SCT ID: none.
    /**
     * Represents the SNOMED RF1 concept: synonym.
     */
    public static ConceptSpec SYNOMYM_DESCRIPTION_TYPE_RF1 =
            new ConceptSpec("RF1 synonym (description type)",
            UUID.fromString("d6fad981-7df6-3388-94d8-238cc0465a79"));
    /**
     * Represents the SNOMED RF1 concept: preferred term.
     */
    // SCT ID: none.
    // SCT Enum: DescriptionType 0 -- Unspecified
    // SCT Enum: DescriptionType 1 -- Preferred
    // SCT ID: none.
    public static ConceptSpec PREFERRED_TERM_DESCRIPTION_TYPE_RF1 =
            new ConceptSpec("RF1 preferred term (description type)",
            UUID.fromString("d8e3b37d-7c11-33ef-b1d0-8769e2264d44"));
    /**
     * Represents the SNOMED RF1 concept: preferred acceptability.
     */
    public static ConceptSpec PREFERRED_ACCEPTABILITY_RF1 =
            new ConceptSpec("preferred acceptability",
            UUID.fromString("15877c09-60d7-3464-bed8-635a98a7e5b2"));
    // SCT ID: none.
    // SCT Enum: DescriptionType 2 -- Acceptible
    /**
     * Represents the SNOMED RF1 concept: acceptable.
     */
    public static ConceptSpec ACCEPTABLE_DESCRIPTION_TYPE_RF1 =
            new ConceptSpec("acceptable",
            UUID.fromString("51b45763-09c4-34eb-a303-062ba8e0c0e9"));
    /**
     * Represents the SNOMED RF1 concept: US Language Refset.
     */
    // US language 100033
    public static ConceptSpec US_LANGUAGE_REFSET_RF1 =
            new ConceptSpec("US English Dialect Subset",
            UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6"));
    // GB language 101032
    /**
     * Represents the SNOMED RF1 concept: GB Language Refset.
     */
    public static ConceptSpec GB_LANGUAGE_REFSET_RF1 =
            new ConceptSpec("GB English Dialect Subset",
            UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd"));

    // Case significant 1
    // Case nosignificant 0
    /**
     * Represents the SNOMED RF1 concept: primitive.
     */
    // primitive 1
    public static ConceptSpec PRIMITIVE_RF1 =
            new ConceptSpec("primitive",
            UUID.fromString("cb834b1d-940e-31ba-8c43-e7fe78f38d93"));
    // defined 0
    /**
     * Represents the SNOMED RF1 concept: defined.
     */
    public static ConceptSpec DEFINED_RF1 =
            new ConceptSpec("defined",
            UUID.fromString("73dde8a7-aeb5-3af5-8db9-7aa60e6726a7"));

    // ICD9 100046
//    public static ConceptSpec NAME_RF1 =
//            new ConceptSpec("",
//            UUID.fromString(""));
    // ICDO 102041
//    public static ConceptSpec NAME_RF1 =
//            new ConceptSpec("",
//            UUID.fromString(""));
    /**
     * Represents the SNOMED RF1 concept: not refinable.
     */
    // notRefinable 0
    public static ConceptSpec NOT_REFINABLE_REFINABILITY_TYPE_RF1 =
            new ConceptSpec("not refinable (refinability type)",
            UUID.fromString("e4cde443-8fb6-11db-b606-0800200c9a66"));
    // optional 1
    /**
     * Represents the SNOMED RF1 concept: optional.
     */
    public static ConceptSpec OPTIONAL_REFINABILITY_TYPE_RF1 =
            new ConceptSpec("optional (refinability type)",
            UUID.fromString("c3d997d3-b0a4-31f8-846f-03fa874f5479"));
    // mandatory 2
    /**
     * Represents the SNOMED RF1 concept: mandatory.
     */
    public static ConceptSpec MANDATORY_REFINABILITY_TYPE_RF1 =
            new ConceptSpec("mandatory (refinability type)",
            UUID.fromString("3f2cec85-be64-339e-ba99-4a75f53bc51c"));
    /**
     * Represents the SNOMED RF1 concept: additional.
     */
    // Additional 3
    public static ConceptSpec ADDITIONAL_CHARACTERISTIC_TYPE_RF1 =
            new ConceptSpec("additional (characteristic type)",
            UUID.fromString("66f4785e-92e9-3d1c-ae3b-f1632b52b111"));
    // Defining 0
    /**
     * Represents the SNOMED RF1 concept: defining.
     */
    public static ConceptSpec DEFINING_CHARACTERISTIC_TYPE_RF1 =
            new ConceptSpec("defining (characteristic type)",
            UUID.fromString("a4c6bf72-8fb6-11db-b606-0800200c9a66"));
    // Inferred 0
    /**
     * Represents the SNOMED RF1 concept: inferred.
     */
    public static ConceptSpec INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1 =
            new ConceptSpec("inferred (defining characteristic type)",
            UUID.fromString("d8fb4fb0-18c3-3352-9431-4919193f85bc"));
    //
    // Stated 0
    /**
     * Represents the SNOMED RF1 concept: stated.
     */
    public static ConceptSpec STATED_DEFINING_CHARACTERISTIC_TYPE_RF1 =
            new ConceptSpec("stated (defining characteristic type)",
            UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d"));
    // Qualifying 1
    /**
     * Represents the SNOMED RF1 concept: qualifier.
     */
    public static ConceptSpec QUALIFIER_CHARACTERISTICS_TYPE_RF1 =
            new ConceptSpec("qualifier (characteristic type)",
            UUID.fromString("416ad0e4-b6bc-386c-900e-121c58b20f55"));
    // Historical 2
    /**
     * Represents the SNOMED RF1 concept: historical.
     */
    public static ConceptSpec HISTORICAL_CHARACTERISTIC_TYPE_RF1 =
            new ConceptSpec("historical (characteristic type)",
            UUID.fromString("1d054ca3-2b32-3004-b7af-2701276059d5"));
    /**
     * Represents the SNOMED RF1 concept: limited.
     */
    // Limited 6
    public static ConceptSpec LIMITED_ACTIVE_STATUS_RF1 =
            new ConceptSpec("limited (active status type)",
            UUID.fromString("4bc081d8-9f64-3a89-a668-d11ca031979b"));
    // Duplicate 2
    /**
     * Represents the SNOMED RF1 concept: duplicate.
     */
    public static ConceptSpec DUPLICATE_INACTIVE_STATUS_RF1 =
            new ConceptSpec("duplicate (inactive status type)",
            UUID.fromString("cbe19851-49f7-32f7-bb27-2d24be0e77e8"));
    // Pending_move 11
    /**
     * Represents the SNOMED RF1 concept: pending move.
     */
    public static ConceptSpec PENDING_MOVE_ACTIVE_STATUS_RF1 =
            new ConceptSpec("pending move (active status type)",
            UUID.fromString("11c24184-4d8a-3cd3-bc30-bb0aa4c76e93"));
    // Ambiguous 4
    /**
     * Represents the SNOMED RF1 concept: ambiguous.
     */
    public static ConceptSpec AMBIGUOUS_INACTIVE_STATUS_RF1 =
            new ConceptSpec("ambiguous (inactive status type)",
            UUID.fromString("3b397a0a-b510-391b-bd2e-5dd168c092ba"));
    // Moved_elsewhere 10
    /**
     * Represents the SNOMED RF1 concept: moved elsewhere.
     */
    public static ConceptSpec MOVED_ELSEWHERE_INACTIVE_STATUS_RF1 =
            new ConceptSpec("moved elsewhere (inactive status type)",
            UUID.fromString("76367831-522f-3250-83a4-8609ab298436"));
    // Concept_non-current 8
    /**
     * Represents the SNOMED RF1 concept: concept retired.
     */
    public static ConceptSpec CONCEPT_RETIRED_ACTIVE_STATUS_RF1 =
            new ConceptSpec("concept retired (active status type)",
            UUID.fromString("181e45e8-b05a-33da-8b52-7027cbee6856"));
    // Outdated 3
    /**
     * Represents the SNOMED RF1 concept: outdated.
     */
    public static ConceptSpec OUTDATED_INACTIVE_STATUS_RF1 =
            new ConceptSpec("outdated (inactive status type)",
            UUID.fromString("ad6b6532-0cb7-35d0-be04-47a9e4634ed8"));
    // Inappropriate 7
    /**
     * Represents the SNOMED RF1 concept: inappropriate.
     */
    public static ConceptSpec INAPPROPRIATE_INACTIVE_STATUS_RF1 =
            new ConceptSpec("inappropriate (inactive status type)",
            UUID.fromString("ab6d6b36-a00a-3978-b279-872420ef7b44"));
    // Erroneous 5
    /**
     * Represents the SNOMED RF1 concept: erroneous.
     */
    public static ConceptSpec ERRONEOUS_INACTIVE_STATUS_RF1 =
            new ConceptSpec("erroneous (inactive status type)",
            UUID.fromString("a09cc39f-2c01-3563-84cc-bad7d7fb597f"));
    // reasonNotStated 1
    /**
     * Represents the SNOMED RF1 concept: retired.
     */
    public static ConceptSpec RETIRED_INACTIVE_STATUS_RF1 =
            new ConceptSpec("retired (inactive status type)",
            UUID.fromString("e1956e7b-08b4-3ad0-ab02-b411869f1c09"));
    // Current 0
    // SCT Enum: ConceptStatus 0
    // SCT Enum: DescriptionStatus 0
    /**
     * Represents the SNOMED RF1 concept: current.
     */
    public static ConceptSpec CURRENT_RF1 =
            new ConceptSpec("current (active status type)",
            UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66"));
    /**
     * Represents the SNOMED RF1 concept: Part of.
     */
    // Part_Of (Refset) 123005000
    public static ConceptSpec PART_OF_ATTRIBUTE_REFSET =
            new ConceptSpec("Part of (attribute)",
            UUID.fromString("b4c3f6f9-6937-30fd-8412-d0c77f8a7f73"));
    // May_Be (Refset) 149016008
    /**
     * Represents the SNOMED RF1 concept: MAY BE A.
     */
    public static ConceptSpec MAY_BE_A_ATTRIBUTE_RF1 =
            new ConceptSpec("MAY BE A (attribute)",
            UUID.fromString("721dadc2-53a0-3ffa-8abd-80ff6aa87db2"));
    // Was_A (Refset) 159083000
    /**
     * Represents the SNOMED RF1 concept: WAS A.
     */
    public static ConceptSpec WAS_A_ATTRIBUTE_RF1 =
            new ConceptSpec("WAS A (attribute)",
            UUID.fromString("a1a598c0-7988-3c8e-9ba2-342f24de7c6b"));
    // Same_As (Refset) 168666000
    /**
     * Represents the SNOMED RF1 concept: SAME AS.
     */
    public static ConceptSpec SAME_AS_ATTRIBUTE_RF1 =
            new ConceptSpec("SAME AS (attribute)",
            UUID.fromString("87594159-50f0-3b5f-aa4f-f6061c0ce497"));
    // Replaced_By (Refset) 370124000
    /**
     * Represents the SNOMED RF1 concept: REPLACED BY.
     */
    public static ConceptSpec REPLACED_BY_ATTRIBUTE_RF1 =
            new ConceptSpec("REPLACED BY (attribute)",
            UUID.fromString("0b010f24-523b-3ae4-b3a2-ec1f425c8a85"));
    // Moved_To (Refset) 370125004
    /**
     * Represents the SNOMED RF1 concept: MOVED TO.
     */
    public static ConceptSpec MOVED_TO_RF1 =
            new ConceptSpec("MOVED TO (attribute)",
            UUID.fromString("c3394436-568c-327a-9d20-4a258d65a936"));
    // Moved_From (Refset) 384598002
    /**
     * Represents the SNOMED RF1 concept: MOVED FROM.
     */
    public static ConceptSpec MOVED_FROM_RF1 =
            new ConceptSpec("MOVED FROM (attribute)",
            UUID.fromString("9ceab5fa-7b4c-3618-b229-6997dc69ad65"));
    /**
     * Represents the SNOMED RF1 concept: Degree of Synonymy Refset.
     */
    public static ConceptSpec DEGREE_OF_SYNONYMY_REFSET_RF1 =
            new ConceptSpec("Degree of Synonymy Refset",
            UUID.fromString("a8dd0021-4994-36b2-a0f5-567b7e007847"));
    /**
     * Represents the SNOMED RF1 concept: Refers To Refset.
     */
    public static ConceptSpec REFERS_TO_REFSET_RF1 =
            new ConceptSpec("Refers To Refset",
            UUID.fromString("1b122b8f-172f-53d5-a2e2-eb1161737c2a"));
    /**
     * Represents the SNOMED RF1 concept: VMP subset.
     */
    public static ConceptSpec VMP_RF1 =
            new ConceptSpec("VMP subset",
            UUID.fromString("d085216e-e34d-52e8-9785-d8af93939f99"));
    /**
     * Represents the SNOMED RF1 concept: VTM subset.
     */
    public static ConceptSpec VTM_RF1 =
            new ConceptSpec("VTM subset",
            UUID.fromString("e65ea362-72d4-3641-bfba-fe4429eea6f9"));
    /**
     * Represents the SNOMED RF1 concept: Non-human Subset.
     */
    public static ConceptSpec NON_HUMAN_RF1 =
            new ConceptSpec("Non-human Subset",
            UUID.fromString("0e2687b7-db28-5a01-b968-b98865648f2b"));
    /* References */
    // Replaced_By 1
//    public static ConceptSpec NAME_RF1 =
//            new ConceptSpec("",
//            UUID.fromString("32429976-257c-5013-82ee-599404b55cc6"));
    // Alternative 4
//    public static ConceptSpec NAME_RF1 =
//            new ConceptSpec("",
//            UUID.fromString("e0674a75-74b3-5a6b-8db4-7c464f7aeaa0"));
    // Refers_To 7
//    public static ConceptSpec NAME_RF1 =
//            new ConceptSpec("",
//            UUID.fromString("1b122b8f-172f-53d5-a2e2-eb1161737c2a"));
}
