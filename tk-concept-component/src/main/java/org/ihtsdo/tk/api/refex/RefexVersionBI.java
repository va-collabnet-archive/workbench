/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

// TODO: Auto-generated Javadoc
/**
 * The Interface RefexVersionBI.
 *
 * @param <A> the generic type
 */
public interface RefexVersionBI<A extends RefexAnalogBI<A>>
        extends ComponentVersionBI, RefexChronicleBI<A>, AnalogGeneratorBI<A>, Comparable<RefexVersionBI<A>> {

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ComponentVersionBI#makeBlueprint(org.ihtsdo.tk.api.coordinate.ViewCoordinate)
     */
    @Override
    RefexCAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException;

    /**
     * Gets the tk refset member active only.
     *
     * @param viewCoordinate the view coordinate
     * @param excludedNids the excluded nids
     * @param conversionMap the conversion map
     * @return the tk refset member active only
     * @throws ContradictionException the contradiction exception
     * @throws IOException signals that an I/O exception has occurred.
     */
    TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate viewCoordinate, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap)
            throws ContradictionException, IOException;

    /**
     * Refex fields equal.
     *
     * @param another the another
     * @return <code>true</code>, if successful
     */
    boolean refexFieldsEqual(RefexVersionBI another);
    
    
}
