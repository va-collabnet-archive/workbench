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
package org.ihtsdo.tk.api.relationship;

import org.ihtsdo.tk.api.ComponentChronicleBI;

/**
 * The Interface RelationshipChronicleBI represents the collection of
 * relationship versions and contains methods specific to interacting with the
 * relationship as a whole.
 *
 * <p>In the relationship A is-a B, the concept A is a source and the concept B is
 * a target. The relationship A-B is an outgoing relationship on concept A and
 * an incoming relationship on concept B.
 *
 * @see ComponentChronicleBI
 */
public interface RelationshipChronicleBI extends ComponentChronicleBI<RelationshipVersionBI> {

    /**
     * Gets nid associated with the source concept for this relationship.
     *
     * @return the nid associated with the source concept
     */
    int getSourceNid();

    /**
     * Gets nid associated with the target concept for this relationship.
     *
     * @return the nid associated with the target concept
     */
    int getTargetNid();
}
