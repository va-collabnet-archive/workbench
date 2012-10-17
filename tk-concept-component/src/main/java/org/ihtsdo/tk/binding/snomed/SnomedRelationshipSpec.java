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

import org.ihtsdo.tk.spec.RelationshipSpec;

/**
 * The Class SnomedRelationshipSpec represents particular types of relationships
 * in a way that is verifiable and human-readable.
 */
public class SnomedRelationshipSpec {

    /**
     * Represents the relationship from a SNOMED concept of type finding site to the concept body structure.
     */
    public static RelationshipSpec FINDING_SITE =
            new RelationshipSpec(Taxonomies.SNOMED,
            SnomedRelationshipType.FINDING_SITE,
            Snomed.BODY_STRUCTURE);
}
