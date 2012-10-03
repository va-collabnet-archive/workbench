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
package org.ihtsdo.tk.api.relationship.group;

import java.util.Collection;

import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

/**
 * The Interface RelationshipGroupChronicleBI represents the collection of
 * relationship group versions and contains methods specific to interacting with
 * the relationship group as a whole. <br> <p>Relationship groups are specified
 * using an integer for the group number. A relationship that is not grouped
 * will have a group number of 0. The number assigned to a relationships is
 * meaningless other than to indicate that other relationships which have the
 * same number are part of the same group.
 */
public interface RelationshipGroupChronicleBI extends ComponentChronicleBI<RelationshipGroupVersionBI> {

    /**
     * Gets the relationships found in this relationship group.
     *
     * @return the relationships that are a part of this relationship group 
     */
    public Collection<? extends RelationshipChronicleBI> getRelationships();

    /**
     * Gets the relationship group number.
     *
     * @return the relationship group number
     */
    public int getRelationshipGroupNumber();
}
