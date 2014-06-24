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

import java.io.IOException;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TypedComponentVersionBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 * The Interface RelationshipVersionBI provides methods for interacting with and
 * creating versions of a relationship.
 *
 * @param <A> the type of object returned by the analog generator
 * @see ComponentVersionBI
 */
public interface RelationshipVersionBI<A extends RelationshipAnalogBI>
        extends TypedComponentVersionBI,
        RelationshipChronicleBI,
        AnalogGeneratorBI<A> {

    /**
     * Gets nid specifying the refinability type of this relationship version.
     *
     * @return the nid associated with the refinability type
     */
    public int getRefinabilityNid();

    /**
     * Gets nid specifying the characteristic type of this relationship version.
     *
     * @return the nid associated with the characteristic type
     */
    public int getCharacteristicNid();

    /**
     * Gets the group number associated with this relationship version.
     *
     * @return the group number associated with this relationship version
     */
    public int getGroup();

    /**
     * Checks if this relationship version is inferred.
     *
     * @return <code>true</code>, if the relationship version is inferred
     */
    public boolean isInferred() throws IOException;

    /**
     * Checks if this relationship version is stated.
     *
     * @return <code>true</code>, if the relationship version is stated
     */
    public boolean isStated() throws IOException;

    /**
     * @param viewCoordinate the view coordinate specifying which version of the
     * relationship to make a blueprint of
     * @return the relationship blueprint, which can be constructed to create
     * a <code>RelationshipChronicleBI</code>
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of the
     * description was returned for the specified view coordinate
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
     */
    @Override
    public RelationshipCAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;
}
