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
 * The Class SnomedRelationshipType contains
 * <code>ConceptSpec</code> representations of concepts using in relationships
 * between SNOMED concepts.
 */
public class SnomedRelationshipType {

    /**
     * Represents the relationship type: finding site.
     */
    public static ConceptSpec FINDING_SITE =
            new ConceptSpec("Finding site",
            UUID.fromString("3a6d919d-6c25-3aae-9bc3-983ead83a928"));
    /**
     * Represents the relationship type: Is a.
     */
    public static ConceptSpec IS_A =
            new ConceptSpec("Is a (attribute)",
            UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
}
