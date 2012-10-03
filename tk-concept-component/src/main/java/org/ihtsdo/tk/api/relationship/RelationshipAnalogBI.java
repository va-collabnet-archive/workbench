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

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.TypedComponentAnalogBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupChronicleBI;

/**
 * The Interface RelationshipAnalogBI contains methods for editing a
 * relationship analog. The preferred method of editing terminology is through a
 * blueprint.
 *
 * @param <A> the type of object returned by the analog generator
 * @see AnalogBI
 * @eee CreateOrAmendBlueprint
 */
public interface RelationshipAnalogBI<A extends RelationshipAnalogBI>
        extends TypedComponentAnalogBI, RelationshipVersionBI<A> {

    /**
     * Sets the target concept for this relationship based on the specified
     * <code>targetNid</code>.
     *
     * @param targetNid the nid associated with the target concept
     * @throws PropertyVetoException if the new value is not valid
     */
    public void setTargetNid(int targetNid) throws PropertyVetoException;

    /**
     * Sets the refinability of this relationship based on the specified
     * </code>refinabilityNid</code>.
     *
     * @param refinabilityNid the nid associated with the refinability type
     * @throws PropertyVetoException if the new value is not valid
     */
    public void setRefinabilityNid(int refinabilityNid) throws PropertyVetoException;

    /**
     * Sets the characteristic type of the relationship based on the specified
     * <code>characteristicNid</code>.
     *
     * @param characteristicNid the nid associated with the characteristic type
     * @throws PropertyVetoException if the new value is not valid
     */
    public void setCharacteristicNid(int characteristicNid) throws PropertyVetoException;

    /**
     * Sets the group number associated with this relationship. If the
     * relationship is not in a group this should be 0. To group this
     * relationship with an existing relationship group, set this number to
     * match the number of the desired group.
     * 
     *
     * @param group the new group
     * @throws PropertyVetoException if the new value is not valid
     * @see RelationshipGroupChronicleBI
     */
    public void setGroup(int group) throws PropertyVetoException;
}
