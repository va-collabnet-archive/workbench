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

import org.ihtsdo.tk.api.constraint.DescriptionConstraint;
import org.ihtsdo.tk.api.constraint.RelationshipConstraintIncoming;
import org.ihtsdo.tk.api.constraint.RelationshipConstraintOutgoing;

/**
 * The Class SnomedConstraints represents constraints that can be declared on
 * SNOMED concepts.
 * 
 * @see ConstraintBI
 */
public class SnomedConstraints {

    /**
     * The outgoing relationship must be of type finding site.
     */
    public static RelationshipConstraintOutgoing FINDING_SITE_OUT =
            SnomedRelationshipSpec.FINDING_SITE.getSourceRelationshipConstraint();
    /**
     * The incoming relationship must be of type finding site.
     */
    public static RelationshipConstraintIncoming FINDING_SITE_IN =
            SnomedRelationshipSpec.FINDING_SITE.getTargetRelationshipConstraint();
    /**
     * The fully specified name must have a semantic tag.
     */
    public static DescriptionConstraint FS_SEMANTIC_TAG =
            new DescriptionConstraint(Taxonomies.SNOMED,
            WbDescType.FULLY_SPECIFIED, "\\(.*\\)$");
}
