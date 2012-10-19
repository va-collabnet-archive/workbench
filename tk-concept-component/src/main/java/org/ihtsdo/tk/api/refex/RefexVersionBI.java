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
package org.ihtsdo.tk.api.refex;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Map;
import java.util.UUID;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;

/**
 * The Interface RefexVersionBI provides methods for interacting with, or
 * creating a version of, a refex.
 *
 * @param <A> the type of object returned by the analog generator
 * @see ComponentVersionBI
 */
public interface RefexVersionBI<A extends RefexAnalogBI<A>>
        extends ComponentVersionBI, RefexChronicleBI<A>, AnalogGeneratorBI<A>, Comparable<RefexVersionBI<A>> {

    /**
     * @param viewCoordinate the view coordinate specifying which version of the
     * description to make a blueprint of
     * @return the refex blueprint, which can be constructed to create a <code>RefexChronicleBI</code>
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of the
     * description was returned for the specified view coordinate
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
     */
    @Override
    RefexCAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Gets the active version of this refex version in the form of a TK Refex Member.
     *
     * @param viewCoordinate the view coordinate specifying which versions are active and inactive
     * @param excludedNids the nids representing the components to exclude
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @return an active TK Refex Member
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     * @throws IOException signals that an I/O exception has occurred
     */
    TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate viewCoordinate, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap)
            throws ContradictionException, IOException;

    /**
     * Compares the fields of this refex with the fields of
     * <code>another</code> refex.
     *
     * @param another the other refex for comparison
     * @return <code>true</code>, the fields are equal
     */
    boolean refexFieldsEqual(RefexVersionBI another);
}
