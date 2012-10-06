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
 * The Class ConceptInactivationType contains
 * <code>ConceptSpec</code> representations of concepts describing concept
 * inactivation types. When a concept is retired one of these concepts is
 * associated to indicate the reason for retirement.
 */
public class ConceptInactivationType {

    /**
     * Represents the inactive concept type: ambiguous concept.
     */
    public static ConceptSpec AMBIGUOUS_CONCEPT =
            new ConceptSpec("Ambiguous concept (inactive concept)",
            UUID.fromString("5adbed85-55d8-3304-a404-4bebab660fff"));
    /**
     * Represents the inactive concept type: duplicate concept.
     */
    public static ConceptSpec DUPLICATE_CONCEPT =
            new ConceptSpec("Duplicate concept (inactive concept)",
            UUID.fromString("a5db42d4-6d94-33b7-92e7-d4a1d0f0d814"));
    /**
     * Represents the inactive concept type: erroneous concept.
     */
    public static ConceptSpec ERRONEOUS_CONCEPT =
            new ConceptSpec("Erroneous concept (inactive concept)",
            UUID.fromString("d4227098-db7a-331e-8f00-9d1e27626fc5"));
    /**
     * Represents the inactive concept type: limited status concept.
     */
    public static ConceptSpec LIMITED_STATUS_CONCEPT =
            new ConceptSpec("Limited status concept (inactive concept)",
            UUID.fromString("0c7b717a-3e41-372b-be57-621befb9b3ee"));
    /**
     * Represents the inactive concept type: moved elsewhere.
     */
    public static ConceptSpec MOVED_ELSEWHERE =
            new ConceptSpec("Moved elsewhere (inactive concept)",
            UUID.fromString("e730d11f-e155-3482-a423-9637db3bc1a2"));
    /**
     * Represents the inactive concept type: outdated concept.
     */
    public static ConceptSpec OUTDATED_CONCEPT =
            new ConceptSpec("Outdated concept (inactive concept)",
            UUID.fromString("d8a42cc5-05dd-3fcf-a1f7-62856e38874a"));
    /**
     * Represents the inactive concept type: reason not stated concept.
     */
    public static ConceptSpec REASON_NOT_STATED_CONCEPT =
            new ConceptSpec("Reason not stated concept (inactive concept)",
            UUID.fromString("a0db7e17-c6b2-3acc-811d-8a523274e869"));
}
