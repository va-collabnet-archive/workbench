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
package org.ihtsdo.tk.dto.concept.component.relationship;

/**
 * The Enum TkRelationshipType lists the possible relationship types.
 *
 */
public enum TkRelationshipType {

    /**
     * Stated hierarchy.
     */
    STATED_HIERARCHY,
    /**
     * Stated relationship.
     */
    STATED_ROLE,
    /**
     * Inferred hierarchy.
     */
    INFERRED_HIERARCY,
    /**
     * Inferred relationship.
     */
    INFERRED_ROLE,
    /**
     * Historic relationship.
     */
    HISTORIC,
    /**
     * Qualifier relationship.
     */
    QUALIFIER;
}
